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

import java.awt.Color;
import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import org.martus.client.swingui.fields.UiBoolEditor;
import org.martus.client.swingui.fields.UiChoiceEditor;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.swing.UiTableWithCellEditingProtection;
import org.martus.swing.UiTextField;

public class GridTable extends UiTableWithCellEditingProtection
{
	public GridTable(GridTableModel model)
	{
		super(model);
		
		stringEditor = createStringEditor();
		booleanEditor = createBooleanEditor();
		
		setMaxColumnWidthToHeaderWidth(0);
		for(int i = 1 ; i < model.getColumnCount(); ++i)
			setColumnWidthToHeaderWidth(i);
		setAutoResizeMode(AUTO_RESIZE_OFF);
		for(int i = 0 ; i < model.getColumnCount(); ++i)
		{
			TableColumn tableColumn = getColumnModel().getColumn(i);
			switch(model.getColumnType(i))
			{
				case FieldSpec.TYPE_NORMAL:
					tableColumn.setCellEditor(stringEditor); 
					tableColumn.setCellRenderer(new GridNormalCellRenderer());
					break;
					
				case FieldSpec.TYPE_DROPDOWN:
					DropDownFieldSpec dropDownFieldSpec = (DropDownFieldSpec)model.getFieldSpec(i);
					tableColumn.setCellEditor(createChoiceEditor(dropDownFieldSpec)); 
					tableColumn.setCellRenderer(new GridDropDownCellRenderer(dropDownFieldSpec));
					break;

				case FieldSpec.TYPE_BOOLEAN:
					tableColumn.setCellEditor(booleanEditor); 
					tableColumn.setCellRenderer(new GridBooleanCellRenderer());
					break;
				case FieldSpec.TYPE_MORPHIC:
					// morphic column models delegate renderer/editor selection back to us 
					break;
			}
		}
		
		stringEditor = new GridTableStringCellEditor();
	}

	private GridTableCellEditor createBooleanEditor()
	{
		UiBoolEditor uiBooleanField = new UiBoolEditor();
		GridTableCellEditor editor = new GridTableCellEditor(uiBooleanField);
		return editor;
	}

	private GridTableCellEditor createChoiceEditor(DropDownFieldSpec dropDownFieldSpec)
	{
		UiChoiceEditor uiChoiceField = new UiChoiceEditor(dropDownFieldSpec);
		GridTableCellEditor editor = new GridTableCellEditor(uiChoiceField);
		return editor;
	}

	private GridTableCellEditor createStringEditor()
	{
		UiTextField uiTextField = new UiTextField();
		uiTextField.setBorder(new LineBorder(Color.BLUE));
		GridTableCellEditor editor = new GridTableCellEditor(uiTextField);
		return editor;
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
			default:
				System.out.println("GridTable.getCellEditor Unexpected type: " + type);
				return stringEditor;
		}
	}

	TableCellEditor stringEditor;
	TableCellEditor booleanEditor;


}

class GridTableStringCellEditor extends AbstractCellEditor implements TableCellEditor
{
	GridTableStringCellEditor()
	{
		editor = new UiTextField();
	}

	public Object getCellEditorValue()
	{
		return editor.getText();
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		editor.setBackground(Color.RED);
		editor.setForeground(Color.YELLOW);
		editor.setText((String)value);
		return editor;
	}
	
	UiTextField editor;
}
