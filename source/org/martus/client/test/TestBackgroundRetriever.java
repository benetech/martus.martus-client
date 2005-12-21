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

import org.martus.client.core.MartusApp;
import org.martus.client.core.RetrieveCommand;
import org.martus.common.packet.UniversalId;
import org.martus.util.TestCaseEnhanced;

public class TestBackgroundRetriever extends TestCaseEnhanced
{
	public TestBackgroundRetriever(String name)
	{
		super(name);
	}

	public void testAppCurrentRetrieveCommand() throws Exception
	{
		MartusApp app = MockMartusApp.create();
		RetrieveCommand shouldBeEmpty = app.getCurrentRetrieveCommand();
		assertEquals("not empty?", 0, shouldBeEmpty.getRemainingToRetrieveCount());
		
		RetrieveCommand rc = createSampleRetrieveCommand();
		app.setCurrentRetrieveCommand(rc);
		RetrieveCommand got = app.getCurrentRetrieveCommand();
		assertEquals("didn't get it back?", rc.getRemainingToRetrieveCount(), got.getRemainingToRetrieveCount());
	}

	private RetrieveCommand createSampleRetrieveCommand()
	{
		String sampleFolderName = "Destination";
		Vector sampleUidList = new Vector();
		sampleUidList.add(UniversalId.createDummyUniversalId());
		sampleUidList.add(UniversalId.createDummyUniversalId());
		sampleUidList.add(UniversalId.createDummyUniversalId());
		RetrieveCommand rc = new RetrieveCommand(sampleFolderName, sampleUidList);
		return rc;
	}

}
