/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.test.MockBulletinStore;
import org.martus.client.test.MockMartusApp;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.GridData;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.crypto.MartusCrypto;
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
import org.martus.common.fieldspec.FieldTypeUnknown;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.fieldspec.PopUpTreeFieldSpec;
import org.martus.common.fieldspec.SearchableFieldChoiceItem;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.fieldspec.GridFieldSpec.UnsupportedFieldTypeException;
import org.martus.util.TestCaseEnhanced;

public class TestFieldChooserSpecBuilder extends TestCaseEnhanced
{
	public TestFieldChooserSpecBuilder(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		app = MockMartusApp.create();
		getStore().createFieldSpecCacheFromDatabase();
		tempDir = createTempDirectory();
		localization = new MartusLocalization(tempDir, new String[0]);
		localization.setCurrentLanguageCode(MiniLocalization.ENGLISH);
		searchBuilder = new SearchFieldChooserSpecBuilder(localization);
		sortBuilder = new SortFieldChooserSpecBuilder(localization);
	}
	
	public void tearDown()throws Exception
	{
		tempDir.delete();
		app.deleteAllFiles();
	}
	
	public void testCaseInsensitiveSorting() throws Exception
	{
		FieldSpec upperCase = FieldSpec.createCustomField("upper", "UPPER", new FieldTypeNormal());
		FieldSpec lowerCase = FieldSpec.createCustomField("lower", "lower", new FieldTypeNormal());
		
		ClientBulletinStore store = new MockBulletinStore();
		Bulletin b = new Bulletin(store.getSignatureGenerator(), new FieldSpec[] {upperCase, lowerCase}, new FieldSpec[0]);
		store.saveBulletinForTesting(b);
		
		Vector specs = new Vector(Arrays.asList(searchBuilder.createFieldSpecArray(store)));
		assertTrue("Didn't put lower before upper?", specs.indexOf(lowerCase) < specs.indexOf(upperCase));
	}
	
	public void testCreateFieldColumnSpec() throws Exception
	{
		app.loadSampleData();

		String languageCode = "en";
		localization.addEnglishTranslations(EnglishCommonStrings.strings);
		localization.setCurrentLanguageCode(languageCode);
		
		PopUpTreeFieldSpec spec = searchBuilder.createSpec(getStore());
		SearchFieldTreeNode root = (SearchFieldTreeNode)spec.getModel().getRoot();
		SearchFieldTreeNode firstNode = (SearchFieldTreeNode)root.getChildAt(0);
		SearchableFieldChoiceItem allFieldsItem = firstNode.getChoiceItem();
		assertEquals("ALL FIELDS not first?", "", allFieldsItem.getSearchTag());
		
		assertNotNull("no last-saved date?", FancySearchHelper.findSearchTag(spec, Bulletin.PSEUDOFIELD_LAST_SAVED_DATE));
		assertNotNull("no author?", FancySearchHelper.findSearchTag(spec, BulletinConstants.TAGAUTHOR));
		assertNotNull("no private?", FancySearchHelper.findSearchTag(spec, BulletinConstants.TAGPRIVATEINFO));
		assertNotNull("no eventdate.begin?", FancySearchHelper.findSearchTag(spec, BulletinConstants.TAGEVENTDATE + "." + MartusDateRangeField.SUBFIELD_BEGIN));
		assertNotNull("no eventdate.end?", FancySearchHelper.findSearchTag(spec, BulletinConstants.TAGEVENTDATE + "." + MartusDateRangeField.SUBFIELD_END));
		assertNull("has raw eventdate?", FancySearchHelper.findSearchTag(spec, BulletinConstants.TAGEVENTDATE));
	}
	
	public void testAllFieldTypesSearchable() throws Exception
	{
		FieldSpec message = createSampleMessageSpec();

		final String value = "value";
		ClientBulletinStore store = new MockBulletinStore();
		Bulletin b = new Bulletin(store.getSignatureGenerator(), new FieldSpec[] {message}, new FieldSpec[0]);
		b.set(message.getTag(), value);
		store.saveBulletinForTesting(b);
		
		PopUpTreeFieldSpec spec = searchBuilder.createSpec(store);
		SearchableFieldChoiceItem item = spec.findSearchTag( message.getTag());
		assertEquals("wrong label?", message.getLabel(), item.toString());
	}
	
	public void testAnyFieldShowsFirst() throws Exception
	{
		FieldSpec aardvark = FieldSpec.createCustomField("a", "Aardvark", new FieldTypeNormal());
		ClientBulletinStore store = new MockBulletinStore();
		Bulletin b = new Bulletin(store.getSignatureGenerator(), new FieldSpec[] {aardvark}, new FieldSpec[0]);
		b.set(aardvark.getTag(), "whatever");
		store.saveBulletinForTesting(b);
		
		PopUpTreeFieldSpec spec = searchBuilder.createSpec(store);
		SearchableFieldChoiceItem anyField = spec.getFirstChoice();
		assertEquals("Any Field not first?", "", anyField.getSpec().getTag());
	}
	
	public void testMultilineNotSortable() throws Exception
	{
		Bulletin b = new Bulletin(getStore().getSignatureGenerator());
		getStore().saveBulletin(b);
		
		PopUpTreeFieldSpec spec = sortBuilder.createSpec(getStore());
		assertNull("multiline is sortable?", spec.findSearchTag(Bulletin.TAGPUBLICINFO));
	}

	public void testGridNotSortable() throws Exception
	{
		GridFieldSpec gridSpec = new GridFieldSpec();
		gridSpec.setTag("grid");
		gridSpec.setLabel("Grid");
		gridSpec.addColumn(FieldSpec.createCustomField("", "Label", new FieldTypeNormal()));
		MartusCrypto security = getStore().getSignatureGenerator();
		Bulletin b = new Bulletin(security, new FieldSpec[] {gridSpec}, new FieldSpec[0]);
		GridData gridData = new GridData(gridSpec);
		gridData.addEmptyRow();
		gridData.setValueAt("Data", 0, 0);
		b.set(gridSpec.getTag(), gridData.getXmlRepresentation());
		getStore().saveBulletin(b);
		
		PopUpTreeFieldSpec spec = sortBuilder.createSpec(getStore());
		assertNull("grid is sortable?", spec.findSearchTag(gridSpec.getTag()));
		assertNull("grid column is sortable?", spec.findSearchTag(gridSpec.getTag() + ".Label"));
	}

	public void testGetChoiceItemsForThisField() throws Exception
	{
		FieldSpec normal = StandardFieldSpecs.findStandardFieldSpec(BulletinConstants.TAGAUTHOR);
		Set normalChoices = searchBuilder.getChoiceItemsForThisField(normal);
		assertEquals("more than one choice for a plain text field?", 1, normalChoices.size());
		
		FieldSpec dateRange = StandardFieldSpecs.findStandardFieldSpec(BulletinConstants.TAGEVENTDATE);
		Set dateRangeChoices = searchBuilder.getChoiceItemsForThisField(dateRange);
		assertEquals("not two choices for date range?", 2, dateRangeChoices.size());
		
		DropDownFieldSpec dropDownSpec = createSampleDropDownSpec("dropdown");
		Set dropDownChoices = searchBuilder.getChoiceItemsForThisField(dropDownSpec);
		assertEquals("not one choice for dropdown?", 1, dropDownChoices.size());
		{
			ChoiceItem createdChoice = (ChoiceItem)dropDownChoices.iterator().next();
			DropDownFieldSpec createdSpec = (DropDownFieldSpec)createdChoice.getSpec();
			assertEquals("doesn't have blank plus both sample choices?", 3, createdSpec.getCount());
		}
		
		FieldSpec withLabel = FieldSpec.createCustomField("tag", "Label", new FieldTypeNormal());
		Set withLabelChoices = searchBuilder.getChoiceItemsForThisField(withLabel);
		assertEquals("not one choice for normal with label?", 1, withLabelChoices.size());
		{
			ChoiceItem createdChoice = (ChoiceItem)withLabelChoices.iterator().next();
			assertEquals(new MiniFieldSpec(withLabel).toJson().toString(), createdChoice.getCode());
			assertEquals(withLabel.getLabel(), createdChoice.toString());
		}
		
		FieldSpec messageType = createSampleMessageSpec();
		Set messageTypeChoices = searchBuilder.getChoiceItemsForThisField(messageType);
		assertEquals("not one choice for message fields?", 1, messageTypeChoices.size());
		ChoiceItem messageChoice = (ChoiceItem)messageTypeChoices.iterator().next();
		FieldSpec messageChoiceSpec = messageChoice.getSpec();
		assertEquals("message doesn't have string search?", new FieldTypeNormal(), messageChoiceSpec.getType());
		
		FieldSpec multilineType = createSampleMultilineSpec();
		Set multilineTypeChoices = searchBuilder.getChoiceItemsForThisField(multilineType);
		assertEquals("not one choice for multiline fields?", 1, multilineTypeChoices.size());
		ChoiceItem multilineChoice = (ChoiceItem)multilineTypeChoices.iterator().next();
		FieldSpec multilineChoiceSpec = multilineChoice.getSpec();
		assertEquals("multiline doesn't have string search?", new FieldTypeNormal(), multilineChoiceSpec.getType());
		
		FieldSpec booleanType = createSampleBooleanSpec();
		Set booleanTypeChoices = searchBuilder.getChoiceItemsForThisField(booleanType);
		assertEquals("not one choice for boolean fields?", 1, booleanTypeChoices.size());
		ChoiceItem booleanChoice = (ChoiceItem)booleanTypeChoices.iterator().next();
		FieldSpec booleanChoiceSpec = booleanChoice.getSpec();
		assertEquals("boolean doesn't have checkbox?", new FieldTypeBoolean(), booleanChoiceSpec.getType());

		FieldSpec unknownType = FieldSpec.createStandardField("tag", new FieldTypeUnknown());
		Set unknownTypeChoices = searchBuilder.getChoiceItemsForThisField(unknownType);
		assertEquals("not zero choices for unknown type?", 0, unknownTypeChoices.size());
		
		FieldSpec blankLabel = FieldSpec.createCustomField("tag", "  ", new FieldTypeNormal());
		Set blankLabelChoices = searchBuilder.getChoiceItemsForThisField(blankLabel);
		ChoiceItem blankLabelChoice = (ChoiceItem)blankLabelChoices.iterator().next();
		assertEquals("didn't use tag for blank label", blankLabel.getTag(), blankLabelChoice.toString());
	}
	
	public void testGetChoiceItemsForThisFieldGrid() throws Exception
	{
		GridFieldSpec gridSpec = createSampleGridSpec();
		Set gridTypeChoices = searchBuilder.getChoiceItemsForThisField(gridSpec);
		assertEquals("not one choice for each grid column?", gridSpec.getColumnCount(), gridTypeChoices.size());
		
		Iterator iter = gridTypeChoices.iterator();
		while(iter.hasNext())
		{
			ChoiceItem choice = (ChoiceItem)iter.next();
			if(choice.getType().isString())
			{
				ChoiceItem gridChoiceNormalColumn = choice;
				FieldSpec gridChoiceNormalColumnSpec = gridChoiceNormalColumn.getSpec();
				assertEquals("bad normal grid column?", new FieldTypeNormal(), gridChoiceNormalColumnSpec.getType());
				assertEquals("Grid Label: column 1", gridChoiceNormalColumnSpec.getLabel());
				assertEquals("column 1", gridChoiceNormalColumnSpec.getSubFieldTag());
				assertEquals("column parent not the grid?", gridSpec.getTag(), gridChoiceNormalColumnSpec.getParent().getTag());
				assertEquals("wrong column fulltag?", "gridtag.column 1", gridChoiceNormalColumnSpec.getTag());
			}
			else
			{
				ChoiceItem gridChoiceDropDownColumn = choice;
				DropDownFieldSpec gridChoiceDropDownColumnSpec = (DropDownFieldSpec)gridChoiceDropDownColumn.getSpec();
				assertEquals("bad dropdown grid column?", new FieldTypeDropdown(), gridChoiceDropDownColumnSpec.getType());
				assertEquals("Grid Label: column.2", gridChoiceDropDownColumnSpec.getLabel());
				assertEquals("gridtag.column 2", gridChoiceDropDownColumnSpec.getTag());
				ChoiceItem empty = gridChoiceDropDownColumnSpec.getChoice(0);
				ChoiceItem first = gridChoiceDropDownColumnSpec.getChoice(1);
				ChoiceItem second = gridChoiceDropDownColumnSpec.getChoice(2);
				assertEquals("wrong empty choice?", "", empty.toString());
				assertEquals("wrong first choice?", "choice 1", first.toString());
				assertEquals("wrong second choice?", "choice 2", second.toString());
			}
		}
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
		final String tag = "gridtag";

		return createSampleGridSpecWithTag(tag);
	}

	private GridFieldSpec createSampleGridSpecWithTag(final String tag) throws UnsupportedFieldTypeException
	{
		GridFieldSpec gridSpec = new GridFieldSpec();
		gridSpec.setLabel("Grid Label");
		gridSpec.setTag(tag);
		String label1 = "column 1";
		FieldSpec column1 = FieldSpec.createFieldSpec(label1, new FieldTypeNormal());
		final String GRID_COLUMNS_DONT_HAVE_TAGS = "";
		column1.setTag(GRID_COLUMNS_DONT_HAVE_TAGS);

		String label2 = "column.2";
		CustomDropDownFieldSpec column2 = createSampleDropDownSpec(label2);
		gridSpec.addColumn(column1);
		gridSpec.addColumn(column2);
		
		return gridSpec;
	}

	private CustomDropDownFieldSpec createSampleDropDownSpec(String label2)
	{
		CustomDropDownFieldSpec column2 = new CustomDropDownFieldSpec();
		column2.setTag("dropdowntag");
		Vector choices = new Vector();
		String choice1 = "choice 1";
		String choice2 = "choice 2";
		choices.add(choice1);
		choices.add(choice2);
		
		column2.setChoices(choices);
		column2.setLabel(label2);
		return column2;
	}
	
	ClientBulletinStore getStore()
	{
		return app.getStore();
	}
	
	MockMartusApp app;
	File tempDir;
	MartusLocalization localization;
	FieldChooserSpecBuilder searchBuilder;
	FieldChooserSpecBuilder sortBuilder;
}
