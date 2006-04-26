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
package org.martus.client.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Vector;
import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.tools.ImporterOfXmlFilesOfBulletins;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.PendingAttachmentList;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.packet.UniversalId;
import org.martus.util.DirectoryUtils;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeReader;

public class TestImporterOfXmlFilesOfBulletins extends TestCaseEnhanced
{
	public TestImporterOfXmlFilesOfBulletins(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();
		if(security == null)
		{
			security = MockMartusSecurity.createClient();
			clientStore = new ClientBulletinStore(security);
			dataDirectory = createTempDirectory();
			clientStore.doAfterSigninInitialization(dataDirectory);
			clientStore.createFieldSpecCacheFromDatabase();
		}
		importFolder = clientStore.createOrFindFolder("Import");
		xmlInputDirectory = createTempDirectory();
	}
	
	public void tearDown() throws Exception
	{
		super.tearDown();
		DirectoryUtils.deleteEntireDirectoryTree(xmlInputDirectory);
		DirectoryUtils.deleteEntireDirectoryTree(dataDirectory);
	}

	public void testImportXMLWithMultipleBulletins() throws Exception
	{
		File xmlBulletin1 = new File(xmlInputDirectory, "$$$bulletin1.xml");
		copyResourceFileToLocalFile(xmlBulletin1, "SampleXmlBulletin.xml");
		xmlBulletin1.deleteOnExit();
		File xmlBulletin2 = new File(xmlInputDirectory, "$$$bulletin2.xml");
		copyResourceFileToLocalFile(xmlBulletin2, "SampleXmlTwoBulletins.xml");
		xmlBulletin2.deleteOnExit();
		File[] xmlFiles = new File[] {xmlBulletin1, xmlBulletin2};
		PrintStream nullPrinter = new PrintStream(new ByteArrayOutputStream());
		ImporterOfXmlFilesOfBulletins importer = new ImporterOfXmlFilesOfBulletins(xmlFiles, clientStore, importFolder, nullPrinter);
		importer.importFiles();
		xmlBulletin1.delete();
		xmlBulletin2.delete();
		
		assertEquals("Didn't get all 3 bulletins?", 3, importer.getNumberOfBulletinsImported());
		Vector bulletinIds = new Vector(clientStore.getAllBulletinLeafUids());
		
		Bulletin b1 = clientStore.getBulletinRevision((UniversalId)bulletinIds.get(0));
		Bulletin b2 = clientStore.getBulletinRevision((UniversalId)bulletinIds.get(1));
		Bulletin b3 = clientStore.getBulletinRevision((UniversalId)bulletinIds.get(2));
		Vector titles = new Vector();
		titles.add(b1.getField(Bulletin.TAGTITLE).toString());
		titles.add(b2.getField(Bulletin.TAGTITLE).toString());
		titles.add(b3.getField(Bulletin.TAGTITLE).toString());
		assertContains("Title Bulletin #1", titles);
		assertContains("Title Bulletin #2", titles);
		assertContains("import export example", titles);
	}

	public void testImportXMLWithAttachments() throws Exception
	{
		assertTrue(xmlInputDirectory.exists());
		File xmlBulletinWithAttachments = new File(xmlInputDirectory, "$$$bulletinWithAttachments.xml");
		copyResourceFileToLocalFile(xmlBulletinWithAttachments, "SampleXmlBulletinWithAttachments.xml");
		xmlBulletinWithAttachments.deleteOnExit();
	
		File attachment1 = new File(xmlInputDirectory, "$$$Sample Attachment1.txt");
		copyResourceFileToLocalFile(attachment1, "Sample Attachment1.txt");
		attachment1.deleteOnExit();

		File attachment2 = new File(xmlInputDirectory, "$$$Sample Attachment2.txt");
		copyResourceFileToLocalFile(attachment2, "Sample Attachment2.txt");
		attachment2.deleteOnExit();

		File attachment3 = new File(xmlInputDirectory, "$$$Sample Attachment3.txt");
		copyResourceFileToLocalFile(attachment3, "Sample Attachment3.txt");
		attachment3.deleteOnExit();
		
		clientStore.deleteAllBulletins();
		File[] xmlFiles = new File[] {xmlBulletinWithAttachments};
		PrintStream nullPrinter = new PrintStream(new ByteArrayOutputStream());
		ImporterOfXmlFilesOfBulletins importer = new ImporterOfXmlFilesOfBulletins(xmlFiles, clientStore, importFolder, nullPrinter);
		importer.setAttachmentsDirectory(xmlInputDirectory);
		importer.importFiles();
		xmlBulletinWithAttachments.delete();
		
		UnicodeReader reader = new UnicodeReader(attachment1);
		String attachment1Data = reader.readAll();
		reader.close();
		reader = new UnicodeReader(attachment2);
		String attachment2Data = reader.readAll();
		reader.close();
		reader = new UnicodeReader(attachment3);
		String attachment3Data = reader.readAll();
		reader.close();
		
		attachment1.delete();
		attachment2.delete();
		attachment3.delete();
		
		assertEquals("Didn't get 1 bulletins?", 1, importer.getNumberOfBulletinsImported());
		Vector bulletinIds = new Vector(clientStore.getAllBulletinLeafUids());
		Bulletin b1 = clientStore.getBulletinRevision((UniversalId)bulletinIds.get(0));
		AttachmentProxy[] publicAttachments = b1.getPublicAttachments();
		AttachmentProxy[] privateAttachments = b1.getPrivateAttachments();
		PendingAttachmentList pendingPublicAttachments = b1.getPendingPublicAttachments();
		assertEquals("Found pending Public attachments?",0,pendingPublicAttachments.size());
		PendingAttachmentList pendingPrivateAttachments = b1.getPendingPrivateAttachments();
		assertEquals("Found pending Private attachments?",0,pendingPrivateAttachments.size());
		
		assertEquals("Didn't find 2 public attachments?", 2, publicAttachments.length);
		assertEquals("Didn't find 1 private attachments?", 1, privateAttachments.length);
		
		assertEquals("Wrong File name Public 1?", "$$$Sample Attachment1.txt", publicAttachments[0].getLabel());
		assertEquals("Wrong File name Public 2?", "$$$Sample Attachment2.txt", publicAttachments[1].getLabel());
		assertEquals("Wrong File name Private 1?", "$$$Sample Attachment3.txt", privateAttachments[0].getLabel());

		File BulletinAttachment1 = b1.getAsFileProxy(publicAttachments[0], clientStore.getDatabase(), b1.getStatus()).getFile();
		
		reader = new UnicodeReader(BulletinAttachment1);
		String bulletinAttachment1Data = reader.readAll();
		reader.close();
		BulletinAttachment1.delete();
		File BulletinAttachment2 = b1.getAsFileProxy(publicAttachments[1], clientStore.getDatabase(), b1.getStatus()).getFile();
		reader = new UnicodeReader(BulletinAttachment2);
		String bulletinAttachment2Data = reader.readAll();
		reader.close();
		BulletinAttachment2.delete();
		File BulletinAttachment3 = b1.getAsFileProxy(privateAttachments[0], clientStore.getDatabase(), b1.getStatus()).getFile();
		reader = new UnicodeReader(BulletinAttachment3);
		String bulletinAttachment3Data = reader.readAll();
		reader.close();
		BulletinAttachment3.delete();
		
		assertEquals("Attachment 1 public data not equal?", attachment1Data, bulletinAttachment1Data);
		assertEquals("Attachment 2 public data not equal?", attachment2Data, bulletinAttachment2Data);
		assertEquals("Attachment 3 pravate data not equal?", attachment3Data, bulletinAttachment3Data);
	}
	
	static ClientBulletinStore clientStore;
	static BulletinFolder importFolder;
	static File dataDirectory;
	static MartusCrypto security;
	static File xmlInputDirectory;
	
}
