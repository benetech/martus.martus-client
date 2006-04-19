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
import java.util.Set;
import java.util.Vector;
import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.tools.ImporterOfXmlFilesOfBulletins;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.packet.UniversalId;
import org.martus.util.DirectoryUtils;
import org.martus.util.TestCaseEnhanced;

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

			importFolder = clientStore.createOrFindFolder("Import");
			
			xmlInputDirectory = createTempDirectory();
		}
	}
	
	public void tearDown() throws Exception
	{
		super.tearDown();
		DirectoryUtils.deleteEntireDirectoryTree(xmlInputDirectory);
		DirectoryUtils.deleteEntireDirectoryTree(dataDirectory);
	}

	public void testImportXMLWithMultipleBulletins() throws Exception
	{
		File xmlBulletin1 = new File(xmlInputDirectory, "bulletin1.xml");
		copyResourceFileToLocalFile(xmlBulletin1, "SampleXmlBulletin.xml");
		File xmlBulletin2 = new File(xmlInputDirectory, "bulletin2.xml");
		copyResourceFileToLocalFile(xmlBulletin2, "SampleXmlTwoBulletins.xml");
		File[] xmlFiles = new File[] {xmlBulletin1, xmlBulletin2};
		PrintStream nullPrinter = new PrintStream(new ByteArrayOutputStream());
		ImporterOfXmlFilesOfBulletins importer = new ImporterOfXmlFilesOfBulletins(xmlFiles, clientStore, importFolder, nullPrinter);
		importer.importFiles();
		assertEquals("Didn't get all 3 bulletins?", 3, importer.getNumberOfBulletinsImported());
		Set bulletinSetIds = clientStore.getAllBulletinLeafUids();
		Vector bulletinIds = new Vector(bulletinSetIds);
		
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
	
	static ClientBulletinStore clientStore;
	static BulletinFolder importFolder;
	static File dataDirectory;
	static MartusCrypto security;
	static File xmlInputDirectory;
	
}
