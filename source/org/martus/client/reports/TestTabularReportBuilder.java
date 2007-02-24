/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeDateRange;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.language.LanguageOptions;
import org.martus.util.xml.XmlUtilities;

public class TestTabularReportBuilder extends TestCaseEnhanced
{
	public TestTabularReportBuilder(String name)
	{
		super(name);
	}

	public void testCreateTabularReport()
	{
		MiniFieldSpec[] specs = new MiniFieldSpec[] {
			new MiniFieldSpec(FieldSpec.createCustomField("tag1", "Label1'#$\\", new FieldTypeNormal())),
			new MiniFieldSpec(FieldSpec.createCustomField("tag2", "Label2", new FieldTypeNormal())),
			new MiniFieldSpec(FieldSpec.createCustomField("tag3", "Label3", new FieldTypeNormal())),
		};
		TabularReportBuilder builder = new TabularReportBuilder(new MiniLocalization());
		ReportFormat rf = builder.createTabular(specs);
		
		
		String startSection = rf.getDocumentStartSection();
		assertStartsWith("<html>", startSection);
		
		String headerSection = rf.getHeaderSection();
		for(int i = 0; i < specs.length; ++i)
		{
			String label = ReportBuilder.bodyEscape(XmlUtilities.getXmlEncoded(specs[i].getLabel()));
			assertContains("Missing " + label + "?", label, headerSection);
		}
		
		String detailSection = rf.getDetailSection();
		for(int i = 0; i < specs.length; ++i)
		{
			String fieldCall = "$bulletin.field(\"" + specs[i].getTag() + 
			"\", \"" + ReportBuilder.quotedEscape(specs[i].getLabel()) + 
			"\", \"" + specs[i].getType().getTypeName() + 
			"\").html($localization)";
			assertContains("Missing " + specs[i].getTag() + "?", fieldCall, detailSection);
		}
		
		String endSection = rf.getDocumentEndSection();
		assertContains("</html>", endSection);

		LanguageOptions.setDirectionRightToLeft();
		ReportFormat rf2 = builder.createTabular(specs);
		assertNotEquals("Didn't swap column Headers?", rf.getHeaderSection(), rf2.getHeaderSection());
		LanguageOptions.setDirectionLeftToRight();
	}
	
	public void testFieldCall() throws Exception
	{
		String tag = "a$b#c'd\\e\"";
		String label = "a$b#c'd\\e\"";
		FieldSpec spec = FieldSpec.createCustomField(tag, label, new FieldTypeNormal());
		MiniFieldSpec miniSpec = new MiniFieldSpec(spec);
		TabularReportBuilder builder = new TabularReportBuilder(new MiniLocalization());
		String call = builder.getFieldCall(miniSpec);
		assertEquals("Didn't encode the call?", "$bulletin.field(\"a$b#c'd\\\\e\\042\", \"a$b#c'd\\\\e\\042\", \"STRING\")", call);
	}
	
	public void testSubFields() throws Exception
	{
		FieldSpec range = FieldSpec.createCustomField("range", "Date Range", new FieldTypeDateRange());
		FieldSpec begin = FieldSpec.createSubField(range, "begin", "Range Begin", new FieldTypeDate());
		MiniFieldSpec specs[] = {new MiniFieldSpec(begin)};
		TabularReportBuilder builder = new TabularReportBuilder(new MiniLocalization());
		ReportFormat rf = builder.createTabular(specs);
		String detail = rf.getDetailSection();
		String expected = "$bulletin.field(\"range\", \"Date Range\", \"DATERANGE\").getSubField(\"begin\", $localization).html($localization)";
		assertContains("Bad date range subfield?", expected, detail);
		
	}
}
