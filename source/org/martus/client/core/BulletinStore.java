/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2004, Beneficent
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

package org.martus.client.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipFile;

import org.martus.common.FieldSpec;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusXml;
import org.martus.common.StandardFieldSpecs;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.bulletin.BulletinSaver;
import org.martus.common.bulletin.BulletinZipImporter;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.common.database.FileDatabase.MissingAccountMapException;
import org.martus.common.database.FileDatabase.MissingAccountMapSignatureException;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.Packet;
import org.martus.common.packet.UniversalId;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.Packet.WrongAccountException;
import org.martus.common.packet.Packet.WrongPacketTypeException;
import org.martus.common.packet.UniversalId.NotUniversalIdException;
import org.martus.util.FileInputStreamWithSeek;
import org.martus.util.InputStreamWithSeek;
import org.martus.util.Base64.InvalidBase64Exception;
import org.martus.util.xml.SimpleXmlDefaultLoader;
import org.martus.util.xml.SimpleXmlParser;
import org.martus.util.xml.SimpleXmlStringLoader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;


/*
	This class represents a collection of bulletins
	(and also a collection of folders) stored on the
	client pc.

	It is responsible for managing the lifetimes of
	both bulletins and folders, including saving and
	loading them to/from disk.
*/
public class BulletinStore
{
	public BulletinStore(MartusCrypto cryptoToUse)
	{
		setSignatureGenerator(cryptoToUse);
		cache = new BulletinCache();
	}
	
	public File getStoreRootDir()
	{
		return dir;
	}

	public void doAfterSigninInitialization(File dataRootDirectory) throws FileVerificationException, MissingAccountMapException, MissingAccountMapSignatureException
	{
		File dbDirectory = new File(dataRootDirectory, "packets");
		Database db = new ClientFileDatabase(dbDirectory, signer);
		doAfterSigninInitialization(dataRootDirectory, db);
	}

	public void doAfterSigninInitialization(File dataRootDirectory, Database db) throws FileVerificationException, MissingAccountMapException, MissingAccountMapSignatureException
	{
		dir = dataRootDirectory;
		database = db;
		initializeFolders();

		database.initialize();

		publicFieldTags = StandardFieldSpecs.getDefaultPublicFieldSpecs();
		privateFieldTags = StandardFieldSpecs.getDefaultPrivateFieldSpecs();
		
		loadCache();
		
		File obsoleteCacheFile = new File(dir, OBSOLETE_CACHE_FILE_NAME);
		obsoleteCacheFile.delete();
	}

	public void prepareToExitNormally()
	{
		saveCache();
		getSignatureGenerator().flushSessionKeyCache();
	}
	
	public void prepareToExitWithoutSavingState()
	{
		getSignatureGenerator().flushSessionKeyCache();
	}
	
	private void loadCache()
	{
		//System.out.println("BulletinStore.loadCache");
		File cacheFile = new File(dir, CACHE_FILE_NAME);
		if(!cacheFile.exists())
			return;
		
		byte[] sessionKeyCache = new byte[(int)cacheFile.length()];
		try
		{
			FileInputStream in = new FileInputStream(cacheFile);
			in.read(sessionKeyCache);
			in.close();
			getSignatureGenerator().setSessionKeyCache(sessionKeyCache);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			cacheFile.delete();
		}
	}

	private void saveCache()
	{
		System.out.println("BulletinStore.saveCache");
		try
		{
			byte[] sessionKeyCache = getSignatureGenerator().getSessionKeyCache();
			File cacheFile = new File(dir, CACHE_FILE_NAME);
			FileOutputStream out = new FileOutputStream(cacheFile);
			out.write(sessionKeyCache);
			out.close();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getAccountId()
	{
		return signer.getPublicKeyString();
	}

	public void setSignatureGenerator(MartusCrypto signerToUse)
	{
		signer = signerToUse;
	}

	public MartusCrypto getSignatureGenerator()
	{
		return signer;
	}

	public MartusCrypto getSignatureVerifier()
	{
		return signer;
	}

	public boolean mustEncryptPublicData()
	{
		return getDatabase().mustEncryptLocalData();
	}

	public int getBulletinCount()
	{
		class BulletinCounter implements Database.PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				++count;
			}

			int count = 0;
		}

		BulletinCounter counter = new BulletinCounter();
		visitAllBulletins(counter);
		return counter.count;
	}

	public Vector getAllBulletinUids()
	{
		class UidCollector implements Database.PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				uidList.add(key.getUniversalId());
			}
			Vector uidList = new Vector();
		}

		UidCollector uidCollector = new UidCollector();
		visitAllBulletins(uidCollector);
		return uidCollector.uidList;
	}

	public void visitAllBulletins(Database.PacketVisitor visitorToUse)
	{
		class BulletinKeyFilter implements Database.PacketVisitor
		{
			BulletinKeyFilter(Database db, Database.PacketVisitor visitorToUse)
			{
				visitor = visitorToUse;
				db.visitAllRecords(this);
			}

			public void visit(DatabaseKey key)
			{
				if(BulletinHeaderPacket.isValidLocalId(key.getLocalId()))
				{
					++count;
					visitor.visit(key);
				}
			}
			Database.PacketVisitor visitor;
			int count;
		}

		new BulletinKeyFilter(getDatabase(), visitorToUse);
	}

	public Set getSetOfAllBulletinUniversalIds()
	{
		class Visitor implements Database.PacketVisitor
		{
			Visitor()
			{
				setOfUniversalIds = new HashSet();
			}

			public void visit(DatabaseKey key)
			{
				setOfUniversalIds.add(key.getUniversalId());
			}

			Set setOfUniversalIds;
		}

		Visitor visitor = new Visitor();
		visitAllBulletins(visitor);
		return visitor.setOfUniversalIds;
	}

	public synchronized Set getSetOfBulletinUniversalIdsInFolders()
	{
		Set setOfUniversalIds = new HashSet();

		for(int f = 0; f < getFolderCount(); ++f)
		{
			BulletinFolder folder = getFolder(f);
			if(!folder.isVisible())
				continue;
			for(int b = 0; b < folder.getBulletinCount(); ++b)
			{
				UniversalId uid = folder.getBulletinUniversalIdUnsorted(b);
				setOfUniversalIds.add(uid);
			}
		}

		return setOfUniversalIds;
	}

	public Set getSetOfOrphanedBulletinUniversalIds()
	{
		Set possibleOrphans = getSetOfAllBulletinUniversalIds();
		Set inFolders = getSetOfBulletinUniversalIdsInFolders();
		possibleOrphans.removeAll(inFolders);
		return possibleOrphans;
	}

	public synchronized void destroyBulletin(Bulletin b) throws IOException
	{
		UniversalId id = b.getUniversalId();
		boolean DONT_SAVE_FOLDERS = false;
		for(int f = 0; f < getFolderCount(); ++f)
		{
			removeBulletinFromFolder(b, getFolder(f), DONT_SAVE_FOLDERS);
		}
		saveFolders();
		removeBulletinFromStore(id);
	}

	public synchronized void removeBulletinFromStore(UniversalId uid) throws IOException
	{
		cache.remove(uid);
		Bulletin foundBulletin = findBulletinByUniversalId(uid);
		MartusCrypto crypto = getSignatureVerifier();
		try
		{
			MartusUtilities.deleteBulletinFromDatabase(foundBulletin.getBulletinHeaderPacket(), database, crypto);
		}
		catch(Exception e)
		{
			//e.printStackTrace();
			throw new IOException("Unable to delete bulletin");
		}
	}

	public Bulletin findBulletinByUniversalId(UniversalId uid)
	{
		Database db = getDatabase();

		DatabaseKey key = new DatabaseKey(uid);
		if(!db.doesRecordExist(key))
		{
			//System.out.println("BulletinStore.findBulletinByUniversalId: !doesRecordExist");
			return null;
		}

		try
		{
			Bulletin b = loadFromDatabase(key);
			return b;
		}
		catch(NullPointerException e)
		{
			e.printStackTrace();
			return null;
		}
		catch(Exception e)
		{
			//TODO: Better error handling
			System.out.println("BulletinStore.findBulletinByUniversalId: " + e);
			e.printStackTrace();
			return null;
		}
	}

	public String getFieldData(UniversalId uid, String fieldTag)
	{
		Bulletin b = findBulletinByUniversalId(uid);
		
		if(fieldTag.equals(Bulletin.TAGSTATUS))
			return b.getStatus();
			
		if (fieldTag.equals(Bulletin.TAGLASTSAVED))
			return Long.toString(b.getLastSavedTime());			
		
		return b.get(fieldTag);
	}

	public Bulletin loadFromDatabase(DatabaseKey key) throws
		IOException,
		Bulletin.DamagedBulletinException,
		MartusCrypto.NoKeyPairException
	{
		Bulletin fromCache = cache.find(key.getUniversalId());
		if(fromCache != null)
			return fromCache;
		
		Bulletin b = BulletinLoader.loadFromDatabase(getDatabase(), key, getSignatureVerifier());
		cache.add(b);
		return b;
	}

	public void saveBulletin(Bulletin b) throws IOException, CryptoException
	{
		cache.remove(b.getUniversalId());
		BulletinSaver.saveToClientDatabase(b, database, mustEncryptPublicData(), getSignatureGenerator());
	}

	public synchronized void discardBulletin(BulletinFolder f, Bulletin b, boolean saveFolders) throws IOException
	{
		try
		{
			getFolderDiscarded().add(b);
		}
		catch (BulletinAlreadyExistsException saveToIgnoreException)
		{
		}
		removeBulletinFromFolder(b, f, saveFolders);
		if(isOrphan(b))
			destroyBulletin(b);
	}

	public synchronized BulletinFolder createFolder(String name)
	{
		BulletinFolder folder = rawCreateFolder(name);
		return folder;
	}

	public synchronized boolean renameFolder(String oldName, String newName)
	{
		if(!BulletinFolder.isNameVisible(newName))
			return false;

		if(newName.length() == 0 || newName.charAt(0) < '0')
			return false;
		
		if(!MartusUtilities.isFileNameValid(newName))
			return false;

		if(findFolder(newName) != null)
			return false;

		BulletinFolder folder = findFolder(oldName);
		if(folder == null)
			return false;

		folder.setName(newName);
		saveFolders();
		return true;
	}

	public synchronized boolean deleteFolder(String name)
	{
		BulletinFolder folder = findFolder(name);
		if(folder == null)
			return false;

		if(!folder.canDelete())
			return false;

		BulletinFolder discarded = getFolderDiscarded();

		while(folder.getBulletinCount() > 0)
		{
			Bulletin b = folder.getBulletinSorted(0);
			try
			{
				discarded.add(b);
			}
			catch (BulletinAlreadyExistsException safeToIgnore)
			{
			}
			catch (IOException safeToIgnore)
			{
				safeToIgnore.printStackTrace();
			}
			folder.remove(b.getUniversalId());
		}

		folders.remove(folder);
		saveFolders();
		return true;
	}

	public void clearFolder(String folderName)
	{
		BulletinFolder folder = findFolder(folderName);
		if(folder == null)
			return;

		folder.removeAll();
		saveFolders();
	}

	public synchronized int getFolderCount()
	{
		return folders.size();
	}

	private synchronized BulletinFolder getFolder(int index)
	{
		if(index < 0 || index >= folders.size())
			return null;

		return (BulletinFolder)folders.get(index);
	}

	public synchronized BulletinFolder findFolder(String name)
	{
		for(int index=0; index < getFolderCount(); ++index)
		{
			BulletinFolder folder = getFolder(index);
			if(name.equals(folder.getName()))
				return folder;
		}
		return null;
	}

	public synchronized Vector getAllFolders()
	{
		Vector allFolders = new Vector();
		for(int f = 0; f < getFolderCount(); ++f)
		{
			BulletinFolder folder = getFolder(f);
			allFolders.add(folder);
		}
		return allFolders;
	}
	
	public synchronized Vector getAllFolderNames()
	{
		Vector names = new Vector();
		Vector folders = getAllFolders();
		for(int f = 0; f < folders.size(); ++f)
		{
			names.add(((BulletinFolder)folders.get(f)).getName());
		}
		return names;
	}

	public synchronized Vector getVisibleFolderNames()
	{
		Vector names = new Vector();
		for(int f = 0; f < getFolderCount(); ++f)
		{
			BulletinFolder folder = getFolder(f);
			String folderName = folder.getName();
			if(BulletinFolder.isNameVisible(folderName))
				names.add(folderName);
		}
		return names;
	}

	public String getSearchFolderName()
	{
		return SEARCH_RESULTS_BULLETIN_FOLDER;
	}

	public String getOrphanFolderName()
	{
		return RECOVERED_BULLETIN_FOLDER;
	}

	public String getNameOfFolderRetrievedSealed()
	{
		return RETRIEVE_SEALED_BULLETIN_FOLDER;
	}

	public String getNameOfFolderRetrievedDraft()
	{
		return RETRIEVE_DRAFT_BULLETIN_FOLDER;
	}

	public String getNameOfFolderRetrievedFieldOfficeSealed()
	{
		return RETRIEVE_SEALED_FIELD_OFFICE_BULLETIN_FOLDER;
	}

	public String getNameOfFolderRetrievedFieldOfficeDraft()
	{
		return RETRIEVE_DRAFT_FIELD_OFFICE_BULLETIN_FOLDER;
	}

	public String getNameOfFolderDamaged()
	{
		return DAMAGED_BULLETIN_FOLDER;
	}


	public BulletinFolder getFolderOutbox()
	{
		return folderOutbox;
	}

	public BulletinFolder getFolderDiscarded()
	{
		return folderDiscarded;
	}

	public BulletinFolder getFolderSent()
	{
		return folderSent;
	}

	public BulletinFolder getFolderDrafts()
	{
		return folderDrafts;
	}

	public BulletinFolder getFolderDraftOutbox()
	{
		return folderDraftOutbox;
	}

	public void createSystemFolders()
	{
		folderOutbox = createSystemFolder(OUTBOX_FOLDER);
		folderOutbox.setStatusAllowed(Bulletin.STATUSSEALED);
		folderSent = createSystemFolder(SENT_FOLDER);
		folderSent.setStatusAllowed(Bulletin.STATUSSEALED);
		folderDrafts = createSystemFolder(DRAFT_FOLDER);
		folderDrafts.setStatusAllowed(Bulletin.STATUSDRAFT);
		folderDiscarded = createSystemFolder(DISCARDED_FOLDER);
		folderDraftOutbox = createSystemFolder(DRAFT_OUTBOX);
		folderDraftOutbox.setStatusAllowed(Bulletin.STATUSDRAFT);
	}

	public BulletinFolder createSystemFolder(String name)
	{
		BulletinFolder folder = rawCreateFolder(name);
		folder.preventRename();
		folder.preventDelete();
		return folder;
	}

	public synchronized void moveBulletin(Bulletin b, BulletinFolder from, BulletinFolder to)
	{
		if(from.equals(to))
			return;
		try
		{
			to.add(b);
		}
		catch (BulletinAlreadyExistsException e)
		{
			System.out.println("Bulletin already exists in destination folder");
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean saveFolders = true;
		removeBulletinFromFolder(b, from, saveFolders);
	}

	public synchronized void removeBulletinFromFolder(Bulletin b, BulletinFolder from, boolean saveFolders)
	{
		from.remove(b.getUniversalId());
		if(saveFolders)
			saveFolders();
	}

	public Vector findBulletinInAllVisibleFolders(Bulletin b)
	{
		Vector allFolders= getVisibleFolderNames();
		Vector foldersContainingBulletin = new Vector();
		for(int i = 0; i < allFolders.size(); ++i)
		{
			BulletinFolder folder = findFolder((String)allFolders.get(i));
			if(folder != null && folder.contains(b))
				foldersContainingBulletin.add(folder);
		}
		return foldersContainingBulletin;
	}

	public void deleteAllData() throws Exception
	{
		deleteAllBulletins();
		deleteFoldersDatFile();
		resetFolders();
	}			
	
	public void deleteAllBulletins() throws Exception
	{
		database.deleteAllData();
	}

	public void deleteFoldersDatFile()
	{
		getFoldersFile().delete();
	}	
	
	public void resetFolders()
	{
		initializeFolders();
	}
	
	public void scrubAllData() throws Exception
	{
		class PacketScrubber implements Database.PacketVisitor 
		{
			public void visit(DatabaseKey key)
			{
				try
				{
					database.scrubRecord(key);
					database.discardRecord(key);
				}
				catch (Exception e)
				{				
					e.printStackTrace();
				}				
			}			
		}
	
		PacketScrubber ac = new PacketScrubber();
		database.visitAllRecords(ac);
		deleteFoldersDatFile();
	}	

	public Database getDatabase()
	{
		return database;
	}

	public void setDatabase(Database toUse)
	{
		database = toUse;
	}

	public synchronized void loadFolders()
	{
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStreamWithSeek in = new FileInputStreamWithSeek(getFoldersFile());
			getSignatureVerifier().decrypt(in, out);
			in.close();

			String folderXml = new String(out.toByteArray(), "UTF-8");
			if(folderXml == null)
				return;
				
			internalLoadFolders(folderXml);
			if(needsLegacyFolderConversion())
			{
				saveFolders();
				loadedLegacyFolders = false;
			}
		}
		catch(UnsupportedEncodingException e)
		{
			System.out.println("BulletinStore.loadFolders: " + e);
		}
		catch(FileNotFoundException expectedIfFoldersDontExistYet)
		{
		}
		catch(Exception e)
		{
			// TODO: Improve error handling!!!
			System.out.println("BulletinStore.loadFolders: " + e);
		}
	}

	public synchronized void saveFolders()
	{
		try
		{
			String xml = foldersToXml();
			byte[] bytes = xml.getBytes("UTF-8");
			ByteArrayInputStream in = new ByteArrayInputStream(bytes);

			FileOutputStream out = new FileOutputStream(getFoldersFile());
			if(getSignatureGenerator() == null)
				return;
			getSignatureGenerator().encrypt(in, out);
			out.close();
		}
		catch(UnsupportedEncodingException e)
		{
			System.out.println("BulletinStore.saveFolders: " + e);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("BulletinStore.saveFolders: " + e);
		}
	}

	public File getFoldersFile()
	{
		return new File(dir, "MartusFolders.dat");
	}

	public Bulletin createEmptyBulletin()
	{
		FieldSpec[] standardFieldNames = getPublicFieldTags();
		FieldSpec[] privateFieldNames = getPrivateFieldTags();
		Bulletin b = new Bulletin(getSignatureGenerator(), standardFieldNames, privateFieldNames);
		return b;
	}

	private FieldSpec[] getPrivateFieldTags()
	{
		return privateFieldTags;
	}

	public FieldSpec[] getPublicFieldTags()
	{
		return publicFieldTags;
	}

	public synchronized BulletinFolder createOrFindFolder(String name)
	{
		BulletinFolder result = findFolder(name);
		if(result != null)
			return result;
		return createFolder(name);
	}

	public synchronized void addBulletinToFolder(UniversalId uId, BulletinFolder folder) throws BulletinAlreadyExistsException, IOException
	{
		Bulletin b = findBulletinByUniversalId(uId);
		if(b == null)
			return;

		folder.add(b);
	}
	

	private void initializeFolders()
	{
		folders = new Vector();
		createSystemFolders();
	}
	
	public void setPublicFieldTags(FieldSpec[] newTags)
	{
		publicFieldTags = newTags;
	}

	public int quarantineUnreadableBulletins()
	{
		class Quarantiner implements Database.PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				InputStreamWithSeek in = null;
				try
				{
					in = database.openInputStream(key, getSignatureVerifier());
					Packet.validateXml(in, key.getAccountId(), key.getLocalId(), null, getSignatureVerifier());
					in.close();
				}
				catch(Exception e)
				{
					++quarantinedCount;
					if(in != null)
					{
						try { in.close(); } catch(Exception ignore) {}
					}
					try
					{
						database.moveRecordToQuarantine(key);
					}
					catch (RecordHiddenException shouldNeverHappen)
					{
						shouldNeverHappen.printStackTrace();
					}
				}
			}

			int quarantinedCount;
		}

		Quarantiner visitor = new Quarantiner();
		visitAllBulletins(visitor);
		return visitor.quarantinedCount;
	}

	public synchronized boolean isOrphan(Bulletin b)
	{
		Vector allFolders= getVisibleFolderNames();
		for(int i = 0; i < allFolders.size(); ++i)
		{
			BulletinFolder folder = findFolder((String)allFolders.get(i));
			if(folder != null && folder.contains(b))
				return false;
		}

		return true;
	}

	private synchronized BulletinFolder rawCreateFolder(String name)
	{
		if(findFolder(name) != null)
			return null;

		BulletinFolder folder = new BulletinFolder(this, name);
		folders.add(folder);
		return folder;
	}

	public synchronized String foldersToXml()
	{
		String xml = MartusClientXml.getFolderListTagStart();

		for(int index=0; index < getFolderCount(); ++index)
		{
			BulletinFolder folder = getFolder(index);
			xml += folderToXml(folder);
		}

		xml += MartusClientXml.getFolderListTagEnd();
		return xml;
	}

	public String folderToXml(BulletinFolder folder)
	{
		String xml = MartusClientXml.getFolderTagStart(folder.getName());
		for(int index=0; index < folder.getBulletinCount(); ++index)
		{
			UniversalId uid = folder.getBulletinUniversalIdSorted(index);
			if(uid == null)
				System.out.println("WARNING: Unexpected null id");
			xml += MartusXml.getIdTag(uid.toString());
		}
		xml += MartusClientXml.getFolderTagEnd();
		return xml;
	}

	public synchronized void internalLoadFolders(String folderXml)
	{
		folders.clear();
		loadedLegacyFolders = false;
		createSystemFolders();
		XmlFolderListLoader loader = new XmlFolderListLoader(this);
		try
		{
			SimpleXmlParser.parse(loader, new StringReader(folderXml));
		}
		catch (Exception e)
		{
			// TODO Improve error handling!!!
			e.printStackTrace();
		}
	}
	
	class XmlFolderListLoader extends SimpleXmlDefaultLoader
	{
		public XmlFolderListLoader(BulletinStore storeToFill)
		{
			super(MartusClientXml.tagFolderList);
			store = storeToFill;
		}
		
		public SimpleXmlDefaultLoader startElement(String tag)
			throws SAXParseException
		{
			if(tag.equals(MartusClientXml.tagFolder))
			{
				return new XmlFolderLoader(tag, store);
			}
			else
				return super.startElement(tag);
		}

		public void endElement(String tag, SimpleXmlDefaultLoader ended)
			throws SAXParseException
		{
			if(tag.equals(MartusClientXml.tagFolder))
				;
			else
				super.endElement(tag, ended);
		}
		
		BulletinStore store;
	}
	
	class XmlFolderLoader extends SimpleXmlDefaultLoader
	{
		public XmlFolderLoader(String tag, BulletinStore storeToFill)
		{
			super(tag);
			store = storeToFill;
		}
		
		String convertLegacyFolder(String name)
		{
			if(name.equals("Outbox"))
				name = OUTBOX_FOLDER;
			else if(name.equals("Sent Bulletins"))
				name = SENT_FOLDER;
			else if(name.equals("Draft Bulletins"))
				name = DRAFT_FOLDER;
			else if(name.equals("Discarded Bulletins"))
				name = DISCARDED_FOLDER;
			return name;
		}
		
		public void startDocument(Attributes attrs)
		{
			String name = attrs.getValue(MartusClientXml.attrFolder);
			String convertedName = convertLegacyFolder(name);
			if(!convertedName.equals(name))
				store.setNeedsLegacyFolderConversion();
					
			folder = store.createOrFindFolder(convertedName);
		}

		public SimpleXmlDefaultLoader startElement(String tag)
			throws SAXParseException
		{
			if(tag.equals(MartusClientXml.tagId))
				return new SimpleXmlStringLoader(tag);
			else
				return super.startElement(tag);
		}

		public void endElement(String tag, SimpleXmlDefaultLoader ended)
			throws SAXParseException
		{
			if(tag.equals(MartusClientXml.tagId))
			{
				String uidText = ((SimpleXmlStringLoader)ended).getText();
				try
				{
					UniversalId bulletinId = UniversalId.createFromString(uidText);
					folder.add(bulletinId);
				}
				catch (NotUniversalIdException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (BulletinAlreadyExistsException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
				super.endElement(tag, ended);
		}
		
		BulletinStore store;
		BulletinFolder folder;
	}

	public static class StatusNotAllowedException extends Exception {}
	public static class BulletinAlreadyExistsException extends Exception {}
	
	public void importZipFileBulletin(File zipFile, BulletinFolder toFolder, boolean forceSameUids) throws
			StatusNotAllowedException,
			InvalidPacketException,
			SignatureVerificationException,
			WrongPacketTypeException,
			CryptoException,
			IOException,
			InvalidBase64Exception, BulletinAlreadyExistsException
	{
		ZipFile zip = new ZipFile(zipFile);
		try
		{
			BulletinHeaderPacket bhp = BulletinHeaderPacket.loadFromZipFile(zip, getSignatureVerifier());
			if(!canPutBulletinInFolder(toFolder, bhp.getAccountId(), bhp.getStatus()))
			{
				throw new StatusNotAllowedException();
			}
			UniversalId uid = bhp.getUniversalId();

			boolean isSealed = bhp.getStatus().equals(Bulletin.STATUSSEALED);
			boolean isMine = getAccountId().equals(bhp.getAccountId());
			if(forceSameUids || !isMine || isSealed)
			{
				importZipFileToStoreWithSameUids(zipFile);
			}
			else
				uid = importZipFileToStoreWithNewUids(zipFile);

			addBulletinToFolder(uid, toFolder);
		}
		finally
		{
			zip.close();
		}

		saveFolders();
	}

	public void importZipFileToStoreWithSameUids(File inputFile) throws
		IOException,
		MartusCrypto.CryptoException,
		Packet.InvalidPacketException,
		Packet.SignatureVerificationException
	{
		ZipFile zip = new ZipFile(inputFile);
		try
		{
			BulletinZipUtilities.importBulletinPacketsFromZipFileToDatabase(getDatabase(), null, zip, getSignatureVerifier());
		}
		catch (Database.RecordHiddenException shouldBeImpossible)
		{
			shouldBeImpossible.printStackTrace();
			throw new IOException(shouldBeImpossible.toString());
		}
		catch(WrongAccountException shouldBeImpossible)
		{
			throw new Packet.InvalidPacketException("Wrong account???");
		}
		finally
		{
			zip.close();
		}
	}

	public boolean canPutBulletinInFolder(BulletinFolder folder, String bulletinAuthorAccount, String bulletinStatus)
	{
		if(!folder.canAdd(bulletinStatus))
			return false;
		if(folder.equals(getFolderOutbox()) && !bulletinAuthorAccount.equals(getAccountId()))
			return false;
		return true;
	}

	public UniversalId importZipFileToStoreWithNewUids(File inputFile) throws
		InvalidPacketException,
		SignatureVerificationException,
		WrongPacketTypeException,
		CryptoException,
		IOException,
		InvalidBase64Exception
	{
		final MartusCrypto security = getSignatureGenerator();
		Bulletin imported = BulletinZipImporter.loadFromFile(security, inputFile);
		saveBulletin(imported);
		return imported.getUniversalId();
	}
	
	void setNeedsLegacyFolderConversion()
	{
		loadedLegacyFolders = true;
	}
	
	public boolean needsLegacyFolderConversion()
	{
		return loadedLegacyFolders;
	}

	public static int maxCachedBulletinCount = 100;

	public static final String OUTBOX_FOLDER = "%OutBox";
	public static final String SENT_FOLDER = "%Sent";
	public static final String DRAFT_FOLDER = "%Draft";
	public static final String DISCARDED_FOLDER = "%Discarded";
	public static final String SEARCH_RESULTS_BULLETIN_FOLDER = "%SearchResults";
	public static final String RECOVERED_BULLETIN_FOLDER = "%RecoveredBulletins";
	public static final String RETRIEVE_SEALED_BULLETIN_FOLDER = "%RetrievedMyBulletin";
	public static final String RETRIEVE_SEALED_FIELD_OFFICE_BULLETIN_FOLDER = "%RetrievedFieldOfficeBulletin";
	public static final String RETRIEVE_DRAFT_BULLETIN_FOLDER = "%RetrievedMyBulletinDraft";
	public static final String RETRIEVE_DRAFT_FIELD_OFFICE_BULLETIN_FOLDER = "%RetrievedFieldOfficeBulletinDraft";
	public static final String DAMAGED_BULLETIN_FOLDER = "%DamagedBulletins";
	private static final String DRAFT_OUTBOX = "*DraftOutbox";

	private static final String CACHE_FILE_NAME = "skcache.dat";
	private static final String OBSOLETE_CACHE_FILE_NAME = "sfcache.dat";
	private MartusCrypto signer;
	private File dir;
	Database database;
	private Vector folders;
	private BulletinFolder folderOutbox;
	private BulletinFolder folderSent;
	private BulletinFolder folderDrafts;
	private BulletinFolder folderDiscarded;
	private BulletinFolder folderDraftOutbox;
	boolean loadedLegacyFolders;
	private BulletinCache cache;

	FieldSpec[] publicFieldTags;
	FieldSpec[] privateFieldTags;	
}
