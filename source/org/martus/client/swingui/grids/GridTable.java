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

package org.martus.client.swingui.grids;

import java.util.HashMap;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.martus.client.swingui.UiLocalization;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.swing.UiTableWithCellEditingProtection;

public class GridTable extends UiTableWithCellEditingProtection
{
	public GridTable(GridTableModel model, UiLocalization localizationToUse)
	{
		super(model);
		localization = localizationToUse;

		// NOTE: We need to keep renderers and editors separate, because otherwise
		// they get confused about focus when you click on a renderer but the 
		// editor is supposed to end up getting the click because they occupy 
		// the same screen location
		renderers = createEditorsOrRenderers();
		editors = createEditorsOrRenderers();
		
		setMaxColumnWidthToHeaderWidth(0);
		for(int i = 1 ; i < model.getColumnCount(); ++i)
			setColumnWidthToHeaderWidth(i);
		setAutoResizeMode(AUTO_RESIZE_OFF);
	}

	private HashMap createEditorsOrRenderers()
	{
		HashMap map = new HashMap();
		map.put(new Integer(FieldSpec.TYPE_BOOLEAN), new GridBooleanCellEditor());
		map.put(new Integer(FieldSpec.TYPE_DATE), new GridDateCellEditor(localization));
		map.put(new Integer(FieldSpec.TYPE_DROPDOWN), new GridDropDownCellEditor());
		map.put(new Integer(FieldSpec.TYPE_NORMAL), new GridNormalCellEditor(localization));
		return map;
	}
	
	FieldSpec getFieldSpecForColumn(int column)
	{
		return ((GridTableModel)getModel()).getFieldSpec(column);		
	}
	
	public void changeSelection(int rowIndex, int columnIndex,
			boolean toggle, boolean extend)
	{
		if(columnIndex == 0)
			columnIndex = 1;
		super.changeSelection(rowIndex, columnIndex, toggle, extend);
	}

	public TableCellEditor getCellEditor(int row, int column)
	{
		return (TableCellEditor)getCellEditorOrRenderer(editors, row, column);
	}
	
	public TableCellRenderer getCellRenderer(int row, int column)
	{
		return (TableCellRenderer)getCellEditorOrRenderer(renderers, row, column);
	}

	private Object getCellEditorOrRenderer(HashMap map, int row, int column)
	{
		GridTableModel model = (GridTableModel)getModel();
		Integer type = new Integer(model.getCellType(row, column));
		TableCellEditor editor = (TableCellEditor)map.get(type);
		if(editor == null)
		{
			System.out.println("GridTable.getCellEditorOrRenderer Unexpected type: " + type);
			editor = (TableCellEditor)map.get(new Integer(FieldSpec.TYPE_NORMAL));
		}
		return editor;
	}

	UiLocalization localization;
	HashMap renderers;
	HashMap editors;
}

