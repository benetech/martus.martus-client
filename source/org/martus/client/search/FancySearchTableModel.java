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

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.martus.client.swingui.grids.GridTableModel;
import org.martus.clientside.UiLocalization;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;

public class FancySearchTableModel extends GridTableModel implements TableModelListener
{

	public FancySearchTableModel(GridFieldSpec fieldSpecToUse, UiLocalization localizationToUse)
	{
		super(fieldSpecToUse);
		localization = localizationToUse;
		addTableModelListener(this);
	}
	
	public FieldSpec getFieldSpecForCell(int row, int column)
	{
		if(column == valueColumn)
			return getCurrentValueColumnSpec(getSelectedFieldSpec(row));
		else if(column == opColumn)
			return getCurrentOpColumnSpec(getSelectedFieldSpec(row).getType());
		else
			return super.getFieldSpecForCell(row, column);
	}

	private FieldSpec getSelectedFieldSpec(int row)
	{
		String selectedFieldTag = (String)getValueAt(row, fieldColumn);
		DropDownFieldSpec fieldColumnSpec = (DropDownFieldSpec)getFieldSpecForColumn(fieldColumn);
		ChoiceItem selectedFieldChoiceItem = fieldColumnSpec.getChoice(fieldColumnSpec.findCode(selectedFieldTag));
		FieldSpec selectedFieldSpec = selectedFieldChoiceItem.getSpec();
		return selectedFieldSpec;
	}

	private FieldSpec getCurrentValueColumnSpec(FieldSpec selectedFieldSpec)
	{
		if(selectedFieldSpec.getType() == FieldSpec.TYPE_LANGUAGE)
			selectedFieldSpec = new DropDownFieldSpec(localization.getLanguageNameChoices());

		return selectedFieldSpec;
	}
	
	private static Vector getCompareChoices()
	{
		Vector opChoiceVector = new Vector();
		opChoiceVector.add(new ChoiceItem(":>", ">"));
		opChoiceVector.add(new ChoiceItem(":>=", ">="));
		opChoiceVector.add(new ChoiceItem(":<", "<"));
		opChoiceVector.add(new ChoiceItem(":<=", "<="));
		return opChoiceVector;
	}

	private static Vector getExactChoices()
	{
		Vector opChoiceVector = new Vector();
		opChoiceVector.add(new ChoiceItem(":=", "="));
		opChoiceVector.add(new ChoiceItem(":!=", "!="));
		return opChoiceVector;
	}

	private static Vector getContainsChoices(UiLocalization localization)
	{
		Vector opChoiceVector = new Vector();
		opChoiceVector.add(new ChoiceItem(":", localization.getFieldLabel("SearchOpContains")));
		return opChoiceVector;
	}
	
	public DropDownFieldSpec getCurrentOpColumnSpec(int selectedFieldType)
	{
		UiLocalization uiLocalization = localization;

		return getCurrentOpColumnSpec(selectedFieldType, uiLocalization);
	}

	public static DropDownFieldSpec getCurrentOpColumnSpec(int selectedFieldType, UiLocalization localization)
	{
		Vector opChoiceVector = new Vector();
		switch(selectedFieldType)
		{
			case FieldSpec.TYPE_NORMAL:
			case FieldSpec.TYPE_MULTILINE:
				opChoiceVector.addAll(getContainsChoices(localization));
				opChoiceVector.addAll(getExactChoices());
				opChoiceVector.addAll(getCompareChoices());
				break;
			case FieldSpec.TYPE_DATE:
				opChoiceVector.addAll(getExactChoices());
				opChoiceVector.addAll(getCompareChoices());
				break;
			case FieldSpec.TYPE_LANGUAGE:
			case FieldSpec.TYPE_BOOLEAN:
			case FieldSpec.TYPE_DROPDOWN:
				opChoiceVector.addAll(getExactChoices());
				break;
			case FieldSpec.TYPE_ANY_FIELD:
				opChoiceVector.addAll(getContainsChoices(localization));
				break;
			case FieldSpec.TYPE_DATERANGE:
			case FieldSpec.TYPE_MESSAGE:
			case FieldSpec.TYPE_GRID:
			default:
				throw new RuntimeException("Don't know ops for type: " + selectedFieldType);
		}
		ChoiceItem[] opChoices = (ChoiceItem[])opChoiceVector.toArray(new ChoiceItem[0]); 
		DropDownFieldSpec opSpec = new DropDownFieldSpec();
		opSpec.setLabel(localization.getFieldLabel("SearchGridHeaderOp"));
		opSpec.setChoices(opChoices);
		return opSpec;
	}

	public void tableChanged(TableModelEvent event)
	{
		if(event.getColumn() == fieldColumn)
		{
			int row = event.getFirstRow();
			
			FieldSpec targetValueSpec = getFieldSpecForCell(row, valueColumn);
			String defaultValueValue = targetValueSpec.getDefaultValue();
			setValueAt(defaultValueValue, row, valueColumn);
			
			FieldSpec targetOpSpec = getFieldSpecForCell(row, opColumn);
			String defaultOpValue = targetOpSpec.getDefaultValue();
			setValueAt(defaultOpValue, row, opColumn);
		}
	}
	
	public static int fieldColumn = 1;
	public static int opColumn = 2;
	public static int valueColumn = 3;
	
	UiLocalization localization;

}

