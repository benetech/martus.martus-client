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

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;

import org.martus.common.MartusLogger;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.DataInvalidException;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;

public class FieldValidator implements ChangeListener<String>
{
	public FieldValidator(FieldSpec specToUse, MiniLocalization localizationToUse)
	{
		spec = specToUse;
		localization = localizationToUse;
		fieldIsValidProperty = new SimpleBooleanProperty(); 
	}
	
	public ObservableBooleanValue fieldIsValidProperty()
	{
		return fieldIsValidProperty;
	}
	
	@Override
	public void changed(ObservableValue<? extends String> property, String oldValue, String newValue)
	{
		updateStatus(newValue);
	}

	public void updateStatus(String newValue)
	{
		boolean isValid = false;
		try
		{
			if(newValue == null)
				newValue = "";
			FieldValidator.validateField(spec, newValue, localization);
			isValid = true;
		} 
		catch (DataInvalidException noNeedToLogOrThrow)
		{
			isValid = false;
		}
		
		fieldIsValidProperty.setValue(isValid);
	}

	protected static void validateField(FieldSpec spec, String value, MiniLocalization localization) throws DataInvalidException
	{
		String label = ZawgyiLabelUtilities.getDisplayableLabel(spec, localization);
		validateField(spec, label, value, localization);
	}

	private static void validateField(FieldSpec spec, String displayableLabel, String fieldDataValue, MiniLocalization localization) throws DataInvalidException
	{
		FieldType type = spec.getType();
		if(type.isGrid())
		{
			MartusLogger.logError("******* Validation not handled yet for " + type.getTypeName());
			return;
		}
		spec.validate(displayableLabel, fieldDataValue, localization);
	}

	private FieldSpec spec;
	private MiniLocalization localization;
	private SimpleBooleanProperty fieldIsValidProperty;
}