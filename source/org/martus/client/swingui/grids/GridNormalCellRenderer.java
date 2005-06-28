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
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;

import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.fields.UiNormalTextViewer;

class GridNormalCellRenderer implements TableCellRenderer
{
	GridNormalCellRenderer(UiLocalization localization)
	{
		renderer = new UiNormalTextViewer(localization);
		borderWithoutFocus = new EmptyBorder(1,1,1,1);
		borderWithFocus = new LineBorder(Color.BLACK,1);

		// this code should go away when the first grid column becomes a TYPE_MESSAGE 
		normalForeground = renderer.getComponent().getForeground();
		normalBackground = renderer.getComponent().getBackground();
		// end code that should go away

	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		renderer.setText((String)value);
		JComponent component = renderer.getComponent();

		
		// this code should go away when the first grid column becomes a TYPE_MESSAGE 
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
		
		
		Border border = borderWithoutFocus;
		if(hasFocus)
			border = borderWithFocus;
		component.setBorder(border);

		return component;
	}
	
	UiNormalTextViewer renderer;
	Border borderWithFocus;
	Border borderWithoutFocus;
	// this code should go away when the first grid column becomes a TYPE_MESSAGE 
	Color normalForeground;
	Color normalBackground;
	// end code that should go away
}

