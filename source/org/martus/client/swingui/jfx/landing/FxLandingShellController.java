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
package org.martus.client.swingui.jfx.landing;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import org.martus.client.core.ConfigInfo;
import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionMenuBackupMyKeyPair;
import org.martus.client.swingui.actions.ActionMenuChangeUserNamePassword;
import org.martus.client.swingui.actions.ActionMenuContactInfo;
import org.martus.client.swingui.actions.ActionMenuCreateNewBulletin;
import org.martus.client.swingui.actions.ActionMenuManageContacts;
import org.martus.client.swingui.actions.ActionMenuPreferences;
import org.martus.client.swingui.actions.ActionMenuQuickSearch;
import org.martus.client.swingui.actions.ActionMenuSelectServer;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.generic.FxNonWizardShellController;
import org.martus.client.swingui.jfx.generic.FxmlLoaderWithController;
import org.martus.client.swingui.jfx.landing.bulletins.BulletinsListController;
import org.martus.client.swingui.jfx.landing.cases.FxCaseManagementController;
import org.martus.common.MartusLogger;
import org.martus.common.network.OrchidTransportWrapper;

public class FxLandingShellController extends FxNonWizardShellController
{
	public FxLandingShellController(UiMainWindow mainWindowToUse, FxController contentController)
	{
		super(mainWindowToUse, contentController);
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/LandingShell.fxml";
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		updateOnlineStatus();
		updateTorStatus();
	}
	
	@Override
	public void loadAndIntegrateContentPane(FxController contentController) throws Exception
	{
		Parent createContents = contentController.createContents();
		mainContentPane.getChildren().addAll(createContents);
		
		setupCaseManagementSidebar();
	}

	private void setupCaseManagementSidebar() throws Exception, IOException
	{
		FxCaseManagementController caseManagementSideBar = new FxCaseManagementController(getMainWindow());
		caseManagementSideBar.setShellController(getSwingStage().getShellController());
		URL url = getBestFxmlLocation(caseManagementSideBar.getFxmlLocation());
		FxmlLoaderWithController sidebarLoader = new FxmlLoaderWithController(caseManagementSideBar, url);
		Node sideBarNode = (Node) sidebarLoader.load();
		sideContentPane.getChildren().addAll(sideBarNode);
	}

	@FXML
	private void onPreferences(ActionEvent event)
	{
		doAction(new ActionMenuPreferences(getMainWindow()));
	}
	
	private String getStatusMessage(Boolean state)
	{
		MartusLocalization localization = getLocalization();
		String on = localization.getButtonLabel("On");
		String off = localization.getButtonLabel("Off");
		return state ? on : off;
	}

	private void updateTorStatus()
	{
		OrchidTransportWrapper transport = getApp().getTransport();
		boolean isTorRequested = transport.isTorEnabled();
		toolbarButtonTor.setText(getStatusMessage(isTorRequested));
	}
	
	private void updateOnlineStatus()
	{
		boolean isOnline = getApp().getTransport().isOnline();
		toolbarButtonOnline.setText(getStatusMessage(isOnline));
	}

	@FXML
	private void onManageContacts(ActionEvent event)
	{
		doAction(new ActionMenuManageContacts(getMainWindow()));
	}

	@FXML
	private void onConfigureServer(ActionEvent event)
	{
		doAction(new ActionMenuSelectServer(getMainWindow()));
	}
	
	@FXML
	private void onChangeUsernameAndPassword(ActionEvent event)
	{
		doAction(new ActionMenuChangeUserNamePassword(getMainWindow()));
	}
	
	@FXML
	private void onCreateNewAccount(ActionEvent event)
	{
		//TODO: add doAction
	}
	
	@FXML
	private void onQuickSearch(ActionEvent event)
	{
		doAction(new  ActionMenuQuickSearch(getMainWindow(), searchText.getText()));
	}

	@FXML
	private void onCreateNewBulletin(ActionEvent event)
	{
		doAction(new ActionMenuCreateNewBulletin(getMainWindow()));
	}
	
	@FXML
	private void onOnline(ActionEvent event)
	{
		boolean oldState = getApp().getTransport().isOnline();
		try
		{
			ConfigInfo configInfo = getApp().getConfigInfo();
			boolean newState = !oldState;
			
			configInfo.setIsNetworkOnline(newState);
			getApp().saveConfigInfo();

			updateOnlineStatus();
		} 
		catch (SaveConfigInfoException e)
		{
			MartusLogger.logException(e);
			getMainWindow().notifyDlg("ErrorSavingConfig");
		}
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	@FXML
	private void onTor(ActionEvent event)
	{
		boolean oldState = getApp().getTransport().isTorEnabled();
		try
		{
			ConfigInfo configInfo = getApp().getConfigInfo();
			boolean newState = !oldState;
			
			configInfo.setUseInternalTor(newState);
			getApp().saveConfigInfo();

			updateTorStatus();
		} 
		catch (SaveConfigInfoException e)
		{
			MartusLogger.logException(e);
			getMainWindow().notifyDlg("ErrorSavingConfig");
		}
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	@FXML
	private void onContactInformation(ActionEvent event)
	{
		doAction(new ActionMenuContactInfo(getMainWindow()));
	}
	
	@FXML
	private void onBackupKeypair(ActionEvent event)
	{
		doAction(new ActionMenuBackupMyKeyPair(getMainWindow()));
	}
	

	@FXML
	public void onLogoClicked(MouseEvent mouseEvent) 
	{
		try
		{
			FxMainStage stage = (FxMainStage) getStage();
			BulletinsListController bulletinListController = stage.getBulletinsListController();
			bulletinListController.loadAllBulletinsAndSortByMostRecent();
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}


	@FXML
	private TextField searchText;
	
	@FXML
	private Button toolbarButtonOnline;
	
	@FXML
	private Button toolbarButtonTor;
	
	@FXML
	private Pane sideContentPane;
	
	@FXML
	private Pane mainContentPane;
	
	
}
