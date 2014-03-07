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

import org.martus.client.core.ConfigInfo;
import org.martus.client.core.MartusApp;
import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.setupwizard.tasks.IsServerAvailableTask;
import org.martus.clientside.ClientSideNetworkGateway;
import org.martus.common.MartusLogger;

abstract public class FxSetupWizardAbstractServerSetupController extends AbstractFxSetupWizardContentController
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

		try
		{
			IsServerAvailableTask task = new IsServerAvailableTask(getApp(), gateway);
			showTimeoutDialog("*Connecting*", "Attempting to connect to server", task, 15);
			if(!task.isAvailable())
			{
				// FIXME: This should be a confirmation
				showNotifyDialog("ServerSSLNotResponding");
				saveServerConfig(serverIPAddress, serverPublicKey, "");
				return true;
			}
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
			showNotifyDialog("ErrorServerNotAvailable");
			return false;
		}

		try
		{
			String complianceStatement = getServerCompliance(gateway);
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
		catch(SaveConfigInfoException e)
		{
			MartusLogger.logException(e);
			showNotifyDialog("ErrorSavingConfig");
			return false;
		} 
	}
	
	private String getServerCompliance(ClientSideNetworkGateway gateway)
	{
		try
		{
			return getApp().getServerCompliance(gateway);
		}
		catch (Exception e)
		{
			return "";
		}
	}

	private boolean acceptCompliance(String newServerCompliance)
	{
		// FIXME: Actually allow the user to accept/reject
		showNotifyDialog("ReplaceThisWithAConfirmationDialogShowingTheComplianceStatement");
		boolean accepted = true;
		if(!accepted)
			showNotifyDialog("UserRejectedServerCompliance");
		
		return accepted;
	}

	
	private void saveServerConfig(String serverIPAddress, String serverPublicKey, String complianceStatement) throws SaveConfigInfoException
	{
		getApp().setServerInfo(serverIPAddress, serverPublicKey, complianceStatement);
	}

}
