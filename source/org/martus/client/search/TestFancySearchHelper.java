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
import org.martus.client.test.MockBulletinStore;
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
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.FieldTypeDropdown;
import org.martus.common.fieldspec.FieldTypeMessage;
import org.martus.common.fieldspec.FieldTypeMultiline;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.FieldTypeSearchValue;
import org.martus.common.fieldspec.FieldTypeUnknown;
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
		assertEquals("ALL FIELDS not first?", 0, spec.findCode(""));
		assertTrue("no last-saved date?", spec.findCode(Bulletin.PSEUDOFIELD_LAST_SAVED_DATE) >= 0);
		assertTrue("no author?", spec.findCode(BulletinConstants.TAGAUTHOR) >= 0);
		assertTrue("no private?", spec.findCode(BulletinConstants.TAGPRIVATEINFO) >= 0);
		assertTrue("no eventdate.begin?", spec.findCode(BulletinConstants.TAGEVENTDATE + "." + MartusDateRangeField.SUBFIELD_BEGIN) >= 0);
		assertTrue("no eventdate.end?", spec.findCode(BulletinConstants.TAGEVENTDATE + "." + MartusDateRangeField.SUBFIELD_END) >= 0);
		assertFalse("has raw eventdate?", spec.findCode(BulletinConstants.TAGEVENTDATE) >= 0);
	}
	
	public void testAllFieldTypesSearchable() throws Exception
	{
		FieldSpec message = createSampleMessageSpec();

		final String value = "value";
		ClientBulletinStore store = new MockBulletinStore();
		Bulletin b = new Bulletin(store.getSignatureGenerator(), new FieldSpec[] {message}, new FieldSpec[0]);
		b.set(message.getTag(), value);
		store.saveBulletinForTesting(b);
		
		DropDownFieldSpec spec = helper.createFieldColumnSpec(store);
		assertTrue("no message-type field?", spec.findCode(message.getTag()) >= 0);
		assertEquals("wrong label?", message.getLabel(), spec.getDisplayString(message.getTag()));
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
		Vector gridTypeChoices = helper.getChoiceItemsForThisField(gridSpec);
		assertEquals("not one choice for a grid?", 1, gridTypeChoices.size());
		ChoiceItem gridChoice = (ChoiceItem)gridTypeChoices.get(0);
		FieldSpec gridChoiceSpec = gridChoice.getSpec();
		assertEquals("grid doesn't have string search?", new FieldTypeNormal(), gridChoiceSpec.getType());
		
		DropDownFieldSpec dropDownSpec = createSampleDropDownSpec("dropdown");
		Vector dropDownChoices = helper.getChoiceItemsForThisField(dropDownSpec);
		assertEquals("not one choice for dropdown?", 1, dropDownChoices.size());
		{
			ChoiceItem createdChoice = (ChoiceItem)dropDownChoices.get(0);
			DropDownFieldSpec createdSpec = (DropDownFieldSpec)createdChoice.getSpec();
			assertEquals("doesn't have blank plus both sample choices?", 3, createdSpec.getCount());
		}
		
		FieldSpec withLabel = FieldSpec.createCustomField("tag", "Label", new FieldTypeNormal());
		Vector withLabelChoices = helper.getChoiceItemsForThisField(withLabel);
		assertEquals("not one choice for normal with label?", 1, withLabelChoices.size());
		{
			ChoiceItem createdChoice = (ChoiceItem)withLabelChoices.get(0);
			assertEquals(withLabel.getTag(), createdChoice.getCode());
			assertEquals(withLabel.getLabel(), createdChoice.toString());
		}
		
		FieldSpec messageType = createSampleMessageSpec();
		Vector messageTypeChoices = helper.getChoiceItemsForThisField(messageType);
		assertEquals("not one choice for message fields?", 1, messageTypeChoices.size());
		ChoiceItem messageChoice = (ChoiceItem)messageTypeChoices.get(0);
		FieldSpec messageChoiceSpec = messageChoice.getSpec();
		assertEquals("message doesn't have string search?", new FieldTypeNormal(), messageChoiceSpec.getType());
		
		FieldSpec multilineType = createSampleMultilineSpec();
		Vector multilineTypeChoices = helper.getChoiceItemsForThisField(multilineType);
		assertEquals("not one choice for multiline fields?", 1, multilineTypeChoices.size());
		ChoiceItem multilineChoice = (ChoiceItem)multilineTypeChoices.get(0);
		FieldSpec multilineChoiceSpec = multilineChoice.getSpec();
		assertEquals("multiline doesn't have string search?", new FieldTypeNormal(), multilineChoiceSpec.getType());
		
		FieldSpec booleanType = createSampleBooleanSpec();
		Vector booleanTypeChoices = helper.getChoiceItemsForThisField(booleanType);
		assertEquals("not one choice for boolean fields?", 1, booleanTypeChoices.size());
		ChoiceItem booleanChoice = (ChoiceItem)booleanTypeChoices.get(0);
		FieldSpec booleanChoiceSpec = booleanChoice.getSpec();
		assertEquals("boolean doesn't have checkbox?", new FieldTypeBoolean(), booleanChoiceSpec.getType());

		FieldSpec unknownType = FieldSpec.createStandardField("tag", new FieldTypeUnknown());
		Vector unknownTypeChoices = helper.getChoiceItemsForThisField(unknownType);
		assertEquals("not zero choices for unknown type?", 0, unknownTypeChoices.size());
	}
	
	private FieldSpec createSampleMessageSpec()
	{
		final String tag = "messagetag";
		final String label = "Message Label: ";
		FieldSpec message = FieldSpec.createCustomField(tag, label, new FieldTypeMessage());
		return message;
	}
	
	private FieldSpec createSampleMultilineSpec()
	{
		final String tag = "multilinetag";
		final String label = "Multiline Label: ";
		FieldSpec message = FieldSpec.createCustomField(tag, label, new FieldTypeMultiline());
		return message;
	}
	
	private FieldSpec createSampleBooleanSpec()
	{
		final String tag = "booleantag";
		final String label = "Boolean Label: ";
		FieldSpec message = FieldSpec.createCustomField(tag, label, new FieldTypeBoolean());
		return message;
	}
	
	private GridFieldSpec createSampleGridSpec() throws Exception
	{
		GridFieldSpec gridSpec = new GridFieldSpec();
		String label1 = "column 1";
		FieldSpec column1 = new FieldSpec(label1, new FieldTypeNormal());

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
		assertEquals("no field column?", new FieldTypeDropdown(), spec.getColumnType(0));
		assertEquals("no op column?", new FieldTypeDropdown(), spec.getColumnType(1));
		assertEquals("no value column?", new FieldTypeSearchValue(), spec.getColumnType(2));
		assertEquals("no andor column?", new FieldTypeDropdown(), spec.getColumnType(3));
	}
	
	public void testAndOrColumn() throws Exception
	{
		DropDownFieldSpec spec = helper.createAndOrColumnSpec();
		assertEquals(2, spec.getCount());
		assertEquals("and", spec.getChoice(0).getCode());
		assertEquals("or", spec.getChoice(1).getCode());
	}
	
	public void testGetSearchTreeBooleanValue() throws Exception
	{
		FieldSpec booleanSpec = FieldSpec.createCustomField("tag", "Label", new FieldTypeBoolean());
		ChoiceItem[] fields = new ChoiceItem[] {
			new ChoiceItem(booleanSpec),
		};
		DropDownFieldSpec fieldColumnSpec = new DropDownFieldSpec(fields);
		
		GridFieldSpec spec = new GridFieldSpec();
		spec.addColumn(fieldColumnSpec);
		spec.addColumn(FieldSpec.createStandardField("op", new FieldTypeNormal()));
		spec.addColumn(FieldSpec.createStandardField("value", new FieldTypeSearchValue()));
		spec.addColumn(FieldSpec.createStandardField("andor", new FieldTypeNormal()));
		GridData data = new GridData(spec);
		String localizedTrue = localization.getButtonLabel("yes");
		addRow(data, "tag", "=", "1", "or");
		
		SearchTreeNode booleanEquals = helper.getSearchTree(data);
		assertEquals(SearchTreeNode.VALUE, booleanEquals.getOperation());
		assertEquals("tag", booleanEquals.getField().getTag());
		assertEquals(localizedTrue, booleanEquals.getValue());
	}
	
	public void testGetSearchTreeOneRow() throws Exception
	{
		FieldSpec normalSpec = FieldSpec.createCustomField("field", "Label", new FieldTypeNormal());
		ChoiceItem[] fields = new ChoiceItem[] {
			new ChoiceItem(normalSpec),
		};
		DropDownFieldSpec fieldColumnSpec = new DropDownFieldSpec(fields);

		GridFieldSpec spec = new GridFieldSpec();
		spec.addColumn(fieldColumnSpec);
		spec.addColumn(FieldSpec.createStandardField("op", new FieldTypeNormal()));
		spec.addColumn(FieldSpec.createStandardField("value", new FieldTypeSearchValue()));
		spec.addColumn(FieldSpec.createStandardField("andor", new FieldTypeNormal()));
		GridData data = new GridData(spec);
		addRow(data, "field", "", "value", "or");
		SearchTreeNode root = helper.getSearchTree(data);
		verifyFieldCompareOpValue("single row", root, normalSpec, SearchTreeNode.CONTAINS, "value");
	}
	
	public void testGetSearchTreeTwoRows() throws Exception
	{
		FieldSpec a = FieldSpec.createCustomField("a", "A", new FieldTypeNormal());
		FieldSpec c = FieldSpec.createCustomField("c", "C", new FieldTypeNormal());
		ChoiceItem[] fields = new ChoiceItem[] {
			new ChoiceItem(a),
			new ChoiceItem(c),
		};
		DropDownFieldSpec fieldColumnSpec = new DropDownFieldSpec(fields);

		GridFieldSpec spec = new GridFieldSpec();
		spec.addColumn(fieldColumnSpec);
		spec.addColumn(FieldSpec.createStandardField("op", new FieldTypeNormal()));
		spec.addColumn(FieldSpec.createStandardField("value", new FieldTypeSearchValue()));
		spec.addColumn(FieldSpec.createStandardField("andor", new FieldTypeNormal()));
		GridData data = new GridData(spec);
		addRow(data, "a", "", "b", "or");
		addRow(data, "c", "", "d", "or");
		SearchTreeNode root = helper.getSearchTree(data);
		verifyOp("top level", root, SearchTreeNode.OR);
		verifyFieldCompareOpValue("two rows left", root.getLeft(), a, SearchTreeNode.CONTAINS, "b");
		verifyFieldCompareOpValue("two rows right", root.getRight(), c, SearchTreeNode.CONTAINS, "d");
	}
	
	public void testGetSearchTreeComplex() throws Exception
	{
		FieldSpec any = FieldSpec.createCustomField("", "Any", new FieldTypeNormal());
		FieldSpec a = FieldSpec.createCustomField("a", "A", new FieldTypeNormal());
		FieldSpec d = FieldSpec.createCustomField("d", "D", new FieldTypeNormal());
		FieldSpec g = FieldSpec.createCustomField("g", "G", new FieldTypeNormal());
		ChoiceItem[] fields = new ChoiceItem[] {
			new ChoiceItem(any),
			new ChoiceItem(a),
			new ChoiceItem(d),
			new ChoiceItem(g),
		};
		DropDownFieldSpec fieldColumnSpec = new DropDownFieldSpec(fields);

		GridFieldSpec spec = new GridFieldSpec();
		spec.addColumn(fieldColumnSpec);
		spec.addColumn(FieldSpec.createStandardField("op", new FieldTypeNormal()));
		spec.addColumn(FieldSpec.createStandardField("value", new FieldTypeSearchValue()));
		spec.addColumn(FieldSpec.createStandardField("andor", new FieldTypeNormal()));
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
		verifyFieldCompareOpValue("any:j", beforeJ.getRight(), any, SearchTreeNode.CONTAINS, "j");
		
		SearchTreeNode beforeGii = beforeJ.getLeft();
		verifyOp("before gii", beforeGii, SearchTreeNode.AND);
		verifyFieldCompareOpValue("g!=\"ii\"", beforeGii.getRight(), g, SearchTreeNode.NOT_EQUAL, "i i");
		
		SearchTreeNode beforeDf = beforeGii.getLeft();
		verifyOp("before df", beforeDf, SearchTreeNode.OR);
		verifyFieldCompareOpValue("d>f", beforeDf.getRight(), d, SearchTreeNode.GREATER, "f");
		
		SearchTreeNode beforeAandA = beforeDf.getLeft();
		verifyOp("before a a", beforeAandA, SearchTreeNode.OR);
		
		SearchTreeNode betweenAandA = beforeAandA.getRight();
		verifyOp("before a a", betweenAandA, SearchTreeNode.AND);
		verifyFieldCompareOpValue("a:c1", betweenAandA.getLeft(), a, SearchTreeNode.CONTAINS, "c1");
		verifyFieldCompareOpValue("a:c2", betweenAandA.getRight(), a, SearchTreeNode.CONTAINS, "c2");
		
		verifyFieldCompareOpValue("whiz", beforeAandA.getLeft(), any, SearchTreeNode.CONTAINS, "whiz");
	}
	
	private void verifyOp(String message, SearchTreeNode node, int expectedOp)
	{
		assertEquals(message, expectedOp, node.getOperation());
	}
	
	private void verifyFieldCompareOpValue(String message, SearchTreeNode node, FieldSpec field, int compareOp, String value)
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
