/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2003, Beneficent
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

import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.martus.client.swingui.UiMainWindow;
import org.martus.common.bulletin.AttachmentProxy;


class AttachmentTableModel extends AbstractTableModel
{
	public AttachmentTableModel(UiMainWindow window, JTable table)
	{
		attachmentList = new Vector();
		mainWindow = window;
		attachmentTable = table;
	}

	public int getRowCount()
	{
		return attachmentList.size();
	}

	public int getColumnCount()
	{
		return 1;
	}

	void clear()
	{
		attachmentList.clear();
		fireTableDataChanged();
	}

	public void add(AttachmentProxy a)
	{
		attachmentList.add(a);
		fireTableDataChanged();
	}

	public void remove(int row)
	{
		attachmentList.remove(row);
		fireTableDataChanged();
	}

	public String getColumnName(int column)
	{
		return mainWindow.getLocalization().getButtonLabel("attachmentlabel");
	}

	public AttachmentProxy getAttachmentProxyAt(int row, int column)
	{
		return (AttachmentProxy)attachmentList.get(row);
	}
		
	public AttachmentProxy[] getSelectedAttachments()
	{
		int[] rows = attachmentTable.getSelectedRows();
		if(rows.length <= 0)
			return null;
		AttachmentProxy[] list = new AttachmentProxy[rows.length];
		for(int i = 0; i < rows.length; ++i)
			list[i] = (AttachmentProxy)attachmentList.get(rows[i]);
		return list;
	}

	public AttachmentProxy[] getAttachments()
	{
		AttachmentProxy[] list = new AttachmentProxy[attachmentList.size()];
		for(int i = 0; i < list.length; ++i)
			list[i] = (AttachmentProxy)attachmentList.get(i);
	
		return list;
	}

	public Object getValueAt(int row, int column)
	{
		AttachmentProxy a = (AttachmentProxy)attachmentList.get(row);
		return a.getLabel();
	}

	public void setValueAt(Object value, int row, int column)
	{
	}

	public boolean isCellEditable(int row, int column)
	{
		return false;
	}

	
	Vector attachmentList;
	UiMainWindow mainWindow;
	JTable attachmentTable;
}
