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

import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.HQKey;
import org.martus.common.HQKeys;
import org.martus.common.bulletin.Bulletin;
import org.martus.swing.UiLabel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiTable;
import org.martus.swing.UiWrappedTextArea;
import org.martus.util.Base64.InvalidBase64Exception;

public class UiBulletinComponentHeadQuarters extends UiBulletinComponentSection
{
	public UiBulletinComponentHeadQuarters(UiMainWindow mainWindowToUse, String tagQualifierToUse, Bulletin bulletinToUse)
	{
		super(mainWindowToUse);
		bulletin = bulletinToUse;
		HQKeys hqKeys = bulletin.getAuthorizedToReadKeys();
		if(hqKeys.size() > 0)
		{
			String hqText = getLabel("HQInfoFor" + tagQualifierToUse); 
			JComponent hqInfo = createField(hqText);
			UiScrollPane hqScroller = createHeadquartersTable(hqKeys);

			add(hqInfo);
			addComponents(new UiLabel(getLabel("Headquarters")), hqScroller);
		}
		
	}

	private UiScrollPane createHeadquartersTable(HQKeys hqKeys)
	{
		DefaultTableModel hqModel = new DefaultTableModel();
		hqModel.addColumn(getLabel("HQLabel"));
		hqModel.addColumn(getLabel("HQPublicCode"));
		hqModel.setRowCount(hqKeys.size());
		
		for(int i=0; i < hqKeys.size(); ++i)
		{
			HQKey key = hqKeys.get(i);
			String publicCode = key.getPublicKey();
			try
			{
				publicCode = key.getPublicCode();
			}
			catch (InvalidBase64Exception e)
			{
				e.printStackTrace();
			}
			
			hqModel.setValueAt(mainWindow.getApp().getHQLabelIfPresent(key), i, 0);
			hqModel.setValueAt(publicCode, i, 1);
		}
		UiTable hqTable = new UiTable(hqModel);
		hqTable.setColumnSelectionAllowed(false);
		hqTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		hqTable.setShowGrid(true);
		hqTable.resizeTable();
		hqTable.setEnabled(false);
		UiScrollPane hqScroller = new UiScrollPane(hqTable);
		return hqScroller;
	}

	private String getLabel(String tag)
	{
		return getLocalization().getFieldLabel("BulletinDetails" + tag);
	}

	private JComponent createField(String text)
	{
		UiWrappedTextArea component = new UiWrappedTextArea(text);
		component.setEditable(false);
		return component;
	}

	Bulletin bulletin;
}
