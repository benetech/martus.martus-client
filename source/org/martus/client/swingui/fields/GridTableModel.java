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
package org.martus.client.swingui.fields;

import java.io.IOException;

import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.ParserConfigurationException;

import org.martus.common.GridData;
import org.xml.sax.SAXException;


public class GridTableModel extends DefaultTableModel
{
	public GridTableModel(int columnCount)
	{
		super(0, columnCount);
		gridData = new GridData(columnCount);
	}
	
	public void addEmptyRow()
	{
		gridData.addEmptyRow();
		int newRowIndex = getRowCount()-1;
		this.fireTableRowsInserted(newRowIndex, newRowIndex);
	}
	
	public int getRowCount()
	{
		if(gridData == null)
			return 0;
		return gridData.getRowCount();
	}

	public Object getValueAt(int row, int column)
	{
		return gridData.getValueAt(row, column);
	}

	public void setValueAt(Object aValue, int row, int column)
	{
		gridData.setValueAt((String)aValue, row, column);
		fireTableCellUpdated(row,column);
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
	
	private GridData gridData;
}
