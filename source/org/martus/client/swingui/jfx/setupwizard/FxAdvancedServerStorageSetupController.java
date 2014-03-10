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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import org.martus.client.core.ConfigInfo;
import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.FxController;
import org.martus.client.swingui.jfx.FxController.UserCancelledException;
import org.martus.client.swingui.jfx.setupwizard.tasks.GetServerPublicKeyTask;
import org.martus.common.MartusLogger;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;

public class FxAdvancedServerStorageSetupController extends	FxSetupWizardAbstractServerSetupController implements Initializable
{
	public FxAdvancedServerStorageSetupController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		
		clearServerStatus();
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		ipAddressField.textProperty().addListener(new TextFieldChangeHandler());
		publicCodeField.textProperty().addListener(new TextFieldChangeHandler());
		
		ConfigInfo config = getApp().getConfigInfo();
		ipAddressField.setText(config.getServerName());
		String serverKey = config.getServerPublicKey();
		if(serverKey.length() > 0)
		{
			try
			{
				String publicCode = MartusSecurity.computeFormattedPublicCode(serverKey);
				publicCodeField.setText(publicCode);
			}
			catch(Exception e)
			{
				MartusLogger.logException(e);
				// TODO: Should we display an error here, or just be silent?
			}
		}
		
		updateButtonStates();
	}

	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/AdvancedServerStorageSetup.fxml";
	}
	
	@FXML
	public void connect()
	{
		try
		{
			clearServerStatus();
			
			String ip = ipAddressField.getText();
			
			GetServerPublicKeyTask task = new GetServerPublicKeyTask(getApp(), ip);
			showTimeoutDialog("*Connecting*", "Getting server information", task, 60);
			
			String serverKey = task.getPublicKey();
			String serverPublicCode = MartusCrypto.computePublicCode(serverKey);

			String userEnteredPublicCode = publicCodeField.getText();
			String normalizedPublicCode = MartusCrypto.removeNonDigits(userEnteredPublicCode);
			if(!serverPublicCode.equals(normalizedPublicCode))
			{
				showError("ServerCodeWrong");
				return;
			}
			
			isConnected = attemptToConnect(ip, serverKey, true);
		} 
		catch(UserCancelledException e)
		{
			return;
		}
		catch (SaveConfigInfoException e)
		{
			MartusLogger.logException(e);
			System.exit(1);
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			System.exit(1);
		}

		updateButtonStates();
	}
	
	@Override
	public FxController getNextControllerClassName()
	{
		return new FxAddContactsController(getMainWindow());
	}
	
	protected class TextFieldChangeHandler implements ChangeListener<String>
	{
		@Override
		public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2)
		{
			clearServerStatus();
			updateButtonStates();
		}
		
	}
	
	protected void clearServerStatus()
	{
		isConnected = false;
	}
	
	protected void updateButtonStates()
	{
		boolean hasIp = false;
		boolean hasPublicCode = false;
		
		String ip = ipAddressField.getText();
		if(ip.length() > 0)
			hasIp = true;
		
		String publicCode = publicCodeField.getText();
		if(publicCode.length() > 0)
			hasPublicCode = true;
		
		boolean canConnect = (hasIp && hasPublicCode);
		connectButton.setDisable(!canConnect);

		getWizardNavigationHandler().getNextButton().setDisable(!isConnected);
	}
	
	private void showError(String text)
	{
		showNotifyDialog(text);
	}

	@FXML
	private Label statusLabel;
	
	@FXML
	private Button connectButton;
	
	@FXML
	private TextField ipAddressField;
	
	@FXML
	private TextField publicCodeField;
	
	@FXML
	private TextField magicWordField;
	
	private boolean isConnected;
}
