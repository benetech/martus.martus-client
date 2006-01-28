/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2005, Beneficent
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
package org.martus.client.test;

import java.util.Calendar;
import java.util.Date;

import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.util.MartusCalendar;
import org.martus.util.TestCaseEnhanced;

public class TestMartusFlexidate extends TestCaseEnhanced
{
	
	public TestMartusFlexidate(String name)
	{
		super(name);
	}
	
	public void testFlexiDate()
	{
		MartusFlexidate mf = MartusFlexidate.createFromInternalMartusFlexidateString("20030105+2");		
		assertEquals("20030105+2", mf.getMartusFlexidateString());
		
		assertEquals("2003-01-05", FieldSpec.calendarToYYYYMMDD(mf.getBeginDate()));
		assertEquals("2003-01-07", FieldSpec.calendarToYYYYMMDD(mf.getEndDate()));																
	}
		
	public void testFlexiDateOverMonths()
	{
		MartusFlexidate mf = MartusFlexidate.createFromInternalMartusFlexidateString("20030105+120");		
		assertEquals("20030105+120", mf.getMartusFlexidateString());

		assertEquals("2003-01-05", FieldSpec.calendarToYYYYMMDD(mf.getBeginDate()));
		assertEquals("2003-05-05", FieldSpec.calendarToYYYYMMDD(mf.getEndDate()));
	
	}
	
	public void testFlexiDateOverYear()
	{
		MartusFlexidate mf = MartusFlexidate.createFromInternalMartusFlexidateString("20020105+366");		
		assertEquals("20020105+366", mf.getMartusFlexidateString());

		assertEquals("2002-01-05", FieldSpec.calendarToYYYYMMDD(mf.getBeginDate()));
		assertEquals("2003-01-06", FieldSpec.calendarToYYYYMMDD(mf.getEndDate()));		
	}
	
	
	public void testExactDate()
	{
		MartusFlexidate mf = MartusFlexidate.createFromInternalMartusFlexidateString("20030105");
		
		assertEquals("20030105+0", mf.getMartusFlexidateString());
		
		assertEquals("2003-01-05", FieldSpec.calendarToYYYYMMDD(mf.getBeginDate()));
		assertEquals("2003-01-05", FieldSpec.calendarToYYYYMMDD(mf.getEndDate()));			
	}	
	
	public void testDateRange()
	{
		MartusCalendar beginDate = getDate(2000,Calendar.JANUARY,10);
		MartusCalendar endDate = getDate(2000,Calendar.JANUARY, 15);
						
		MartusFlexidate mf = new MartusFlexidate(beginDate, endDate);
		
		assertEquals("20000110+5", mf.getMartusFlexidateString());	
		
		assertEquals("2000-01-10", FieldSpec.calendarToYYYYMMDD(mf.getBeginDate()));
		assertEquals("2000-01-15", FieldSpec.calendarToYYYYMMDD(mf.getEndDate()));			
	}	
	
	public void testSameDateRange()
	{
		MartusCalendar beginDate = getDate(2000,Calendar.JANUARY,10);
		MartusCalendar endDate = getDate(2000,Calendar.JANUARY, 10);
		
		MartusFlexidate mf = new MartusFlexidate(beginDate, endDate);

		assertEquals("20000110+0", mf.getMartusFlexidateString());	

		assertEquals("2000-01-10", FieldSpec.calendarToYYYYMMDD(mf.getBeginDate()));
		assertEquals("2000-01-10", FieldSpec.calendarToYYYYMMDD(mf.getEndDate()));
		
		mf = MartusFlexidate.createFromInternalMartusFlexidateString("20030105+0");		
		assertEquals("20030105+0", mf.getMartusFlexidateString());

		assertEquals("2003-01-05", FieldSpec.calendarToYYYYMMDD(mf.getBeginDate()));
		assertEquals("2003-01-05", FieldSpec.calendarToYYYYMMDD(mf.getEndDate()));			
	}
	
	public void testDateRangeSwap()
	{
		MartusCalendar beginDate = getDate(2000, Calendar.JANUARY, 10);
		MartusCalendar endDate = new MartusCalendar();
		endDate.setTime(new Date(beginDate.getTime().getTime() - (360L*24*60*60*1000)));
					
		MartusFlexidate mf = new MartusFlexidate(beginDate, endDate);
	
		assertEquals("Initial date incorrect", "19990115+360", mf.getMartusFlexidateString());	
	
		assertEquals("1999-01-15", FieldSpec.calendarToYYYYMMDD(mf.getBeginDate()));
		assertEquals("2000-01-10", FieldSpec.calendarToYYYYMMDD(mf.getEndDate()));
	}
	
	public void testCreateFromMartusString()
	{
		MartusFlexidate mfd = MartusFlexidate.createFromBulletinFlexidateFormat("2000-01-10");
		assertEquals("2000-01-10", FieldSpec.calendarToYYYYMMDD(mfd.getBeginDate()));	
		
		mfd = MartusFlexidate.createFromBulletinFlexidateFormat("2000-01-10,20000101+0");
		assertEquals("single begin", "2000-01-01", FieldSpec.calendarToYYYYMMDD(mfd.getBeginDate()));
		assertEquals("single end", "2000-01-01", FieldSpec.calendarToYYYYMMDD(mfd.getEndDate()));
		
		mfd = MartusFlexidate.createFromBulletinFlexidateFormat("2000-01-10,20001203+5");
		assertEquals("range begin","2000-12-03", FieldSpec.calendarToYYYYMMDD(mfd.getBeginDate()));
		assertEquals("range end","2000-12-08", FieldSpec.calendarToYYYYMMDD(mfd.getEndDate()));						
	}
	
	public void testCreateInvalidDateFromMartusString()
	{
		MartusFlexidate mfd = MartusFlexidate.createFromBulletinFlexidateFormat("185[01-10");
		assertEquals("1900-01-01", FieldSpec.calendarToYYYYMMDD(mfd.getBeginDate()));					
	}
	
	public void testCreateMartusDateStringFromDateRange()
	{
		assertNull(MartusFlexidate.createMartusDateStringFromDateRange("invalidDate"));
		String standardDateRange = "1988-02-01,1988-02-05";
		assertEquals("1988-02-01,19880201+4", MartusFlexidate.createMartusDateStringFromDateRange(standardDateRange));

		String reversedDateRange = "1988-02-05,1988-02-01";
		assertEquals("1988-02-01,19880201+4", MartusFlexidate.createMartusDateStringFromDateRange(reversedDateRange));

		String noDateRange = "1988-02-05,1988-02-05";
		assertEquals("1988-02-05,19880205+0", MartusFlexidate.createMartusDateStringFromDateRange(noDateRange));
	}

	private MartusCalendar getDate(int year, int month, int day)
	{			
		MartusCalendar cal = MartusCalendar.createMartusCalendarFromGregorian(year, month, day);
		return cal;
	} 
}
