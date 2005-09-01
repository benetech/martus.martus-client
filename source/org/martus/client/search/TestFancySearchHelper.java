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
import org.martus.common.bulletin.Bulletin;
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
		app = MockMartusApp.create();
		getStore().createFieldSpecCacheFromDatabase();
		tempDir = createTempDirectory();
		localization = new MartusLocalization(tempDir, new String[0]);
		UiDialogLauncher nullLauncher = new UiDialogLauncher(null,new MartusLocalization(null, new String[0]));
		helper = new FancySearchHelper(getStore(), nullLauncher);
		
	}
	
	public void tearDown()
	{
		tempDir.delete();
	}
	
	public void testCreateFieldColumnSpec() throws Exception
	{
		app.loadSampleData();

		String languageCode = "en";
		localization.addEnglishTranslations(EnglishCommonStrings.strings);
		localization.setCurrentLanguageCode(languageCode);
		
		DropDownFieldSpec spec = helper.createFieldColumnSpec(getStore());
//		assertNotEquals("inserted an empty first entry?", "", spec.getChoice(0).getCode());
		assertTrue("no ALL FIELDS?", spec.findCode("") >= 0);
		assertTrue("no last-saved date?", spec.findCode(Bulletin.PSEUDOFIELD_LAST_SAVED_DATE) >= 0);
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
		
		FieldSpec withLabel = FieldSpec.createCustomField("tag", "Label", FieldSpec.TYPE_NORMAL);
		Vector withLabelChoices = helper.getChoiceItemsForThisField(withLabel);
		assertEquals("not one choice for normal with label?", 1, withLabelChoices.size());
		{
			ChoiceItem createdChoice = (ChoiceItem)withLabelChoices.get(0);
			assertEquals(withLabel.getTag(), createdChoice.getCode());
			assertEquals(withLabel.getLabel(), createdChoice.toString());
		}
		
		FieldSpec unknownType = FieldSpec.createStandardField("tag", FieldSpec.TYPE_UNKNOWN);
		Vector unknownTypeChoices = helper.getChoiceItemsForThisField(unknownType);
		assertEquals("not zero choices for unknown type?", 0, unknownTypeChoices.size());
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
	
	public void testCreateGridSpec()
	{
		GridFieldSpec spec = helper.getGridSpec(getStore());
		assertEquals(4, spec.getColumnCount());
		assertEquals("no field column?", FieldSpec.TYPE_DROPDOWN, spec.getColumnType(0));
		assertEquals("no op column?", FieldSpec.TYPE_DROPDOWN, spec.getColumnType(1));
		assertEquals("no value column?", FieldSpec.TYPE_SEARCH_VALUE, spec.getColumnType(2));
		assertEquals("no andor column?", FieldSpec.TYPE_DROPDOWN, spec.getColumnType(3));
	}
	
	public void testAndOrColumn() throws Exception
	{
		DropDownFieldSpec spec = helper.createAndOrColumnSpec();
		assertEquals(2, spec.getCount());
		assertEquals("and", spec.getChoice(0).getCode());
		assertEquals("or", spec.getChoice(1).getCode());
	}
	
	public void testGetSearchTreeOneRow() throws Exception
	{
		GridFieldSpec spec = new GridFieldSpec();
		spec.addColumn(FieldSpec.createStandardField("field", FieldSpec.TYPE_NORMAL));
		spec.addColumn(FieldSpec.createStandardField("op", FieldSpec.TYPE_NORMAL));
		spec.addColumn(FieldSpec.createStandardField("value", FieldSpec.TYPE_SEARCH_VALUE));
		spec.addColumn(FieldSpec.createStandardField("andor", FieldSpec.TYPE_NORMAL));
		GridData data = new GridData(spec);
		addRow(data, "field", "", "value", "or");
		SearchTreeNode root = helper.getSearchTree(data);
		verifyFieldCompareOpValue("single row", root, "field", SearchTreeNode.CONTAINS, "value");
	}
	
	public void testGetSearchTreeTwoRows() throws Exception
	{
		GridFieldSpec spec = new GridFieldSpec();
		spec.addColumn(FieldSpec.createStandardField("field", FieldSpec.TYPE_NORMAL));
		spec.addColumn(FieldSpec.createStandardField("op", FieldSpec.TYPE_NORMAL));
		spec.addColumn(FieldSpec.createStandardField("value", FieldSpec.TYPE_SEARCH_VALUE));
		spec.addColumn(FieldSpec.createStandardField("andor", FieldSpec.TYPE_NORMAL));
		GridData data = new GridData(spec);
		addRow(data, "a", "", "b", "or");
		addRow(data, "c", "", "d", "or");
		SearchTreeNode root = helper.getSearchTree(data);
		verifyOp("top level", root, SearchTreeNode.OR);
		verifyFieldCompareOpValue("two rows left", root.getLeft(), "a", SearchTreeNode.CONTAINS, "b");
		verifyFieldCompareOpValue("two rows right", root.getRight(), "c", SearchTreeNode.CONTAINS, "d");
	}
	
	public void testGetSearchTreeComplex() throws Exception
	{
		GridFieldSpec spec = new GridFieldSpec();
		spec.addColumn(FieldSpec.createStandardField("field", FieldSpec.TYPE_NORMAL));
		spec.addColumn(FieldSpec.createStandardField("op", FieldSpec.TYPE_NORMAL));
		spec.addColumn(FieldSpec.createStandardField("value", FieldSpec.TYPE_SEARCH_VALUE));
		spec.addColumn(FieldSpec.createStandardField("andor", FieldSpec.TYPE_NORMAL));
		GridData data = new GridData(spec);
		addRow(data, "", "", "whiz", "or");
		addRow(data, "a", "", "c1 and c2", "or");
		addRow(data, "d", ">", " f", "and");
		addRow(data, "g", "!=", "\"i i\"", "or");
		addRow(data, "", "", "j", "and");
		
		// (((any:whiz or (a~c1 and a~c2)) or d>f) and g!="ii") or any:j
		// OR  - any:j
		//  |
		// AND - g!="ii"
		//  |
		// OR  - d>f
		//  |
		// OR  - AND - a~c2
		//  |     |
		//  |    a~c1
		//  |
		// any:whiz
		SearchTreeNode beforeJ = helper.getSearchTree(data);
		verifyOp("before j", beforeJ, SearchTreeNode.OR);
		verifyFieldCompareOpValue("any:j", beforeJ.getRight(), null, SearchTreeNode.CONTAINS, "j");
		
		SearchTreeNode beforeGii = beforeJ.getLeft();
		verifyOp("before gii", beforeGii, SearchTreeNode.AND);
		verifyFieldCompareOpValue("g!=\"ii\"", beforeGii.getRight(), "g", SearchTreeNode.NOT_EQUAL, "i i");
		
		SearchTreeNode beforeDf = beforeGii.getLeft();
		verifyOp("before df", beforeDf, SearchTreeNode.OR);
		verifyFieldCompareOpValue("d>f", beforeDf.getRight(), "d", SearchTreeNode.GREATER, "f");
		
		SearchTreeNode beforeAandA = beforeDf.getLeft();
		verifyOp("before a a", beforeAandA, SearchTreeNode.OR);
		
		SearchTreeNode betweenAandA = beforeAandA.getRight();
		verifyOp("before a a", betweenAandA, SearchTreeNode.AND);
		verifyFieldCompareOpValue("a:c1", betweenAandA.getLeft(), "a", SearchTreeNode.CONTAINS, "c1");
		verifyFieldCompareOpValue("a:c2", betweenAandA.getRight(), "a", SearchTreeNode.CONTAINS, "c2");
		
		verifyFieldCompareOpValue("whiz", beforeAandA.getLeft(), null, SearchTreeNode.CONTAINS, "whiz");
	}
	
	private void verifyOp(String message, SearchTreeNode node, int expectedOp)
	{
		assertEquals(message, expectedOp, node.getOperation());
	}
	
	private void verifyFieldCompareOpValue(String message, SearchTreeNode node, String field, int compareOp, String value)
	{
		assertEquals(message + " wrong op?", SearchTreeNode.VALUE, node.getOperation());
		assertEquals(message + " wrong field?", field, node.getField());
		assertEquals(message + " wrong compareOp?", compareOp, node.getComparisonOperator());
		assertEquals(message + " wrong value?", value, node.getValue());
	}
	
	private void addRow(GridData data, String field, String op, String value, String andOr)
	{
		int row = data.getRowCount();
		data.addEmptyRow();
		data.setValueAt(field, row, 0);
		data.setValueAt(op, row, 1);
		data.setValueAt(value, row, 2);
		data.setValueAt(andOr, row, 3);
	}

	ClientBulletinStore getStore()
	{
		return app.getStore();
	}
	
	MockMartusApp app;
	File tempDir;
	MartusLocalization localization;
	FancySearchHelper helper;
}
