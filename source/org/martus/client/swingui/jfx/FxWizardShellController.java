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
package org.martus.client.swingui.jfx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import org.martus.client.swingui.UiMainWindow;


abstract public class FxWizardShellController extends FxInSwingDialogController implements WizardNavigationButtonsInterface
{
	public FxWizardShellController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	private FxWizardStage getWizardStage()
	{
		return (FxWizardStage) getStage();
	}

	@FXML
	protected void onNext(ActionEvent event)
	{
		getContentNavigationHandler().nextWasPressed(event);
		getWizardStage().next();
	}
	
	@FXML
	protected void onBack(ActionEvent event) 
	{
		getContentNavigationHandler().backWasPressed(event);
		getWizardStage().back();
	}
	
	protected void disableNext()
	{
		nextButton.setDisable(true);
	}
	
	protected void enableNext()
	{
		nextButton.setDisable(false);
	}
	
	public Button getNextButton()
	{
		return nextButton;
	}
	
	public Button getBackButton()
	{
		return backButton; 
	}
	
	public void setContentController(WizardNavigationHandlerInterface contentNavigationHandlerToUse)
	{
		contentNavigationHandler = contentNavigationHandlerToUse;
		contentNavigationHandler.setNavigationHandler(this);
	}
	
	private WizardNavigationHandlerInterface getContentNavigationHandler()
	{
		return contentNavigationHandler;
	}
	
	@FXML
	protected Button nextButton;
	
	@FXML
	protected Button backButton;
	
	private WizardNavigationHandlerInterface contentNavigationHandler;
}
