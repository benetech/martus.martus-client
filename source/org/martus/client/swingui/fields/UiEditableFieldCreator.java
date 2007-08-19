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
package org.martus.client.swingui.fields;

import java.util.HashMap;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.client.swingui.grids.GridTableModel;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.swing.UiComboBox;
import org.martus.util.MultiCalendar;

public class UiEditableFieldCreator extends UiFieldCreator
{
	public UiEditableFieldCreator(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		gridFields = new HashMap();
	}

	public UiField createUnknownField()
	{
		return new UiUnknownViewer(getLocalization());
	}
	
	public UiField createNormalField()
	{
		return new UiNormalTextEditor(getLocalization(), mainWindow.getEditingTextFieldColumns());
	}

	public UiField createMultilineField()
	{
		return new UiMultilineTextEditor(getLocalization(), mainWindow.getEditingTextFieldColumns());
	}

	public UiField createMessageField(FieldSpec spec)
	{
		return new UiMessageField(spec, mainWindow.getEditingTextFieldColumns());
	}

	public UiField createChoiceField(DropDownFieldSpec spec)
	{
		UiChoiceEditor dropDownField = new UiChoiceEditor(spec);
		String gridTag = spec.getDataSourceGridTag();
		if(gridTag != null)
		{
			UiGridEditor grid = (UiGridEditor)gridFields.get(gridTag);
			DropDownDataSourceHandler dropDownDataSourceHandler = new DropDownDataSourceHandler(dropDownField, grid);
			grid.getGridTableModel().addTableModelListener(dropDownDataSourceHandler);
		}
		return dropDownField;
	}
	
	class DropDownDataSourceHandler implements TableModelListener
	{
		public DropDownDataSourceHandler(UiChoiceEditor dropDownToUpdate, UiGridEditor dataSourceToMonitor)
		{
			dropDown = dropDownToUpdate;
			dataSource = dataSourceToMonitor;
			String gridColumnLabel = dropDown.getSpec().getDataSourceGridColumn();
			gridColumn = dataSource.getGridTableModel().findColumn(gridColumnLabel);
		}

		public void tableChanged(TableModelEvent e)
		{
			String selected = dropDown.getText();

			UiComboBox component = (UiComboBox)dropDown.getComponent();
			component.removeAllItems();
			dropDown.setChoices(getCurrentGridValuesAsChoices());
			
			dropDown.setText(selected);
		}
		
		ChoiceItem[] getCurrentGridValuesAsChoices()
		{
			GridTableModel model = dataSource.getGridTableModel();
			ChoiceItem[] values = new ChoiceItem[1 + model.getRowCount()];
			values[0] = new ChoiceItem("", "");
			for(int row = 0; row < model.getRowCount(); ++row)
			{
				String thisValue = (String)model.getValueAt(row, gridColumn);
				values[row + 1] = new ChoiceItem(thisValue, thisValue);
			}
			
			return values;
		}

		UiChoiceEditor dropDown;
		UiGridEditor dataSource;
		int gridColumn;
	}

	public UiField createDateField(FieldSpec spec)
	{
		MultiCalendar maxDate = null;
		if(StandardFieldSpecs.isStandardFieldTag(spec.getTag()))
			maxDate = new MultiCalendar();

		return new UiDateEditor(getLocalization(), maxDate);		
	}
	
	public UiField createFlexiDateField(FieldSpec spec)
	{
		return new UiFlexiDateEditor(getLocalization(), spec);	
	}

	public UiField createBoolField()
	{
		return new UiBoolEditor();
	}

	public UiField createGridField(GridFieldSpec fieldSpec)
	{
		MartusLocalization localization = mainWindow.getLocalization();
		fieldSpec.setColumnZeroLabel(localization.getFieldLabel("ColumnGridRowNumber"));
		UiDialogLauncher dlgLauncher = new UiDialogLauncher(mainWindow.getCurrentActiveFrame(), localization);
		UiGridEditor gridEditor = new UiGridEditor(mainWindow, fieldSpec, dlgLauncher, gridFields, mainWindow.getEditingTextFieldColumns());
		gridFields.put(fieldSpec.getTag(), gridEditor);
		return gridEditor;
	}
	
	HashMap gridFields;
}
