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
import java.util.Arrays;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.martus.client.reports.SpecTableModel;
import org.martus.client.search.FieldChooserSpecBuilder;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.swing.UiButton;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiTable;
import org.martus.swing.UiWrappedTextPanel;
import org.martus.swing.Utilities;

public class UiReportFieldOrganizerDlg extends JDialog
{
	public UiReportFieldOrganizerDlg(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		setModal(true);
		mainWindow = mainWindowToUse;
		
		String dialogTag = "OrganizeReportFields";
		MartusLocalization localization = mainWindow.getLocalization();
		setTitle(localization.getWindowTitle(dialogTag));
		
		fieldSelector = new ReportFieldSelector(mainWindow);
		
		UiButton addButton = new UiButton(localization.getButtonLabel("AddFieldToReport"));
		addButton.addActionListener(new AddButtonHandler());
		UiButton removeButton = new UiButton(localization.getButtonLabel("RemoveFieldFromReport"));
		removeButton.addActionListener(new RemoveButtonHandler());
		UiButton upButton = new UiButton(localization.getButtonLabel("MoveFieldUpInReport"));
		upButton.addActionListener(new UpButtonHandler());
		UiButton downButton = new UiButton(localization.getButtonLabel("MoveFieldDownInReport"));
		downButton.addActionListener(new DownButtonHandler());

		Box sideButtonBar = Box.createVerticalBox();
		sideButtonBar.add(addButton);
		sideButtonBar.add(removeButton);
		sideButtonBar.add(upButton);
		sideButtonBar.add(downButton);
		
		UiButton okButton = new UiButton(localization.getButtonLabel("ok"));
		okButton.addActionListener(new OkButtonHandler());
		UiButton cancelButton = new UiButton(localization.getButtonLabel("cancel"));
		cancelButton.addActionListener(new CancelButtonHandler());
		Box bottomButtonBar = Box.createHorizontalBox();
		Component[] buttons = {Box.createHorizontalGlue(), okButton, cancelButton};
		Utilities.addComponentsRespectingOrientation(bottomButtonBar, buttons);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new UiWrappedTextPanel(localization.getFieldLabel(dialogTag)), BorderLayout.BEFORE_FIRST_LINE);
		panel.add(new UiScrollPane(fieldSelector), BorderLayout.CENTER);
		panel.add(sideButtonBar, BorderLayout.EAST);
		panel.add(bottomButtonBar, BorderLayout.AFTER_LAST_LINE);

		getContentPane().add(panel);
		pack();
		Utilities.centerDlg(this);
	}
	
	class AddButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			FieldChooserSpecBuilder allFieldSpecBuilder = new FieldChooserSpecBuilder(mainWindow.getLocalization());
			FieldSpec[] allFieldSpecs = allFieldSpecBuilder.createFieldSpecArray(mainWindow.getStore());
			Vector possibleSpecsToAdd = new Vector(Arrays.asList(allFieldSpecs));
			FieldSpec[] currentSpecs = fieldSelector.getItems();
			if(currentSpecs != null)
				possibleSpecsToAdd.removeAll(Arrays.asList(currentSpecs));
			UiReportFieldChooserDlg dlg = new UiReportFieldChooserDlg(mainWindow, (FieldSpec[])possibleSpecsToAdd.toArray(new FieldSpec[0]));
			dlg.setVisible(true);
			model.AddSpecs(dlg.getSelectedSpecs());
		}
	}

	class RemoveButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			int selectedRow = table.getSelectedRow();
			model.RemoveSpec(selectedRow);
			if(selectedRow == table.getRowCount())
				--selectedRow;
			if(selectedRow >= 0)
				selectRow(selectedRow);
		}
	}

	class UpButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			int selectedRow = table.getSelectedRow();
			int rowSelectionWillMoveTo = selectedRow-1;
			if(rowSelectionWillMoveTo < 0)
				return;
			model.MoveSpecUp(selectedRow);
			selectRow(rowSelectionWillMoveTo);
		}
	}

	class DownButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			int selectedRow = table.getSelectedRow();
			int rowSelectionWillMoveTo = selectedRow+1;
			if(selectedRow < 0 ||rowSelectionWillMoveTo >= table.getRowCount())
				return;
			model.MoveSpecDown(selectedRow);
			selectRow(rowSelectionWillMoveTo);
		}
	}

	class OkButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			orderedSpecs = fieldSelector.getItems();
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
		return orderedSpecs;
	}
	
	void selectRow(int rowToSelect)
	{
		table.getSelectionModel().setSelectionInterval(rowToSelect, rowToSelect);
	}

	class ReportFieldSelector extends JPanel
	{
		public ReportFieldSelector(UiMainWindow mainWindow)
		{
			super(new BorderLayout());
			FieldSpec[] emptyFieldSpecs = new FieldSpec[0];
			model = new SpecTableModel(emptyFieldSpecs, mainWindow.getLocalization());
			table = new UiTable(model);
			table.setMaxGridWidth(40);
			table.useMaxWidth();
			table.setFocusable(false);
			table.createDefaultColumnsFromModel();
			table.setColumnSelectionAllowed(false);
			table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			add(new JScrollPane(table), BorderLayout.CENTER);
		}
		
		public FieldSpec[] getItems()
		{
			int rows = table.getRowCount();
			FieldSpec[] items = new FieldSpec[rows];
			for(int i = 0; i < rows; ++i)
				items[i] = model.getSpec(i);
			return items;
		}
		
	}

	UiMainWindow mainWindow;
	SpecTableModel model;
	UiTable table;
	ReportFieldSelector fieldSelector;
	FieldSpec[] orderedSpecs;
}
