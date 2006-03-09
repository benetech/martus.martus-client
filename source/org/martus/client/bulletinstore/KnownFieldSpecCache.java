/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2006, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/

package org.martus.client.bulletinstore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletinstore.BulletinStoreCache;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.crypto.MartusCrypto.DecryptionException;
import org.martus.common.crypto.MartusCrypto.NoKeyPairException;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.Packet;
import org.martus.common.packet.UniversalId;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.Packet.WrongPacketTypeException;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;

public class KnownFieldSpecCache extends BulletinStoreCache implements ReadableDatabase.PacketVisitor
{
	public KnownFieldSpecCache(ReadableDatabase databaseToUse, MartusCrypto securityToUse)
	{
		db = databaseToUse;
		security = securityToUse;
	}
	
	public void restoreCacheFromSavedState()
	{
		// FIXME: Need to do this for real
		initializeFromDatabase();
	}
	
	public void initializeFromDatabase()
	{
		createMap();
		db.visitAllRecords(this);
	}

	private void createMap()
	{
		accountsToMapsOfLocalIdsToSetsOfSpecs = new HashMap();
	}
	
	public void storeWasCleared()
	{
		clear();
	}

	private void clear()
	{
		accountsToMapsOfLocalIdsToSetsOfSpecs.clear();
	}

	public void revisionWasSaved(UniversalId uid)
	{
		DatabaseKey key = findKey(db, uid);
		if(key == null)
			return;
		addFieldSpecsFromBulletin(key);
	}
	
	public void revisionWasSaved(Bulletin b)
	{
		Set publicAndPrivateSpecs = new HashSet();
		publicAndPrivateSpecs.addAll(arrayToSet(b.getPublicFieldSpecs()));
		publicAndPrivateSpecs.addAll(arrayToSet(b.getPrivateFieldSpecs()));
		setSpecs(b.getUniversalId(), publicAndPrivateSpecs);
	}

	public void revisionWasRemoved(UniversalId uid)
	{
		String accountId = uid.getAccountId();
		Map mapForAccount = getMapForAccount(accountId);
		mapForAccount.remove(uid.getLocalId());
		if(mapForAccount.size() == 0)
			accountsToMapsOfLocalIdsToSetsOfSpecs.remove(accountId);
	}
	
	public Set getAllKnownFieldSpecs()
	{
		Set knownSpecs = new HashSet();
		Collection specsForAllAccounts = accountsToMapsOfLocalIdsToSetsOfSpecs.values();
		Iterator iter = specsForAllAccounts.iterator();
		while(iter.hasNext())
		{
			Map specsForOneAccount = (Map)iter.next();
			knownSpecs.addAll(getSpecsForAccount(specsForOneAccount));
		}
			
		return knownSpecs;
	}
	
	public void saveToStream(OutputStream out) throws Exception
	{
		byte[] plainBytes = getCacheAsBytes();
		byte[] bundle = security.createSignedBundle(plainBytes);
		DataOutputStream dataOut = new DataOutputStream(out);
		try
		{
			dataOut.writeInt(bundle.length);
			dataOut.write(bundle);
		}
		finally
		{
			dataOut.close();
		}
		
	}

	private byte[] getCacheAsBytes() throws IOException
	{
		ByteArrayOutputStream plain = new ByteArrayOutputStream();
		DataOutputStream dataOut = new DataOutputStream(plain);
		try
		{
			dataOut.writeByte(FILE_VERSION);
			writeAllAccountsData(dataOut);
		}
		finally
		{
			dataOut.close();
		}
		return plain.toByteArray();
	}

	private void writeAllAccountsData(DataOutputStream dataOut) throws IOException
	{
		Collection accountIds = accountsToMapsOfLocalIdsToSetsOfSpecs.keySet();
		Iterator iter = accountIds.iterator();
		while(iter.hasNext())
		{
			String accountId = (String)iter.next();
			dataOut.writeUTF(accountId);
			writeOneAccountsData(dataOut, accountId);
		}
		dataOut.writeUTF("");
	}
	
	private void writeOneAccountsData(DataOutputStream dataOut, String accountId) throws IOException
	{
		Map localIdsToSetOfSpecs = (Map)accountsToMapsOfLocalIdsToSetsOfSpecs.get(accountId);
		Collection localIds = localIdsToSetOfSpecs.keySet();
		Iterator iter = localIds.iterator();
		while(iter.hasNext())
		{
			String localId = (String)iter.next();
			dataOut.writeUTF(localId);
			Set specs = (Set)localIdsToSetOfSpecs.get(localId);
			writeSpecs(dataOut, specs);
		}
		dataOut.writeUTF("");
	}
	
	private void writeSpecs(DataOutputStream dataOut, Set specs) throws IOException
	{
		Iterator iter = specs.iterator();
		while(iter.hasNext())
		{
			FieldSpec spec = (FieldSpec)iter.next();
			dataOut.writeUTF(spec.toString());
		}
		dataOut.writeUTF("");
	}
	
	public void loadFromStream(InputStream in) throws Exception
	{
		DataInputStream dataIn = new DataInputStream(in);
		try
		{
			int length = dataIn.readInt();
			byte[] encrypted = new byte[length];
			dataIn.read(encrypted);
			byte[] plain = security.extractFromSignedBundle(encrypted);
			loadFromBytes(plain);
		}
		finally
		{
			dataIn.close();
		}
	}
	
	private void loadFromBytes(byte[] cacheAsBytes) throws Exception
	{
		ByteArrayInputStream in = new ByteArrayInputStream(cacheAsBytes);
		createMap();
		DataInputStream dataIn = new DataInputStream(in);
		try
		{
			if(dataIn.readByte() != FILE_VERSION)
				throw new IOException("Bad version of field spec cache file");
			readAllAccountsData(dataIn);
		}
		finally
		{
			dataIn.close();
		}
		
	}
	
	private void readAllAccountsData(DataInputStream dataIn) throws Exception
	{
		while(true)
		{
			String accountId = dataIn.readUTF();
			if(accountId.length() == 0)
				break;
			Map localIdsToVectorsOfSpecs = new HashMap();
			accountsToMapsOfLocalIdsToSetsOfSpecs.put(accountId, localIdsToVectorsOfSpecs);
			readOneAccountsData(dataIn, localIdsToVectorsOfSpecs);
		}
	}
	
	private void readOneAccountsData(DataInputStream dataIn, Map localIdsToVectorsOfSpecs) throws Exception
	{
		while(true)
		{
			String localId = dataIn.readUTF();
			if(localId.length() == 0)
				break;
			Set specs = new HashSet();
			localIdsToVectorsOfSpecs.put(localId, specs);
			readSpecs(dataIn, specs);
		}
	}
	
	private void readSpecs(DataInputStream dataIn, Set specsToPopulate) throws Exception
	{
		while(true)
		{
			String specString = dataIn.readUTF();
			if(specString.length() == 0)
				break;
			FieldSpec spec = FieldSpec.createFromXml(specString);
			specsToPopulate.add(spec);
		}
	}

	private Set getSpecsForAccount(Map specsForOneAccount)
	{
		Set specsForThisAccount = new HashSet();
		
		Collection localIds = specsForOneAccount.keySet();
		Iterator outerIter = localIds.iterator();
		while(outerIter.hasNext())
		{
			String localId = (String)outerIter.next();
			Set specsForOneBulletin = (Set)specsForOneAccount.get(localId);
			specsForThisAccount.addAll(getSpecsForBulletin(specsForOneBulletin));
		}
		
		return specsForThisAccount;
	}

	private Set getSpecsForBulletin(Set specsForOneBulletin)
	{
		Set specsForThisBulletin = new HashSet();
		Iterator innerIter = specsForOneBulletin.iterator();
		while(innerIter.hasNext())
		{
			FieldSpec spec = (FieldSpec)innerIter.next();
			specsForThisBulletin.add(spec);
		}
		
		return specsForThisBulletin;
	}

	public void visit(DatabaseKey key)
	{
		if(!BulletinHeaderPacket.isValidLocalId(key.getLocalId()))
			return;
		addFieldSpecsFromBulletin(key);
		
	}

	private void addFieldSpecsFromBulletin(DatabaseKey bulletinHeaderKey)
	{
		try
		{
			BulletinHeaderPacket bhp = new BulletinHeaderPacket(BulletinHeaderPacket.createUniversalId(security));
			loadPacket(bhp, bulletinHeaderKey);
			String status = bhp.getStatus();
			String accountId = bhp.getAccountId();
			
			Set publicAndPrivateSpecs = new HashSet();
			try
			{
				String packetLocalId = bhp.getFieldDataPacketId();
				publicAndPrivateSpecs.addAll(loadFieldSpecsForPacket(accountId, packetLocalId, status));
			}
			catch(DecryptionException e)
			{
				// ignore because it's probably not our bulletin, and we certainly can't search it
			}

			try
			{
				String packetLocalId = bhp.getPrivateFieldDataPacketId();
				publicAndPrivateSpecs.addAll(loadFieldSpecsForPacket(accountId, packetLocalId, status));
			}
			catch(DecryptionException e)
			{
				// ignore because it's probably not our bulletin, and we certainly can't search it
			}

			setSpecs(bhp.getUniversalId(), publicAndPrivateSpecs);
		}
		catch(Exception e)
		{
			System.out.println("WARNING: Unable to read " + bulletinHeaderKey.getLocalId() + ": " + e);
			//e.printStackTrace();
			// we need to keep going anyway
			// TODO: Alert the user that there was some problem
		}
	}

	private Set loadFieldSpecsForPacket(String accountId, String packetLocalId, String status) throws IOException, CryptoException, InvalidPacketException, WrongPacketTypeException, SignatureVerificationException, DecryptionException, NoKeyPairException
	{
		UniversalId packetUid = UniversalId.createFromAccountAndLocalId(accountId, packetLocalId);
		FieldDataPacket fdp = loadFieldDataPacket(packetUid, status);
		Set specs = arrayToSet(fdp.getFieldSpecs());
		return specs;
	}

	private FieldDataPacket loadFieldDataPacket(UniversalId publicPacketUid, String status) throws IOException, CryptoException, InvalidPacketException, WrongPacketTypeException, SignatureVerificationException, DecryptionException, NoKeyPairException
	{
		DatabaseKey publicPacketKey = DatabaseKey.createKey(publicPacketUid, status);
		FieldDataPacket publicData = new FieldDataPacket(publicPacketKey.getUniversalId(), new FieldSpec[0]);
		loadPacket(publicData, publicPacketKey);
		return publicData;
	}

	private void loadPacket(Packet packet, DatabaseKey key) throws IOException, CryptoException, InvalidPacketException, WrongPacketTypeException, SignatureVerificationException, DecryptionException, NoKeyPairException
	{
		InputStreamWithSeek in = db.openInputStream(key, security);
		try
		{
			packet.loadFromXml(in, security);
		}
		finally
		{
			in.close();
		}
	}

	private void setSpecs(UniversalId bulletinUid, Set specs)
	{
		String accountId = bulletinUid.getAccountId();
		String localId = bulletinUid.getLocalId();

		Map specsForOneAccount = getMapForAccount(accountId);

		specsForOneAccount.put(localId, specs);
	}

	private Map getMapForAccount(String accountId)
	{
		Map specsForOneAccount = (Map)accountsToMapsOfLocalIdsToSetsOfSpecs.get(accountId);
		if(specsForOneAccount == null)
		{
			specsForOneAccount = new HashMap();
			accountsToMapsOfLocalIdsToSetsOfSpecs.put(accountId, specsForOneAccount);
		}
		return specsForOneAccount;
	}
	
	private Set arrayToSet(FieldSpec[] array)
	{
		Set vector = new HashSet();
		for(int i=0; i < array.length; ++i)
			vector.add(array[i]);
		return vector;
	}
	
	private static final int FILE_VERSION = 0;

	ReadableDatabase db;
	MartusCrypto security;
	Map accountsToMapsOfLocalIdsToSetsOfSpecs;
}
