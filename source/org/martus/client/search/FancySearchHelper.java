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

import java.util.Arrays;
import java.util.Vector;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.common.GridData;
import org.martus.common.field.MartusDateRangeField;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.fieldspec.GridFieldSpec.UnsupportedFieldTypeException;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;

public class FancySearchHelper
{
	public FancySearchHelper(ClientBulletinStore storeToUse, UiDialogLauncher dlgLauncherToUse)
	{
		dlgLauncher = dlgLauncherToUse;
		model = new FancySearchTableModel(getGridSpec(storeToUse));
	}
	
	MartusLocalization getLocalization()
	{
		return dlgLauncher.GetLocalization();
	}
	
	UiDialogLauncher getDialogLauncher()
	{
		return dlgLauncher;
	}
	
	FancySearchTableModel getModel()
	{
		return model;
	}
	
	public DropDownFieldSpec createFieldColumnSpec(ClientBulletinStore storeToUse)
	{
		Vector allAvailableFields = new Vector();
		FieldSpec anyField = FieldSpec.createCustomField("", getLocalization().getFieldLabel("SearchAnyField"), FieldSpec.TYPE_NORMAL);
		allAvailableFields.add(new ChoiceItem(anyField));
		allAvailableFields.addAll(convertToChoiceItems(storeToUse.getAllKnownFieldSpecs()));

		ChoiceItem[] fieldChoices = (ChoiceItem[])allAvailableFields.toArray(new ChoiceItem[0]);
		Arrays.sort(fieldChoices);
		                                  
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
	
	private Vector convertToChoiceItems(Vector specs)
	{
		Vector choices = new Vector();
		for(int i=0; i < specs.size(); ++i)
		{
			FieldSpec spec = (FieldSpec)specs.get(i);
			String tag = spec.getTag();
			String displayString = tag;
			if(StandardFieldSpecs.isStandardFieldTag(tag))
				displayString = getLocalization().getFieldLabel(tag);
			if(spec.getType() == FieldSpec.TYPE_DATERANGE)
			{
				addDateRangeChoiceItem(choices, tag, MartusDateRangeField.SUBFIELD_BEGIN, displayString);
				addDateRangeChoiceItem(choices, tag, MartusDateRangeField.SUBFIELD_END, displayString);
			}
			else
			{
				FieldSpec thisSpec = FieldSpec.createCustomField(tag, displayString, spec.getType());
				ChoiceItem choiceItem = new ChoiceItem(thisSpec);
				choices.add(choiceItem);
			}
		}
			
		return choices;
	}
	
	private void addDateRangeChoiceItem(Vector choices, String tag, String subfield, String baseDisplayString) 
	{
		String fullTag = tag + "." + subfield;
		String displayTemplate = dlgLauncher.GetLocalization().getFieldLabel("DateRangeTemplate" + subfield);
		try
		{
			String fullDisplayString = TokenReplacement.replaceToken(displayTemplate, "#FIELDLABEL#", baseDisplayString);
			FieldSpec dateSpec = FieldSpec.createCustomField(fullTag, fullDisplayString, FieldSpec.TYPE_DATE);
			choices.add(new ChoiceItem(dateSpec));
		}
		catch (TokenInvalidException e)
		{
			// bad translation--not much we can do about it
			e.printStackTrace();
		}
	}
	
	public GridFieldSpec getGridSpec(ClientBulletinStore storeToUse)
	{
		GridFieldSpec spec = new GridFieldSpec();

		try
		{
			spec.addColumn(createFieldColumnSpec(storeToUse));
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
	
	public static final int COLUMN_ROW_NUMBER = 0;
	public static final int COLUMN_FIELD = 1;
	public static final int COLUMN_COMPARE_HOW = 2;
	public static final int COLUMN_VALUE = 3;
	
	FancySearchTableModel model;
	UiDialogLauncher dlgLauncher;
}

