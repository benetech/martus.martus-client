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

import java.util.Vector;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;

import org.martus.client.swingui.jfx.generic.data.ObservableChoiceItemList;
import org.martus.client.swingui.jfx.landing.bulletins.GridRowFields;
import org.martus.common.GridData;
import org.martus.common.GridRow;
import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.ReusableChoices;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DataInvalidException;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.GridFieldSpec;

public class FxBulletinField
{
	public FxBulletinField(FxBulletin bulletinToUse, FieldSpec fieldSpecToUse, MiniLocalization localizationToUse)
	{
		fxb = bulletinToUse;
		fieldSpec = fieldSpecToUse;
		localization = localizationToUse;
		
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

	public boolean isDropdown()
	{
		return getType().isDropdown();
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
	
	public GridFieldSpec getGridFieldSpec()
	{
		return (GridFieldSpec) getFieldSpec();
	}

	public FxBulletin getBulletin()
	{
		return fxb;
	}

	public SimpleStringProperty valueProperty()
	{
		if(isGrid())
			throw new RuntimeException("valueProperty not available for grid: " + getTag());
		
		return valueProperty;
	}

	public String getValue()
	{
		if(isGrid())
			return getGridValue();
		
		return valueProperty.getValue();
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

	public void setGridData(String xmlGridData) throws Exception
	{
		GridFieldSpec gridSpec = getGridFieldSpec();
		PoolOfReusableChoicesLists poolOfReusableChoicesLists = fxb.getAllReusableChoicesLists();
		GridData data = new GridData(gridSpec, poolOfReusableChoicesLists );
		data.setFromXml(xmlGridData);
		
		for(int row = 0; row < data.getRowCount(); ++row)
		{
			GridRowFields rowFields = appendEmptyGridRow();
			for(int column = 0; column < data.getColumnCount(); ++column)
			{
				String columnLabel = gridSpec.getColumnLabel(column);
				FxBulletinField cellField = rowFields.get(columnLabel);
				String value = data.getValueAt(row, column);
				cellField.setValue(value);
			}
		}
	}

	public GridRowFields appendEmptyGridRow()
	{
		if(!isGrid())
			throw new RuntimeException("Cannot append rows to non-grid field: " + getTag());
		
		GridRowFields newRowFields = createEmptyRow();
		gridDataProperty().add(newRowFields);
		return newRowFields;
	}

	public void removeGridRow(GridRowFields rowToRemove)
	{
		GridFieldData gridData = gridDataProperty();
		if(!gridData.contains(rowToRemove))
			throw new RuntimeException("Attempted to remove grid row that didn't exist");
		
		gridData.remove(rowToRemove);
	}

	public Vector<ObservableChoiceItemList> getChoiceItemLists()
	{
		if(!isDropdown())
			throw new RuntimeException("Field is not a dropdown: " + getTag());
		
		DropDownFieldSpec dropDownSpec = (DropDownFieldSpec) getFieldSpec();
		boolean isDataDriven = (dropDownSpec.getDataSource() != null);
		boolean isReusable = (dropDownSpec.getReusableChoicesCodes().length > 0);
		if(isDataDriven)
			return new Vector<ObservableChoiceItemList>();
		
		if(isReusable)
			return getReusableChoiceItemLists(dropDownSpec);
		
		return getSimpleChoiceItemLists();
	}

	public void validate() throws DataInvalidException
	{
		if(isGrid())
			return;
		
		validator.validate(valueProperty().getValue());
	}

	private Vector<ObservableChoiceItemList> getReusableChoiceItemLists(DropDownFieldSpec dropDownSpec)
	{
		Vector<ObservableChoiceItemList> listOfLists = new Vector<ObservableChoiceItemList>();

		String[] reusableChoicesCodes = dropDownSpec.getReusableChoicesCodes();

		for(int i = 0; i < reusableChoicesCodes.length; ++i)
		{
			String onlyReusableChoicesCode = reusableChoicesCodes[i];
			ReusableChoices reusableChoices = fxb.getReusableChoices(onlyReusableChoicesCode);
			ChoiceItem[] choiceItems = reusableChoices.getChoices();
			ObservableChoiceItemList list = new ObservableChoiceItemList();
			ChoiceItem emptyItemAtTheStartOfEveryReusableList = new ChoiceItem("", "");
			list.add(emptyItemAtTheStartOfEveryReusableList);
			list.addAll(choiceItems);
			
			listOfLists.add(list);
		}
		return listOfLists;
	}

	private Vector<ObservableChoiceItemList> getSimpleChoiceItemLists()
	{
		DropDownFieldSpec dropDownSpec = (DropDownFieldSpec) getFieldSpec();
		Vector<ObservableChoiceItemList> listOfLists = new Vector<ObservableChoiceItemList>();
		ObservableChoiceItemList simpleChoices = new ObservableChoiceItemList();
		simpleChoices.addAll(dropDownSpec.getAllChoices());
		listOfLists.add(simpleChoices);
		return listOfLists;
	}
	
	private String getGridValue()
	{
		PoolOfReusableChoicesLists irrelevantReusableLists = null;
		GridFieldSpec gridSpec = getGridFieldSpec();
		GridData gridData = new GridData(gridSpec, irrelevantReusableLists);
		gridDataProperty().forEach(gridRowFields -> gridData.addRow(convertGridRowFieldsToGridRow(gridRowFields)));
		return gridData.getXmlRepresentation();
	}

	private GridRowFields createEmptyRow()
	{
		GridRowFields rowFields = new GridRowFields();
		GridFieldSpec gridFieldSpec = getGridFieldSpec();
		for(int column = 0; column < gridFieldSpec.getColumnCount(); ++column)
		{
			String columnLabel = gridFieldSpec.getColumnLabel(column);
			FieldSpec cellSpec = gridFieldSpec.getFieldSpec(column);
			FxBulletinField cellField = new FxBulletinField(fxb, cellSpec, getLocalization());
			rowFields.put(columnLabel, cellField);
		}
		return rowFields;
	}

	private GridRow convertGridRowFieldsToGridRow(GridRowFields gridRowFields)
	{
		PoolOfReusableChoicesLists irrelevantReusableLists = null;
		GridFieldSpec gridFieldSpec = getGridFieldSpec();
		GridRow gridRow = new GridRow(gridFieldSpec, irrelevantReusableLists);
		for(int column = 0; column < gridRow.getColumnCount(); ++column)
		{
			FieldSpec columnFieldSpec = gridFieldSpec.getFieldSpec(column);
			String label = columnFieldSpec.getLabel();
			FxBulletinField field = gridRowFields.get(label);
			String value = field.getValue();
			gridRow.setCellText(column, value);
		}
		
		return gridRow;
	}

	private FieldType getType()
	{
		return getFieldSpec().getType();
	}
	
	private MiniLocalization getLocalization()
	{
		return localization;
	}

	private FxBulletin fxb;
	private FieldSpec fieldSpec;
	private MiniLocalization localization;
	private SimpleStringProperty valueProperty;
	private FieldValidator validator;
	private GridFieldData gridDataIfApplicable;
}
