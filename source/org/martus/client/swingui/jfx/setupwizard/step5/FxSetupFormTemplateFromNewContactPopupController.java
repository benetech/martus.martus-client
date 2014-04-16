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
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.FxWizardStage;
import org.martus.client.swingui.jfx.setupwizard.AccessTokenChangeHandler;
import org.martus.common.ContactKey;
import org.martus.common.Exceptions.ServerNotAvailableException;
import org.martus.common.MartusAccountAccessToken;
import org.martus.common.MartusAccountAccessToken.TokenNotFoundException;
import org.martus.common.MartusLogger;
import org.martus.common.fieldspec.CustomFieldTemplate;


public class FxSetupFormTemplateFromNewContactPopupController extends AbstractFxImportFormTemplateController implements Initializable
{
	public FxSetupFormTemplateFromNewContactPopupController(UiMainWindow mainWindowToUse, FxWizardStage wizardPanelToUse)
	{
		super(mainWindowToUse, wizardPanelToUse);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		formTemplateChoiceBox.setConverter(new FormTemplateToStringConverter());
		formTemplateChoiceBox.valueProperty().addListener(new FieldTemplateChoiceChangeHandler());
		
		formTemplateChoiceBox.setVisible(false);
		continueButton.setVisible(false);
		noTemplatesAvailableLabel.setVisible(false);
		
		seeFormTemplatesButton.setDisable(true);
		accessTokenTextField.textProperty().addListener(new AccessTokenChangeHandler(accessTokenTextField, seeFormTemplatesButton));
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
			MartusAccountAccessToken token = new MartusAccountAccessToken(accessTokenTextField.getText());
			MartusApp app = getApp();
			String contactAccountId = app.getMartusAccountIdFromAccessTokenOnServer(token);
			if(contactAccountId.equals(app.getAccountId()))
			{
				showNotifyDialog(getWizardStage(), "ContactKeyIsOurself");
				return;
			}
			
			noTemplatesAvailableLabel.setVisible(false);
			
			formTemplateChoiceBox.setVisible(false);
			ObservableList<CustomFieldTemplate> fieldTemplates = getFormTemplates(new ContactKey(contactAccountId));
			if (fieldTemplates.isEmpty())
			{
				noTemplatesAvailableLabel.setVisible(true);
				return;
			}
				
			formTemplateChoiceBox.setVisible(true);
			ObservableList<CustomFieldTemplate> currentItems = formTemplateChoiceBox.getItems();
			currentItems.setAll(fieldTemplates);
		} 
		catch (ServerNotAvailableException e)
		{
			showNotifyDialog(getWizardStage(), "ContactsNoServer");
		} 
		catch (TokenNotFoundException e)
		{
			showNotifyDialog(getWizardStage(), "UnableToRetrieveContactFromServer");
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			showNotifyDialog(getWizardStage(), "UnexpectedError");
		} 
	}

	protected void updateButtonVisibility(boolean isVisible)
	{
		continueButton.setVisible(isVisible);
	}

	@FXML
	private void onContinue()
	{
		wasTemplateChosen = true;
		getStage().close();
	}

	public CustomFieldTemplate getSelectedFormTemplate()
	{
		if(wasTemplateChosen)
			return formTemplateChoiceBox.getSelectionModel().getSelectedItem();
		return null;
	}

	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/step5/SetupTemplateFromNewContactPopup.fxml";
	}

	@Override
	public String getDialogTitle()
	{
		return getLocalization().getWindowTitle("notifyImportTemplate"); 
	}
	
	@Override
	public String getLabel()
	{
		return getLocalization().getFieldLabel("DownloadTemplateFromMartusUser");
	}
	
	protected class FieldTemplateChoiceChangeHandler implements ChangeListener<CustomFieldTemplate>
	{
		@Override
		public void changed(ObservableValue<? extends CustomFieldTemplate> observable, CustomFieldTemplate oldValue, CustomFieldTemplate newValue)
		{
			boolean isVisible = newValue != null;
			updateButtonVisibility(isVisible); 
		}
	}
	
	@FXML
	private ChoiceBox<CustomFieldTemplate> formTemplateChoiceBox;
	
	
	@FXML
	private Button continueButton;
	
	@FXML
	private TextField accessTokenTextField;
	
	@FXML
	private Button seeFormTemplatesButton;
	
	@FXML
	protected Label noTemplatesAvailableLabel;
	
	private boolean wasTemplateChosen;
}
