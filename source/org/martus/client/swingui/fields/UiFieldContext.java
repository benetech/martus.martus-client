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

import org.martus.common.FieldSpecCollection;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;

public class UiFieldContext
{
	public UiFieldContext()
	{
		fieldSpecs = new FieldSpecCollection();
		gridFieldsByTag = new HashMap();
		fieldsByTag = new HashMap();
	}

	public void setSectionFieldSpecs(FieldSpecCollection specs)
	{
		fieldSpecs = specs;
	}
	
	public int getSectionFieldCount()
	{
		return fieldSpecs.size();
	}
	
	public FieldSpec getFieldSpec(int index)
	{
		return fieldSpecs.get(index);
	}
	
	public void registerField(FieldSpec spec, UiField field)
	{
		fieldsByTag.put(spec.getTag(), field);
		if(spec.getType().isGrid())
			addGrid(spec.getTag(), (UiGrid)field);
	}
	
	public UiField getField(String tag)
	{
		return (UiField)fieldsByTag.get(tag);
	}

	private void addGrid(String gridTag, UiGrid gridEditor)
	{
		gridFieldsByTag.put(gridTag, gridEditor);
	}

	public UiGrid getGridField(String tag)
	{
		return (UiGrid)gridFieldsByTag.get(tag);
	}
	
	public ChoiceItem[] getCurrentDropDownChoices(DropDownFieldSpec spec)
	{
		UiGrid dataSource = getGrid(spec);
		if(dataSource != null)
			return getDataDrivenChoices(spec, dataSource);
		
		String reusableChoicesCode = spec.getReusableChoicesCode();
		if(reusableChoicesCode != null)
			return fieldSpecs.getReusableChoices(reusableChoicesCode).getChoices();

		return spec.getAllChoices();
		
	}

	private ChoiceItem[] getDataDrivenChoices(DropDownFieldSpec spec,
			UiGrid dataSource)
	{
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

	private FieldSpecCollection fieldSpecs;
	private HashMap gridFieldsByTag;
	private HashMap fieldsByTag;
}
