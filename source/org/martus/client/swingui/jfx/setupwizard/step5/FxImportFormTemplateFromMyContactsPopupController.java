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
package org.martus.client.swingui.jfx.setupwizard.step5;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;

import org.martus.client.swingui.UiMainWindow;
import org.martus.common.ContactKey;
import org.martus.common.ContactKeys;
import org.martus.common.MartusLogger;
import org.martus.common.fieldspec.CustomFieldTemplate;

public class FxImportFormTemplateFromMyContactsPopupController extends AbstractFxImportFormTemplateController
{
	public FxImportFormTemplateFromMyContactsPopupController(UiMainWindow mainWindow)
	{
		super(mainWindow);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		
		continueButton.setVisible(false);
		continueLabel.setVisible(false);
		
		contactsChoiceBox.valueProperty().addListener(new ContactsChangeHandler());
		contactsChoiceBox.setConverter(new ContactKeyStringConverter());
		
		chooseFormTemplateLabel.setVisible(false);
		templatesChoiceBox.setVisible(false);
		templatesChoiceBox.valueProperty().addListener(new TemplatesChangeHandler());
		templatesChoiceBox.setConverter(new FormTemplateToStringConverter());
		
		fillContactsChoiceBox();
	}
	
	private void fillContactsChoiceBox()
	{
		try
		{
			ContactKeys contactKeys = getApp().getContactKeys();
			ObservableList<ContactKey> contactKeysObservableList = FXCollections.observableArrayList();
			for (int index = 0; index < contactKeys.size(); ++index)
			{
				contactKeysObservableList.add(contactKeys.get(index));
			}

			contactsChoiceBox.setItems(contactKeysObservableList);
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}

	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/step5/SetupImportTemplateFromMyContactsPopup.fxml";
	}

	@Override
	public String getDialogTitle()
	{
		return getLocalization().getWindowTitle("notifyImportTemplate"); 
	}

	@FXML
	private void onCancel()
	{
		getStage().close();
	}
	
	@FXML
	private void onContinue()
	{
		onCancel();
	}
	
	@Override
	public String getLabel()
	{
		return "Import from My Contacts";
	}

	@Override
	public CustomFieldTemplate getSelectedFormTemplate()
	{
		return templatesChoiceBox.getSelectionModel().getSelectedItem();
	}
	
	protected class ContactsChangeHandler implements ChangeListener<ContactKey>
	{
		@Override
		public void changed(ObservableValue<? extends ContactKey> observable, ContactKey oldValue, ContactKey newValue)
		{
			if (newValue == null)
			{
				chooseFormTemplateLabel.setVisible(false);
				templatesChoiceBox.setVisible(false);
				templatesChoiceBox.getItems().clear();

				return;
			}

			try
			{
				ObservableList<CustomFieldTemplate> formTemplates = getFormTemplates(newValue);
				templatesChoiceBox.getItems().clear();
				templatesChoiceBox.getItems().addAll(formTemplates);
				chooseFormTemplateLabel.setVisible(true);
				templatesChoiceBox.setVisible(true);
			}
			catch (Exception e)
			{
				MartusLogger.logException(e);
			}
		}
	}
	
	protected class TemplatesChangeHandler implements ChangeListener<CustomFieldTemplate>
	{
		@Override
		public void changed(ObservableValue<? extends CustomFieldTemplate> observable, CustomFieldTemplate oldValue, CustomFieldTemplate newValue)
		{
			boolean shouldDisplayContinueButton = getSelectedFormTemplate() != null;
			continueButton.setVisible(shouldDisplayContinueButton);
			continueLabel.setVisible(shouldDisplayContinueButton);
		}
	}
	
	@FXML
	private ChoiceBox<ContactKey> contactsChoiceBox;
	
	@FXML
	protected ChoiceBox<CustomFieldTemplate> templatesChoiceBox;
	
	@FXML
	protected Label continueLabel;
	
	@FXML
	protected Button continueButton;
	
	@FXML
	protected Label chooseFormTemplateLabel;
}
