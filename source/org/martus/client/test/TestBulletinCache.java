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

import org.martus.client.core.BulletinCache;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.packet.UniversalId;
import org.martus.util.TestCaseEnhanced;


public class TestBulletinCache extends TestCaseEnhanced
{
	public TestBulletinCache(String name)
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
	}
	
	public void testBasics() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		BulletinCache cache = new BulletinCache();
		Bulletin b1 = new Bulletin(security);
		Bulletin b2 = new Bulletin(security);
		
		assertNull("already in cache?", cache.find(b1.getUniversalId()));
		cache.add(b1);
		assertEquals("b1 not found?", b1, cache.find(b1.getUniversalId()));
		cache.add(b2);
		assertEquals("b1 not refound?", b1, cache.find(b1.getUniversalId()));
		assertEquals("b2 not found?", b2, cache.find(b2.getUniversalId()));
		
		cache.remove(b1.getUniversalId());
		assertNull("b1 still found?", cache.find(b1.getUniversalId()));
		assertEquals("b2 not refound?", b2, cache.find(b2.getUniversalId()));
		
	}
	
	public void testFullCache() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		BulletinCache cache = new BulletinCache();

		Bulletin first = new Bulletin(security);
		cache.add(first);
		for(int i = 0; i < BulletinCache.MAX_SIZE + 2; ++i)
		{
			cache.add(new Bulletin(security));
		}
		Bulletin last = new Bulletin(security);
		cache.add(last);
		
		assertNull("Can still find first?", cache.find(first.getUniversalId()));
		assertEquals("Can't find last?", last, cache.find(last.getUniversalId()));
	}
	
	public void testAddTwiceRemoveOnce() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		BulletinCache cache = new BulletinCache();

		Bulletin b = new Bulletin(security);
		UniversalId uid = b.getUniversalId();

		cache.add(b);
		cache.add(b);
		cache.remove(uid);
		assertNull("Didn't remove all copies?", cache.find(uid));
	}
}
