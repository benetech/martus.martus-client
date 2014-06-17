/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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

import java.awt.Container;
import java.awt.event.ActionEvent;

import org.martus.client.core.SortableBulletinList;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.FxContentController;
import org.martus.client.swingui.jfx.landing.BulletinsListController;
import org.martus.client.swingui.jfx.landing.FxMainStage;
import org.martus.clientside.CurrentUiState;
import org.martus.common.MartusLogger;

public class ActionMenuSearch extends UiMenuAction implements ActionDoer
{
	public ActionMenuSearch(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse, "search");
		searchString = null;
	}

	public ActionMenuSearch(UiMainWindow mainWindowToUse, String simpleSearch)
	{
		super(mainWindowToUse, "search");
		CurrentUiState uiState = getMainWindow().getUiState();
		uiState.setSearchFinalBulletinsOnly(true);
		searchString = simpleSearch;
	}

	public void actionPerformed(ActionEvent ae)
	{
		doAction();
	}

	@Override
	public void doAction()
	{
		if(searchString == null)
		{
			SortableBulletinList bulletinIdsFromSearch = doSearch();
			mainWindow.updateSearchFolderAndNotifyUserOfTheResults(bulletinIdsFromSearch);
			return;
		}
		SortableBulletinList bulletinIdsFromSearch = doSearch(searchString);
		FxMainStage stage = mainWindow.getMainStage();
		if (stage != null)
		{
			try
			{
				BulletinsListController controller = (BulletinsListController)stage.getCurrentController();
				controller.updateSearchResultsTable(bulletinIdsFromSearch);
			} 
			catch (Exception e)
			{
				MartusLogger.logException(e);
			}
		}
	}

	private String searchString;
}