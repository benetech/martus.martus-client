/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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
import java.util.Vector;

import org.martus.client.core.MartusLogger;
import org.martus.common.FieldSpecCollection;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletinstore.BulletinStoreCache;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.crypto.MartusCrypto.DecryptionException;
import org.martus.common.crypto.MartusCrypto.NoKeyPairException;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
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
	
	synchronized public void restoreCacheFromSavedState()
	{
		// FIXME: Need to do this for real
		initializeFromDatabase();
	}
	
	synchronized public void initializeFromDatabase()
	{
		try
		{
			saving = true;
			createMap();
			db.visitAllRecords(this);
		}
		finally
		{
			saving = false;
		}
	}

	private void createMap()
	{
		accountsToMapsOfLocalIdsToSetsOfSpecs = new HashMap();
	}
	
	synchronized public void storeWasCleared()
	{
		clear();
	}

	private void clear()
	{
		accountsToMapsOfLocalIdsToSetsOfSpecs.clear();
	}

	synchronized public void revisionWasSaved(UniversalId uid)
	{
		DatabaseKey key = findKey(db, uid);
		if(key == null)
			return;
		addFieldSpecsFromBulletin(key);
	}
	
	synchronized public void revisionWasSaved(Bulletin b)
	{
		FieldSpecCollection publicSpecs = new FieldSpecCollection(b.getTopSectionFieldSpecs());
		FieldSpecCollection privateSpecs = new FieldSpecCollection(b.getBottomSectionFieldSpecs());
		setSpecs(b.getUniversalId(), new FieldSpecCollection[] {publicSpecs, privateSpecs});
	}

	synchronized public void revisionWasRemoved(UniversalId uid)
	{
		String accountId = uid.getAccountId();
		Map mapForAccount = getMapForAccount(accountId);
		mapForAccount.remove(uid.getLocalId());
		if(mapForAccount.size() == 0)
			accountsToMapsOfLocalIdsToSetsOfSpecs.remove(accountId);
	}
	
	synchronized public Set getAllKnownFieldSpecs()
	{
		Set knownSpecs = new HashSet();
		Collection specsForAllAccounts = accountsToMapsOfLocalIdsToSetsOfSpecs.values();
		Iterator iter = specsForAllAccounts.iterator();
		while(iter.hasNext())
		{
			Map specsForOneAccount = (Map)iter.next();
			Set specsForAccount = getSpecsForAccount(specsForOneAccount);
			knownSpecs.addAll(specsForAccount);
		}
			
		return knownSpecs;
	}
	
	synchronized public void saveToStream(OutputStream out) throws Exception
	{
		DataOutputStream dataOut = new DataOutputStream(out);
		try
		{
			saving = true;
			MartusLogger.log("Inside KnownFieldSpecCache.saveToStream");
			byte[] plainBytes = getCacheAsBytes();
			byte[] bundle = security.createSignedBundle(plainBytes);
			dataOut.writeInt(bundle.length);
			dataOut.write(bundle);
		}
		finally
		{
			saving = false;
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
			PersistableMap fieldSpecCache = createFieldSpecCacheMap();
			fieldSpecCache.writeTo(dataOut);
		}
		finally
		{
			dataOut.close();
		}
		return plain.toByteArray();
	}
	
	/*
	 * Builds a JSON file with this structure:
	 *   TAG_VERSION: {version}
	 *   TAG_ALL_SECTION_SPECS: Array, one entry for each SpecCollection:
	 *   	Array, one entry for each Spec: Spec XML
	 *   TAG_SPEC_INDEXES_FOR_ALL_ACCOUNTS: Array, one entry for each Account:
	 *   	Account: Array, one entry for each LocalId:
	 *   		LocalId: Array of SpecCollection indexes: indexes
	 */
	
	public PersistableMap createFieldSpecCacheMap()
	{
		HashMap allSpecs = new HashMap();
		PersistableMap specIndexes = createSpecIndexesForAllBulletins(allSpecs);

		PersistableMap map = new PersistableMap();
		map.put(TAG_SPEC_INDEXES_FOR_ALL_ACCOUNTS, specIndexes);
		map.put(TAG_ALL_SECTION_SPECS, createSpecCollectionVector(allSpecs));
		return map;
	}
	
	PersistableVector createSpecCollectionVector(Map allSpecs)
	{
		System.out.println("createSpecCollectionVector, allSpecs.size() = " + allSpecs.size());
		PersistableVector vector = new PersistableVector();
		Iterator iter = allSpecs.keySet().iterator();
		while(iter.hasNext())
		{
			FieldSpecCollection fsc = (FieldSpecCollection)iter.next();
			PersistableInt index = (PersistableInt)allSpecs.get(fsc);
			vector.putAt(index.asInt(), createSpecCollectionVector(fsc));
		}
		
		return vector;
	}
	
	PersistableVector createSpecCollectionVector(FieldSpecCollection fsc)
	{
		PersistableVector vector = new PersistableVector();
		for(int i = 0; i < fsc.size(); ++i)
			vector.add(new PersistableString(fsc.get(i).toString()));
		return vector;
	}

	//	PersistableVector createSpecsCollection(FieldSpecCollection specs)
//	{
//		for(int spec = 0; spec < specs.size(); ++spec)
//			add(new PersistableString(specs.get(spec).toString()));
//	}
//		
//		public FieldSpecCollection getFieldSpecCollection() throws Exception
//		{
//			FieldSpecCollection specs = new FieldSpecCollection(length());
//			for(int spec = 0; spec < specs.size(); ++spec)
//				specs.set(spec, FieldSpec.createFromXml(getString(spec)));
//			
//			return specs;
//		}
//		
//	}
//	
	PersistableMap createSpecIndexesForAllBulletins(HashMap allSpecs)
	{
		PersistableMap map = new PersistableMap();
		
		Collection accountIds = accountsToMapsOfLocalIdsToSetsOfSpecs.keySet();
		Iterator iter = accountIds.iterator();
		while(iter.hasNext())
		{
			String accountId = (String)iter.next();
			PersistableMap specIndexesForAccount = createSpecIndexesForAccount(allSpecs, accountId);
			map.put(accountId, specIndexesForAccount);
		}
		
		return map;
	}
	
	PersistableMap createSpecIndexesForAccount(HashMap allSpecs, String accountId)
	{
		PersistableMap map = new PersistableMap();
		
		Map localIdsToSetOfSpecs = (Map)accountsToMapsOfLocalIdsToSetsOfSpecs.get(accountId);
		if(localIdsToSetOfSpecs == null)
			return map;
		
		Collection localIds = localIdsToSetOfSpecs.keySet();
		Iterator iter = localIds.iterator();
		while(iter.hasNext())
		{
			String localId = (String)iter.next();
			FieldSpecCollection[] specs = (FieldSpecCollection[])localIdsToSetOfSpecs.get(localId);
			if(specs == null)
				specs = new FieldSpecCollection[0];
			PersistableVector specIndexesForBulletin = createSpecIndexesForOneBulletin(allSpecs, specs);
			map.put(localId, specIndexesForBulletin);
		}
		
		return map;
	}

	PersistableVector createSpecIndexesForOneBulletin(HashMap allSpecs, FieldSpecCollection[] specCollections)
	{
		PersistableVector specIndexesForOnebulletin = new PersistableVector();
		for(int i = 0; i < specCollections.length; ++i)
		{
			FieldSpecCollection thisSpecCollection = specCollections[i];
			PersistableInt index = (PersistableInt)allSpecs.get(thisSpecCollection);
			if(index == null)
			{
				index = new PersistableInt(allSpecs.size());
				allSpecs.put(thisSpecCollection, index);
			}
			specIndexesForOnebulletin.add(index);
		}
		
		return specIndexesForOnebulletin;
	}

	synchronized public void loadFromStream(InputStream in) throws Exception
	{
		MartusLogger.log("Inside KnownFieldSpecCache.loadFromStream");
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
			PersistableMap fieldSpecCacheMap = (PersistableMap)PersistableObject.createFrom(dataIn);
			populateCacheFromJson(fieldSpecCacheMap);
		}
		finally
		{
			dataIn.close();
		}
		
	}
	
	private void populateCacheFromJson(PersistableMap fieldSpecCacheMap) throws Exception
	{
		PersistableVector allSpecsVector = (PersistableVector)fieldSpecCacheMap.get(TAG_ALL_SECTION_SPECS);
		HashMap allSpecs = new HashMap();
		for(int i = 0; i < allSpecsVector.size(); ++i)
		{
			FieldSpecCollection fsc = rebuildFieldSpecCollection((PersistableVector)allSpecsVector.get(i));
			allSpecs.put(new PersistableInt(i), fsc);
		}
		
		
		PersistableMap specIndexesForAll = (PersistableMap)fieldSpecCacheMap.get(TAG_SPEC_INDEXES_FOR_ALL_ACCOUNTS);

		PersistableObject[] accounts = specIndexesForAll.keys();
		for(int a = 0; a < accounts.length; ++a)
		{
			PersistableString accountId = (PersistableString)accounts[a];
			PersistableMap specIndexesForAccount = (PersistableMap)specIndexesForAll.get(accountId);
			PersistableObject[] localIds = specIndexesForAccount.keys();
			for(int i = 0; i < localIds.length; ++i)
			{
				PersistableString localId = (PersistableString)localIds[i];
				PersistableVector specIndexesForBulletin = (PersistableVector)specIndexesForAccount.get(localId);
				
				FieldSpecCollection[] specsForBulletin = new FieldSpecCollection[specIndexesForBulletin.size()];
				for(int s = 0; s < specIndexesForBulletin.size(); ++s)
				{
					PersistableInt index = (PersistableInt)specIndexesForBulletin.get(s);
					specsForBulletin[s] = (FieldSpecCollection)allSpecs.get(index);
				}
				UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId.toString(), localId.toString());
				setSpecs(uid, specsForBulletin);
			}
		}
		
	}
	
	private FieldSpecCollection rebuildFieldSpecCollection(PersistableVector vector) throws Exception 
	{
		Vector specs = new Vector();
		for(int i = 0; i < vector.size(); ++i)
		{
			PersistableString specString = (PersistableString)vector.get(i);
			specs.add(FieldSpec.createFromXml(specString.toString()));
		}
		
		FieldSpec[] specArray = (FieldSpec[])specs.toArray(new FieldSpec[0]);
		return new FieldSpecCollection(specArray);
	}

	private Set getSpecsForAccount(Map specCollectionsForOneAccount)
	{
		Set specsForThisAccount = new HashSet();
		
		Collection localIds = specCollectionsForOneAccount.keySet();
		Iterator outerIter = localIds.iterator();
		while(outerIter.hasNext())
		{
			String localId = (String)outerIter.next();
			FieldSpecCollection[] specCollectionsForOneBulletin = (FieldSpecCollection[])specCollectionsForOneAccount.get(localId);
			if(specCollectionsForOneBulletin == null)
				specCollectionsForOneBulletin = new FieldSpecCollection[0];
			for(int i = 0; i < specCollectionsForOneBulletin.length; ++i)
				specsForThisAccount.addAll(specCollectionsForOneBulletin[i].asSet());
		}
		
		return specsForThisAccount;
	}

	synchronized public void visit(DatabaseKey key)
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
			
			Vector publicAndPrivateSpecs = new Vector();
			try
			{
				String packetLocalId = bhp.getFieldDataPacketId();
				FieldSpec[] defaultSpecs = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
				FieldSpecCollection packetSpecs = loadFieldSpecsForPacket(accountId, packetLocalId, status, defaultSpecs);
				publicAndPrivateSpecs.add(packetSpecs);
			}
			catch(DecryptionException e)
			{
				// ignore because it's probably not our bulletin, and we certainly can't search it
			}

			try
			{
				String packetLocalId = bhp.getPrivateFieldDataPacketId();
				FieldSpec[] defaultSpecs = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
				FieldSpecCollection packetSpecs = loadFieldSpecsForPacket(accountId, packetLocalId, status, defaultSpecs);
				publicAndPrivateSpecs.add(packetSpecs);
			}
			catch(DecryptionException e)
			{
				// ignore because it's probably not our bulletin, and we certainly can't search it
			}

			setSpecs(bhp.getUniversalId(), (FieldSpecCollection[])publicAndPrivateSpecs.toArray(new FieldSpecCollection[0]));
		}
		catch(Exception e)
		{
			System.out.println("WARNING: Unable to read " + bulletinHeaderKey.getLocalId() + ": " + e);
			e.printStackTrace();
			// we need to keep going anyway
			// TODO: Alert the user that there was some problem
		}
	}

	private FieldSpecCollection loadFieldSpecsForPacket(String accountId, String packetLocalId, String status, FieldSpec[] defaultSpecs) throws IOException, CryptoException, InvalidPacketException, WrongPacketTypeException, SignatureVerificationException, DecryptionException, NoKeyPairException
	{
		UniversalId packetUid = UniversalId.createFromAccountAndLocalId(accountId, packetLocalId);
		FieldDataPacket fdp = loadFieldDataPacket(packetUid, status, defaultSpecs);
		return new FieldSpecCollection(fdp.getFieldSpecs());
	}

	private FieldDataPacket loadFieldDataPacket(UniversalId publicPacketUid, String status, FieldSpec[] defaultSpecs) throws IOException, CryptoException, InvalidPacketException, WrongPacketTypeException, SignatureVerificationException, DecryptionException, NoKeyPairException
	{
		DatabaseKey publicPacketKey = DatabaseKey.createKey(publicPacketUid, status);
		FieldDataPacket publicData = new FieldDataPacket(publicPacketKey.getUniversalId(), defaultSpecs);
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

	private void setSpecs(UniversalId bulletinUid, FieldSpecCollection[] specs)
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
	
	private static final int FILE_VERSION = 3;
	private static final String TAG_ALL_SECTION_SPECS = "AllSectionSpecs";
	private static final String TAG_SPEC_INDEXES_FOR_ALL_ACCOUNTS = "SpecIndexesForAllAccounts";

	public boolean saving;
	ReadableDatabase db;
	MartusCrypto security;
	Map accountsToMapsOfLocalIdsToSetsOfSpecs;
}
