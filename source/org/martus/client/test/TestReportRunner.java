/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005, Beneficent
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

import java.io.StringWriter;
import java.util.Vector;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.martus.client.reports.ReportFormat;
import org.martus.client.reports.ReportRunner;
import org.martus.common.BulletinStore;
import org.martus.common.LegacyCustomFields;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.DatabaseKey;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.util.TestCaseEnhanced;


public class TestReportRunner extends TestCaseEnhanced
{
	public TestReportRunner(String name)
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		rr = new ReportRunner(MockMartusSecurity.createClient());
		context = new VelocityContext();
	}	
	
	public void testNoVariables() throws Exception
	{
		String templateWithoutVariables = "no variables";
		assertEquals(templateWithoutVariables, performMerge(templateWithoutVariables));
	}
	
	public void testOneVariable() throws Exception
	{
		String name = "test";
		String value = "hello";
		String templateWithVariable = "$" + name;
		context.put(name, value);
		assertEquals(value, performMerge(templateWithVariable));
	}
	
	public void testComplex() throws Exception
	{
		String[] array = {"dog", "cat", "monkey", };
		context.put("array", array);
		String template = 
			"#* multi-line comment\n" +
			"*#\n" +
			"#foreach ($x in $array)\n" +
			"  $x\n" +
			"#end\n" +
			"$nosuchvariable";
		assertEquals("\n  dog\n  cat\n  monkey\n$nosuchvariable", performMerge(template));
	}
	
	public void testRunReport() throws Exception
	{
		MockMartusApp app = MockMartusApp.create();
		app.loadSampleData();
		BulletinStore store = app.getStore();
		ReportFormat rf = new ReportFormat("$i. $bulletin.M_LocalId\n");
		StringWriter result = new StringWriter();
		Vector keys = store.scanForLeafKeys();
		rr.runReport(rf, store.getDatabase(), keys, result);
		StringBuffer expected = new StringBuffer();
		for(int i=0; i < keys.size(); ++i)
		{
			DatabaseKey key = (DatabaseKey)keys.get(i);
			expected.append(Integer.toString(i+1));
			expected.append(". ");
			expected.append(key.getLocalId());
			expected.append("\n");
		}
		assertEquals(new String(expected), result.toString());
	}
	
	public void testCustomField() throws Exception
	{
		FieldSpec[] specs = new FieldSpec[] 
		{
			FieldSpec.createStandardField("date", FieldSpec.TYPE_DATE),
			FieldSpec.createStandardField("text", FieldSpec.TYPE_NORMAL),
			FieldSpec.createStandardField("multi", FieldSpec.TYPE_MULTILINE),
			FieldSpec.createStandardField("range", FieldSpec.TYPE_DATERANGE),
			FieldSpec.createStandardField("bool", FieldSpec.TYPE_BOOLEAN),
			FieldSpec.createStandardField("language", FieldSpec.TYPE_LANGUAGE),
			LegacyCustomFields.createFromLegacy("custom,Custom <label>"),
		};
		
		MockMartusApp app = MockMartusApp.create();
		Bulletin b = new Bulletin(app.getSecurity(), specs, new FieldSpec[0]);
		String sampleCustomData = "Robert Plant";
		b.set("custom", sampleCustomData);
		app.saveBulletin(b, app.getFolderDraftOutbox());
		
		Vector keys = new Vector();
		keys.add(b.getDatabaseKey());
		ReportFormat rf = new ReportFormat("$bulletin.custom");
		StringWriter result = new StringWriter();
		rr.runReport(rf, app.getStore().getDatabase(), keys, result);
		
		assertEquals(sampleCustomData, result.toString());
	}
	
	private String performMerge(String template) throws Exception
	{
		StringWriter result = new StringWriter();
		rr.performMerge(template, result, context);
		return result.toString();
	}

	ReportRunner rr;
	Context context;
}
