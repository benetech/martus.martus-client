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

package org.martus.client.swingui.fields;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.Box;
import javax.swing.JComponent;

import org.martus.clientside.UiLocalization;
import org.martus.swing.UiComboBox;
import org.martus.swing.Utilities;
import org.martus.util.MultiCalendar;

public class UiDateEditor extends UiField
{
	public UiDateEditor(UiLocalization localizationToUse, MultiCalendar highestAllowableDate)
	{
		localization = localizationToUse;
		Box box = Box.createHorizontalBox();
		dayCombo = new UiComboBox();
		monthCombo = new UiComboBox(localization.getMonthLabels());
		yearCombo = new UiComboBox();
		
		buildCustomDate(box, localization, yearCombo, monthCombo, dayCombo);
		maxDate = highestAllowableDate;
 				
		component = box;
	}
	
	public static void buildCustomDate(Box box, UiLocalization localizationToUse,
			UiComboBox yCombo, UiComboBox mCombo, UiComboBox dCombo)
	{							
		buildDay(dCombo);
		buildCustomYear(yCombo);
		addComponentsToBox(box,localizationToUse, yCombo, mCombo,dCombo);
	}
	
	public static void buildDate(Box box, UiLocalization localizationToUse,
			UiComboBox yCombo, UiComboBox mCombo, UiComboBox dCombo)
	{							
		buildDay(dCombo);
		buildYear(yCombo);
		addComponentsToBox(box,localizationToUse, yCombo, mCombo,dCombo);
	}
	
	private static void buildCustomYear(UiComboBox yCombo)	
	{
		Calendar cal = new GregorianCalendar();
		int thisYear = cal.get(Calendar.YEAR);			
		
		for(int year = 1900; year <= thisYear+10;++year)
			yCombo.addItem(new Integer(year).toString());
		
		yCombo.setSelectedItem(new Integer(thisYear).toString());	
	}		
	
	private static void buildYear(UiComboBox yCombo)	
	{
		MultiCalendar cal = new MultiCalendar();
		int thisYear = cal.getGregorianYear();			
		
		for(int year = 1900; year <= thisYear; ++year)
			yCombo.addItem(new Integer(year).toString());			
	}		
	
	private static void buildDay(UiComboBox dCombo)
	{
		for(int day=1; day <= 31; ++day)
			dCombo.addItem(new Integer(day).toString());	
	}
	
	private static void addComponentsToBox(Box box, UiLocalization localizationToUse,UiComboBox yCombo, UiComboBox mCombo, UiComboBox dCombo)
	{
		JComponent[] dateInOrderLeftToRight = getComponentsInOrder(yCombo, mCombo, dCombo, localizationToUse);	
		Utilities.addComponentsRespectingOrientation(box, dateInOrderLeftToRight);
	}

	static JComponent[] getComponentsInOrder(UiComboBox yCombo, UiComboBox mCombo, UiComboBox dCombo, UiLocalization localizationForOrdering)
	{
		JComponent[] dateInOrderLeftToRight = new JComponent[3];
		
		String mdyOrder = localizationForOrdering.getMdyOrder();
		for(int i = 0; i < mdyOrder.length(); ++i)
		{
			switch(mdyOrder.charAt(i))
			{
				case 'd': dateInOrderLeftToRight[i]=dCombo;	break;
				case 'm': dateInOrderLeftToRight[i]=mCombo;	break;
				case 'y': dateInOrderLeftToRight[i]=yCombo;	break;
			}
		}
		return dateInOrderLeftToRight;
	}

	public JComponent getComponent()
	{
		return component;
	}

	public JComponent[] getFocusableComponents()
	{
		return getComponentsInOrder(yearCombo, monthCombo, dayCombo, localization);
	}

	public static class DateFutureException extends UiField.DataInvalidException
	{
		public DateFutureException()
		{
			super();
		}
		public DateFutureException(String tag)
		{
			super(tag);
		}

	}
	
	public void validate() throws UiField.DataInvalidException 
	{
		if(maxDate == null)
			return;
	
		MultiCalendar value = getDate(yearCombo, monthCombo, dayCombo);
		if (value.after(maxDate))
		{
			dayCombo.requestFocus();	
			throw new DateFutureException();
		}
	}

	public String getText()
	{
		MultiCalendar date = getDate(yearCombo, monthCombo, dayCombo);
		return date.toIsoDateString();
	}

	public static MultiCalendar getDate(UiComboBox yCombo, UiComboBox mCombo, UiComboBox dCombo) 
	{
		MultiCalendar cal = new MultiCalendar();
		cal.setGregorian(Integer.parseInt((String)yCombo.getSelectedItem()),
				mCombo.getSelectedIndex(),
				dCombo.getSelectedIndex()+1);
		
		return cal;
	}

	public void setText(String newText)
	{
		setDate(newText, yearCombo, monthCombo, dayCombo);			
	}
	
	public static void setDate(String dateText, UiComboBox yCombo, UiComboBox mCombo, UiComboBox dCombo)
	{
		try
		{
			MultiCalendar cal = MultiCalendar.createFromIsoDateString(dateText);
		
			yCombo.setSelectedItem( (new Integer(cal.getGregorianYear())).toString());
			mCombo.setSelectedIndex((cal.getGregorianMonth() - 1));
			dCombo.setSelectedItem( (new Integer(cal.getGregorianDay())).toString());
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}	

	JComponent component;
	UiComboBox monthCombo;
	UiComboBox dayCombo;
	UiComboBox yearCombo;	
	MultiCalendar maxDate;
	boolean isCustomField;
	UiLocalization localization; 
}

