/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2005, Beneficent
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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.CellEditor;
import javax.swing.JComponent;
import javax.swing.table.TableCellEditor;

import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.client.swingui.grids.GridDropDownCellEditor;
import org.martus.client.swingui.grids.GridTableModel;
import org.martus.common.fieldspec.GridFieldSpec;



public class UiGridEditor extends UiGrid implements FocusListener
{
	public UiGridEditor(GridFieldSpec fieldSpec, UiDialogLauncher dlgLauncher)
	{
		super(fieldSpec, dlgLauncher);
		initialize();
	}
	
	public UiGridEditor(GridTableModel model, UiDialogLauncher dlgLauncher)
	{
		super(model, dlgLauncher);
		initialize();
	}
	
	private void initialize()
	{
		table.resizeTable(DEFAULT_VIEABLE_ROWS);
		table.addKeyListener(new GridKeyListener());
		addFocusListener(this);
	}
	
	
	public JComponent[] getFocusableComponents()
	{
		return table.getFocusableComponents();
	}

	public void focusGained(FocusEvent arg0)
	{
	}

	public void focusLost(FocusEvent event)
	{
		// if we validate on a temporary focus loss, we end up here twice
		// because popping up a dialog temporarily loses our focus
		if(event.isTemporary())
			return;
		
		TableCellEditor cellEditor = table.getCellEditor();
		if(cellEditor == null)
			return;
		cellEditor.stopCellEditing();
	}

	class GridKeyListener implements KeyListener
	{
		public void keyPressed(KeyEvent e)
		{
			// We have to handle TAB in pressed, because we need to grab it 
			// before the default grid handler
			if(e.getKeyCode() ==  KeyEvent.VK_TAB)
				handleTabKey(e);
		}

		public void keyReleased(KeyEvent e)
		{
		}

		public void keyTyped(KeyEvent e)
		{
            if (e.getKeyChar() == KeyEvent.VK_SPACE) 
                handleSpaceKey();
			if(e.getKeyChar() == KeyEvent.VK_ENTER)
				handleEnterKey();
		}

		private void handleSpaceKey()
		{
			CellEditor editor = table.getCellEditor();
			if (editor == null || !(editor instanceof GridDropDownCellEditor) )
				return;
			
			((GridDropDownCellEditor)editor).showPopup();
		}

		private void handleEnterKey()
		{
			model.addEmptyRow();
			table.changeSelection(getLastRowIndex(),0,false,false);
		}

		private void handleTabKey(KeyEvent e)
		{
			if(e.isControlDown())
				return;
			
			if(e.isShiftDown())
			{
				if(inFirstRowFirstColumn())
				{
					e.consume();
					table.transferFocusBackward();
				}
				else if(inFirstColumn())
				{
					e.consume();
					table.changeSelection(table.getSelectedRow()-1, getLastColumnIndex(), false, false);
				}
				
			}
			else 
			{ 
				if(inLastRowLastColumn())
				{
					e.consume();
					table.transferFocus();
				}
			}
		}

		private boolean inFirstColumn()
		{
			return table.getSelectedColumn() <= 1;
		}

		private boolean inFirstRowFirstColumn()
		{
			return table.getSelectedRow() <= 0 && 
					inFirstColumn();
		}

		private boolean inLastRowLastColumn()
		{
			return table.getSelectedRow() >= getLastRowIndex() && 
					table.getSelectedColumn() >= getLastColumnIndex();
		}

		private int getLastRowIndex()
		{
			return table.getRowCount()-1;
		}

		private int getLastColumnIndex()
		{
			return table.getColumnCount()-1;
		}

	}
	private static final int DEFAULT_VIEABLE_ROWS = 5;
}
