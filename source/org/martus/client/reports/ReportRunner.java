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

package org.martus.client.reports;

import java.io.Writer;
import java.util.Vector;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;


public class ReportRunner
{
	public ReportRunner(MartusCrypto security) throws Exception
	{
		signatureVerifier = security;
		engine = new VelocityEngine();
		engine.init();
	}
	
	public void runReport(ReportFormat rf, ReadableDatabase db, Vector keysToInclude, Writer destination) throws Exception
	{
		Context context = new VelocityContext();
		
		for(int i=0; i < keysToInclude.size(); ++i)
		{
			DatabaseKey key = (DatabaseKey)keysToInclude.get(i);
			Bulletin b = BulletinLoader.loadFromDatabase(db, key, signatureVerifier);
			context.put("bulletin", new ReportableBulletin(b));
			
			context.put("i", new Integer(i+1));
			performMerge(rf.getDetailSection(), destination, context);
		}
	}
	
	public void performMerge(String template, Writer result, Context context) throws Exception
	{
		engine.evaluate(context, result, "Martus", template);
	}
	
	VelocityEngine engine;
	MartusCrypto signatureVerifier;
}
