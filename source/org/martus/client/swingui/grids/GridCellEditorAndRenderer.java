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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.martus.client.swingui.fields.UiField;

public class GridCellEditorAndRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, FocusListener
{
	GridCellEditorAndRenderer(UiField widgetToWrap)
	{
		widget = widgetToWrap;
		getComponent().addFocusListener(this);
		borderWithoutFocus = new EmptyBorder(1,1,1,1);
		borderWithFocus = new LineBorder(Color.BLACK,1);
	}

	public Component getTableCellEditorComponent(JTable tableToUse, Object stringValue, boolean isSelected, int row, int column)
	{
		widget.setText((String)stringValue);
		JComponent component = getComponent();
		
		component.setBorder(borderWithFocus);
		return component;
	}

	public JComponent getComponent()
	{
		return widget.getComponent();
	}

	public Object getCellEditorValue()
	{
		return widget.getText();
	}

	public Component getTableCellRendererComponent(JTable tableToUse, Object stringValue, boolean isSelected, boolean hasFocus, int row, int column)
	{
		widget.setText((String)stringValue);
		JComponent component = getComponent();
		
		Border border = borderWithoutFocus;
		if(hasFocus)
			border = borderWithFocus;
		component.setBorder(border);
		return component;
	}
	
	public void focusGained(FocusEvent arg0)
	{
	}

	// NOTE: I would have expected Swing to do this automatically, but without 
	// this, clicking in a grid cell, making changes, and then directly clicking 
	// in some other field, or on the Save As Draft button causes the edits
	// to be discarded. See also UiTableWithCellEditingProtection for a 
	// related case with a different solution
	public void focusLost(FocusEvent arg0)
	{
//		System.out.println("GridCellEditorAndRenderer focusLost, so calling stopCellEditing");
		stopCellEditing();
	}

	UiField widget;
	Border borderWithFocus;
	Border borderWithoutFocus;
}
