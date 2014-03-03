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
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.FxPopupController;
import org.martus.common.Exceptions.ServerNotAvailableException;
import org.martus.common.MartusAccountAccessToken;
import org.martus.common.MartusAccountAccessToken.TokenNotFoundException;
import org.martus.common.MartusLogger;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.fieldspec.CustomFieldTemplate;
import org.martus.util.TokenReplacement;

public class FxSetupTemplateFromNewContactPopupController extends FxPopupController implements Initializable
{
	public FxSetupTemplateFromNewContactPopupController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		importFromThisConctactLabel.setVisible(false);
		customFieldTemplatesComboBox.setVisible(false);
		formsFromUserMessageLabel.setVisible(false);
		continueButton.setVisible(false);
	}
	
	@FXML
	private void onCancel()
	{
		getStage().close();
	}
	
	@FXML
	private void onSeeForms()
	{
		try
		{
			MartusAccountAccessToken token = new MartusAccountAccessToken(accessCodeTextField.getText());
			MartusApp app = getApp();
			String contactAccountId = app.getMartusAccountIdFromAccessTokenOnServer(token);
			if(contactAccountId.equals(app.getAccountId()))
			{
				showNotifyDlg("ContactKeyIsOurself");
				return;
			}
			
			formsFromUserMessageLabel.setVisible(true);
			String formsFromUserMessage = TokenReplacement.replaceToken("Forms from user #userAccessCode", "#userAccessCode", accessCodeTextField.getText());
			formsFromUserMessageLabel.setText(formsFromUserMessage);
			
			String contactPublicCode = MartusSecurity.computeFormattedPublicCode(contactAccountId);
			ObservableList<CustomFieldTemplate> fieldTemplates = getCustomFieldTemplates(contactPublicCode);
			
			customFieldTemplatesComboBox.setVisible(true);
			customFieldTemplatesComboBox.setItems(fieldTemplates);
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

	//FIXME these two methods are duplicated and need to be pulled up into new super class
	private ObservableList<CustomFieldTemplate> getCustomFieldTemplates(String contactPublicCode) throws Exception
	{
		Vector<Vector<String>> result = getApp().getListOfFormTemplatesOnServer(contactPublicCode);

		return getTitlesFromResults(contactPublicCode, result);
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

	@FXML
	private void comboSelectionChanged()
	{
		importFromThisConctactLabel.setVisible(true);
		continueButton.setVisible(true);
	}

	@FXML
	private void onContinue()
	{
		CustomFieldTemplate customFieldTemplateToSave = customFieldTemplatesComboBox.getSelectionModel().getSelectedItem();
		//FIXME urgent, save the template
	}

	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/SetupTemplateFromNewContactPopup.fxml";
	}

	@Override
	public String getDialogTitle()
	{
		return getLocalization().getWindowTitle("notifyImportTemplate"); 
	}

	@FXML
	private Label formsFromUserMessageLabel;
	
	@FXML
	private ComboBox<CustomFieldTemplate> customFieldTemplatesComboBox;
	
	
	@FXML
	private Label importFromThisConctactLabel;
	
	@FXML
	private Button continueButton;
	
	@FXML
	private TextField accessCodeTextField;
}
