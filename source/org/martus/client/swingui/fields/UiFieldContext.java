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

import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;

public class UiFieldContext
{
	public UiFieldContext()
	{
		fieldSpecs = new FieldSpec[0];
		gridFieldsByTag = new HashMap();
		fieldsByTag = new HashMap();
	}

	public void setSectionFieldSpecs(FieldSpec[] specs)
	{
		fieldSpecs = specs;
	}
	
	public int getSectionFieldCount()
	{
		return fieldSpecs.length;
	}
	
	public FieldSpec getFieldSpec(int index)
	{
		return fieldSpecs[index];
	}
	
	public void registerField(String tag, UiField field)
	{
		fieldsByTag.put(tag, field);
	}
	
	public UiField getField(String tag)
	{
		return (UiField)fieldsByTag.get(tag);
	}

	public void addGrid(String gridTag, UiGrid gridEditor)
	{
		gridFieldsByTag.put(gridTag, gridEditor);
	}

	public UiGrid getGridField(String tag)
	{
		return (UiGrid)gridFieldsByTag.get(tag);
	}
	
	public ChoiceItem[] getCurrentGridValuesAsChoices(DropDownFieldSpec spec)
	{
		UiGrid dataSource = getGrid(spec);
		if(dataSource == null)
			return spec.getAllChoices();
		
		String gridColumnLabel = spec.getDataSourceGridColumn();
		return dataSource.buildChoicesFromColumnValues(gridColumnLabel);
	}
	
	private UiGrid getGrid(DropDownFieldSpec spec)
	{
		String gridTag = spec.getDataSourceGridTag();
		if(gridTag == null)
			return null;
		
		return getGridField(gridTag);
	}

	private FieldSpec[] fieldSpecs;
	private HashMap gridFieldsByTag;
	private HashMap fieldsByTag;
}
