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

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.Toggle;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.FxBindingHelpers;
import org.martus.client.swingui.jfx.FxRadioButtonCellFactory;
import org.martus.client.swingui.jfx.setupwizard.ContactsWithTemplatesTableData;
import org.martus.common.ContactKey;
import org.martus.common.ContactKeys;
import org.martus.common.Exceptions.AccountNotFoundException;
import org.martus.common.Exceptions.ServerNotAvailableException;
import org.martus.common.MartusLogger;
import org.martus.common.fieldspec.CustomFieldTemplate;
import org.martus.util.TokenReplacement;

public class NonWorkingFxImportFormTemplateFromMyContactsPopupController extends AbstractFxImportFormTemplateController implements Initializable
{
	public NonWorkingFxImportFormTemplateFromMyContactsPopupController(UiMainWindow mainWindowToUse)
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
		
			contactSelectedColumn.setCellValueFactory(new PropertyValueFactory<ContactsWithTemplatesTableData, Boolean>("isContactChosen"));
			FxRadioButtonCellFactory value = new FxRadioButtonCellFactory();
			value.selectedToggleProperty().addListener(new ToggleChangeListener());
			contactSelectedColumn.setCellFactory(value);
			
			contactNameColumn.setCellValueFactory(new PropertyValueFactory<ContactsWithTemplatesTableData, String>("contactName"));
			contactNameColumn.setCellFactory(TextFieldTableCell.<ContactsWithTemplatesTableData>forTableColumn());
			contactNameColumn.setEditable(false);
			
			publicCodeColumn.setCellValueFactory(new PropertyValueFactory<ContactsWithTemplatesTableData, String>("publicCode"));
			publicCodeColumn.setCellFactory(TextFieldTableCell.<ContactsWithTemplatesTableData>forTableColumn());
			publicCodeColumn.setEditable(false);
			
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
			rowData.isContactChosenProperty().addListener(new FillComboBoxHandler(rowData));
			contactsWithTemplatesTableData.add(rowData);
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
		
	public CustomFieldTemplate getSelectedFormTemplate()
	{
		ObservableList<ContactsWithTemplatesTableData> tableData = contactsWithTemplatesTableView.getItems();
		for (ContactsWithTemplatesTableData rowData : tableData)
		{
			if (rowData.getIsContactChosen())
				return rowData.getSelectedFormTemplate();
		}
		
		return null;
	}
	
	@Override
	public String getLabel()
	{
		return "Import from My Contacts";
	}
	
	protected class ComboBoxEventHandler implements EventHandler<ActionEvent>
	{
		@Override
		public void handle(ActionEvent event)
		{
			boolean shouldBeVisible = true;
			continueMessage.setVisible(shouldBeVisible);
			continueButton.setVisible(shouldBeVisible);
		}
	}
	
	protected class ToggleChangeListener implements ChangeListener<Toggle>
	{
		@Override
		public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue)
		{
			continueMessage.setVisible(false);
			continueButton.setVisible(false);
		}
	}
	
	protected class FormTemplateComboBoxCellFactory implements Callback<TableColumn<ContactsWithTemplatesTableData,CustomFieldTemplate>, TableCell<ContactsWithTemplatesTableData,CustomFieldTemplate>>
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
            comboBox.addEventHandler(ActionEvent.ACTION, new ComboBoxEventHandler());
            comboBox.setPromptText("Choose one...");
            comboBox.setVisible(false);
            comboBox.setPrefWidth(250.0);
        }
        
        @Override
        protected void updateItem(CustomFieldTemplate item, boolean empty)
        {
        	super.updateItem(item, empty);

        	try
        	{
        		if (empty) 
        		{
        			setText(null);
        			setGraphic(null);
        		}
        		else
        		{
        			ContactsWithTemplatesTableData rowData = (ContactsWithTemplatesTableData) getTableRow().getItem();
        			if (rowData == null)
        				return;

        			bindComboBoxVisibilityToRadioButtonSelection(rowData);
        			comboBox.setItems(rowData.getFormTemplateChoices());
        			setGraphic(comboBox);
        			
        			comboBox.valueProperty().bindBidirectional(rowData.selectedFormTemplateProperty());
        			Property cellProperty = (Property)getTableColumn().getCellObservableValue(getIndex());
        			Property currentFieldProperty = comboBox.valueProperty();
        			cellBooleanPropertyBoundToCurrently = FxBindingHelpers.bindToOurPropertyField(cellProperty, currentFieldProperty, cellBooleanPropertyBoundToCurrently);
        		}
        	} 
        	catch (Exception e)
        	{
        		MartusLogger.logException(e);
        	}
        }

		private void bindComboBoxVisibilityToRadioButtonSelection(ContactsWithTemplatesTableData rowData)
		{
			comboBox.visibleProperty().bindBidirectional(rowData.isContactChosenProperty());
		}

        private ComboBox<CustomFieldTemplate> comboBox;
        private Property cellBooleanPropertyBoundToCurrently;
	}
	
	private class FillComboBoxHandler implements ChangeListener<Boolean>
	{
		public FillComboBoxHandler(ContactsWithTemplatesTableData rowDataToUse)
		{
			rowData = rowDataToUse;
		}

		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
		{
			if (!newValue)
				return;
			
			try
			{
				fillComboBox();
			}
			catch (Exception e)
			{
				MartusLogger.logException(e);
			}
		}

		private void fillComboBox() throws Exception
		{
			try
			{
				ObservableList<CustomFieldTemplate> formTemplateChoices = rowData.getFormTemplateChoices();
				formTemplateChoices.clear();
				getFormTemplates(rowData.getContactKey(), formTemplateChoices);
			} 
			catch (ServerNotAvailableException e)
			{
				MartusLogger.logException(e);
				MartusLogger.logError("Contact not found on server");
			}
			catch (AccountNotFoundException e)
			{
				MartusLogger.logException(e);
				MartusLogger.logError(TokenReplacement.replaceToken("Account not found on server. Account=#account", "#account", rowData.getContactKey().getFormattedPublicCode()));
			}
		}
		
		private ContactsWithTemplatesTableData rowData;
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
	protected Label continueMessage;
	
	@FXML
	protected Button continueButton;
	
	private ObservableList<ContactsWithTemplatesTableData> contactsWithTemplatesTableData;
}
