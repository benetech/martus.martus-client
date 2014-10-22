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

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
import org.martus.client.swingui.actions.ActionMenuQuickEraseDeleteMyData;
import org.martus.client.swingui.actions.ActionMenuQuickSearch;
import org.martus.client.swingui.jfx.generic.DialogWithCloseShellController;
import org.martus.client.swingui.jfx.generic.FxNonWizardShellController;
import org.martus.client.swingui.jfx.landing.bulletins.BulletinListProvider;
import org.martus.client.swingui.jfx.landing.bulletins.BulletinsListController;
import org.martus.client.swingui.jfx.landing.cases.CaseListProvider;
import org.martus.client.swingui.jfx.landing.cases.FxCaseManagementController;
import org.martus.client.swingui.jfx.landing.general.SettingsController;
import org.martus.common.MartusLogger;
import org.martus.common.network.OrchidTransportWrapper;

public class FxLandingShellController extends FxNonWizardShellController
{
	public FxLandingShellController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		bulletinListProvider = new BulletinListProvider(getMainWindow());
		bulletinsListController = new BulletinsListController(getMainWindow(), bulletinListProvider);
		caseManagementController = new FxCaseManagementController(getMainWindow());
	}
	
	public BulletinsListController getBulletinsListController()
	{
		return bulletinsListController;
	}
	
	public CaseListProvider getAllCaseListProvider()
	{
		return caseManagementController.getAllCaseListProvider();
	}
	
	public BooleanBinding getShowTrashBinding()
	{
		return bulletinsListController.getTrashNotBeingDisplayedBinding();
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/LandingShell.fxml";
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		updateOnlineStatus();
		updateTorStatus();
		initializeTorListener();
		caseManagementController.addFolderSelectionListener(bulletinListProvider);		
	}

	private void initializeTorListener()
	{
		TorChangeListener torChangeListener = new TorChangeListener();
		Property<Boolean> configInfoUseInternalTorProperty = getApp().getConfigInfo().useInternalTorProperty();
		configInfoUseInternalTorProperty.addListener(torChangeListener);
		Property<Boolean> orchidTransportWrapperTorProperty = getApp().getTransport().getIsTorActiveProperty();
		orchidTransportWrapperTorProperty.addListener(torChangeListener);
	}

	@Override
	public Parent createContents() throws Exception
	{
		Parent contents = super.createContents();
		loadControllerAndEmbedInPane(caseManagementController, sideContentPane);
		loadControllerAndEmbedInPane(bulletinsListController, mainContentPane);
		return contents;
	}

	private String getStatusMessage(Boolean state)
	{
		MartusLocalization localization = getLocalization();
		String on = localization.getButtonLabel("On");
		String off = localization.getButtonLabel("Off");
		return state ? on : off;
	}

	protected void updateTorStatus()
	{
		OrchidTransportWrapper transport = getApp().getTransport();
		boolean isTorEnabled = transport.isTorEnabled();
		toolbarButtonTor.setText(getStatusMessage(isTorEnabled));
	}
	
	class UpdateTorStatusLater implements Runnable
	{
		public void run()
		{
			updateTorStatus();
		}
	}

	
	private final class TorChangeListener implements ChangeListener<Boolean>
	{
		public TorChangeListener()
		{
		}

		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) 
		{
			runUpdateOnFxThread();
		}

		private void runUpdateOnFxThread()
		{
			Platform.runLater(new UpdateTorStatusLater());
		}
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

	private void onSettings(String tabToDisplayFirst)
	{
		SettingsController settingsController = new SettingsController(getMainWindow());
		settingsController.firstTabToDisplay(tabToDisplayFirst);
		DialogWithCloseShellController shellController = new DialogWithCloseShellController(getMainWindow(), settingsController);
		doAction(shellController);
	}

	@FXML
	private void onConfigureServer(ActionEvent event)
	{
		//TODO remove this Old Swing doAction(new ActionMenuSelectServer(getMainWindow()));
		onSettings(SettingsController.SERVER_TAB);
	}
	
	@FXML
	private void onSystemPreferences(ActionEvent event)
	{
		//TODO remove this Old Swing doAction(new ActionMenuPreferences(getMainWindow()));
		onSettings(SettingsController.SYSTEM_TAB);
	}
	
	@FXML
	private void onTorPreferences(ActionEvent event)
	{
		onSettings(SettingsController.TOR_TAB);
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
		boolean newState = !oldState;
		getApp().turnNetworkOnOrOff(newState);
		updateOnlineStatus();
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
		} 
		catch (SaveConfigInfoException e)
		{
			MartusLogger.logException(e);
			showNotifyDialog("ErrorSavingConfig");
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
			BulletinsListController bulletinListController = getBulletinsListController();
			bulletinListController.loadAllBulletinsAndSortByMostRecent();
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}
	
	@FXML
	public void onDeleteMyData(ActionEvent event)
	{
		if(showOkCancelConfirmationDialog("confirmDeleteMyData", "QuickEraseWillNotRemoveItems"))
		{
			doAction(new ActionMenuQuickEraseDeleteMyData(getMainWindow()));
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
	
	private BulletinsListController bulletinsListController;
	private BulletinListProvider bulletinListProvider;
	private FxCaseManagementController caseManagementController;
}

