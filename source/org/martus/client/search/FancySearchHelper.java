/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005, Beneficent
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

package org.martus.client.search;

import java.util.Vector;

import org.martus.client.swingui.grids.GridTableModel;
import org.martus.common.GridData;
import org.martus.common.MiniLocalization;
import org.martus.common.clientside.ChoiceItem;
import org.martus.common.field.MartusDateRangeField;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.fieldspec.GridFieldSpec.UnsupportedFieldTypeException;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;

public class FancySearchHelper
{
	public FancySearchHelper(MiniLocalization localizationToUse)
	{
		localization = localizationToUse;
	}
	
	MiniLocalization getLocalization()
	{
		return localization;
	}
	
	public DropDownFieldSpec createFieldColumnSpec()
	{
		Vector allAvailableFields = new Vector();
		allAvailableFields.add(new ChoiceItem("", getLocalization().getFieldLabel("SearchAnyField"), FieldSpec.TYPE_NORMAL));
		allAvailableFields.addAll(convertToChoiceItems(StandardFieldSpecs.getDefaultPublicFieldSpecs()));
		allAvailableFields.addAll(convertToChoiceItems(StandardFieldSpecs.getDefaultPrivateFieldSpecs()));

		ChoiceItem[] fieldChoices = (ChoiceItem[])allAvailableFields.toArray(new ChoiceItem[0]);
		                                  
		DropDownFieldSpec fieldColumnSpec = new DropDownFieldSpec();
		fieldColumnSpec.setLabel(getLocalization().getFieldLabel("SearchGridHeaderField"));
		fieldColumnSpec.setChoices(fieldChoices);
		return fieldColumnSpec;
	}
	
	public DropDownFieldSpec createOpColumnSpec()
	{
		ChoiceItem[] opChoices = 
		{
			new ChoiceItem(":", getLocalization().getFieldLabel("SearchOpContains")),
			new ChoiceItem(":>", ">"),
			new ChoiceItem(":>=", ">="),
			new ChoiceItem(":<", "<"),
			new ChoiceItem(":<=", "<="),
		};
		                                  
		DropDownFieldSpec opSpec = new DropDownFieldSpec();
		opSpec.setLabel(getLocalization().getFieldLabel("SearchGridHeaderOp"));
		opSpec.setChoices(opChoices);
		return opSpec;
	}
	
	private Vector convertToChoiceItems(FieldSpec[] specs)
	{
		Vector choices = new Vector();
		for(int i=0; i < specs.length; ++i)
		{
			String tag = specs[i].getTag();
			String displayString = tag;
			if(StandardFieldSpecs.isStandardFieldTag(tag))
				displayString = getLocalization().getFieldLabel(tag);
			if(specs[i].getType() == FieldSpec.TYPE_DATERANGE)
			{
				addDateRangeChoiceItem(choices, tag, MartusDateRangeField.SUBFIELD_BEGIN, displayString);
				addDateRangeChoiceItem(choices, tag, MartusDateRangeField.SUBFIELD_END, displayString);
			}
			else
			{
				ChoiceItem choiceItem = new ChoiceItem(specs[i].getTag(), displayString, specs[i].getType());
				choices.add(choiceItem);
			}
		}
			
		return choices;
	}
	
	private void addDateRangeChoiceItem(Vector choices, String tag, String subfield, String baseDisplayString) 
	{
		String fullTag = tag + "." + subfield;
		String displayTemplate = localization.getFieldLabel("DateRangeTemplate" + subfield);
		try
		{
			String fullDisplayString = TokenReplacement.replaceToken(displayTemplate, "#FIELDLABEL#", baseDisplayString);
			choices.add(new ChoiceItem(fullTag, fullDisplayString, FieldSpec.TYPE_DATE));
		}
		catch (TokenInvalidException e)
		{
			// bad translation--not much we can do about it
			e.printStackTrace();
		}
	}
	
	public GridTableModel getSearchTableModel()
	{
		return new FancySearchTableModel(getGridSpec());
	}
	
	public GridFieldSpec getGridSpec()
	{
		GridFieldSpec spec = new GridFieldSpec();

		try
		{
			spec.addColumn(createFieldColumnSpec());
			spec.addColumn(createOpColumnSpec());
			
			String columnTag = "value";
			String columnHeader = getLocalization().getFieldLabel("SearchGridHeaderValue");
			spec.addColumn(FieldSpec.createCustomField(columnTag, columnHeader, FieldSpec.TYPE_MORPHIC));
		}
		catch (UnsupportedFieldTypeException e)
		{
			// TODO: better error handling?
			e.printStackTrace();
			throw new RuntimeException();
		}
		return spec;
	}

	public String getSearchString(GridData gridData)
	{
		StringBuffer searchExpression = new StringBuffer();
		for(int row = 0; row < gridData.getRowCount(); ++row)
		{
			String field = gridData.getValueAt(row, 0);
			String op = gridData.getValueAt(row, 1);
			String value = gridData.getValueAt(row, 2);
			value = value.trim();
			value = value.replaceAll("\\\"", "");
		
			if(field.length() > 0)
			{
				searchExpression.append(field);
				searchExpression.append(op);
			}
			
			searchExpression.append("\"");
			searchExpression.append(value);
			searchExpression.append("\"");
			searchExpression.append(" ");
		}

		return new String(searchExpression);
	}
	
	MiniLocalization localization;
}

