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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JScrollPane;

public class UiGridEditor extends UiGrid
{
	public UiGridEditor()
	{
		super(3);
		table.resizeTable(5);
		table.addKeyListener(new GridKeyListener());
		widget.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		widget.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	}
	
	class GridKeyListener implements KeyListener
	{
		public void keyPressed(KeyEvent e)
		{
			if(e.getKeyCode() ==  KeyEvent.VK_TAB && !e.isControlDown())
			{
				if(e.isShiftDown())
				{
					if(table.getSelectedRow()==0 && table.getSelectedColumn() == 0)
					{
						e.consume();
						table.transferFocusBackward();
					}
				}
				else if(table.getSelectedRow()== table.getRowCount()-1)
				{
					if(table.getSelectedColumn() == table.getColumnCount() -1)
					{
						e.consume();
						table.transferFocus();
					}
				}
			}
		}

		public void keyReleased(KeyEvent e)
		{
		}

		public void keyTyped(KeyEvent e)
		{
			if(e.getKeyChar()==KeyEvent.VK_ENTER)
			{
				model.addEmptyRow();
				table.changeSelection(table.getRowCount()-1,0,false,false);
			}
		}
	}
}
