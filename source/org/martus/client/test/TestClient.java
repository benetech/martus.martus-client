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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.martus.client.core.TestSafeReadableBulletin;
import org.martus.client.reports.TestReportFormat;
import org.martus.client.reports.TestReportRunner;
import org.martus.client.search.TestBulletinSearcher;
import org.martus.client.search.TestFancySearchHelper;
import org.martus.client.search.TestSearchParser;
import org.martus.client.search.TestSearchTreeNode;

public class TestClient
{
	public static void main(String[] args)
	{
		runTests();
	}

	public static void runTests ()
	{
		junit.textui.TestRunner.run (suite());
	}

	public static Test suite ( )
	{
		TestSuite suite= new TestSuite("All Client Martus Tests");
		
		suite.addTest(new TestSuite(TestBulletinCache.class));
		suite.addTest(new TestSuite(TestBulletinFolder.class));
		suite.addTest(new TestSuite(TestBulletinSearcher.class));
		suite.addTest(new TestSuite(TestBulletinTableModel.class));
		suite.addTest(new TestSuite(TestBulletinXmlExporter.class));
		suite.addTest(new TestSuite(TestClientBulletinStore.class));
		suite.addTest(new TestSuite(TestConfigInfo.class));
		suite.addTest(new TestSuite(TestCustomFieldSpecValidator.class));
		suite.addTest(new TestSuite(TestCustomFieldTemplate.class));
		suite.addTest(new TestSuite(TestFancySearchHelper.class));
		suite.addTest(new TestSuite(TestFolderList.class));
		suite.addTest(new TestSuite(TestGridTableModel.class));
		suite.addTest(new TestSuite(TestMartusApp_NoServer.class));
		suite.addTest(new TestSuite(TestMartusFlexidate.class));
		suite.addTest(new TestSuite(TestLocalization.class));
		suite.addTest(new TestSuite(TestMartusUserNameAndPassword.class));
		suite.addTest(new TestSuite(TestReportFormat.class));
		suite.addTest(new TestSuite(TestReportRunner.class));
		suite.addTest(new TestSuite(TestSafeReadableBulletin.class));
		suite.addTest(new TestSuite(TestSearchParser.class));
		suite.addTest(new TestSuite(TestSearchTreeNode.class));
		suite.addTest(new TestSuite(TestTokenReplacement.class));
		suite.addTest(new TestSuite(TestTransferableAttachments.class));
		suite.addTest(new TestSuite(TestTransferableBulletin.class));
		suite.addTest(new TestSuite(TestRandomAccessFileOverwrite.class));
		

	    return suite;
	}
}
