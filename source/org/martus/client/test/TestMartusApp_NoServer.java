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

package org.martus.client.test;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Vector;

import org.martus.client.core.BulletinFolder;
import org.martus.client.core.ClientBulletinStore;
import org.martus.client.core.ConfigInfo;
import org.martus.client.core.MartusApp;
import org.martus.client.core.MartusApp.AccountAlreadyExistsException;
import org.martus.client.core.MartusApp.CannotCreateAccountFileException;
import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.client.swingui.EnglishStrings;
import org.martus.client.swingui.UiLocalization;
import org.martus.common.CustomFields;
import org.martus.common.FieldSpec;
import org.martus.common.HQKey;
import org.martus.common.HQKeys;
import org.martus.common.LegacyCustomFields;
import org.martus.common.MartusUtilities;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.clientside.ChoiceItem;
import org.martus.common.clientside.DateUtilities;
import org.martus.common.clientside.Localization;
import org.martus.common.clientside.PasswordHelper;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.common.clientside.test.ServerSideNetworkHandlerNotAvailable;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.FileDatabase;
import org.martus.common.packet.UniversalId;
import org.martus.swing.Utilities;
import org.martus.util.DirectoryUtils;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;

public class TestMartusApp_NoServer extends TestCaseEnhanced
{
	public TestMartusApp_NoServer(String name)
	{
		super(name);
		VERBOSE = false;
	}

	public void setUp() throws Exception
	{
		super.setUp();
		TRACE_BEGIN("setUp");

		mockSecurityForApp = MockMartusSecurity.createClient();

		localization = new UiLocalization(null, EnglishStrings.strings);
		localization.setCurrentLanguageCode("en");
		appWithAccount = MockMartusApp.create(mockSecurityForApp);
		appWithAccount.setSSLNetworkInterfaceHandlerForTesting(new ServerSideNetworkHandlerNotAvailable());

		File keyPairFile = appWithAccount.getCurrentKeyPairFile();
		keyPairFile.delete();
		appWithAccount.getConfigInfoFile().delete();
		appWithAccount.getConfigInfoSignatureFile().delete();

		TRACE_END();
	}

	public void tearDown() throws Exception
	{
		appWithAccount.deleteAllFiles();
		super.tearDown();
	}

	public void testBasics()
	{
		TRACE_BEGIN("testBasics");

		ClientBulletinStore store = appWithAccount.getStore();
		assertNotNull("BulletinStore", store);
		TRACE_END();
	}
	
	public void testGetHelp() throws Exception
	{
		TRACE_BEGIN("testGetHelp");
		File translationDirectory = appWithAccount.martusDataRootDirectory;
		String languageCode = "xx";
		InputStream helpMain = appWithAccount.getHelpMain(languageCode);
		assertNull("Language pack doesn't exists help should return null", helpMain);
		InputStream helpTOC = appWithAccount.getHelpTOC(languageCode);
		assertNull("Language pack doesn't exists help toc should return null", helpTOC);

		File mlpkTranslation = new File(translationDirectory, UiBasicLocalization.getMlpkFilename(languageCode));
		copyResourceFileToLocalFile(mlpkTranslation, "Martus-xx-notSigned.mlp");
		mlpkTranslation.deleteOnExit();
		helpMain = appWithAccount.getHelpMain(languageCode);
		helpTOC = appWithAccount.getHelpTOC(languageCode);
		assertNull("Language pack exists but isn't signed help should return null", helpMain);
		assertNull("Language pack exists but isn't signed help toc should return null", helpTOC);
		mlpkTranslation.delete();

		copyResourceFileToLocalFile(mlpkTranslation, "Martus-xx.mlp");
		mlpkTranslation.deleteOnExit();
		helpMain = appWithAccount.getHelpMain(languageCode);
		UnicodeReader reader = new UnicodeReader(helpMain);
		reader.read();//unused char.
		String line1InFile = reader.readLine(); 
		reader.close();
		String helpTextInFile = "Temp Help File for testing";
		
		helpTOC = appWithAccount.getHelpTOC(languageCode);
		reader = new UnicodeReader(helpTOC);
		reader.read();//unused char.
		String line1InTOCFile = reader.readLine();
		reader.close();
		String helpTextInTOCFile = "chapter 1";

		mlpkTranslation.delete();

		assertNotNull("Language pack exists and is signed help should return not null", helpMain);
		assertNotNull("Language pack exists and is signed help toc should return not null", helpTOC);
		assertEquals("Contents of help didn't match?", helpTextInFile, line1InFile);
		assertEquals("Contents of help TOC didn't match?", helpTextInTOCFile, line1InTOCFile);
		TRACE_END();
	}	
	
	public void testSaveBulletin() throws Exception
	{
		ClientBulletinStore store = appWithAccount.getStore();
		BulletinFolder outbox = store.getFolderDraftOutbox();
		BulletinFolder discarded = store.getFolderDiscarded();
		
		store.getFoldersFile().delete();
		assertFalse("couldn't delete folders?", store.getFoldersFile().exists());
		
		Bulletin b = appWithAccount.createBulletin();
		appWithAccount.saveBulletin(b, outbox);
		DatabaseKey key = DatabaseKey.createDraftKey(b.getUniversalId());
		assertTrue("didn't save?", store.getDatabase().doesRecordExist(key));
		assertTrue("didn't put in outbox?", outbox.contains(b));
		assertTrue("didn't put in saved?", appWithAccount.getFolderSaved().contains(b));
		assertTrue("didn't save folders?", store.getFoldersFile().exists());
		assertFalse("marked as sent?", store.isProbablyOnServer(b));
		assertTrue("didn't mark as unsent?", store.isProbablyNotOnServer(b));
		
		store.setIsOnServer(b);
		store.moveBulletin(b, outbox, discarded);
		appWithAccount.saveBulletin(b, outbox);
		assertFalse("didn't remove from discarded?", discarded.contains(b));
		assertFalse("not unmarked as sent?", store.isProbablyOnServer(b));
		assertTrue("didn't remark as unsent?", store.isProbablyNotOnServer(b));
	}
	
	public void testLoadOldCustomFieldConfigInfo() throws Exception
	{
		ConfigInfo infoToConvert = new ConfigInfo();
		String sampleLegacyFields = "tag1;tag2";
		infoToConvert.setCustomFieldSpecs(sampleLegacyFields);
		CustomFields fields = new CustomFields(MartusApp.getCustomFieldSpecs(infoToConvert));

		CustomFields expected = new CustomFields(LegacyCustomFields.parseFieldSpecsFromString(sampleLegacyFields));
		assertEquals(expected.toString(), fields.toString());
	}
	
	public void testLoadConvertedCustomFieldInfo() throws Exception
	{
		ConfigInfo convertedInfo = new ConfigInfo();
		String newFields = "new,label;another,show";
		FieldSpec[] newSpecs = LegacyCustomFields.parseFieldSpecsFromString(newFields);
		CustomFields convertedFields = new CustomFields(newSpecs);
		convertedInfo.setCustomFieldXml(convertedFields.toString());
		CustomFields fields = new CustomFields(MartusApp.getCustomFieldSpecs(convertedInfo));

		CustomFields expected = new CustomFields(LegacyCustomFields.parseFieldSpecsFromString(newFields));
		assertEquals(expected.toString(), fields.toString());
	}
	
	public void testSetDefaultUiState() throws Exception
	{
		UiBasicLocalization testLocalization = new UiBasicLocalization(null, noEnglishStrings);
		File tmpFile = createTempFile();
		MartusApp.setInitialUiDefaultsFromFileIfPresent(testLocalization, tmpFile);
		assertNull("File doesn't exist localization should not be set", testLocalization.getCurrentLanguageCode());
		FileOutputStream out = new FileOutputStream(tmpFile);
		out.write("invalidLanguageCode".getBytes());
		out.close();
		MartusApp.setInitialUiDefaultsFromFileIfPresent(testLocalization, tmpFile);
		assertNull("Invalid language code, localization should not be set", testLocalization.getCurrentLanguageCode());
		tmpFile.delete();
		out = new FileOutputStream(tmpFile);
		out.write("en".getBytes());
		out.close();
		MartusApp.setInitialUiDefaultsFromFileIfPresent(testLocalization, tmpFile);
		assertEquals("English should be set", Localization.ENGLISH, testLocalization.getCurrentLanguageCode());
		assertEquals("English code should set DMY correctly", DateUtilities.MDY_SLASH.getCode(), testLocalization.getCurrentDateFormatCode());
		tmpFile.delete();
		out = new FileOutputStream(tmpFile);
		out.write("es".getBytes());
		out.close();
		MartusApp.setInitialUiDefaultsFromFileIfPresent(testLocalization, tmpFile);
		assertEquals("Spanish should be set", Localization.SPANISH, testLocalization.getCurrentLanguageCode());
		assertEquals("Spanish code should set MDY correctly", DateUtilities.DMY_SLASH.getCode(), testLocalization.getCurrentDateFormatCode());
		tmpFile.delete();
		out = new FileOutputStream(tmpFile);
		out.write("ru".getBytes());
		out.close();
		MartusApp.setInitialUiDefaultsFromFileIfPresent(testLocalization, tmpFile);
		assertEquals("Russian should be set", Localization.RUSSIAN, testLocalization.getCurrentLanguageCode());
		assertEquals("Russian code should set MDY Dot correctly", DateUtilities.DMY_DOT.getCode(), testLocalization.getCurrentDateFormatCode());
		tmpFile.delete();
	}

	public void testDiscardBulletinsFromFolder() throws Exception
	{
		Bulletin b1 = appWithAccount.createBulletin();
		Bulletin b2 = appWithAccount.createBulletin();
		Bulletin b3 = appWithAccount.createBulletin();
		b3.setSealed();
		appWithAccount.getStore().saveBulletin(b1);
		appWithAccount.getStore().saveBulletin(b2);
		appWithAccount.getStore().saveBulletin(b3);

		BulletinFolder f1 = appWithAccount.createUniqueFolder("testFolder");
		f1.add(b1);
		f1.add(b2);
		f1.add(b3);
		BulletinFolder draftOutbox = appWithAccount.getFolderDraftOutbox();
		draftOutbox.add(b1);
		BulletinFolder sealedOutbox = appWithAccount.getFolderSealedOutbox();
		sealedOutbox.add(b3);
		
		appWithAccount.discardBulletinsFromFolder(f1, new Bulletin[] {b1, b3});
		assertEquals(3, appWithAccount.getStore().getBulletinCount());
		assertEquals(1, f1.getBulletinCount());
		assertEquals("removed from draft outbox?", 1, draftOutbox.getBulletinCount());
		assertEquals("removed from sealed outbox?", 1, sealedOutbox.getBulletinCount());
		
		Database db = appWithAccount.getWriteableDatabase();
		DatabaseKey key = DatabaseKey.createLegacyKey(b1.getBulletinHeaderPacket().getUniversalId());
		db.discardRecord(key);

		BulletinFolder trash = appWithAccount.getFolderDiscarded();
		try
		{
			appWithAccount.discardBulletinsFromFolder(trash, new Bulletin[] {b1, b3});
			fail("discard damaged record should have thrown");
		}
		catch(IOException ignoreExpectedException)
		{
		}
	}
	
	public void testDeleteAllBulletins() throws Exception
	{
		Bulletin b1 = appWithAccount.createBulletin();
		Bulletin b2 = appWithAccount.createBulletin();
		appWithAccount.getStore().saveBulletin(b1);
		appWithAccount.getStore().saveBulletin(b2);

		BulletinFolder f1 = appWithAccount.createUniqueFolder("testFolder");
		BulletinFolder f2 = appWithAccount.getFolderDraftOutbox();
		f1.add(b1);
		f2.add(b2);
		assertEquals(2, appWithAccount.getStore().getBulletinCount());		
		appWithAccount.deleteAllBulletinsAndUserFolders();
		assertEquals(0, appWithAccount.getStore().getBulletinCount());
		assertNotNull("System Folder deleted?", appWithAccount.getFolderDraftOutbox());
		assertNull("User Folder Not deleted?", appWithAccount.getStore().findFolder(f1.getName()));
	}
	

	public void testDbInitializerExceptionForMissingAccountMap() throws Exception
	{
		File fakeDataDirectory = null;
		File packetDirectory = null;
		File subdirectory = null;

		try
		{
			fakeDataDirectory = createTempFileFromName("$$$MartusTestApp");
			fakeDataDirectory.delete();
			fakeDataDirectory.mkdir();

			packetDirectory = new File(fakeDataDirectory, "packets");
			subdirectory = new File(packetDirectory, "anyfolder");
			subdirectory.mkdirs();

			try
			{
				UiBasicLocalization localization = new UiBasicLocalization(fakeDataDirectory, noEnglishStrings);
				MartusApp app = new MartusApp(mockSecurityForApp, fakeDataDirectory, localization);
				app.setCurrentAccount("some user", app.getMartusDataRootDirectory());
				app.doAfterSigninInitalization();
				fail("Should have thrown because map is missing");
			}
			catch(FileDatabase.MissingAccountMapException expectedException)
			{
			}
		}
		finally
		{
			if(subdirectory != null)
				subdirectory.delete();
			if(packetDirectory != null)
				packetDirectory.delete();
			if(fakeDataDirectory != null)
			{
				new File(fakeDataDirectory, "MartusConfig.dat").delete();
				new File(fakeDataDirectory, "MartusConfig.sig").delete();
				new File(fakeDataDirectory, "AccountToken.txt").delete();
				fakeDataDirectory.delete();
			}
		}
	}

	public void testDbInitializerExceptionForMissingAccountMapSignature() throws Exception
	{
		File fakeDataDirectory = null;
		File packetDirectory = null;
		File subdirectory = null;
		File acctMap = null;

		try
		{
			fakeDataDirectory = createTempFileFromName("$$$MartusTestApp");
			fakeDataDirectory.delete();
			fakeDataDirectory.mkdir();

			packetDirectory = new File(fakeDataDirectory, "packets");
			subdirectory = new File(packetDirectory, "anyfolder");
			subdirectory.mkdirs();

			acctMap = new File(packetDirectory,"acctmap.txt");
			acctMap.deleteOnExit();

			FileOutputStream out = new FileOutputStream(acctMap.getPath(), true);
			UnicodeWriter writer = new UnicodeWriter(out);
			writer.writeln("noacct=123456789");
			writer.flush();
			out.flush();
			writer.close();

			try
			{
				UiBasicLocalization localization = new UiBasicLocalization(fakeDataDirectory, noEnglishStrings);
				MartusApp app = new MartusApp(mockSecurityForApp, fakeDataDirectory, localization);
				app.setCurrentAccount("some user", app.getMartusDataRootDirectory());
				app.doAfterSigninInitalization();
				fail("Should have thrown because of missing map signature");
			}
			catch(FileDatabase.MissingAccountMapSignatureException expectedException)
			{
			}
		}
		finally
		{
			if(acctMap != null )
				acctMap.delete();
			if(subdirectory != null)
				subdirectory.delete();
			if(packetDirectory != null)
				packetDirectory.delete();
			if(fakeDataDirectory != null)
			{
				new File(fakeDataDirectory, "MartusConfig.dat").delete();
				new File(fakeDataDirectory, "MartusConfig.sig").delete();
				new File(fakeDataDirectory, "AccountToken.txt").delete();
				fakeDataDirectory.delete();
			}
		}
	}

	public void testDbInitializerExceptionForInvalidAccountMapSignature() throws Exception
	{
		File fakeDataDirectory = null;
		File packetDirectory = null;
		File subdirectory = null;
		File acctMap = null;
		File signatureFile = null;

		try
		{
			fakeDataDirectory = createTempFileFromName("$$$MartusTestApp");
			fakeDataDirectory.delete();
			fakeDataDirectory.mkdir();

			packetDirectory = new File(fakeDataDirectory, "packets");
			subdirectory = new File(packetDirectory, "anyfolder");
			subdirectory.mkdirs();

			acctMap = new File(packetDirectory,"acctmap.txt");
			acctMap.deleteOnExit();

			UnicodeWriter writer = new UnicodeWriter(acctMap);
			writer.writeln("noacct=123456789");
			writer.flush();
			writer.close();

			signatureFile = new File(packetDirectory,"acctmap.txt.sig");
			signatureFile.deleteOnExit();

			writer = new UnicodeWriter(signatureFile);
			writer.writeln("a fake signature");
			writer.flush();
			writer.close();

			try
			{
				UiBasicLocalization localization = new UiBasicLocalization(fakeDataDirectory, noEnglishStrings);
				MartusApp app = new MartusApp(mockSecurityForApp, fakeDataDirectory, localization);
				app.setCurrentAccount("some user", app.getMartusDataRootDirectory());
				app.doAfterSigninInitalization();
				fail("Should have thrown because of invalid map signature");
			}
			catch(MartusUtilities.FileVerificationException expectedException)
			{
			}
		}
		finally
		{
			if(acctMap != null )
				acctMap.delete();
			if(signatureFile != null)
				signatureFile.delete();
			if(subdirectory != null)
				subdirectory.delete();
			if(packetDirectory != null)
				packetDirectory.delete();
			if(fakeDataDirectory != null)
			{
				new File(fakeDataDirectory, "MartusConfig.dat").delete();
				new File(fakeDataDirectory, "MartusConfig.sig").delete();
				new File(fakeDataDirectory, "AccountToken.txt").delete();
				fakeDataDirectory.delete();
			}
		}
	}

	public void testGetClientId()
	{
		TRACE_BEGIN("testGetClientId");
		String securityAccount = mockSecurityForApp.getPublicKeyString();
		String appAccount = appWithAccount.getAccountId();
		assertEquals("mock account wrong?", securityAccount, appAccount);
		Bulletin b = appWithAccount.createBulletin();
		assertEquals("client id wrong?", b.getAccount(), appWithAccount.getAccountId());
		TRACE_END();
	}
	
	public void testContactInfo() throws Exception
	{
		TRACE_BEGIN("testContactInfo");

		File file = appWithAccount.getConfigInfoFile();
		file.delete();
		assertEquals("delete didn't work", false, file.exists());
		appWithAccount.loadConfigInfo();

		ConfigInfo originalInfo = appWithAccount.getConfigInfo();
		assertEquals("should be empty", "", originalInfo.getAuthor());

		originalInfo.setAuthor("blah");
		assertEquals("should have been set", "blah", appWithAccount.getConfigInfo().getAuthor());
		appWithAccount.saveConfigInfo();
		assertEquals("should still be there", "blah", appWithAccount.getConfigInfo().getAuthor());
		assertEquals("save didn't work!", true, file.exists());

		originalInfo.setAuthor("something else");
		appWithAccount.loadConfigInfo();
		assertNotNull("ContactInfo null", appWithAccount.getConfigInfo());
		assertEquals("should have reloaded", "blah", appWithAccount.getConfigInfo().getAuthor());

		File sigFile = appWithAccount.getConfigInfoSignatureFile();
		sigFile.delete();
		appWithAccount.saveConfigInfo();
		assertTrue("Missing Signature file", sigFile.exists());
		appWithAccount.loadConfigInfo();
		assertEquals("blah", appWithAccount.getConfigInfo().getAuthor());
		sigFile.delete();
		try
		{
			appWithAccount.loadConfigInfo();
			fail("Should not have verified");
		}
		catch (MartusApp.LoadConfigInfoException e)
		{
			//Expected
		}
		assertEquals("", appWithAccount.getConfigInfo().getAuthor());

		TRACE_END();

	}

	public void testCreateAccountBadDirectory() throws Exception
	{
		TRACE_BEGIN("testCreateAccountBadDirectory");

		mockSecurityForApp.clearKeyPair();
		try
		{

			File badDirectory = new File(BAD_FILENAME);
			appWithAccount.createAccountInternal(badDirectory, userName, userPassword);
			fail("Can't create an account if we can't write the file!");

		}
		catch(MartusApp.CannotCreateAccountFileException e)
		{
			// expected exception
		}
		assertEquals("store account not unset on error?", false, mockSecurityForApp.hasKeyPair());
		TRACE_END();
	}

	public void testCreateAccount() throws Exception
	{
		TRACE_BEGIN("testCreateAccount");
		MockMartusApp app = MockMartusApp.create();
		app.createAccount(userName, userPassword);
		File keyPairFile = app.getCurrentKeyPairFile();
		assertEquals("not root dir?", app.getMartusDataRootDirectory(), keyPairFile.getParentFile());
		File backupKeyPairFile = MartusApp.getBackupFile(keyPairFile);
		assertEquals("no backup key file?", true, backupKeyPairFile.exists());

		assertEquals("store account not set?", app.getAccountId(), app.getStore().getAccountId());
		assertEquals("User name not set?",userName, app.getUserName());
		verifySignInThatWorks(app, userName, userPassword);

		try
		{
			app.createAccountInternal(app.getMartusDataRootDirectory(), userName+"a", userPassword);
			fail("Can't create an account if one already exists!");
		}
		catch(MartusApp.AccountAlreadyExistsException e)
		{
			// expected exception
		}
		assertEquals("store account not kept if already exists?", app.getAccountId(), app.getStore().getAccountId());

		app.deleteAllFiles();
		TRACE_END();
	}

	public void testMultipleCreateAccounts() throws Exception
	{
		TRACE_BEGIN("testMultipleCreateAccounts");
		MockMartusApp app = MockMartusApp.create();
		String newUserName = "testName";
		char[] newUserPassword = "passWOrd".toCharArray();
		app.createAccount(newUserName, newUserPassword);
		File keyPairFile = app.getCurrentKeyPairFile();
		assertEquals("not root dir?", app.getMartusDataRootDirectory(), keyPairFile.getParentFile());
		assertEquals("accountDir not where the keypair file is?", keyPairFile.getParentFile(), app.getAccountDirectoryForUser(newUserName, newUserPassword));
		File backupKeyPairFile = MartusApp.getBackupFile(keyPairFile);
		assertEquals("no backup key file?", true, backupKeyPairFile.exists());

		String accountId1 = app.getAccountId();
		assertEquals("store account not set?", accountId1, app.getStore().getAccountId());
		assertEquals("User name not set?",newUserName, app.getUserName());
		verifySignInThatWorks(app, newUserName, newUserPassword);

		try
		{
			app.createAccount(newUserName, newUserPassword);
			fail("Should not be able to create an account with the same user name.");
		}
		catch(MartusApp.AccountAlreadyExistsException e)
		{
			// expected exception
		}
		assertEquals("store account not kept if already exists?", accountId1, app.getStore().getAccountId());

		String accountId2 = createAnotherAccount(app, userName2);
		assertNotEquals("account id's should be different", accountId1, accountId2);
		
		File account2KeypairFile = app.getKeyPairFile(app.getCurrentAccountDirectory());
		assertEquals("accountDir2 not where the keypair file is?", account2KeypairFile.getParentFile(), app.getAccountDirectoryForUser(userName2, userPassword));
		account2KeypairFile.delete();
		try
		{
			createAnotherAccount(app, userName2);
			fail("Can't create an account2 with the same user name even if keypair file is missing.");
		}
		catch(MartusApp.AccountAlreadyExistsException e)
		{
			// expected exception
		}
		
		String accountId3 = createAnotherAccount(app, "another");
		assertNotEquals("account1 id's should be different", accountId1, accountId3);
		assertNotEquals("account2 id's should be different", accountId2, accountId3);

		app.deleteAllFiles();
		TRACE_END();
	}
	
	private String createAnotherAccount(MockMartusApp app, String thisUserName) throws AccountAlreadyExistsException, CannotCreateAccountFileException, IOException, Exception
	{
		assertTrue("Must already have default account", app.doesDefaultAccountExist());
		app.createAccount(thisUserName, userPassword);
		File keyPairFile = app.getCurrentKeyPairFile();
		assertTrue("Keypair File for new user doesn't exist?", keyPairFile.exists());
		File accountDirectoryForUser = app.getAccountDirectoryForUser(thisUserName, userPassword);
		assertNotNull("accountDirectoryForUser should not be null :Username ="+thisUserName, accountDirectoryForUser);
		assertEquals("We dont own this directory?", keyPairFile.getParent(), accountDirectoryForUser.getPath());

		File currentAccountDirectory = app.getCurrentAccountDirectory();
		assertEquals("The directory holding the keypair file & current account directory should match",keyPairFile.getParent(), currentAccountDirectory.getAbsolutePath());
		assertTrue("Keypair file does not exist? " + keyPairFile.getPath(), keyPairFile.exists());
		assertNotEquals("Should not be in root directory?", app.getMartusDataRootDirectory(), keyPairFile.getParentFile());
		assertEquals("Parent of Parent should be the accounts dir.", app.getAccountsDirectory(), keyPairFile.getParentFile().getParentFile());
		File backupKeyPairFile = MartusApp.getBackupFile(keyPairFile);
		assertEquals("no backup key file?", true, backupKeyPairFile.exists());

		String accountId = app.getAccountId();
		assertEquals("store account not set?", accountId, app.getStore().getAccountId());
		assertEquals("User name not set?",thisUserName, app.getUserName());
		File currentAccountDirectory2 = app.getCurrentAccountDirectory();
		assertEquals("current account directory should still match",currentAccountDirectory2.getAbsolutePath(), currentAccountDirectory.getAbsolutePath());
		verifySignInThatWorks(app, thisUserName, userPassword);
		File currentAccountDirectory3 = app.getCurrentAccountDirectory();
		assertEquals("current account directory should still match",currentAccountDirectory3.getAbsolutePath(), currentAccountDirectory.getAbsolutePath());
		return accountId;
	}

	void verifySignInThatWorks(MartusApp appWithRealAccount, String userName, char[] userPassword) throws Exception
	{
		appWithRealAccount.attemptReSignIn(userName, userPassword);
		assertEquals("store account not set?", mockSecurityForApp.getPublicKeyString(), appWithAccount.getStore().getAccountId());
		assertEquals("wrong username?", userName, appWithRealAccount.getUserName());
	}

	public void testSetAndGetHQKey() throws Exception
	{
		File configFile = appWithAccount.getConfigInfoFile();
		configFile.deleteOnExit();
		assertEquals("already exists?", false, configFile.exists());
		String sampleHQKey = "abc123";
		String sampleLabel = "Fred";
		HQKeys keys = new HQKeys();
		HQKey key = new HQKey(sampleHQKey, sampleLabel);
		keys.add(key);
		appWithAccount.setAndSaveHQKeys(keys);
		assertEquals("Incorrect public key", sampleHQKey, appWithAccount.getLegacyHQKey());
		assertEquals("Didn't save?", true, configFile.exists());
	}
	
	public void testGetAndSetMultipleHQKeys() throws Exception
	{
		File configFile = appWithAccount.getConfigInfoFile();
		configFile.deleteOnExit();
		String sampleHQKey1 = "abc123";
		String sampleLabel1 = "Fred";
		String sampleHQKey2 = "234567";
		String sampleLabel2 = "Bev";
		HQKeys keys = new HQKeys();
		HQKey key1 = new HQKey(sampleHQKey1, sampleLabel1);
		HQKey key2 = new HQKey(sampleHQKey2, sampleLabel2);
		keys.add(key1);
		keys.add(key2);
		appWithAccount.setAndSaveHQKeys(keys);
		assertEquals("Incorrect default public key", sampleHQKey1, appWithAccount.getLegacyHQKey());
		HQKeys returnedKeys = appWithAccount.getHQKeys();
		assertTrue(returnedKeys.containsKey(sampleHQKey1));
		assertTrue(returnedKeys.containsKey(sampleHQKey2));
	}

	public void testClearHQKey() throws Exception
	{
		File configFile = appWithAccount.getConfigInfoFile();
		configFile.deleteOnExit();
		assertEquals("already exists?", false, configFile.exists());
		HQKeys empty = new HQKeys();
		appWithAccount.setAndSaveHQKeys(empty);
		assertEquals("HQ key exists?", "", appWithAccount.getLegacyHQKey());
		assertEquals("Didn't save?", true, configFile.exists());

		String sampleHQKey1 = "abc123";
		String sampleLabel1 = "Fred";
		HQKeys keys = new HQKeys();
		HQKey key1 = new HQKey(sampleHQKey1, sampleLabel1);
		keys.add(key1);

		appWithAccount.setAndSaveHQKeys(keys);
		assertEquals("Incorrect public key", sampleHQKey1, appWithAccount.getLegacyHQKey());
		appWithAccount.setAndSaveHQKeys(empty);
		assertEquals("HQ not cleared", "", appWithAccount.getLegacyHQKey());
	}

	public void testGetCombinedPassPhrase()
	{
		char[] combined1 = PasswordHelper.getCombinedPassPhrase(userName, userPassword);
		char[] combined2 = PasswordHelper.getCombinedPassPhrase(userName2, userPassword);
		char[] combined3 = PasswordHelper.getCombinedPassPhrase(userName, userPassword2);
		assertFalse("username diff", Arrays.equals(combined1, combined2));
		assertFalse("password diff",  Arrays.equals(combined1, combined3));

		char[] ab_c = PasswordHelper.getCombinedPassPhrase("ab", "c".toCharArray());
		char[] a_bc = PasswordHelper.getCombinedPassPhrase("a", "bc".toCharArray());
		assertFalse("abc diff", Arrays.equals(ab_c, a_bc));
	}

	public void testAttemptSignInBadKeyPairFile() throws Exception
	{
		TRACE_BEGIN("testAttemptSignInBadKeyPairFile");

		File badFile = new File(BAD_FILENAME);
		try
		{
			appWithAccount.attemptSignInInternal(badFile, userName, userPassword);
			fail("didn't throw on bad file?");
		}
		catch (Exception expected)
		{
		}
		assertEquals("keypair not cleared?", false, mockSecurityForApp.hasKeyPair());
		assertEquals("non-blank username?", "", appWithAccount.getUserName());
		appWithAccount.getSecurity().createKeyPair();
		TRACE_END();
	}	
	
	public void testFileOutputStreamReadOnly() throws Exception
	{
		TRACE_BEGIN("testFileOutputStreamReadOnly");
		File readOnlyFile = createTempFileFromName("FileOutputStreamReadOnly_"+TestCaseEnhanced.getCallingTestClass());
		readOnlyFile.setReadOnly();
		try
		{
			new FileOutputStream(readOnlyFile);
			fail("Should have thrown IO exception on ReadOnly File");
		}
		catch (IOException expected)
		{
		}
		readOnlyFile.delete();
		TRACE_END();
	}
	
	public void testScrubAndDeleteKeyPairFileAndRelatedFiles() throws Exception
	{
		TRACE_BEGIN("testScrubAndDeleteKeyPairFileAndRelatedFiles");

		File accountsDirectory = appWithAccount.getCurrentAccountDirectory();
		File keyPairFile = appWithAccount.getCurrentKeyPairFile();	
		File backupKeyPairFile = MartusApp.getBackupFile(keyPairFile);
		File accountTokenFile = appWithAccount.getUserNameHashFile(keyPairFile.getParentFile());
		File configInfoFile = appWithAccount.getConfigInfoFile();
		File configInfoSigFile = appWithAccount.getConfigInfoSignatureFile();
		File uploadedFile = appWithAccount.getUploadInfoFile();
		File uiStateFile = appWithAccount.getUiStateFileForAccount(accountsDirectory);
		File foldersFile = ClientBulletinStore.getFoldersFileForAccount(accountsDirectory);
		File cacheFile = ClientBulletinStore.getCacheFileForAccount(accountsDirectory);
		File key1File = new File(accountsDirectory, "Key1.mpi");
		File key2File = new File(accountsDirectory, "Key2.mpi");
		
		appWithAccount.writeKeyPairFileWithBackup(keyPairFile,"UserA", "Password".toCharArray());
		appWithAccount.saveConfigInfo();
		FileOutputStream out = new FileOutputStream(uploadedFile);
		out.write(1);
		out.close();
		FileOutputStream out2 = new FileOutputStream(uiStateFile);
		out2.write(1);
		out2.close();
		FileOutputStream out3 = new FileOutputStream(foldersFile);
		out3.write(1);
		out3.close();
		FileOutputStream out4 = new FileOutputStream(cacheFile);
		out4.write(1);
		out4.close();
		FileOutputStream out5 = new FileOutputStream(key1File);
		out5.write(1);
		out5.close();
		FileOutputStream out6 = new FileOutputStream(key2File);
		out6.write(1);
		out6.close();
		key1File.setReadOnly();
	 
		assertTrue("keypair file doesn't exist?", keyPairFile.exists());
		assertTrue("backup keypair file doesn't exist?", backupKeyPairFile.exists());
		assertTrue("account Token file doesn't exist?", accountTokenFile.exists());
		assertTrue("configInfo file doesn't exist?", configInfoFile.exists());
		assertTrue("configInfo sig file doesn't exist?", configInfoSigFile.exists());
		assertTrue("upload reminder file doesn't exist?", uploadedFile.exists());
		assertTrue("uiState file doesn't exist?", uiStateFile.exists());
		assertTrue("folders file doesn't exist?", foldersFile.exists());
		assertTrue("cache file doesn't exist?", cacheFile.exists());
		assertTrue("key1 file doesn't exist?", key1File.exists());
		assertTrue("key2 file doesn't exist?", key2File.exists());
		
		appWithAccount.deleteKeypairAndRelatedFilesForAccount(accountsDirectory);
		
		//TODO Make sure the files really get scrubbed.
		
		assertFalse("keypair file still exist?", keyPairFile.exists());
		assertFalse("backup keypair file still exist?", backupKeyPairFile.exists());
		assertFalse("account Token file still exists?", accountTokenFile.exists());
		assertFalse("configInfo file still exists?", configInfoFile.exists());
		assertFalse("configInfo sig file still exists?", configInfoSigFile.exists());
		assertFalse("upload reminder file still exists?", uploadedFile.exists());
		assertFalse("uiState file still exists?", uiStateFile.exists());
		assertFalse("folders file still exists?", foldersFile.exists());
		assertFalse("cache file still exists?", cacheFile.exists());
		assertFalse("key1 file still exists?", key1File.exists());
		assertFalse("key2 file still exists?", key2File.exists());
			
		TRACE_END();		
	}
	
	protected void checkScrubbedData(File file) throws Exception
	{
		RandomAccessFile randomFile = new RandomAccessFile(file, "r");
		randomFile.seek(0);
		for (int i = 0; i < randomFile.length(); i++)
		{
			assertEquals("wrong byte?", 0x55, randomFile.read());
		}
		randomFile.close();
	}

	public void testAttemptSignInAuthorizationFailure() throws Exception
	{
		TRACE_BEGIN("testAttemptSignInAuthorizationFailure");

		mockSecurityForApp.fakeAuthorizationFailure = true;
		try
		{
			appWithAccount.attemptSignIn(userName, userPassword);
			fail("should throw here");
		}
		catch (Exception expected)
		{
		}
		assertEquals("keypair not cleared?", false, mockSecurityForApp.hasKeyPair());
		mockSecurityForApp.fakeAuthorizationFailure = false;
		appWithAccount.getSecurity().createKeyPair();

		TRACE_END();
	}

	public void testAttemptReSignInAuthorizationFailure() throws Exception
	{
		TRACE_BEGIN("testAttemptReSignInAuthorizationFailure");
		MockMartusApp app = MockMartusApp.create();
		app.createAccount(userName, userPassword);
		app.getSecurity().clearKeyPair();
		app.currentUserName = "";
		try
		{
			app.attemptReSignIn(userName, userPassword);
			fail("Before Signin should throw");
		}
		catch (Exception expected)
		{
		}		
		assertNull("keypair not empty?", app.getAccountId());
		app.attemptSignIn(userName, userPassword);
		assertNotNull("keypair empty?", app.getAccountId());
		app.attemptReSignIn(userName, userPassword);
		assertNotNull("keypair cleared?", app.getAccountId());

		String oldAccountId = app.getAccountId();
		try
		{
			app.attemptReSignIn(userName+"x", userPassword);
			fail("wrong resignin should throw");
		}
		catch (Exception expected)
		{
		}	
		assertEquals("keypair cleared for bad username?", oldAccountId, app.getAccountId());

		try
		{
			app.attemptReSignIn(userName, "wrong passphrase by a mile".toCharArray());
			fail("Should have thrown for missing keypair");
		}
		catch(Exception ignoreExpectedException)
		{
		}
		assertEquals("keypair cleared for missing keypair file?", oldAccountId, app.getAccountId());

		app.attemptReSignIn(userName, userPassword);
		assertEquals("keypair not still there?", oldAccountId, app.getAccountId());
		app.deleteAllFiles();
		TRACE_END();
	}

	public void testAttemptSignInToAdditionalAccount() throws Exception
	{
		TRACE_BEGIN("testAttemptSignInToAdditionalAccount");
		MockMartusApp app = MockMartusApp.create();
		app.createAccount(userName, userPassword);
		String userName2 = "user2";
		char[] userPassword2 = "pass2".toCharArray();
		app.createAccount(userName2, userPassword2);
		
		app.attemptSignIn(userName2, userPassword2);
		app.attemptSignIn(userName, userPassword);
		app.deleteAllFiles();
		TRACE_END();
	}
	
	
	public void testIsUserOwnerOfThisAccountDirectory() throws Exception
	{
		MockMartusApp app = MockMartusApp.create();
		File tempDirectory = createTempDirectory();
		tempDirectory.deleteOnExit();
		File hashFile = app.getUserNameHashFile(tempDirectory);
		hashFile.deleteOnExit();
		assertFalse("Should not have this hash file yet", hashFile.exists());
		String username = "chuck";
		assertFalse("This user should not own this directory", app.isUserOwnerOfThisAccountDirectory(mockSecurityForApp, username, null, app.getCurrentAccountDirectory()));
		
		app.setCurrentAccount(username, tempDirectory);
		assertEquals("Current Account Directory not set?", tempDirectory, app.getCurrentAccountDirectory());
		assertTrue("Hash File should now exist", hashFile.exists());
		assertTrue("This user should be the owner of this directory", app.isUserOwnerOfThisAccountDirectory(mockSecurityForApp, username, null, app.getCurrentAccountDirectory()));

		String myUserName = "goodMan";
		char[] myPassword = "goodM".toCharArray();
		app.createAccount(myUserName,myPassword);
		assertTrue("My new user should be the owner of this directory", app.isUserOwnerOfThisAccountDirectory(mockSecurityForApp, myUserName, myPassword, app.getCurrentAccountDirectory()));
		File myHashFile = app.getUserNameHashFile(app.getCurrentAccountDirectory()); 
		myHashFile.delete();
		assertTrue("My new user should still be the owner of this directory even without a hashFile", app.isUserOwnerOfThisAccountDirectory(mockSecurityForApp, myUserName, myPassword, app.getCurrentAccountDirectory()));
		hashFile.delete();
		tempDirectory.delete();
		app.deleteAllFiles();
	}
	
	public void testAttemptSignInKeyPairVersionFailure() throws Exception
	{
		TRACE_BEGIN("testAttemptSignInKeyPairVersionFailure");

		mockSecurityForApp.fakeKeyPairVersionFailure = true;
		try
		{
			appWithAccount.attemptSignIn(userName, userPassword);
			fail("should throw.");
		}
		catch (Exception expected)
		{
		}
		assertEquals("keypair not cleared?", false, mockSecurityForApp.hasKeyPair());
		mockSecurityForApp.fakeKeyPairVersionFailure = false;
		appWithAccount.getSecurity().createKeyPair();

		TRACE_END();
	}

	public void testDoesAccountExist() throws Exception
	{
		TRACE_BEGIN("testDoesAccountExist");

		File keyPairFile = appWithAccount.getCurrentKeyPairFile();
		keyPairFile.delete();
		assertEquals("account exists without a file?", false, appWithAccount.doesAnyAccountExist());

		FileOutputStream out = new FileOutputStream(keyPairFile);
		out.write(0);
		out.close();

		assertEquals("account doesn't exist with a file?", true, appWithAccount.doesAnyAccountExist());

		keyPairFile.delete();

		TRACE_END();
	}

	public void testGetAllAccountDirectories() throws Exception
	{
		TRACE_BEGIN("testGetAllAccountDirectories");
		File rootDir = createTempDirectory();
		MartusApp app = new MartusApp(mockSecurityForApp, rootDir, localization);
		assertEquals("1 account should exist", 1, app.getAllAccountDirectories().size());
		assertEquals("not root dir?", rootDir, app.getAllAccountDirectories().get(0));
		
		File accountsDir = app.getAccountsDirectory();
		accountsDir.deleteOnExit();
		String directoryName1 = "1111.1111.1111.1111.1111";
		File newAccountDir = new File(accountsDir, directoryName1);
		newAccountDir.deleteOnExit();
		newAccountDir.mkdirs();
		
		String directoryName3 = "3333.3333.3333.3333.3333";
		File thirdAccountDir = new File(accountsDir, directoryName3);
		thirdAccountDir.deleteOnExit();
		thirdAccountDir.mkdirs();
		
		File nonAccountDir = new File(accountsDir, "notAPublicCodeBut24long!");
		nonAccountDir.deleteOnExit();
		nonAccountDir.mkdirs();

		File wrongAccountLengthDir = new File(accountsDir, "4444.4444");
		wrongAccountLengthDir.deleteOnExit();
		wrongAccountLengthDir.mkdirs();
		
		File notADirectory = File.createTempFile("justAFile", ".test", accountsDir);
		
		assertEquals("3 account should now exist", 3, app.getAllAccountDirectories().size());
		assertContains("no root dir?", rootDir, app.getAllAccountDirectories());
		assertContains("no newAccountDir dir?", newAccountDir, app.getAllAccountDirectories());
		assertContains("no thirdAccountDir dir?", thirdAccountDir, app.getAllAccountDirectories());
		
		wrongAccountLengthDir.delete();
		notADirectory.delete();
		nonAccountDir.delete();
		new File(thirdAccountDir, "AccountToken.txt").delete();
		thirdAccountDir.delete();
		new File(newAccountDir, "AccountToken.txt").delete();
		newAccountDir.delete();
		accountsDir.delete();
		TRACE_END();
	}

	public void testGetAccountDirectory() throws Exception
	{
		TRACE_BEGIN("testGetAccountDirectory");
		File rootDir = createTempDirectory();
		try
		{
			MartusApp app = new MartusApp(mockSecurityForApp, rootDir, localization);
			
			assertEquals("first account not root dir?", rootDir, app.getAccountDirectory("anything"));
	
			String username1 = "name";
			createAccount(app, username1);
			String realAccountId1 = app.getAccountId();
			saveConfigInfo(app);
			
			String username2 = "other";
			createAccount(app, username2);
			String realAccountId2 = app.getAccountId();
			saveConfigInfo(app);
	
			File account2Directory = app.getAccountDirectory(realAccountId2);
			String digest2 = MartusCrypto.computeFormattedPublicCode(realAccountId2);
			assertEquals("second account not in digest dir?", digest2, account2Directory.getName());

			String sillyAccountId = "something new";
			File sillyAccountDirectory = app.getAccountDirectory(sillyAccountId);
			String sillyDigest = MartusCrypto.computeFormattedPublicCode(sillyAccountId);
			assertEquals("silly account not in digest dir?", sillyDigest, sillyAccountDirectory.getName());
			assertTrue("didn't create silly dir?", sillyAccountDirectory.exists());
			
			File rootAccountDirectory = app.getAccountDirectory(realAccountId1);
			assertEquals("not root?", rootDir.getAbsolutePath(), rootAccountDirectory.getAbsolutePath());
			
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(rootDir);
		}
		TRACE_END();
	}
	
	
	private void saveConfigInfo(MartusApp app) throws SaveConfigInfoException 
	{
		File configFile1 = app.getConfigInfoFile();
		app.saveConfigInfo();
		assertTrue("no config?", configFile1.exists());
		File sigFile1 = app.getConfigInfoSignatureFile();
		assertTrue("no config sig?", sigFile1.exists());
	}

	private void createAccount(MartusApp app, String username1) throws CannotCreateAccountFileException, Exception 
	{
		char[] password1 = "pass".toCharArray();
		app.createAccount(username1, password1);
	}

	public void testDoesAccountExistForMultipleAccounts() throws Exception
	{
		TRACE_BEGIN("testDoesAccountExistForMultipleAccounts");
		File rootDir = createTempDirectory();
		MartusApp app = new MartusApp(mockSecurityForApp, rootDir, localization);
		
		File accountsDir = app.getAccountsDirectory();
		accountsDir.deleteOnExit();
		String accountDirectoryName = "1234.5678.9012.3456.7890";
		File newAccountDir = new File(accountsDir, accountDirectoryName);
		newAccountDir.deleteOnExit();
		newAccountDir.mkdirs();
		File keyPairFile = new File(newAccountDir,MartusApp.KEYPAIR_FILENAME);
		keyPairFile.deleteOnExit();
		
		assertEquals("account should not exist yet", false, app.doesAnyAccountExist());
		
		FileOutputStream out = new FileOutputStream(keyPairFile);
		out.write(0);
		out.close();
		assertEquals("account in a sub folder doesn't exist with a file?", true, app.doesAnyAccountExist());
		
		new File(newAccountDir, "AccountToken.txt").delete();
		keyPairFile.delete();
		newAccountDir.delete();
		accountsDir.delete();
		TRACE_END();
	}
	
	public void testDoesDefaultAccountExist() throws Exception
	{
		TRACE_BEGIN("testDoesDefaultAccountExist");

		File keyPairFile = appWithAccount.getCurrentKeyPairFile();
		keyPairFile.delete();
		assertEquals("Default account exists?", false, appWithAccount.doesDefaultAccountExist());

		FileOutputStream out = new FileOutputStream(keyPairFile);
		out.write(0);
		out.close();

		assertEquals("Default Account doesn't exist?", true, appWithAccount.doesDefaultAccountExist());
		File rootPacketsDir = new File(keyPairFile.getParentFile(), MartusApp.PACKETS_DIRECTORY_NAME);
		rootPacketsDir.deleteOnExit();
		rootPacketsDir.mkdir();
		assertEquals("Default Account should still exist?", true, appWithAccount.doesDefaultAccountExist());
		
		keyPairFile.delete();
		assertEquals("Default Account should not exist because the packets dir is empty.", false, appWithAccount.doesDefaultAccountExist());
		File anyFile = new File(rootPacketsDir,"anyFile");
		anyFile.deleteOnExit();
		FileOutputStream outFile = new FileOutputStream(anyFile);
		outFile.write(0);
		outFile.close();
		assertEquals("Default Account should now exist because the packets dir is not empty.", true, appWithAccount.doesDefaultAccountExist());
		anyFile.delete();
		
		rootPacketsDir.delete();
		assertEquals("Default account should now not exist", false, appWithAccount.doesDefaultAccountExist());
		
		TRACE_END();
	}
	
	public void testCreateBulletin() throws Exception
	{
		TRACE_BEGIN("testCreateBulletin");
		mockSecurityForApp.loadSampleAccount();
		ConfigInfo info = appWithAccount.getConfigInfo();
		String source = "who?";
		String organization = "those guys";
		String template = "Was there a bomb?";
		info.setAuthor(source);
		info.setOrganization(organization);
		info.setTemplateDetails(template);
		Bulletin b = appWithAccount.createBulletin();
		assertNotNull("null Bulletin", b);
		assertEquals(source, b.get(Bulletin.TAGAUTHOR));
		assertEquals(organization, b.get(Bulletin.TAGORGANIZATION));
		assertEquals(template, b.get(Bulletin.TAGPUBLICINFO));
		assertEquals(Bulletin.STATUSDRAFT, b.getStatus());
		assertEquals("not automatically private?", true, b.isAllPrivate());
		TRACE_END();
	}

	public void testLoadBulletins() throws Exception
	{
		TRACE_BEGIN("testLoadBulletins");
		mockSecurityForApp.loadSampleAccount();

		appWithAccount.loadSampleData(); //SLOW!!!

		ClientBulletinStore store = appWithAccount.getStore();
		int sampleCount = store.getBulletinCount();
		assertTrue("Should start with samples", sampleCount > 0);

		appWithAccount.loadFolders();
		assertEquals("Should have loaded samples", sampleCount, store.getBulletinCount());
		BulletinFolder sent = store.getFolderSaved();
		assertEquals("Sent should have bulletins", sampleCount, sent.getBulletinCount());

		store.deleteAllData();
		assertEquals("Should have deleted samples", 0, store.getBulletinCount());
		appWithAccount.loadFolders();

		TRACE_END();
	}

	public void testSearch() throws Exception
	{
		TRACE_BEGIN("testSearch");
		ClientBulletinStore store = appWithAccount.getStore();
		String startDate = "1900-01-01";
		String endDate = "2099-12-31";
		assertNull("Search results already exists?", store.findFolder(store.getSearchFolderName()));

		appWithAccount.loadSampleData(); //SLOW!!!
		Bulletin b = store.getBulletinRevision((UniversalId)store.getAllBulletinUids().get(0));
		String andKeyword = "and";
		String orKeyword = "or";
		appWithAccount.search(b.get("title"), startDate, endDate, andKeyword, orKeyword);
		assertNotNull("Search results should have been created", store.getSearchFolderName());

		appWithAccount.search("--not in any bulletin--", startDate, endDate, andKeyword, orKeyword);
		assertEquals("search should clear results folder", 0, store.findFolder(store.getSearchFolderName()).getBulletinCount());

		assertTrue("not enough bulletins?", appWithAccount.getStore().getBulletinCount() >= 5);
		assertTrue("too many bulletins?", appWithAccount.getStore().getBulletinCount() <= 15);
		appWithAccount.search(b.get("author"), startDate, endDate, andKeyword, orKeyword);
		assertEquals(1, store.findFolder(store.getSearchFolderName()).getBulletinCount());
		appWithAccount.search(b.get(""), startDate, endDate, andKeyword, orKeyword);
		assertEquals(10, store.findFolder(store.getSearchFolderName()).getBulletinCount());

		startDate = "1999-01-19";
		endDate = startDate;
		appWithAccount.search(b.get(""), startDate, endDate, andKeyword, orKeyword);
		assertEquals(1, store.findFolder(store.getSearchFolderName()).getBulletinCount());

		TRACE_END();
	}

	public void testFindBulletinInAllFolders() throws Exception
	{
		TRACE_BEGIN("testFindBulletinInAllFolders");
		MockMartusApp app = MockMartusApp.create();
		Bulletin b1 = app.createBulletin();
		Bulletin b2 = app.createBulletin();
		app.getStore().saveBulletin(b1);
		app.getStore().saveBulletin(b2);

		assertEquals("Found the bulletin already in a folder?", 0, app.findBulletinInAllVisibleFolders(b1).size());
		BulletinFolder f1 = app.createUniqueFolder("testFolder");
		BulletinFolder f2 = app.createUniqueFolder("testFolder");
		BulletinFolder f3 = app.createUniqueFolder("testFolder");
		BulletinFolder f4 = app.getFolderDraftOutbox();
		f1.add(b1);
		f2.add(b2);
		f3.add(b1);
		f3.add(b2);
		f4.add(b2);
		BulletinFolder discarded = app.getFolderDiscarded();
		discarded.add(b2);

		Vector v1 = app.findBulletinInAllVisibleFolders(b1);
		Vector v2 = app.findBulletinInAllVisibleFolders(b2);
		assertEquals("Wrong # of folders for b1?", 2, v1.size());
		assertEquals("Wrong # of folders for b2?", 3, v2.size());
		assertTrue("Doesn't contain f1 for bulletin b1?", v1.contains(f1));
		assertEquals("Does contain f2 for bulletin b1?", false, v1.contains(f2));
		assertTrue("Doesn't contain f3 for bulletin b1?", v1.contains(f3));
		assertEquals("Does contain Discarded for bulletin b1?",false, v1.contains(discarded));

		assertEquals("Does contain f1 for bulletin b2?", false, v2.contains(f1));
		assertTrue("Doesn't contain f2 for bulletin b2?", v2.contains(f2));
		assertTrue("Doesn't contain f3 for bulletin b2?", v2.contains(f3));
		assertTrue("Doesn't contain Discarded for bulletin b2?", v2.contains(discarded));

		app.deleteAllFiles();
		TRACE_END();
	}

	public void testGetPublicCodeFromAccount() throws Exception
	{
		MockMartusSecurity security = MockMartusSecurity.createClient();
		String publicKeyString = security.getPublicKeyString();
		String publicCode = MartusCrypto.computePublicCode(publicKeyString);
		assertEquals("wrong code?", "71887634433124687372", publicCode);
	}

	public void testgetHexDigest() throws Exception
	{
		
		byte[] completelyNegativeString;
		completelyNegativeString = new byte[20];
		Arrays.fill(completelyNegativeString,(byte)0xff);
		String digest = MartusUtilities.byteArrayToHexString(completelyNegativeString);
		assertEquals("should still be 40 char's long", 40, digest.length());
		assertEquals("any normal string should return a digest of 40 characters", 40, MartusCrypto.getHexDigest("hi1234fdsfjlk").length());
		
	}
	public void testGetFileLength() throws Exception
	{
		class MockFile extends File
		{
			MockFile()
			{
				super(".");
			}

			public long length()
			{
				return mockLength;
			}

			long mockLength;
		}

		MockFile mockFile = new MockFile();
		final int normalLength = 555;
		mockFile.mockLength = normalLength;
		assertEquals(normalLength, MartusUtilities.getCappedFileLength(mockFile));

		mockFile.mockLength = 10L *1024*1024*1024;
		try
		{
			MartusUtilities.getCappedFileLength(mockFile);
			fail("Should have thrown too large for big number");
		}
		catch(MartusUtilities.FileTooLargeException ignoreExpectedException)
		{
		}


		mockFile.mockLength = -255;
		try
		{
			MartusUtilities.getCappedFileLength(mockFile);
			fail("Should have thrown too large for negative number");
		}
		catch(MartusUtilities.FileTooLargeException ignoreExpectedException)
		{
		}
	}

	public void testEncryptPublicData() throws Exception
	{
		TRACE_BEGIN("testEncryptPublicData");
		File temp = createTempDirectory();
		MartusCrypto security = MockMartusSecurity.createClient();
		MartusApp app = new MartusApp(security, temp, new Localization(temp));
		app.doAfterSigninInitalization();
		app.getStore().deleteAllData();
		assertEquals("App Not Encypting Public?", true, app.getStore().mustEncryptPublicData());

		TRACE_END();
	}

	public void testExportPublicInfo() throws Exception
	{
		File temp = createTempFile();
		temp.delete();
		appWithAccount.exportPublicInfo(temp);
		assertTrue("not created?", temp.exists());
		UnicodeReader reader = new UnicodeReader(temp);
		String publicKey = reader.readLine();
		String signature = reader.readLine();
		reader.close();
		MartusCrypto security = appWithAccount.getSecurity();
		assertEquals("Public Key wrong?", security.getPublicKeyString(), publicKey);
		MartusUtilities.validatePublicInfo(publicKey, signature, security);
	}

	public void testExtractPublicInfo() throws Exception
	{
		File temp = createTempFile();
		temp.delete();
		appWithAccount.exportPublicInfo(temp);
		String publicKey = appWithAccount.extractPublicInfo(temp);
		assertEquals("Public Key wrong?", appWithAccount.getSecurity().getPublicKeyString(), publicKey);

		UnicodeWriter writer = new UnicodeWriter(temp);
		writer.write("flkdjfl");
		writer.close();
		try
		{
			appWithAccount.extractPublicInfo(temp);
			fail("Should have thrown exception");
		}
		catch (Exception ignoreExpectedException)
		{
		}
	}

	public void testCenter()
	{
		TRACE_BEGIN("testCenter");
		{
			Point upperLeft = Utilities.center(new Dimension(800, 600), new Rectangle(0, 0, 800, 600));
			assertEquals(0, upperLeft.x);
			assertEquals(0, upperLeft.y);
		}
		{
			Point upperLeft = Utilities.center(new Dimension(400, 300), new Rectangle(0, 0, 800, 600));
			assertEquals(200, upperLeft.x);
			assertEquals(150, upperLeft.y);
		}
		TRACE_END();
	}

	public void testFieldLabels()
	{
		TRACE_BEGIN("testFieldLabels");
		assertEquals("Keep ALL Information Private", localization.getFieldLabel("allprivate"));
		assertEquals("Author", localization.getFieldLabel("author"));
		assertEquals("Organization", localization.getFieldLabel("organization"));
		assertEquals("Title", localization.getFieldLabel("title"));
		assertEquals("Location", localization.getFieldLabel("location"));
		assertEquals("Date of Event", localization.getFieldLabel("eventdate"));
		assertEquals("Date Created", localization.getFieldLabel("entrydate"));
		assertEquals("Keywords", localization.getFieldLabel("keywords"));
		assertEquals("Summary", localization.getFieldLabel("summary"));
		assertEquals("Details", localization.getFieldLabel("publicinfo"));
		assertEquals("Private", localization.getFieldLabel("privateinfo"));
		assertEquals("Language", localization.getFieldLabel("language"));
		TRACE_END();
	}

	public void testFolderLabels()
	{
		//assertEquals("Retrieved Bulletins", appWithAccount.getFolderLabel("%RetrievedMyBulletin"));
		//assertEquals("Field Desk Bulletins", appWithAccount.getFolderLabel("%RetrievedFieldOfficeBulletin"));
	}

	public void testLanguageNames()
	{
		TRACE_BEGIN("testLanguageNames");
		assertNotNull(localization.getLanguageName("Not a valid code"));
		assertEquals("English", localization.getLanguageName("en"));
		assertEquals("Arabic", localization.getLanguageName("ar"));
		assertEquals("Azerbaijani", localization.getLanguageName("az"));
		assertEquals("Bengali, Bangla", localization.getLanguageName("bn"));
		assertEquals("Burmese", localization.getLanguageName("my"));
		assertEquals("Chinese", localization.getLanguageName("zh"));
		assertEquals("Dutch", localization.getLanguageName("nl"));
		assertEquals("Esperanto", localization.getLanguageName("eo"));
		assertEquals("French", localization.getLanguageName("fr"));
		assertEquals("German", localization.getLanguageName("de"));
		assertEquals("Gujarati", localization.getLanguageName("gu"));
		assertEquals("Hausa", localization.getLanguageName("ha"));
		assertEquals("Hebrew", localization.getLanguageName("he"));
		assertEquals("Hindi", localization.getLanguageName("hi"));
		assertEquals("Hungarian", localization.getLanguageName("hu"));
		assertEquals("Italian", localization.getLanguageName("it"));
		assertEquals("Japanese", localization.getLanguageName("ja"));
		assertEquals("Javanese", localization.getLanguageName("jv"));
		assertEquals("Kannada", localization.getLanguageName("kn"));
		assertEquals("Korean", localization.getLanguageName("ko"));
		assertEquals("Malayalam", localization.getLanguageName("ml"));
		assertEquals("Marathi", localization.getLanguageName("mr"));
		assertEquals("Oriya", localization.getLanguageName("or"));
		assertEquals("Panjabi", localization.getLanguageName("pa"));
		assertEquals("Polish", localization.getLanguageName("pl"));
		assertEquals("Portuguese", localization.getLanguageName("pt"));
		assertEquals("Romanian", localization.getLanguageName("ro"));
		assertEquals("Russian", localization.getLanguageName("ru"));
		assertEquals("Serbian", localization.getLanguageName("sr"));
		assertEquals("Sindhi", localization.getLanguageName("sd"));
		assertEquals("Sinhalese", localization.getLanguageName("si"));
		assertEquals("Spanish", localization.getLanguageName("es"));
		assertEquals("Tamil", localization.getLanguageName("ta"));
		assertEquals("Telugu", localization.getLanguageName("te"));
		assertEquals("Thai", localization.getLanguageName("th"));
		assertEquals("Turkish", localization.getLanguageName("tr"));
		assertEquals("Ukranian", localization.getLanguageName("uk"));
		assertEquals("Urdu", localization.getLanguageName("ur"));
		assertEquals("Vietnamese", localization.getLanguageName("vi"));
		TRACE_END();
	}

	public void testGetLanguageNameChoices()
	{
		TRACE_BEGIN("testWindowTitles");
		String[] testLanguageCodes = {"es", "en", "si"};
		ChoiceItem[] languageChoicesTest = localization.getLanguageNameChoices(testLanguageCodes);
		assertEquals(localization.getLanguageName("en"), languageChoicesTest[0].toString());
		assertEquals(localization.getLanguageName("si"), languageChoicesTest[1].toString());
		assertEquals(localization.getLanguageName("es"), languageChoicesTest[2].toString());
		TRACE_END();
	}

	public void testWindowTitles()
	{
		TRACE_BEGIN("testWindowTitles");
		assertEquals("Martus Human Rights Bulletin System", localization.getWindowTitle("main"));
		TRACE_END();
	}

	public void testButtonLabels()
	{
		TRACE_BEGIN("testButtonLabels");
		assertEquals("Help", localization.getButtonLabel("help"));
		TRACE_END();
	}

	public void testMenuLabels()
	{
		TRACE_BEGIN("testMenuLabels");
		assertEquals("File", localization.getMenuLabel("file"));
		TRACE_END();
	}

	public void testCurrentLanguage()
	{
		TRACE_BEGIN("testCurrentLanguage");

		assertEquals("en", localization.getCurrentLanguageCode());
		assertEquals("MartusHelp-en.txt", appWithAccount.getHelpFilename("en"));
		localization.setCurrentLanguageCode("es");
		assertEquals("es", localization.getCurrentLanguageCode());
		assertEquals("MartusHelp-es.txt", appWithAccount.getHelpFilename("es"));
		char iWithAccentInUtf8 = 237;
		char[] titleInSpanish = {'T', iWithAccentInUtf8, 't', 'u', 'l', 'o'};
		assertEquals(new String(titleInSpanish), localization.getFieldLabel("title"));
		localization.setCurrentLanguageCode("en");
		TRACE_END();
	}


	public void testDateConvert()
	{
		TRACE_BEGIN("testDateConvert");
		assertEquals("12/13/1987", localization.convertStoredDateToDisplay("1987-12-13"));
		assertEquals("", localization.convertStoredDateToDisplay("abc"));
		assertEquals("", localization.convertStoredDateToDisplay("1987-13-13"));
		TRACE_END();
	}

	public void testCurrentDateFormatCode()
	{
		TRACE_BEGIN("testCurrentDateFormatCode");
		
		assertEquals("MM/dd/yyyy", localization.getCurrentDateFormatCode());
		localization.setCurrentDateFormatCode("dd.MM.yyyy");
		assertEquals("dd.MM.yyyy", localization.getCurrentDateFormatCode());
		localization.setCurrentDateFormatCode("MM/dd/yyyy");
		assertEquals("MM/dd/yyyy", localization.getCurrentDateFormatCode());
		TRACE_END();
	}

	public void testMonthLabels()
	{
		TRACE_BEGIN("testMonthLabels");

		assertEquals("Mar", localization.getMonthLabel("mar"));
		String[] months = localization.getMonthLabels();
		assertEquals("Jan", months[0]);
		localization.setCurrentLanguageCode("es");
		months = localization.getMonthLabels();
		assertEquals("Ene", months[0]);
		localization.setCurrentLanguageCode("en");

		TRACE_END();
	}

	public void testStatusLabels()
	{
		TRACE_BEGIN("testStatusLabels");
		assertEquals("Draft", localization.getStatusLabel(Bulletin.STATUSDRAFT));
		assertEquals("Sealed", localization.getStatusLabel(Bulletin.STATUSSEALED));
		TRACE_END();
	}

	public void testCreateFolders() throws Exception
	{
		TRACE_BEGIN("testCreateFolders");
		final int MAXFOLDERS = 10;
		appWithAccount.setMaxNewFolders(MAXFOLDERS);
		String baseName = "testing";
		assertNotNull("New Folder is null?", appWithAccount.createUniqueFolder(baseName));
		assertNotNull("Could not find first new folder", appWithAccount.store.findFolder(baseName));

		for(int i = 1; i < MAXFOLDERS; ++i)
		{
			assertNotNull("Folder"+i+" is null?", appWithAccount.createUniqueFolder(baseName));
			assertNotNull("Could not find new folder"+i, appWithAccount.store.findFolder(baseName+i));
		}
		assertNull("Max Folders reached, why is this not null?", appWithAccount.createUniqueFolder(baseName));
		assertNull("Found this folder"+MAXFOLDERS, appWithAccount.store.findFolder(baseName+MAXFOLDERS));
		TRACE_END();
	}

	public void testFormatPublicCode() throws Exception
	{
		TRACE_BEGIN("testCreateFolders");
		String clientId = appWithAccount.getAccountId();
		assertNotNull("clientId Null?", clientId);
		String publicCode = MartusCrypto.computePublicCode(clientId);
		assertNotNull("publicCode Null?", publicCode);
		String formattedCode = MartusCrypto.formatPublicCode(publicCode);
		assertNotEquals("formatted code is the same as the public code?", formattedCode, publicCode);
		assertEquals("Not formatted correctly", "1234.5678.9012.3456", MartusCrypto.formatPublicCode("1234567890123456"));
		String formattedCode2 = MartusCrypto.computeFormattedPublicCode(clientId);
		assertEquals("Not formatted the same", formattedCode, formattedCode2);
		TRACE_END();

	}

	public void testRepairOrphans() throws Exception
	{
		assertEquals("already have orphans?", 0, appWithAccount.repairOrphans());		
		assertNull("Orphan Folder exists?", appWithAccount.getStore().findFolder(ClientBulletinStore.RECOVERED_BULLETIN_FOLDER));
		int draftCount = appWithAccount.getStore().getFolderDraftOutbox().getBulletinCount();
		assertEquals("is draft outbox folder empty?", 0,draftCount);
		
		Bulletin b1 = appWithAccount.createBulletin();
		b1.setDraft();
		appWithAccount.getStore().saveBulletin(b1);
		assertEquals("didn't find the orphan?", 1, appWithAccount.repairOrphans());
		draftCount = appWithAccount.getStore().getFolderDraftOutbox().getBulletinCount();
		assertEquals("is draft outbox folder not empty?", 1, draftCount);				
		assertEquals("didn't fix the orphan?", 0, appWithAccount.repairOrphans());		

		BulletinFolder orphanFolder = appWithAccount.getStore().findFolder(ClientBulletinStore.RECOVERED_BULLETIN_FOLDER);
		assertEquals("where did the orphan go?", 1, orphanFolder.getBulletinCount());
		assertTrue("wrong bulletin?", orphanFolder.contains(b1));

		appWithAccount.loadFolders();
		BulletinFolder orphanFolder2 = appWithAccount.getStore().findFolder(ClientBulletinStore.RECOVERED_BULLETIN_FOLDER);
		assertEquals("forgot to save folders?", 1, orphanFolder2.getBulletinCount());
	}

	public void testSetBulletinHQKey() throws Exception
	{
		String sampleHQKey1 = "abc123";
		String sampleLabel1 = "Fred";
		HQKeys keys = new HQKeys();
		HQKey key1 = new HQKey(sampleHQKey1, sampleLabel1);
		keys.add(key1);
		appWithAccount.setAndSaveHQKeys(keys);
		HQKeys returnedKeys = appWithAccount.getHQKeys();
		HQKey returnedKey1 = returnedKeys.get(0);
		assertEquals("Public Key not set?", sampleHQKey1, returnedKey1.getPublicKey());
		assertEquals("Label not set?", sampleLabel1, returnedKey1.getLabel());

		Bulletin b1 = appWithAccount.createBulletin();
		assertEquals("key already set?", 0, b1.getAuthorizedToReadKeys().size());
		appWithAccount.setHQKeysInBulletin(b1);
		assertEquals("Key not set?", sampleHQKey1, (b1.getAuthorizedToReadKeys().get(0)).getPublicKey());
		assertEquals("Label not set?", sampleLabel1, (b1.getAuthorizedToReadKeys().get(0)).getLabel());
	}

	public void testGetBulletinHQLabel() throws Exception
	{
		String sampleHQKey1 = "abc123";
		String sampleLabel1 = "Fred";
		HQKeys keys = new HQKeys();
		HQKey key1 = new HQKey(sampleHQKey1, sampleLabel1);
		keys.add(key1);
		appWithAccount.setAndSaveHQKeys(keys);
		assertEquals("Label not the same?", sampleLabel1, appWithAccount.getHQLabelIfPresent(key1));
		HQKey missingKey = new HQKey("public key", "some label");
		assertEquals("not Empty for unknown key?", "", appWithAccount.getHQLabelIfPresent(missingKey));
	}
	
	
	private MockMartusSecurity mockSecurityForApp;

	UiLocalization localization;
	private MockMartusApp appWithAccount;
	
	static final String[] noEnglishStrings = {};

	static final String userName = "testuser";
	static final String userName2 = "testuse!";
	static final char[] userPassword = "12345".toCharArray();
	static final char[] userPassword2 = "12347".toCharArray();
}
