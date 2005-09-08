/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2004, Beneficent
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

import java.util.HashMap;
import java.util.Vector;

import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;

public class CustomFieldSpecValidator
{
	public CustomFieldSpecValidator(FieldSpec[] specsToCheck)
	{
		errors = new Vector();
		if(specsToCheck == null)
		{
			errors.add(CustomFieldError.errorNoSpecs());
			return;
		}
		
		checkForRequiredFields(specsToCheck);
		checkForIllegalTagCharacters(specsToCheck);
		checkForBlankTags(specsToCheck);
		checkForDuplicateFields(specsToCheck);
		checkForMissingCustomLabels(specsToCheck);
		checkForUnknownTypes(specsToCheck);
		checkForLabelsOnStandardFields(specsToCheck);
		checkForDuplicateAndNoDropdownEntries(specsToCheck);
		checkForDuplicateAndNoDropdownEntriesInsideGrids(specsToCheck);
	}
		
	public boolean isValid()
	{
		if(errors.size()>0)
			return false;
		return true;
	}
	
	public Vector getAllErrors()
	{
		return errors;
	}

	private void checkForRequiredFields(FieldSpec[] specsToCheck)
	{
		Vector missingTags = new Vector();
		missingTags.add(BulletinConstants.TAGAUTHOR);
		missingTags.add(BulletinConstants.TAGLANGUAGE);
		missingTags.add(BulletinConstants.TAGENTRYDATE);
		missingTags.add(BulletinConstants.TAGTITLE);
		for (int i = 0; i < specsToCheck.length; i++)
		{
			String tag = specsToCheck[i].getTag();
			if(missingTags.contains(tag))
			missingTags.remove(tag);
		}
		
		for (int j = 0; j < missingTags.size(); j++)
			errors.add(CustomFieldError.errorRequiredField((String)missingTags.get(j)));
	}
	
	private void checkForBlankTags(FieldSpec[] specsToCheck)
	{
		for (int i = 0; i < specsToCheck.length; i++)
		{
			FieldSpec thisSpec = specsToCheck[i];
			String tag = thisSpec.getTag();
			if(tag.length() == 0)
				errors.add(CustomFieldError.errorBlankTag(thisSpec.getLabel(), getType(thisSpec)));				
		}
	}
	
	private void checkForIllegalTagCharacters(FieldSpec[] specsToCheck)
	{
		for (int i = 0; i < specsToCheck.length; i++)
		{
			FieldSpec thisSpec = specsToCheck[i];
			boolean allValid = true;
			String thisTag = thisSpec.getTag();
			if(thisTag.length() < 1)
				continue;
			char[] tagChars = thisTag.toCharArray();
			if(!isValidFirstTagCharacter(tagChars[0]))
				allValid = false;
			for(int j = 1; j < tagChars.length; ++j)
			{
				if(!isValidTagCharacter(tagChars[j]))
					allValid = false;
			}
			if(!allValid)
				errors.add(CustomFieldError.errorIllegalTag(thisTag, thisSpec.getLabel(), getType(thisSpec)));
		}
	}
	
	private boolean isValidTagCharacter(char c)
	{
		if(isValidFirstTagCharacter(c))
			return true;
		if(c > 128)
			return true;
		if(c == '-' || c == '.')
			return true;
		return false;
	}
	
	private boolean isValidFirstTagCharacter(char c)
	{
		if(Character.isLetterOrDigit(c))
			return true;
		if(c == '_')
			return true;
		return false;
	}
	
	private void checkForDuplicateFields(FieldSpec[] specsToCheck)
	{
		Vector foundTags = new Vector();
		for (int i = 0; i < specsToCheck.length; i++)
		{
			FieldSpec thisSpec = specsToCheck[i];
			String tag = thisSpec.getTag();
			if(foundTags.contains(tag))
				errors.add(CustomFieldError.errorDuplicateFields(thisSpec.getTag(), thisSpec.getLabel(), getType(thisSpec)));				
			foundTags.add(tag);
		}
	}
	
	private void checkForDuplicateAndNoDropdownEntries(FieldSpec[] specsToCheck)
	{
		for (int i = 0; i < specsToCheck.length; i++)
		{
			FieldSpec thisSpec = specsToCheck[i];
			if(thisSpec.getType() == FieldSpec.TYPE_DROPDOWN)
			{
				DropDownFieldSpec dropdownSpec = (DropDownFieldSpec)thisSpec;
				String tag = thisSpec.getTag();
				String label = thisSpec.getLabel();
				checkForDuplicateEntriesInDropDownSpec(dropdownSpec, tag, label);
				checkForNoDropdownChoices(dropdownSpec, tag, label);
			}
		}
	}
	
	private void checkForDuplicateAndNoDropdownEntriesInsideGrids(FieldSpec[] specsToCheck)
	{
		for (int i = 0; i < specsToCheck.length; i++)
		{
			FieldSpec thisSpec = specsToCheck[i];
			if(thisSpec.getType() == FieldSpec.TYPE_GRID)
			{
				GridFieldSpec gridSpec = (GridFieldSpec)thisSpec;
				for(int columns = 0; columns < gridSpec.getColumnCount(); ++columns)
				{
					FieldSpec columnSpec = gridSpec.getFieldSpec(columns);
					if(columnSpec.getType() == FieldSpec.TYPE_DROPDOWN)
					{
						checkForDuplicateEntriesInDropDownSpec((DropDownFieldSpec)columnSpec, gridSpec.getTag(), gridSpec.getLabel());
						checkForNoDropdownChoices((DropDownFieldSpec)columnSpec, gridSpec.getTag(), gridSpec.getLabel());
					}
				}
			}
		}
	}
	
	private void checkForDuplicateEntriesInDropDownSpec(DropDownFieldSpec dropdownSpec, String tag, String label)
	{
		HashMap labelEntries = new HashMap();
		for(int choice = 0; choice < dropdownSpec.getCount(); ++choice)
		{
			String choiceEntryLabel = dropdownSpec.getValue(choice);
			if(labelEntries.containsKey(choiceEntryLabel))
				errors.add(CustomFieldError.errorDuplicateDropDownEntry(tag, label));				
			labelEntries.put(choiceEntryLabel, choiceEntryLabel);
		}
	}
	
	private void checkForNoDropdownChoices(DropDownFieldSpec dropdownSpec, String tag, String label)
	{
		if(dropdownSpec.getCount() == 0)
			errors.add(CustomFieldError.noDropDownEntries(tag, label));				
	}
	
	private void checkForMissingCustomLabels(FieldSpec[] specsToCheck)
	{
		for (int i = 0; i < specsToCheck.length; i++)
		{
			FieldSpec thisSpec = specsToCheck[i]; 
			String tag = thisSpec.getTag();
			if(StandardFieldSpecs.isCustomFieldTag(tag) && thisSpec.getLabel().equals(""))
				errors.add(CustomFieldError.errorMissingLabel(thisSpec.getTag(), getType(thisSpec)));				
		}
	}

	private void checkForUnknownTypes(FieldSpec[] specsToCheck)
	{
		for (int i = 0; i < specsToCheck.length; i++)
		{
			FieldSpec thisSpec = specsToCheck[i]; 
			if(thisSpec.getType() == FieldSpec.TYPE_UNKNOWN)
				errors.add(CustomFieldError.errorUnknownType(thisSpec.getTag(), thisSpec.getLabel()));				
		}
	}
	
	private void checkForLabelsOnStandardFields(FieldSpec[] specsToCheck)
	{
		for (int i = 0; i < specsToCheck.length; i++)
		{
			FieldSpec thisSpec = specsToCheck[i]; 
			String tag = thisSpec.getTag();
			if(!StandardFieldSpecs.isCustomFieldTag(tag) && !thisSpec.getLabel().equals(""))
				errors.add(CustomFieldError.errorLabelOnStandardField(thisSpec.getTag(), thisSpec.getLabel(), getType(thisSpec)));				
		}
	}
	
	private String getType(FieldSpec thisSpec)
	{
		return FieldSpec.getTypeString( thisSpec.getType());
	}

	private Vector errors;
}
