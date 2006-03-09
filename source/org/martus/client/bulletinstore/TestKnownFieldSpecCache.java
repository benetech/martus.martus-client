/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005, Beneficent
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
import java.io.IOException;
import java.util.Set;
import java.util.zip.ZipFile;

import org.martus.client.test.MockMartusApp;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.MockClientDatabase;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeDateRange;
import org.martus.common.fieldspec.FieldTypeMultiline;
import org.martus.util.TestCaseEnhanced;

public class TestKnownFieldSpecCache extends TestCaseEnhanced
{
	public TestKnownFieldSpecCache(String name)
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		security = MockMartusSecurity.createClient();
		app = MockMartusApp.create(security);
		ClientBulletinStore store = app.getStore();
		cache = store.knownFieldSpecCache;
	}
	
	public void tearDown() throws Exception
	{
		app.deleteAllFiles();
		assertEquals("Didn't clear cache?", 0, cache.getAllKnownFieldSpecs().size());
	}

	public void testClearAndInitialize() throws Exception
	{
		Bulletin one = createSampleBulletin(security);
		app.saveBulletin(one, app.getFolderDraftOutbox());
		Set specs = cache.getAllKnownFieldSpecs();
		assertEquals("wrong number of specs?", 2, specs.size());
		assertContains("public spec not found?", publicSpecs[0], specs);
		assertContains("private spec not found?", privateSpecs[0], specs);
	}

	public void testSaveBulletin() throws Exception
	{
		Bulletin withCustom = createSampleBulletin(security);
		app.saveBulletin(withCustom, app.getFolderDraftOutbox());
		Set specsAfterSave = cache.getAllKnownFieldSpecs();
		int newExpectedCount = publicSpecs.length + privateSpecs.length;
		assertEquals("didn't add new specs?", newExpectedCount, specsAfterSave.size());
		assertContains("didn't add public?", publicSpecs[0], specsAfterSave);
		assertContains("didn't add private?", privateSpecs[0], specsAfterSave);
		
		// two fieldspecs with same tag name
	}

	public void testIgnoreUnauthorizedBulletins() throws Exception
	{
		MockMartusSecurity otherSecurity = MockMartusSecurity.createOtherClient();
		MockMartusApp otherApp = MockMartusApp.create(otherSecurity);
		Bulletin notOurs = createSampleBulletin(otherSecurity);
		otherApp.saveBulletin(notOurs, otherApp.getFolderDraftOutbox());
		File zipFile = createTempFile();
		BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(otherApp.getStore().getDatabase(), notOurs.getDatabaseKey(), zipFile, otherSecurity);
		
		app.getStore().importBulletinZipFile(new ZipFile(zipFile));
		Set specsWithNotOurs = cache.getAllKnownFieldSpecs();
		assertEquals("didn't ignore other author's bulletin?",0, specsWithNotOurs.size());
	}

	public void testDeleteAndImportBulletin() throws Exception
	{
		int expectedCountAfterSaveOrImport = publicSpecs.length + privateSpecs.length;
		Bulletin toImport = createSampleBulletin(security);
		app.saveBulletin(toImport, app.getFolderDraftOutbox());
		assertEquals("save didn't add specs?", expectedCountAfterSaveOrImport, cache.getAllKnownFieldSpecs().size());

		File zipFile = createTempFile();
		BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(app.getStore().getDatabase(), toImport.getDatabaseKey(), zipFile, security);
		app.getStore().destroyBulletin(toImport);
		Set specsAfterDelete = cache.getAllKnownFieldSpecs();
		assertEquals("didn't remove specs from deleted bulletin?", 0, specsAfterDelete.size());
		
		app.getStore().importBulletinZipFile(new ZipFile(zipFile));
		Set specsAfterImport = cache.getAllKnownFieldSpecs();
		assertEquals("didn't include imported specs?", expectedCountAfterSaveOrImport, specsAfterImport.size());
	}
	
	public void testSaveAndLoad() throws Exception
	{
		app.loadSampleData();
		ByteArrayOutputStream saved = new ByteArrayOutputStream();
		cache.saveToStream(saved);
		ByteArrayInputStream loadable = new ByteArrayInputStream(saved.toByteArray());
		KnownFieldSpecCache reloaded = new KnownFieldSpecCache(new MockClientDatabase(), security);
		reloaded.loadFromStream(loadable);
		Set specs = reloaded.getAllKnownFieldSpecs();
		assertEquals("Didn't reload properly?", specs, cache.getAllKnownFieldSpecs());
		assertEquals("Didn't load correct count?", sampleDataSpecTags.length, specs.size());
	}
	
	public void testLoadFromBadData() throws Exception
	{
		byte[] badData = {1, 22, 15, 121, 1, 0};
		ByteArrayInputStream badIn = new ByteArrayInputStream(badData);
		KnownFieldSpecCache scratch = new KnownFieldSpecCache(new MockClientDatabase(), security);
		try
		{
			scratch.loadFromStream(badIn);
			fail("Should have thrown");
		}
		catch(IOException ignoreExpected)
		{
		}
	}

	private Bulletin createSampleBulletin(MartusCrypto authorSecurity)
	{
		Bulletin b = new Bulletin(authorSecurity, publicSpecs, privateSpecs);
		b.set(publicSpecs[0].getTag(), "Just any text");
		b.set(privateSpecs[0].getTag(), "Just any text");
		return b;
	}
	
	String[] sampleDataSpecTags = {
			"language", "title", "eventdate", "entrydate",  
			"author", "organization", "location",
			"summary", "keywords", "publicinfo", "privateinfo",
		};
	FieldSpec[] publicSpecs = {FieldSpec.createCustomField("frodo", "Younger Baggins", new FieldTypeMultiline()),}; 
	FieldSpec[] privateSpecs = {FieldSpec.createCustomField("bilbo", "Older Baggins", new FieldTypeDateRange()),};
	
	MockMartusSecurity security;
	MockMartusApp app;
	KnownFieldSpecCache cache;
}
