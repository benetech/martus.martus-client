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

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.ContentController;
import org.martus.client.swingui.jfx.FxWizardStage;
import org.martus.client.swingui.jfx.WizardNavigationButtonsInterface;
import org.martus.client.swingui.jfx.WizardNavigationHandlerInterface;
import org.martus.client.swingui.jfx.setupwizard.tasks.IsServerAvailableTask;

abstract public class AbstractFxSetupWizardContentController extends ContentController implements WizardNavigationHandlerInterface, Initializable
{
	public AbstractFxSetupWizardContentController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void initialize(URL rootLocation, ResourceBundle bundle)
	{
	}
	
	public WizardNavigationButtonsInterface getWizardNavigationHandler()
	{
		return wizardNavigationHandler;
	}
	
	public void nextWasPressed(ActionEvent actionEvent)
	{
	}
	
	public void backWasPressed(ActionEvent actionEvent)
	{
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
	
	protected boolean isCurrentServerAvailable() throws Exception
	{
		if(getWizardStage().hasServerAvailabilityBeenInitialized())
			return getWizardStage().isDefaultServerAvailable();

		if(getApp().getCurrentNetworkInterfaceGateway().getInterface() == null)
			return false;
		
		IsServerAvailableTask task = new IsServerAvailableTask(getApp());
		showTimeoutDialog("*Connecting*", "Attempting to connect to server", task);
		boolean isDefaultServerAvailable = task.isAvailable();
		getWizardStage().setDefaultServerIsAvailable(isDefaultServerAvailable);
		
		return isDefaultServerAvailable;
	}
	
	private WizardNavigationButtonsInterface wizardNavigationHandler;
}
