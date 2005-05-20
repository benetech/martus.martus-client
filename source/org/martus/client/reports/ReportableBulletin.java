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

import org.martus.common.bulletin.Bulletin;


/*
 * This class wraps a Bulletin object to allow the report runner
 * to safely pull any of its data. Exposing Bulletin itself to a 
 * user-created Velocity report would certainly allow users to obtain
 * non-reportable data (such as attachments), and might even allow 
 * them to somehow modify the bulletin.
 * 
 * This provides a safe, read-only, limited set of getters
 * 
 */
public class ReportableBulletin
{
	public ReportableBulletin(Bulletin bulletinToWrap)
	{
		realBulletin = bulletinToWrap;
	}
	
	public String getLocalId()
	{
		return realBulletin.getLocalId();
	}
	
	public String get(String tag)
	{
		return realBulletin.get(tag);
	}
	
	Bulletin realBulletin;
}
