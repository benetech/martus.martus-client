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
package org.martus.client.swingui.renderers;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;
import org.martus.swing.UiCheckBox;

public class GridBooleanCellRenderer implements TableCellRenderer
{
	public Component getTableCellRendererComponent(JTable tableToUse, Object booleanValue, boolean isSelected, boolean hasFocus, int row, int column)
	{
		UiCheckBox cell = new UiCheckBox();
		cell.setSelected(((Boolean)booleanValue).booleanValue());
		cell.setBorder(new EmptyBorder(0,0,0,0));
		cell.setBorderPainted(true);
		cell.setBackground(tableToUse.getBackground());
		cell.setForeground(tableToUse.getForeground());
		if(hasFocus)
			cell.setBorder(new LineBorder(Color.BLACK,1));
		return cell;
	}
}
