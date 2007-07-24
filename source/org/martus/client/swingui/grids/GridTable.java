/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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

package org.martus.client.swingui.grids;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.clientside.UiLocalization;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.FieldTypeAnyField;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeDateRange;
import org.martus.common.fieldspec.FieldTypeDropdown;
import org.martus.common.fieldspec.FieldTypeGrid;
import org.martus.common.fieldspec.FieldTypeLanguage;
import org.martus.common.fieldspec.FieldTypeMultiline;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.FieldTypePopUpTree;
import org.martus.swing.UiTableWithCellEditingProtection;
import org.martus.util.language.LanguageOptions;

public class GridTable extends UiTableWithCellEditingProtection
{
	
	public GridTable(GridTableModel model, UiDialogLauncher dlgLauncherToUse, boolean isTableEditable)
	{
		super(model);
		dlgLauncher = dlgLauncherToUse;
		// NOTE: We need to keep renderers and editors separate, because otherwise
		// they get confused about focus when you click on a renderer but the 
		// editor is supposed to end up getting the click because they occupy 
		// the same screen location
		if(isTableEditable)
		{
			renderers = createEditableEditorsOrRenderers();
			editors = createEditableEditorsOrRenderers();
		}
		else
		{
			renderers = createReadOnlyEditorsOrRenderers();
			editors = createReadOnlyEditorsOrRenderers();
		}
		useMaxWidth();
		setMaxColumnWidthToHeaderWidth(0);
		for(int i = 1 ; i < model.getColumnCount(); ++i)
		{
			FieldType columnType = model.getColumnType(i);
			if(columnType.isDropdown())
				setColumnMaxWidth(i, getDropDownColumnWidth(i, (DropDownFieldSpec)model.getFieldSpecForColumn(i)));
			else if(columnType.isDate())
				setColumnMaxWidth(i, getDateColumnWidth(i));
			else if(columnType.isDateRange())
				setColumnMaxWidth(i, getDateRangeColumnWidth(i));
			else if(columnType.isLanguageDropdown())
				setColumnWidthToHeaderWidth(i);
			else if(columnType.isBoolean())
				setColumnWidthToHeaderWidth(i);
			else
				setColumnWidthToMinimumRequred(i);
		}
		setAutoResizeMode(AUTO_RESIZE_OFF);
	}
	
	public UiDialogLauncher getDialogLauncher()
	{
		return dlgLauncher;
	}

	public int getDateColumnWidth(int column)
	{
		GridCellEditorAndRenderer gridDateCellEditor = ((GridCellEditorAndRenderer)getEditorOrRendererForType(editors, new FieldTypeDate()));
		int width = gridDateCellEditor.getMinimumCellWidth();
		
		int columnHeaderWidth = getColumnHeaderWidth(column);
		if(width < columnHeaderWidth)
			width = columnHeaderWidth;
		return width;
	}
	
	private int getDateRangeColumnWidth(int column)
	{
		GridCellEditorAndRenderer gridDateRangeCellEditor = ((GridCellEditorAndRenderer)getEditorOrRendererForType(editors, new FieldTypeDateRange()));
		int width = gridDateRangeCellEditor.getMinimumCellWidth();
		int columnHeaderWidth = getColumnHeaderWidth(column);
		if(width < columnHeaderWidth)
			width = columnHeaderWidth;
		return width;
	}

	private int getDropDownColumnWidth(int column, DropDownFieldSpec spec)
	{
		final int SCROLL_BAR_ALLOWANCE = 50;
		final int DROPDOWN_LANGUAGE_PADDING = 15;
		int widestWidth = getColumnHeaderWidth(column);
		for(int i = 0; i < spec.getCount(); ++i)
		{
			String thisValue = spec.getChoice(i).toString();
			int thisWidth = getRenderedWidth(column, thisValue) + SCROLL_BAR_ALLOWANCE;
			if(thisWidth > widestWidth)
				widestWidth = thisWidth;
		}
		if(LanguageOptions.needsLanguagePadding())
			widestWidth += DROPDOWN_LANGUAGE_PADDING;
		
		return widestWidth;
	}

	public JComponent[] getFocusableComponents()
	{
		Vector components = new Vector();
		Collection editorUiFields = editors.values();
		Iterator iter = editorUiFields.iterator();
		while(iter.hasNext())
		{
			GridCellEditorAndRenderer editor = (GridCellEditorAndRenderer)iter.next();
			List subComponents = Arrays.asList(editor.getUiField().getFocusableComponents());
			components.addAll(subComponents);
		}
		return (JComponent[])components.toArray(new JComponent[0]);
	}

	private HashMap createEditableEditorsOrRenderers()
	{
		HashMap map = new HashMap();
		UiLocalization localization = dlgLauncher.GetLocalization();
		map.put(new FieldTypeBoolean(), new GridBooleanCellEditor());
		map.put(new FieldTypeDate(), new GridDateCellEditor(localization));
		map.put(new FieldTypeDateRange(), new GridDateRangeCellEditor(dlgLauncher));
		map.put(new FieldTypeDropdown(), new GridDropDownCellEditor());
		map.put(new FieldTypeLanguage(), new GridDropDownCellEditor());
		map.put(new FieldTypeNormal(), new GridNormalCellEditor(localization));
		map.put(new FieldTypeMultiline(), new GridNormalCellEditor(localization));
		map.put(new FieldTypeAnyField(), new GridNormalCellEditor(localization));
		map.put(new FieldTypeGrid(), new GridNormalCellEditor(localization));
		map.put(new FieldTypePopUpTree(), new GridPopUpTreeCellEditor(localization));
		return map;
	}
	
	private HashMap createReadOnlyEditorsOrRenderers()
	{
		HashMap map = new HashMap();
		UiLocalization localization = dlgLauncher.GetLocalization();
		map.put(new FieldTypeBoolean(), new GridBooleanCellViewer(localization));
		map.put(new FieldTypeDate(), new GridDateCellViewer(localization));
		map.put(new FieldTypeDateRange(), new GridDateRangeCellViewer(localization));
		map.put(new FieldTypeDropdown(), new GridDropDownCellViewer());
		map.put(new FieldTypeLanguage(), new GridDropDownCellViewer());
		map.put(new FieldTypeNormal(), new GridNormalCellEditor(localization));
		map.put(new FieldTypeMultiline(), new GridNormalCellEditor(localization));
		map.put(new FieldTypeAnyField(), new GridNormalCellEditor(localization));
		map.put(new FieldTypeGrid(), new GridNormalCellEditor(localization));
		map.put(new FieldTypePopUpTree(), new GridPopUpTreeCellEditor(localization));
		return map;
	}

	FieldSpec getFieldSpecForColumn(int column)
	{
		return ((GridTableModel)getModel()).getFieldSpecForColumn(column);		
	}
	
	FieldSpec getFieldSpecForCell(int row, int column)
	{
		return ((GridTableModel)getModel()).getFieldSpecForCell(row, column);
	}
	
	public void changeSelection(int rowIndex, int columnIndex,
			boolean toggle, boolean extend)
	{
		if(columnIndex == 0)
			columnIndex = 1;
		super.changeSelection(rowIndex, columnIndex, toggle, extend);
	}

	public TableCellEditor getCellEditor(int row, int column)
	{
		return (TableCellEditor)getCellEditorOrRenderer(editors, row, column);
	}
	
	public TableCellRenderer getCellRenderer(int row, int column)
	{
		return (TableCellRenderer)getCellEditorOrRenderer(renderers, row, column);
	}
	
	private Object getCellEditorOrRenderer(HashMap map, int row, int column)
	{
		GridTableModel model = (GridTableModel)getModel();
		return getEditorOrRendererForType(map, model.getCellType(row, column));
	}

	private TableCellEditor getEditorOrRendererForType(HashMap map, FieldType type)
	{
		TableCellEditor editor = (TableCellEditor)map.get(type);
		if(editor != null)
			return editor;

		System.out.println("GridTable.getCellEditorOrRenderer Unexpected type: " + type);
		return (TableCellEditor)map.get(new FieldTypeNormal());
	}

	UiDialogLauncher dlgLauncher;
	HashMap renderers;
	HashMap editors;
}

