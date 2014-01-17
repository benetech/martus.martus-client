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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.FxController;
import org.martus.client.swingui.jfx.FxWizardStage;
import org.martus.common.MartusLogger;

public class FxSetupUsernamePasswordController extends FxController
{
	public FxSetupUsernamePasswordController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	@FXML
	protected void handleNext(ActionEvent event) 
	{
		try
		{
			createAccount();
			getStage().handleNavigationEvent(FxWizardStage.NAVIGATION_NEXT);
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}

	private void createAccount() throws Exception
	{
		String userNameValue = userName.getText();
		String passwordValue = passwordField.getText();
		String confirmedPasswordValue = confirmPasswordField.getText();
		//FIXME need better handling of invalid pw and u
		if (!passwordValue.equals(confirmedPasswordValue))
			return;
		
		getMainWindow().getApp().createAccount(userNameValue, passwordValue.toCharArray());
	}

	@FXML
	protected void handleBack(ActionEvent event) 
	{
		getStage().handleNavigationEvent(FxWizardStage.NAVIGATION_BACK);
	}

	@FXML
	private TextField userName;
	
	@FXML
	private PasswordField passwordField;
	
	@FXML
	private PasswordField confirmPasswordField;
}
