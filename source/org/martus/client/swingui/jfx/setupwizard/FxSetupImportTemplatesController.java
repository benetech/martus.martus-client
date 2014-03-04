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

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Vector;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.filefilters.MCTFileFilter;
import org.martus.client.swingui.jfx.FxController;
import org.martus.common.MartusLogger;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.CustomFieldTemplate;
import org.martus.util.TokenReplacement;

public class FxSetupImportTemplatesController extends AbstractFxSetupWizardContentController implements Initializable
{
	public FxSetupImportTemplatesController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/SetupImportTemplate.fxml";
	}

	@Override
	public FxController getNextControllerClassName()
	{
		return new FxSetupBackupYourKeyController(getMainWindow());
	}

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		genericTemplatesComboBox.setConverter(new CustomFieldTemplateToStringConverter());
		genericTemplatesComboBox.setItems(FXCollections.observableArrayList(getGenericTemplateChoices()));

		customTemplatesComboBox.setItems(FXCollections.observableArrayList(getImportTemplateChoices()));
		
		genericTemplatesComboBox.setVisible(false);
		customTemplatesComboBox.setVisible(false);
		
		selectedTemplateLabel.setVisible(false);
		switchFormsLaterLabel.setVisible(false);
		
		getWizardNavigationHandler().getNextButton().addEventHandler(ActionEvent.ACTION, new NextButtonHandler());
	} 
	
	private ObservableList<ChoiceItem> getImportTemplateChoices()
	{
		Vector<ChoiceItem> choices = new Vector<ChoiceItem>();
		choices.add(new ChoiceItem(IMPORT_FROM_CONTACTS_CODE, "Import from My Contacts"));
		choices.add(new ChoiceItem(IMPORT_FROM_NEW_CONTACT_CODE, "Import from New Contact"));
		
		return FXCollections.observableArrayList(choices);
	}

	private ObservableList<CustomFieldTemplate> getGenericTemplateChoices()
	{
		try
		{
			Vector<CustomFieldTemplate> customTemplates = loadCustomTemplates();

			return FXCollections.observableArrayList(customTemplates);
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
			return FXCollections.observableArrayList();
		}
	}
	
	private Vector<CustomFieldTemplate> loadCustomTemplates() throws Exception
	{
		Vector<CustomFieldTemplate> customTemplates = new Vector<CustomFieldTemplate>();
		File accountsDirs = getApp().getMartusDataRootDirectory();
		File[] customTemplateFiles = accountsDirs.listFiles(new MCTFileFilter(getLocalization()));
		for (File customTemplateFile : customTemplateFiles)
		{
			if (customTemplateFile.isDirectory())
				continue;
			
			CustomFieldTemplate customTemplate = new CustomFieldTemplate();
			customTemplate.importTemplate(getApp().getSecurity(), customTemplateFile);
			customTemplates.add(customTemplate);
		}
		
		return customTemplates;
	}
	
	@FXML
	private void genericComboBoxSelectionChanged() throws Exception
	{
		CustomFieldTemplate genericCustomFieldTemplate = genericTemplatesComboBox.getSelectionModel().getSelectedItem();
		if (genericCustomFieldTemplate == null)
			return;
		
		updateSelectedCustomFieldTemplateComponents(genericCustomFieldTemplate);
	}

	@FXML
	private void customDropDownSelectionChanged() throws Exception
	{
		String selectedCode = customTemplatesComboBox.getSelectionModel().getSelectedItem().getCode();
		if (selectedCode.equals(IMPORT_FROM_CONTACTS_CODE))
			importFromContacts();
		
		if (selectedCode.equals(IMPORT_FROM_NEW_CONTACT_CODE))
			importFromNewContact();
	}
	
	@FXML
	private void radioButtonSelectionChanged()
	{
		genericTemplatesComboBox.setVisible(false);
		customTemplatesComboBox.setVisible(false);
		if (genericRadioButton.isSelected())
			genericTemplatesComboBox.setVisible(true);
		
		if (downloadCustomRadioButton.isSelected())
			customTemplatesComboBox.setVisible(true);
	}
	
	private void importFromNewContact() throws Exception
	{
		FxSetupTemplateFromNewContactPopupController controller = new FxSetupTemplateFromNewContactPopupController(getMainWindow());
		showControllerInsideModalDialog(controller);
		CustomFieldTemplate selectedTemplate = controller.getSelectedCustomFieldTemplate();
		updateSelectedCustomFieldTemplateComponents(selectedTemplate);
	}

	private void importFromContacts() throws Exception
	{
		FxImportTemplateFromMyContactsPopupController controller = new FxImportTemplateFromMyContactsPopupController(getMainWindow());
		showControllerInsideModalDialog(controller);
		CustomFieldTemplate selectedTemplate = controller.getSelectedFormTemplate();
		updateSelectedCustomFieldTemplateComponents(selectedTemplate);
	}
	
	private void updateSelectedCustomFieldTemplateComponents(CustomFieldTemplate customFieldTemplate) throws Exception
	{
		if (customFieldTemplate == null)
			return;
		
		selectedTemplateLabel.setText(TokenReplacement.replaceToken(">Import the #templateName Form", "#templateName", customFieldTemplate.getTitle()));
		selectedTemplateLabel.setVisible(true);
		switchFormsLaterLabel.setVisible(true);
	}
	
	private void saveCustomFieldTemplate(CustomFieldTemplate customFieldTemplate)
	{
		try
		{
			getApp().updateCustomFieldTemplate(customFieldTemplate);
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}
	
	private class NextButtonHandler implements EventHandler<ActionEvent>
	{
		public NextButtonHandler()
		{
		}

		@Override
		public void handle(ActionEvent event)
		{
			if (genericRadioButton.isSelected())
			{
				CustomFieldTemplate genericCustomFieldTemplate = genericTemplatesComboBox.getSelectionModel().getSelectedItem();
				saveCustomFieldTemplate(genericCustomFieldTemplate);
			}
		}
	}
	
	@FXML
	private ComboBox<CustomFieldTemplate> genericTemplatesComboBox;
	
	@FXML
	private ComboBox<ChoiceItem> customTemplatesComboBox;
	
	@FXML
	private RadioButton genericRadioButton;
	
	@FXML
	private RadioButton downloadCustomRadioButton;
	
	@FXML
	private Label switchFormsLaterLabel;
	
	@FXML
	private Label selectedTemplateLabel;
	
	private static final String IMPORT_FROM_CONTACTS_CODE = "importFromContacts";
	private static final String IMPORT_FROM_NEW_CONTACT_CODE = "importFromNewContact";
}
