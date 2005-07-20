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

package org.martus.client.bulletinstore;

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
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipFile;

import org.martus.client.core.MartusClientXml;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusXml;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.bulletin.BulletinZipImporter;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.database.ClientFileDatabase;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.common.database.FileDatabase.MissingAccountMapException;
import org.martus.common.database.FileDatabase.MissingAccountMapSignatureException;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.Packet;
import org.martus.common.packet.UniversalId;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.Packet.WrongPacketTypeException;
import org.martus.common.packet.UniversalId.NotUniversalIdException;
import org.martus.util.Base64.InvalidBase64Exception;
import org.martus.util.inputstreamwithseek.FileInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;
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
public class ClientBulletinStore extends BulletinStore
{
	public ClientBulletinStore(MartusCrypto cryptoToUse)
	{
		setSignatureGenerator(cryptoToUse);
		bulletinDataCache = new BulletinCache();
	}
	
	public void doAfterSigninInitialization(File dataRootDirectory) throws FileVerificationException, MissingAccountMapException, MissingAccountMapSignatureException
	{
		File dbDirectory = new File(dataRootDirectory, "packets");
		Database db = new ClientFileDatabase(dbDirectory, getSignatureGenerator());
		doAfterSigninInitialization(dataRootDirectory, db);
	}

	public void doAfterSigninInitialization(File dataRootDirectory, Database db) throws FileVerificationException, MissingAccountMapException, MissingAccountMapSignatureException
	{
		super.doAfterSigninInitialization(dataRootDirectory, db);
		
		initializeFolders();

		publicFieldSpecs = StandardFieldSpecs.getDefaultPublicFieldSpecs();
		privateFieldSpecs = StandardFieldSpecs.getDefaultPrivateFieldSpecs();
		
		loadCache();
		
		File obsoleteCacheFile = new File(getStoreRootDir(), OBSOLETE_CACHE_FILE_NAME);
		obsoleteCacheFile.delete();

		knownFieldSpecCache = new KnownFieldSpecCache(getDatabase(), getSignatureGenerator());
		knownFieldSpecCache.restoreCacheFromSavedState();
		addCache(knownFieldSpecCache);
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
	
	public boolean mustEncryptPublicData()
	{
		return getDatabase().mustEncryptLocalData();
	}
	
	public boolean isMyBulletin(UniversalId uid)
	{
		return(uid.getAccountId().equals(getAccountId()));
	}

	public synchronized Set getSetOfBulletinUniversalIdsInFolders()
	{
		Set setOfUniversalIds = new HashSet();

		Vector visibleFolders = getAllVisibleFolders();
		for(Iterator f = visibleFolders.iterator(); f.hasNext();)
		{
			BulletinFolder folder = (BulletinFolder) f.next();
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
		Set possibleOrphans = new HashSet(getAllBulletinLeafUids());
		Set inFolders = getSetOfBulletinUniversalIdsInFolders();
		possibleOrphans.removeAll(inFolders);
		return possibleOrphans;
	}

	public synchronized void destroyBulletin(Bulletin b) throws IOException
	{
		removeBulletinFromAllFolders(b);
		saveFolders();

		bulletinDataCache.remove(b.getUniversalId());
		removeBulletinFromStore(b);
	}

	public void removeBulletinFromAllFolders(Bulletin b) throws IOException
	{
		BulletinHistory history = b.getHistory();
		for(int i = 0; i < history.size(); ++i)
		{
			String localId = history.get(i);
			UniversalId uidOfAncestor = UniversalId.createFromAccountAndLocalId(b.getAccount(), localId);
			removeRevisionFromAllFolders(uidOfAncestor);
		}
		
		removeRevisionFromAllFolders(b.getUniversalId());
	}

	private void removeRevisionFromAllFolders(UniversalId id)
	{
		for(int f = 0; f < getFolderCount(); ++f)
		{
			removeBulletinFromFolder(getFolder(f), id);
		}
	}

	public Bulletin getBulletinRevision(UniversalId uid)
	{
		DatabaseKey key = DatabaseKey.createLegacyKey(uid);
		if(!doesBulletinRevisionExist(key))
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

	public String getSentTag(Bulletin b)
	{
		boolean knownNotOnServer = isProbablyNotOnServer(b);

		if(getFolderDraftOutbox().contains(b))
		{
			if(isMyBulletin(b.getBulletinHeaderPacket()))
				return WAS_SENT_NO;
			if(!knownNotOnServer)
				return null;
		}

		if(knownNotOnServer)
			return WAS_SENT_NO;

		if(isProbablyOnServer(b))
			return WAS_SENT_YES;
		
		return null;
	}

	public String getFieldData(UniversalId uid, String fieldTag)
	{
		Bulletin b = getBulletinRevision(uid);
		
		if(fieldTag.equals(Bulletin.TAGWASSENT))
		{
			String tag = getSentTag(b);
			if(tag == null)
				return "";
			return tag;
		}
			
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
		Bulletin fromCache = bulletinDataCache.find(key.getUniversalId());
		if(fromCache != null)
			return fromCache;
		
		Bulletin b = BulletinLoader.loadFromDatabase(getDatabase(), key, getSignatureVerifier());
		bulletinDataCache.add(b);
		return b;
	}

	public void saveBulletin(Bulletin b) throws IOException, CryptoException
	{
		bulletinDataCache.remove(b.getUniversalId());
		saveBulletin(b, mustEncryptPublicData());
	}

	public synchronized void discardBulletin(BulletinFolder f, Bulletin b) throws IOException
	{
		try
		{
			getFolderDiscarded().add(b);
		}
		catch (BulletinAlreadyExistsException saveToIgnoreException)
		{
		}
		removeBulletinFromFolder(f, b);
		if(isOrphan(b))
			destroyBulletin(b);
	}

	public Bulletin chooseBulletinToUpload(BulletinFolder hiddenFolder, int startIndex)
	{
		int bulletinCount = hiddenFolder.getBulletinCount();
		for(int i=0; i < bulletinCount; ++i)
		{
			++startIndex;
			if(startIndex >= bulletinCount)
				startIndex = 0;
			
			Bulletin b = hiddenFolder.getBulletinUnsorted(startIndex);
			if(!isDiscarded(b))
				return b;
		}
		return null;
	}

	public boolean hasAnyNonDiscardedBulletins(BulletinFolder hiddenFolder)
	{
		for(int i=0; i < hiddenFolder.getBulletinCount(); ++i)
		{
			Bulletin b = hiddenFolder.getBulletinSorted(i);
			if(!isDiscarded(b))
				return true;
		}
		return false;
	}
	
	private boolean isDiscarded(Bulletin b)
	{
		return getFolderDiscarded().contains(b);
	}

	public synchronized BulletinFolder createFolder(String name)
	{
		BulletinFolder folder = rawCreateFolder(name);
		return folder;
	}	

	public synchronized boolean renameFolder(String oldName, String newName)
	{		
		if (newName.length() == 0 || newName.startsWith(" "))
			return false;
			
		char[] strOfArray = newName.toCharArray();								
		for(int i = 0; i < strOfArray.length; ++i)
		{			
			if (!MartusUtilities.isValidCharInFolder(strOfArray[i]))
				return false;
		}	
		
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
	
	public synchronized Vector getAllVisibleFolders()
	{
		Vector allFolders = getAllFolders();
		Vector visibleFolders = new Vector();
		for(Iterator f = allFolders.iterator(); f.hasNext();)
		{
			BulletinFolder folder = (BulletinFolder) f.next();
			if(folder.isVisible())
				visibleFolders.add(folder);
		}
		return visibleFolders;
	}
	
	public synchronized Vector getAllFolderNames()
	{
		Vector names = new Vector();
		Vector allFolders = getAllFolders();
		for(int f = 0; f < allFolders.size(); ++f)
		{
			names.add(((BulletinFolder)allFolders.get(f)).getName());
		}
		return names;
	}

	public synchronized Vector getVisibleFolderNames()
	{
		Vector names = new Vector();
		Vector visibleFolders = getAllVisibleFolders();
		for(Iterator f = visibleFolders.iterator(); f.hasNext();)
		{
			BulletinFolder folder = (BulletinFolder) f.next();
			String folderName = folder.getName();
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


	public BulletinFolder getFolderDiscarded()
	{
		return folderDiscarded;
	}

	public BulletinFolder getFolderSaved()
	{
		return folderSaved;
	}

	public BulletinFolder getFolderDraftOutbox()
	{
		return folderDraftOutbox;
	}
	
	public BulletinFolder getFolderSealedOutbox()
	{
		return folderSealedOutbox;
	}
	
	private BulletinFolder getFolderOnServer()
	{
		return createOrFindFolder(ON_SERVER_FOLDER);
	}
	
	private BulletinFolder getFolderNotOnServer()
	{
		return createOrFindFolder(NOT_ON_SERVER_FOLDER);
	}
	
	public boolean needsFolderMigration()
	{
		if(findFolder(OBSOLETE_DRAFT_FOLDER) != null)
			return true;
		if(findFolder(OBSOLETE_OUTBOX_FOLDER) != null)
			return true;
		return false;
	}
	
	public boolean migrateFolders() throws IOException
	{
		// NOTE: Perform the steps from most critical to least!
		BulletinFolder oldOutbox = findFolder(OBSOLETE_OUTBOX_FOLDER);
		BulletinFolder newSealedOutbox = getFolderSealedOutbox();
		BulletinFolder saved = getFolderSaved();
		Vector oldSavedBulletinIds = pullBulletinUidsOutOfFolder(oldOutbox);
		addBulletinIdsToFolder(newSealedOutbox, oldSavedBulletinIds);
		folders.remove(oldOutbox);
		addBulletinIdsToFolder(saved, oldSavedBulletinIds);
		
		BulletinFolder oldDraftFolder = findFolder(OBSOLETE_DRAFT_FOLDER);
		Vector oldDraftBulletinIds = pullBulletinUidsOutOfFolder(oldDraftFolder);
		folders.remove(oldDraftFolder);
		addBulletinIdsToFolder(saved, oldDraftBulletinIds);
		
		saveFolders();

		return true;
	}

	public void addBulletinIdsToFolder(BulletinFolder folder, Vector bulletinUids) throws IOException
	{
		for (int i = 0; i < bulletinUids.size(); i++) 
		{
			UniversalId uid = (UniversalId)bulletinUids.get(i);
			try
			{
				ensureBulletinIsInFolder(folder, uid);
			}
			catch(BulletinOlderException harmlessException)
			{
				System.out.println("Exception: Bulletin:"+uid+" is older.");
			}
		}
	}

	private Vector pullBulletinUidsOutOfFolder(BulletinFolder folder)
	{
		Vector bulletinUids = new Vector();
		if(folder != null)
		{
			for(int i=0; i < folder.getBulletinCount(); ++i)
			{
				UniversalId uid = folder.getBulletinUniversalIdUnsorted(i);
				bulletinUids.add(uid);
				removeBulletinFromFolder(folder, uid);
			}
		}
		return bulletinUids;
	}

	public void createSystemFolders()
	{
		folderSaved = createSystemFolder(SAVED_FOLDER);
		folderDiscarded = createSystemFolder(DISCARDED_FOLDER);
		folderDraftOutbox = createSystemFolder(DRAFT_OUTBOX);
		
		folderSealedOutbox = createSystemFolder(SEALED_OUTBOX);
	}

	public BulletinFolder createSystemFolder(String name)
	{
		BulletinFolder folder = rawCreateFolder(name);
		folder.preventRename();
		folder.preventDelete();
		return folder;
	}
	
	// synchronized because updateOnServerLists is called from background thread
	public synchronized boolean isProbablyOnServer(Bulletin b)
	{
		return getFolderOnServer().contains(b);
	}
	
	// synchronized because updateOnServerLists is called from background thread
	public synchronized boolean isProbablyNotOnServer(Bulletin b)
	{
		return getFolderNotOnServer().contains(b);
	}
	
	public  void setIsOnServer(Bulletin b)
	{
		setIsOnServer(b.getUniversalId());
	}

	// synchronized because updateOnServerLists is called from background thread
	public synchronized void setIsOnServer(UniversalId uid)
	{
		removeBulletinFromFolder(getFolderNotOnServer(), uid);
		try
		{
			getFolderOnServer().add(uid);
		}
		catch(BulletinAlreadyExistsException harmless)
		{
		}
		catch(Exception ignoreForNow)
		{
			// TODO: Figure out if this should be propagated
			ignoreForNow.printStackTrace();
		}
	}

	public  void setIsNotOnServer(Bulletin b)
	{
		setIsNotOnServer(b.getUniversalId());
	}
	
	// synchronized because updateOnServerLists is called from background thread
	public synchronized void setIsNotOnServer(UniversalId uid)
	{
		removeBulletinFromFolder(getFolderOnServer(), uid);
		try
		{
			getFolderNotOnServer().add(uid);
		}
		catch(BulletinAlreadyExistsException harmless)
		{
		}
		catch(Exception ignoreForNow)
		{
			// TODO: Figure out if this should be propagated
			ignoreForNow.printStackTrace();
		}
	}

	// synchronized because updateOnServerLists is called from background thread
	public synchronized void clearOnServerLists()
	{
		getFolderOnServer().removeAll();
		getFolderNotOnServer().removeAll();
	}
	
	public void updateOnServerLists(Set uidsOnServer)
	{
		HashSet uids = new HashSet(1000);
		uids.addAll(getUidsOfAllBulletinRevisions());
		internalUpdateOnServerLists(uidsOnServer, uids);
		saveFolders();
	}
	
	//	 synchronized because updateOnServerLists is called from background thread
	private synchronized void internalUpdateOnServerLists(Set uidsOnServer, HashSet uidsInStore)
	{
		BulletinFolder draftOutbox = getFolderDraftOutbox();
		for(Iterator iter = uidsInStore.iterator(); iter.hasNext();)
		{
			UniversalId uid = (UniversalId) iter.next();
			if(uidsOnServer.contains(uid))
			{
				if(!draftOutbox.contains(uid))
				{
					setIsOnServer(uid);
				}
			}
			else
				setIsNotOnServer(uid);
		}
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
			//System.out.println("Bulletin already exists in destination folder");
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		removeBulletinFromFolder(from, b);
		saveFolders();
	}

	public void removeBulletinFromFolder(BulletinFolder from, Bulletin b)
	{
		removeBulletinFromFolder(from, b.getUniversalId());
	}

	public synchronized void removeBulletinFromFolder(BulletinFolder from, UniversalId uid)
	{
		from.remove(uid);
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
		super.deleteAllData();
		deleteFoldersDatFile();
		resetFolders();
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
			PacketScrubber(Database databaseToUse)
			{
				db = databaseToUse;
			}
			
			public void visit(DatabaseKey key)
			{
				try
				{
					db.scrubRecord(key);
					db.discardRecord(key);
					revisionWasRemoved(key.getUniversalId());
				}
				catch (Exception e)
				{				
					e.printStackTrace();
				}				
			}
			
			Database db;
		}
	
		PacketScrubber ac = new PacketScrubber(getWriteableDatabase());
		getDatabase().visitAllRecords(ac);
		deleteFoldersDatFile();
	}	

	public void signAccountMap() throws MartusSignatureException, IOException
	{
		getWriteableDatabase().signAccountMap();
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
		return getFoldersFileForAccount(getStoreRootDir());
	}

	static public File getFoldersFileForAccount(File AccountDir)
	{
		return new File(AccountDir, "MartusFolders.dat");
	}

	public Bulletin createEmptyBulletin()
	{
		return createEmptyBulletin(getPublicFieldSpecs(), getPrivateFieldSpecs());
	}
	
	public Bulletin createEmptyBulletin(FieldSpec[] publicSpecs, FieldSpec[] privateSpecs)
	{
		Bulletin b = new Bulletin(getSignatureGenerator(), publicSpecs, privateSpecs);
		return b;
	}

	public FieldSpec[] getPrivateFieldSpecs()
	{
		return privateFieldSpecs;
	}

	public FieldSpec[] getPublicFieldSpecs()
	{
		return publicFieldSpecs;
	}

	public synchronized BulletinFolder createOrFindFolder(String name)
	{
		BulletinFolder result = findFolder(name);
		if(result != null)
			return result;
		return createFolder(name);
	}

	public void ensureBulletinIsInFolder(BulletinFolder folder, UniversalId uid) throws IOException, BulletinOlderException
	{
		try
		{
			addBulletinToFolder(folder, uid);
		}
		catch (BulletinAlreadyExistsException ignoreHarmless)
		{
		}
	}
	
	public synchronized void addBulletinToFolder(BulletinFolder folder, UniversalId uidToAdd) throws BulletinAlreadyExistsException, IOException, BulletinOlderException
	{
		Bulletin b = getBulletinRevision(uidToAdd);
		if(b == null)
			return;
		
		if(folder.isVisible() && !isLeaf(uidToAdd))
			throw new BulletinOlderException();
		
		folder.add(uidToAdd);

		String accountId = uidToAdd.getAccountId();
		Vector visibleFolders = getAllVisibleFolders();
		BulletinHistory history = b.getHistory();
		for(int i = 0; i < history.size(); ++i)
		{
			String localId = history.get(i);
			UniversalId uidToRemove = UniversalId.createFromAccountAndLocalId(accountId, localId);
			for(Iterator f = visibleFolders.iterator(); f.hasNext();)
			{
				BulletinFolder folderToFix = (BulletinFolder) f.next();
				if( folderToFix.contains(uidToRemove))
				{
					try
					{
						folderToFix.add(uidToAdd);
					}
					catch (BulletinAlreadyExistsException ignoreHarmless)
					{
					}
					removeBulletinFromFolder(folderToFix, uidToRemove);
				}
			}
		}
	}
	
	public void migrateFoldersForBulletinVersioning()
	{
		Vector allBulletinUids = getUidsOfAllBulletinRevisions();
		Vector visibleFolders = getAllVisibleFolders();
		for(Iterator i = allBulletinUids.iterator(); i.hasNext();)
		{
			UniversalId bId = (UniversalId) i.next();
			Bulletin b = getBulletinRevision(bId);
			if(b == null)
			{
				System.out.println("Migration Error: Unable to find bulletin: "+bId);
				continue;
			}
			if(!isLeaf(b.getUniversalId()))
			{
				for(Iterator f = visibleFolders.iterator(); f.hasNext();)
				{
					BulletinFolder folderToFix = (BulletinFolder) f.next();
					folderToFix.remove(bId);
				}
			}
		}
	}
	
	public synchronized void addRepairBulletinToFolders(UniversalId uId) throws BulletinAlreadyExistsException, IOException
	{
		Bulletin b = getBulletinRevision(uId);
		if(b == null)
			return;
			
		String name = getOrphanFolderName();
		BulletinFolder orphanFolder = createOrFindFolder(name);
		orphanFolder.add(b);
		
		BulletinFolder outboxFolder =  (b.isDraft())? getFolderDraftOutbox():getFolderSealedOutbox();
		if (outboxFolder != null)
			outboxFolder.add(b);
	}
	

	private void initializeFolders()
	{
		folders = new Vector();
		createSystemFolders();
	}
	
	public void setPublicFieldTags(FieldSpec[] newTags)
	{
		publicFieldSpecs = newTags;
	}

	public int quarantineUnreadableBulletins()
	{
		class Quarantiner implements Database.PacketVisitor
		{
			public Quarantiner(Database databaseToUse)
			{
				db = databaseToUse;
			}
			
			public void visit(DatabaseKey key)
			{
				InputStreamWithSeek in = null;
				try
				{
					in = db.openInputStream(key, getSignatureVerifier());
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
						db.moveRecordToQuarantine(key);
						revisionWasRemoved(key.getUniversalId());
					}
					catch (RecordHiddenException shouldNeverHappen)
					{
						shouldNeverHappen.printStackTrace();
					}
				}
			}

			Database db;
			int quarantinedCount;
		}

		Quarantiner visitor = new Quarantiner(getWriteableDatabase());
		visitAllBulletinRevisions(visitor);
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
		StringBuffer xml = new StringBuffer();
		xml.append(MartusClientXml.getFolderListTagStart());

		for(int index=0; index < getFolderCount(); ++index)
		{
			BulletinFolder folder = getFolder(index);
			xml.append(folderToXml(folder));
		}

		xml.append(MartusClientXml.getFolderListTagEnd());
		return new String(xml);
	}

	public String folderToXml(BulletinFolder folder)
	{
		StringBuffer xml = new StringBuffer();
		xml.append(MartusClientXml.getFolderTagStart(folder.getName()));
		for(int index=0; index < folder.getBulletinCount(); ++index)
		{
			UniversalId uid = folder.getBulletinUniversalIdUnsorted(index);
			if(uid == null)
				System.out.println("WARNING: Unexpected null id");
			xml.append(MartusXml.getIdTag(uid.toString()));
		}
		xml.append(MartusClientXml.getFolderTagEnd());
		return new String(xml);
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
		public XmlFolderListLoader(ClientBulletinStore storeToFill)
		{
			super(MartusClientXml.tagFolderList);
			store = storeToFill;
		}
		
		public SimpleXmlDefaultLoader startElement(String tag)
			throws SAXParseException
		{
			if(tag.equals(MartusClientXml.tagFolder))
				return new XmlFolderLoader(tag, store);
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
		
		ClientBulletinStore store;
	}
	
	class XmlFolderLoader extends SimpleXmlDefaultLoader
	{
		public XmlFolderLoader(String tag, ClientBulletinStore storeToFill)
		{
			super(tag);
			store = storeToFill;
		}
		
		String convertLegacyFolder(String name)
		{
			if(name.equals("Outbox"))
				name = OBSOLETE_OUTBOX_FOLDER;
			else if(name.equals("Sent Bulletins"))
				name = SAVED_FOLDER;
			else if(name.equals("Draft Bulletins"))
				name = OBSOLETE_DRAFT_FOLDER;
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
		
		ClientBulletinStore store;
		BulletinFolder folder;
	}

	public static class BulletinAlreadyExistsException extends Exception 
	{
	}
	
	public static class BulletinOlderException extends Exception 
	{
	}

	public void importZipFileBulletin(File zipFile, BulletinFolder toFolder, boolean forceSameUids) throws
			InvalidPacketException,
			SignatureVerificationException,
			WrongPacketTypeException,
			CryptoException,
			InvalidBase64Exception, 
			BulletinAlreadyExistsException, 
			IOException, 
			BulletinOlderException
	{
		ZipFile zip = new ZipFile(zipFile);
		try
		{
			BulletinHeaderPacket bhp = BulletinHeaderPacket.loadFromZipFile(zip, getSignatureVerifier());
			UniversalId uid = bhp.getUniversalId();

			boolean isSealed = bhp.getStatus().equals(Bulletin.STATUSSEALED);
			if(forceSameUids || !isMyBulletin(bhp) || isSealed)
			{
				importZipFileToStoreWithSameUids(zipFile);
			}
			else
				uid = importZipFileToStoreWithNewUids(zipFile);

			addBulletinToFolder(toFolder, uid);
		}
		finally
		{
			zip.close();
		}

		saveFolders();
	}

	public boolean isMyBulletin(BulletinHeaderPacket bhp)
	{
		return getAccountId().equals(bhp.getAccountId());
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
		Bulletin imported = BulletinZipImporter.loadFromFileAsNewDraft(security, inputFile);
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
	
	public void populateFieldSpecCacheFromDatabase()
	{
		knownFieldSpecCache.clearAndInitialize();
	}
	
	public Vector getAllKnownFieldSpecs()
	{
		return knownFieldSpecCache.getAllKnownFieldSpecs();
	}
	
	protected void loadCache()
	{
		//System.out.println("BulletinStore.loadCache");
		File cacheFile = getCacheFileForAccount(getStoreRootDir());
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

	protected void saveCache()
	{
		//System.out.println("BulletinStore.saveCache");
		try
		{
			byte[] sessionKeyCache = getSignatureGenerator().getSessionKeyCache();
			File cacheFile = new File(getStoreRootDir(), CACHE_FILE_NAME);
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

	public BulletinCache getCache()
	{
		return bulletinDataCache;
	}

	public static File getCacheFileForAccount(File accountDir)
	{
		return new File(accountDir, CACHE_FILE_NAME);
	}

	public boolean bulletinHasExtraFields(Bulletin b)
	{
		return !FieldSpec.isAllFieldsPresent(b.getPublicFieldSpecs(), getPublicFieldSpecs());
	}

	public Bulletin createClone(Bulletin original, FieldSpec[] publicFieldSpecsToUse, FieldSpec[] privateFieldSpecsToUse) throws Exception 
	{
		Bulletin clone = createEmptyBulletin(publicFieldSpecsToUse, privateFieldSpecsToUse);
		clone.createDraftCopyOf(original, getDatabase());
		return clone;
	}

	public Vector getUidsOfAllBulletinRevisions()
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
		visitAllBulletinRevisions(uidCollector);
		return uidCollector.uidList;
	}

	public static int maxCachedBulletinCount = 100;

	public static final String SAVED_FOLDER = "%Sent";
	public static final String DISCARDED_FOLDER = "%Discarded";
	public static final String SEARCH_RESULTS_BULLETIN_FOLDER = "%SearchResults";
	public static final String RECOVERED_BULLETIN_FOLDER = "%RecoveredBulletins";
	public static final String RETRIEVE_SEALED_BULLETIN_FOLDER = "%RetrievedMyBulletin";
	public static final String RETRIEVE_SEALED_FIELD_OFFICE_BULLETIN_FOLDER = "%RetrievedFieldOfficeBulletin";
	public static final String RETRIEVE_DRAFT_BULLETIN_FOLDER = "%RetrievedMyBulletinDraft";
	public static final String RETRIEVE_DRAFT_FIELD_OFFICE_BULLETIN_FOLDER = "%RetrievedFieldOfficeBulletinDraft";
	public static final String DAMAGED_BULLETIN_FOLDER = "%DamagedBulletins";
	private static final String DRAFT_OUTBOX = "*DraftOutbox";
	private static final String SEALED_OUTBOX = "*SealedOutbox";
	private static final String ON_SERVER_FOLDER = "*OnServer";
	private static final String NOT_ON_SERVER_FOLDER = "*NotOnServer";

	public static final String OBSOLETE_OUTBOX_FOLDER = "%OutBox";
	public static final String OBSOLETE_DRAFT_FOLDER = "%Draft";
	public static final String WAS_SENT_YES = "WasSentYes";
	public static final String WAS_SENT_NO = "WasSentNo";

	private static final String CACHE_FILE_NAME = "skcache.dat";
	private static final String OBSOLETE_CACHE_FILE_NAME = "sfcache.dat";

	private Vector folders;
	private BulletinFolder folderSaved;
	private BulletinFolder folderDiscarded;
	private BulletinFolder folderDraftOutbox;
	private BulletinFolder folderSealedOutbox;
	private boolean loadedLegacyFolders;

	private FieldSpec[] publicFieldSpecs;
	private FieldSpec[] privateFieldSpecs;
	BulletinCache bulletinDataCache;
	KnownFieldSpecCache knownFieldSpecCache;
}
