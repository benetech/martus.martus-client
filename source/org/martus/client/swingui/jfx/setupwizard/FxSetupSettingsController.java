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
package org.martus.client.swingui.jfx.setupwizard;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Vector;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;

import org.martus.client.core.ConfigInfo;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiPreferencesDlg;
import org.martus.client.swingui.jfx.FxController;
import org.martus.clientside.CurrentUiState;
import org.martus.common.fieldspec.ChoiceItem;

public class FxSetupSettingsController extends AbstractFxSetupWizardContentController implements Initializable
{
	public FxSetupSettingsController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		userTorCheckBox.setSelected(getApp().getConfigInfo().useInternalTor());

		ObservableList<ChoiceItem> dateFormatChoices = getDateFormatChoices();
		dateFormatSequenceDropDown.setItems(FXCollections.observableArrayList(dateFormatChoices));
		
		MartusLocalization localization = getLocalization();
		String dateFormatCode = localization.getMdyOrder();
		selectItemByCode(dateFormatSequenceDropDown, dateFormatCode);
		
		ObservableList<ChoiceItem> dateDelimeterChoices = getDateDelimeterChoices();
		dateDelimeterComboBox.setItems(FXCollections.observableArrayList(dateDelimeterChoices));
	
		String dateDelimeterCode = "" + localization.getDateDelimiter();
		selectItemByCode(dateDelimeterComboBox, dateDelimeterCode);
	}

	private void selectItemByCode(ChoiceBox choiceBox, String code)
	{
		ObservableList<ChoiceItem> choices = choiceBox.getItems();
		for(int i = 0; i < choices.size(); ++i)
			if(choices.get(i).getCode().equals(code))
				choiceBox.getSelectionModel().select(i);
	}
	
	@Override
	public void nextWasPressed(ActionEvent event) 
	{
		saveTorConfigurationAndForceBulletinsAllPrivate();
		saveDateFormatConfiguration();
	}

	private void saveDateFormatConfiguration()
	{
		MartusLocalization localization = getMainWindow().getLocalization();
		localization.setMdyOrder(dateFormatSequenceDropDown.getSelectionModel().getSelectedItem().getCode());
		String delimiter = dateDelimeterComboBox.getSelectionModel().getSelectedItem().getCode();
		localization.setDateDelimiter(delimiter.charAt(0));
		String dateFormat = localization.getCurrentDateFormatCode();
		CurrentUiState uiState = getMainWindow().getCurrentUiState();
		uiState.setCurrentDateFormat(dateFormat);
		getMainWindow().saveCurrentUiState();
	}

	private void saveTorConfigurationAndForceBulletinsAllPrivate()
	{
		ConfigInfo configInfo = getApp().getConfigInfo();
		//NOTE: This might belong somewhere else, but for now it's important to set it.
		configInfo.setForceBulletinsAllPrivate(true);
		configInfo.setUseInternalTor(userTorCheckBox.isSelected());
		getMainWindow().saveConfigInfo();
		getApp().startOrStopTorAsRequested();
	}
	
	private ObservableList<ChoiceItem> getDateFormatChoices()
	{
		Vector<ChoiceItem> choices = new Vector<ChoiceItem>();
		choices.add(new ChoiceItem("ymd", UiPreferencesDlg.buildMdyLabel(getLocalization(), "ymd")));
		choices.add(new ChoiceItem("mdy", UiPreferencesDlg.buildMdyLabel(getLocalization(), "mdy")));
		choices.add(new ChoiceItem("dmy", UiPreferencesDlg.buildMdyLabel(getLocalization(), "dmy")));

		return FXCollections.observableArrayList(choices);
	}
	
	private ObservableList<ChoiceItem> getDateDelimeterChoices()
	{
		Vector<ChoiceItem> choices = new Vector<ChoiceItem>();
		choices.add(new ChoiceItem("/", getLocalization().getFieldLabel("DateDelimiterSlash")));
		choices.add(new ChoiceItem("-", getLocalization().getFieldLabel("DateDelimiterDash")));
		choices.add(new ChoiceItem(".", getLocalization().getFieldLabel("DateDelimiterDot")));

		return FXCollections.observableArrayList(choices);
	}
	
	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/SetupSettings.fxml";
	}
	
	@Override
	public FxController getNextControllerClassName()
	{
		return new FxSetupStorageServerController(getMainWindow());
	}

	@FXML
	private CheckBox userTorCheckBox;
	
	@FXML
	private ChoiceBox<ChoiceItem> dateFormatSequenceDropDown;
	
	@FXML
	private ChoiceBox<ChoiceItem> dateDelimeterComboBox;
}
