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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.filefilters.BulletinXmlFileFilter;
import org.martus.client.swingui.filefilters.MCTFileFilter;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.clientside.FormatFilter;
import org.martus.common.fieldspec.FormTemplate;

public class ConfirmUnencyptedXmlController extends FxController
{
	public ConfirmUnencyptedXmlController(UiMainWindow mainWindowToUse, String initialFileExportLocation)
	{
		super(mainWindowToUse);
		File fullPathOfInitialLocation = new File(getRootDirectory(), initialFileExportLocation);
		this.initialFileExportLocation = fullPathOfInitialLocation.getAbsolutePath();
	}

	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		fileLocation.setText(initialFileExportLocation);
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/bulletins/FxConfirmUnencryptedExport.fxml";
	}
	
	@FXML
	public void onChangeFileLocation(ActionEvent event)
	{
		//FIXME: This dialog can be hidden behind
		FileChooser fileChooser = new FileChooser();
		File martusRootDir = getRootDirectory();
		fileChooser.setInitialDirectory(martusRootDir);
		fileChooser.setTitle(getLocalization().getWindowTitle("FileDialogExportBulletins"));
		BulletinXmlFileFilter exportXmlFileFilter = new BulletinXmlFileFilter(getLocalization());
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter(exportXmlFileFilter.getDescription(), exportXmlFileFilter.getWildCardExtension()),
				new FileChooser.ExtensionFilter(getLocalization().getFieldLabel("AllFiles"), "*.*"));
		File templateFile = fileChooser.showSaveDialog(null);
		if(templateFile == null)
			return;
		fileLocation.setText(templateFile.getAbsolutePath());
	}

	private File getRootDirectory()
	{
		return getApp().getMartusDataRootDirectory();
	}
	
	public boolean includeAttachments()
	{
		return includeAttachments.isSelected();
	}
	
	public boolean includeAllRevisions()
	{
		return false;
	}
	
	@FXML
	TextArea textMessageArea;
	
	@FXML
	CheckBox includeAttachments;
	
	@FXML
	TextField fileLocation;
	
	private String initialFileExportLocation;
}
