/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.client.swingui.grids.GridTable;
import org.martus.client.swingui.grids.GridTableModel;
import org.martus.clientside.UiLocalization;
import org.martus.common.GridData;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiParagraphPanel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.Utilities;


abstract public class UiGrid extends UiField
{
	public UiGrid(UiMainWindow mainWindowToUse, GridFieldSpec fieldSpec, UiDialogLauncher dlgLauncher, Map gridFields, boolean isEditable)
	{
		this(mainWindowToUse, new GridTableModel(fieldSpec), dlgLauncher, gridFields, isEditable);
	}
	
	public UiGrid(UiMainWindow mainWindowToUse, GridTableModel modelToUse, UiDialogLauncher dlgLauncher, Map gridFields, boolean isEditable)
	{
		app = mainWindowToUse.getApp();
		model = modelToUse;
		
		if(isEditable)
			fieldCreator = new UiEditableFieldCreator(mainWindowToUse);
		else
			fieldCreator = new UiReadOnlyFieldCreator(mainWindowToUse);
		
		table = new GridTable(model, dlgLauncher, gridFields, isEditable);
		table.setColumnSelectionAllowed(false);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setShowGrid(true);
		table.changeSelection(0, 1, false, false);
		table.setRowHeight(table.getRowHeight() + ROW_HEIGHT_PADDING);
		widget = new JPanel();
		widget.setLayout(new BorderLayout());

		buttonBox = Box.createHorizontalBox();
		buttonBox.setBorder(new EmptyBorder(10,0,0,0));
		setButtons(createButtons());
		if(app.isGridExpanded(getGridTag()))
			showExpanded();
		else
			showCollapsed();
	}

	String getGridTag()
	{
		return model.getGridData().getSpec().getTag();
	} 
	
	protected UiLocalization getLocalization()
	{
		return table.getDialogLauncher().GetLocalization();
	}
	
	protected Vector createButtons()
	{
		return new Vector();
	}
	
	protected UiButton createShowExpandedButton()
	{
		UiButton expand = new UiButton(getLocalization().getButtonLabel("ShowGridExpanded"));
		expand.addActionListener(new ExpandButtonHandler());
		return expand;
	}
	
	class ExpandButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0)
		{
			app.setGridExpansionState(getGridTag(), true);
			showExpanded();
			widget.getTopLevelAncestor().validate();
		}
		
	}
	
	void showExpanded()
	{
		stopCellEditing();
		widget.removeAll();
		
		expandedFieldRows = new Vector();
		UiParagraphPanel fakeTable = new UiParagraphPanel();
		for(int row = 0; row < model.getRowCount(); ++row)
		{
			UiField[] rowFields = new UiField[model.getColumnCount()];
			expandedFieldRows.add(rowFields);
			UiParagraphPanel rowPanel = new UiParagraphPanel();
			for(int column = FIRST_REAL_FIELD_COLUMN; column < model.getColumnCount(); ++column)
			{
				UiField cellField = fieldCreator.createField(model.getFieldSpecForCell(row, column));
				cellField.getComponent().addFocusListener(new ExpandedGridFieldFocusHandler());
				String value = (String)model.getValueAt(row, column);
				cellField.setText(value);
				JComponent cellComponent = cellField.getComponent();
				cellComponent.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				rowPanel.addComponents(new UiLabel(model.getColumnName(column)), cellComponent);
				rowFields[column] = cellField;
			}
			fakeTable.addComponents(new UiLabel(model.getColumnName(0) + Integer.toString(row+1)), rowPanel);
		}
		addButtonsBelowExpandedGrid(fakeTable);
		
		widget.add(fakeTable);
		UiButton showCollapsedButton = new UiButton(getLocalization().getButtonLabel("ShowGridNormal"));
		showCollapsedButton.addActionListener(new CollapseButtonHandler());
		Box box = Box.createHorizontalBox();
		Utilities.addComponentsRespectingOrientation(box, new Component[] {showCollapsedButton, Box.createHorizontalGlue()});
		widget.add(box, BorderLayout.BEFORE_FIRST_LINE);
	}
	
	void addButtonsBelowExpandedGrid(UiParagraphPanel fakeTable) 
	{
		// NOTE: This method should be overridden where appropriate
	}

	class ExpandedGridFieldFocusHandler implements FocusListener
	{
		public void focusGained(FocusEvent event) 
		{
		}

		public void focusLost(FocusEvent event) 
		{
			copyExpandedFieldsToTableModel();
		}
		
	}
	
	class CollapseButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0)
		{
			app.setGridExpansionState(getGridTag(), false);
			showCollapsed();
			widget.getTopLevelAncestor().validate();
		}
		
	}
	
	void showCollapsed()
	{
		widget.removeAll();
		copyExpandedFieldsToTableModel();
		updateVisibleRowCount();
		expandedFieldRows = null;
		widget.add(buttonBox, BorderLayout.SOUTH);
		UiScrollPane tableScroller = new UiScrollPane(table);
		widget.add(tableScroller, BorderLayout.CENTER);
	}
	
	void copyExpandedFieldsToTableModel()
	{
		if(expandedFieldRows == null)
			return;
		
		for(int row = 0; row < expandedFieldRows.size(); ++row)
		{
			UiField[] rowFields = (UiField[])expandedFieldRows.get(row); 
			for(int column = FIRST_REAL_FIELD_COLUMN; column < rowFields.length; ++column)
			{
				String value = rowFields[column].getText();
				model.setValueAt(value, row, column);
			}
		}
	}

	void updateVisibleRowCount()
	{
		int rows = table.getRowCount();
		if(rows < MINIMUM_VISIBLE_ROWS)
			rows = MINIMUM_VISIBLE_ROWS;
		if(rows > MAXIMUM_VISIBLE_ROWS)
			rows = MAXIMUM_VISIBLE_ROWS;
		table.resizeTable(rows);
		Container topLevelAncestor = table.getTopLevelAncestor();
		if(topLevelAncestor != null)
			topLevelAncestor.validate();
	}

	protected void setButtons(Vector buttons) 
	{
		buttonBox.removeAll();
		buttons.add(Box.createHorizontalGlue());
		Component[] buttonsAsArray = (Component[])buttons.toArray(new Component[0]);
		Utilities.addComponentsRespectingOrientation(buttonBox, buttonsAsArray);
	}	
	
	public JComponent getComponent()
	{
		return widget;
	}
	
	public String getText()
	{
		copyExpandedFieldsToTableModel();
		return model.getXmlRepresentation();
	}

	public void setText(String newText)
	{
		try
		{
			model.setFromXml(newText);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public boolean isRowSelected()
	{
		return (table.getSelectedRow() != NO_ROW_SELECTED);
	}
	
	protected void stopCellEditing() 
	{
	}
	
	public GridTableModel getGridTableModel()
	{
		return model;
	}

	public GridData getGridData()
	{
		return model.getGridData();
	}
	
	public GridTable getTable()
	{
		return table;
	}
	
	private static final int NO_ROW_SELECTED = -1;
	private static final int ROW_HEIGHT_PADDING = 10;
	int FIRST_REAL_FIELD_COLUMN = 1;
	
	private static final int MINIMUM_VISIBLE_ROWS = 1;
	private static final int MAXIMUM_VISIBLE_ROWS = 5;

	MartusApp app;
	UiFieldCreator fieldCreator;
	JPanel widget;
	Box buttonBox;
	protected GridTable table;
	protected GridTableModel model;
	Vector expandedFieldRows;
}

