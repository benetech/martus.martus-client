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

package org.martus.client.search;

import java.awt.Dimension;
import javax.swing.JTable;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.client.swingui.fields.UiGridEditor;
import org.martus.client.swingui.grids.GridDropDownCellEditor;
import org.martus.client.swingui.grids.GridTable;
import org.martus.swing.UiComboBox;
import org.martus.swing.Utilities;

public class FancySearchGridEditor extends UiGridEditor
{
	public static FancySearchGridEditor create(ClientBulletinStore storeToSearch, UiDialogLauncher dlgLauncher)
	{
		FancySearchHelper helper = new FancySearchHelper(storeToSearch, dlgLauncher);
		FancySearchGridEditor gridEditor = new FancySearchGridEditor(helper);
		gridEditor.initalize();
		return gridEditor;
	}
	
	private FancySearchGridEditor(FancySearchHelper helperToUse)
	{
		super(helperToUse.getModel(), helperToUse.getDialogLauncher());
		helper = helperToUse;
		getTable().setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		setSearchForColumnWideEnoughForDates();
		setGridTableSize();
		addListenerSoFieldChangeCanTriggerRepaintOfValueColumn();
	}

	private void setGridTableSize()
	{
		Dimension searchGridSize = Utilities.getViewableScreenSize();
		searchGridSize.setSize(searchGridSize.getWidth() * 0.9, 200);
		getComponent().setPreferredSize(searchGridSize);
	}

	private void setSearchForColumnWideEnoughForDates()
	{
		GridTable searchTable = getTable();
		int searchForColumn = FancySearchHelper.COLUMN_VALUE;
		int widthToHoldDates = searchTable.getDateColumnWidth();
		searchTable.setColumnWidth(searchForColumn, widthToHoldDates);
	}

	private GridDropDownCellEditor getFieldColumnEditor()
	{
		int column = FancySearchTableModel.fieldColumn;
		return (GridDropDownCellEditor)getTable().getCellEditor(0, column);
	}
	
	public String getSearchString()
	{
		return helper.getSearchString(getGridData());		
	}

	private void addListenerSoFieldChangeCanTriggerRepaintOfValueColumn()
	{
		UiComboBox fieldChoiceCombo = (UiComboBox)getFieldColumnEditor().getComponent();
		fieldChoiceCombo.addPopupMenuListener(new DropDownPopUpListener());
	}

	class DropDownPopUpListener implements PopupMenuListener
	{
		public void popupMenuCanceled(PopupMenuEvent event)
		{
		}
	
		public void popupMenuWillBecomeInvisible(PopupMenuEvent event)
		{
			//System.out.println("will become invisible: " + ((JComboBox)event.getSource()).getSelectedIndex());
			stopCellEditing();
		}
	
		public void popupMenuWillBecomeVisible(PopupMenuEvent event)
		{
		}
	}

	FancySearchHelper helper;
}
