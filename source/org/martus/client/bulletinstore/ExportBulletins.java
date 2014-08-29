/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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
import java.util.Vector;

import org.martus.client.core.BulletinXmlExporter;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.MartusLogger;
import org.martus.common.ProgressMeterInterface;
import org.martus.util.UnicodeWriter;

public class ExportBulletins
{
	public ExportBulletins(UiMainWindow mainWindowToUse, ProgressMeterInterface progressDlgToUse)
	{
		mainWindow = mainWindowToUse;
		progressDlg = progressDlgToUse;
	}
	
	public void doExport(File destFile, Vector bulletinsToUse, boolean userWantsToExportPrivateToUse, boolean userWantsToExportAttachmentsToUse, boolean userWantsToExportAllVersionsToUse) {
		destinationFile = destFile;
		bulletinsToExport = bulletinsToUse;
		userWantsToExportPrivate = userWantsToExportPrivateToUse;
		userWantsToExportAttachments = userWantsToExportAttachmentsToUse;
		userWantsToExportAllVersions = userWantsToExportAllVersionsToUse;
		ExporterThread exporterThread = new ExporterThread(progressDlg);
		exporterThread.start();
	}
	
	public String getErrorMessage()
	{
		return exportMessageTag;
	}
	
	public Map getErrorMessageTokenMap()
	{
		return exportMessageTokensMap;
	}

	protected void updateExportMessage(ExporterThread exporterThread,
			int bulletinsExported, int numberOfMissingAttachment) 
	{
		exportMessageTag = "ExportComplete";
		if(exporterThread.didUnrecoverableErrorOccur())
		{
			exportMessageTag = "ErrorExportingBulletins";
			errorOccuredOrMissingAttachment = true;
		}
		else if(numberOfMissingAttachment > 0)
		{
			exportMessageTag = "ExportCompleteMissingAttachments";
			errorOccuredOrMissingAttachment = true;
		}
		exportMessageTokensMap = getTokenReplacementImporter(bulletinsExported, bulletinsToExport.size(), numberOfMissingAttachment);
	}

	Map getTokenReplacementImporter(int numberOfBulletinsExported, int totalNumberOfBulletins, int numberOfMissingAttachment) 
	{
		HashMap map = new HashMap();
		map.put("#BulletinsExported#", Integer.toString(numberOfBulletinsExported));
		map.put("#TotalBulletinsToExport#", Integer.toString(totalNumberOfBulletins));
		map.put("#AttachmentsNotExported#", Integer.toString(numberOfMissingAttachment));
		return map;
	}

	
	class ExporterThread extends Thread
	{
		public ExporterThread(ProgressMeterInterface progressRetrieveDlgToUse)
		{
			clientStore = mainWindow.getStore();
			progressMeter = progressRetrieveDlgToUse;
			exporter = new BulletinXmlExporter(mainWindow.getApp(), mainWindow.getLocalization(), progressMeter);
		}

		public void run()
		{
			try
			{
				UnicodeWriter writer = new UnicodeWriter(destinationFile);
				exporter.exportBulletins(writer, bulletinsToExport, userWantsToExportPrivate, userWantsToExportAttachments, userWantsToExportAllVersions, destinationFile.getParentFile());
				writer.close();
			}
			catch (Exception e)
			{
				MartusLogger.logException(e);
				errorOccured = true;
			}
			finally
			{
				progressMeter.finished();
				int numberOfMissingAttachment = getNumberOfFailingAttachments();
				int bulletinsExported = getNumberOfBulletinsExported();
				updateExportMessage(this, bulletinsExported, numberOfMissingAttachment);
			}
		}

		public int getNumberOfBulletinsExported()
		{
			return exporter.getNumberOfBulletinsExported();
		}
		
		public int getNumberOfFailingAttachments()
		{
			return exporter.getNumberOfFailingAttachments();
		}

		public boolean didUnrecoverableErrorOccur()
		{
			return errorOccured;
		}
		
		ProgressMeterInterface progressMeter;
		ClientBulletinStore clientStore;
		private BulletinXmlExporter exporter;
		private boolean errorOccured;
	}
	

	Map getTokenReplacementImporter(int bulletinsImported, int totalBulletins, String folder) 
	{
		HashMap map = new HashMap();
		map.put("#BulletinsSuccessfullyImported#", Integer.toString(bulletinsImported));
		map.put("#TotalBulletinsToImport#", Integer.toString(totalBulletins));
		map.put("#ImportFolder#", folder);
		return map;
	}
	
	public boolean didErrorOccur()
	{
		return errorOccuredOrMissingAttachment;
	}
	
	protected UiMainWindow mainWindow;
	protected File destinationFile;
	protected Vector bulletinsToExport;
	protected boolean userWantsToExportPrivate;
	protected boolean userWantsToExportAttachments;
	protected boolean userWantsToExportAllVersions;
	protected boolean errorOccuredOrMissingAttachment;
	
	private String exportMessageTag;
	private Map exportMessageTokensMap;
	private ProgressMeterInterface progressDlg;
}


