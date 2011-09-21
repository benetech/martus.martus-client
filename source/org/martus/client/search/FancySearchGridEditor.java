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

package org.martus.client.search;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTable;

import org.json.JSONObject;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.client.swingui.fields.UiEditableGrid;
import org.martus.client.swingui.fields.UiFieldContext;
import org.martus.client.swingui.fields.UiPopUpFieldChooserEditor;
import org.martus.client.swingui.grids.GridPopUpTreeCellEditor;
import org.martus.client.swingui.grids.GridTable;
import org.martus.client.swingui.grids.SearchGridTable;
import org.martus.common.FieldSpecCollection;
import org.martus.swing.Utilities;

public class FancySearchGridEditor extends UiEditableGrid
{
	public static FancySearchGridEditor create(UiMainWindow mainWindowToUse, UiDialogLauncher dlgLauncher)
	{
		ClientBulletinStore store = mainWindowToUse.getStore();
		FancySearchHelper helper = new FancySearchHelper(store, dlgLauncher);
		UiFieldContext contextToUse = new UiFieldContext();
		FieldSpecCollection allSpecs = new FieldSpecCollection();
		allSpecs.addAllSpecs(store.getAllKnownFieldSpecs());
		allSpecs.addAllReusableChoicesLists(store.getAllReusableChoiceLists());
		contextToUse.setSectionFieldSpecs(allSpecs);
		FancySearchGridEditor gridEditor = new FancySearchGridEditor(mainWindowToUse, helper, contextToUse);
		gridEditor.initalize();
		return gridEditor;
	}
	
	private FancySearchGridEditor(UiMainWindow mainWindowToUse, FancySearchHelper helperToUse, UiFieldContext contextToUse)
	{
		super(mainWindowToUse, contextToUse, helperToUse.getModel(), helperToUse.getDialogLauncher(), NUMBER_OF_COLUMNS_FOR_GRID);
		mainWindow = mainWindowToUse;
		helper = helperToUse;
		getTable().setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		setSearchForColumnWideEnoughForDates();
		setGridTableSize();
		addListenerSoFieldChangeCanTriggerRepaintOfValueColumn();
	}
	
	public UiMainWindow getMainWindow()
	{
		return mainWindow;
	}
	
	protected GridTable createGridTable(UiDialogLauncher dlgLauncher, UiFieldContext context)
	{
		return new SearchGridTable(getMainWindow(), getFancySearchTableModel(), dlgLauncher, context);
	}
	
	private FancySearchTableModel getFancySearchTableModel()
	{
		return (FancySearchTableModel) getGridTableModel();
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
		int widthToHoldDates = searchTable.getDateColumnWidth(searchForColumn);
		searchTable.setColumnWidth(searchForColumn, widthToHoldDates);
	}

	private GridPopUpTreeCellEditor getFieldColumnEditor()
	{
		int column = FancySearchTableModel.fieldColumn;
		return (GridPopUpTreeCellEditor)getTable().getCellEditor(0, column);
	}
	
	public void setFromJson(JSONObject json)
	{
		helper.setSearchFromJson(getGridData(), json);
		getFancySearchTableModel().updateAllDataDrivenDropdownChoices();
	}
	
	public JSONObject getSearchAsJson() throws Exception
	{
		return helper.getSearchAsJson(getGridData());
	}
	
	public SearchTreeNode getSearchTree()
	{
		return helper.getSearchTree(getGridData());		
	}

	private void addListenerSoFieldChangeCanTriggerRepaintOfValueColumn()
	{
		UiPopUpFieldChooserEditor fieldChoiceEditor = getFieldColumnEditor().getPopUpTreeEditor();
		fieldChoiceEditor.addActionListener(new PopUpActionHandler());
	}

	class PopUpActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			stopCellEditing();
		}
	}

	private static final int NUMBER_OF_COLUMNS_FOR_GRID = 80;

	UiMainWindow mainWindow;
	FancySearchHelper helper;
}
