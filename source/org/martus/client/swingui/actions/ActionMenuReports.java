/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006, Beneficent
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

import java.awt.event.ActionEvent;
import java.util.Set;
import java.util.Vector;

import org.martus.client.swingui.UiMainWindow;
import org.martus.common.packet.UniversalId;

public class ActionMenuReports extends ActionPrint
{
	public ActionMenuReports(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse, "Reports");
	}

	public boolean isEnabled()
	{
		return true;
	}

	public void actionPerformed(ActionEvent ae)
	{
		Set bulletinIdsFromSearch = mainWindow.doSearch();
		if(bulletinIdsFromSearch == null)
			return;
		int bulletinsMatched = bulletinIdsFromSearch.size();
		if(bulletinIdsFromSearch.size() == 0)
		{
			mainWindow.notifyDlg("SearchFailed");
			return;
		}
		mainWindow.showNumberOfBulletinsFound(bulletinsMatched, "ReportFound");
		UniversalId[] bulletinIds = (UniversalId[])bulletinIdsFromSearch.toArray(new UniversalId[0]);
		Vector bulletinsToReportOn = mainWindow.getBulletins(bulletinIds);
		printBulletins(bulletinsToReportOn);
	}

}
