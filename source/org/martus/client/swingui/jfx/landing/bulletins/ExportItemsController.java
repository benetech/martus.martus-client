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
package org.martus.client.swingui.jfx.landing.bulletins;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import org.martus.client.core.MartusApp;
import org.martus.client.core.TransferableBulletinList;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.filefilters.BulletinXmlFileFilter;
import org.martus.client.swingui.filefilters.MartusBulletinArchiveFileFilter;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.clientside.FormatFilter;

public class ExportItemsController extends FxController
{
	public ExportItemsController(UiMainWindow mainWindowToUse, String initialExportFilenameOnly, boolean singleBulletinBeingExported)
	{
		super(mainWindowToUse);
		this.singleBulletinBeingExported = singleBulletinBeingExported;
		this.initialExportFilenameOnly = initialExportFilenameOnly;
	}

	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		
		BooleanProperty encryptingExport = encryptExportFile.selectedProperty();
		includeAttachments.disableProperty().bind(encryptingExport);

		String initialFileExportPath = getInitialFileAbsolutePath();
		fileLocation.setText(initialFileExportPath);

		encryptExportFile.setDisable(isExportingMultipleBulletins());
		encryptExportFile.selectedProperty().addListener(new EncryptedStatusChanged());
		encryptExportFile.setSelected(singleBulletinBeingExported);
		updateControls(shouldExportEncrypted());
	}

	private boolean isExportingMultipleBulletins()
	{
		boolean exportingMultipleBulletins = !singleBulletinBeingExported;
		return exportingMultipleBulletins;
	}
	
	public boolean shouldExportEncrypted()
	{
		return encryptExportFile.isSelected();
	}
	
	private class EncryptedStatusChanged implements ChangeListener<Boolean>
	{
		public EncryptedStatusChanged()
		{
		}

		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) 
		{
			updateControls(newValue);
		}
	}
	
	protected void updateControls(Boolean exportEncrypted)
	{
		String hintMessage = "";
		if(!exportEncrypted)
			hintMessage = getLocalization().getFieldLabel("ExportBulletinDetails");
		textMessageArea.setText(hintMessage);
		includeAttachments.setSelected(exportEncrypted);
		updateExportFilename();
	}

	private void updateExportFilename()
	{
		File exportPath = getExportFile().getParentFile();
		String exportFilename = getExportFilenameBasedOnEncryptionStatus();
		File combinedExportFile = new File(exportPath, exportFilename);
		fileLocation.setText(combinedExportFile.getAbsolutePath());
	}
	
	private String getExportFilenameBasedOnEncryptionStatus()
	{
		String currentExportFilename = getExportFile().getName();
		int positionExtension = currentExportFilename.lastIndexOf('.');
		String fileNameOnly = currentExportFilename;
		if(positionExtension > 0)
			fileNameOnly = currentExportFilename.substring(0, positionExtension);
		return getExportFilenameBasedOnEncryptionStatus(fileNameOnly);
	}
	
	private String getExportFilenameBasedOnEncryptionStatus(String fileNameOnly)
	{
		String fullNameWithCorrectFileExtension = fileNameOnly;
		if(shouldExportEncrypted())
			fullNameWithCorrectFileExtension += TransferableBulletinList.BULLETIN_FILE_EXTENSION;			
		else
			fullNameWithCorrectFileExtension += MartusApp.MARTUS_IMPORT_EXPORT_EXTENSION;
		return fullNameWithCorrectFileExtension;
	}
	
	@Override
	public String getFxmlLocation()
	{
		return "landing/bulletins/FxExportItems.fxml";
	}
	
	@FXML
	public void onChangeFileLocation(ActionEvent event)
	{
		String FileChooserTitle = "FileDialogExportBulletins";
		FormatFilter fileFilter = getFormatFilter();
		File templateFile = getFileSaveLocation(FileChooserTitle, fileFilter);
		if(templateFile == null)
			return;
		fileLocation.setText(templateFile.getAbsolutePath());
	}

	private FormatFilter getFormatFilter()
	{
		MartusLocalization localization = getLocalization();
		if(shouldExportEncrypted())
			return new MartusBulletinArchiveFileFilter(localization);
		return new BulletinXmlFileFilter(localization);
	}
	
	public boolean includeAttachments()
	{
		return includeAttachments.isSelected();
	}
	
	File getExportFile()
	{
		return new File(fileLocation.getText());
	}

	protected String getInitialFileAbsolutePath()
	{
		File fullPathOfInitialLocation = new File(getApp().getMartusDataRootDirectory(), getExportFilenameBasedOnEncryptionStatus(initialExportFilenameOnly));
		return(fullPathOfInitialLocation.getAbsolutePath());
	}
		
	protected File getFileSaveLocation(String FileChooserTitle, FormatFilter fileFilter)
	{
		//FIXME: This dialog can be hidden behind
		MartusLocalization localization = getLocalization();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(getExportFile().getParentFile());
		fileChooser.setInitialFileName(getExportFilenameBasedOnEncryptionStatus());
		fileChooser.setTitle(localization.getWindowTitle(FileChooserTitle));
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter(fileFilter.getDescription(), fileFilter.getWildCardExtension()),
				new FileChooser.ExtensionFilter(localization.getFieldLabel("AllFiles"), "*.*"));
		File templateFile = fileChooser.showSaveDialog(null);
		return templateFile;
	}

	@FXML
	private TextArea textMessageArea;
	
	@FXML 
	private CheckBox encryptExportFile;

	@FXML
	private CheckBox includeAttachments;
	
	@FXML
	private TextField fileLocation;
	
	private boolean singleBulletinBeingExported;
	
	private String initialExportFilenameOnly;
}
