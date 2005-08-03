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

import java.io.File;
import java.util.Vector;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.client.test.MockMartusApp;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.GridData;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.field.MartusDateRangeField;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.CustomDropDownFieldSpec;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.util.TestCaseEnhanced;

public class TestFancySearchHelper extends TestCaseEnhanced
{
	public TestFancySearchHelper(String name)
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		MockMartusApp app = MockMartusApp.create();
		store = app.getStore();
		store.createFieldSpecCacheFromDatabase();
		app.loadSampleData();
		tempDir = createTempDirectory();
		localization = new MartusLocalization(tempDir, new String[0]);
		UiDialogLauncher nullLauncher = new UiDialogLauncher(null,new MartusLocalization(null, new String[0]));
		helper = new FancySearchHelper(store, nullLauncher);
		
	}
	
	public void tearDown()
	{
		tempDir.delete();
	}
	
	public void testCreateFieldColumnSpec()
	{
		String languageCode = "en";
		localization.addEnglishTranslations(EnglishCommonStrings.strings);
		localization.setCurrentLanguageCode(languageCode);
		
		DropDownFieldSpec spec = helper.createFieldColumnSpec(store);
		assertNotEquals("inserted an empty first entry?", "", spec.getChoice(0).getCode());
		assertTrue("no author?", spec.findCode(BulletinConstants.TAGAUTHOR) >= 0);
		assertTrue("no private?", spec.findCode(BulletinConstants.TAGPRIVATEINFO) >= 0);
		assertTrue("no eventdate.begin?", spec.findCode(BulletinConstants.TAGEVENTDATE + "." + MartusDateRangeField.SUBFIELD_BEGIN) >= 0);
		assertTrue("no eventdate.end?", spec.findCode(BulletinConstants.TAGEVENTDATE + "." + MartusDateRangeField.SUBFIELD_END) >= 0);
		assertFalse("has raw eventdate?", spec.findCode(BulletinConstants.TAGEVENTDATE) >= 0);
	}
	
	public void testGetChoiceItemsForThisField() throws Exception
	{
		FieldSpec normal = StandardFieldSpecs.findStandardFieldSpec(BulletinConstants.TAGAUTHOR);
		Vector normalChoices = helper.getChoiceItemsForThisField(normal);
		assertEquals("more than one choice for a plain text field?", 1, normalChoices.size());
		
		FieldSpec dateRange = StandardFieldSpecs.findStandardFieldSpec(BulletinConstants.TAGEVENTDATE);
		Vector dateRangeChoices = helper.getChoiceItemsForThisField(dateRange);
		assertEquals("not two choices for date range?", 2, dateRangeChoices.size());
		
		FieldSpec gridSpec = createSampleGridSpec();
		Vector gridChoices = helper.getChoiceItemsForThisField(gridSpec);
		assertEquals("not zero choices for a grid?", 0, gridChoices.size());
		
		DropDownFieldSpec dropDownSpec = createSampleDropDownSpec("dropdown");
		Vector dropDownChoices = helper.getChoiceItemsForThisField(dropDownSpec);
		assertEquals("not one choice for dropdown?", 1, dropDownChoices.size());
		{
			ChoiceItem createdChoice = (ChoiceItem)dropDownChoices.get(0);
			DropDownFieldSpec createdSpec = (DropDownFieldSpec)createdChoice.getSpec();
			assertEquals("doesn't have blank plus both sample choices?", 3, createdSpec.getCount());
		}
	}
	
	private GridFieldSpec createSampleGridSpec() throws Exception
	{
		GridFieldSpec gridSpec = new GridFieldSpec();
		String label1 = "column 1";
		FieldSpec column1 = new FieldSpec(label1, FieldSpec.TYPE_NORMAL);

		String label2 = "column 2";
		CustomDropDownFieldSpec column2 = createSampleDropDownSpec(label2);
		gridSpec.addColumn(column1);
		gridSpec.addColumn(column2);
		
		return gridSpec;
	}

	private CustomDropDownFieldSpec createSampleDropDownSpec(String label2)
	{
		CustomDropDownFieldSpec column2 = new CustomDropDownFieldSpec();
		Vector choices = new Vector();
		String choice1 = "choice 1";
		String choice2 = "choice 2";
		choices.add(choice1);
		choices.add(choice2);
		
		column2.setChoices(choices);
		column2.setLabel(label2);
		return column2;
	}
	
	public void testCreateOpColumnSpec()
	{
		DropDownFieldSpec spec = helper.createOpColumnSpec();
		assertTrue("no contains?", spec.findCode(":") >= 0);
		assertTrue("no >?", spec.findCode(":>") >= 0);
		assertTrue("no <?", spec.findCode(":<") >= 0);
		assertTrue("no >=?", spec.findCode(":>=") >= 0);
		assertTrue("no <=?", spec.findCode(":<=") >= 0);
		assertTrue("no =?", spec.findCode(":=") >= 0);
	}
	
	public void testCreateGridSpec()
	{
		GridFieldSpec spec = helper.getGridSpec(store);
		assertEquals(3, spec.getColumnCount());
		assertEquals("no field column?", FieldSpec.TYPE_DROPDOWN, spec.getColumnType(0));
		assertEquals("no op column?", FieldSpec.TYPE_DROPDOWN, spec.getColumnType(1));
		assertEquals("no value column?", FieldSpec.TYPE_MORPHIC, spec.getColumnType(2));
	}
	
	public void testGetSearchString() throws Exception
	{
		GridFieldSpec spec = new GridFieldSpec();
		spec.addColumn(FieldSpec.createStandardField("field", FieldSpec.TYPE_NORMAL));
		spec.addColumn(FieldSpec.createStandardField("op", FieldSpec.TYPE_NORMAL));
		spec.addColumn(FieldSpec.createStandardField("value", FieldSpec.TYPE_MORPHIC));
		GridData data = new GridData(spec);
		addRow(data, "", ":", "whiz");
		addRow(data, "a", "b", "c");
		addRow(data, "d", "e", " f");
		addRow(data, "g", "h", "\"ii\"");
		
		assertEquals("\"whiz\" ab\"c\" de\"f\" gh\"ii\" ", helper.getSearchString(data));
	}
	
	private void addRow(GridData data, String field, String op, String value)
	{
		int row = data.getRowCount();
		data.addEmptyRow();
		data.setValueAt(field, row, 0);
		data.setValueAt(op, row, 1);
		data.setValueAt(value, row, 2);
	}

	ClientBulletinStore store;
	File tempDir;
	MartusLocalization localization;
	FancySearchHelper helper;
}
