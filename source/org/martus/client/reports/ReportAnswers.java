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
package org.martus.client.reports;

public class ReportAnswers
{
	
	static class ReportType
	{
		protected ReportType()
		{
		}
		
		public boolean isTabular()
		{
			return false;
		}
		
		public boolean isPage()
		{
			return false;
		}
		
	}
	
	static class ReportTypeTabular extends ReportType
	{
		public boolean isTabular()
		{
			return true;
		}

		public String toString()
		{
			return TYPE_TABULAR;
		}
	}
	
	static class ReportTypePage extends ReportType
	{
		public boolean isPage()
		{
			return true;
		}

		public String toString()
		{
			return TYPE_PAGE;
		}
	}
	
	static class ReportTypeFactory
	{
		public static ReportType createFromTypeString(String typeString)
		{
			if(typeString.equals(TYPE_TABULAR))
				return new ReportTypeTabular();
			if(typeString.equals(TYPE_PAGE))
				return new ReportTypePage();
			
			throw new RuntimeException("Unknown report type: " + typeString);
		}
	}
	
	static final String TYPE_TABULAR = "Tabular";
	static final String TYPE_PAGE = "Page";
	
	public final static int EXPECTED_VERSION = 8;
	
	//private ReportType type;
}
