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

import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.util.TestCaseEnhanced;

public class TestTabularReportBuilder extends TestCaseEnhanced
{
	public TestTabularReportBuilder(String name)
	{
		super(name);
	}

	public void testCreateTabularReport()
	{
		FieldSpec[] specs = new FieldSpec[] {
			FieldSpec.createCustomField("tag1", "Label1", new FieldTypeNormal()),
			FieldSpec.createCustomField("tag2", "Label2", new FieldTypeNormal()),
			FieldSpec.createCustomField("tag3", "Label3", new FieldTypeNormal()),
		};
		TabularReportBuilder builder = new TabularReportBuilder(new MiniLocalization());
		ReportFormat rf = builder.createTabular(specs);
		
		String startSection = rf.getStartSection();
		assertStartsWith("<html>", startSection);
		for(int i = 0; i < specs.length; ++i)
		{
			String label = specs[i].getLabel();
			assertContains("Missing " + label + "?", label, startSection);
		}
		
		String detailSection = rf.getDetailSection();
		for(int i = 0; i < specs.length; ++i)
		{
			String tag = "$bulletin.html('" + specs[i].getTag() + "')";
			assertContains("Missing " + tag + "?", tag, detailSection);
		}
		
		String endSection = rf.getEndSection();
		assertContains("</html>", endSection);
	}
}
