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
package org.martus.client.reports;

import java.util.Vector;

public class SummaryCount
{
	public SummaryCount(StringVector labelsToUse)
	{
		otherLabels = new StringVector(labelsToUse);
		label = "";
		value = "";
		children = new Vector();
	}
	
	public SummaryCount(StringVector labelsToUse, String valueToUse)
	{
		otherLabels = new StringVector(labelsToUse);
		label = otherLabels.remove(0);
		value = valueToUse;
		children = new Vector();
	}
	
	public String label()
	{
		return label;
	}
	
	public String value()
	{
		return value;
	}
	
	public int count()
	{
		return count;
	}
	
	public int getChildCount()
	{
		return children.size();
	}
	
	public SummaryCount getChild(int index)
	{
		return (SummaryCount)children.get(index);
	}
	
	public Vector children()
	{
		return children;
	}
	
	public void increment(StringVector values)
	{
		if(values.size() > 0)
		{
			String thisValue = values.remove(0);
			SummaryCount sc = findOrCreateChild(values, thisValue);
			sc.increment(values);
		}
		++count;
	}

	private SummaryCount findOrCreateChild(StringVector values, String thisValue)
	{
		int scIndex = find(thisValue);
		if(scIndex >= 0)
			return getChild(scIndex);
		
		SummaryCount sc = new SummaryCount(otherLabels, thisValue);
		children.add(sc);
		return sc;
	}
	
	int find(String valueToFind)
	{
		for(int i = 0; i < getChildCount(); ++i)
		{
			SummaryCount sc = getChild(i);
			if(sc.value().equals(valueToFind))
				return i;
		}
		
		return -1;
	}
	
	public String toString()
	{
		StringBuffer result = new StringBuffer();
		result.append("Label: " + label + ", Value: " + value + ", Count: " + count + "\n");
		for(int i = 0; i < getChildCount(); ++i)
			result.append(getChild(i).toString());
		return result.toString();
	}
	
	String label;
	String value;
	int count;
	StringVector otherLabels;
	Vector children;
}
