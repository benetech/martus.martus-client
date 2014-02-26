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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Toggle;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.FxPopupController;
import org.martus.common.ContactKey;
import org.martus.common.ContactKeys;
import org.martus.common.MartusLogger;
import org.martus.common.fieldspec.ChoiceItem;

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
			contactsWithTemplatesTableView.setEditable(true);
			fillTableWithContacts();
		
			contactSelectedColumn.setCellValueFactory(new PropertyValueFactory<ContactsWithTemplatesTableData, Boolean>("rowSelected"));
			contactSelectedColumn.setCellFactory(new RadioButtonCellFactory(new ToggleChangeListener(contactsWithTemplatesTableView)));
			
			contactNameColumn.setCellValueFactory(new PropertyValueFactory<ContactsWithTemplatesTableData, String>("contactName"));
			contactNameColumn.setCellFactory(TextFieldTableCell.<ContactsWithTemplatesTableData>forTableColumn());
			
			publicCodeColumn.setCellValueFactory(new PropertyValueFactory<ContactsWithTemplatesTableData, String>("publicCode"));
			publicCodeColumn.setCellFactory(TextFieldTableCell.<ContactsWithTemplatesTableData>forTableColumn());
			
			selectedTemplateColumn.setCellValueFactory(new PropertyValueFactory<ContactsWithTemplatesTableData, ChoiceItem>("selectedTemplateName"));
			selectedTemplateColumn.setCellFactory(ComboBoxTableCell.<ContactsWithTemplatesTableData, ChoiceItem>forTableColumn());
			
			contactsWithTemplatesTableView.setItems(data);
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}

	private void fillTableWithContacts() throws Exception
	{
		ObservableList<ChoiceItem> observableList = FXCollections.observableArrayList();
		observableList.add(new ChoiceItem("", "Choose One..."));
		observableList.add(new ChoiceItem("NewItem1", "New Item1"));
		observableList.add(new ChoiceItem("NewItem2", "New Item2"));

		data = FXCollections.observableArrayList();
		ContactKeys contactKey = getApp().getContactKeys();
		for (int index = 0; index < contactKey.size(); ++index)
		{
			ContactKey key = contactKey.get(index);
			ContactsWithTemplatesTableData e = new ContactsWithTemplatesTableData(key.getLabel(), key.getPublicCode(), true, observableList);
			data.add(e);
		}
		
		System.out.println("data size =" + data.size());
	}
	
	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/SetupImportTemplateFromMyContactsPopup.fxml";
	}
	
	private static class ToggleChangeListener implements ChangeListener<Toggle>
	{
		public ToggleChangeListener(TableView<ContactsWithTemplatesTableData> contactsWithTemplatesTableViewToUse)
		{
			contactsWithTemplatesTableView = contactsWithTemplatesTableViewToUse;
		}

		@Override
		public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue)
		{
			if (contactsWithTemplatesTableView.getSelectionModel().isEmpty())
				return;
			
			ContactsWithTemplatesTableData dataForRow = contactsWithTemplatesTableView.getSelectionModel().getSelectedItem();
			dataForRow.setSelectedTemplateName(getAllTemplatesForContact());
		}
		
		private ObservableList<ChoiceItem> getAllTemplatesForContact()
		{
			ObservableList<ChoiceItem> observableArrayList = FXCollections.observableArrayList();
			//ADD contact templates here.  

			return 	observableArrayList;
		}

		private TableView<ContactsWithTemplatesTableData> contactsWithTemplatesTableView;
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
	private TableColumn<ContactsWithTemplatesTableData, ChoiceItem> selectedTemplateColumn;
	
	private ObservableList<ContactsWithTemplatesTableData> data;

}
