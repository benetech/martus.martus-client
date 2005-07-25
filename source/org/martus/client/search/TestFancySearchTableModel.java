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

package org.martus.client.search;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.client.test.MockMartusApp;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.util.TestCaseEnhanced;

public class TestFancySearchTableModel extends TestCaseEnhanced
{
	public TestFancySearchTableModel(String name)
	{
		super(name);
	}

	public void testGetCellType() throws Exception
	{
		MockMartusApp app = MockMartusApp.create();
		ClientBulletinStore store = app.getStore();
		store.createFieldSpecCacheFromDatabase();
		app.loadSampleData();
		MartusLocalization localization = new MartusLocalization(null, new String[0]);
		UiDialogLauncher nullLauncher = new UiDialogLauncher(null,localization);
		FancySearchHelper helper = new FancySearchHelper(store, localization, nullLauncher);
		GridFieldSpec gridSpec = helper.getGridSpec(store);
		FancySearchTableModel model = new FancySearchTableModel(gridSpec);
		model.addEmptyRow();
		model.setValueAt("eventdate.begin", 0, FancySearchTableModel.fieldColumn);
		assertEquals(FieldSpec.TYPE_DATE, model.getCellType(0, FancySearchTableModel.valueColumn));
		
		model.setValueAt("author", 0, FancySearchTableModel.fieldColumn);
		assertEquals(FieldSpec.TYPE_NORMAL, model.getCellType(0, FancySearchTableModel.valueColumn));
		
	}
}
