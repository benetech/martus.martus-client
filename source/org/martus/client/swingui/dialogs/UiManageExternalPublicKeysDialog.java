package org.martus.client.swingui.dialogs;
/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2013, Beneficent
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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JDialog;
import javax.swing.ListSelectionModel;

import org.martus.client.swingui.HeadquartersTableModel;
import org.martus.client.swingui.UiMainWindow;
import org.martus.clientside.UiLocalization;
import org.martus.swing.UiTable;

public class UiManageExternalPublicKeysDialog extends JDialog
{
	public UiManageExternalPublicKeysDialog(UiMainWindow owner, String title)
	{
		super(owner);
		setTitle(title);
		setModal(true);

		mainWindow = owner;
		localization = mainWindow.getLocalization();
	}

	protected UiTable createHeadquartersTable(HeadquartersTableModel hqModel) 
	{
		UiTable hqTable = new UiTable(hqModel);
		hqTable.setRenderers(hqModel);
		hqTable.createDefaultColumnsFromModel();
		hqTable.addKeyListener(new TableListener());
		hqTable.setColumnSelectionAllowed(false);
		hqTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		hqTable.setShowGrid(true);
		hqTable.setMaxColumnWidthToHeaderWidth(0);
		hqTable.resizeTable(DEFAULT_VIEABLE_ROWS);
		
		return hqTable;
	}

	
	class TableListener implements KeyListener
	{
		public void keyPressed(KeyEvent e)
		{
			if(e.getKeyCode() ==  KeyEvent.VK_TAB && !e.isControlDown())
			{
				e.consume();
				if(e.isShiftDown())
					table.transferFocusBackward();
				else 
					table.transferFocus();
			}
		}

		public void keyReleased(KeyEvent e)
		{
		}

		public void keyTyped(KeyEvent e)
		{
		}
	}

	private static final int DEFAULT_VIEABLE_ROWS = 5;

	UiMainWindow mainWindow;
	UiTable table;
	UiLocalization localization;
}
