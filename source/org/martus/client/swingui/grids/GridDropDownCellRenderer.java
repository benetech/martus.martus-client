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

import org.martus.client.swingui.fields.UiChoiceEditor;
import org.martus.common.fieldspec.DropDownFieldSpec;

public class GridDropDownCellRenderer implements TableCellRenderer
{
	public GridDropDownCellRenderer()
	{
		widget = new UiChoiceEditor(null);
		borderWithoutFocus = new EmptyBorder(1,1,1,1);
		borderWithFocus = new LineBorder(Color.BLACK,1);
	}
	
	public Component getTableCellRendererComponent(JTable tableToUse, Object codeString, boolean isSelected, boolean hasFocus, int row, int column)
	{
		GridTable gridTable = (GridTable)tableToUse;
		widget.setSpec((DropDownFieldSpec)gridTable.getFieldSpecForColumn(column));
		widget.setText((String)codeString);

		JComponent component = widget.getComponent();
		Border border = borderWithoutFocus;
		if(hasFocus)
			border = borderWithoutFocus;
		component.setBorder(border);
		return component;
	}
	
	UiChoiceEditor widget;
	Border borderWithFocus;
	Border borderWithoutFocus;
}
