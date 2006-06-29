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

import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.packet.UniversalId;
import org.martus.util.TestCaseEnhanced;

public class TestSortableBulletinList extends TestCaseEnhanced
{
	public TestSortableBulletinList(String name)
	{
		super(name);
	}

	public void testBasics() throws Exception
	{
		MockMartusSecurity security = MockMartusSecurity.createClient();
		
		String tags[] = {Bulletin.TAGAUTHOR, Bulletin.TAGTITLE};

		SortableBulletinList list = new SortableBulletinList(MiniLocalization.ENGLISH, tags);
		String[] authors = {"Sue", "Wendy", "Alan", "Wendy", };
		String[] titles = {"Wow", "Yowza", "Yippee", "Eureka!", };
		Bulletin[] bulletins = new Bulletin[authors.length];
		for(int i = 0; i < authors.length; ++i)
		{
			bulletins[i] = new Bulletin(security);
			bulletins[i].set(Bulletin.TAGAUTHOR, authors[i]);
			bulletins[i].set(Bulletin.TAGTITLE, titles[i]);
			list.add(bulletins[i]);
		}
		
		UniversalId[] uids = list.getSortedUniversalIds();
		assertEquals("Alan not first?", bulletins[2].getUniversalId(), uids[0]);
		assertEquals("Sue not second?", bulletins[0].getUniversalId(), uids[1]);
		assertEquals("Wendy/Eureka not third?", bulletins[3].getUniversalId(), uids[2]);
		assertEquals("Wendy Yowza not last?", bulletins[1].getUniversalId(), uids[3]);
	}
	
}
