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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.FxController;
import org.martus.client.swingui.jfx.FxStage;
import org.martus.common.ContactKey;
import org.martus.common.ContactKeys;
import org.martus.common.Exceptions.ServerNotAvailableException;
import org.martus.common.MartusAccountAccessToken;
import org.martus.common.MartusAccountAccessToken.TokenInvalidException;
import org.martus.common.MartusAccountAccessToken.TokenNotFoundException;
import org.martus.common.MartusLogger;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.crypto.MartusSecurity;
import org.martus.util.StreamableBase64.InvalidBase64Exception;

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
		
		contactNameColumn.setCellFactory(TextFieldTableCell.<ContactsTableData>forTableColumn());
		publicCodeColumn.setCellFactory(TextFieldTableCell.<ContactsTableData>forTableColumn());
		
		contactsTable.setItems(data);
		loadExistingContactData();

		updateAddContactButtonState();
		
		accessTokenField.textProperty().addListener(new AccessTokenChangeHandler());
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
			showAddContactsDialog(contactAccountId);
		} 
		catch (TokenInvalidException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (TokenNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (MartusSignatureException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (ServerNotAvailableException e)
		{
			showNotifyDlg("ContactsNoServer");
		}
		
	}

	private void showAddContactsDialog(String contactAccountId)
	{
		try
		{
			PopupController popupController = new PopupController(getMainWindow(), contactAccountId);
			showControllerInsideModalDialog(popupController, "AddContact");
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}
	
	public static class PopupController extends FxController implements Initializable
	{
		public PopupController(UiMainWindow mainWindowToUse, String contactAccountIdToUse)
		{
			super(mainWindowToUse);
			contactAccountId = contactAccountIdToUse;
		}
		
		@Override
		public void initialize(URL arg0, ResourceBundle arg1)
		{
			try
			{
				String contactPublicCode = MartusSecurity.computeFormattedPublicCode(contactAccountId);
				contactPublicCodeLabel.setText(contactPublicCode);
			} 
			catch (InvalidBase64Exception e)
			{
				MartusLogger.logException(e);
				System.exit(1);
			}
		}
		
		@Override
		public String getFxmlLocation()
		{
			return "setupwizard/SetupAddContactPopup.fxml";
		}

		@FXML
		public void cancelVerify()
		{
			getStage().close();
		}

		@FXML
		public void verifyContact()
		{
			//FIXME do real work here.
			getStage().close();
		}

		@FXML
		private Label contactPublicCodeLabel;
		
		private String contactAccountId;
	}
	
	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/SetupAddContacts.fxml";
	}
	
	@Override
	public FxController getNextControllerClassName()
	{
		return new FxSetupImportFormTemplates(getMainWindow());
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
				String name = contact.getLabel();
				String publicCode = contact.getPublicCode();
				boolean canSendTo = contact.getCanSendTo();
				boolean canReceiveFrom = contact.getCanReceiveFrom();
				ContactsTableData contactData = new ContactsTableData(name, publicCode, canSendTo, canReceiveFrom); 
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
	private TextField accessTokenField;
	
	@FXML
	private Button addContactButton;
	
	private ObservableList<ContactsTableData> data = FXCollections.observableArrayList();
}
