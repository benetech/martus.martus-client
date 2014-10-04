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
package org.martus.client.swingui.jfx.generic.controls;

import java.time.LocalDate;

import org.martus.util.MultiCalendar;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.DatePicker;

public class MartusDatePicker extends DatePicker
{
	public MartusDatePicker()
	{
		overallValueProperty = new SimpleStringProperty();
		valueProperty().addListener((observable, oldValue, newValue) -> updateOverallValue());
	}
	
	public void setValue(String existingDateString)
	{
		LocalDate localDate = convertIsoDateStringToLocalDate(existingDateString);
		setValue(localDate);
	}

	public static LocalDate convertIsoDateStringToLocalDate(String existingDateString)
	{
		if(existingDateString.isEmpty())
			return null;
		
		MultiCalendar multiCalendar = MultiCalendar.createFromIsoDateString(existingDateString);
		return DateRangePicker.getLocalDate(multiCalendar);
	}
	
	public ReadOnlyStringProperty overallValueProperty()
	{
		return overallValueProperty;
	}

	private void updateOverallValue()
	{
		LocalDate localDate = getValue();
		String isoDate = convertLocalDateToString(localDate);
		overallValueProperty.setValue(isoDate);
	}
	
	private String convertLocalDateToString(LocalDate localDate)
	{
		if(localDate == null)
			return "";
		
		MultiCalendar multiCalendar = DateRangePicker.convertLocalDateToMultiCalendar(localDate);
		return multiCalendar.toIsoDateString();
	}

	private SimpleStringProperty overallValueProperty;
}
