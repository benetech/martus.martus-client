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

import javax.swing.JComponent;
import javax.swing.JTable;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.fields.UiSingleLineTextEditor;

class GridNormalCellEditor extends GridCellEditorAndRenderer
{
	GridNormalCellEditor(MartusLocalization localization)
	{
		super(new UiSingleLineTextEditor(localization));

		// this code should go away when the first grid column becomes a TYPE_MESSAGE 
		normalForeground = uiField.getComponent().getForeground();
		normalBackground = uiField.getComponent().getBackground();
		// end code that should go away

	}
	
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		setColors(column);
		return super.getTableCellEditorComponent(table, value, isSelected, row, column);
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		setColors(column);
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}

	private void setColors(int column)
	{
		// this code should go away when the first grid column becomes a TYPE_MESSAGE 
		JComponent component = uiField.getComponent();
		Color fg = normalForeground;
		Color bg = normalBackground;
		if(column == 0)
		{
			fg = Color.BLACK;
			bg = Color.LIGHT_GRAY;
		}
		component.setBackground(bg);
		component.setForeground(fg);
		// end code that should go away
	}
	
	// this code should go away when the first grid column becomes a TYPE_MESSAGE 
	Color normalForeground;
	Color normalBackground;
	// end code that should go away
}
