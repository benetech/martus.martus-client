/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2004, Beneficent
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

import java.io.IOException;

import javax.swing.table.AbstractTableModel;
import javax.xml.parsers.ParserConfigurationException;

import org.martus.common.GridData;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.xml.sax.SAXException;


public class GridTableModel extends AbstractTableModel
{
	public GridTableModel(GridFieldSpec fieldSpecToUse)
	{
		fieldSpec = fieldSpecToUse;
		gridData = new GridData(fieldSpec);
	}
	
	public int getColumnCount() 
	{
		int realDataColumnCount = gridData.getColumnCount();
		return realDataColumnCount + EXTRA_COLUMN;
	}

	public boolean isCellEditable(int row, int column)
	{
		if(column == 0)
			return false;
		return true;
	}
	
	public void addEmptyRow()
	{
		gridData.addEmptyRow();
		int newRowIndex = getRowCount()-1;
		fireTableRowsInserted(newRowIndex, newRowIndex);
	}
	
	public int getRowCount()
	{
		if(gridData == null)
			return 0;
		return gridData.getRowCount();
	}

	public String getColumnName(int column) 
	{
		if(column == 0)
			return fieldSpec.getColumnZeroLabel();
		return (String)fieldSpec.getAllColumnLabels().get(column - EXTRA_COLUMN);
	}
	
	public int getColumnType(int column) 
	{
		if(column == 0)
			return FieldSpec.TYPE_NORMAL;
		return fieldSpec.getColumnType(column - EXTRA_COLUMN);
	}

	public FieldSpec getFieldSpec(int column) 
	{
		if(column == 0)
			return new FieldSpec(FieldSpec.TYPE_NORMAL);
		return fieldSpec.getFieldSpec(column - EXTRA_COLUMN);
	}

	public Object getValueAt(int row, int column)
	{
		if(column == 0)
			return new Integer(row+1).toString();
		String value = gridData.getValueAt(row, column - EXTRA_COLUMN );
		if(getColumnType(column) == FieldSpec.TYPE_BOOLEAN)
		{
			if(value.equals(FieldSpec.TRUESTRING))
				return new Boolean(true);
			return new Boolean(false);
		}
		return value;
	}

	public void setValueAt(Object aValue, int row, int column)
	{
		if(column == 0)
			return;
		if(getColumnType(column) == FieldSpec.TYPE_BOOLEAN)
		{
			String selected = FieldSpec.FALSESTRING;
			if(((Boolean)aValue).booleanValue())
				selected = FieldSpec.TRUESTRING;
			gridData.setValueAt(selected, row, column - EXTRA_COLUMN);
		}
		else
			gridData.setValueAt((String)aValue, row, column - EXTRA_COLUMN);
		fireTableCellUpdated(row,column);
	}
	
	public GridData getGridData()
	{
		return gridData;
	}
	
	public String getXmlRepresentation()
	{
		return gridData.getXmlRepresentation();
	}
	
	public void setFromXml(String xmlText) throws IOException, ParserConfigurationException, SAXException
	{
		gridData.setFromXml(xmlText);
		fireTableDataChanged();
	}

	private int EXTRA_COLUMN = 1;
	private GridData gridData;
	private GridFieldSpec fieldSpec;
}
