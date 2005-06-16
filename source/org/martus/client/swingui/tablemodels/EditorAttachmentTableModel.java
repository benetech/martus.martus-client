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
package org.martus.client.swingui.tablemodels;

import java.io.IOException;
import java.io.NotSerializableException;
import javax.swing.JButton;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.swing.UiTable;

public class EditorAttachmentTableModel extends AttachmentTableModel
{
	public EditorAttachmentTableModel(UiMainWindow window, UiTable table, JButton remove)
	{
		super(window, table);
		this.remove = remove;
	}

	public void clear()
	{
		super.clear();
		remove.setEnabled(false);
	}

	public void add(AttachmentProxy a)
	{
		super.add(a);
		remove.setEnabled(true);
	}
	
	public void remove(int row)
	{
		super.remove(row);
		if(getRowCount() == 0)
			remove.setEnabled(false);
	}

	// This class is NOT intended to be serialized!!!
	private static final long serialVersionUID = 1;
	private void writeObject(java.io.ObjectOutputStream stream) throws IOException
	{
		throw new NotSerializableException();
	}
	JButton remove;
}
