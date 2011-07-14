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

package org.martus.client.search;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.SafeReadableBulletin;
import org.martus.client.swingui.dialogs.UiProgressWithCancelDlg;
import org.martus.client.swingui.grids.GridTableModel;
import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.field.MartusField;
import org.martus.common.field.MartusSearchableGridColumnField;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.fieldspec.PopUpTreeFieldSpec;
import org.martus.common.packet.UniversalId;

public class FancySearchTableModel extends GridTableModel implements TableModelListener
{

	public FancySearchTableModel(ClientBulletinStore storeToUse, GridFieldSpec fieldSpecToUse, PoolOfReusableChoicesLists reusableChoicesList, MiniLocalization localizationToUse)
	{
		super(fieldSpecToUse, reusableChoicesList);
		store = storeToUse;
		localization = localizationToUse;
		addTableModelListener(this);
		memorizedFieldValues = new HashMap();
	}
	
	public FieldSpec getFieldSpecForCell(int row, int column)
	{
		if(column == valueColumn)
			return getCurrentValueColumnSpec(getSelectedFieldSpec(row), (String)getValueAt(row, opColumn));
		else if(column == opColumn)
			return getCurrentOpColumnSpec(getSelectedFieldSpec(row).getType());
		else
			return super.getFieldSpecForCell(row, column);
	}

	public FieldSpec getSelectedFieldSpec(int row)
	{
		String selectedFieldTag = (String)getValueAt(row, fieldColumn);
		PopUpTreeFieldSpec fieldColumnSpec = (PopUpTreeFieldSpec)getFieldSpecForColumn(fieldColumn);
		return getFieldSpecForChosenField(selectedFieldTag, fieldColumnSpec);
	}

	public static FieldSpec getFieldSpecForChosenField(String selectedFieldTag, PopUpTreeFieldSpec fieldColumnSpec)
	{
		ChoiceItem selectedFieldChoiceItem = fieldColumnSpec.findCode(selectedFieldTag);
		if(selectedFieldChoiceItem == null)
		{
			throw new RuntimeException("Couldn't find " + selectedFieldTag + " in " + fieldColumnSpec.toString());
		}
		FieldSpec selectedFieldSpec = selectedFieldChoiceItem.getSpec();
		return selectedFieldSpec;
	}

	private FieldSpec getCurrentValueColumnSpec(FieldSpec selectedFieldSpec, String op)
	{
		if(selectedFieldSpec.getType().isLanguageDropdown())
			selectedFieldSpec = new DropDownFieldSpec(localization.getLanguageNameChoices());

		if(canUseMemorizedPossibleValues(selectedFieldSpec))
		{
			MiniFieldSpec miniSpec = new MiniFieldSpec(selectedFieldSpec);
			HashSet choices = (HashSet) memorizedFieldValues.get(miniSpec);
			if(choices != null)
			{
				DropDownFieldSpec spec = new DropDownFieldSpec((ChoiceItem[])choices.toArray(new ChoiceItem[0]));
				return spec;
			}
		}
		return selectedFieldSpec;
	}
	
	public boolean hasMemorizedPossibleValues(FieldSpec selectedFieldSpec)
	{
		MiniFieldSpec miniSpec = new MiniFieldSpec(selectedFieldSpec);
		return memorizedFieldValues.containsKey(miniSpec);
	}

	public boolean canUseMemorizedPossibleValues(FieldSpec selectedFieldSpec)
	{
		if(selectedFieldSpec.getType().isString())
			return true;
		
		if(selectedFieldSpec.getType().isDropdown())
		{
			DropDownFieldSpec spec = (DropDownFieldSpec) selectedFieldSpec;
			if(spec.getDataSourceGridTag() != null)
				return true;
		}
		return false;
	}
	
	protected void memorizeFieldValuesFromAllBulletinRevisions(UiProgressWithCancelDlg progressMeter, int rowOfField)
	{
		FieldSpec fieldSpec = getSelectedFieldSpec(rowOfField);
		MiniFieldSpec miniSpec = new MiniFieldSpec(fieldSpec);
		HashSet choices = (HashSet)memorizedFieldValues.get(miniSpec);
		if(choices == null)
		{
			choices = getFieldValueFromAllBulletinRevisions(progressMeter, miniSpec);
			memorizedFieldValues.put(miniSpec, choices);
		}
		progressMeter.finished();
	}

	private HashSet getFieldValueFromAllBulletinRevisions(UiProgressWithCancelDlg progressMeter, MiniFieldSpec miniSpec)
	{
		HashSet choices = new HashSet();
		Vector allUids = store.getUidsOfAllBulletinRevisions();
		for(int i = 0; i < allUids.size(); ++i)
		{
			progressMeter.updateProgressMeter(i, allUids.size());
			if(progressMeter.shouldExit())
				return null;

			Bulletin revision = store.getBulletinRevision((UniversalId) allUids.get(i));
			SafeReadableBulletin bulletin = new SafeReadableBulletin(revision, localization);
			MartusField field = bulletin.getPossiblyNestedField(miniSpec);
			if(field != null)
			{
				if(field.isGridColumnField())
				{
					choices.addAll(getGridColumnChoices((MartusSearchableGridColumnField)field));
				}
				else
				{
					String value = field.getData();
					choices.add(createChoiceItem(value));
				}
			}
		}
		return choices;
	}

	private ChoiceItem createChoiceItem(String value)
	{
		return new ChoiceItem("\"" + value + "\"", value);
	}
	
	private Collection getGridColumnChoices(MartusSearchableGridColumnField field)
	{
		HashSet choices = new HashSet();
		for(int row = 0; row < field.getRowCount(); ++row)
		{
			String value = field.getData(row);
			choices.add(createChoiceItem(value));
		}
		return choices;
	}

	private static Vector getCompareChoices()
	{
		Vector opChoiceVector = new Vector();
		opChoiceVector.add(new ChoiceItem(">", ">"));
		opChoiceVector.add(new ChoiceItem(">=", ">="));
		opChoiceVector.add(new ChoiceItem("<", "<"));
		opChoiceVector.add(new ChoiceItem("<=", "<="));
		return opChoiceVector;
	}

	private static Vector getExactChoices()
	{
		Vector opChoiceVector = new Vector();
		opChoiceVector.add(new ChoiceItem("=", "="));
		opChoiceVector.add(new ChoiceItem("!=", "!="));
		return opChoiceVector;
	}

	private static Vector getContainsChoices(MiniLocalization localization)
	{
		Vector opChoiceVector = new Vector();
		opChoiceVector.add(new ChoiceItem("", localization.getFieldLabel("SearchOpContains")));
		return opChoiceVector;
	}
	
	public DropDownFieldSpec getCurrentOpColumnSpec(FieldType selectedFieldType)
	{
		MiniLocalization uiLocalization = localization;

		return getCurrentOpColumnSpec(selectedFieldType, uiLocalization);
	}

	public static DropDownFieldSpec getCurrentOpColumnSpec(FieldType selectedFieldType, MiniLocalization localization)
	{
		Vector opChoiceVector = new Vector();
		if(selectedFieldType.isString() || selectedFieldType.isMultiline())
		{
			opChoiceVector.addAll(getContainsChoices(localization));
			opChoiceVector.addAll(getExactChoices());
			opChoiceVector.addAll(getCompareChoices());
		}
		else if(selectedFieldType.isDate())
		{
			opChoiceVector.addAll(getExactChoices());
			opChoiceVector.addAll(getCompareChoices());
		}
		else if(selectedFieldType.isLanguageDropdown() || selectedFieldType.isBoolean() || selectedFieldType.isDropdown())
		{
			opChoiceVector.addAll(getExactChoices());
		}
		else if(selectedFieldType.isAnyField())
		{
			opChoiceVector.addAll(getContainsChoices(localization));
		}
		else if(selectedFieldType.isGrid())
		{
			opChoiceVector.addAll(getContainsChoices(localization));
		}
		else
		{
			throw new RuntimeException("Don't know ops for type: " + selectedFieldType.getTypeName());
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
	
	private MiniLocalization localization;
	private ClientBulletinStore store;
	private HashMap memorizedFieldValues;
}

