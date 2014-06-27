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
		initialFolderNameChoiceCode = FolderDefaultCode;
		initialCustomFolderName = "";
		folderLabelIndexListener = folderLabelIndexListenertoUse;
		folderCustomLabelListener = folderCustomLabelListenerToUse;
	}
	
	public void setInitialFolderName(String folderNameCode, String customFolderNameToUse)
	{
		initialCustomFolderName = customFolderNameToUse;
		if(folderNameCode.isEmpty())
			initialFolderNameChoiceCode = FolderDefaultCode;
		else
			initialFolderNameChoiceCode = folderNameCode;
	}
	
	public void initialize()
	{
		fxFolderCustomTextField.setText(initialCustomFolderName);
		ObservableList<ChoiceItem> folderNameChoices = getFolderLabelChoices(getMainWindow().getLocalization(), initialCustomFolderName);
		fxFolderChoiceBox.setItems(folderNameChoices);
		try
		{
			ChoiceItem initialChoice = getChoiceItem(folderNameChoices, initialFolderNameChoiceCode);
			fxFolderChoiceBox.getSelectionModel().select(initialChoice);
			updateCustomFolder(initialChoice);
		} 
		catch (FolderNotFoundException e)
		{
			getShellController().getStage().logAndNotifyUnexpectedError(e);
		}
		
		fxFolderCustomTextField.textProperty().addListener(folderCustomLabelListener);
		ReadOnlyObjectProperty<ChoiceItem> selectedItemProperty = fxFolderChoiceBox.getSelectionModel().selectedItemProperty();
		selectedItemProperty.addListener(new FolderNameChoiceBoxListener());
		selectedItemProperty.addListener(folderLabelIndexListener);
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
			updateCustomFolder(newItem);
		}
	}
	
	protected void updateCustomFolder(ChoiceItem currentSelectedItem)
	{
		if(currentSelectedItem.getCode().equals(FolderCodeCustom))
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

		folderChoices.add(new ChoiceItem(FolderCodeCases, localization.getFieldLabel("FolderNameCases")));
		folderChoices.add(new ChoiceItem(FolderCodeIncidents, localization.getFieldLabel("FolderNameIncidents")));
		folderChoices.add(new ChoiceItem(FolderCodeProjects, localization.getFieldLabel("FolderNameProjects")));
		folderChoices.add(new ChoiceItem(FolderCodeCustom, localization.getFieldLabel("FolerNameUserDefined")));
		return folderChoices;
	}	
		
	public static String getFolderLabel(MartusLocalization localization, String folderCodeToFind, String customFolderLabel) throws FolderNotFoundException
	{
		if(folderCodeToFind.equals(FolderCodeCustom))
			return customFolderLabel;
		ObservableList<ChoiceItem> folderChoices = getFolderLabelChoices(localization, customFolderLabel);
		if(folderCodeToFind.isEmpty())
			folderCodeToFind = FolderDefaultCode; 
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
	
	public static final String FolderCodeCases = "cases"; 
	public static final String FolderCodeIncidents = "incidents"; 
	public static final String FolderCodeProjects = "projects"; 
	public static final String FolderCodeCustom = "custom"; 
	public static final String FolderDefaultCode = FolderCodeCases;
	
	private static final String LOCATION_FOLDER_SETTINGS_FXML = "landing/FolderSettings.fxml";
	private String initialFolderNameChoiceCode;
	private String initialCustomFolderName;
	private ChangeListener folderLabelIndexListener;
	private ChangeListener folderCustomLabelListener;
}
