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
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.table.TableCellEditor;

import org.martus.client.swingui.dialogs.UiDialogLauncher;
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
			if(e.getKeyCode() ==  KeyEvent.VK_TAB && !e.isControlDown())
			{
				if(e.isShiftDown())
				{
					if(table.getSelectedRow()==0 && table.getSelectedColumn() == 1)
					{
						e.consume();
						table.transferFocusBackward();
					}
					else if(table.getSelectedColumn() <= 1)
					{
						e.consume();
						table.changeSelection(table.getSelectedRow()-1, table.getColumnCount()-1, false, false);
					}
					
				}
				else 
				{ 
					if(table.getSelectedRow()== table.getRowCount()-1)
					{
						if(table.getSelectedColumn() >= table.getColumnCount()-1)
						{
							e.consume();
							table.transferFocus();
						}
					}
				}
			}
		}

		public void keyReleased(KeyEvent e)
		{
            if (e.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE) 
			{
                CellEditor editor = table.getCellEditor();
                if (editor != null && editor instanceof DefaultCellEditor ) 
				{
                    Object component = ((DefaultCellEditor)editor).getComponent();
                    if(component instanceof JComboBox) 
					{
                        JComboBox comboBox = (JComboBox)(component);
						comboBox.setVisible( true );
						comboBox.requestFocus();
						comboBox.showPopup();
                   }
                }
            }
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
	private static final int DEFAULT_VIEABLE_ROWS = 5;
}
