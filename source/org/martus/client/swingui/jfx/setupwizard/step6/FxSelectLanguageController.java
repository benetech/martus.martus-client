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
package org.martus.client.swingui.jfx.setupwizard.step6;

import java.util.Vector;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.setupwizard.AbstractFxSetupWizardContentController;
import org.martus.clientside.MtfAwareLocalization;
import org.martus.common.fieldspec.ChoiceItem;

public class FxSelectLanguageController extends FxStep6Controller
{
	public FxSelectLanguageController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void nextWasPressed() throws Exception
	{
		String selectedLanguageCode = languagesDropdown.getSelectionModel().getSelectedItem().getCode();
		
		if (MtfAwareLocalization.isRecognizedLanguage(selectedLanguageCode))
			getLocalization().setCurrentLanguageCode(selectedLanguageCode);
		super.nextWasPressed();
	}
	
	public void initializeMainContentPane()
	{
		ObservableList<ChoiceItem> availableLanguages = FXCollections.observableArrayList(getAvailableLanguages());
		languagesDropdown.setItems(availableLanguages);
		ChoiceItem currentLanguageChoiceItem = findCurrentLanguageChoiceItem();
		languagesDropdown.getSelectionModel().select(currentLanguageChoiceItem);
		
		getWizardNavigationHandler().getBackButton().setVisible(false);
	}
	
	private ChoiceItem findCurrentLanguageChoiceItem()
	{
		String currentLanguageCode = getLocalization().getCurrentLanguageCode();

		ObservableList<ChoiceItem> availableLanguages = getAvailableLanguages();
		for (ChoiceItem choiceItem : availableLanguages)
		{
			if (choiceItem.getCode().equals(currentLanguageCode))
				return choiceItem;
		}
		
		return null;
	}

	private ObservableList<ChoiceItem> getAvailableLanguages()
	{
		ChoiceItem[] allUILanguagesSupported = getLocalization().getUiLanguages();
		Vector<ChoiceItem> languageChoices = new Vector<ChoiceItem>();
		for(int i = 0; i < allUILanguagesSupported.length; ++i)
		{
			String currentCode = allUILanguagesSupported[i].getCode();
			String languageName = getLocalization().getLanguageName(currentCode);
			languageChoices.add(new ChoiceItem(currentCode, languageName));
		}
		return FXCollections.observableArrayList(languageChoices);
	}

	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/step6/SetupLanguage.fxml";
	}
	
	@Override
	public AbstractFxSetupWizardContentController getNextController()
	{
		return null;
	}
	
	@FXML // fx:id="languagesDropdown"
	private ChoiceBox<ChoiceItem> languagesDropdown; // Value injected by FXMLLoader
}
