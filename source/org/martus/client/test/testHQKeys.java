/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2004, Beneficent
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

import java.util.Vector;

import org.martus.client.core.HQKeys;
import org.martus.common.MartusXml;
import org.martus.util.TestCaseEnhanced;


public class testHQKeys extends TestCaseEnhanced
{
	public testHQKeys(String name)
	{
		super(name);
	}
	
	public void testEmpty()
	{
		HQKeys hqKeys = new HQKeys(new Vector());
		String xmlExpected = MartusXml.getTagStart(HQKeys.HQ_KEYS_TAG) +
		 MartusXml.getTagEnd(HQKeys.HQ_KEYS_TAG);
		assertEquals(xmlExpected, hqKeys.toString());
	}
	
	public void testXmlRepresentation()
	{
		Vector keys = new Vector();
		String key1 = "key 1";
		String key2 = "key 2";
		keys.add(key1);
		keys.add(key2);
		HQKeys hqKeys = new HQKeys(keys);
		String xmlExpected = MartusXml.getTagStart(HQKeys.HQ_KEYS_TAG) +
		 MartusXml.getTagStart(HQKeys.HQ_KEY_TAG) + 
		 MartusXml.getTagStart(HQKeys.HQ_PUBLIC_KEY_TAG) + 
		 key1 +
		 MartusXml.getTagEndWithoutNewline(HQKeys.HQ_PUBLIC_KEY_TAG) +
		 MartusXml.getTagEnd(HQKeys.HQ_KEY_TAG) +
		 MartusXml.getTagStart(HQKeys.HQ_KEY_TAG) + 
		 MartusXml.getTagStart(HQKeys.HQ_PUBLIC_KEY_TAG) + 
		 key2 +
		 MartusXml.getTagEndWithoutNewline(HQKeys.HQ_PUBLIC_KEY_TAG) +
		 MartusXml.getTagEnd(HQKeys.HQ_KEY_TAG) +
		 MartusXml.getTagEnd(HQKeys.HQ_KEYS_TAG);
		
		assertEquals(xmlExpected, hqKeys.toString());
	}
	
	public void testParseXml() throws Exception
	{
		Vector keys = new Vector();
		String key1 = "key 1";
		String key2 = "key 2";
		keys.add(key1);
		keys.add(key2);
		HQKeys hqKeys = new HQKeys(keys);
		
		Vector newKeys = HQKeys.parseXml(hqKeys.toString());
		assertContains(key1,newKeys);
		assertContains(key2,newKeys);
		HQKeys hqKeys2 = new HQKeys(newKeys);
		
		assertEquals(hqKeys.toString(), hqKeys2.toString());
		
	}
	
	
}
