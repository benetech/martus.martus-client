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

package org.martus.client.core;

import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.packet.UniversalId;


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
	public SafeReadableBulletin(Bulletin bulletinToWrap, MiniLocalization localizationToUse)
	{
		realBulletin = bulletinToWrap;
		localization = localizationToUse;
	}
	
	public MartusField field(String tag)
	{
		try
		{
			MartusField original = realBulletin.getField(tag);
			if(original == null)
				return null;
			
			MartusField result = new MartusField(original);
			
			if(omitPrivate)
			{
				if(realBulletin.isAllPrivate() || realBulletin.isFieldInPrivateSection(tag))
					result.setData("");
			}
			
			return result;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public UniversalId getUniversalId()
	{
		return realBulletin.getUniversalId();
	}
	
	public String getLocalId()
	{
		return realBulletin.getLocalId();
	}
	
	public boolean contains(String lookFor)
	{
		return realBulletin.contains(lookFor, localization);
	}
	
	public FieldType getFieldType(String tag)
	{
		return realBulletin.getFieldType(tag);
	}
	
	public MartusField getPossiblyNestedField(FieldSpec nestedFieldTag)
	{
		return getPossiblyNestedField(nestedFieldTag.getTag());
	}

	public MartusField getPossiblyNestedField(String tag)
	{
		String[] tags = parseNestedTags(tag);
		MartusField field = null;
		
		for(int i=0; i < tags.length; ++i)
		{
			if(field == null)
				field = field(tags[0]);
			else
				field = field.getSubField(tags[i], localization);
			if(field == null)
				return null;
		}
			
		return field;
	}
	
	public void removePrivateData()
	{
		omitPrivate = true;
	}
	
	public static String[] parseNestedTags(String tagsToParse)
	{
		return tagsToParse.split("\\.");
	}

	Bulletin realBulletin;
	MiniLocalization localization;
	boolean omitPrivate;
}
