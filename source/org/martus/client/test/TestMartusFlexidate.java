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
import java.util.GregorianCalendar;

import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.util.TestCaseEnhanced;

public class TestMartusFlexidate extends TestCaseEnhanced
{
	
	public TestMartusFlexidate(String name)
	{
		super(name);
	}
	
	public void testFlexiDate()
	{
		MartusFlexidate mf = new MartusFlexidate("20030105+2");		
		assertEquals("20030105+2", mf.getMartusFlexidateString());
		
		assertEquals("2003-01-05", FieldSpec.calendarToYYYYMMDD(mf.getBeginDate()));
		assertEquals("2003-01-07", FieldSpec.calendarToYYYYMMDD(mf.getEndDate()));																
	}
		
	public void testFlexiDateOverMonths()
	{
		MartusFlexidate mf = new MartusFlexidate("20030105+120");		
		assertEquals("20030105+120", mf.getMartusFlexidateString());

		assertEquals("2003-01-05", FieldSpec.calendarToYYYYMMDD(mf.getBeginDate()));
		assertEquals("2003-05-05", FieldSpec.calendarToYYYYMMDD(mf.getEndDate()));
	
	}
	
	public void testFlexiDateOverYear()
	{
		MartusFlexidate mf = new MartusFlexidate("20020105+366");		
		assertEquals("20020105+366", mf.getMartusFlexidateString());

		assertEquals("2002-01-05", FieldSpec.calendarToYYYYMMDD(mf.getBeginDate()));
		assertEquals("2003-01-06", FieldSpec.calendarToYYYYMMDD(mf.getEndDate()));		
	}
	
	
	public void testExactDate()
	{
		MartusFlexidate mf = new MartusFlexidate("20030105");
		
		assertEquals("20030105+0", mf.getMartusFlexidateString());
		
		assertEquals("2003-01-05", FieldSpec.calendarToYYYYMMDD(mf.getBeginDate()));
		assertEquals("2003-01-05", FieldSpec.calendarToYYYYMMDD(mf.getEndDate()));			
	}	
	
	public void testDateRange()
	{
		Calendar beginDate = getDate(2000,Calendar.JANUARY,10);
		Calendar endDate = getDate(2000,Calendar.JANUARY, 15);
						
		MartusFlexidate mf = new MartusFlexidate(beginDate, endDate);
		
		assertEquals("20000110+5", mf.getMartusFlexidateString());	
		
		assertEquals("2000-01-10", FieldSpec.calendarToYYYYMMDD(mf.getBeginDate()));
		assertEquals("2000-01-15", FieldSpec.calendarToYYYYMMDD(mf.getEndDate()));			
	}	
	
	public void testSameDateRange()
	{
		Calendar beginDate = getDate(2000,Calendar.JANUARY,10);
		Calendar endDate = getDate(2000,Calendar.JANUARY, 10);
		
		MartusFlexidate mf = new MartusFlexidate(beginDate, endDate);

		assertEquals("20000110+0", mf.getMartusFlexidateString());	

		assertEquals("2000-01-10", FieldSpec.calendarToYYYYMMDD(mf.getBeginDate()));
		assertEquals("2000-01-10", FieldSpec.calendarToYYYYMMDD(mf.getEndDate()));
		
		mf = new MartusFlexidate("20030105+0");		
		assertEquals("20030105+0", mf.getMartusFlexidateString());

		assertEquals("2003-01-05", FieldSpec.calendarToYYYYMMDD(mf.getBeginDate()));
		assertEquals("2003-01-05", FieldSpec.calendarToYYYYMMDD(mf.getEndDate()));			
	}
	
	public void testDateRangeSwap()
	{
		Calendar beginDate = getDate(2000, Calendar.JANUARY, 10);
		Calendar endDate = new GregorianCalendar();
		endDate.setTime(new Date(beginDate.getTime().getTime() - (360L*24*60*60*1000)));
					
		MartusFlexidate mf = new MartusFlexidate(beginDate, endDate);
	
		assertEquals("Initial date incorrect", "19990115+360", mf.getMartusFlexidateString());	
	
		assertEquals("1999-01-15", FieldSpec.calendarToYYYYMMDD(mf.getBeginDate()));
		assertEquals("2000-01-10", FieldSpec.calendarToYYYYMMDD(mf.getEndDate()));
		
		long ONE_HOUR_OF_MILLIS = 60*60*1000;
		endDate.setTime(new Date(endDate.getTime().getTime() + ONE_HOUR_OF_MILLIS));	
		MartusFlexidate mf2 = new MartusFlexidate(beginDate, endDate);
		assertEquals("After setting date incorrect", "19990115+360", mf2.getMartusFlexidateString());	
	}
	
	public void testCreateFromMartusString()
	{
		MartusFlexidate mfd = MartusFlexidate.createFromMartusDateString("2000-01-10");
		assertEquals("2000-01-10", FieldSpec.calendarToYYYYMMDD(mfd.getBeginDate()));	
		
		mfd = MartusFlexidate.createFromMartusDateString("2000-01-10,20000101+0");
		assertEquals("single begin", "2000-01-01", FieldSpec.calendarToYYYYMMDD(mfd.getBeginDate()));
		assertEquals("single end", "2000-01-01", FieldSpec.calendarToYYYYMMDD(mfd.getEndDate()));
		
		mfd = MartusFlexidate.createFromMartusDateString("2000-01-10,20001203+5");
		assertEquals("range begin","2000-12-03", FieldSpec.calendarToYYYYMMDD(mfd.getBeginDate()));
		assertEquals("range end","2000-12-08", FieldSpec.calendarToYYYYMMDD(mfd.getEndDate()));						
	}
	
	public void testCreateInvalidDateFromMartusString()
	{
		MartusFlexidate mfd = MartusFlexidate.createFromMartusDateString("185[01-10");
		assertEquals("1900-01-01", FieldSpec.calendarToYYYYMMDD(mfd.getBeginDate()));					
	}

	private Calendar getDate(int year, int month, int day)
	{			
		Calendar cal = new GregorianCalendar();
		cal.set(year,month,day, 12, 0, 0);		
						
		return cal;
	} 
}
