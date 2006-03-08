/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2006, Beneficent
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

import javax.swing.ListSelectionModel;
import org.martus.client.swingui.HeadQuartersTableModel;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.bulletin.Bulletin;
import org.martus.swing.UiLabel;
import org.martus.swing.UiTable;
import org.martus.swing.UiWrappedTextArea;

abstract public class UiBulletinComponentHeadQuartersSection extends UiBulletinComponentSection
{
	public UiBulletinComponentHeadQuartersSection(UiMainWindow mainWindowToUse, Bulletin bulletinToUse, String tagQualifierToUse, int widthToUse)
	{
		super(mainWindowToUse);
		bulletin = bulletinToUse;
		
		String hqText = getLabel("HQInfoFor" + tagQualifierToUse); 
		UiWrappedTextArea hqInfo = new UiWrappedTextArea(hqText, widthToUse);
		hqInfo.setEditable(false);
		addComponents(new UiLabel(""),hqInfo);
	}

	protected String getLabel(String tag)
	{
		return getLocalization().getFieldLabel("BulletinHeadQuarters" + tag);
	}
	
	protected UiTable createHeadquartersTable(HeadQuartersTableModel hqModel) 
	{
		UiTable hqTable = new UiTable(hqModel);
		hqTable.setRenderers(hqModel);
		hqTable.createDefaultColumnsFromModel();
		hqTable.setMaxGridWidth(40);
		hqTable.useMaxWidth();
		hqTable.setColumnSelectionAllowed(false);
		hqTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		hqTable.setShowGrid(true);
		hqTable.resizeTable();
		return hqTable;
	}

	abstract public void copyDataToBulletin(Bulletin bulletinToCopyInto);

	Bulletin bulletin;
	HeadQuartersTableModel tableModel;

}
