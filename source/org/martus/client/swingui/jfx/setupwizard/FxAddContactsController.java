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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;

import org.martus.client.core.MartusApp;
import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.FxController;
import org.martus.client.swingui.jfx.FxInSwingDialogStage;
import org.martus.client.swingui.jfx.FxPopupController;
import org.martus.common.ContactKey;
import org.martus.common.ContactKeys;
import org.martus.common.Exceptions.ServerNotAvailableException;
import org.martus.common.MartusAccountAccessToken;
import org.martus.common.MartusAccountAccessToken.TokenNotFoundException;
import org.martus.common.MartusLogger;
import org.martus.common.crypto.MartusSecurity;

public class FxAddContactsController extends AbstractFxSetupWizardContentController implements Initializable
{
	public FxAddContactsController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		publicCodeColumn.setEditable(false);
		
		contactNameColumn.setCellValueFactory(new PropertyValueFactory<ContactsTableData, String>("contactName"));
		publicCodeColumn.setCellValueFactory(new PropertyValueFactory<ContactsTableData, String>("publicCode"));
		canSendToColumn.setCellValueFactory(new PropertyValueFactory<ContactsTableData, Boolean>("canSendTo"));
		canReceiveFromColumn.setCellValueFactory(new PropertyValueFactory<ContactsTableData, Boolean>("canReceiveFrom"));
		removeContactColumn.setCellValueFactory(new PropertyValueFactory<ContactsTableData, String>("deleteContact")); 

		contactNameColumn.setCellFactory(TextFieldTableCell.<ContactsTableData>forTableColumn());
		publicCodeColumn.setCellFactory(TextFieldTableCell.<ContactsTableData>forTableColumn());
		canSendToColumn.setCellFactory(CheckBoxTableCell.<ContactsTableData>forTableColumn(canSendToColumn));
		canReceiveFromColumn.setCellFactory(CheckBoxTableCell.<ContactsTableData>forTableColumn(canReceiveFromColumn));

	      Callback<TableColumn<ContactsTableData, String>, TableCell<ContactsTableData, String>> deleteColumnCellFactory = 
	                createRemoveButtonCallback();

	    removeContactColumn.setCellFactory(deleteColumnCellFactory);
		contactsTable.setItems(data);
		loadExistingContactData();
		updateAddContactButtonState();
		accessTokenField.textProperty().addListener(new AccessTokenChangeHandler());
	}

	private Callback<TableColumn<ContactsTableData, String>, TableCell<ContactsTableData, String>> createRemoveButtonCallback()
	{
		return new TableButtonCallbackHandler();
	}
	
	protected void removeContactFromTable(ContactsTableData contactData)
	{
		data.remove(contactData);
	}

	protected ContactsTableData getSelectedContact()
	{
		return contactsTable.getSelectionModel().getSelectedItem();
	}
	
	@FXML
	public void addContact() 
	{
		try
		{
			MartusAccountAccessToken token = new MartusAccountAccessToken(accessTokenField.getText());
			MartusApp app = getApp();
			String contactAccountId = app.getMartusAccountIdFromAccessTokenOnServer(token);
			if(contactAccountId.equals(app.getAccountId()))
			{
				showNotifyDlg("ContactKeyIsOurself");
				return;
			}
			String contactPublicCode = MartusSecurity.computeFormattedPublicCode(contactAccountId);
			if(DoesContactAlreadyExistInTable(contactPublicCode))
			{
				showNotifyDlg("ContactKeyAlreadyExists");
				return;
			}
			showAndAddContactsDialog(contactAccountId);
		} 
		catch (ServerNotAvailableException e)
		{
			showNotifyDlg("ContactsNoServer");
		} 
		catch (TokenNotFoundException e)
		{
			showNotifyDlg("UnableToRetrieveContactFromServer");
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			showNotifyDlg("UnexpectedError");
		} 
	}

	private boolean DoesContactAlreadyExistInTable(String contactPublicCode)
	{
		for(int i =0; i < data.size(); ++i)
		{
			ContactsTableData contactData = data.get(i);
			if(contactData.getPublicCode().equals(contactPublicCode))
			return true;
		}
		return false;
	}
	
	private void showAndAddContactsDialog(String contactAccountId)
	{
		try
		{
			ContactKey newContact = new ContactKey(contactAccountId);
			PopupController popupController = new PopupController(getMainWindow(), newContact.getPublicCode());
			showControllerInsideModalDialog(popupController);
			if(popupController.hasContactBeenAccepted())
			{
				int verification = popupController.getVerification();
				newContact.setVerificationStatus(verification);
				ContactsTableData contactData = new ContactsTableData(newContact); 
				data.add(contactData);
				clearAccessTokenField();
			}
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}

	private void clearAccessTokenField()
	{
		accessTokenField.setText("");
	}
	
	final class TableButtonCallbackHandler implements Callback<TableColumn<ContactsTableData, String>, TableCell<ContactsTableData, String>>
	{
	final class ButtonCellUpdateHandler extends TableCell
		{
			private final TableColumn param;
			ButtonCellUpdateHandler(TableColumn param)
			{
				this.param = param;
			}
			@Override
			public void updateItem(Object item, boolean empty) 
			{
			    super.updateItem(item, empty);
			    if (empty) 
			    {
			        setText(null);
			        setGraphic(null);
			    } 
			    else 
			    {
			        final Button removeContactButton = new Button((String)item);
			        removeContactButton.setStyle("-fx-base: red;");
			        removeContactButton.setOnAction
			        (new EventHandler<ActionEvent>() 
			        		{
			        			@Override
			        			public void handle(ActionEvent event) 
			        			{
			        				param.getTableView().getSelectionModel().select(getIndex());
			        				ContactsTableData contactData = getSelectedContact();
			        				removeContactFromTable(contactData);
			        			}

			        		}
			        );
			        setGraphic(removeContactButton);
			    	}
				}
		}

		@Override
		 public TableCell call(final TableColumn param) 
		 {
			 	final TableCell cell = new ButtonCellUpdateHandler(param);
			 	return cell;
		 }
	}

	public static class PopupController extends FxPopupController implements Initializable
	{
		public PopupController(UiMainWindow mainWindowToUse, String contactPublicCodeToUse)
		{
			super(mainWindowToUse);
			contactPublicCode = contactPublicCodeToUse;
			verification=ContactKey.NOT_VERIFIED;
			contactAccepted = false;
		}
		
		@Override
		public void initialize(URL arg0, ResourceBundle arg1)
		{
			contactPublicCodeLabel.setText(contactPublicCode);
		}
		
		@Override
		public String getFxmlLocation()
		{
			return "setupwizard/SetupAddContactPopup.fxml";
		}

		@Override
		public String getDialogTitle()
		{
			return getLocalization().getWindowTitle("notifyAddContact"); 
		}

		@FXML
		public void cancelVerify()
		{
			getStage().close();
		}

		@FXML
		public void verifyContact()
		{
			verification=ContactKey.VERIFIED_VISUALLY;
			contactAccepted = true;
			getStage().close();
		}

		@FXML
		private Label contactPublicCodeLabel;
		
		public int getVerification()
		{
			return verification;
		}
		
		public boolean hasContactBeenAccepted()
		{
			return contactAccepted;
		}
		
		public void setFxStage(FxInSwingDialogStage stageToUse)
		{
			fxStage = stageToUse;
		}

		public FxInSwingDialogStage getFxStage()
		{
			return fxStage;
		}

		private String contactPublicCode;
		private FxInSwingDialogStage fxStage;
		private int verification;
		private boolean contactAccepted;


	}
	
	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/SetupAddContacts.fxml";
	}
	
	@Override
	public FxController getNextControllerClassName()
	{
		return new FxSetupImportTemplatesController(getMainWindow());
	}
	
	public void nextWasPressed(ActionEvent actionEvent)
	{
		SaveContacts();
		super.nextWasPressed(actionEvent);
	}
	
	public void backWasPressed(ActionEvent actionEvent)
	{
		SaveContacts();
		super.backWasPressed(actionEvent);
	}
	
	public void SaveContacts()
	{
		ContactKeys allContactsInTable = new ContactKeys();
		for(int i =0; i < data.size(); ++i)
		{
			allContactsInTable.add(data.get(i).getContact());
		}
		try
		{
			getApp().setContactKeys(allContactsInTable);
		} 
		catch (SaveConfigInfoException e)
		{
			MartusLogger.logException(e);
		}
	}
	
	
	protected class AccessTokenChangeHandler implements ChangeListener<String>
	{
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
		{
			updateAddContactButtonState();
		}

	}
	
	protected void updateAddContactButtonState()
	{
		String candidateToken = accessTokenField.getText();
		boolean canAdd = (isValidAccessToken(candidateToken));

		Button nextButton = getWizardNavigationHandler().getNextButton();
		if(candidateToken.length() == 0)
		{
			addContactButton.setDefaultButton(false);
			nextButton.setDefaultButton(true);
		}
		else if(canAdd)
		{
			nextButton.setDefaultButton(false);
			addContactButton.setDefaultButton(true);
		}
		else
		{
			nextButton.setDefaultButton(false);
			addContactButton.setDefaultButton(true);
		}

		addContactButton.setDisable(!canAdd);
	}

	private boolean isValidAccessToken(String tokenToValidate)
	{
		if(tokenToValidate.length() == 0)
			return false;
		
		return MartusAccountAccessToken.isTokenValid(tokenToValidate);
	}
	
	private void loadExistingContactData()
	{
		data.clear();
		
		try
		{
			ContactKeys keys = getApp().getContactKeys();
			for(int i = 0; i < keys.size(); ++i)
			{
				ContactKey contact = keys.get(i);
				ContactsTableData contactData = new ContactsTableData(contact); 
				data.add(contactData);
			}
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
		
	}

	@FXML 
	private TableView<ContactsTableData> contactsTable;
	
	@FXML
	private TableColumn<ContactsTableData, String> contactNameColumn;
	
	@FXML
	private TableColumn<ContactsTableData, String> publicCodeColumn;
	
	@FXML
	private TableColumn<ContactsTableData, Boolean> canSendToColumn;

	@FXML
	private TableColumn<ContactsTableData, Boolean> canReceiveFromColumn;

	@FXML
	private TableColumn<ContactsTableData, String> removeContactColumn;
	
	@FXML
	private TextField accessTokenField;
	
	@FXML
	private Button addContactButton;
	
	private ObservableList<ContactsTableData> data = FXCollections.observableArrayList();
}
