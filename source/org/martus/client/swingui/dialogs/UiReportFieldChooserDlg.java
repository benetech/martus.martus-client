/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006, Beneficent
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.martus.client.search.FieldChooserSpecBuilder;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionMenuReports.SpecTableModel;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.swing.UiButton;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiTable;
import org.martus.swing.UiWrappedTextPanel;
import org.martus.swing.Utilities;

public class UiReportFieldChooserDlg extends JDialog
{
	public UiReportFieldChooserDlg(UiMainWindow mainWindow)
	{
		super(mainWindow);
		setModal(true);
		
		String dialogTag = "ChooseReportFields";
		MartusLocalization localization = mainWindow.getLocalization();
		setTitle(localization.getWindowTitle(dialogTag));
		selectedSpecs = null;
		
		fieldSelector = new ReportFieldSelector(mainWindow);
		
		UiButton okButton = new UiButton(localization.getButtonLabel("ok"));
		okButton.addActionListener(new OkButtonHandler());
		UiButton cancelButton = new UiButton(localization.getButtonLabel("cancel"));
		cancelButton.addActionListener(new CancelButtonHandler());
		Box buttonBar = Box.createHorizontalBox();
		Component[] buttons = {Box.createHorizontalGlue(), okButton, cancelButton};
		Utilities.addComponentsRespectingOrientation(buttonBar, buttons);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new UiWrappedTextPanel(localization.getFieldLabel(dialogTag)), BorderLayout.BEFORE_FIRST_LINE);
		panel.add(new UiScrollPane(fieldSelector), BorderLayout.CENTER);
		panel.add(buttonBar, BorderLayout.AFTER_LAST_LINE);

		getContentPane().add(panel);
		pack();
		Utilities.centerDlg(this);
	}
	
	
	class OkButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			selectedSpecs = fieldSelector.getSelectedItems();
			dispose();
		}
	}
	
	class CancelButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			dispose();
		}
	}

	public FieldSpec[] getSelectedSpecs()
	{
		return selectedSpecs;
	}
	
	static class ReportFieldSelector extends JPanel
	{
		public ReportFieldSelector(UiMainWindow mainWindow)
		{
			super(new BorderLayout());
			FieldChooserSpecBuilder builder = new FieldChooserSpecBuilder(mainWindow.getLocalization());
			FieldSpec[] rawFieldSpecs = builder.createFieldSpecArray(mainWindow.getStore());
			model = new SpecTableModel(rawFieldSpecs, mainWindow.getLocalization());
			table = new UiTable(model);
			table.setMaxGridWidth(40);
			table.useMaxWidth();
			table.setFocusable(false);
			table.createDefaultColumnsFromModel();
			table.setColumnSelectionAllowed(false);
			add(new JScrollPane(table), BorderLayout.CENTER);
		}
		
		public FieldSpec[] getSelectedItems()
		{
			int[] selectedRows = table.getSelectedRows();
			FieldSpec[] selectedItems = new FieldSpec[selectedRows.length];
			for(int i = 0; i < selectedRows.length; ++i)
				selectedItems[i] = model.getSpec(selectedRows[i]);
			return selectedItems;
		}
		
		SpecTableModel model;
		UiTable table;
	}

	ReportFieldSelector fieldSelector;
	FieldSpec[] selectedSpecs;
}
