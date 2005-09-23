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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.martus.client.swingui.tablemodels.UiTableModel;

public class UiRenderer extends DefaultTableCellRenderer
{
	public UiRenderer(UiTableModel modelToUse, Color disabledBackgroundColorToUse,TableCellRenderer oldRendererToUse)
	{
		tableModel = modelToUse;
		oldRenderer = oldRendererToUse;
		disabledBackgroundColor = disabledBackgroundColorToUse;
	}

	public Component getTableCellRendererComponent(
			JTable tableToUse, Object value,
			boolean isSelected, boolean hasFocus,
			int row, int column)
	{
		Component cell = oldRenderer.getTableCellRendererComponent(tableToUse, value, isSelected, hasFocus, row, column);
		if(normalBackgroundColor == null)
			normalBackgroundColor = cell.getBackground();
		boolean isEnabled = tableModel.isEnabled(row);
		cell.setEnabled(isEnabled);
		if(!isSelected)
		{
			if(isEnabled)
				cell.setBackground(normalBackgroundColor);
			else
				cell.setBackground(disabledBackgroundColor);
		}			
		return cell;
	}

	Color normalBackgroundColor;
	Color disabledBackgroundColor;
	UiTableModel tableModel;
	TableCellRenderer oldRenderer;
}
