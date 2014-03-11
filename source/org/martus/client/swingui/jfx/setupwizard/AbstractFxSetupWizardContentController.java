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
import org.martus.client.swingui.jfx.ContentNavigationHandlerInterface;
import org.martus.client.swingui.jfx.FxInSwingDialogController;
import org.martus.client.swingui.jfx.FxWizardStage;
import org.martus.client.swingui.jfx.NavigationButtonsInterface;
import org.martus.client.swingui.jfx.setupwizard.tasks.IsServerAvailableTask;

abstract public class AbstractFxSetupWizardContentController extends FxInSwingDialogController implements ContentNavigationHandlerInterface, Initializable
{
	public AbstractFxSetupWizardContentController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void initialize(URL rootLocation, ResourceBundle bundle)
	{
	}
	
	public NavigationButtonsInterface getWizardNavigationHandler()
	{
		return wizardNavigationHandler;
	}
	
	public void nextWasPressed(ActionEvent actionEvent)
	{
	}
	
	public void backWasPressed(ActionEvent actionEvent)
	{
	}
	
	public void setWizardNavigationHandler(NavigationButtonsInterface wizardNavigationHandlerToUse)
	{
		wizardNavigationHandler = wizardNavigationHandlerToUse;
	}
	
	public FxWizardStage getWizardStage()
	{
		return (FxWizardStage)getFxStage();
	}
	
	abstract public int getWizardStepNumber();
	
	protected boolean isCurrentServerAvailable() throws Exception
	{
		IsServerAvailableTask task = new IsServerAvailableTask(getApp());
		showTimeoutDialog("*Connecting*", "Attempting to connect to server", task, 60);
		boolean isDefaultServerAvailable = task.isAvailable();
		return isDefaultServerAvailable;
	}
	
	private NavigationButtonsInterface wizardNavigationHandler;
}
