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
package org.martus.client.swingui.fields;

import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import org.martus.clientside.UiLocalization;
import org.martus.swing.UiComboBox;
import org.martus.swing.Utilities;
import org.martus.util.MultiCalendar;

public class UiDateEditorComponent extends Box
{
	public UiDateEditorComponent(UiLocalization localizationToUse, boolean allowFutureDates)
	{
		super(BoxLayout.X_AXIS);
		localization = localizationToUse;
		allowFuture = allowFutureDates;
		
		yearCombo = createYearCombo();
		monthCombo = createMonthCombo();
		dayCombo = createDayCombo();
	
		addComponentsToBox();
	}

	private UiComboBox createYearCombo()	
	{
		MultiCalendar calToday = new MultiCalendar();
		int thisYear = localization.getLocalizedYear(calToday);	
		int maxYear = thisYear;
		if(allowFuture)
			maxYear += 10;
		
		MultiCalendar cal1900 = MultiCalendar.createFromGregorianYearMonthDay(1900, 1, 1);
		int minYear = localization.getLocalizedYear(cal1900);

		UiComboBox yCombo = new UiComboBox();

		if(THAI_AND_PERSIAN_TESTING)
		{
			System.out.println("WARNING: THAI_AND_PERSIAN Testing mode!!!");
			yCombo.addItem(Integer.toString(1385));
			yCombo.addItem(Integer.toString(2549));
		}
		
		for(int year = minYear; year <= maxYear; ++year)
			yCombo.addItem(new Integer(year).toString());
		
		yCombo.setSelectedItem(new Integer(thisYear).toString());
		return yCombo;
	}		
	
	private UiComboBox createMonthCombo()
	{
		return new UiComboBox(localization.getMonthLabels());
	}
	
	private UiComboBox createDayCombo()
	{
		UiComboBox dCombo = new UiComboBox();
		for(int day=1; day <= 31; ++day)
			dCombo.addItem(new Integer(day).toString());
		return dCombo;
	}
	
	private void addComponentsToBox()
	{
		JComponent[] dateInOrderLeftToRight = getComponentsInOrder();	
		Utilities.addComponentsRespectingOrientation(this, dateInOrderLeftToRight);
	}
	
	public Dimension getPreferredSize()
	{
		Dimension preferredSize = super.getPreferredSize();
		preferredSize.width += EXTRA_WIDTH_SO_FIELDS_DISPLAY_WHEN_COLAPSED;
		return preferredSize;
	}

	JComponent[] getComponentsInOrder()
	{
		JComponent[] dateInOrderLeftToRight = new JComponent[3];
		
		String mdyOrder = localization.getMdyOrder();
		for(int i = 0; i < mdyOrder.length(); ++i)
		{
			switch(mdyOrder.charAt(i))
			{
				case 'd': dateInOrderLeftToRight[i]=dayCombo;	break;
				case 'm': dateInOrderLeftToRight[i]=monthCombo;	break;
				case 'y': dateInOrderLeftToRight[i]=yearCombo;	break;
			}
		}
		return dateInOrderLeftToRight;
	}

	public JComponent[] getFocusableComponents()
	{
		return getComponentsInOrder();
	}

	public String getStoredDateText()
	{
		return getDate().toIsoDateString();
	}

	public MultiCalendar getDate()
	{
		int year = Integer.parseInt((String)yearCombo.getSelectedItem());
		int month = monthCombo.getSelectedIndex()+1;
		int day = dayCombo.getSelectedIndex()+1;

		return localization.createCalendarFromLocalizedYearMonthDay(year, month, day);
	}

	public void setStoredDateText(String newText)
	{
		try
		{
			MultiCalendar cal = localization.createCalendarFromIsoDateString(newText);
			setDate(cal);
		}
		catch(Exception e)
		{
			System.out.println(e);
		}			
	}

	public void setDate(MultiCalendar cal)
	{
		yearCombo.setSelectedItem( Integer.toString(localization.getLocalizedYear(cal)));
		monthCombo.setSelectedIndex((localization.getLocalizedMonth(cal) - 1));
		dayCombo.setSelectedIndex((localization.getLocalizedDay(cal) - 1));
	}
	
	public void requestFocus()
	{
		getComponentsInOrder()[0].requestFocus();
	}
	
	// Enable the following to add a Persian year and a 
	// Thai year to the Date Editor year dropdowns
	static final boolean THAI_AND_PERSIAN_TESTING = false;
	
	
	static final int EXTRA_WIDTH_SO_FIELDS_DISPLAY_WHEN_COLAPSED = 20;
	UiLocalization localization;
	boolean allowFuture;
	UiComboBox yearCombo;	
	UiComboBox dayCombo;
	UiComboBox monthCombo;
}
