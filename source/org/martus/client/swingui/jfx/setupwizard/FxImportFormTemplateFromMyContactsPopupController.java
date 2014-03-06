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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;

import org.martus.client.swingui.UiMainWindow;
import org.martus.common.ContactKey;
import org.martus.common.ContactKeys;
import org.martus.common.Exceptions.AccountNotFoundException;
import org.martus.common.Exceptions.ServerNotAvailableException;
import org.martus.common.MartusLogger;
import org.martus.common.fieldspec.CustomFieldTemplate;
import org.martus.util.TokenReplacement;

public class FxImportFormTemplateFromMyContactsPopupController extends AbstractFxImportFormTemplateController implements Initializable
{
	public FxImportFormTemplateFromMyContactsPopupController(UiMainWindow mainWindowToUse) throws Exception
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		try
		{
			continueButton.setVisible(false);
			continueMessage.setVisible(false);
			
			contactsWithTemplatesTableView.setEditable(true);
			fillTableWithContacts();
		
			contactSelectedColumn.setCellValueFactory(new PropertyValueFactory<ContactsWithTemplatesTableData, Boolean>("rowSelected"));
			contactSelectedColumn.setCellFactory(new FxRadioButtonCellFactory());
			
			contactNameColumn.setCellValueFactory(new PropertyValueFactory<ContactsWithTemplatesTableData, String>("contactName"));
			contactNameColumn.setCellFactory(TextFieldTableCell.<ContactsWithTemplatesTableData>forTableColumn());
			
			publicCodeColumn.setCellValueFactory(new PropertyValueFactory<ContactsWithTemplatesTableData, String>("publicCode"));
			publicCodeColumn.setCellFactory(TextFieldTableCell.<ContactsWithTemplatesTableData>forTableColumn());
			
			formTemplateColumn.setCellValueFactory(new PropertyValueFactory<ContactsWithTemplatesTableData, CustomFieldTemplate>("selectedFormTemplate"));
			formTemplateColumn.setCellFactory(new FormTemplateComboBoxCellFactory());
			
			contactsWithTemplatesTableView.setItems(contactsWithTemplatesTableData);
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}

	private void fillTableWithContacts() throws Exception
	{
		contactsWithTemplatesTableData = FXCollections.observableArrayList();
		ContactKeys contactKeys = getApp().getContactKeys();
		for (int index = 0; index < contactKeys.size(); ++index)
		{
			ContactKey contactKey = contactKeys.get(index);
 			ObservableList<CustomFieldTemplate> observableArrayList = FXCollections.observableArrayList();
			ContactsWithTemplatesTableData rowData = new ContactsWithTemplatesTableData(contactKey, false, new CustomFieldTemplate(), observableArrayList);
			contactsWithTemplatesTableData.add(rowData);
		}
	}

	private ObservableList<CustomFieldTemplate> getFormTemplatesForContact(ContactKey contactKey) throws Exception
	{
		try
		{
			//NOTE: Server should return a different error if contact not found
			ObservableList<CustomFieldTemplate> formTemplates = getFormTemplates(contactKey);
			return formTemplates;
		}
		catch (ServerNotAvailableException e)
		{
			MartusLogger.logError("Contact not found on server");
			return FXCollections.observableArrayList();
		}
		catch (AccountNotFoundException e)
		{
			MartusLogger.logError(TokenReplacement.replaceToken("Account not found on server. Account=#account", "#account", contactKey.getPublicCode()));
			return FXCollections.observableArrayList();
		}
	}
	
	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/SetupImportTemplateFromMyContactsPopup.fxml";
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
		
	public CustomFieldTemplate getSelectedFormTemplate()
	{
		ObservableList<ContactsWithTemplatesTableData> tableData = contactsWithTemplatesTableView.getItems();
		for (ContactsWithTemplatesTableData rowData : tableData)
		{
			if (rowData.getRowSelected())
				return rowData.getSelectedFormTemplate();
		}
		
		return null;
	}
	
	private class ComboBoxHandler implements EventHandler<ActionEvent>
	{
		public ComboBoxHandler()
		{
		}

		@Override
		public void handle(ActionEvent event)
		{
			ComboBox<CustomFieldTemplate> comboBox = (ComboBox) event.getSource();
			boolean shouldBeVisible = true;
			if (comboBox.getSelectionModel().isEmpty())
				shouldBeVisible = false;
			
			continueMessage.setVisible(shouldBeVisible);
			continueButton.setVisible(shouldBeVisible);
		}
	}
	
	private class FormTemplateComboBoxCellFactory implements Callback<TableColumn<ContactsWithTemplatesTableData,CustomFieldTemplate>, TableCell<ContactsWithTemplatesTableData,CustomFieldTemplate>>
	{
		@Override
		public TableCell<ContactsWithTemplatesTableData, CustomFieldTemplate> call(TableColumn<ContactsWithTemplatesTableData, CustomFieldTemplate> param)
		{
			return new FormTemplateComboBoxTableCell();
		}
	}
	
	private class FormTemplateComboBoxTableCell extends TableCell<ContactsWithTemplatesTableData, CustomFieldTemplate>
	{
        public FormTemplateComboBoxTableCell()
        {
            comboBox = new ComboBox<CustomFieldTemplate>();
            comboBox.setConverter(new FormTemplateToStringConverter());
            comboBox.addEventHandler(ActionEvent.ACTION, new ComboBoxHandler());
            comboBox.setPromptText("Choose one...");
        }
        
        @Override
        protected void updateItem(CustomFieldTemplate item, boolean empty)
        {
        	super.updateItem(item, empty);

        	try
        	{
            	setGraphic(null);
            	ContactsWithTemplatesTableData rowData = (ContactsWithTemplatesTableData) getTableRow().getItem();
            	if (rowData == null)
            		return;

        		comboBox.getItems().clear();
        		comboBox.setItems(getFormTemplatesForContact(rowData.getPublicKey()));
        		setGraphic(comboBox);
        	} 
        	catch (Exception e)
        	{
        		MartusLogger.logException(e);
        	}
        }

        private ComboBox<CustomFieldTemplate> comboBox;
	}

	@FXML
	private TableView<ContactsWithTemplatesTableData> contactsWithTemplatesTableView;
	
	@FXML
	private TableColumn<ContactsWithTemplatesTableData, String> contactNameColumn;
	
	@FXML
	private TableColumn<ContactsWithTemplatesTableData, String> publicCodeColumn;
	
	@FXML
	private TableColumn<ContactsWithTemplatesTableData, Boolean> contactSelectedColumn;

	@FXML
	private TableColumn<ContactsWithTemplatesTableData, CustomFieldTemplate> formTemplateColumn;
	
	@FXML
	private Label continueMessage;
	
	@FXML
	private Button continueButton;
	
	private ObservableList<ContactsWithTemplatesTableData> contactsWithTemplatesTableData;
}
