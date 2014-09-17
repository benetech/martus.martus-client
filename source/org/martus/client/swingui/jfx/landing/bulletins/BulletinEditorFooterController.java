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
package org.martus.client.swingui.jfx.landing.bulletins;

import java.util.HashMap;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import org.martus.client.core.FxBulletin;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiBulletinDetailsDialog;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.UniversalId;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;

public class BulletinEditorFooterController extends FxController
{

	public BulletinEditorFooterController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	private class HistoryItem extends Label
	{
		public HistoryItem(String data, String localIdToUse)
		{
			super(data);
//			localId = localIdToUse;
		}
		
//		String localId;
	}

	public void showBulletin(FxBulletin bulletinToShow)
	{
		bulletin = bulletinToShow;
		try
		{
			BulletinHistory history = bulletinToShow.getHistory().getValue();
			UniversalId bulletinUid = bulletinToShow.getUniversalIdProperty().getValue();
			String accountId = bulletinUid.getAccountId();
			UiMainWindow mainWindow = getMainWindow();
			historyItemLabels = FXCollections.observableArrayList();
			for(int i = 0; i < history.size(); ++i)
			{
				String localId = history.get(i);
				UniversalId versionUid = UniversalId.createFromAccountAndLocalId(accountId, localId);
				String dateSaved = UiBulletinDetailsDialog.getSavedDateToDisplay(versionUid,bulletinUid, mainWindow);
				String title = UiBulletinDetailsDialog.getTitleToDisplay(versionUid, bulletinUid, mainWindow);
				String versionsData =  getHistoryItemData(i+1, dateSaved, title);
				historyItemLabels.add(new HistoryItem(versionsData, localId));
			}
			String currentVersionTitle = bulletinToShow.getFieldProperty(Bulletin.TAGTITLE).getValue();
			String currentVersionLastSaved = UiBulletinDetailsDialog.getSavedDateToDisplay(bulletinUid,bulletinUid, mainWindow);
			String versionsData =  getHistoryItemData(history.size() + 1, currentVersionLastSaved, currentVersionTitle);
			historyItemLabels.add(new HistoryItem(versionsData, bulletinUid.getLocalId()));
			historyItems.setItems(historyItemLabels);
		} 
		catch (TokenInvalidException e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	private String getHistoryItemData(int versionNumber, String dateSaved, String title) throws TokenInvalidException
	{
		HashMap tokenReplacement = new HashMap();
		tokenReplacement.put("#Title#", title);
		tokenReplacement.put("#DateSaved#", dateSaved);
		tokenReplacement.put("#VersionNumber#", Integer.toString(versionNumber));
		String historyItemTextWithTokens = getLocalization().getFieldLabel("HistoryVersion");
		return TokenReplacement.replaceTokens(historyItemTextWithTokens, tokenReplacement);
	}
	
	@Override
	public String getFxmlLocation()
	{
		return "landing/bulletins/BulletinEditorFooter.fxml";
	}

	@FXML
	private void onShowBulletinDetails(ActionEvent event) 
	{
		BulletinDetailsController details = new BulletinDetailsController(getMainWindow(), bulletin);
		showDialogWithClose("BulletinDetails", details);
	}
	
	@FXML
	private ComboBox historyItems;

	private ObservableList<HistoryItem> historyItemLabels;
	private FxBulletin bulletin;
}
