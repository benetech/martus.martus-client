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

import java.io.Writer;
import java.util.Arrays;
import java.util.Vector;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.martus.client.core.SafeReadableBulletin;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;


public class ReportRunner
{
	public ReportRunner(MartusCrypto security, MiniLocalization localizationToUse) throws Exception
	{
		signatureVerifier = security;
		localization = localizationToUse;
		
		engine = new VelocityEngine();
		engine.init();
	}
	
	public void runReport(ReportFormat rf, ReadableDatabase db, Vector keysToInclude, String[] sortTags, Writer destination, boolean includePrivate) throws Exception
	{
		Context context = new VelocityContext();
		context.put("localization", localization);
		
		String[] previousBreakValues = new String[sortTags.length];
		Arrays.fill(previousBreakValues, "");
		int[] breakCounts = new int[sortTags.length];
		Arrays.fill(breakCounts, 0);
		
		performMerge(rf.getStartSection(), destination, context);
		for(int bulletin = 0; bulletin < keysToInclude.size(); ++bulletin)
		{
			DatabaseKey key = (DatabaseKey)keysToInclude.get(bulletin);
			Bulletin b = BulletinLoader.loadFromDatabase(db, key, signatureVerifier);
			SafeReadableBulletin safeReadableBulletin = new SafeReadableBulletin(b, localization);
			if(!includePrivate)
				safeReadableBulletin.removePrivateData();
			
			for(int breakLevel = sortTags.length - 1; breakLevel >= 0; --breakLevel)
			{
				String current = b.getField(sortTags[breakLevel]).getData();
				if(!current.equals(previousBreakValues[breakLevel]))
				{
					if(bulletin > 0)
						performBreak(rf, context, destination, previousBreakValues, breakLevel, breakCounts[breakLevel]);
					previousBreakValues[breakLevel] = current;
					breakCounts[breakLevel] = 0;
				}
			}
			
			context.put("i", new Integer(bulletin+1));
			context.put("bulletin", safeReadableBulletin);
			performMerge(rf.getDetailSection(), destination, context);
			
			for(int breakLevel = sortTags.length - 1; breakLevel >= 0; --breakLevel)
			{
				++breakCounts[breakLevel];
			}
			
			context.remove("bulletin");
		}
		for(int breakLevel = sortTags.length - 1; breakLevel >= 0; --breakLevel)
			performBreak(rf, context, destination, previousBreakValues, breakLevel, breakCounts[breakLevel]);
		
		performMerge(rf.getEndSection(), destination, context);
	}

	private void performBreak(ReportFormat rf, Context context, Writer destination, String[] previousBreakValues, int breakLevel, int breakCount) throws Exception
	{
		Vector breakValues = new Vector();
		for(int i = 0; i < breakLevel + 1; ++i)
			breakValues.add(previousBreakValues[i]);
		context.put("BreakCount", new Integer(breakCount));
		context.put("BreakValues", breakValues);
		performMerge(rf.getBreakSection(), destination, context);
	}
	
	public void performMerge(String template, Writer result, Context context) throws Exception
	{
		engine.evaluate(context, result, "Martus", template);
	}
	
	VelocityEngine engine;
	MiniLocalization localization;
	MartusCrypto signatureVerifier;
}
