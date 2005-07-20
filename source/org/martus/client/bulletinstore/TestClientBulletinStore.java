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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.martus.client.bulletinstore.ClientBulletinStore.BulletinAlreadyExistsException;
import org.martus.client.bulletinstore.ClientBulletinStore.BulletinOlderException;
import org.martus.client.core.MartusClientXml;
import org.martus.client.test.MockBulletinStore;
import org.martus.common.HQKey;
import org.martus.common.HQKeys;
import org.martus.common.MartusXml;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.bulletin.BulletinForTesting;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.MockDatabase;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.UniversalIdForTesting;
import org.martus.util.Stopwatch;
import org.martus.util.TestCaseEnhanced;


public class TestClientBulletinStore extends TestCaseEnhanced
{
	static Stopwatch sw = new Stopwatch();
	
    public TestClientBulletinStore(String name) {
        super(name);
    }

	public void TRACE(String text)
	{
		//System.out.println("before " + text + ": " + sw.elapsed());
		sw.start();
	}


    public void setUp() throws Exception
    {
    	super.setUp();
		store = new MockBulletinStore();
		db = (MockDatabase)store.getDatabase();
		security = (MockMartusSecurity)store.getSignatureGenerator();

    	if(tempFile1 == null)
    	{
			tempFile1 = createTempFileWithData(sampleBytes1);
			tempFile2 = createTempFileWithData(sampleBytes2);
    	}
    	
    	if(customSpecs == null)
    	{
    		FieldSpec title = new FieldSpec(FieldSpec.TYPE_NORMAL);
    		title.setTag(Bulletin.TAGTITLE);
    		
    		customSpecs = new FieldSpec[] {title};
    	}
    }

    public void tearDown() throws Exception
    {
    	assertEquals("Still some mock streams open?", 0, db.getOpenStreamCount());
		store.deleteAllData();
		super.tearDown();
	}

    public void testBasics()
    {
		TRACE("testBasics");

		BulletinFolder folder = store.createFolder("blah");
		assertEquals(false, (folder == null));

		Bulletin b = store.createEmptyBulletin();
		assertEquals("wrong author?", "", b.get("Author"));
		assertEquals("wrong account?", security.getPublicKeyString(), b.getAccount());
	}
    
    public void testGetAllVisibleFolders() throws Exception
	{
		MockBulletinStore clientStore = new MockBulletinStore(security);
		assertEquals("Should only have 4 folders", 4, clientStore.getAllFolders().size());
		assertEquals("Should only have 2 visible folders", 2, clientStore.getAllVisibleFolders().size());
		clientStore.createFolder("visible1");
		clientStore.createFolder("visible2");
		clientStore.createFolder("*invisible1");
		assertEquals("Should now have 7 folders", 7, clientStore.getAllFolders().size());
		assertEquals("Should now have 4 visible folders", 4, clientStore.getAllVisibleFolders().size());
	}
    
   public void testMigrateFoldersForBulletinVersioning() throws Exception
   {
		MockBulletinStore clientStore = new MockBulletinStore(security);
		Bulletin original = store.createEmptyBulletin();
		original.setSealed();
		clientStore.saveBulletin(original);
		BulletinFolder folderA = clientStore.createFolder("A");
		BulletinFolder folderB = clientStore.createFolder("B");
		BulletinFolder invisiblefolderC = clientStore.createFolder("*C");
		
		UniversalId originalUid = original.getUniversalId();
		clientStore.addBulletinToFolder(folderA,originalUid);
		clientStore.addBulletinToFolder(folderB,originalUid);
		clientStore.addBulletinToFolder(invisiblefolderC,originalUid);
		
		assertTrue("original not in folder A?", folderA.contains(original));
		assertTrue("original not in folder B?", folderB.contains(original));
		assertTrue("original not in folder C?", invisiblefolderC.contains(original));
	
		Bulletin newVersion = store.createClone(original, customSpecs, customSpecs);
		clientStore.saveBulletin(newVersion);
		assertTrue("original should still be in folder A?", folderA.contains(original));
		assertTrue("original should still be in folder B?", folderB.contains(original));
		assertTrue("original should be in folder C?", invisiblefolderC.contains(original));
	
		folderA.add(newVersion);
		folderB.add(newVersion);
		invisiblefolderC.add(newVersion);
		
		assertTrue("original should still be in folder A for this test?", folderA.contains(original));
		assertTrue("original should still be in folder B for this test?", folderB.contains(original));
		assertTrue("original should still be in folder C?", invisiblefolderC.contains(original));
		assertTrue("newVersion not in folder A?", folderA.contains(newVersion));
		assertTrue("newVersion not in folder B?", folderB.contains(newVersion));
		assertTrue("newVersion not in folder C?", invisiblefolderC.contains(newVersion));
		
		clientStore.migrateFoldersForBulletinVersioning();
		assertFalse("original should not be in folder A after migration", folderA.contains(original));
		assertFalse("original should not be in folder B after migration", folderB.contains(original));
		assertTrue("original should still be in folder C after migration?", invisiblefolderC.contains(original));
		assertTrue("newVersion not in folder A after migration?", folderA.contains(newVersion));
		assertTrue("newVersion not in folder B after migration?", folderB.contains(newVersion));
		assertTrue("newVersion not in folder C after migration?", invisiblefolderC.contains(newVersion));
		
   }

    
    public void testRemoveBulletinFromAllFolders() throws Exception
	{
    	Bulletin original = store.createEmptyBulletin();
    	original.setSealed();
    	store.saveBulletin(original);
    	store.setIsOnServer(original);
    	assertTrue("original not on server?", store.isProbablyOnServer(original));

    	Bulletin clone = store.createClone(original, customSpecs, customSpecs);
    	store.saveBulletin(clone);
    	store.setIsOnServer(clone);
    	
    	assertTrue("new version not on server?", store.isProbablyOnServer(clone));
    	assertTrue("original still not on server?", store.isProbablyOnServer(original));
    	
    	store.removeBulletinFromAllFolders(clone);
    	assertFalse("didn't remove original?", store.isProbablyOnServer(original));
	}
    
    public void testCreateCloneOfMySealed() throws Exception
	{
    	Bulletin original = createSealedBulletin(security);
    	
    	{
	    	Bulletin clone = store.createClone(original, customSpecs, customSpecs);
	    	assertEquals("wrong account?", store.getAccountId(), clone.getAccount());
	    	assertNotEquals("not new local id?", original.getLocalId(), clone.getLocalId());
	    	assertEquals("no data?", original.get(Bulletin.TAGTITLE), clone.get(Bulletin.TAGTITLE));
	    	assertEquals("Did not kept hq?", 1, clone.getAuthorizedToReadKeys().size());
	    	assertTrue("not draft?", clone.isDraft());
	    	assertEquals("wrong public field specs?", customSpecs.length, clone.getPublicFieldSpecs().length);
	    	assertEquals("wrong private field specs?", customSpecs.length, clone.getPrivateFieldSpecs().length);
	    	BulletinHistory history = clone.getHistory();
			assertEquals("no history?", 1, history.size());
	    	assertEquals("wrong ancestor?", original.getLocalId(), history.get(0));
    	}
	}
    
    public void testCreateCloneOfMyDraft() throws Exception
	{
    	Bulletin original = createSealedBulletin(security);
    	original.setDraft();
    	
    	{
	    	Bulletin clone = store.createClone(original, customSpecs, customSpecs);
	    	assertEquals("wrong account?", store.getAccountId(), clone.getAccount());
	    	assertNotEquals("not new local id?", original.getLocalId(), clone.getLocalId());
	    	assertEquals("no data?", original.get(Bulletin.TAGTITLE), clone.get(Bulletin.TAGTITLE));
	    	assertEquals("did not keep hq?", 1, clone.getAuthorizedToReadKeys().size());
	    	assertTrue("not draft?", clone.isDraft());
	    	assertEquals("wrong public field specs?", customSpecs.length, clone.getPublicFieldSpecs().length);
	    	assertEquals("wrong private field specs?", customSpecs.length, clone.getPrivateFieldSpecs().length);
	    	BulletinHistory history = clone.getHistory();
			assertEquals("has history?", 0, history.size());
    	}
	}
    
    public void testCreateCloneOfNotMyBulletin() throws Exception
	{
    	MartusCrypto otherSecurity = MockMartusSecurity.createOtherClient();

    	Bulletin original = createSealedBulletin(otherSecurity);

    	{
	    	Bulletin clone = store.createClone(original, customSpecs, customSpecs);
	    	assertEquals("wrong account?", store.getAccountId(), clone.getAccount());
	    	assertNotEquals("not new local id?", original.getLocalId(), clone.getLocalId());
	    	assertEquals("no data?", original.get(Bulletin.TAGTITLE), clone.get(Bulletin.TAGTITLE));
	    	assertEquals("Did not keep hq?", 1, clone.getAuthorizedToReadKeys().size());
	    	assertTrue("not draft?", clone.isDraft());
	    	assertEquals("wrong public field specs?", customSpecs.length, clone.getPublicFieldSpecs().length);
	    	assertEquals("wrong private field specs?", customSpecs.length, clone.getPrivateFieldSpecs().length);
	    	assertEquals("has history?", 0, clone.getHistory().size());
    	}
	}
    
    private Bulletin createSealedBulletin(MartusCrypto otherSecurity)
	{
		HQKeys oldHq = new HQKeys(new HQKey(fakeHqKey));
    	
    	Bulletin original = new Bulletin(otherSecurity);
    	original.set(Bulletin.TAGTITLE, "oeiwjfio");
    	original.setAuthorizedToReadKeys(oldHq);
    	original.setSealed();
		return original;
	}

	public void testChooseBulletinToUpload() throws Exception
	{
    	BulletinFolder outbox = store.createFolder("*My outbox");
    	BulletinFolder normal = store.createFolder("Normal Folder");
    	int count = 10;
    	Bulletin[] bulletins = new Bulletin[count];
    	for(int i=0; i < count; ++i)
    	{
    		bulletins[i] = store.createEmptyBulletin();
    		store.saveBulletin(bulletins[i]);
        	store.addBulletinToFolder(outbox, bulletins[i].getUniversalId());
        	store.addBulletinToFolder(normal, bulletins[i].getUniversalId());
    	}
    	
    	store.removeBulletinFromFolder(normal, bulletins[3].getUniversalId());
    	store.removeBulletinFromFolder(normal, bulletins[9].getUniversalId());
    	
    	BulletinFolder discarded = store.getFolderDiscarded();
    	store.addBulletinToFolder(discarded, bulletins[3].getUniversalId());
    	store.addBulletinToFolder(discarded, bulletins[6].getUniversalId());
    	store.addBulletinToFolder(discarded, bulletins[9].getUniversalId());

    	int expected[] = {1, 2, 4, 4, 5, 7, 7, 8, 0, 0};
    	for(int startIndex=0; startIndex < count; ++startIndex)
    	{
    		UniversalId gotUid = store.chooseBulletinToUpload(outbox, startIndex).getUniversalId();
    		int gotIndex = -1;
    		for(int i=0; i < bulletins.length; ++i)
    			if(gotUid.equals(bulletins[i].getUniversalId()))
    				gotIndex = i;
    		assertEquals("wrong for " + startIndex, expected[startIndex], gotIndex);
    	}
    	
	}
    
	public void testHasAnyNonDiscardedBulletins() throws Exception
	{
		Bulletin b1 = store.createEmptyBulletin();
		Bulletin b2 = store.createEmptyBulletin();
		store.saveBulletin(b1);
		store.saveBulletin(b2);
		
		BulletinFolder outbox = store.createFolder("*My Outbox");
		store.addBulletinToFolder(outbox, b1.getUniversalId());
		store.addBulletinToFolder(outbox, b2.getUniversalId());

		BulletinFolder visible = store.createFolder("Other Folder");
		store.addBulletinToFolder(visible, b1.getUniversalId());
		store.addBulletinToFolder(visible, b2.getUniversalId());

		assertTrue("thinks some are discarded?", store.hasAnyNonDiscardedBulletins(outbox));
		
		BulletinFolder discarded = store.getFolderDiscarded();
		store.addBulletinToFolder(discarded, b1.getUniversalId());
		assertTrue("2 in x but all discarded?", store.hasAnyNonDiscardedBulletins(outbox));

		store.addBulletinToFolder(discarded, b2.getUniversalId());
		assertFalse("all in x and discarded means we don't have any that has not been discarded?", store.hasAnyNonDiscardedBulletins(outbox));
		store.removeBulletinFromFolder(visible, b1);
		store.removeBulletinFromFolder(visible, b2);
		assertFalse("doesn't see all are discarded?", store.hasAnyNonDiscardedBulletins(outbox));
	}
	
    public void testNeedsFolderMigration()
    {
    	assertFalse("normal store needs migration?", store.needsFolderMigration());
		store.createSystemFolder(ClientBulletinStore.OBSOLETE_OUTBOX_FOLDER);
    	assertTrue("outbox doesn't trigger migration?", store.needsFolderMigration());
    	store.deleteFolder(ClientBulletinStore.OBSOLETE_OUTBOX_FOLDER);
		store.createSystemFolder(ClientBulletinStore.OBSOLETE_DRAFT_FOLDER);
    	assertTrue("drafts doesn't trigger migration?", store.needsFolderMigration());
    	store.deleteFolder(ClientBulletinStore.OBSOLETE_DRAFT_FOLDER);
    }
    
    public void testMigrateFolders() throws Exception
    {

		BulletinFolder outbox = store.createSystemFolder(ClientBulletinStore.OBSOLETE_OUTBOX_FOLDER);
    	Bulletin saved = store.createEmptyBulletin();
    	store.saveBulletin(saved);
    	store.addBulletinToFolder(outbox, saved.getUniversalId());

    	BulletinFolder drafts = store.createSystemFolder(ClientBulletinStore.OBSOLETE_DRAFT_FOLDER);
    	Bulletin draft = store.createEmptyBulletin();
    	store.saveBulletin(draft);
    	store.addBulletinToFolder(drafts, draft.getUniversalId());
    	
		assertFalse("Already saved folders?", store.getFoldersFile().exists());
    	assertTrue("Migration failed?", store.migrateFolders());
		assertTrue("Didn't save changes?", store.getFoldersFile().exists());

    	assertEquals(2, store.getFolderSaved().getBulletinCount());
    	assertEquals(1, store.getFolderSealedOutbox().getBulletinCount());
    	assertEquals(0, store.getFolderSealedOutbox().find(saved.getUniversalId()));

    	assertNull("Didn't remove outbox?", store.findFolder(ClientBulletinStore.OBSOLETE_OUTBOX_FOLDER));
    	assertNull("Didn't remove drafts folder?", store.findFolder(ClientBulletinStore.OBSOLETE_DRAFT_FOLDER));
    	
    }
    
	public void testGetStandardFieldNames()
	{
		FieldSpec[] publicFields = StandardFieldSpecs.getDefaultPublicFieldSpecs();
		Set publicTags = new HashSet();
		for(int i = 0; i < publicFields.length; ++i)
			publicTags.add(publicFields[i].getTag());
		assertEquals(true, publicTags.contains("author"));
		assertEquals(false, publicTags.contains("privateinfo"));
		assertEquals(false, publicTags.contains("nope"));
		assertEquals(true, publicTags.contains("language"));
		assertEquals(true, publicTags.contains("organization"));

		FieldSpec[] privateFields = StandardFieldSpecs.getDefaultPrivateFieldSpecs();
		Set privateTags = new HashSet();
		for(int i = 0; i < privateFields.length; ++i)
			privateTags.add(privateFields[i].getTag());
		
		assertEquals(true, privateTags.contains("privateinfo"));
		assertEquals(false, privateTags.contains("nope"));
	}

	public void testGetAllBulletinUids() throws Exception
	{
		TRACE("testGetAllBulletinUids");
		Vector empty = store.getAllBulletinLeafUids();
		assertEquals("not empty?", 0, empty.size());

		Bulletin b = store.createEmptyBulletin();
		store.saveBulletin(b);
		Vector one = store.getAllBulletinLeafUids();
		assertEquals("not one?", 1, one.size());
		UniversalId gotUid = (UniversalId)one.get(0);
		UniversalId bUid = b.getUniversalId();
		assertEquals("wrong uid 1?", bUid, gotUid);

		Bulletin b2 = store.createEmptyBulletin();
		store.saveBulletin(b2);
		Vector two = store.getAllBulletinLeafUids();
		assertEquals("not two?", 2, two.size());
		assertTrue("missing 1?", two.contains(b.getUniversalId()));
		assertTrue("missing 2?", two.contains(b2.getUniversalId()));
	}

	public void testDestroyBulletin() throws Exception
	{
		TRACE("testDestroyBulletin");
		int originalRecordCount = db.getRecordCount();

		Bulletin b = store.createEmptyBulletin();
		AttachmentProxy a1 = new AttachmentProxy(tempFile1);
		AttachmentProxy a2 = new AttachmentProxy(tempFile2);
		b.addPublicAttachment(a1);
		assertEquals("added one", 1, b.getPublicAttachments().length);

		b.addPrivateAttachment(a2);
		assertEquals("added 4", 1, b.getPrivateAttachments().length);

		store.saveBulletin(b);
		BulletinFolder f = store.createFolder("test");
		f.add(b);
		store.destroyBulletin(b);
		assertEquals(0, store.getBulletinCount());
		assertEquals(0, f.getBulletinCount());
		assertEquals(originalRecordCount, db.getRecordCount());
		
		BulletinCache cache = store.getCache();
		assertNull("found destroyed bulletin?", cache.find(b.getUniversalId()));
	}

	public void testGetFieldData() throws Exception
	{
		TRACE("testGetFieldData");
		assertEquals(0, store.getBulletinCount());
		String sampleSummary = "Summary Data";
		String sampleEventDate = "11-20-2002";
		Bulletin b = store.createEmptyBulletin();
		b.set(Bulletin.TAGSUMMARY, sampleSummary);
		b.set(Bulletin.TAGEVENTDATE, sampleEventDate);
		b.setDraft();
		store.saveBulletin(b);
		UniversalId uId = b.getUniversalId();
		assertEquals("Wrong summary?", sampleSummary, store.getFieldData(uId, Bulletin.TAGSUMMARY));
		assertEquals("Wrong event date?", sampleEventDate, store.getFieldData(uId, Bulletin.TAGEVENTDATE));
		assertEquals("Wrong status?", b.getStatus(), store.getFieldData(uId, Bulletin.TAGSTATUS));
		assertEquals("Unknown status not set?", "", store.getFieldData(uId, Bulletin.TAGWASSENT));
		store.setIsOnServer(b);
		assertEquals("Status not Sent?", ClientBulletinStore.WAS_SENT_YES, store.getFieldData(uId, Bulletin.TAGWASSENT));
		store.setIsNotOnServer(b);
		assertEquals("Status not unSent?", ClientBulletinStore.WAS_SENT_NO, store.getFieldData(uId, Bulletin.TAGWASSENT));

	}
	public void testSaveBulletin() throws Exception
	{
		TRACE("testSaveBulletin");

		final String initialSummary = "New bulletin";

		assertEquals(0, store.getBulletinCount());

		Bulletin b = store.createEmptyBulletin();
		b.set(Bulletin.TAGSUMMARY, initialSummary);
		store.saveBulletin(b);
		UniversalId uId = b.getUniversalId();
		assertEquals(1, store.getBulletinCount());
		assertEquals(false, (uId.toString().length() == 0));
		assertEquals("not saved initially?", initialSummary, store.getBulletinRevision(uId).get(Bulletin.TAGSUMMARY));

		// re-saving the same bulletin replaces the old one
		UniversalId id = b.getUniversalId();
		store.saveBulletin(b);
		assertEquals(1, store.getBulletinCount());
		assertEquals("Saving should keep same id", id, b.getUniversalId());
		assertEquals("not still saved?", initialSummary, store.getBulletinRevision(uId).get(Bulletin.TAGSUMMARY));

		// unsaved bulletin changes should not be in the store
		b.set(Bulletin.TAGSUMMARY, "not saved yet");
		assertEquals("saved without asking?", initialSummary, store.getBulletinRevision(uId).get(Bulletin.TAGSUMMARY));

		// saving a new bulletin with a non-empty id should retain that id
		int oldCount = store.getBulletinCount();
		b = store.createEmptyBulletin();
		UniversalId uid = b.getBulletinHeaderPacket().getUniversalId();
		store.saveBulletin(b);
		assertEquals(oldCount+1, store.getBulletinCount());
		assertEquals("b uid?", uid, b.getBulletinHeaderPacket().getUniversalId());

		b = store.getBulletinRevision(uid);
		assertEquals("store uid?", uid, b.getBulletinHeaderPacket().getUniversalId());

	}

	public void testFindBulletinById() throws Exception
	{
		TRACE("testFindBulletinById");

		assertEquals(0, store.getBulletinCount());
		UniversalId uInvalidId = UniversalId.createDummyUniversalId();
		Bulletin b = store.getBulletinRevision(uInvalidId);
		assertEquals(true, (b == null));

		b = store.createEmptyBulletin();
		b.set(BulletinConstants.TAGSUMMARY, "whoop-dee-doo");
		b.setDraft();
		store.saveBulletin(b);
		UniversalId id = b.getUniversalId();

		Bulletin b2 = store.getBulletinRevision(id);
		assertEquals(false, (b2 == null));
		assertEquals(b.get(BulletinConstants.TAGSUMMARY), b2.get(BulletinConstants.TAGSUMMARY));
		
		b.setSealed();
		store.saveBulletin(b);

		Bulletin b3 = store.getBulletinRevision(id);
		assertEquals(false, (b3 == null));
		assertEquals(b.get(BulletinConstants.TAGSUMMARY), b3.get(BulletinConstants.TAGSUMMARY));
	}

	public void testDiscardBulletin() throws Exception
	{
		TRACE("testDiscardBulletin");

		BulletinFolder f = store.getFolderSaved();
		assertNotNull("Need Sent folder", f);
		BulletinFolder discarded = store.getFolderDiscarded();
		assertNotNull("Need Discarded folder", f);

		Bulletin start1 = store.createEmptyBulletin();
		store.saveBulletin(start1);
		f.add(start1);

		Bulletin b = f.getBulletinSorted(0);
		assertNotNull("Sent folder should have bulletins", b);

		assertEquals(true, f.contains(b));
		assertEquals(false, discarded.contains(b));
		store.discardBulletin(f, b);
		assertEquals("Bulletin wasn't discarded!", false, f.contains(b));
		assertEquals("Bulletin wasn't copied to Discarded", true, discarded.contains(b));

		Bulletin b2 = store.createEmptyBulletin();
		b2.set("subject", "amazing");
		store.saveBulletin(b2);
		BulletinFolder user1 = store.createFolder("1");
		BulletinFolder user2 = store.createFolder("2");
		user1.add(b2);
		user2.add(b2);

		assertEquals(true, user1.contains(b2));
		assertEquals(true, user2.contains(b2));
		assertEquals(false, discarded.contains(b2));
		store.discardBulletin(user1, b2);
		assertEquals("Bulletin wasn't discarded!", false, user1.contains(b2));
		assertEquals("Copy of bulletin accidentally discarded\n", true, user2.contains(b2));
		assertEquals("Should be in Discarded now", true, discarded.contains(b2));
		store.discardBulletin(user2, b2);
		assertEquals("Bulletin wasn't discarded!", false, user2.contains(b2));
		assertEquals("Should be in Discarded now", true, discarded.contains(b2));

		store.discardBulletin(discarded, b2);
		assertEquals("Should no longer be in Discarded", false, discarded.contains(b2));
		assertNull("Should no longer exist at all", store.getBulletinRevision(b2.getUniversalId()));
	}

	public void testRemoveBulletinFromFolder() throws Exception
	{
		TRACE("testRemoveBulletinFromFolder");

		BulletinFolder f = store.getFolderSaved();
		assertNotNull("Need Sent folder", f);

		Bulletin b1 = store.createEmptyBulletin();
		store.saveBulletin(b1);
		f.add(b1);
		assertEquals(true, f.contains(b1));
		store.removeBulletinFromFolder(f, b1);
		assertEquals(false, f.contains(b1));
	}

	public void testCreateFolder()
	{
		TRACE("testCreateFolder");

		BulletinFolder folder = store.createFolder("blah");
		assertEquals(false, (folder == null));

		BulletinFolder folder2 = store.createFolder("blah");
		assertNull("Can't create two folders with same name", folder2);
	}

	public void testCreateOrFindFolder()
	{
		TRACE("testCreateOrFindFolder");

		assertNull("x shouldn't exist", store.findFolder("x"));
		BulletinFolder folder = store.createOrFindFolder("x");
		assertNotNull("Create x", folder);

		BulletinFolder folder2 = store.createOrFindFolder("x");
		assertEquals(folder, folder2);
	}

	public void testCreateSystemFolders()
	{
		TRACE("testCreateSystemFolders");

		BulletinFolder fSent = store.getFolderSaved();
		assertNotNull("Should have created Sent folder", fSent);

		BulletinFolder fDiscarded = store.getFolderDiscarded();
		assertNotNull("Should have created Discarded folder", fDiscarded);

		BulletinFolder fDraftOutbox = store.getFolderDraftOutbox();
		assertNotNull("Should have created DraftOutbox folder", fDraftOutbox);

		BulletinFolder fSealedOutbox = store.getFolderSealedOutbox();
		assertNotNull("No SealedOutbox?", fSealedOutbox);
	}

	public void testFindFolder()
	{
		TRACE("testFindFolder");

		int count = store.getFolderCount();

		store.createFolder("peter");
		store.createFolder("paul");
		store.createFolder("john");
		store.createFolder("ringo");
		assertEquals(count+4, store.getFolderCount());

		BulletinFolder folder = store.findFolder("paul");
		assertEquals(false, (folder==null));
	}

	public void testRenameFolder()
	{
		TRACE("testRenameFolder");

		assertEquals(false, store.renameFolder("a", "b"));

		BulletinFolder folder = store.createFolder("a");
		assertEquals(true, store.renameFolder("a", "b"));
		assertEquals(null, store.findFolder("a"));
		assertEquals(folder, store.findFolder("b"));

		BulletinFolder f2 = store.createFolder("a");
		assertEquals(false, store.renameFolder("a", "b"));
		assertEquals(folder, store.findFolder("b"));
		assertEquals(f2, store.findFolder("a"));
		
		BulletinFolder f3 = store.createFolder("abc");
		assertEquals(false, store.renameFolder("abc", "*-   abcd"));
		assertEquals(f3, store.findFolder("abc"));
		
		assertEquals(false, store.renameFolder("abc", " abcd"));
		assertEquals(f3, store.findFolder("abc"));
		
		assertEquals(true, store.renameFolder("abc", "ab cd "));
		assertEquals(f3, store.findFolder("ab cd "));
		
		
		BulletinFolder f4 = store.createFolder("folder1");
		assertEquals(false, store.renameFolder("folder1", "fo--d"));
		assertEquals(f4, store.findFolder("folder1"));
		
		BulletinFolder f5 = store.createFolder("folder2");
		assertEquals(false, store.renameFolder("folder2", "fo@\\"));
		assertEquals(f5, store.findFolder("folder2"));
		
	}

	public void testDeleteFolder() throws Exception
	{
		TRACE("testDeleteFolder");

		assertEquals(false, store.deleteFolder("a"));
		BulletinFolder folder = store.createFolder("a");
		assertEquals(true, store.deleteFolder("a"));

		folder = store.createFolder("a");
		assertNotNull("Couldn't create folder a", folder);
		folder.preventDelete();
		assertEquals(false, store.deleteFolder("a"));
		folder = store.findFolder("a");
		assertNotNull("Should have been non-deletable", folder);

		folder = store.createFolder("b");
		assertNotNull("Couldn't create folder b", folder);
		Bulletin b = store.createEmptyBulletin();
		b.set("subject", "golly");
		store.saveBulletin(b);
		folder.add(b);
		assertEquals(true, folder.contains(b));
		store.deleteFolder("b");
		folder = store.getFolderDiscarded();
		assertEquals("B should be in discarded", true, folder.contains(b));
	}

	public void testMoveBulletin() throws Exception
	{
		TRACE("testMoveBulletin");

		BulletinFolder folderA = store.createFolder("a");
		BulletinFolder folderB = store.createFolder("b");
		Bulletin b = store.createEmptyBulletin();
		store.saveBulletin(b);
		assertEquals("not in a", false, folderA.contains(b));
		assertEquals("not in b", false, folderB.contains(b));

		store.moveBulletin(b, folderA, folderB);
		assertEquals("still not in a", false, folderA.contains(b));
		assertEquals("moved into b", true, folderB.contains(b));

		store.moveBulletin(b, folderB, folderA);
		assertEquals("now in a", true, folderA.contains(b));
		assertEquals("no longer in b", false, folderB.contains(b));

		store.moveBulletin(b, folderA, folderA);
		assertEquals("still in a", true, folderA.contains(b));
		assertEquals("still not in b", false, folderB.contains(b));

		store.moveBulletin(b, folderB, folderB);
		assertEquals("still in a", true, folderA.contains(b));
		assertEquals("still not in b again", false, folderB.contains(b));
	}
	
	public void testSetIsOnServer() throws Exception
	{
		Bulletin b = store.createEmptyBulletin();
		store.saveBulletin(b);
		store.setIsOnServer(b);
		assertTrue("not in on?", store.isProbablyOnServer(b));
		assertFalse("in not on?", store.isProbablyNotOnServer(b));
		store.setIsOnServer(b);
		assertTrue("not still in on?", store.isProbablyOnServer(b));
		assertFalse("now in not on?", store.isProbablyNotOnServer(b));
		
		store.setIsNotOnServer(b);
		store.setIsOnServer(b);
		assertTrue("not again in on?", store.isProbablyOnServer(b));
		assertFalse("still in not on?", store.isProbablyNotOnServer(b));
	}

	public void testSetIsNotOnServer() throws Exception
	{
		Bulletin b = store.createEmptyBulletin();
		store.saveBulletin(b);
		store.setIsNotOnServer(b);
		assertTrue("not in not on?", store.isProbablyNotOnServer(b));
		assertFalse("in on?", store.isProbablyOnServer(b));
		store.setIsNotOnServer(b);
		assertTrue("not still in not on?", store.isProbablyNotOnServer(b));
		assertFalse("now in on?", store.isProbablyOnServer(b));
		
		store.setIsOnServer(b);
		store.setIsNotOnServer(b);
		assertTrue("not again in not on?", store.isProbablyNotOnServer(b));
		assertFalse("still in on?", store.isProbablyOnServer(b));
	}
	
	public void testClearnOnServerLists() throws Exception
	{
		Bulletin on = store.createEmptyBulletin();
		store.saveBulletin(on);
		store.setIsOnServer(on);
		
		Bulletin off = store.createEmptyBulletin();
		store.saveBulletin(off);
		store.setIsNotOnServer(off);
		
		store.clearOnServerLists();
		
		assertFalse("on still on?", store.isProbablyOnServer(on));
		assertFalse("on now off?", store.isProbablyNotOnServer(on));
		assertFalse("off now on?", store.isProbablyOnServer(off));
		assertFalse("off still off?", store.isProbablyNotOnServer(off));
	}
	
	public void testUpdateOnServerLists() throws Exception
	{
		Bulletin sentButNotOnServer = createAndSaveBulletin();
		store.setIsOnServer(sentButNotOnServer);
		
		Bulletin unknownAndNotOnServer = createAndSaveBulletin();
		
		Bulletin unsentToAndNotOnServer = createAndSaveBulletin();
		store.setIsNotOnServer(unsentToAndNotOnServer);
		
		HashSet onServer = new HashSet();

		Bulletin sentAndOnServer = createAndSaveBulletin();
		store.setIsOnServer(sentAndOnServer);
		onServer.add(sentAndOnServer.getUniversalId());
		
		Bulletin unknownButOnServer = createAndSaveBulletin();
		onServer.add(unknownButOnServer.getUniversalId());

		Bulletin unsentButOnServer = createAndSaveBulletin();
		store.setIsNotOnServer(unsentButOnServer);
		onServer.add(unsentButOnServer.getUniversalId());
		
		BulletinFolder draftOutbox = store.getFolderDraftOutbox();
		Bulletin draftInOutboxSentAndOnServer = createAndSaveBulletin();
		store.setIsOnServer(draftInOutboxSentAndOnServer);
		onServer.add(draftInOutboxSentAndOnServer.getUniversalId());
		store.ensureBulletinIsInFolder(draftOutbox, draftInOutboxSentAndOnServer.getUniversalId());
		
		Bulletin draftInOutboxUnknownButOnServer = createAndSaveBulletin();
		onServer.add(draftInOutboxUnknownButOnServer.getUniversalId());
		store.ensureBulletinIsInFolder(draftOutbox, draftInOutboxUnknownButOnServer.getUniversalId());

		Bulletin draftInOutboxUnsentButOnServer = createAndSaveBulletin();
		store.setIsNotOnServer(draftInOutboxUnsentButOnServer);
		onServer.add(draftInOutboxUnsentButOnServer.getUniversalId());
		store.ensureBulletinIsInFolder(draftOutbox, draftInOutboxUnsentButOnServer.getUniversalId());

		store.getFoldersFile().delete();
		assertFalse("already saved folders?", store.getFoldersFile().exists());
		
		store.updateOnServerLists(onServer);
		
		assertTrue("thought sent; not on server", store.isProbablyNotOnServer(sentButNotOnServer));
		assertTrue("unknown; is on server", store.isProbablyNotOnServer(unknownAndNotOnServer));
		assertTrue("thought unsent; not on server", store.isProbablyNotOnServer(unsentToAndNotOnServer));
		
		assertTrue("thought sent; is on server", store.isProbablyOnServer(sentAndOnServer));
		assertTrue("unknown; is on server", store.isProbablyOnServer(unknownButOnServer));
		assertTrue("thought unsent; is on server", store.isProbablyOnServer(unsentButOnServer));
		
		assertTrue("thought sent; in draft outbox; on server", store.isProbablyOnServer(draftInOutboxSentAndOnServer));
		assertFalse("(1) unknown; in draft outbox; on server", store.isProbablyOnServer(draftInOutboxUnknownButOnServer));
		assertFalse("(2) unknown; in draft outbox; on server", store.isProbablyNotOnServer(draftInOutboxUnknownButOnServer));
		assertTrue("thought unsent; in draft outbox; on server", store.isProbablyNotOnServer(draftInOutboxUnsentButOnServer));
		
		assertTrue("didn't save folders?", store.getFoldersFile().exists());
	}
	
	private Bulletin createAndSaveBulletin() throws Exception
	{
		Bulletin b = store.createEmptyBulletin();
		store.saveBulletin(b);
		return b;
	}

	public void testAddBulletinToFolder() throws Exception
	{
		TRACE("testAddBulletinToFolder");

		Bulletin b = store.createEmptyBulletin();
		store.saveBulletin(b);
		UniversalId id = b.getUniversalId();
		BulletinFolder folder = store.createFolder("test");
		store.addBulletinToFolder(folder, id);
		assertEquals("now in folder", true, folder.contains(b));
		try
		{
			store.addBulletinToFolder(folder, id);
			fail("should have thrown exists exception");
		}
		catch (BulletinAlreadyExistsException expectedException)
		{
		}
		assertEquals("still in folder", true, folder.contains(b));
		UniversalId bFakeId = UniversalIdForTesting.createFromAccountAndPrefix("aa", "abc");
		store.addBulletinToFolder(folder, bFakeId);
		UniversalId badId2 = UniversalId.createDummyUniversalId();
		assertEquals("bad bulletin", -1, folder.find(badId2));

	}
	
	public void testAddBulletinToFolderRemovesAncestors() throws Exception
	{
		FieldSpec[] publicFields = StandardFieldSpecs.getDefaultPublicFieldSpecs();
		FieldSpec[] privateFields = StandardFieldSpecs.getDefaultPrivateFieldSpecs();
		
		BulletinFolder aFolder = store.createFolder("blah");

		Bulletin original = store.createEmptyBulletin();
		original.setSealed();
		store.saveBulletin(original);
		store.addBulletinToFolder(aFolder, original.getUniversalId());
		assertEquals(1, aFolder.getBulletinCount());

		Bulletin firstClone = store.createClone(original, publicFields, privateFields);
		firstClone.setSealed();
		store.saveBulletin(firstClone);
		
		Bulletin unrelated = store.createEmptyBulletin();
		store.saveBulletin(unrelated);
		store.addBulletinToFolder(aFolder, unrelated.getUniversalId());
		assertEquals(2, aFolder.getBulletinCount());
		

		store.addBulletinToFolder(aFolder, firstClone.getUniversalId());
		assertEquals(2, aFolder.getBulletinCount());
		assertTrue("lost unrelated (1)?", aFolder.contains(unrelated));
		assertTrue("didn't update to first clone?", aFolder.contains(firstClone));
		
		Bulletin lastClone = store.createClone(firstClone, publicFields, privateFields);
		lastClone.setSealed();
		store.saveBulletin(lastClone);
		BulletinFolder otherFolder = store.getFolderDiscarded();
		store.addBulletinToFolder(otherFolder, lastClone.getUniversalId());
		assertEquals(2, aFolder.getBulletinCount());
		assertTrue("lost unrelated (2)?", aFolder.contains(unrelated));
		assertTrue("didn't update to later clone?", aFolder.contains(lastClone));
	}
	
	class BulletinUidCollector implements Database.PacketVisitor
	{
		public void visit(DatabaseKey key)
		{
			uids.add(key.getUniversalId());
		}

		Vector uids = new Vector();
	}
	
	public void testAddOriginalBulletinToFolderWithNewerVersion() throws Exception
	{
		FieldSpec[] publicFields = StandardFieldSpecs.getDefaultPublicFieldSpecs();
		FieldSpec[] privateFields = StandardFieldSpecs.getDefaultPrivateFieldSpecs();
		MockBulletinStore clientStore = new MockBulletinStore(security);
		Bulletin original = clientStore.createEmptyBulletin();
		original.setSealed();

		Bulletin clone = clientStore.createClone(original, publicFields, privateFields);
		clone.setSealed();
		clientStore.saveBulletinForTesting(clone);
		BulletinUidCollector collector = new BulletinUidCollector();
		clientStore.visitAllBulletinRevisions(collector);
		assertEquals("should have 1 bulletin", 1, collector.uids.size());
		clientStore.saveBulletinForTesting(original);
		BulletinUidCollector collector2 = new BulletinUidCollector();
		clientStore.visitAllBulletinRevisions(collector2);
		assertEquals("should now have 2 bulletin", 2, collector2.uids.size());

		BulletinFolder aFolder = clientStore.createFolder("a");
		clientStore.addBulletinToFolder(aFolder, clone.getUniversalId());
		assertEquals("Should only have 1 bulletin in folder", 1, aFolder.getBulletinCount());
		try
		{
			clientStore.addBulletinToFolder(aFolder, original.getUniversalId());
			fail("Should have thrown here.");
		}
		catch(BulletinOlderException expected)
		{
		}
		assertEquals("Should still only have 1 bulletin in folder since there is a newer version", 1, aFolder.getBulletinCount());
		clientStore.deleteAllBulletins();
	}
	
	public void testAddingBulletinVersionThenOriginalToVisibleAndInvisibleFolders() throws Exception
	{
		FieldSpec[] publicFields = StandardFieldSpecs.getDefaultPublicFieldSpecs();
		FieldSpec[] privateFields = StandardFieldSpecs.getDefaultPrivateFieldSpecs();
		MockBulletinStore clientStore = new MockBulletinStore(security);
		Bulletin original = clientStore.createEmptyBulletin();
		original.setSealed();

		Bulletin newerVersion = clientStore.createClone(original, publicFields, privateFields);
		newerVersion.setSealed();
		clientStore.saveBulletinForTesting(newerVersion);
		clientStore.saveBulletinForTesting(original);

		BulletinFolder visibleFolderA = clientStore.createFolder("a");
		assertTrue("Should be a visibleFolder", visibleFolderA.isVisible());
		clientStore.addBulletinToFolder(visibleFolderA, newerVersion.getUniversalId());
		assertEquals("Should only have 1 bulletin in visible folder", 1, visibleFolderA.getBulletinCount());
		try
		{
			clientStore.addBulletinToFolder(visibleFolderA, original.getUniversalId());
			fail("Should have thrown an exception");
		}
		catch(BulletinOlderException expected)
		{
		}
		assertEquals("Should still only have 1 bulletin in visible folder since there is a newer version", 1, visibleFolderA.getBulletinCount());
		assertTrue("Should still have newer version only", visibleFolderA.contains(newerVersion));

		BulletinFolder invisibleFolderC = clientStore.createFolder("*c");
		assertFalse("Should be an invisibleFolder", invisibleFolderC.isVisible());
		clientStore.addBulletinToFolder(invisibleFolderC, newerVersion.getUniversalId());
		assertEquals("Should only have 1 bulletin in invisible folder", 1, invisibleFolderC.getBulletinCount());
		clientStore.addBulletinToFolder(invisibleFolderC, original.getUniversalId());
		assertEquals("Should now have 2 bulletin in invisible folder", 2, invisibleFolderC.getBulletinCount());

		clientStore.deleteAllBulletins();
	}

	public void testAddingBulletinOriginalThenNewVersionToVisibleAndInvisibleFolders() throws Exception
	{
		FieldSpec[] publicFields = StandardFieldSpecs.getDefaultPublicFieldSpecs();
		FieldSpec[] privateFields = StandardFieldSpecs.getDefaultPrivateFieldSpecs();
		MockBulletinStore clientStore = new MockBulletinStore(security);
		Bulletin original = clientStore.createEmptyBulletin();
		original.setSealed();

		Bulletin newVersion = clientStore.createClone(original, publicFields, privateFields);
		newVersion.setSealed();
		clientStore.saveBulletinForTesting(original);

		BulletinFolder visibleFolderA = clientStore.createFolder("a");
		BulletinFolder visibleFolderB = clientStore.createFolder("b");
		BulletinFolder invisibleFolderC = clientStore.createFolder("*c");

		clientStore.addBulletinToFolder(visibleFolderA, original.getUniversalId());
		clientStore.addBulletinToFolder(visibleFolderB, original.getUniversalId());
		clientStore.addBulletinToFolder(invisibleFolderC, original.getUniversalId());
		
		assertEquals("Should only have 1 bulletin in visible folder A", 1, visibleFolderA.getBulletinCount());
		assertEquals("Should only have 1 bulletin in visible folder B", 1, visibleFolderB.getBulletinCount());
		assertEquals("Should only have 1 bulletin in invisible folder C", 1, invisibleFolderC.getBulletinCount());
		
		clientStore.saveBulletinForTesting(newVersion);
		clientStore.addBulletinToFolder(visibleFolderA, newVersion.getUniversalId());
		assertTrue("visibleFolder A should contain the new version", visibleFolderA.contains(newVersion));
		assertTrue("visibleFolder B should contain the new version", visibleFolderB.contains(newVersion));
		assertFalse("invisibleFolder C Should not contain the new version", invisibleFolderC.contains(newVersion));
		assertFalse("visibleFolder A should not contain the original version", visibleFolderA.contains(original));
		assertFalse("visibleFolder B should not contain the original version", visibleFolderB.contains(original));
		assertTrue("invisibleFolder C Should contain the original version", invisibleFolderC.contains(original));

		clientStore.addBulletinToFolder(invisibleFolderC, newVersion.getUniversalId());
		assertTrue("invisibleFolder C Should still contain the original version", invisibleFolderC.contains(original));
		assertTrue("invisibleFolder C Should now also contain the new version", invisibleFolderC.contains(newVersion));
		
		clientStore.deleteAllBulletins();
	}

	public void testFolderToXml() throws Exception
	{
		TRACE("testFolderToXml");

		BulletinFolder folder = store.createFolder("Test");
		String xml = store.folderToXml(folder);
		assertEquals(MartusClientXml.getFolderTagStart("Test") + MartusClientXml.getFolderTagEnd(), xml);

		Bulletin b = store.createEmptyBulletin();
		store.saveBulletin(b);
		folder.add(b);
		xml = store.folderToXml(folder);
		assertStartsWith(MartusClientXml.getFolderTagStart("Test"), xml);
		assertContains(MartusXml.getIdTag(folder.getBulletinSorted(0).getUniversalIdString()), xml);
		assertEndsWith(MartusClientXml.getFolderTagEnd(), xml);

	}

	public void testFoldersToXml() throws Exception
	
	{
		TRACE("testFoldersToXml");

		int i;
		String expected;

		expected = MartusClientXml.getFolderListTagStart();
		Vector originalFolderNames = store.getAllFolderNames();
		for(i = 0; i < originalFolderNames.size(); ++i)
		{
			BulletinFolder folder = store.findFolder((String)originalFolderNames.get(i));
			expected += store.folderToXml(folder);
		}
		expected += MartusClientXml.getFolderListTagEnd();
		assertEquals(expected, store.foldersToXml());

		BulletinFolder f1 = store.createFolder("First");
		Bulletin b = store.createEmptyBulletin();
		store.saveBulletin(b);
		f1.add(b);

		expected = MartusClientXml.getFolderListTagStart();
		Vector updatedFolderNames = store.getAllFolderNames();
		for(i = 0; i < updatedFolderNames.size(); ++i)
		{
			BulletinFolder folder = store.findFolder((String)updatedFolderNames.get(i));
			expected += store.folderToXml(folder);
		}
		expected += MartusClientXml.getFolderListTagEnd();
		assertEquals(expected, store.foldersToXml());
	}

	public void testLoadXmlNoFolders()
	{
		TRACE("testLoadXmlNoFolders");

		int count = store.getFolderCount();
		String xml = "<FolderList></FolderList>";
		store.internalLoadFolders(xml);
		assertEquals(0, store.getBulletinCount());
		assertEquals(count, store.getFolderCount());
		assertNull("found?", store.findFolder("fromxml"));
	}

	public void testLoadXmlFolders()
	{
		TRACE("testLoadXmlFolders");

		int count = store.getFolderCount();
		String xml = "<FolderList><Folder name='one'></Folder><Folder name='two'></Folder></FolderList>";
		store.internalLoadFolders(xml);
		assertEquals(count+2, store.getFolderCount());
		assertNotNull("Folder one must exist", store.findFolder("one"));
		assertNotNull("Folder two must exist", store.findFolder("two"));
		assertNull("Folder three must not exist", store.findFolder("three"));
	}

	public void testLoadXmlLegacyFolders() throws Exception
	{
		TRACE("testLoadXmlFolders");

		int systemFolderCount = store.getFolderCount();

		ClientBulletinStore tempStore = new MockBulletinStore();
		String xml = "<FolderList><Folder name='Sent Bulletins'></Folder><Folder name='new two'></Folder></FolderList>";
		tempStore.internalLoadFolders(xml);
		assertTrue("Legacy folder not detected?", tempStore.needsLegacyFolderConversion());
		assertEquals(systemFolderCount + 1, tempStore.getFolderCount());
		assertNotNull("Folder %Sent must exist", tempStore.findFolder("%Sent"));
		assertNull("Folder Sent Bulletins must not exist", tempStore.findFolder("Sent Bulletins"));
		assertNotNull("Folder two new must exist", tempStore.findFolder("new two"));
		assertNull("Folder three must not exist", tempStore.findFolder("three"));
		xml = "<FolderList><Folder name='%Sent'></Folder><Folder name='new two'></Folder></FolderList>";
		tempStore.internalLoadFolders(xml);
		assertFalse("Not Legacy folder didn't return false on load", tempStore.needsLegacyFolderConversion());
		tempStore.deleteAllData();
	}

	/* missing tests:
		- invalid xml (empty, badly nested tags, two root nodes)
		- <Id> not nested within <Folder>
		- <Field> not nested within <Bulletin>
		- <Folder> or <Bulletin> outside <FolderList> or <BulletinList>
		- Missing folder name attribute, bulletin id attribute, field name attribute
		- Empty bulletin id
		- Illegal bulletin id
		- Duplicate bulletin id
		- Folder id that is blank or isn't a bulletin
		- Folder name blank or duplicate
		- Bulletin field name isn't one of our predefined field names
		- Confirm that attributes are case-sensitive
	*/

	public void testDatabaseBulletins() throws Exception
	{
		TRACE("testDatabaseBulletins");

		assertEquals("empty", 0, store.getBulletinCount());

		Bulletin b = store.createEmptyBulletin();
		final String author = "Mr. Peabody";
		b.set(Bulletin.TAGAUTHOR, author);
		store.saveBulletin(b);
		store.saveFolders();
		assertEquals("saving", 1, store.getBulletinCount());
		assertEquals("keys", 3*store.getBulletinCount(), db.getRecordCount());

		ClientBulletinStore newStoreSameDatabase = new MockBulletinStore(db, store.getSignatureGenerator());
		newStoreSameDatabase.loadFolders();
		assertEquals("loaded", 1, newStoreSameDatabase.getBulletinCount());
		Bulletin b2 = newStoreSameDatabase.getBulletinRevision(b.getUniversalId());
		assertEquals("id", b.getLocalId(), b2.getLocalId());
		assertEquals("author", b.get(Bulletin.TAGAUTHOR), b2.get(Bulletin.TAGAUTHOR));
		assertEquals("wrong security?", store.getSignatureGenerator(), b2.getSignatureGenerator());
		newStoreSameDatabase.deleteAllData();
	}

	public void testDatabaseFolders() throws Exception
	{
		TRACE("testDatabaseFolders");

		final String folderName = "Gotta work";
		int systemFolderCount = store.getFolderCount();
		BulletinFolder f = store.createFolder(folderName);
		Bulletin b = store.createEmptyBulletin();
		store.saveBulletin(b);
		f.add(b);
		store.saveFolders();

		assertEquals("keys", 3*store.getBulletinCount(), db.getRecordCount());

		File storeRootDir = store.getStoreRootDir();
		ClientBulletinStore store = new ClientBulletinStore(security);
		store.doAfterSigninInitialization(storeRootDir, db);
		assertEquals("before load", systemFolderCount, store.getFolderCount());
		store.loadFolders();
		assertEquals("loaded", 1+systemFolderCount, store.getFolderCount());
		BulletinFolder f2 = store.findFolder(folderName);
		assertNotNull("folder", f2);
		assertEquals("bulletins in folder", 1, f2.getBulletinCount());
		assertEquals("contains", true, f2.contains(b));
		store.deleteAllData();
	}

	public void testLoadAllDataWithErrors() throws Exception
	{
		TRACE("testLoadAllDataWithErrors");
		Bulletin b = store.createEmptyBulletin();
		store.saveBulletin(b);
		BulletinHeaderPacket bhp = b.getBulletinHeaderPacket();
		FieldDataPacket fdp = b.getFieldDataPacket();
		DatabaseKey headerKey = DatabaseKey.createLegacyKey(b.getUniversalId());
		UniversalId.createFromAccountAndLocalId(b.getAccount(), fdp.getLocalId());

		security.fakeSigVerifyFailure = true;
		store.loadFolders();

		security.fakeSigVerifyFailure = false;
		store.loadFolders();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);
		byte[] bytes = out.toByteArray();

		bytes[0] = '!';
		String invalidPacketString = new String(bytes, "UTF-8");
		db.writeRecord(headerKey, invalidPacketString);
		store.loadFolders();
	}

	public void testClearFolder() throws Exception
	{
		TRACE("testClearFolder");
		Bulletin b1 = store.createEmptyBulletin();
		store.saveBulletin(b1);
		Bulletin b2 = store.createEmptyBulletin();
		store.saveBulletin(b2);
		BulletinFolder folder = store.createFolder("blah");
		folder.add(b1);
		folder.add(b2);
		assertEquals(store.getBulletinCount(), folder.getBulletinCount());
		store.clearFolder("blah");
		assertEquals(0, folder.getBulletinCount());
	}

	public void testSave() throws Exception
	{
		TRACE("testSave");
		//TODO: This was moved in from TestBulletin, and it may
		//not be needed--compare with testSaveBulletin
		int oldCount = store.getBulletinCount();
		Bulletin b = store.createEmptyBulletin();
		b.set("author", "testsave");
		store.saveBulletin(b);
		assertEquals(oldCount+1, store.getBulletinCount());
		b = store.getBulletinRevision(b.getUniversalId());
		assertEquals("testsave", b.get("author"));
		boolean empty = (b.getLocalId().length() == 0);
		assertEquals("Saved ID must be non-empty\n", false, empty);

		Bulletin b2 = store.createEmptyBulletin();
		store.saveBulletin(b2);
		assertNotEquals("Saved ID must be unique\n", b.getLocalId(), b2.getLocalId());

		store.saveBulletin(b2);
	}

	public void testLastSavedTime() throws Exception
	{
		TRACE("testLastSavedTime");
		Bulletin b = store.createEmptyBulletin();
		long createdTime = b.getLastSavedTime();
		assertEquals("time already set?", BulletinHeaderPacket.TIME_UNKNOWN, createdTime);

		Thread.sleep(200);
		store.saveBulletin(b);
		long firstSavedTime = b.getLastSavedTime();
		assertNotEquals("Didn't update time saved?", createdTime, firstSavedTime);
		long delta2 = Math.abs(firstSavedTime - System.currentTimeMillis());
		assertTrue("time wrong?", delta2 < 1000);

		Thread.sleep(200);
		Bulletin b2 = store.loadFromDatabase(DatabaseKey.createLegacyKey(b.getUniversalId()));
		long loadedTime = b2.getLastSavedTime();
		assertEquals("Didn't keep time saved?", firstSavedTime, loadedTime);
	}

	public void testClearFolderCausesSave() throws Exception
	{
		TRACE("testClearFolderCausesSave");

		store.deleteAllData();
		Bulletin b = store.createEmptyBulletin();
		BulletinFolder f = store.getFolderSaved();
		store.addBulletinToFolder(f, b.getUniversalId());

		store.deleteAllData();
		store.clearFolder(f.getName());
		assertTrue("clearFolder f ", store.getFoldersFile().exists());
		DatabaseKey bulletinKey = DatabaseKey.createLegacyKey(b.getUniversalId());
		assertNull("clearFolder b ", store.getDatabase().readRecord(bulletinKey, security));

		store.saveBulletin(b);
		store.destroyBulletin(b);
		assertNull("destroyBulletin b ", store.getDatabase().readRecord(bulletinKey, security));
	}

	public void testDeleteFolderCausesSave() throws Exception, IOException, CryptoException
	{
		TRACE("testDeleteFolderCausesSave");
		
		DatabaseKey foldersKey = DatabaseKey.createLegacyKey(UniversalId.createDummyUniversalId());
		store.deleteAllData();
		Bulletin b = store.createEmptyBulletin();
		store.createFolder("z");
		db.discardRecord(foldersKey);
		store.deleteFolder("z");
		assertTrue("deleteFolder f ", store.getFoldersFile().exists());
		DatabaseKey bulletinKey = DatabaseKey.createLegacyKey(b.getUniversalId());
		assertNull("deleteFolder b ", store.getDatabase().readRecord(bulletinKey, security));
	}

	public void testRenameFolderCausesSave() throws Exception, IOException, CryptoException
	{
		TRACE("testRenameFolderCausesSave");

		DatabaseKey foldersKey = DatabaseKey.createLegacyKey(UniversalId.createDummyUniversalId());
		store.deleteAllData();
		Bulletin b = store.createEmptyBulletin();
		store.createFolder("x");
		db.discardRecord(foldersKey);
		store.renameFolder("x", "b");
		assertTrue("renameFolder f ", store.getFoldersFile().exists());
		DatabaseKey bulletinKey = DatabaseKey.createLegacyKey(b.getUniversalId());
		assertNull("renameFolder b ", store.getDatabase().readRecord(bulletinKey, security));
	}

	public void testSaveFoldersDoesNotSaveBulletins() throws Exception, IOException, CryptoException
	{
		TRACE("testSaveFoldersDoesNotSaveBulletins");

		store.deleteAllData();
		Bulletin b = store.createEmptyBulletin();
		store.createFolder("a");
		store.saveFolders();
		assertTrue("createFolder f ", store.getFoldersFile().exists());
		DatabaseKey bulletinKey = DatabaseKey.createLegacyKey(b.getUniversalId());
		assertNull("createFolder b ", store.getDatabase().readRecord(bulletinKey, security));
	}

	public void testSaveBulletinDoesNotSaveFolders() throws Exception, IOException, CryptoException
	{
		TRACE("testSaveBulletinDoesNotSaveFolders");

		store.deleteAllData();
		Bulletin b = store.createEmptyBulletin();
		store.saveBulletin(b);

		assertEquals("save bulletin f ", false, store.getFoldersFile().exists());

		DatabaseKey bulletinKey = DatabaseKey.createLegacyKey(b.getUniversalId());
		assertNotNull("save bulletin b ", store.getDatabase().readRecord(bulletinKey, security));
	}

	public void testImportZipFileWithAttachmentSealed() throws Exception
	{
		TRACE("testImportZipFileWithAttachmentSealed");
		
		Bulletin original = store.createEmptyBulletin();
		DatabaseKey originalKey = DatabaseKey.createLegacyKey(original.getUniversalId());
		AttachmentProxy a = new AttachmentProxy(tempFile1);
		AttachmentProxy aPrivate = new AttachmentProxy(tempFile2);
		original.set(Bulletin.TAGTITLE, "abbc");
		original.set(Bulletin.TAGPRIVATEINFO, "priv");
		original.addPublicAttachment(a);
		original.addPrivateAttachment(aPrivate);
		original.setSealed();
		store.saveBulletinForTesting(original);
		File zipFile = createTempFileFromName("$$$MartusTestZipSealed");
		Bulletin loaded = store.loadFromDatabase(originalKey);
		BulletinForTesting.saveToFile(db,loaded, zipFile, store.getSignatureVerifier());
		store.deleteAllData();
		assertEquals("still a record?", 0, db.getRecordCount());

		store.importZipFileToStoreWithSameUids(zipFile);
		assertEquals("Packet count incorrect", 5, db.getRecordCount());

		DatabaseKey headerKey = DatabaseKey.createLegacyKey(loaded.getBulletinHeaderPacket().getUniversalId());
		DatabaseKey dataKey = DatabaseKey.createLegacyKey(loaded.getFieldDataPacket().getUniversalId());
		DatabaseKey privateKey = DatabaseKey.createLegacyKey(loaded.getPrivateFieldDataPacket().getUniversalId());
		AttachmentProxy gotAttachment = loaded.getPublicAttachments()[0];
		DatabaseKey attachmentKey = DatabaseKey.createLegacyKey(gotAttachment.getUniversalId());
		AttachmentProxy gotPrivateAttachment = loaded.getPrivateAttachments()[0];
		DatabaseKey attachmentPrivateKey = DatabaseKey.createLegacyKey(gotPrivateAttachment.getUniversalId());

		assertTrue("Header Packet missing", db.doesRecordExist(headerKey));
		assertTrue("Data Packet missing", db.doesRecordExist(dataKey));
		assertTrue("Private Packet missing", db.doesRecordExist(privateKey));
		assertTrue("Attachment Packet missing", db.doesRecordExist(attachmentKey));
		assertTrue("Attachment Private Packet missing", db.doesRecordExist(attachmentPrivateKey));

		Bulletin reloaded = store.loadFromDatabase(originalKey);
		assertEquals("public?", original.get(Bulletin.TAGTITLE), reloaded.get(Bulletin.TAGTITLE));
		assertEquals("private?", original.get(Bulletin.TAGPRIVATEINFO), reloaded.get(Bulletin.TAGPRIVATEINFO));

		File tempRawFilePublic = createTempFileFromName("$$$MartusTestImpSealedZipRawPublic");
		BulletinLoader.extractAttachmentToFile(db, reloaded.getPublicAttachments()[0], security, tempRawFilePublic);
		byte[] rawBytesPublic = new byte[sampleBytes1.length];
		FileInputStream in = new FileInputStream(tempRawFilePublic);
		in.read(rawBytesPublic);
		in.close();
		assertEquals("wrong bytes", true, Arrays.equals(sampleBytes1, rawBytesPublic));

		File tempRawFilePrivate = createTempFileFromName("$$$MartusTestImpSealedZipRawPrivate");
		BulletinLoader.extractAttachmentToFile(db, reloaded.getPrivateAttachments()[0], security, tempRawFilePrivate);
		byte[] rawBytesPrivate = new byte[sampleBytes2.length];
		FileInputStream in2 = new FileInputStream(tempRawFilePrivate);
		in2.read(rawBytesPrivate);
		in2.close();
		assertEquals("wrong Private bytes", true, Arrays.equals(sampleBytes2, rawBytesPrivate));

		zipFile.delete();
		tempRawFilePublic.delete();
		tempRawFilePrivate.delete();
	}

	public void testImportZipFileBulletin() throws Exception
	{
		TRACE("testImportZipFileBulletin");
		File tempFile = createTempFile();

		Bulletin b = store.createEmptyBulletin();
		BulletinForTesting.saveToFile(db,b, tempFile, store.getSignatureVerifier());

		BulletinFolder folder = store.createFolder("test");

		store.importZipFileBulletin(tempFile, folder, false);
		assertEquals("not imported to store?", 1, store.getBulletinCount());
		assertEquals("not imported to folder?", 1, folder.getBulletinCount());
		assertNull("resaved with draft id?", store.getBulletinRevision(b.getUniversalId()));

		store.deleteAllData();
		folder = store.createFolder("test2");

		b.setSealed();
		BulletinForTesting.saveToFile(db,b, tempFile, store.getSignatureVerifier());
		store.importZipFileBulletin(tempFile, folder, false);
		assertEquals("not imported to store?", 1, store.getBulletinCount());
		assertEquals("not imported to folder count?", 1, folder.getBulletinCount());
		assertEquals("not imported to folder uid?", 0, folder.find(b.getUniversalId()));
		assertNotNull("not saved with sealed id?", store.getBulletinRevision(b.getUniversalId()));

		BulletinFolder folder2 = store.createFolder("another");
		store.importZipFileBulletin(tempFile, folder2, false);
		assertEquals("imported to store again?", 1, store.getBulletinCount());
		assertEquals("not imported to another folder uid?", 0, folder2.find(b.getUniversalId()));
	}

	public void testImportZipFileBulletinNotMine() throws Exception
	{
		TRACE("testImportZipFileBulletinNotMine");
		File tempFile = createTempFile();

		Bulletin original = store.createEmptyBulletin();
		BulletinForTesting.saveToFile(db,original, tempFile, store.getSignatureVerifier());

		ClientBulletinStore importer = createTempStore();
		BulletinFolder folder = importer.createFolder("test");
		importer.importZipFileBulletin(tempFile, folder, false);

		Bulletin imported = folder.getBulletinSorted(0);
		assertEquals("changed uid?", original.getUniversalId(), imported.getUniversalId());
		importer.deleteAllData();
	}

	public void testImportZipFileFieldOffice() throws Exception
	{
		TRACE("testImportZipFileFieldOffice");
		File tempFile = createTempFile();

		ClientBulletinStore hqStore = createTempStore();

		Bulletin original = store.createEmptyBulletin();
		HQKeys keys = new HQKeys();
		HQKey key1 = new HQKey(hqStore.getAccountId());
		keys.add(key1);
		original.setAuthorizedToReadKeys(keys);
		original.setSealed();
		BulletinForTesting.saveToFile(db,original, tempFile, store.getSignatureVerifier());

		BulletinFolder folder = hqStore.createFolder("test");
		hqStore.importZipFileBulletin(tempFile, folder, false);

		Bulletin imported = folder.getBulletinSorted(0);
		assertEquals("changed uid?", original.getUniversalId(), imported.getUniversalId());
		hqStore.deleteAllData();
	}

	public void testImportDraftZipFile() throws Exception
	{
		TRACE("testImportDraftZipFile");
		File tempFile = createTempFile();

		Bulletin b = store.createEmptyBulletin();
		BulletinForTesting.saveToFile(db,b, tempFile, store.getSignatureVerifier());
		UniversalId originalUid = b.getUniversalId();

		BulletinFolder folder = store.createFolder("test");
		store.importZipFileBulletin(tempFile, folder, true);
		assertEquals("Didn't fully import?", 1, store.getBulletinCount());
		assertNotNull("Not same ID?", store.getBulletinRevision(originalUid));

		store.importZipFileBulletin(tempFile, folder, false);
		assertEquals("Not different IDs?", 2, store.getBulletinCount());

	}

	public void testImportZipFileWithAttachmentDraft() throws Exception
	{
		TRACE("testImportZipFileWithAttachmentDraft");
		Bulletin original = store.createEmptyBulletin();
		DatabaseKey originalKey = DatabaseKey.createLegacyKey(original.getUniversalId());
		AttachmentProxy a = new AttachmentProxy(tempFile1);
		AttachmentProxy aPrivate = new AttachmentProxy(tempFile2);
		original.set(Bulletin.TAGTITLE, "abc");
		original.set(Bulletin.TAGPRIVATEINFO, "private");
		original.addPublicAttachment(a);
		original.addPrivateAttachment(aPrivate);
		store.saveBulletin(original);

		Bulletin loaded = store.loadFromDatabase(originalKey);

		File zipFile = createTempFileFromName("$$$MartusTestZipDraft");
		BulletinForTesting.saveToFile(db,loaded, zipFile, store.getSignatureVerifier());

		store.deleteAllData();
		assertEquals("still a record?", 0, db.getRecordCount());

		UniversalId savedAsId = store.importZipFileToStoreWithNewUids(zipFile);
		assertEquals("record count not 5?", 5, db.getRecordCount());

		DatabaseKey headerKey = DatabaseKey.createLegacyKey(loaded.getBulletinHeaderPacket().getUniversalId());
		DatabaseKey dataKey = DatabaseKey.createLegacyKey(loaded.getFieldDataPacket().getUniversalId());
		DatabaseKey privateKey = DatabaseKey.createLegacyKey(loaded.getPrivateFieldDataPacket().getUniversalId());
		AttachmentProxy gotAttachment = loaded.getPublicAttachments()[0];
		AttachmentProxy gotAttachmentPrivate = loaded.getPrivateAttachments()[0];
		DatabaseKey attachmentKey = DatabaseKey.createLegacyKey(gotAttachment.getUniversalId());
		DatabaseKey attachmentPrivateKey = DatabaseKey.createLegacyKey(gotAttachmentPrivate.getUniversalId());

		assertEquals("Header Packet present?", false, db.doesRecordExist(headerKey));
		assertEquals("Data Packet present?", false, db.doesRecordExist(dataKey));
		assertEquals("Private Packet present?", false, db.doesRecordExist(privateKey));
		assertEquals("Attachment Public Packet present?", false, db.doesRecordExist(attachmentKey));
		assertEquals("Attachment Private Packet present?", false, db.doesRecordExist(attachmentPrivateKey));

		Bulletin reloaded = store.loadFromDatabase(DatabaseKey.createLegacyKey(savedAsId));

		assertEquals("public?", original.get(Bulletin.TAGTITLE), reloaded.get(Bulletin.TAGTITLE));
		assertEquals("private?", original.get(Bulletin.TAGPRIVATEINFO), reloaded.get(Bulletin.TAGPRIVATEINFO));
		assertEquals("attachment", true, db.doesRecordExist(DatabaseKey.createLegacyKey(reloaded.getPublicAttachments()[0].getUniversalId())));
		assertEquals("attachment Private", true, db.doesRecordExist(DatabaseKey.createLegacyKey(reloaded.getPrivateAttachments()[0].getUniversalId())));

		ByteArrayOutputStream publicStream = new ByteArrayOutputStream();
		BulletinLoader.extractAttachmentToStream(db, reloaded.getPublicAttachments()[0], security, publicStream);
		byte[] rawBytes = publicStream.toByteArray();
		assertEquals("wrong bytes Public", true, Arrays.equals(sampleBytes1,rawBytes));

		ByteArrayOutputStream privateStream = new ByteArrayOutputStream();
		BulletinLoader.extractAttachmentToStream(db, reloaded.getPrivateAttachments()[0], security, privateStream);
		byte[] rawBytesPrivate = privateStream.toByteArray();
		assertEquals("wrong bytes Private", true, Arrays.equals(sampleBytes2, rawBytesPrivate));

		zipFile.delete();
	}

	public void testGetSetOfBulletinUniversalIdsInFolders() throws Exception
	{
		TRACE("testGetSetOfBulletinUniversalIdsInFolders");
		Set emptySet = store.getSetOfBulletinUniversalIdsInFolders();
		assertTrue("not empty to start?", emptySet.isEmpty());

		Bulletin b1 = store.createEmptyBulletin();
		store.saveBulletin(b1);
		Bulletin b2 = store.createEmptyBulletin();
		store.saveBulletin(b2);
		Set stillEmptySet = store.getSetOfBulletinUniversalIdsInFolders();
		assertTrue("not still empty", stillEmptySet.isEmpty());

		store.getFolderSaved().add(b1);
		store.getFolderDiscarded().add(b1);
		store.getFolderDiscarded().add(b2);
		Set two = store.getSetOfBulletinUniversalIdsInFolders();

		assertEquals("not two?", 2, two.size());
		assertTrue("Missing b1?", two.contains(b1.getUniversalId()));
		assertTrue("Missing b2?", two.contains(b2.getUniversalId()));
	}

	public void testGetSetOfOrphanedBulletinUniversalIds() throws Exception
	{
		TRACE("testGetSetOfOrphanedBulletinUniversalIds");
		Set emptySet = store.getSetOfOrphanedBulletinUniversalIds();
		assertTrue("not empty to start?", emptySet.isEmpty());

		Bulletin b1 = store.createEmptyBulletin();
		store.saveBulletin(b1);
		Bulletin b2 = store.createEmptyBulletin();
		store.saveBulletin(b2);

		Set two = store.getSetOfOrphanedBulletinUniversalIds();
		assertEquals("not two?", 2, two.size());
		assertTrue("two Missing b1?", two.contains(b1.getUniversalId()));
		assertTrue("two Missing b2?", two.contains(b2.getUniversalId()));

		store.getFolderSaved().add(b1);
		store.getFolderDiscarded().add(b1);
		Set one = store.getSetOfOrphanedBulletinUniversalIds();
		assertEquals("not one?", 1, one.size());
		assertTrue("one Missing b2?", one.contains(b2.getUniversalId()));

		store.getFolderDraftOutbox().add(b2);
		one = store.getSetOfOrphanedBulletinUniversalIds();
		assertEquals("A bulletin only existing in a hidden folder is orphaned", 1,  one.size());

		store.getFolderSaved().add(b2);
		Set empty = store.getSetOfOrphanedBulletinUniversalIds();
		assertTrue("now b2 is in a visable folder so we should not have any orphans", empty.isEmpty());
	}

	public void testOrphansInHiddenFolders() throws Exception
	{
		TRACE("testOrphansInHiddenFolders");
		Bulletin b1 = store.createEmptyBulletin();
		store.saveBulletin(b1);
		Bulletin b2 = store.createEmptyBulletin();
		store.saveBulletin(b2);

		store.getFolderDraftOutbox().add(b1);
		assertEquals("hidden-only not an orphan?", true, store.isOrphan(b1));

		store.getFolderSaved().add(b2);
		store.getFolderDraftOutbox().add(b2);
		assertEquals("hidden-plus is an orphan?", false, store.isOrphan(b2));
	}

	public void testOrphansInVisibleFolders() throws Exception
	{
		TRACE("testOrphansInVisibleFolders");
		Bulletin b1 = store.createEmptyBulletin();
		store.saveBulletin(b1);

		assertEquals("Not in any folder, bulletin not orphaned?", true, store.isOrphan(b1));
		store.getFolderSaved().add(b1);
		assertEquals("In a visible folder, bulletin is orphaned?", false, store.isOrphan(b1));
	}
	
	
	public void testQuarantineUnreadableBulletinsSimple() throws Exception
	{
		TRACE("testQuarantineUnreadableBulletinsSimple");
		assertEquals("found a bad bulletin in an empty database?", 0, store.quarantineUnreadableBulletins());
		Bulletin b1 = store.createEmptyBulletin();
		store.saveBulletin(b1);
		assertEquals("not one leaf?", 1, store.scanForLeafKeys().size());
		assertEquals("quarantined a good record?", 0, store.quarantineUnreadableBulletins());
		corruptBulletinHeader(b1);
		assertEquals("didn't claim to quarantine 1 record?", 1, store.quarantineUnreadableBulletins());
		DatabaseKey key = DatabaseKey.createLegacyKey(b1.getUniversalId());
		assertTrue("didn't actually quarantine our record?", store.getDatabase().isInQuarantine(key));
		assertEquals("didn't update leaf?", 0, store.scanForLeafKeys().size());
	}

	public void testQuarantineUnreadableBulletinsMany() throws Exception
	{
		TRACE("testQuarantineUnreadableBulletinsMany");
		final int totalCount = 20;
		Bulletin bulletins[] = new Bulletin[totalCount];
		for (int i = 0; i < bulletins.length; i++)
		{
			bulletins[i] = store.createEmptyBulletin();
			store.saveBulletin(bulletins[i]);
		}

		final int badCount = 4;
		DatabaseKey badKeys[] = new DatabaseKey[badCount];
		for (int i = 0; i < badKeys.length; i++)
		{
			int bulletinIndex = i * (totalCount/badCount);
			Bulletin b = bulletins[bulletinIndex];
			badKeys[i] = DatabaseKey.createLegacyKey(b.getUniversalId());
			corruptBulletinHeader(b);
		}

		assertEquals("wrong quarantine count?", badCount, store.quarantineUnreadableBulletins());
		for (int i = 0; i < badKeys.length; i++)
			assertTrue("didn't quarantine " + i, store.getDatabase().isInQuarantine(badKeys[i]));
	}

	private void corruptBulletinHeader(Bulletin b) throws Exception
	{
		UniversalId uid = b.getUniversalId();
		DatabaseKey key = DatabaseKey.createLegacyKey(uid);
		String goodData = db.readRecord(key, security);
		String badData = "x" + goodData;
		db.writeRecord(key, badData);
	}

	private ClientBulletinStore createTempStore() throws Exception
	{
		MockMartusSecurity tempSecurity = MockMartusSecurity.createOtherClient();
		return new MockBulletinStore(tempSecurity);
	}
	
	public void testScrubAllData() throws Exception
	{		
		TRACE("testScrubAllData");
		
		Bulletin b = store.createEmptyBulletin();
		store.saveBulletin(b);
		
		Vector one = store.getUidsOfAllBulletinRevisions();
		assertEquals("not one?", 1, one.size());		
		
		store.scrubAllData();
		Vector empty = store.getUidsOfAllBulletinRevisions();
		assertEquals("not empty?", 0, empty.size());			
	}	


	final int sampleRecordCount = 5;

	static MockBulletinStore store;
	static MockMartusSecurity security;
	static MockDatabase db;
	static FieldSpec[] customSpecs;

	static File tempFile1;
	static File tempFile2;
	static final byte[] sampleBytes1 = {1,1,2,3,0,5,7,11};
	static final byte[] sampleBytes2 = {3,1,4,0,1,5,9,2,7};
	static final String fakeHqKey = "wwwllkjsfdkjf";
}
