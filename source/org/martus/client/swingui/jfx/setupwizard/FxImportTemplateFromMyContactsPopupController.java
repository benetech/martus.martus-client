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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Toggle;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.FxPopupController;
import org.martus.common.ContactKey;
import org.martus.common.ContactKeys;
import org.martus.common.Exceptions.ServerNotAvailableException;
import org.martus.common.MartusLogger;
import org.martus.common.fieldspec.CustomFieldTemplate;

public class FxImportTemplateFromMyContactsPopupController extends FxPopupController implements Initializable
{
	public FxImportTemplateFromMyContactsPopupController(UiMainWindow mainWindowToUse) throws Exception
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
			contactSelectedColumn.setCellFactory(new RadioButtonCellFactory());
			
			contactNameColumn.setCellValueFactory(new PropertyValueFactory<ContactsWithTemplatesTableData, String>("contactName"));
			contactNameColumn.setCellFactory(TextFieldTableCell.<ContactsWithTemplatesTableData>forTableColumn());
			
			publicCodeColumn.setCellValueFactory(new PropertyValueFactory<ContactsWithTemplatesTableData, String>("publicCode"));
			publicCodeColumn.setCellFactory(TextFieldTableCell.<ContactsWithTemplatesTableData>forTableColumn());
			
			selectedTemplateColumn.setCellValueFactory(new PropertyValueFactory<ContactsWithTemplatesTableData, CustomFieldTemplate>("selectedTemplateName"));
			selectedTemplateColumn.setCellFactory(new TemplateComboBoxTableCellFactory());
			
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
 			ObservableList<CustomFieldTemplate> observableArrayList = getCustomTemplatesForContact(contactKey);
			ContactsWithTemplatesTableData rowData = new ContactsWithTemplatesTableData(contactKey, true, new CustomFieldTemplate(), observableArrayList);
			contactsWithTemplatesTableData.add(rowData);
		}
	}

	private ObservableList<CustomFieldTemplate> getCustomTemplatesForContact(ContactKey contactKey) throws Exception
	{
		try
		{
			//NOTE: Server should return a different error if contact not found
			ObservableList<CustomFieldTemplate> customFieldTemplates = getCustomFieldTemplates(contactKey);
			//System.out.println(contactKey.getPublicCode() + "-Connected-" + customFieldTemplates.size());
			return customFieldTemplates;
		}
		catch (ServerNotAvailableException e)
		{
			MartusLogger.logError("Contact not found on server");
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
	
	private ObservableList<CustomFieldTemplate> getCustomFieldTemplates(ContactKey contactKey) throws Exception
	{
		Vector<Vector<String>> result = getApp().getListOfFormTemplatesOnServer(contactKey.getPublicKey());

		return getTitlesFromResults(contactKey.getPublicKey(), result);
	}
	
	private ObservableList<CustomFieldTemplate> getTitlesFromResults(String publicKey, Vector<Vector<String>> formTemplatesTitlesDescriptionsList) throws Exception
	{
		ObservableList<CustomFieldTemplate> customFieldTemplates = FXCollections.observableArrayList();
		for (Vector<String> titleAndDescriptonVector : formTemplatesTitlesDescriptionsList)
		{
			String title = titleAndDescriptonVector.firstElement();
			customFieldTemplates.add(getApp().getFormTemplateOnServer(publicKey, title));
		}
		
		return customFieldTemplates;
	}
	
	public CustomFieldTemplate getSelectedCustomFieldTemplate()
	{
		ContactsWithTemplatesTableData selectedRowItem = contactsWithTemplatesTableView.getSelectionModel().getSelectedItem();
		if (selectedRowItem.getRowSelected())
			return selectedRowItem.getSelectedTemplateName();
		
		return null;
	}

	private class TemplateComboBoxTableCellFactory implements Callback<TableColumn<ContactsWithTemplatesTableData, CustomFieldTemplate>, TableCell<ContactsWithTemplatesTableData, CustomFieldTemplate>>
	{
		public TemplateComboBoxTableCellFactory()
		{
		}

		@Override
		public TableCell<ContactsWithTemplatesTableData, CustomFieldTemplate> call(TableColumn<ContactsWithTemplatesTableData, CustomFieldTemplate> param)
		{
			return new TemplateComboBoxCell();
		}
	}
	
	private class TemplateComboBoxCell extends ComboBoxTableCell<ContactsWithTemplatesTableData, CustomFieldTemplate>
	{
		public TemplateComboBoxCell()
		{
			super(new CustomFieldTemplateToStringConverter());
		}
		
		@Override
		public void updateItem(CustomFieldTemplate item, boolean empty)
		{
			super.updateItem(item, empty);
			
			final TableRow tableRow = getTableRow(); 
			ContactsWithTemplatesTableData data = (ContactsWithTemplatesTableData) tableRow.getItem();
			if (data == null)
				return;
			
			getItems().clear();
			ObservableList<CustomFieldTemplate> customFieldTemplateChoices = data.getCustomFieldTemplateChoices();
			getItems().addAll(customFieldTemplateChoices);
		}
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
	private TableColumn<ContactsWithTemplatesTableData, CustomFieldTemplate> selectedTemplateColumn;
	
	@FXML
	private Label continueMessage;
	
	@FXML
	private Button continueButton;
	
	private ObservableList<ContactsWithTemplatesTableData> contactsWithTemplatesTableData;
}
