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
package org.martus.client.swingui.jfx.setupwizard.step3;

import org.martus.client.core.ConfigInfo;
import org.martus.client.core.MartusApp;
import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.setupwizard.tasks.IsCompliantServerAvailableTask;
import org.martus.clientside.ClientSideNetworkGateway;
import org.martus.common.MartusLogger;

abstract public class FxSetupWizardAbstractServerSetupController extends FxStep3Controller
{
	public FxSetupWizardAbstractServerSetupController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	public boolean attemptToConnect(String serverIPAddress, String serverPublicKey, boolean askComplianceAcceptance)
	{
		MartusLogger.log("Attempting to connect to: " + serverIPAddress);
		MartusApp app = getApp();
		getMainWindow().clearStatusMessage();
		ClientSideNetworkGateway gateway = ClientSideNetworkGateway.buildGateway(serverIPAddress, serverPublicKey, getApp().getTransport());

		IsCompliantServerAvailableTask task = new IsCompliantServerAvailableTask(getApp(), gateway);
		try
		{
			showTimeoutDialog("*Connecting*", "Attempting to connect to server", task);
			if(!task.isAvailable())
			{
				//FIXME put in real text/title here.
				if(showConfirmationDialog("title", "SSL Not responding.  Save this configuration?"))
				{
					saveServerConfig(serverIPAddress, serverPublicKey, "");
					return true;
				}
			}
			String complianceStatement = task.getComplianceStatement();
			if(askComplianceAcceptance)
			{
				if(complianceStatement.equals(""))
				{
					showNotifyDialog("ServerComplianceFailed");
					saveServerConfig(serverIPAddress, serverPublicKey, "");
					return true;
				}
				
				if(!acceptCompliance(complianceStatement))
				{
					ConfigInfo previousServerInfo = getApp().getConfigInfo();

					//TODO:The following line shouldn't be necessary but without it, the trustmanager 
					//will reject the old server, we don't know why.
					ClientSideNetworkGateway.buildGateway(previousServerInfo.getServerName(), previousServerInfo.getServerPublicKey(), getApp().getTransport());
					
					if(serverIPAddress.equals(previousServerInfo.getServerName()) &&
					   serverPublicKey.equals(previousServerInfo.getServerPublicKey()))
					{
						getApp().setServerInfo("","","");
					}

					return false;
				}
			}

			getApp().setServerInfo(serverIPAddress, serverPublicKey, complianceStatement);
			
			app.getStore().clearOnServerLists();
			
			getMainWindow().forceRecheckOfUidsOnServer();
			app.getStore().clearOnServerLists();
			getMainWindow().repaint();
			getMainWindow().setStatusMessageReady();
			return true;
		}
		catch(UserCancelledException e)
		{
			return false;
		}
		catch(SaveConfigInfoException e)
		{
			MartusLogger.logException(e);
			showNotifyDialog("ErrorSavingConfig");
			return false;
		} 
		catch(Exception e)
		{
			MartusLogger.logException(e);
			showNotifyDialog("ErrorGettingCompliance");
			return false;
		} 
	}
	
	private boolean acceptCompliance(String newServerCompliance)
	{
		MartusLocalization localization = getLocalization();
		String title = localization.getWindowTitle("ServerCompliance");
		String complianceStatementMsg = String.format("%s\n\n%s", localization.getFieldLabel("ServerComplianceDescription"), newServerCompliance);
		if(!showConfirmationDialog(title, complianceStatementMsg))
		{
			showNotifyDialog("UserRejectedServerCompliance");
			return false;
		}
		return true;
	}

	
	private void saveServerConfig(String serverIPAddress, String serverPublicKey, String complianceStatement) throws SaveConfigInfoException
	{
		getApp().setServerInfo(serverIPAddress, serverPublicKey, complianceStatement);
	}

}
