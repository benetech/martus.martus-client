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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import org.martus.client.swingui.UiMainWindow;
import org.martus.common.MartusLogger;

public class FxSetupUsernamePasswordController extends AbstractFxSetupWizardController
{
	public FxSetupUsernamePasswordController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	@Override
	public void handleNext(ActionEvent event) 
	{
		StaticAccountCreationData.setUserName(userName.getText());
		StaticAccountCreationData.setPassword(passwordField.getText());
	}

	@FXML
	protected void handleUsernameChanged(KeyEvent keyEvent)
	{
		String userNameValue = userName.getText();
		String passwordValue = passwordField.getText();
		try
		{ 
			getWizardNavigationHandler().getNextButton().setDisable(false);
			errorLabel.setText("");
			if (getMainWindow().getApp().doesAccountExist(userNameValue, passwordValue.toCharArray()))
			{
				getWizardNavigationHandler().getNextButton().setDisable(true);
				errorLabel.setText("Account already Exists!");
			}
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}
	
	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/SetupUsernamePassword.fxml";
	}
	
	@FXML
	private TextField userName;
	
	@FXML
	private PasswordField passwordField;
	
	@FXML
	private PasswordField confirmPasswordField;
	
	@FXML
	private Label errorLabel;
}
