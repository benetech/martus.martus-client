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

import java.util.Iterator;
import java.util.Vector;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionDoer;
import org.martus.client.swingui.actions.ActionMenuBackupMyKeyPair;
import org.martus.client.swingui.actions.ActionMenuChangeUserNamePassword;
import org.martus.client.swingui.actions.ActionMenuContactInfo;
import org.martus.client.swingui.actions.ActionMenuCreateNewBulletin;
import org.martus.client.swingui.actions.ActionMenuManageContacts;
import org.martus.client.swingui.actions.ActionMenuPreferences;
import org.martus.client.swingui.actions.ActionMenuQuickSearch;
import org.martus.client.swingui.actions.ActionMenuSelectServer;
import org.martus.client.swingui.actions.ActionMenuStopStartTor;
import org.martus.client.swingui.jfx.FxContentController;
import org.martus.client.swingui.jfx.FxInSwingFrameController;
import org.martus.common.MartusLogger;
import org.martus.common.network.OrchidTransportWrapper;

public class FxLandingShellController extends FxInSwingFrameController
{
	public FxLandingShellController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		caseListProvider = new CaseListProvider();
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/LandingShell.fxml";
	}
	
	@Override
	public void initializeMainContentPane()
	{
		updateTorStatus();
		updateCases();
	}

	private void updateCases()
	{
		caseListProvider.clear();
		Vector visibleFolders = getApp().getStore().getAllVisibleFolders();
		MartusLocalization localization = getLocalization();
		for(Iterator f = visibleFolders.iterator(); f.hasNext();)
		{
			BulletinFolder folder = (BulletinFolder) f.next();
			CaseListItem caseList = new CaseListItem(folder.getLocalizedName(localization));
			caseListProvider.add(caseList);
		}
		casesListView.setItems(caseListProvider);
	}

	@Override
	public void setContentPane(FxContentController contentController) throws Exception
	{
		Parent createContents = contentController.createContents();
		mainContentPane.getChildren().addAll(createContents);
	}

	private void updateTorStatus()
	{
	}

	private void doAction(ActionDoer doer)
	{
		getStage().doAction(doer);
	}
	
	@FXML
	private void onPreferences(ActionEvent event)
	{
		doAction(new ActionMenuPreferences(getMainWindow()));
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
		OrchidTransportWrapper transport = getApp().getTransport();
		boolean oldState = transport.isOnline();
		boolean newState = !oldState;
		transport.setIsOnline(newState);
		String text = newState ? "On" : "Off";
		toolbarButtonOnline.setText(text);
		//FIXME: Need to save to config
	}

	@FXML
	private void onTor(ActionEvent event)
	{
		doAction(new ActionMenuStopStartTor(getMainWindow()));
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
	private void OnCasesMouseClicked(MouseEvent mouseEvent)
	{
		CaseListItem selectedCase = caseListProvider.get(casesListView.getSelectionModel().getSelectedIndex());
		MartusLogger.log("selected" + selectedCase);
	}

	@FXML
	protected TextField searchText;
	
	@FXML
	protected Button toolbarButtonOnline;
	
	@FXML
	protected ToggleButton toolbarButtonTor;
	
	@FXML
	protected AnchorPane mainContentPane;
	
	@FXML
	protected ListView<CaseListItem> casesListView;

	private CaseListProvider caseListProvider;
}
