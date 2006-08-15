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

import org.martus.util.TestCaseEnhanced;

public class TestSummaryCount extends TestCaseEnhanced
{
	public TestSummaryCount(String name)
	{
		super(name);
	}

	public void testBasics()
	{
		String[] labels = {"first", "second", "third"};
		int FIRST = 0;
		int SECOND = 1;
		int THIRD = 2;
		
		String[][] values = {
				{"A", "1", "a"},
				{"A", "1", "b"},
				{"A", "1", "b"},
				{"A", "2", "a"},
				{"B", "3", "c"},
		};
		SummaryCount master = new SummaryCount(new StringVector(labels));
		for(int i = 0; i < values.length; ++i)
		{
			master.increment(new StringVector(values[i]));
		}
		
		assertEquals("top-level wrong count?", 5, master.getCount());
		assertEquals(2, master.getChildCount());
		
		SummaryCount a = master.getChild(0);
		assertEquals(labels[FIRST], a.getLabel());
		assertEquals("A", a.getValue());
		assertEquals(4, a.getCount());
		assertEquals(2, a.getChildCount());

		SummaryCount a1 = a.getChild(0);
		assertEquals(labels[SECOND], a1.getLabel());
		assertEquals("1", a1.getValue());
		assertEquals(3, a1.getCount());
		assertEquals(2, a1.getChildCount());

		SummaryCount a1a = a1.getChild(0);
		assertEquals(labels[THIRD], a1a.getLabel());
		assertEquals("a", a1a.getValue());
		assertEquals(1, a1a.getCount());
		assertEquals(0, a1a.getChildCount());

		SummaryCount a1b = a1.getChild(1);
		assertEquals(labels[THIRD], a1b.getLabel());
		assertEquals("b", a1b.getValue());
		assertEquals(2, a1b.getCount());
		assertEquals(0, a1b.getChildCount());

		SummaryCount a2 = a.getChild(1);
		assertEquals(labels[SECOND], a2.getLabel());
		assertEquals("2", a2.getValue());
		assertEquals(1, a2.getCount());
		assertEquals(1, a2.getChildCount());

		SummaryCount a2a = a2.getChild(0);
		assertEquals(labels[THIRD], a2a.getLabel());
		assertEquals("a", a2a.getValue());
		assertEquals(1, a2a.getCount());
		assertEquals(0, a2a.getChildCount());

		SummaryCount b = master.getChild(1);
		assertEquals(labels[FIRST], b.getLabel());
		assertEquals("B", b.getValue());
		assertEquals(1, b.getCount());
		assertEquals(1, b.getChildCount());
		
		SummaryCount b3 = b.getChild(0);
		assertEquals(labels[SECOND], b3.getLabel());
		assertEquals("3", b3.getValue());
		assertEquals(1, b3.getCount());
		assertEquals(1, b3.getChildCount());

		SummaryCount b3c = b3.getChild(0);
		assertEquals(labels[THIRD], b3c.getLabel());
		assertEquals("c", b3c.getValue());
		assertEquals(1, b3c.getCount());
		assertEquals(0, b3c.getChildCount());


	}
}
