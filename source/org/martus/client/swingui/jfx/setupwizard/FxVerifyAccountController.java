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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.FxController;
import org.martus.common.MartusLogger;

public class FxVerifyAccountController extends AbstractFxSetupWizardController implements Initializable
{
	public FxVerifyAccountController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		getWizardNavigationHandler().getNextButton().setDisable(true);
		userNameField.textProperty().addListener(new LoginChangeHandler());
		passwordField.textProperty().addListener(new LoginChangeHandler());
	}

	@Override
	public void nextWasPressed(ActionEvent event) 
	{
		try
		{
			createAccount();
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}

	private void createAccount() throws Exception
	{
		String userNameValue = userNameField.getText();
		String passwordValue = passwordField.getText();
		
		StaticAccountCreationData.dispose();
		getMainWindow().getApp().createAccount(userNameValue, passwordValue.toCharArray());
	}
	
	private boolean isOkToCreateAccount()
	{
		String userNameValue = userNameField.getText();
		String passwordValue = passwordField.getText();
		if (!userNameValue.equals(StaticAccountCreationData.getUserName()))
			return false;
		
		if (!passwordValue.equals(StaticAccountCreationData.getPassword()))
			return false;
		
		return true;
	}
	
	private Label getAccountConfirmLabel()
	{
		return accountConfirmLabel;
	}
	
	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/VerifyAccount.fxml";
	}
	
	@Override
	public FxController getNextControllerClassName()
	{
		return new FxSetupContactInfoController(getMainWindow());
	}
	
	private class LoginChangeHandler implements ChangeListener<String>
	{
		public LoginChangeHandler()
		{
		}
		
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
		{
			try
			{
				getAccountConfirmLabel().setText("");
				boolean shouldBeEnabled = isOkToCreateAccount();
				getWizardNavigationHandler().getNextButton().setDisable(!shouldBeEnabled);
				if (shouldBeEnabled)
					getAccountConfirmLabel().setText("User name and password match!");
			}
			catch (Exception e)
			{
				MartusLogger.logException(e);
			}
		}
	}

	@FXML
	private TextField userNameField;
	
	@FXML
	private PasswordField passwordField;
	
	@FXML
	private Label accountConfirmLabel;
}
