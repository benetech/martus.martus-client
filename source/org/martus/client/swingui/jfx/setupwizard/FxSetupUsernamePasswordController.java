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

import org.martus.client.core.MartusUserNameAndPassword;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.FxController;

public class FxSetupUsernamePasswordController extends AbstractFxSetupWizardController implements Initializable
{
	public FxSetupUsernamePasswordController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		getWizardNavigationHandler().getNextButton().setDisable(true);
		getUserName().textProperty().addListener(new LoginChangeHandler());
		getPasswordField().textProperty().addListener(new LoginChangeHandler());
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

	public class LoginChangeHandler implements ChangeListener<String>
	{
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
		{
			boolean canContinue = false;
			if(getUserName().getText().length() > 0)
			{
				char[] password = getPasswordField().getText().toCharArray();
				if(password.length >= MartusUserNameAndPassword.BASIC_PASSWORD_LENGTH)
				{
					canContinue = true;
				}
			}

			updateErrorLabel(canContinue);
			
			getWizardNavigationHandler().getNextButton().setDisable(!canContinue);
		}

		private void updateErrorLabel(boolean canContinue)
		{
			String errorMessage = "";
			if (!canContinue)
				errorMessage = "Password must be at least 8 characters, 15 recommened.";
			
			getErrorLabel().setText(errorMessage);
		}
	}
	
	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/SetupUsernamePassword.fxml";
	}
	
	@Override
	public FxController getNextControllerClassName()
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
}
