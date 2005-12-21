/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005, Beneficent
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

import org.martus.client.core.RetrieveCommand;
import org.martus.common.packet.UniversalId;
import org.martus.util.TestCaseEnhanced;

public class TestRetrieveCommand extends TestCaseEnhanced
{
	public TestRetrieveCommand(String name)
	{
		super(name);
	}
	
	public void testEmpty()
	{
		RetrieveCommand empty = new RetrieveCommand();
		assertEquals("not zero to retrieve?", 0, empty.getRemainingToRetrieveCount());
		assertEquals("not zero already retrieved?", 0, empty.getRetrievedCount());
		try
		{
			empty.getNextToRetrieve();
			fail("should have thrown since nothing to retrieve");
		}
		catch(RuntimeException ignoreExpected)
		{
		}
	}
	
	public void testBasics()
	{
		String sampleFolderName = "Destination";
		Vector sampleUidList = new Vector();
		sampleUidList.add(UniversalId.createDummyUniversalId());
		sampleUidList.add(UniversalId.createDummyUniversalId());
		sampleUidList.add(UniversalId.createDummyUniversalId());
		RetrieveCommand rc = new RetrieveCommand(sampleFolderName, sampleUidList);
		assertEquals("wrong folder name?", sampleFolderName, rc.getFolderName());
		assertEquals("didn't start with something to retrieve?", sampleUidList.size(), rc.getRemainingToRetrieveCount());
		assertEquals("didn't start with nothing retrieved?", 0, rc.getRetrievedCount());
		
		assertEquals("wrong next to retrieve?", sampleUidList.get(0), rc.getNextToRetrieve());
		rc.markAsRetrieved((UniversalId)sampleUidList.get(0));
		assertEquals("didn't reduce retrieve count?", sampleUidList.size() - 1, rc.getRemainingToRetrieveCount());
		assertEquals("didn't increase retrieved count?", 1, rc.getRetrievedCount());
		
		try
		{
			rc.markAsRetrieved(UniversalId.createDummyUniversalId());
			fail("Should have thrown trying to remove anything other than the first item");
		}
		catch(RuntimeException ignoreExpected)
		{
		}
		
		rc.markAsRetrieved((UniversalId)sampleUidList.get(1));
		assertEquals("didn't reduce retrieve count again?", sampleUidList.size() - 2, rc.getRemainingToRetrieveCount());
		assertEquals("didn't increase retrieved count again?", 2, rc.getRetrievedCount());
	}

}
