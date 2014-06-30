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

import java.util.Iterator;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import org.martus.client.core.ConfigInfo;
import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.fieldspec.ChoiceItem;

public class FxFolderSettingsController extends DialogWithCloseController
{
	public static class FolderNotFoundException extends Exception {}
	
	public FxFolderSettingsController(UiMainWindow mainWindowToUse, ChangeListener folderLabelIndexListenertoUse, ChangeListener folderCustomLabelListenerToUse)
	{
		super(mainWindowToUse);
		ConfigInfo config = mainWindowToUse.getApp().getConfigInfo();
		folderNameCustom = config.getFolderLabelCustomName();
		folderNameCode = config.getFolderLabelCode();
		if(folderNameCode.isEmpty())
			folderNameCode = FOLDER_CODE_DEFAULT;
		folderLabelIndexListener = folderLabelIndexListenertoUse;
		folderCustomLabelListener = folderCustomLabelListenerToUse;
	}
	
	public void initialize()
	{
		ObservableList<ChoiceItem> folderNameChoices = getFolderLabelChoices(getMainWindow().getLocalization(), folderNameCustom);
		fxFolderChoiceBox.setItems(folderNameChoices);
		try
		{
			ChoiceItem initialChoice = getChoiceItem(folderNameChoices, folderNameCode);
			fxFolderChoiceBox.getSelectionModel().select(initialChoice);
			updateCustomFolder();
		} 
		catch (FolderNotFoundException e)
		{
			getShellController().getStage().logAndNotifyUnexpectedError(e);
		}
		ReadOnlyObjectProperty<ChoiceItem> selectedItemProperty = fxFolderChoiceBox.getSelectionModel().selectedItemProperty();
		selectedItemProperty.addListener(new FolderNameChoiceBoxListener());
		selectedItemProperty.addListener(folderLabelIndexListener);
		
		fxFolderCustomTextField.setText(folderNameCustom);
		fxFolderCustomTextField.textProperty().addListener(new FolderNameCustomLabelListener());
		fxFolderCustomTextField.textProperty().addListener(folderCustomLabelListener);
	}
	
	private ChoiceItem getChoiceItem(ObservableList<ChoiceItem> folderNameChoices, String folderCode) throws FolderNotFoundException
	{
		for (Iterator iterator = folderNameChoices.iterator(); iterator.hasNext();)
		{
			ChoiceItem folderItem = (ChoiceItem) iterator.next();
			if(folderItem.getCode().equals(folderCode))
				return folderItem;
		}
		throw new FolderNotFoundException();
	}
	
	@Override
	public void exitingController()
	{
		try
		{
			ConfigInfo config = getApp().getConfigInfo();
			config.setFolderLabelCode(fxFolderChoiceBox.getSelectionModel().getSelectedItem().getCode());
			config.setFolderLabelCustomName(fxFolderCustomTextField.getText());
			
			getApp().saveConfigInfo();
		} 
		catch (SaveConfigInfoException e)
		{
			getShellController().getStage().logAndNotifyUnexpectedError(e);
		}
	}
	
	private final class FolderNameChoiceBoxListener implements ChangeListener<ChoiceItem>
	{
		public FolderNameChoiceBoxListener()
		{
		}

		@Override public void changed(ObservableValue<? extends ChoiceItem> observableValue, ChoiceItem originalItem, ChoiceItem newItem) 
		{
			folderNameCode = newItem.getCode();
			updateCustomFolder();
		}
	}
	
	private final class FolderNameCustomLabelListener implements ChangeListener<String>
	{
		public FolderNameCustomLabelListener()
		{
		}

		@Override public void changed(ObservableValue<? extends String> observableValue, String original, String newLabel) 
		{
			folderNameCustom = newLabel;
		}
	}
	
	protected void updateCustomFolder()
	{
		if(folderNameCode.equals(FOLDER_CODE_CUSTOM))
		{
			fxFolderCustomTextField.setVisible(true);
		}
		else
		{
			fxFolderCustomTextField.setVisible(false);
		}
	}
	
	private static ObservableList<ChoiceItem> getFolderLabelChoices(MartusLocalization localization, String customFolderName)
	{
		ObservableList<ChoiceItem> folderChoices = FXCollections.observableArrayList();

		folderChoices.add(new ChoiceItem(FOLDER_CODE_CASES, localization.getFieldLabel("FolderNameCases")));
		folderChoices.add(new ChoiceItem(FOLDER_CODE_INCIDENTS, localization.getFieldLabel("FolderNameIncidents")));
		folderChoices.add(new ChoiceItem(FOLDER_CODE_PROJECTS, localization.getFieldLabel("FolderNameProjects")));
		folderChoices.add(new ChoiceItem(FOLDER_CODE_CUSTOM, localization.getFieldLabel("FolerNameUserDefined")));
		return folderChoices;
	}	
		
	public String getFolderLabel(String folderCodeToFind) throws FolderNotFoundException
	{
		if(folderCodeToFind.equals(FOLDER_CODE_CUSTOM))
			return folderNameCustom;
		ObservableList<ChoiceItem> folderChoices = getFolderLabelChoices(getMainWindow().getLocalization(), folderNameCustom);
		for (Iterator iterator = folderChoices.iterator(); iterator.hasNext();)
		{
			ChoiceItem folder = (ChoiceItem) iterator.next();
			if(folder.getCode().equals(folderCodeToFind))
				return folder.getLabel();
		}
		throw new FolderNotFoundException();
	}
	
	@Override
	public String getFxmlLocation()
	{
		return LOCATION_FOLDER_SETTINGS_FXML;
	}

	@FXML
	ChoiceBox<ChoiceItem> fxFolderChoiceBox;
	
	@FXML
	TextField fxFolderCustomTextField;
	
	public static final String FOLDER_CODE_CASES = "cases"; 
	public static final String FOLDER_CODE_INCIDENTS = "incidents"; 
	public static final String FOLDER_CODE_PROJECTS = "projects"; 
	public static final String FOLDER_CODE_CUSTOM = "custom"; 
	public static final String FOLDER_CODE_DEFAULT = FOLDER_CODE_CASES;
	
	private static final String LOCATION_FOLDER_SETTINGS_FXML = "landing/FolderSettings.fxml";
	protected String folderNameCode;
	protected String folderNameCustom;
	private ChangeListener folderLabelIndexListener;
	private ChangeListener folderCustomLabelListener;
}
