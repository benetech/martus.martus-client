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
import org.martus.client.swingui.dialogs.UiImportExportProgressMeterDlg;
import org.martus.client.tools.XmlBulletinsImporter.FieldSpecVerificationException;
import org.martus.common.bulletin.Bulletin;

public class ImporterOfXmlFilesOfBulletins
{
	public ImporterOfXmlFilesOfBulletins(File[] bulletinXmlFilesToImportToUse, ClientBulletinStore clientStoreToUse, BulletinFolder importFolderToUse, PrintStream consoleMonitorToUse)
	{
		this(bulletinXmlFilesToImportToUse, clientStoreToUse, importFolderToUse, consoleMonitorToUse, null);
	}

	public ImporterOfXmlFilesOfBulletins(File[] bulletinXmlFilesToImportToUse, ClientBulletinStore clientStoreToUse, BulletinFolder importFolderToUse,UiImportExportProgressMeterDlg progressMeterToUse)
	{
		this(bulletinXmlFilesToImportToUse, clientStoreToUse, importFolderToUse, null, progressMeterToUse);
	}

	private ImporterOfXmlFilesOfBulletins(File[] bulletinXmlFilesToImportToUse, ClientBulletinStore clientStoreToUse, BulletinFolder importFolderToUse, PrintStream consoleMonitorToUse, UiImportExportProgressMeterDlg progressMeterToUse)
	{
		super();
		progressMeter = progressMeterToUse;
		consoleMonitor = consoleMonitorToUse;
		bulletinXmlFilesToImport = bulletinXmlFilesToImportToUse;
		clientStore = clientStoreToUse;
		importFolder = importFolderToUse;
		baseAttachmentsDirectory = null;
		totalBulletins = 0;
		bulletinsSuccessfullyImported = 0;
	}
	
	
	public void importFiles()  throws FieldSpecVerificationException, Exception
	{
		for(int i= 0; i < bulletinXmlFilesToImport.length; ++i)
		{
			bulletinsSuccessfullyImported += importOneXmlFile(bulletinXmlFilesToImport[i]);
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
		int numberOfBulletinsImportedFromXmlFile = 0;
		int bulletinsToImport = bulletins.length;
		totalBulletins += bulletinsToImport;
		for(int j = 0; j < bulletinsToImport; ++j)
		{
			Bulletin b =  bulletins[j];
			if(progressMeter != null)
			{
				if(progressMeter.shouldExit())
					break;
				progressMeter.updateBulletinCountMeter(j, bulletinsToImport);
				progressMeter.updateBulletinTitle(b.get(Bulletin.TAGTITLE));

			}
			if(consoleMonitor != null)
				consoleMonitor.println("Importing:" +b.get(Bulletin.TAGTITLE));
			try
			{
				clientStore.saveBulletin(b);
				clientStore.addBulletinToFolder(importFolder, b.getUniversalId());
				++numberOfBulletinsImportedFromXmlFile;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		clientStore.saveFolders();
		return numberOfBulletinsImportedFromXmlFile;
	}
	
	public int getNumberOfBulletinsImported()
	{
		return bulletinsSuccessfullyImported;
	}

	public int getTotalNumberOfBulletins()
	{
		return totalBulletins;
	}

	private int bulletinsSuccessfullyImported;
	private int totalBulletins;
	private File[] bulletinXmlFilesToImport;
	private ClientBulletinStore clientStore;
	private BulletinFolder importFolder;
	private UiImportExportProgressMeterDlg progressMeter;
	private PrintStream consoleMonitor;
	public File baseAttachmentsDirectory;
}
