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

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;

import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.setupwizard.step6.FxSelectLanguageController;
import org.martus.clientside.MtfAwareLocalization;
import org.martus.common.MartusLogger;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.ChoiceItem;

public class SettingsforSystemController extends FxController
{
	public SettingsforSystemController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		useZawgyiFont.selectedProperty().setValue(getApp().getConfigInfo().getUseZawgyiFont());
		initializeLanguageChoices();

	} 
		
	private void initializeLanguageChoices()
	{
		ObservableList<ChoiceItem> availableLanguages = FXCollections.observableArrayList(FxSelectLanguageController.getAvailableLanguages(getLocalization()));
		languageSelection.setItems(availableLanguages);
		ChoiceItem currentLanguageChoiceItem = FxSelectLanguageController.findCurrentLanguageChoiceItem(getLocalization());
		languageSelection.getSelectionModel().selectedItemProperty().addListener(new LanguageSelectionListener());
		languageSelection.getSelectionModel().select(currentLanguageChoiceItem);
	}
	
	class LanguageSelectionListener implements ChangeListener<ChoiceItem>
	{
		@Override
		public void changed(ObservableValue<? extends ChoiceItem> observableValue,
				ChoiceItem oldItem, ChoiceItem newItem)
		{
			updateZawgyiFont(newItem);
		}
	}
	
	protected void updateZawgyiFont(ChoiceItem itemSelected)
	{
		if(itemSelected.getCode().equals(MiniLocalization.BURMESE))
			useZawgyiFont.setVisible(true);
		else
			useZawgyiFont.setVisible(false);
	}

	@Override
	public void save()
	{
		try
		{
			getApp().saveConfigInfo();
		} 
		catch (SaveConfigInfoException e)
		{
			MartusLogger.logException(e);
			showNotifyDialog("ErrorSavingConfig");
		}
		super.save();
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/general/SettingsForSystem.fxml";
	}
	
	@FXML
	public void onSaveChanges()
	{
		getApp().getConfigInfo().setUseZawgyiFont(useZawgyiFont.selectedProperty().getValue());
		String selectedLanguageCode = languageSelection.getSelectionModel().getSelectedItem().getCode();
		if (MtfAwareLocalization.isRecognizedLanguage(selectedLanguageCode))
			getLocalization().setCurrentLanguageCode(selectedLanguageCode);

		save();
	}

	@FXML 
	private CheckBox useZawgyiFont;
	
	@FXML
	private CheckBox useThaiPersianLegacyDates;
	
	@FXML
	private ChoiceBox<ChoiceItem> languageSelection;
	
	@FXML
	private ChoiceBox dateFormat;
	
	@FXML
	private ChoiceBox dateDelimiter;
	
	@FXML
	private ChoiceBox calendarType;
}
