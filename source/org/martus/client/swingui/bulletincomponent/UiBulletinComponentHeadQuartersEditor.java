/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005, Beneficent
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
package org.martus.client.swingui.bulletincomponent;

import java.util.Vector;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.HQKey;
import org.martus.common.HQKeys;
import org.martus.common.bulletin.Bulletin;
import org.martus.swing.UiLabel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiTable;

public class UiBulletinComponentHeadQuartersEditor extends UiBulletinComponentHeadQuartersSection
{
	public UiBulletinComponentHeadQuartersEditor(HeadQuartersSelectionListener hqSelectionListener, UiMainWindow mainWindowToUse, Bulletin bulletinToUse, String tagQualifierToUse)
	{
		super(mainWindowToUse, bulletinToUse, tagQualifierToUse);
		UiLabel hqLabel = new UiLabel(getLabel("Headquarters"));

		HQKeys authorizedToReadKeys = bulletinToUse.getAuthorizedToReadKeys();
		HQKeys allHQKeysConfigured = mainWindow.getApp().getHQKeysWithFallback();
		int numberOfHQKeysConfigured = allHQKeysConfigured.size();
		int numberOfHQKeysAuthorizedToRead = authorizedToReadKeys.size();
		
		if(numberOfHQKeysConfigured == 0 && numberOfHQKeysAuthorizedToRead == 0 )
		{
			addComponents(hqLabel, new UiLabel(getLocalization().getFieldLabel("NoHQsConfigured")));
			return;
		}
		
		tableModel = new HeadQuartersTableModelEdit(getLocalization());
		Vector autorizedKeys = new Vector();
		MartusApp app = mainWindow.getApp();
		app.setHQLabelsIfPresent(authorizedToReadKeys);
		for (int i = 0; i < authorizedToReadKeys.size(); ++i) 
		{
			HQKey hqKeyToAddAuthorized = authorizedToReadKeys.get(i);
			HeadQuarterEntry headQuarterEntry = new HeadQuarterEntry(hqKeyToAddAuthorized);
			headQuarterEntry.setSelected(true);
			tableModel.addNewHeadQuarterEntry(headQuarterEntry);
			autorizedKeys.add(hqKeyToAddAuthorized.getPublicKey());
		}
		
		for(int j = 0; j < numberOfHQKeysConfigured; ++j)
		{
			HQKey hqKeyToCheck = allHQKeysConfigured.get(j);
			if(!autorizedKeys.contains(hqKeyToCheck.getPublicKey()))
			{
				HeadQuarterEntry headQuarterEntry = new HeadQuarterEntry(hqKeyToCheck);
				tableModel.addNewHeadQuarterEntry(headQuarterEntry);
			}
		}
		
		tableModel.setHQSelectionListener(hqSelectionListener);

		UiTable hqTable = createHeadquartersTable(tableModel);
		hqTable.setMaxColumnWidthToHeaderWidth(0);
		UiScrollPane hqScroller = new UiScrollPane(hqTable);

		addComponents(hqLabel, hqScroller);
	}

	public void copyDataToBulletin(Bulletin bulletinToCopyInto) 
	{
		if(tableModel == null)
			return;
		bulletinToCopyInto.setAuthorizedToReadKeys(tableModel.getAllSelectedHeadQuarterKeys());
	}
	
}
