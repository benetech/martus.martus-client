/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
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
package org.martus.client.core;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;

import org.martus.client.swingui.jfx.landing.bulletins.GridRowData;
import org.martus.common.GridData;
import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.GridFieldSpec;

public class FxBulletinField
{
	public FxBulletinField(FieldSpec fieldSpecToUse, MiniLocalization localizationToUse)
	{
		fieldSpec = fieldSpecToUse;
		valueProperty = new SimpleStringProperty("");
		gridDataIfApplicable = new GridFieldData();
		FieldValidator fieldValidator = new FieldValidator(fieldSpec, localizationToUse);
		setValidator(fieldValidator);
	}
	
	public boolean isGrid()
	{
		return getType().isGrid();
	}

	public boolean isSectionStart()
	{
		return getType().isSectionStart();
	}

	public String getTag()
	{
		return getFieldSpec().getTag();
	}
	
	public String getLabel()
	{
		return getFieldSpec().getLabel();
	}

	public boolean isRequiredField()
	{
		return getFieldSpec().isRequiredField();
	}

	public FieldSpec getFieldSpec()
	{
		return fieldSpec;
	}
	
	public SimpleStringProperty valueProperty()
	{
		if(isGrid())
			throw new RuntimeException("valueProperty not available for grid: " + getTag());
		
		return valueProperty;
	}

	public void setValue(String value)
	{
		valueProperty.setValue(value);
	}

	public void addValueListener(ChangeListener<String> listener)
	{
		valueProperty.addListener(listener);
	}

	public void clear()
	{
		valueProperty.setValue(null);
	}

	private void setValidator(FieldValidator validatorToUse)
	{
		validator = validatorToUse;
		validator.updateStatus(valueProperty.getValue());
		addValueListener(validator);
	}

	public ObservableBooleanValue fieldIsValidProperty()
	{
		if(isGrid())
			throw new RuntimeException("fieldIsValidProperty not available for grid: " + getTag());

		return validator.fieldIsValidProperty();
	}
	
	public GridFieldData gridDataProperty()
	{
		if(!isGrid())
			throw new RuntimeException("gridDataProperty not available for non-grid: " + getTag());

		return gridDataIfApplicable;
	}

	public void setGridData(String xmlGridData, PoolOfReusableChoicesLists poolOfReusableChoicesLists) throws Exception
	{
		GridFieldSpec gridSpec = (GridFieldSpec) getFieldSpec();
		GridData data = new GridData(gridSpec, poolOfReusableChoicesLists);
		data.setFromXml(xmlGridData);
		
		GridFieldData gridFieldData = gridDataProperty();
		for(int row = 0; row < data.getRowCount(); ++row)
		{
			GridRowData rowData = new GridRowData();
			for(int column = 0; column < data.getColumnCount(); ++column)
			{
				String columnLabel = gridSpec.getColumnLabel(column);
				String value = data.getValueAt(row, column);
				rowData.put(columnLabel, value);
			}
			
			gridFieldData.add(rowData);
		}
	}

	private FieldType getType()
	{
		return getFieldSpec().getType();
	}
	
	private FieldSpec fieldSpec;
	private SimpleStringProperty valueProperty;
	private FieldValidator validator;
	private GridFieldData gridDataIfApplicable;
}
