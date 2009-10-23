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
package org.martus.client.swingui.grids;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.clientside.UiLocalization;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeDateRange;

abstract public class GridFieldTable extends GridTable
{
	public GridFieldTable(GridTableModel model,
			UiDialogLauncher dlgLauncherToUse, Map otherGridFieldsToUse,
			boolean isTableEditable)
	{
		super(model, dlgLauncherToUse, otherGridFieldsToUse, isTableEditable);
	}

	protected void createEditableRenderers()
	{
		renderers = new GridCellEditorAndRenderer[getColumnCount()];
		createEditableEditorsOrRenderers(renderers);
	}
	
	protected void createEditableEditors()
	{
		editors = new GridCellEditorAndRenderer[getColumnCount()];
		createEditableEditorsOrRenderers(editors);
	}
	
	protected void createEditableEditorsOrRenderers(GridCellEditorAndRenderer[] array)
	{
		GridTableModel model = getGridTableModel();
		
		genericDateEditor = createEditor(new FieldTypeDate());
		genericDateRangeEditor = createEditor(new FieldTypeDateRange());
		
		for(int tableColumn = 0; tableColumn < getColumnCount(); ++tableColumn)
		{
			int modelColumn = convertColumnIndexToModel(tableColumn);
			FieldType type = model.getCellType(0, modelColumn);
			array[tableColumn] = createEditor(type);
		}
	}
	
	private GridCellEditorAndRenderer createEditor(FieldType type)
	{
		UiLocalization localization = dlgLauncher.GetLocalization();
		if(type.isBoolean())
			return new GridBooleanCellEditor(localization);
		if(type.isDate())
			return new GridDateCellEditor(localization);
		if(type.isDateRange())
			return new GridDateRangeCellEditor(dlgLauncher, getGridFieldSpec());
		if(type.isDropdown() || type.isLanguageDropdown())
			return new GridDropDownCellEditor(otherGridFields, localization);
		if(type.isPopUpTree())
			return new GridPopUpTreeCellEditor(localization);
		
		if(type.isMultiline() || type.isAnyField() || type.isGrid())
			return new GridNormalCellEditor(localization);
			
		return new GridNormalCellEditor(localization);
	}

	protected void createReadOnlyRenderers()
	{
		renderers = new GridCellEditorAndRenderer[getColumnCount()];
		createReadOnlyEditorsOrRenderers(renderers);
	}
	
	protected void createReadOnlyEditors()
	{
		editors = new GridCellEditorAndRenderer[getColumnCount()];
		createReadOnlyEditorsOrRenderers(editors);
	}
	
	protected void createReadOnlyEditorsOrRenderers(GridCellEditorAndRenderer[] array)
	{
		genericDateEditor = createViewer(new FieldTypeDate());
		genericDateRangeEditor = createViewer(new FieldTypeDateRange());
		
		GridTableModel model = getGridTableModel();
		for(int tableColumn = 0; tableColumn < getColumnCount(); ++tableColumn)
		{
			int modelColumn = convertColumnIndexToModel(tableColumn);
			FieldType type = model.getCellType(0, modelColumn);
			array[tableColumn] = createViewer(type);
		}
	}

	private GridCellEditorAndRenderer createViewer(FieldType type)
	{
		UiLocalization localization = dlgLauncher.GetLocalization();
		if(type.isBoolean())
			return new GridBooleanCellViewer(localization);
		if(type.isDate())
			return new GridDateCellViewer(localization);
		if(type.isDateRange())
			return new GridDateRangeCellViewer(localization);
		if(type.isDropdown() || type.isLanguageDropdown())
			return new GridDropDownCellViewer(otherGridFields, localization);
		if(type.isPopUpTree())
			return new GridPopUpTreeCellEditor(localization);
		
		if(type.isMultiline() || type.isAnyField() || type.isGrid())
			return new GridNormalCellEditor(localization);
			
		return new GridNormalCellEditor(localization);
	}

	public TableCellEditor getCellEditor(int row, int column)
	{
		return editors[column];
	}
	
	public TableCellRenderer getCellRenderer(int row, int column)
	{
		return renderers[column];
	}
	
	protected List getAllEditors()
	{
		return Arrays.asList(editors);
	}

	protected GridCellEditorAndRenderer getDateEditor()
	{
		return genericDateEditor;
	}

	protected GridCellEditorAndRenderer getDateRangeEditor()
	{
		return genericDateRangeEditor;
	}

	private GridCellEditorAndRenderer genericDateEditor;
	private GridCellEditorAndRenderer genericDateRangeEditor;
	
	private GridCellEditorAndRenderer[] renderers;
	private GridCellEditorAndRenderer[] editors;
}
