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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.martus.client.swingui.UiMainWindow;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.swing.UiTable;
import org.martus.swing.Utilities;


public class UiConfigureHQs extends JDialog
{
	public UiConfigureHQs(UiMainWindow mainWindow)
	{
		super(mainWindow, "", true);
		UiBasicLocalization localization = mainWindow.getLocalization();
		setTitle(localization.getWindowTitle("ConfigureHQs"));
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10,10,10,10));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		Box hBox1 = Box.createHorizontalBox();
		hBox1.add(new JLabel(localization.getFieldLabel("ConfigureHQsCurrentHQs")));
		hBox1.add(Box.createHorizontalGlue());
		panel.add(hBox1);
		
		DefaultTableModel model = new DefaultTableModel();
		Vector columnHeaders = new Vector();
		String fieldLabel = localization.getFieldLabel("ConfigureHQColumnHeaderPublicCode");
		columnHeaders.add(fieldLabel);
		model.setColumnIdentifiers(columnHeaders);
		Vector hq = new Vector();
		hq.add("HQ1's Public Code goes here.");
		model.addRow(hq);
		
		UiTable table = new UiTable(model);
		table.setColumnSelectionAllowed(false);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setShowGrid(true);
		table.resizeTable();
		JScrollPane scroller = new JScrollPane(table);
		
		panel.add(scroller);
		panel.add(new JLabel(" "));
		
		Box hBox = Box.createHorizontalBox();
		JButton add = new JButton(localization.getButtonLabel("ConfigureHQsAdd"));
		JButton remove = new JButton(localization.getButtonLabel("ConfigureHQsRemove"));
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
	
	class CancelHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			dispose();
		}
	}
	
	
}
