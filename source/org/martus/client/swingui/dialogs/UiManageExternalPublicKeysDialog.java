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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import org.martus.client.swingui.ExternalPublicKeysTableModel;
import org.martus.client.swingui.UiMainWindow;
import org.martus.clientside.UiLocalization;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiTable;
import org.martus.swing.UiVBox;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;

abstract public class UiManageExternalPublicKeysDialog extends JDialog
{
	public UiManageExternalPublicKeysDialog(UiMainWindow owner, String title)
	{
		super(owner);
		setTitle(title);
		setModal(true);

		mainWindow = owner;
		localization = mainWindow.getLocalization();

		JButton add = new UiButton(localization.getButtonLabel("ConfigureHQsAdd"));
		add.addActionListener(createAddHandler());
		remove = new UiButton(localization.getButtonLabel("ConfigureHQsRemove"));
		remove.addActionListener(createRemoveHandler());
		renameLabel = new UiButton(localization.getButtonLabel("ConfigureHQsReLabel"));
		renameLabel.addActionListener(createRenameHandler());

		String[] dialogText = getDialogText();
		UiVBox vBox = new UiVBox();
		for (String text : dialogText)
		{
			vBox.addCentered(new UiWrappedTextArea(text));
			vBox.addSpace();
		}

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10,10,10,10));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(vBox);
		
		model = createModel();
		table = createTable(model);
		
		addExistingKeysToTable();
		enableDisableButtons();
		
		UiScrollPane scroller = new UiScrollPane(table);
		panel.add(scroller);
		panel.add(new UiLabel(" "));
		
		Box hBox = Box.createHorizontalBox();
		JButton save = new UiButton(localization.getButtonLabel("save"));
		save.addActionListener(createSaveHandler());
		JButton cancel = new UiButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(createCancelHandler());
		Utilities.addComponentsRespectingOrientation(hBox, new Component[]{add,remove,renameLabel,Box.createHorizontalGlue(),save,cancel});
		panel.add(hBox);
		
		getContentPane().add(panel);
		getRootPane().setDefaultButton(cancel);
		Utilities.centerDlg(this);
		setResizable(true);
	}

	abstract ActionListener createAddHandler();
	abstract ActionListener createRemoveHandler();
	abstract String[] getDialogText();
	abstract ExternalPublicKeysTableModel createModel();
	abstract void addExistingKeysToTable();
	abstract void updateConfigInfo();
	abstract String askUserForNewLabel(String publicCode, String previousValue);

	RenameHandler createRenameHandler()
	{
		return new RenameHandler();
	}

	SaveHandler createSaveHandler()
	{
		return new SaveHandler();
	}

	CancelHandler createCancelHandler()
	{
		return new CancelHandler();
	}

	protected UiTable createTable(ExternalPublicKeysTableModel hqModel) 
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
	
	ExternalPublicKeysTableModel getModel()
	{
		return model;
	}

	void enableDisableButtons()
	{
		boolean enableButtons = false;
		if(table.getRowCount()>0)
			enableButtons = true;
		remove.setEnabled(enableButtons);
		renameLabel.setEnabled(enableButtons);

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

	class CancelHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			dispose();
		}
	}
	
	class SaveHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			updateConfigInfo();
			dispose();
		}
	}
	
	class RenameHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			if(table.getSelectedRowCount()==0)
			{
				mainWindow.notifyDlg("NoHQsSelected");
				return;
			}
			int rowCount = model.getRowCount();
			for(int i = rowCount-1; i >=0 ; --i)
			{
				if(table.isRowSelected(i))
				{
					String newLabel = askUserForNewLabel(model.getPublicCode(i), model.getLabel(i));
					if(newLabel== null)
						break;
					getModel().setLabel(i, newLabel);
				}
			}
		}
	}
	
	private static final int DEFAULT_VIEABLE_ROWS = 5;

	UiMainWindow mainWindow;
	UiTable table;
	UiLocalization localization;

	ExternalPublicKeysTableModel model;
	JButton remove;
	JButton renameLabel;
}
