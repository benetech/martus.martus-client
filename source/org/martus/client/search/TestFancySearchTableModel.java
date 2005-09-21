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
import org.martus.clientside.UiLocalization;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldTypeAnyField;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeDropdown;
import org.martus.common.fieldspec.FieldTypeGrid;
import org.martus.common.fieldspec.FieldTypeMultiline;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.FieldTypeSearchValue;
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
		UiLocalization localization = new MartusLocalization(null, new String[0]);
		UiDialogLauncher nullLauncher = new UiDialogLauncher(null, localization);
		FancySearchHelper helper = new FancySearchHelper(store, nullLauncher);
		GridFieldSpec gridSpec = helper.getGridSpec(store);
		FancySearchTableModel model = new FancySearchTableModel(gridSpec, localization);
		model.addEmptyRow();
		assertEquals(new FieldTypeSearchValue(), model.getColumnType(FancySearchTableModel.valueColumn));

		DropDownFieldSpec fieldsSpec = (DropDownFieldSpec)model.getFieldSpecForColumn(FancySearchHelper.COLUMN_FIELD);
		
		int beginDateAt = FancySearchHelper.findSearchTag(fieldsSpec, "eventdate.begin");
		SearchableFieldChoiceItem beginDateItem = (SearchableFieldChoiceItem)fieldsSpec.getChoice(beginDateAt);
		model.setValueAt(beginDateItem.getCode(), 0, FancySearchTableModel.fieldColumn);
		assertEquals(new FieldTypeDate(), model.getCellType(0, FancySearchTableModel.valueColumn));
		
		int authorAt = FancySearchHelper.findSearchTag(fieldsSpec, "author");
		SearchableFieldChoiceItem authorItem = (SearchableFieldChoiceItem)fieldsSpec.getChoice(authorAt);
		model.setValueAt(authorItem.getCode(), 0, FancySearchTableModel.fieldColumn);
		assertEquals(new FieldTypeNormal(), model.getCellType(0, FancySearchTableModel.valueColumn));
		
		int languageAt = FancySearchHelper.findSearchTag(fieldsSpec, "language");
		SearchableFieldChoiceItem languageItem = (SearchableFieldChoiceItem)fieldsSpec.getChoice(languageAt);
		model.setValueAt(languageItem.getCode(), 0, FancySearchTableModel.fieldColumn);
		assertEquals(new FieldTypeDropdown(), model.getCellType(0, FancySearchTableModel.valueColumn));
		
	}
	
	public void testGetCurrentOpColumnSpec() throws Exception
	{
		UiLocalization localization = new UiLocalization(null, new String[0]);
		GridFieldSpec gridSpec = new GridFieldSpec();
		FancySearchTableModel model = new FancySearchTableModel(gridSpec, localization);
		DropDownFieldSpec normalSpec = model.getCurrentOpColumnSpec(new FieldTypeNormal());
		assertEquals("not all ops available for normal?", 7, normalSpec.getCount());
		DropDownFieldSpec multilineSpec = model.getCurrentOpColumnSpec(new FieldTypeMultiline());
		assertEquals("not all ops available for multiline?", 7, multilineSpec.getCount());
		DropDownFieldSpec booleanSpec = model.getCurrentOpColumnSpec(new FieldTypeBoolean());
		assertEquals("more than = and != available for boolean?", 2, booleanSpec.getCount());
		DropDownFieldSpec dropdownSpec = model.getCurrentOpColumnSpec(new FieldTypeDropdown());
		assertEquals("more than = and != available for dropdown?", 2, dropdownSpec.getCount());
		DropDownFieldSpec dateSpec = model.getCurrentOpColumnSpec(new FieldTypeDate());
		assertEquals("contains available for date?", 6, dateSpec.getCount());
		DropDownFieldSpec anyFieldSpec = model.getCurrentOpColumnSpec(new FieldTypeAnyField());
		assertEquals("not just contains available for 'any field'?", 1, anyFieldSpec.getCount());
		DropDownFieldSpec gridFieldSpec = model.getCurrentOpColumnSpec(new FieldTypeGrid());
		assertEquals("not just contains available for grid?", 1, gridFieldSpec.getCount());
	}
}
