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

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.martus.swing.UiTable;


public class UiGridEditor extends UiField
{
	public UiGridEditor()
	{
		super();
		GridTableModel model = new GridTableModel(5,3);
		table = new UiTable(model);
		table.resizeTable();

		widget = new JScrollPane(table);
		widget.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		widget.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	}

	public JComponent getComponent()
	{
		return widget;
	}
	
	public String getText()
	{
		return widget.toString();
	}
	
	public void setText(String newText)
	{
	}
	
	JScrollPane widget;
	UiTable table;
}
