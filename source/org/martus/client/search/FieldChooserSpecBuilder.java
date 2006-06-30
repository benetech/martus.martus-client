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
package org.martus.client.search;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.clientside.UiLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.field.MartusDateRangeField;
import org.martus.common.field.MartusGridField;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.PopUpTreeFieldSpec;
import org.martus.common.fieldspec.SearchFieldTreeModel;
import org.martus.common.fieldspec.SearchableFieldChoiceItem;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;

public class FieldChooserSpecBuilder
{
	public FieldChooserSpecBuilder(UiLocalization localizationToUse)
	{
		localization = localizationToUse;
	}
	
	public PopUpTreeFieldSpec createSpec(ClientBulletinStore storeToUse)
	{
		FieldChoicesByLabel allAvailableFields = new FieldChoicesByLabel();
		addSpecialFields(allAvailableFields);
		allAvailableFields.add(createLastSavedDateChoice());
		allAvailableFields.addAll(convertToChoiceItems(storeToUse.getAllKnownFieldSpecs()));
		
		SearchFieldTreeModel fieldChoiceModel = new SearchFieldTreeModel(allAvailableFields.asTree(getLocalization()));
		PopUpTreeFieldSpec fieldColumnSpec = new PopUpTreeFieldSpec(fieldChoiceModel);
		fieldColumnSpec.setLabel(getLocalization().getFieldLabel("SearchGridHeaderField"));
		return fieldColumnSpec;
	}
	
	public void addSpecialFields(FieldChoicesByLabel fields)
	{
	}
	
	private ChoiceItem createLastSavedDateChoice()
	{
		String tag = Bulletin.PSEUDOFIELD_LAST_SAVED_DATE;
		String label = getLocalization().getFieldLabel(Bulletin.TAGLASTSAVED);
		FieldType type = new FieldTypeDate();
		FieldSpec spec = FieldSpec.createCustomField(tag, label, type);
		return new SearchableFieldChoiceItem(spec);
	}

	private Set convertToChoiceItems(Set specs)
	{
		Set allChoices = new HashSet();
		Iterator iter = specs.iterator();
		while(iter.hasNext())
		{
			FieldSpec spec = (FieldSpec)iter.next();
			allChoices.addAll(getChoiceItemsForThisField(spec));
		}
			
		return allChoices;
	}

	public Set getChoiceItemsForThisField(FieldSpec spec)
	{
		return getChoiceItemsForThisField(spec, spec.getTag(), "");
	}
	
	public Set getChoiceItemsForThisField(FieldSpec spec, String fullTagChain, String displayPrefix)
	{

		Set choicesForThisField = new HashSet();
		final FieldType thisType = spec.getType();
		
		if(shouldOmitType(thisType))
			return choicesForThisField;
		
		String displayString = spec.getLabel();
		if(StandardFieldSpecs.isStandardFieldTag(fullTagChain))
			displayString = getLocalization().getFieldLabel(fullTagChain);
		else if(displayString.trim().equals(""))
			displayString = fullTagChain;

		displayString = displayPrefix + displayString;

		// unknown types (Lewis had one) should not appear in the list at all
		if(thisType.isUnknown())
			return choicesForThisField;

		// dateranges create multiple entries
		if(thisType.isDateRange())
		{
			choicesForThisField.addAll(getDateRangeChoiceItem(fullTagChain, MartusDateRangeField.SUBFIELD_BEGIN, displayString));
			choicesForThisField.addAll(getDateRangeChoiceItem(fullTagChain, MartusDateRangeField.SUBFIELD_END, displayString));
			return choicesForThisField;
		}
		
		// dropdowns MUST be a DropDownFieldSpec, not a plain FieldSpec
		if(thisType.isDropdown())
		{
			DropDownFieldSpec originalSpec = (DropDownFieldSpec)spec;
			DropDownFieldSpec specWithBetterLabel = new DropDownFieldSpec(originalSpec.getAllChoices());
			specWithBetterLabel.setTag(fullTagChain);
			specWithBetterLabel.setLabel(displayString);
			choicesForThisField.add(new SearchableFieldChoiceItem(specWithBetterLabel));
			return choicesForThisField;
		}

		// add one choice per column
		if(thisType.isGrid())
		{
			GridFieldSpec gridSpec = (GridFieldSpec)spec;
			for(int i=0; i < gridSpec.getColumnCount(); ++i)
			{
				final FieldSpec columnSpec = gridSpec.getFieldSpec(i);
				String columnTag = fullTagChain + "." + MartusGridField.sanitizeLabel(columnSpec.getLabel());
				choicesForThisField.addAll(getChoiceItemsForThisField(columnSpec, columnTag, displayString + ": "));
			}
			return choicesForThisField;
		}

		// many types just create a choice with their own type,
		// but we need to default to NORMAL for safety
		FieldType choiceSpecType = new FieldTypeNormal();
		if(shouldSearchSpecTypeBeTheFieldSpecType(thisType))
			choiceSpecType = thisType;

		FieldSpec thisSpec = FieldSpec.createCustomField(fullTagChain, displayString, choiceSpecType);
		ChoiceItem choiceItem = new SearchableFieldChoiceItem(thisSpec);
		choicesForThisField.add(choiceItem);
		return choicesForThisField;
	}
	
	public boolean shouldOmitType(FieldType type)
	{
		return false;
	}

	private boolean shouldSearchSpecTypeBeTheFieldSpecType(final FieldType thisType)
	{
		return (thisType.isDate() || thisType.isLanguage() || thisType.isBoolean()); 
	}
	
	private Set getDateRangeChoiceItem(String tag, String subfield, String baseDisplayString) 
	{
		Set itemIfAny = new HashSet();
		String fullTag = tag + "." + subfield;
		String displayTemplate = getLocalization().getFieldLabel("DateRangeTemplate" + subfield);
		try
		{
			String fullDisplayString = TokenReplacement.replaceToken(displayTemplate, "#FieldLabel#", baseDisplayString);
			FieldSpec dateSpec = FieldSpec.createCustomField(fullTag, fullDisplayString, new FieldTypeDate());
			itemIfAny.add(new SearchableFieldChoiceItem(dateSpec));
		}
		catch (TokenInvalidException e)
		{
			// bad translation--not much we can do about it
			e.printStackTrace();
		}
		
		return itemIfAny;
	}
	
	UiLocalization getLocalization()
	{
		return localization;
	}

	UiLocalization localization;
}
