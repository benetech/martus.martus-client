/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2005, Beneficent
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
package org.martus.client.swingui.fields;

import java.awt.Color;
import java.awt.Component;
import java.io.IOException;
import java.io.NotSerializableException;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.martus.client.swingui.tablemodels.GridTableModel;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiTable;
import org.martus.swing.UiTableWithCellEditingProtection;
import org.martus.swing.UiTextField;


public class UiGrid extends UiField
{
	public UiGrid(GridFieldSpec fieldSpec)
	{
		super();
		model = new GridTableModel(fieldSpec);
		table = new GridTable(model);
		table.setColumnSelectionAllowed(false);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setShowGrid(true);
		table.changeSelection(0, 1, false, false);
		table.setRowHeight(table.getRowHeight() + ROW_HEIGHT_PADDING);
		widget = new UiScrollPane(table);
	}	
	
	class GridTable extends UiTableWithCellEditingProtection
	{
		public GridTable(GridTableModel model)
		{
			super(model);
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
						UiTextField uiTextField = new UiTextField();
						uiTextField.setBorder(new LineBorder(Color.BLUE));
						tableColumn.setCellEditor(new GridTableCellEditor(uiTextField)); 
						tableColumn.setCellRenderer(new GridNormalCellRenderer());
						break;
						
					case FieldSpec.TYPE_DROPDOWN:
						UiChoiceEditor uiChoiceField = new UiChoiceEditor((model.getFieldSpec(i)));
						tableColumn.setCellEditor(new GridTableCellEditor(uiChoiceField)); 
						tableColumn.setCellRenderer(new GridNormalCellRenderer());
						break;
				}
			}
		}
		
		public TableCellRenderer getCellRenderer(int row, int column)
		{
			return getColumnModel().getColumn(column).getCellRenderer();
		}
		
		public void changeSelection(int rowIndex, int columnIndex,
				boolean toggle, boolean extend)
		{
			if(columnIndex == 0)
				columnIndex = 1;
			super.changeSelection(rowIndex, columnIndex, toggle, extend);
		}

		// This class is NOT intended to be serialized!!!
		private static final long serialVersionUID = 1;
		private void writeObject(java.io.ObjectOutputStream stream) throws IOException
		{
			throw new NotSerializableException();
		}

	}
	
	class GridTableCellEditor extends DefaultCellEditor
	{
		public GridTableCellEditor(UiTextField textField)
		{
			super(textField);
		}
		
		public GridTableCellEditor(UiChoiceEditor choiceEditor)
		{
			super((JComboBox)choiceEditor.getComponent());
		}
		
		public GridTableCellEditor(UiBoolEditor booleanEditor)
		{
			super((JCheckBox)booleanEditor.getComponent());
		}

		private static final long serialVersionUID = 1;

	}

	
	class GridNormalCellRenderer implements TableCellRenderer
	{
		public Component getTableCellRendererComponent(JTable tableToUse, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			UiTextField cell = new UiTextField((String)value);
			cell.setBorder(new EmptyBorder(0,0,0,0));
			if(column == 0)
			{
				cell.setBackground(Color.LIGHT_GRAY);
				cell.setForeground(Color.BLACK);
				return cell;
			}
			
			if(hasFocus)
			{
				cell.setBorder(new LineBorder(Color.BLACK,1));
			}
			return cell;
		}
	}

	
	public JComponent getComponent()
	{
		return widget;
	}
	
	public String getText()
	{
		return model.getXmlRepresentation();
	}

	public void setText(String newText)
	{
		try
		{
			model.setFromXml(newText);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static final int ROW_HEIGHT_PADDING = 3;

	UiScrollPane widget;
	UiTable table;
	GridTableModel model;
}
