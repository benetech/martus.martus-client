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

package org.martus.client.core;

import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;


public class BulletinCache
{
	public BulletinCache()
	{
		cache = new Bulletin[MAX_SIZE];
	}
	
	public Bulletin find(UniversalId uid)
	{
		int at = indexOf(uid);
		if(at >= 0)
			return cache[at];
			
		return null;
	}
	
	public void add(Bulletin b)
	{
		if(addNextAt >= MAX_SIZE)
			addNextAt = 0;
		cache[addNextAt++] = b;
	}
	
	public void remove(UniversalId uid)
	{
		int at = indexOf(uid);
		if(at >= 0)
			cache[at] = null;
	}
	
	private int indexOf(UniversalId uid)
	{
		for(int i=0; i < MAX_SIZE; ++i)
		{
			if(cache[i] != null && cache[i].getUniversalId().equals(uid))
				return i;
		}
		return -1;
	}

	public static final int MAX_SIZE = 1000;
	private int addNextAt;
	private Bulletin[] cache;
}
