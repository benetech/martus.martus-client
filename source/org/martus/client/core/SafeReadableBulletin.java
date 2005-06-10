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

package org.martus.client.core;

import org.martus.common.bulletin.Bulletin;
import org.martus.common.field.MartusField;


/*
 * This class wraps a Bulletin object to allow the report runner
 * or a search/filter query to safely pull any of its data. 
 * Exposing Bulletin itself to a user-created Velocity report 
 * or search query would certainly allow users to obtain
 * non-helpful data (such as attachments), and might even allow 
 * them to somehow modify the bulletin.
 * 
 * This provides a safe, read-only, limited set of getters
 * 
 */
public class SafeReadableBulletin
{
	public SafeReadableBulletin(Bulletin bulletinToWrap)
	{
		realBulletin = bulletinToWrap;
	}
	
	public MartusField field(String tag)
	{
		return realBulletin.getField(tag);
	}
	
	public String getLocalId()
	{
		return realBulletin.getLocalId();
	}
	
	public boolean contains(String lookFor)
	{
		return realBulletin.contains(lookFor);
	}
	
	public int getFieldType(String tag)
	{
		return realBulletin.getFieldType(tag);
	}
	
	public MartusField getPossiblyNestedField(String nestedFieldTag)
	{
		String[] tags = parseNestedTags(nestedFieldTag);
		MartusField field = field(tags[0]);
		for(int i=1; i < tags.length; ++i)
			field = field.getSubField(tags[i]);
			
		return field;
	}
	
	public static String[] parseNestedTags(String tagsToParse)
	{
		return tagsToParse.split("\\.");
	}

	Bulletin realBulletin;
}
