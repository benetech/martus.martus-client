/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2004, Beneficent
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
package org.martus.client.swingui.dialogs;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.common.crypto.MartusCrypto;
import org.martus.swing.UiFileChooser;
import org.martus.swing.UiTable;
import org.martus.swing.Utilities;
import org.martus.util.Base64.InvalidBase64Exception;


public class UiConfigureHQs extends JDialog
{
	public UiConfigureHQs(UiMainWindow owner)
	{
		super(owner, "", true);
		mainWindow = owner;
		localization = mainWindow.getLocalization();
		hQKeys = new Vector();
		mapOfKeysToCodes = new HashMap();
		
		setTitle(localization.getWindowTitle("ConfigureHQs"));
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10,10,10,10));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		Box hBox1 = Box.createHorizontalBox();
		hBox1.add(new JLabel(localization.getFieldLabel("ConfigureHQsCurrentHQs")));
		hBox1.add(Box.createHorizontalGlue());
		panel.add(hBox1);
		
		model = new DefaultTableModel();
		Vector columnHeaders = new Vector();
		String fieldLabel = localization.getFieldLabel("ConfigureHQColumnHeaderPublicCode");
		columnHeaders.add(fieldLabel);
		model.setColumnIdentifiers(columnHeaders);
		
		
		table = new HQTable(model);
		table.addKeyListener(new TableListener());
		table.setColumnSelectionAllowed(false);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setShowGrid(true);

		String hQKey = mainWindow.getApp().getHQKey();
		if(hQKey.length() != 0)
			addHQKeyToTable(hQKey);
		if(model.getRowCount()>0)
			table.setRowSelectionInterval(0,0);
		
		table.resizeTable(DEFAULT_VIEABLE_ROWS);
		JScrollPane scroller = new JScrollPane(table);
		
		panel.add(scroller);
		panel.add(new JLabel(" "));
		
		Box hBox = Box.createHorizontalBox();
		JButton add = new JButton(localization.getButtonLabel("ConfigureHQsAdd"));
		add.addActionListener(new AddHandler());
		remove = new JButton(localization.getButtonLabel("ConfigureHQsRemove"));
		remove.addActionListener(new RemoveHandler());
		remove.setEnabled(false);

		hBox.add(add);
		hBox.add(remove);
		hBox.add(Box.createHorizontalGlue());
		JButton close = new JButton(localization.getButtonLabel("close"));
		close.addActionListener(new CancelHandler());
		hBox.add(close);
		panel.add(hBox);
		getContentPane().add(panel);
		getRootPane().setDefaultButton(close);
		Utilities.centerDlg(this);
		setResizable(true);
		show();
	}
	
	class HQTable extends UiTable
	{
		public HQTable(TableModel model)
		{
			super(model);
			hqRenderer = new HQCellRenderer();
		}
		
		public TableCellRenderer getCellRenderer(int row, int column)
		{
			return hqRenderer;
		}
		TableCellRenderer hqRenderer;
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

	
	class HQCellRenderer implements TableCellRenderer
	{
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			JTextField cell = new JTextField((String)value);
			cell.setBorder(new EmptyBorder(0,0,0,0));

			if(hasFocus)
				cell.setBorder(new LineBorder(Color.BLUE, 1));
				
			if(isSelected)
			{
				cell.setBackground(Color.DARK_GRAY);
				cell.setForeground(Color.WHITE);
				remove.setEnabled(true);

			}
			else
			{
				cell.setBackground(Color.WHITE);
				cell.setForeground(Color.BLACK);
			}
			return cell;
		}
	}
	
	
	class CancelHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			dispose();
		}
	}
	
	class AddHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			try
			{
				String publicKey = getPublicKey();
				if(publicKey==null)
					return;
				String publicCode = MartusCrypto.computePublicCode(publicKey);
				if(confirmPublicCode(publicCode, "ImportPublicCode", "AccountCodeWrong"))
				{
					if(mainWindow.confirmDlg("SetImportPublicKey"))
					{
						addHQKeyToTable(publicKey);
						updateConfigInfo();
					}
				}
			}
			catch (Exception e)
			{
				mainWindow.notifyDlg("PublicInfoFileError");
			}
		}
	}

	class RemoveHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			if(!mainWindow.confirmDlg("ClearHQInformation"))
				return;
			
			int rowCount = model.getRowCount();
			for(int i = rowCount-1; i >=0 ; --i)
			{
				if(table.isRowSelected(i))
				{
					
					Object hQKeyToBeRemoved = table.getValueAt(i,PUBLIC_CODE_COLUMN);
					for(int j = 0; j<hQKeys.size(); ++j)
					{
						if (((String)hQKeys.get(j)).equals(mapOfKeysToCodes.get(hQKeyToBeRemoved)))
						{
							hQKeys.remove(j);
							break;
						}
					}
					model.removeRow(i);
				}
			}
			remove.setEnabled(false);
			updateConfigInfo();
		}
//		mainWindow.doClearPublicAccountInfo();
	}

	void addHQKeyToTable(String publicKey)
	{
		try
		{
			String rawCode = MartusCrypto.computePublicCode(publicKey);
			String formattedCode = MartusCrypto.formatPublicCode(rawCode);
			for(int i = 0; i < table.getRowCount(); ++i)
			{
				if(((String)model.getValueAt(i, PUBLIC_CODE_COLUMN)).equals(formattedCode))
				{
					mainWindow.notifyDlg("HQKeyAlradyExists");
					table.setRowSelectionInterval(i,i);
					return;
				}
			}
			
			Vector row = new Vector();
			row.add(formattedCode);
			model.addRow(row);
			mapOfKeysToCodes.put(formattedCode, publicKey);
			hQKeys.add(publicKey);
			int current = table.getRowCount()-1;
			table.setRowSelectionInterval(current,current);
		}
		catch (InvalidBase64Exception e)
		{
			e.printStackTrace();
		}
	}
	
	void updateConfigInfo()
	{
		mainWindow.setAndSaveHQKeysInConfigInfo(hQKeys);
	}
	
	public String getPublicKey() throws Exception
	{
		String windowTitle = localization.getWindowTitle("ImportHQPublicKey");
		String buttonLabel = localization.getButtonLabel("inputImportPublicCodeok");
		
		File currentDirectory = new File(mainWindow.getApp().getCurrentAccountDirectoryName());
		FileFilter filter = new PublicInfoFileFilter();
		UiFileChooser.FileDialogResults results = UiFileChooser.displayFileOpenDialog(mainWindow, windowTitle, null, currentDirectory, buttonLabel, filter);
		if (results.wasCancelChoosen())
			return null;
		
		File importFile = results.getFileChoosen();
		String publicKey = mainWindow.getApp().extractPublicInfo(importFile);
		return publicKey;
	}
	
	class PublicInfoFileFilter extends FileFilter
	{
		public boolean accept(File pathname)
		{
			if(pathname.isDirectory())
				return true;
			return(pathname.getName().endsWith(MartusApp.PUBLIC_INFO_EXTENSION));
		}

		public String getDescription()
		{
			return localization.getFieldLabel("PublicInformationFiles");
		}
	}


	boolean confirmPublicCode(String publicCode, String baseTag, String errorBaseTag)
	{
		String userEnteredPublicCode = "";
System.out.println("Public code required:" + publicCode);
		while(true)
		{
			userEnteredPublicCode = mainWindow.getStringInput(baseTag, "", userEnteredPublicCode);
			if(userEnteredPublicCode == null)
				return false; // user hit cancel
			String normalizedPublicCode = MartusCrypto.removeNonDigits(userEnteredPublicCode);

			if(publicCode.equals(normalizedPublicCode))
				return true;

			mainWindow.notifyDlg(errorBaseTag);
		}
	}
	
	UiMainWindow mainWindow;
	UiTable table;
	DefaultTableModel model;
	JButton remove;
	UiBasicLocalization localization;
	private static final int DEFAULT_VIEABLE_ROWS = 5;
	private static final int PUBLIC_CODE_COLUMN = 0;
	Vector hQKeys;
	HashMap mapOfKeysToCodes;
}
