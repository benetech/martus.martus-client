/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006, Beneficent
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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiImporterProgressMeterDlg;
import org.martus.client.tools.ImporterOfXmlFilesOfBulletins;

public class ImportBulletins
{
	
	public ImportBulletins(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
	}

	public void doImport(File xmlFileToImport, String importingFolderName)
	{
		try
		{
			File[] xmlFilesToImport = new File[] {xmlFileToImport};
			UiImporterProgressMeterDlg progressRetrieveDlg = new UiImporterProgressMeterDlg(mainWindow, "ImportProgress");
			BulletinFolder importFolder = mainWindow.getStore().createOrFindFolder(importingFolderName);
			ImporterThread importThread = new ImporterThread(xmlFilesToImport, importingFolderName, progressRetrieveDlg);
			importThread.start();
			progressRetrieveDlg.setVisible(true);
			mainWindow.selectFolder(importFolder);
			mainWindow.folderContentsHaveChanged(importFolder);
			mainWindow.folderTreeContentsHaveChanged();
			int numberOfBulletinsImported = importThread.getNumberOfBulletinsImported();
			int totalBulletins = importThread.getTotalBulletins();
			mainWindow.notifyDlg("ImportComplete", getTokenReplacementImporter(numberOfBulletinsImported, totalBulletins, importingFolderName));
		}
		catch (Exception e)
		{
			mainWindow.notifyDlg("ErrorImportingBulletins");
		}
	}
	
	class ImporterThread extends Thread
	{
		public ImporterThread(File[] xmlFilesToImport, String importingFolderName, UiImporterProgressMeterDlg progressRetrieveDlgToUse)
		{
			clientStore = mainWindow.getStore();
			BulletinFolder folder = clientStore.createOrFindFolder(importingFolderName);
			filesToImport = xmlFilesToImport;
			importFolder = folder;
			progressMeter = progressRetrieveDlgToUse;
			
		}

		public void run()
		{
			try
			{
				importer = new ImporterOfXmlFilesOfBulletins(filesToImport, clientStore, importFolder, progressMeter);
				importer.setAttachmentsDirectory(filesToImport[0].getParentFile());
				importer.importFiles();
			}
			catch (Exception e)
			{
				mainWindow.notifyDlg("ErrorImportingBulletins");
			}
			finally
			{
				progressMeter.finished();
			}
		}

		public int getNumberOfBulletinsImported()
		{
			return importer.getNumberOfBulletinsImported();
		}
		
		public int getTotalBulletins()
		{
			return importer.getTotalNumberOfBulletins();
		}
		
		private File[] filesToImport;
		private BulletinFolder importFolder;
		private UiImporterProgressMeterDlg progressMeter;
		private ClientBulletinStore clientStore;
		private ImporterOfXmlFilesOfBulletins importer;
	}
	

	Map getTokenReplacementImporter(int bulletinsImported, int totalBulletins, String folder) 
	{
		HashMap map = new HashMap();
		map.put("#BulletinsSuccessfullyImported#", Integer.toString(bulletinsImported));
		map.put("#TotalBulletinsToImport#", Integer.toString(totalBulletins));
		map.put("#ImportFolder#", folder);
		return map;
	}

	UiMainWindow mainWindow;
	
}
