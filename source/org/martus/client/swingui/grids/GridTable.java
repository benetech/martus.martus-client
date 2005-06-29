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

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.fields.UiDateEditor;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.swing.UiTableWithCellEditingProtection;

public class GridTable extends UiTableWithCellEditingProtection
{
	public GridTable(GridTableModel model, UiLocalization localizationToUse)
	{
		super(model);
		localization = localizationToUse;

		stringRenderer = new GridNormalCellRenderer(localization);
		dateRenderer = new GridTableDateRenderer(localization);
		booleanRenderer = new GridBooleanCellRenderer();
		
		stringEditor = new GridNormalCellEditor(localization);
		dateEditor = new GridTableDateEditor(localization);
		booleanEditor = new GridBooleanCellEditor();
		
		setMaxColumnWidthToHeaderWidth(0);
		for(int i = 1 ; i < model.getColumnCount(); ++i)
			setColumnWidthToHeaderWidth(i);
		setAutoResizeMode(AUTO_RESIZE_OFF);
		for(int i = 0 ; i < model.getColumnCount(); ++i)
		{
			TableColumn tableColumn = getColumnModel().getColumn(i);
			FieldSpec columnSpec = getFieldSpecForColumn(i);
			switch(columnSpec.getType())
			{
				case FieldSpec.TYPE_NORMAL:
					tableColumn.setCellEditor(stringEditor); 
					tableColumn.setCellRenderer(stringRenderer);
					break;
					
				case FieldSpec.TYPE_DROPDOWN:
					tableColumn.setCellEditor(new GridDropDownCellEditor((DropDownFieldSpec)columnSpec)); 
					tableColumn.setCellRenderer(new GridDropDownCellRenderer((DropDownFieldSpec)columnSpec));
					break;

				case FieldSpec.TYPE_BOOLEAN:
					tableColumn.setCellEditor(booleanEditor); 
					tableColumn.setCellRenderer(booleanRenderer);
					break;
					
				case FieldSpec.TYPE_MORPHIC:
					// morphic column models delegate renderer/editor selection back to us 
					break;
			}
		}
		
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
		GridTableModel model = (GridTableModel)getModel();
		if((model).getColumnType(column) != FieldSpec.TYPE_MORPHIC)
			return super.getCellEditor(row, column);

		int type = model.getCellType(row, column);
		switch(type)
		{
			case FieldSpec.TYPE_NORMAL:
				return stringEditor;
			case FieldSpec.TYPE_DATE:
				return dateEditor;
			default:
				System.out.println("GridTable.getCellEditor Unexpected type: " + type);
				return stringEditor;
		}
	}

	public TableCellRenderer getCellRenderer(int row, int column)
	{
		GridTableModel model = (GridTableModel)getModel();
		if((model).getColumnType(column) != FieldSpec.TYPE_MORPHIC)
			return super.getCellRenderer(row, column);

		int type = model.getCellType(row, column);
		switch(type)
		{
			case FieldSpec.TYPE_NORMAL:
				return stringRenderer;
			case FieldSpec.TYPE_DATE:
				return dateRenderer;
			default:
				System.out.println("GridTable.getCellEditor Unexpected type: " + type);
				return stringRenderer;
		}
	}

	UiLocalization localization;

	TableCellRenderer stringRenderer;
	TableCellRenderer dateRenderer;
	TableCellRenderer booleanRenderer;
	
	TableCellEditor stringEditor;
	TableCellEditor dateEditor;
	TableCellEditor booleanEditor;

}

class GridTableDateRenderer implements TableCellRenderer
{
	GridTableDateRenderer(UiLocalization localization)
	{
		renderer = new UiDateEditor(localization, null);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean arg2, boolean arg3, int row, int column)
	{
		renderer.setText((String)value);
		return renderer.getComponent();
	}

	UiDateEditor renderer;
}

class GridTableDateEditor extends AbstractCellEditor implements TableCellEditor
{
	GridTableDateEditor(UiLocalization localization)
	{
		editor = new UiDateEditor(localization, null);
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		return editor.getComponent();
	}

	public Object getCellEditorValue()
	{
		return editor.getText();
	}
	
	UiDateEditor editor;
}
