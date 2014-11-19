/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
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
package org.martus.client.swingui.jfx.landing.general;

import java.io.File;

import javafx.application.Platform;

import javax.swing.JFileChooser;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionDoer;
import org.martus.client.swingui.filefilters.AllFileFilter;
import org.martus.client.swingui.filefilters.BulletinXmlFileFilter;
import org.martus.client.swingui.filefilters.MCTFileFilter;
import org.martus.client.swingui.jfx.generic.FxInSwingContentController;
import org.martus.clientside.FormatFilter;
import org.martus.common.MartusLogger;

public class ImportTemplateAction implements ActionDoer
{
	public ImportTemplateAction(ManageTemplatesController manageTemplatesControllerToUse)
	{
		manageTemplatesController = manageTemplatesControllerToUse;
	}
	
	@Override
	public void doAction()
	{
		JFileChooser fileChooser = new JFileChooser(getApp().getMartusDataRootDirectory());
		fileChooser.setDialogTitle(getLocalization().getWindowTitle("confirmImportingCustomizationUnknownSigner"));
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(new AllFileFilter(getLocalization()));
		fileChooser.addChoosableFileFilter(new MCTFileFilter(getLocalization()));
		fileChooser.addChoosableFileFilter(new BulletinXmlFileFilter(getLocalization()));
		int userChoice = fileChooser.showOpenDialog(getMainWindow());
		if (userChoice != JFileChooser.APPROVE_OPTION)
			return;
		
		File templateFile = fileChooser.getSelectedFile();
		if(templateFile == null)
			return;
		
		FormatFilter chosenExtensionFilter = (FormatFilter) fileChooser.getFileFilter();
		Platform.runLater(new ImportFormTemplateRunner(templateFile, chosenExtensionFilter));
	}
	
	protected MartusLocalization getLocalization()
	{
		return getMainWindow().getLocalization();
	}

	private MartusApp getApp()
	{
		return getMainWindow().getApp();
	}
	
	protected UiMainWindow getMainWindow()
	{
		return manageTemplatesController.getMainWindow();
	}

	protected ManageTemplatesController getManageTemplatesController()
	{
		return manageTemplatesController;
	}
	
	protected class ImportFormTemplateRunner implements Runnable
	{
		public ImportFormTemplateRunner(File templateFileToImportToUse, FormatFilter chosenExtensionFilterToUse)
		{
			templateFileToImport = templateFileToImportToUse;
			chosenExtensionFilter = chosenExtensionFilterToUse;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (FxInSwingContentController.isMctFileFilterSelected(getLocalization(), chosenExtensionFilter, templateFileToImport))
					getManageTemplatesController().importFormTemplateFromMctFile(templateFileToImport);

				if (FxInSwingContentController.isXmlExtensionSelected(getLocalization(), chosenExtensionFilter, templateFileToImport))
					getManageTemplatesController().importXmlFormTemplate(templateFileToImport);
			}
			catch (Exception e)
			{
				MartusLogger.logException(e);
				UiMainWindow.showNotifyDlgOnSwingThread(getMainWindow(), "PublicInfoFileError");
			}
		}

		private File templateFileToImport;
		private FormatFilter chosenExtensionFilter;
	}

	private ManageTemplatesController manageTemplatesController;
}
