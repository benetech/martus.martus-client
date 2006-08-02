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

import org.martus.client.search.SaneCollator;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;

public class SortableBulletinList
{
	public SortableBulletinList(MiniLocalization localizationToUse, String[] tagsForSorting, String[] extraFieldTagsToUse)
	{
		localization = localizationToUse;
		sortTags = tagsForSorting;
		tagsToStore = new String[sortTags.length + extraFieldTagsToUse.length];
		System.arraycopy(sortTags, 0, tagsToStore, 0, sortTags.length);
		System.arraycopy(extraFieldTagsToUse, 0, tagsToStore, sortTags.length, extraFieldTagsToUse.length);
		sorter = new PartialBulletinSorter(localization.getCurrentLanguageCode(), sortTags);
		partialBulletins = new HashSet();
		
	}
	
	public SortableBulletinList(MiniLocalization localizationToUse, String[] tagsForSorting)
	{
		this(localizationToUse, tagsForSorting, new String[0]);
	}
	
	public void add(Bulletin b)
	{
		SafeReadableBulletin readableBulletin = new SafeReadableBulletin(b, localization);
		PartialBulletin pb = new PartialBulletin(readableBulletin, tagsToStore);
		add(pb);
	}

	public void add(PartialBulletin pb)
	{
		partialBulletins.add(pb);
	}
	
	public void remove(PartialBulletin pb)
	{
		partialBulletins.remove(pb);
	}
	
	public int size()
	{
		return partialBulletins.size();
	}
	
	public String[] getSortTags()
	{
		return sortTags;
	}
	
	public PartialBulletin[] getUnsortedPartialBulletins()
	{
		return (PartialBulletin[])partialBulletins.toArray(new PartialBulletin[0]);
	}
	
	public UniversalId[] getUniversalIds()
	{
		UniversalId[] uids = new UniversalId[size()];
		Iterator iter = partialBulletins.iterator();
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
		PartialBulletin[] bulletins = getSortedPartialBulletins();
		UniversalId[] uids = new UniversalId[bulletins.length];
		for(int i = 0; i < bulletins.length; ++i)
			uids[i] = bulletins[i].getUniversalId();
		
		return uids;
	}

	public PartialBulletin[] getSortedPartialBulletins()
	{
		PartialBulletin[] bulletins = (PartialBulletin[])partialBulletins.toArray(new PartialBulletin[0]);
		Arrays.sort(bulletins, sorter);
		return bulletins;
	}
	
	static class PartialBulletinSorter implements Comparator
	{
		public PartialBulletinSorter(String languageCode, String[] tagsToSortBy)
		{
			tags = tagsToSortBy;
			collator = new SaneCollator(languageCode);
		}
		
		public int compare(Object o1, Object o2)
		{
			PartialBulletin pb1 = (PartialBulletin)o1;
			PartialBulletin pb2 = (PartialBulletin)o2;
			for(int i = 0; i < tags.length; ++i)
			{
				String s1 = pb1.getData(tags[i]);
				String s2 = pb2.getData(tags[i]);
				if(s1 == null && s2 == null)
					return 0;
				if(s1 == null)
					return -1;
				if(s2 == null)
					return 1;
				int result = collator.compare(s1, s2);
				if(result != 0)
					return result;
			}
			return 0;
		}

		String[] tags;
		SaneCollator collator;
	}
	
	MiniLocalization localization;
	Comparator sorter;
	String[] sortTags;
	String[] tagsToStore;
	HashSet partialBulletins;
}
