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
package org.martus.client.swingui.actions;

import javax.swing.JDialog;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.SortableBulletinList;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.UiSession;
import org.martus.client.swingui.foldertree.UiFolderTreePane;
import org.martus.client.swingui.jfx.landing.FxMainStage;
import org.martus.client.swingui.jfx.landing.bulletins.BulletinsListController;
import org.martus.common.EnglishCommonStrings;
import org.martus.swing.UiOptionPane;
import org.martus.util.TokenReplacement;

public class ActionMenuSearch extends ActionSearch
{
	public ActionMenuSearch(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	@Override
	public void doAction()
	{
		SortableBulletinList bulletinIdsFromSearch = doSearch();
		if(UiSession.isJavaFx())
		{
			FxMainStage stage = mainWindow.getMainStage();
			BulletinsListController controller = stage.getBulletinsListController();
			controller.updateSearchResultsTable(bulletinIdsFromSearch);
		}
		else
		{
			showSearchResults(bulletinIdsFromSearch);
		}
	}

	public void showSearchResults(SortableBulletinList bulletinIdsFromSearch)
	{
		updateSearchFolderAndNotifyUserOfTheResults(bulletinIdsFromSearch);
	}	

	public void updateSearchFolderAndNotifyUserOfTheResults(SortableBulletinList matchedBulletinsFromSearch)
	{
		if(matchedBulletinsFromSearch == null)
			return;
		getApp().updateSearchFolder(matchedBulletinsFromSearch);
		ClientBulletinStore store = getStore();
		BulletinFolder searchFolder = store.findFolder(store.getSearchFolderName());
		UiFolderTreePane folderTreePane = getFolderTreePane();
		if(folderTreePane == null)
			return;
		folderTreePane.folderTreeContentsHaveChanged();
		folderTreePane.folderContentsHaveChanged(searchFolder);
		int bulletinsFound = searchFolder.getBulletinCount();
		if(bulletinsFound > 0)
		{
			getMainWindow().selectSearchFolder();
			showNumberOfBulletinsFound(bulletinsFound, "SearchFound");
		}
		else
		{
			getMainWindow().notifyDlg("SearchFailed");
		}
	}

	public void showNumberOfBulletinsFound(int bulletinsFound,String messageTag)
	{
		try
		{
			String title = getLocalization().getWindowTitle("notifySearchFound");
			String message = getLocalization().getFieldLabel(messageTag);
			String ok = getLocalization().getButtonLabel(EnglishCommonStrings.OK);
			String[] buttons = { ok };
			message = TokenReplacement.replaceToken(message , "#NumberBulletinsFound#", (new Integer(bulletinsFound)).toString());
			UiOptionPane pane = new UiOptionPane(message, UiOptionPane.INFORMATION_MESSAGE, UiOptionPane.DEFAULT_OPTION,
									null, buttons);
			JDialog dialog = pane.createDialog(getMainWindow().getSwingFrame(), title);
			dialog.setVisible(true);
		}
		catch(Exception e)
		{
			getMainWindow().unexpectedErrorDlg(e);
		}
	}

}
