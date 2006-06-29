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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;

public class SortableBulletinList
{
	public SortableBulletinList(String[] tagsForSorting)
	{
		tags = tagsForSorting;
		set = new HashSet();
	}
	
	public void add(Bulletin b)
	{
		PartialBulletin pb = new PartialBulletin(b, tags);
		set.add(pb);
	}
	
	public int size()
	{
		return set.size();
	}
	
	public UniversalId[] getUniversalIds()
	{
		UniversalId[] uids = new UniversalId[size()];
		Iterator iter = set.iterator();
		int next = 0;
		while(iter.hasNext())
		{
			PartialBulletin pb = (PartialBulletin)iter.next();
			uids[next++] = pb.getUniversalId();
		}
		
		return uids;
	}
	
	public UniversalId[] getSortedUniversalIds()
	{
		PartialBulletin[] bulletins = (PartialBulletin[])set.toArray(new PartialBulletin[0]);
		Arrays.sort(bulletins, new PartialBulletinSorter(tags));
		UniversalId[] uids = new UniversalId[bulletins.length];
		for(int i = 0; i < bulletins.length; ++i)
			uids[i] = bulletins[i].getUniversalId();
		
		return uids;
	}
	
	static class PartialBulletinSorter implements Comparator
	{
		public PartialBulletinSorter(String[] tagsToSortBy)
		{
			tags = tagsToSortBy;
		}
		
		public int compare(Object o1, Object o2)
		{
			PartialBulletin pb1 = (PartialBulletin)o1;
			PartialBulletin pb2 = (PartialBulletin)o2;
			for(int i = 0; i < tags.length; ++i)
			{
				int result = pb1.getData(tags[i]).compareTo(pb2.getData(tags[i]));
				if(result != 0)
					return result;
			}
			return 0;
		}

		String[] tags;
	}
	
	String[] tags;
	HashSet set;
}
