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
package org.martus.client.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.tools.XmlBulletinsImporter.FieldSpecVerificationException;
import org.martus.common.bulletin.Bulletin;

public class ImporterOfXmlFilesOfBulletins
{
	public ImporterOfXmlFilesOfBulletins(File[] bulletinXmlFilesToImportToUse, ClientBulletinStore clientStoreToUse, BulletinFolder importFolderToUse, PrintStream progressMonitorToUse)
	{
		super();
		progressMonitor = progressMonitorToUse;
		bulletinXmlFilesToImport = bulletinXmlFilesToImportToUse;
		clientStore = clientStoreToUse;
		importFolder = importFolderToUse;
		baseAttachmentsDirectory = null;
	}
	
	public void importFiles()  throws FieldSpecVerificationException, Exception
	{
		for(int i= 0; i < bulletinXmlFilesToImport.length; ++i)
		{
			bulletinsImported += importOneXmlFile(bulletinXmlFilesToImport[i]);
		}
	}
	
	public void setAttachmentsDirectory(File baseAttachmentsDirectoryToUse)
	{
		baseAttachmentsDirectory = baseAttachmentsDirectoryToUse;
	}

	private int importOneXmlFile(File bulletinXmlFileToImport) throws FieldSpecVerificationException, Exception
	{
		FileInputStream xmlIn = new FileInputStream(bulletinXmlFileToImport);
		XmlBulletinsImporter importer = new XmlBulletinsImporter(clientStore.getSignatureVerifier(), xmlIn, baseAttachmentsDirectory);
		Bulletin[] bulletins = importer.getBulletins();
		importFolder.prepareForBulkOperation();
		try
		{
			for(int j = 0; j < bulletins.length; ++j)
			{
				Bulletin b =  bulletins[j];
				progressMonitor.println("Importing:" +b.get(Bulletin.TAGTITLE));
				clientStore.saveBulletin(b);
				clientStore.addBulletinToFolder(importFolder, b.getUniversalId());
			}
		}
		finally
		{
			clientStore.saveFolders();
		}
		return bulletins.length;
	}
	
	public int getNumberOfBulletinsImported()
	{
		return bulletinsImported;
	}

	private int bulletinsImported;
	private File[] bulletinXmlFilesToImport;
	private ClientBulletinStore clientStore;
	private BulletinFolder importFolder;
	private PrintStream progressMonitor;
	public File baseAttachmentsDirectory;
}
