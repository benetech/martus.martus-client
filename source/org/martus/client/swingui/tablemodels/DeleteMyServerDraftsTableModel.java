/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2003, Beneficent
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

package org.martus.client.swingui.tablemodels;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.dialogs.UiProgressRetrieveSummariesDlg;
import org.martus.common.MartusUtilities.ServerErrorException;
import org.martus.common.bulletin.Bulletin;

public class DeleteMyServerDraftsTableModel extends RetrieveTableModelNonHQ
{

	public DeleteMyServerDraftsTableModel(MartusApp appToUse, UiLocalization localizationToUse)
	{
		super(appToUse, localizationToUse);
	}

	public void initialize(UiProgressRetrieveSummariesDlg progressDlg) throws ServerErrorException
	{
		setProgressDialog(progressDlg);
		getMyDraftSummaries();
		setCurrentSummaries();
	}

	public String getColumnName(int column)
	{
		if(column == 0)
			return getLocalization().getFieldLabel("DeleteFlag");
		if(column == 1)
			return getLocalization().getFieldLabel(Bulletin.TAGTITLE);
		return getLocalization().getFieldLabel("BulletinSize");
	}
}
