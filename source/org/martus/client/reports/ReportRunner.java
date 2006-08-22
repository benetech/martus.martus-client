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
import java.io.Writer;
import java.util.Arrays;
import java.util.Vector;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.martus.client.core.SafeReadableBulletin;
import org.martus.client.core.SortableBulletinList;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.bulletin.Bulletin.DamagedBulletinException;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.NoKeyPairException;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.UniversalId;


public class ReportRunner
{
	public ReportRunner(MartusCrypto security, MiniLocalization localizationToUse) throws Exception
	{
		signatureVerifier = security;
		localization = localizationToUse;
		
		engine = new VelocityEngine();
		engine.init();
	}
	
	public void runReport(ReportFormat rf, ReadableDatabase db, SortableBulletinList bulletins, ReportOutput destination, RunReportOptions options) throws Exception
	{
		UniversalId[] uids = bulletins.getSortedUniversalIds();
		ReportOutput breakDestination = destination;
		if(options.hideDetail)
			breakDestination = new NullReportOutput();
		SummaryBreakHandler breakHandler = new SummaryBreakHandler(rf, breakDestination, options, bulletins.getSortSpecs());

		context = new VelocityContext();
		context.put("localization", localization);
		class Specs extends Vector
		{
			public Specs(MiniFieldSpec[] specs)
			{
				super(Arrays.asList(specs));
			}
			
			public boolean contains(Object o)
			{
				boolean result = super.contains(o);
				return result;
			}
		}
		context.put("specsToInclude", new Specs(rf.getSpecsToInclude()));
		
		StringWriter pageBreak = new StringWriter();
		performMerge(rf.getFakePageBreakSection(), pageBreak);
		destination.setFakePageBreak(pageBreak.toString());

		StringWriter documentStart = new StringWriter();
		performMerge(rf.getDocumentStartSection(), documentStart);
		destination.setDocumentStart(documentStart.toString());

		for(int bulletin = 0; bulletin < uids.length; ++bulletin)
		{
			SafeReadableBulletin safeReadableBulletin = getCensoredBulletin(db, uids[bulletin], options);

			if(bulletin == 0 || rf.getBulletinPerPage())
			{
				ReportOutput headerDestination = destination;
				if(options.hideDetail)
					headerDestination = new ReportOutput();
				performMerge(rf.getHeaderSection(), headerDestination);
			}

			breakHandler.doBreak(safeReadableBulletin);
			doDetail(rf, destination, options, bulletin, safeReadableBulletin);
			breakHandler.incrementCounts();

			if(bulletin == uids.length - 1)
				breakHandler.doFinalBreak();
			
			if(bulletin == uids.length - 1 || rf.getBulletinPerPage())
				performMerge(rf.getFooterSection(), destination);
			
			if(rf.getBulletinPerPage())
			{
				destination.startNewPage();
			}
		}

		if(options.printBreaks)
		{
			context.put("totals", breakHandler.getSummaryTotals());
			performMerge(rf.getTotalSection(), destination);
		}
		
		StringWriter documentEnd = new StringWriter();
		performMerge(rf.getDocumentEndSection(), documentEnd);
		destination.setDocumentEnd(documentEnd.toString());
		context = null;
	}

	private void doDetail(ReportFormat rf, ReportOutput destination, RunReportOptions options, int bulletin, SafeReadableBulletin safeReadableBulletin) throws Exception
	{
		context.put("i", new Integer(bulletin+1));
		context.put("bulletin", safeReadableBulletin);
		
		ReportOutput detailDestination = destination;
		if(options.hideDetail)
			detailDestination = new ReportOutput();
		
		performMerge(rf.getDetailSection(), detailDestination);
		context.remove("bulletin");
	}

	private SafeReadableBulletin getCensoredBulletin(ReadableDatabase db, UniversalId uid, RunReportOptions options) throws IOException, DamagedBulletinException, NoKeyPairException
	{
		DatabaseKey key = DatabaseKey.createLegacyKey(uid);
		Bulletin b = BulletinLoader.loadFromDatabase(db, key, signatureVerifier);
		SafeReadableBulletin safeReadableBulletin = new SafeReadableBulletin(b, localization);
		if(!options.includePrivate)
			safeReadableBulletin.removePrivateData();
		return safeReadableBulletin;
	}

	static class NullReportOutput extends ReportOutput
	{
		public void close() throws IOException
		{
		}

		public void flush() throws IOException
		{
		}

		public void write(char[] cbuf, int off, int len) throws IOException
		{
		}
	
	}
	
	class SummaryBreakHandler
	{
		public SummaryBreakHandler(ReportFormat rf, ReportOutput destination, RunReportOptions options, MiniFieldSpec[] breakSpecsToUse)
		{
			output = destination;
			
			breakSection = rf.getBreakSection();
			breakSpecs = breakSpecsToUse;
			if(!options.printBreaks)
				breakSpecs = new MiniFieldSpec[0];
			
			previousBreakValues = new String[breakSpecs.length];
			Arrays.fill(previousBreakValues, "");
			breakCounts = new int[breakSpecs.length];
			Arrays.fill(breakCounts, 0);
			
			StringVector breakLabels = new StringVector();
			for(int i = 0; i < breakSpecsToUse.length; ++i)
			{
				MiniFieldSpec spec = breakSpecsToUse[i];
				breakLabels.add(StandardFieldSpecs.getLocalizedLabel(spec.getTag(), spec.getLabel(), localization));
			}
			summaryCounts = new SummaryCount(breakLabels);
		}
		
		public void doBreak(SafeReadableBulletin upcomingBulletin) throws Exception
		{
			if(upcomingBulletin != null)
			{
				StringVector values = new StringVector();
				for(int i = 0; i < breakSpecs.length; ++i)
				{
					values.add(getBreakData(upcomingBulletin, i));
				}
				summaryCounts.increment(values);
			}
			
			for(int breakLevel = breakSpecs.length - 1; breakLevel >= 0; --breakLevel)
			{
				String current = getBreakData(upcomingBulletin, breakLevel);
				if(current == null || !current.equals(previousBreakValues[breakLevel]))
				{
					if(breakCounts[0] > 0)
						performBreak(breakLevel);
					previousBreakValues[breakLevel] = current;
					breakCounts[breakLevel] = 0;
				}
			}
		}
		
		public void doFinalBreak() throws Exception
		{
			doBreak(null);
		}
		
		public SummaryCount getSummaryTotals()
		{
			return summaryCounts;
		}

		private String getBreakData(SafeReadableBulletin upcomingBulletin, int breakLevel)
		{
			if(upcomingBulletin == null)
				return null;
			
			String current = "";
			MartusField thisField = upcomingBulletin.getPossiblyNestedField(breakSpecs[breakLevel]);
			if(thisField != null)
				current = thisField.getData();
			return current;
		}
		
		private void performBreak(int breakLevel) throws Exception
		{
			BreakFields breakFields = new BreakFields();
			for(int i = 0; i < breakLevel + 1; ++i)
			{
				MiniFieldSpec miniSpec = breakSpecs[i];
				FieldSpec spec = miniSpec.getType().createEmptyFieldSpec();
				spec.setTag(miniSpec.getTag());
				spec.setLabel(miniSpec.getLabel());
				MartusField field = new MartusField(spec);
				field.setData(previousBreakValues[i]);
				breakFields.add(field);
			}
			context.put("BreakLevel", new Integer(breakLevel));
			context.put("BreakCount", new Integer(breakCounts[breakLevel]));
			context.put("BreakFields", breakFields);
			performMerge(breakSection, output);
		}
		
		public void incrementCounts()
		{
			for(int breakLevel = breakSpecs.length - 1; breakLevel >= 0; --breakLevel)
			{
				++breakCounts[breakLevel];
			}
		}
		
		ReportOutput output;
		String breakSection;
		MiniFieldSpec[] breakSpecs;
		int[] breakCounts;
		String[] previousBreakValues;
		SummaryCount summaryCounts;
	}
	
	public void performMerge(String template, Writer result) throws Exception
	{
		engine.evaluate(context, result, "Martus", template);
	}
	
	public class BreakFields extends Vector
	{
	}
	
	VelocityEngine engine;
	MiniLocalization localization;
	MartusCrypto signatureVerifier;
	VelocityContext context;
}
