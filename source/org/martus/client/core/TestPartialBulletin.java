/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006, Beneficent
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
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.util.TestCaseEnhanced;

public class TestPartialBulletin extends TestCaseEnhanced
{
	public TestPartialBulletin(String name)
	{
		super(name);
	}
	
	public void testBasics() throws Exception
	{
		MockMartusSecurity security = MockMartusSecurity.createClient();
		Bulletin b = new Bulletin(security);
		String[] tagsToStore = new String[] {
			Bulletin.TAGTITLE,
			Bulletin.TAGSTATUS,
		};
		for(int i = 0; i < tagsToStore.length; ++i)
			b.set(tagsToStore[i], tagsToStore[i]);
		PartialBulletin pb = new PartialBulletin(b, tagsToStore);
		
		assertEquals("Didn't copy uid?", b.getUniversalId(), pb.getUniversalId());
		for(int i = 0; i < tagsToStore.length; ++i)
		{
			String tag = tagsToStore[i];
			assertEquals("Didn't store " + tag + "?", b.get(tag), pb.getData(tag));
		}
	}
	
	public void testPseudoTags() throws Exception
	{
		String tags[] = {Bulletin.PSEUDOFIELD_LAST_SAVED_DATE, Bulletin.PSEUDOFIELD_LOCAL_ID};
		MockMartusSecurity security = MockMartusSecurity.createClient();
		Bulletin b = new Bulletin(security);
		for(int i = 0; i < tags.length; ++i)
			assertNotEquals("Pseudotag not working: " + tags[i] + "?", "", b.get(tags[i]));
		PartialBulletin pb = new PartialBulletin(b, tags);
		for(int i = 0; i < tags.length; ++i)
			assertEquals("Didn't copy pseudo tag " + tags[i] + "?", b.get(tags[i]), pb.getData(tags[i]));
	}

}
