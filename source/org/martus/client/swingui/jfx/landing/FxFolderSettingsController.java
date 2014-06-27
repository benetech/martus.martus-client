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
package org.martus.client.swingui.jfx.landing;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;

public class FxFolderSettingsController extends DialogWithCloseController
{
	public FxFolderSettingsController(UiMainWindow mainWindowToUse, ChangeListener folderLabelIndexListenertoUse, ChangeListener folderCustomLabelListenerToUse)
	{
		super(mainWindowToUse);
		currentFolderNameChoice = FolderNameCases;
		customFolderName = "";
		folderLabelIndexListener = folderLabelIndexListenertoUse;
		folderCustomLabelListener = folderCustomLabelListenerToUse;
	}
	
	public void setInitialFolderName(int folderNameIndex, String customFolderNameToUse)
	{
		currentFolderNameChoice = folderNameIndex;
		customFolderName = customFolderNameToUse;
	}
	
	public void initialize()
	{
		ArrayObservableList<String> folderNameChoices = new ArrayObservableList<>();
		MartusLocalization localization = getMainWindow().getLocalization();
		
		folderNameChoices.add(localization.getFieldLabel("FolderNameCases"));
		folderNameChoices.add(localization.getFieldLabel("FolderNameIncidents"));
		folderNameChoices.add(localization.getFieldLabel("FolderNameProjects"));
		folderNameChoices.add(localization.getFieldLabel("FolerNameUserDefined"));
		
		fxFolderCustomTextField.textProperty().addListener(folderCustomLabelListener);
		fxFolderCustomTextField.setText(customFolderName);

		fxFolderChoiceBox.setItems(folderNameChoices);
		ReadOnlyIntegerProperty selectedIndexProperty = fxFolderChoiceBox.getSelectionModel().selectedIndexProperty();
		selectedIndexProperty.addListener(new FolderNameChoiceBoxListener());
		selectedIndexProperty.addListener(folderLabelIndexListener);
		fxFolderChoiceBox.getSelectionModel().select(currentFolderNameChoice);
		
	}

	private final class FolderNameChoiceBoxListener implements ChangeListener<Number>
	{
		public FolderNameChoiceBoxListener()
		{
		}

		@Override public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) 
		{
			updateCustomFolder();
		}
	}
	
	protected void updateCustomFolder()
	{
		if(fxFolderChoiceBox.getSelectionModel().getSelectedIndex() == FolderNameCustom)
		{
			fxFolderCustomTextField.setVisible(true);
		}
		else
		{
			fxFolderCustomTextField.setVisible(false);
		}
	}
	
	@Override
	public String getFxmlLocation()
	{
		return LOCATION_FOLDER_SETTINGS_FXML;
	}

	@FXML
	ChoiceBox<String> fxFolderChoiceBox;
	
	@FXML
	TextField fxFolderCustomTextField;
	
	public static final int FolderNameCases = 0; 
	public static final int FolderNameIncidents = 1; 
	public static final int FolderNameProjects = 2; 
	public static final int FolderNameCustom = 3; 
	
	private static final String LOCATION_FOLDER_SETTINGS_FXML = "landing/FolderSettings.fxml";
	private int currentFolderNameChoice;
	private String customFolderName;
	private ChangeListener folderLabelIndexListener;
	private ChangeListener folderCustomLabelListener;
}
