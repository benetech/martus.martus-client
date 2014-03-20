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
package org.martus.client.swingui.jfx.setupwizard.step1;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

import org.martus.client.core.MartusUserNameAndPassword;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.setupwizard.AbstractFxSetupWizardContentController;
import org.martus.client.swingui.jfx.setupwizard.StaticAccountCreationData;
import org.martus.common.MartusLogger;

public class FxSetupUsernamePasswordController extends FxStep1Controller
{
	public FxSetupUsernamePasswordController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		getWizardNavigationHandler().getBackButton().setVisible(false);
		getWizardNavigationHandler().getNextButton().setDisable(true);
		getUserName().textProperty().addListener(new LoginChangeHandler());
		getPasswordField().textProperty().addListener(new LoginChangeHandler());
		hintLabel.setTooltip(new Tooltip("Create secure passwords by using numbers, letters and sympbols."));

		try
		{
			String sidebarHintText = getLocalization().getFieldLabel("CreateAccountTipsHtml");
			getWizardStage().getWizardShellController().setSideBarHintHtml(sidebarHintText);
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			showNotifyDialog(getWizardStage(), "UnexpectedError");
		}
	}

	@Override
	public void nextWasPressed(ActionEvent event) 
	{
		StaticAccountCreationData.setUserName(getUserName().getText());
		StaticAccountCreationData.setPassword(getPasswordField().getText().toCharArray());
	}
	
	private Label getErrorLabel()
	{
		return errorLabel;
	}

	public void updateDisplay()
	{
		boolean canContinue = false;
		String errorMessage = "";
		
		boolean hasUserName;
		boolean isPasswordLongEnough;
		boolean doesAccountExist;
		boolean usernameSameAsPassword;
		try
		{
			String candidateUserName = getUserName().getText();
			hasUserName = candidateUserName.length() > 0;
			
			char[] candidatePassword = getPasswordField().getText().toCharArray();
			isPasswordLongEnough = (candidatePassword.length >= MartusUserNameAndPassword.BASIC_PASSWORD_LENGTH);

			doesAccountExist = getApp().doesAccountExist(candidateUserName, candidatePassword);
			usernameSameAsPassword = areSame(candidateUserName, candidatePassword);

			if (!hasUserName)
				errorMessage = "Must enter a Username.";
			else if(!isPasswordLongEnough)
				errorMessage = "Password must be at least 8 characters, 15 recommened.";
			else if(usernameSameAsPassword)
				errorMessage = getLocalization().getFieldLabel("notifyPasswordMatchesUserNamecause");
			else if(doesAccountExist)
				errorMessage = "That account already exists.";

			canContinue = hasUserName && isPasswordLongEnough && !doesAccountExist && !usernameSameAsPassword;
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			errorMessage = "Unexpected error";
		}
		
		getErrorLabel().setText(errorMessage);
		getWizardNavigationHandler().getNextButton().setDisable(!canContinue);
	}

	private boolean areSame(String candidateUserName, char[] candidatePassword)
	{
		char[] username = candidateUserName.toCharArray();
		return Arrays.equals(username, candidatePassword);
	}

	public class LoginChangeHandler implements ChangeListener<String>
	{
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
		{
			updateDisplay();
		}

	}
	
	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/step1/SetupUsernamePassword.fxml";
	}
	
	@Override
	public AbstractFxSetupWizardContentController getNextController()
	{
		return new FxVerifyAccountController(getMainWindow());
	}
	
	public PasswordField getPasswordField()
	{
		return passwordField;
	}

	public void setPasswordField(PasswordField passwordField)
	{
		this.passwordField = passwordField;
	}

	public TextField getUserName()
	{
		return userName;
	}

	public void setUserName(TextField userName)
	{
		this.userName = userName;
	}

	@FXML
	private TextField userName;
	
	@FXML
	private PasswordField passwordField;
	
	@FXML
	private Label errorLabel;
	
	@FXML
	private Label hintLabel;
}
