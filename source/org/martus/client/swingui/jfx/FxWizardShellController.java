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

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import org.martus.client.swingui.UiMainWindow;


abstract public class FxWizardShellController extends ShellController implements WizardNavigationButtonsInterface
{
	public FxWizardShellController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		
		sidebarHints.setVisible(false);
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
	
	public void setSideBarHintHtml(String hintText) throws Exception
	{
		WebEngine engine = sidebarHints.getEngine();
		URL cssUrl = getScene().getBestCssLocation();
		engine.setUserStyleSheetLocation(cssUrl.toExternalForm());
		engine.loadContent("<div class='sidebar-hint'>" + hintText + "</div>");
		sidebarHints.setVisible(true);
	}

	private WizardNavigationHandlerInterface getContentNavigationHandler()
	{
		return contentNavigationHandler;
	}
	
	@FXML
	protected Button nextButton;
	
	@FXML
	protected Button backButton;
	
	@FXML
	protected WebView sidebarHints;

	private WizardNavigationHandlerInterface contentNavigationHandler;
}
