/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2006, Beneficent
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

import java.io.IOException;
import java.io.StringWriter;
import java.util.Vector;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.core.SortableBulletinList;
import org.martus.client.test.MockMartusApp;
import org.martus.common.LegacyCustomFields;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.bulletin.Bulletin.DamagedBulletinException;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.crypto.MartusCrypto.NoKeyPairException;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeDateRange;
import org.martus.common.fieldspec.FieldTypeLanguage;
import org.martus.common.fieldspec.FieldTypeMultiline;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.UniversalId;
import org.martus.util.TestCaseEnhanced;


public class TestReportRunner extends TestCaseEnhanced
{
	public TestReportRunner(String name)
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		rr = new ReportRunner(MockMartusSecurity.createClient(), new MiniLocalization());
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
		app.getLocalization().setCurrentLanguageCode(MiniLocalization.ENGLISH);
		app.loadSampleData();
		BulletinStore store = app.getStore();
		ReportFormat rf = new ReportFormat();
		rf.setDetailSection("$i. $bulletin.localId\n");
		StringWriter result = new StringWriter();
		Vector keys = store.scanForLeafKeys();
		SortableBulletinList list = new SortableBulletinList(app.getLocalization(), new MiniFieldSpec[0]);
		for(int i = 0; i < keys.size(); ++i)
		{
			DatabaseKey key = (DatabaseKey)keys.get(i);
			list.add(BulletinLoader.loadFromDatabase(store.getDatabase(), key, app.getSecurity()));
		}
		
		RunReportOptions options = new RunReportOptions();
		rr.runReport(rf, store.getDatabase(), list, result, options);
		StringBuffer expected = new StringBuffer();
		UniversalId[] uids = list.getSortedUniversalIds();
		for(int i=0; i < uids.length; ++i)
		{
			expected.append(Integer.toString(i+1));
			expected.append(". ");
			expected.append(uids[i].getLocalId());
			expected.append("\n");
		}
		assertEquals(new String(expected), result.toString());
	}
	
	public void testCustomField() throws Exception
	{
		FieldSpec[] specs = new FieldSpec[] 
		{
			FieldSpec.createStandardField("date", new FieldTypeDate()),
			FieldSpec.createStandardField("text", new FieldTypeNormal()),
			FieldSpec.createStandardField("multi", new FieldTypeMultiline()),
			FieldSpec.createStandardField("range", new FieldTypeDateRange()),
			FieldSpec.createStandardField("bool", new FieldTypeBoolean()),
			FieldSpec.createStandardField("language", new FieldTypeLanguage()),
			LegacyCustomFields.createFromLegacy("custom,Custom <label>"),
		};
		
		MockMartusApp app = MockMartusApp.create();
		app.getLocalization().setCurrentLanguageCode(MiniLocalization.ENGLISH);
		Bulletin b = new Bulletin(app.getSecurity(), specs, new FieldSpec[0]);
		String sampleCustomData = "Robert Plant";
		b.set("custom", sampleCustomData);
		b.setAllPrivate(false);
		app.saveBulletin(b, app.getFolderDraftOutbox());
		
		SortableBulletinList list = new SortableBulletinList(app.getLocalization(), new MiniFieldSpec[0]);
		list.add(b);
		ReportFormat rf = new ReportFormat();
		rf.setDetailSection("$bulletin.field('custom')");
		StringWriter result = new StringWriter();
		RunReportOptions options = new RunReportOptions();
		rr.runReport(rf, app.getStore().getDatabase(), list, result, options);
		
		assertEquals(sampleCustomData, result.toString());
	}
	
	public void testStartSection() throws Exception
	{
		ReportFormat rf = new ReportFormat();
		String startSection = "start";
		rf.setStartSection(startSection);
		String result = runReportOnSampleData(rf);
		assertEquals("didn't output start section just once?", startSection, result);
	}
	
	public void testBreakSection() throws Exception
	{
		MockMartusApp app = MockMartusApp.create();
		String sampleDate = "2004-06-19";
		createAndSaveSampleBulletin(app, "a", "1", sampleDate);
		createAndSaveSampleBulletin(app, "a", "2", sampleDate);
		createAndSaveSampleBulletin(app, "b", "1", sampleDate);
		ReportFormat rf = new ReportFormat();
		String breakSection = "$BreakLevel had $BreakCount\n" +
				"#foreach($x in [0..$BreakLevel])\n" +
				"$BreakFields.get($x).getLocalizedLabel($localization): " +
				"$BreakFields.get($x).html($localization) " +
				"#end\n\n";
		rf.setBreakSection(breakSection);
		
		RunReportOptions options = new RunReportOptions();
		options.includePrivate = true;
		options.printBreaks = true;
		
		MiniLocalization localization = new MiniLocalization();
		String authorLabel = localization.getFieldLabel(Bulletin.TAGAUTHOR);
		String summaryLabel = localization.getFieldLabel(Bulletin.TAGSUMMARY);
		
		
		String sortByAuthorSummary = runReportOnAppData(rf, app, options);
		assertEquals("1 had 1\n" + authorLabel + ": a " + summaryLabel + ": 1 \n" +
				"1 had 1\n" + authorLabel + ": a " + summaryLabel + ": 2 \n" +
				"0 had 2\n" + authorLabel + ": a \n" +
				"1 had 1\n" + authorLabel + ": b " + summaryLabel + ": 1 \n" +
				"0 had 1\n" + authorLabel + ": b \n", 
				sortByAuthorSummary);
		
		MiniFieldSpec[] entryDateSorting = {
			new MiniFieldSpec(StandardFieldSpecs.findStandardFieldSpec(Bulletin.TAGENTRYDATE)),
		};
		
		String entryDateLabel = localization.getFieldLabel(Bulletin.TAGENTRYDATE);
		String formattedDate = localization.convertStoredDateToDisplay(sampleDate);
		String sortedByEntryDate = runReportOnAppData(rf, app, options, entryDateSorting);
		assertEquals("0 had 3\n" + entryDateLabel + ": " + formattedDate + " \n", sortedByEntryDate);
		
		options.printBreaks = false;
		assertEquals("Still had output?", "", runReportOnAppData(rf, app, options));
	}

	private void createAndSaveSampleBulletin(MockMartusApp app, String author, String summary, String entryDate) throws Exception
	{
		BulletinFolder outbox = app.getFolderDraftOutbox();
		Bulletin b = app.createBulletin();
		b.set(Bulletin.TAGAUTHOR, author);
		b.set(Bulletin.TAGSUMMARY, summary);
		b.set(Bulletin.TAGENTRYDATE, entryDate);
		app.saveBulletin(b, outbox);
	}
	public void testEndSection() throws Exception
	{
		ReportFormat rf = new ReportFormat();
		String endSection = "end";
		rf.setEndSection(endSection);
		String result = runReportOnSampleData(rf);
		assertEquals("didn't output end section just once?", endSection, result);
	}
	
	private String runReportOnSampleData(ReportFormat rf) throws Exception
	{
		MockMartusApp app = MockMartusApp.create();
		app.loadSampleData();
		return runReportOnAppData(rf, app);
	}

	private String runReportOnAppData(ReportFormat rf, MockMartusApp app) throws Exception
	{
		RunReportOptions options = new RunReportOptions();
		return runReportOnAppData(rf, app, options);
	}

	private String runReportOnAppData(ReportFormat rf, MockMartusApp app, RunReportOptions options) throws IOException, DamagedBulletinException, NoKeyPairException, Exception
	{
		MiniFieldSpec sortSpecs[] = {
				new MiniFieldSpec(StandardFieldSpecs.findStandardFieldSpec(Bulletin.TAGAUTHOR)), 
				new MiniFieldSpec(StandardFieldSpecs.findStandardFieldSpec(Bulletin.TAGSUMMARY)),
			};

		return runReportOnAppData(rf, app, options, sortSpecs);
	}

	private String runReportOnAppData(ReportFormat rf, MockMartusApp app, RunReportOptions options, MiniFieldSpec[] sortSpecs) throws IOException, DamagedBulletinException, NoKeyPairException, Exception
	{
		BulletinStore store = app.getStore();
		MartusCrypto security = app.getSecurity();
		ReadableDatabase db = store.getDatabase();
		Vector unsortedKeys = store.scanForLeafKeys();
		MiniLocalization localization = new MiniLocalization();
		localization.setCurrentLanguageCode(MiniLocalization.ENGLISH);
		SortableBulletinList list = new SortableBulletinList(localization, sortSpecs);
		for(int i = 0; i < unsortedKeys.size(); ++i)
		{
			DatabaseKey key = (DatabaseKey)unsortedKeys.get(i);
			Bulletin b = BulletinLoader.loadFromDatabase(db, key, security);
			list.add(b);
		}
		StringWriter result = new StringWriter();
		rr.runReport(rf, store.getDatabase(), list, result, options);
		return result.toString();
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
