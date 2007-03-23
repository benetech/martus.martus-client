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
import java.util.TreeMap;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;
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
		FieldSpecCollection publicSpecs = new FieldSpecCollection(b.getTopSectionFieldSpecs());
		FieldSpecCollection privateSpecs = new FieldSpecCollection(b.getBottomSectionFieldSpecs());
		setSpecs(b.getUniversalId(), new FieldSpecCollection[] {publicSpecs, privateSpecs});
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
			Set specsForAccount = getSpecsForAccount(specsForOneAccount);
			knownSpecs.addAll(specsForAccount);
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
			JsonFieldSpecCache json = new JsonFieldSpecCache();
			json.build();
			String jsonString = json.toString();

			dataOut.writeInt(jsonString.length());
			for(int i = 0; i < jsonString.length(); ++i)
				dataOut.writeChar(jsonString.charAt(i));
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
	
	class OurJsonObject extends JSONObject
	{
		public OurJsonObject()
		{
			super();
		}
		
		public OurJsonObject(String jsonString) throws Exception
		{
			super(jsonString);
		}
		
		public void clear()
		{
			Iterator keys = keys();
			while(keys.hasNext())
			{
				String key = (String)keys.next();
				remove(key);
			}
		}
	}
	
	class OurJsonArray extends JSONArray
	{
		public OurJsonArray()
		{
			super();
		}
		
		public OurJsonArray(String jsonString) throws Exception
		{
			super(jsonString);
		}
		
		public void clear()
		{
			while(length() > 0)
				removeAt(0);
		}
	}
	
	class JsonFieldSpecCache extends OurJsonObject
	{
		public JsonFieldSpecCache()
		{
			super();
		}
		
		public JsonFieldSpecCache(String jsonString) throws Exception
		{
			super(jsonString);
		}
		
		public void build()
		{
			clear();
			put(TAG_VERSION, FILE_VERSION);
			JsonAllFieldSpecs allSpecs = new JsonAllFieldSpecs();
			allSpecs.build();
			JsonSpecIndexesForAllBulletins specIndexes = new JsonSpecIndexesForAllBulletins();
			specIndexes.build(allSpecs);
			put(TAG_SPEC_INDEXES_FOR_ALL_ACCOUNTS, specIndexes);
			put(TAG_ALL_SECTION_SPECS, allSpecs);
			
		}
		
		public JsonAllFieldSpecs getAllFieldSpecs() throws Exception
		{
			return new JsonAllFieldSpecs(getJSONArray(TAG_ALL_SECTION_SPECS).toString());
		}

		public JsonSpecIndexesForAllBulletins getSpecIndexesForAllBulletins() throws Exception 
		{
			return new JsonSpecIndexesForAllBulletins(getJSONObject(TAG_SPEC_INDEXES_FOR_ALL_ACCOUNTS).toString());
		}
	}
	
	class JsonAllFieldSpecs extends OurJsonArray
	{
		public JsonAllFieldSpecs()
		{
			super();
		}
		
		public JsonAllFieldSpecs(String jsonString) throws Exception
		{
			super(jsonString);
		}
		
		public void build()
		{
			clear();
			specCollectionToIndex = new TreeMap();
		}
		
		public int getIndexOf(FieldSpecCollection collection)
		{
			Integer index = (Integer)specCollectionToIndex.get(collection);
			if(index == null)
			{
				index = new Integer(specCollectionToIndex.size());
				specCollectionToIndex.put(collection, index);
				JsonSpecsCollection jsonSpecsCollection = new JsonSpecsCollection();
				jsonSpecsCollection.build(collection);
				put(jsonSpecsCollection);
			}
			return index.intValue();
		}
		
		public JsonSpecsCollection getSpecsCollection(int index) throws Exception 
		{
			return new JsonSpecsCollection(getJSONArray(index).toString());
		}

		TreeMap specCollectionToIndex;

	}

	class JsonSpecsCollection extends OurJsonArray
	{
		public JsonSpecsCollection()
		{
			super();
		}
		
		public JsonSpecsCollection(String jsonString) throws Exception
		{
			super(jsonString);
		}
		
		public void build(FieldSpecCollection specs)
		{	
			clear();
			for(int spec = 0; spec < specs.size(); ++spec)
				put(specs.get(spec).toString());
		}
		
		public FieldSpecCollection getFieldSpecCollection() throws Exception
		{
			FieldSpecCollection specs = new FieldSpecCollection(length());
			for(int spec = 0; spec < specs.size(); ++spec)
				specs.set(spec, FieldSpec.createFromXml(getString(spec)));
			
			return specs;
		}
		
	}
	
	class JsonSpecIndexesForAllBulletins extends OurJsonObject
	{
		public JsonSpecIndexesForAllBulletins()
		{
			super();
		}
		
		public JsonSpecIndexesForAllBulletins(String jsonString) throws Exception
		{
			super(jsonString);
		}
		
		public void build(JsonAllFieldSpecs allSpecs)
		{
			Collection accountIds = accountsToMapsOfLocalIdsToSetsOfSpecs.keySet();
			Iterator iter = accountIds.iterator();
			while(iter.hasNext())
			{
				String accountId = (String)iter.next();
				JsonSpecIndexesForOneAccount specIndexesForAccount = new JsonSpecIndexesForOneAccount();
				specIndexesForAccount.build(allSpecs, accountId);
				put(accountId, specIndexesForAccount);
			}
			
		}

		public JsonSpecIndexesForOneAccount getSpecIndexesForAccount(String accountId) throws Exception 
		{
			return new JsonSpecIndexesForOneAccount(getJSONObject(accountId).toString());
		}
	}
	
	class JsonSpecIndexesForOneAccount extends OurJsonObject
	{
		public JsonSpecIndexesForOneAccount()
		{
			super();
		}
		
		public JsonSpecIndexesForOneAccount(String jsonString) throws Exception
		{
			super(jsonString);
		}
		
		public void build(JsonAllFieldSpecs allSpecs, String accountId)
		{
			Map localIdsToSetOfSpecs = (Map)accountsToMapsOfLocalIdsToSetsOfSpecs.get(accountId);
			if(localIdsToSetOfSpecs == null)
				return;
			
			Collection localIds = localIdsToSetOfSpecs.keySet();
			Iterator iter = localIds.iterator();
			while(iter.hasNext())
			{
				String localId = (String)iter.next();
				FieldSpecCollection[] specs = (FieldSpecCollection[])localIdsToSetOfSpecs.get(localId);
				if(specs == null)
					specs = new FieldSpecCollection[0];
				JsonSpecIndexesForOneBulletin specIndexesForBulletin = new JsonSpecIndexesForOneBulletin();
				specIndexesForBulletin.build(allSpecs, specs);
				put(localId, specIndexesForBulletin);
			}
		}

		public JsonSpecIndexesForOneBulletin getSpecIndexesForBulletin(String localId) throws Exception 
		{
			return new JsonSpecIndexesForOneBulletin(getJSONArray(localId).toString());
		}
	}
	
	class JsonSpecIndexesForOneBulletin extends OurJsonArray
	{
		public JsonSpecIndexesForOneBulletin()
		{
			super();
		}
		
		public JsonSpecIndexesForOneBulletin(String jsonString) throws Exception
		{
			super(jsonString);
		}
		
		public void build(JsonAllFieldSpecs allSpecs, FieldSpecCollection[] specCollections)
		{
			for(int i = 0; i < specCollections.length; ++i)
			{
				put(allSpecs.getIndexOf(specCollections[i]));
			}
			
		}
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
			int length = dataIn.readInt();
			StringBuffer jsonString = new StringBuffer(length);
			for(int i = 0; i < length; ++i)
				jsonString.append(dataIn.readChar());
			JsonFieldSpecCache json = new JsonFieldSpecCache(jsonString.toString());
			populateCacheFromJson(json);
		}
		finally
		{
			dataIn.close();
		}
		
	}
	
	private void populateCacheFromJson(JsonFieldSpecCache json) throws Exception
	{
		if(json.getInt(TAG_VERSION) != FILE_VERSION)
			throw new IOException("Bad version of field spec cache file");
		
		JsonAllFieldSpecs allSpecs = json.getAllFieldSpecs();
		JsonSpecIndexesForAllBulletins specIndexesForAll = json.getSpecIndexesForAllBulletins();
		Iterator accounts = specIndexesForAll.keys();
		while(accounts.hasNext())
		{
			String accountId = (String)accounts.next();
			JsonSpecIndexesForOneAccount specIndexesForAccount = specIndexesForAll.getSpecIndexesForAccount(accountId);
			Iterator bulletins = specIndexesForAccount.keys();
			while(bulletins.hasNext())
			{
				String localId = (String)bulletins.next();
				JsonSpecIndexesForOneBulletin specIndexesForBulletin = specIndexesForAccount.getSpecIndexesForBulletin(localId);
				
				FieldSpecCollection[] specsForBulletin = new FieldSpecCollection[specIndexesForBulletin.length()];
				for(int i = 0; i < specIndexesForBulletin.length(); ++i)
				{
					int index = specIndexesForBulletin.getInt(i);
					JsonSpecsCollection jsonSpecs = allSpecs.getSpecsCollection(index);
					FieldSpecCollection specs = jsonSpecs.getFieldSpecCollection();
					specsForBulletin[i] = specs;
				}
				UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, localId);
				setSpecs(uid, specsForBulletin);
			}
		}
		
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
	
	private static final int FILE_VERSION = 2;
	private static final String TAG_VERSION = "Version";
	private static final String TAG_ALL_SECTION_SPECS = "AllSectionSpecs";
	private static final String TAG_SPEC_INDEXES_FOR_ALL_ACCOUNTS = "SpecIndexesForAllAccounts";

	ReadableDatabase db;
	MartusCrypto security;
	Map accountsToMapsOfLocalIdsToSetsOfSpecs;
}
