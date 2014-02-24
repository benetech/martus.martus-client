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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.FxController;
import org.martus.client.swingui.jfx.FxStage;
import org.martus.common.FieldDeskKey;
import org.martus.common.FieldDeskKeys;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MartusLogger;
import org.martus.common.fieldspec.ChoiceItem;

//FIXME this class is under development and is not tied to the wizard 
public class FxSetupImportTemplatesController extends AbstractFxSetupWizardController implements Initializable
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
		return new FxSetupUsernamePasswordController(getMainWindow());
	}

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		genericTemplatesComboBox.setItems(FXCollections.observableArrayList(getDateFormatChoices()));
		customTemplatesComboBox.setItems(FXCollections.observableArrayList(getCustomDropdownChoices()));
	} 
	
	private ObservableList<ChoiceItem> getCustomDropdownChoices()
	{
		Vector<ChoiceItem> choices = new Vector<ChoiceItem>();
		choices.add(new ChoiceItem(IMPORTF_FROM_CONTACTS_CODE, "Import from My Contacts"));
		choices.add(new ChoiceItem(IMPORT_FROM_NEW_CONTACT_CODE, "Import from New Contact"));
		
		return FXCollections.observableArrayList(choices);
	}

	private ObservableList<ChoiceItem> getDateFormatChoices()
	{
		Vector<ChoiceItem> choices = new Vector<ChoiceItem>();
		choices.add(new ChoiceItem("template1", "Template 1"));
		choices.add(new ChoiceItem("template2", "Template 2"));
		choices.add(new ChoiceItem("template3", "Template 3"));

		return FXCollections.observableArrayList(choices);
	}
	
	@FXML
	private void customDropDownSelectionChanged() throws Exception
	{
		String selectedCode = customTemplatesComboBox.getSelectionModel().getSelectedItem().getCode();
		if (selectedCode.equals(IMPORTF_FROM_CONTACTS_CODE));
			importFromContacts();
		
		if (selectedCode.equals(IMPORT_FROM_NEW_CONTACT_CODE))
			importFromNewContact();
	}
	
	private void importFromNewContact() throws Exception
	{
		showAddContactsDialog();
	}

	private void importFromContacts() throws Exception
	{
		showAddContactsDialog();
	}
	
	private void showAddContactsDialog() throws Exception
	{
		Stage popupStage = new Stage();
		popupStage.setTitle("Import Template");
		popupStage.initModality(Modality.WINDOW_MODAL);

		FXMLLoader fl = new FXMLLoader();
		ImportTemplatesFromMyContactsController popupController = new ImportTemplatesFromMyContactsController(getMainWindow(), popupStage);
		fl.setController(popupController);
		fl.setLocation(FxStage.class.getResource(popupController.getFxmlLocation()));
		fl.load();
		Parent root = fl.getRoot();

		Scene scene = new Scene(root);
		popupStage.setScene(scene);
	    popupStage.showAndWait();
	}
	
	private static class ImportTemplatesFromMyContactsController extends FxController implements Initializable
	{
		public ImportTemplatesFromMyContactsController(UiMainWindow mainWindowToUse, Stage popupStage) throws Exception
		{
			super(mainWindowToUse);

			ourStage = popupStage;
		}
		
		@Override
		public void initialize(URL arg0, ResourceBundle arg1)
		{
			try
			{
				contactsWithTemplatesTableView.setEditable(true);
				//getApp().attemptSignIn("c", "cccccccc".toCharArray());
				//data.add(new ContactsWithTemplatesTableData("ABC", "1234", true, "dropDown"));
				HeadquartersKeys headquarterKeys = getApp().getAllHQKeys();
				for (int index = 0; index < headquarterKeys.size(); ++index)
				{
					HeadquartersKey key = headquarterKeys.get(index);
					data.add(new ContactsWithTemplatesTableData(key.getLabel(), key.getPublicCode(), false, ""));
				}
				
				FieldDeskKeys fieldDeskKeys = getApp().getFieldDeskKeys();
				for (int index = 0; index < fieldDeskKeys.size(); ++index)
				{
					FieldDeskKey key = fieldDeskKeys.get(index);
					data.add(new ContactsWithTemplatesTableData(key.getLabel(), key.getPublicCode(), false, ""));
				}
				
				Callback<TableColumn<ContactsWithTemplatesTableData, Boolean>, TableCell<ContactsWithTemplatesTableData, Boolean>> radioButtonCellFactory = 
		                new Callback<TableColumn<ContactsWithTemplatesTableData, Boolean>, TableCell<ContactsWithTemplatesTableData, Boolean>>() {

		            @Override
		            public TableCell call(final TableColumn param) 
		            {
		                final TableCell cell = new TableCell() 
		                {
		                    @Override
		                    public void updateItem(Object item, boolean empty) 
		                    {
		                    	if (item != null)
		                    	{
		                    		//NOT GETTING CALLED
		                    		RadioButton choice = new RadioButton();                                                     
		                    		setGraphic(choice);
		                    	}
		                    }
		                };

		                return cell;
		            }
		        };
		        
				System.out.println("data size =" + data.size());
				
				contactNameColumn.setCellValueFactory(new PropertyValueFactory<ContactsWithTemplatesTableData, String>("contactName"));
				publicCodeColumn.setCellValueFactory(new PropertyValueFactory<ContactsWithTemplatesTableData, String>("publicCode"));
				
				contactSelectedColumn.setCellValueFactory(new PropertyValueFactory<ContactsWithTemplatesTableData, Boolean>("rowSelected"));
				contactSelectedColumn.setCellFactory(radioButtonCellFactory);

				contactNameColumn.setCellFactory(TextFieldTableCell.<ContactsWithTemplatesTableData>forTableColumn());
				publicCodeColumn.setCellFactory(TextFieldTableCell.<ContactsWithTemplatesTableData>forTableColumn());

				contactsWithTemplatesTableView.setItems(data);
			}
			catch (Exception e)
			{
				MartusLogger.logException(e);
			}
		}
		
		@Override
		public String getFxmlLocation()
		{
			return "setupwizard/SetupImportTemplateFromMyContactsPopup.fxml";
		}

		private Stage ourStage;

		@FXML
		private TableView<ContactsWithTemplatesTableData> contactsWithTemplatesTableView;
		
		@FXML
		private TableColumn<ContactsWithTemplatesTableData, String> contactNameColumn;
		
		@FXML
		private TableColumn<ContactsWithTemplatesTableData, String> publicCodeColumn;
		
		@FXML
		private TableColumn<ContactsWithTemplatesTableData, Boolean> contactSelectedColumn;
		
		private ObservableList<ContactsWithTemplatesTableData> data = FXCollections.observableArrayList();
	}

	@FXML
	private ComboBox<ChoiceItem> genericTemplatesComboBox;
	
	@FXML
	private ComboBox<ChoiceItem> customTemplatesComboBox;
	
	@FXML
	private RadioButton genericRadioButton;
	
	@FXML
	private RadioButton downloadCustomRadioButton;
	
	private static final String IMPORTF_FROM_CONTACTS_CODE = "importFromContacts";
	private static final String IMPORT_FROM_NEW_CONTACT_CODE = "importFromNewContact";
}
