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

import javafx.fxml.Initializable;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.ContentController;
import org.martus.client.swingui.jfx.FxWizardStage;
import org.martus.client.swingui.jfx.WizardNavigationButtonsInterface;
import org.martus.client.swingui.jfx.WizardNavigationHandlerInterface;
import org.martus.client.swingui.jfx.setupwizard.tasks.ConnectToServerTask;

abstract public class AbstractFxSetupWizardContentController extends ContentController implements WizardNavigationHandlerInterface, Initializable
{
	public AbstractFxSetupWizardContentController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	
	final public void initialize(URL arg0, ResourceBundle arg1)
	{
		super.initialize(arg0, arg1);
		if(!hasMainContentPaneBeenInitialized)
		{
			initializeMainContentPane();
			hasMainContentPaneBeenInitialized = true;
		}
		else
		{
			initializeSidebarContentPane();
		}
	}
	
	private void clearHasBeenInitialized()
	{
		hasMainContentPaneBeenInitialized = false;
	}
 
	abstract public void initializeMainContentPane();

	public void initializeSidebarContentPane()
	{
	}
	
	public WizardNavigationButtonsInterface getWizardNavigationHandler()
	{
		return wizardNavigationHandler;
	}
	
	public void nextWasPressed() throws Exception
	{
		clearHasBeenInitialized();
	}
	
	public void backWasPressed() throws Exception
	{
		clearHasBeenInitialized();
	}
	
	public void setNavigationHandler(WizardNavigationButtonsInterface navigationHandlerToUse)
	{
		wizardNavigationHandler = navigationHandlerToUse;
	}
	
	public FxWizardStage getWizardStage()
	{
		return (FxWizardStage)getStage();
	}
	
	abstract public int getWizardStepNumber();

	public String getSidebarFxmlLocation()
	{
		return "setupwizard/DefaultSidebar.fxml";
	}
	
	protected boolean isCurrentServerAvailable() throws Exception
	{
		if(getWizardStage().hasServerAvailabilityBeenInitialized())
			return getWizardStage().checkIfCurrentServerIsAvailable();

		if(getApp().getCurrentNetworkInterfaceGateway().getInterface() == null)
			return false;
		
		ConnectToServerTask task = new ConnectToServerTask(getApp());
		showTimeoutDialog(getWizardStage(), getLocalization().getFieldLabel("AttemptToConnectToServerNoCompliance"), task);
		boolean isServerAvailable = task.isAvailable();
		getWizardStage().setCurrentServerIsAvailable(isServerAvailable);
		
		return isServerAvailable;
	}
	
	private WizardNavigationButtonsInterface wizardNavigationHandler;
	private boolean hasMainContentPaneBeenInitialized;

}
