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
import javafx.scene.control.ComboBox;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiPreferencesDlg;
import org.martus.common.fieldspec.ChoiceItem;

public class FxSetupSettingsController extends AbstractFxSetupWizardController implements Initializable
{
	public FxSetupSettingsController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void nextWasPressed(ActionEvent event) 
	{
		getMainWindow().getApp().getConfigInfo().setForceBulletinsAllPrivate(preventPublicBulletinsCheckBox.isSelected());
		getMainWindow().getApp().getConfigInfo().setCheckForFieldOfficeBulletins(userTorCheckBox.isSelected());
		
		getLocalization().setMdyOrder(dateFormatSequenceDropDown.getSelectionModel().getSelectedItem().getCode());
		String delimiter = dateDelimeterComboBox.getSelectionModel().getSelectedItem().getCode();
		getLocalization().setDateDelimiter(delimiter.charAt(0));
		getMainWindow().saveConfigInfo();
	}
	
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		dateFormatSequenceDropDown.setItems(FXCollections.observableArrayList(getDateFormatChoices()));
		dateFormatSequenceDropDown.getSelectionModel().select(0);
		
		dateDelimeterComboBox.setItems(FXCollections.observableArrayList(getDateDelimeterChoices()));
		dateDelimeterComboBox.getSelectionModel().select(0);
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
	public String getNextControllerClassName()
	{
		return FxSetupStorageServerController.class.getSimpleName();
	}

	@FXML
	private CheckBox preventPublicBulletinsCheckBox;
	
	@FXML
	private CheckBox userTorCheckBox;
	
	@FXML
	private ComboBox<ChoiceItem> dateFormatSequenceDropDown;
	
	@FXML
	private ComboBox<ChoiceItem> dateDelimeterComboBox;
}
