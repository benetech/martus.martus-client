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

import java.util.Vector;

import org.martus.common.FieldCollection;
import org.martus.common.LegacyCustomFields;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.CustomFieldError;
import org.martus.common.fieldspec.CustomFieldSpecValidator;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeLanguage;
import org.martus.common.fieldspec.FieldTypeMultiline;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.util.TestCaseEnhanced;

public class TestCustomFieldSpecValidator extends TestCaseEnhanced
{
	public TestCustomFieldSpecValidator(String name)
	{
		super(name);
	}
	
	public void testAllValid() throws Exception
	{
		FieldSpec[] specsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpec[] specsBottomSection = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		String tag = "_A.-_AllValid0123456789";
		String label = "my Label";
		specsTopSection = addFieldSpec(specsTopSection, LegacyCustomFields.createFromLegacy(tag+","+label));
		String tagB = "_B.-_AllValid0123456789";
		String labelB = "my Label B";
		specsBottomSection = addFieldSpec(specsBottomSection, LegacyCustomFields.createFromLegacy(tagB+","+labelB));
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertTrue("not valid?", checker.isValid());
	}

	public void testNull() throws Exception
	{
		FieldSpec[] nullSpecs = null;
		FieldSpec[] specsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpec[] specsBottomSection = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(nullSpecs, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		CustomFieldError error = (CustomFieldError)checker.getAllErrors().get(0);
		assertEquals("Incorrect Error code?", CustomFieldError.CODE_NULL_SPECS, error.getCode());

		checker = new CustomFieldSpecValidator(specsTopSection, nullSpecs);
		assertFalse("valid Bottom?", checker.isValid());
		error = (CustomFieldError)checker.getAllErrors().get(0);
		assertEquals("Incorrect Error code Bottom?", CustomFieldError.CODE_NULL_SPECS, error.getCode());
	}
	
	public void testIllegalTagCharactersTopSection() throws Exception
	{
		FieldSpec[] specsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpec[] specsBottomSection = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		String label = "anything";
		String[] variousIllegalTags = {"a tag", "a&amp;b", "a=b", "a'b", ".a"};
		for(int i=0; i < variousIllegalTags.length; ++i)
		{
			String thisTag = variousIllegalTags[i];
			FieldSpec thisSpec = FieldSpec.createCustomField(thisTag, label, new FieldTypeNormal());
			specsTopSection = addFieldSpec(specsTopSection, thisSpec);
		}
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("didn't catch all errors?", variousIllegalTags.length, errors.size());
		for(int i=0; i < errors.size(); ++i)
		{
			verifyExpectedError("IllegalTagCharactersTop", CustomFieldError.CODE_ILLEGAL_TAG, variousIllegalTags[i], label, null, (CustomFieldError)errors.get(i));
		}
	}
	
	public void testIllegalTagCharactersBottomSection() throws Exception
	{
		FieldSpec[] specsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpec[] specsBottomSection = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		String label = "anything";
		String[] variousIllegalTags = {"a tag", "a&amp;b", "a=b", "a'b", ".a"};
		for(int i=0; i < variousIllegalTags.length; ++i)
		{
			String thisTag = variousIllegalTags[i];
			FieldSpec thisSpec = FieldSpec.createCustomField(thisTag, label, new FieldTypeNormal());
			specsBottomSection = addFieldSpec(specsBottomSection, thisSpec);
		}
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("didn't catch all errors?", variousIllegalTags.length, errors.size());
		for(int i=0; i < errors.size(); ++i)
		{
			verifyExpectedError("IllegalTagCharactersBottom", CustomFieldError.CODE_ILLEGAL_TAG, variousIllegalTags[i], label, null, (CustomFieldError)errors.get(i));
		}
	}

	public void testMissingRequiredFields() throws Exception
	{
		FieldSpec[] emptySpecs = {};
		
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(emptySpecs, emptySpecs);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		int numberOfRequiredFields = 4;
		assertEquals("Should require 4 fields", numberOfRequiredFields , errors.size());
		for (int i = 0; i<numberOfRequiredFields; ++i)
		{
			assertEquals("Incorrect Error code required "+i, CustomFieldError.CODE_REQUIRED_FIELD, ((CustomFieldError)errors.get(i)).getCode());
		}
		Vector errorFields = new Vector();
		for (int i = 0; i<numberOfRequiredFields; ++i)
		{
			errorFields.add(((CustomFieldError)errors.get(i)).getTag());
		}
		assertContains(BulletinConstants.TAGAUTHOR, errorFields);
		assertContains(BulletinConstants.TAGLANGUAGE, errorFields);
		assertContains(BulletinConstants.TAGENTRYDATE, errorFields);
		assertContains(BulletinConstants.TAGTITLE, errorFields);
	}
	
	public void testReservedFields() throws Exception
	{
		FieldSpec[] specsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpec[] specsBottomSection = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();

		String tagStatus = BulletinConstants.TAGSTATUS;
		String labelStatus ="status";
		specsTopSection = addFieldSpec(specsTopSection, LegacyCustomFields.createFromLegacy(tagStatus+","+labelStatus));
		
		String tagSent = BulletinConstants.TAGWASSENT;
		String labelSent ="sent";
		specsTopSection = addFieldSpec(specsTopSection, LegacyCustomFields.createFromLegacy(tagSent+","+labelSent));
		
		String tagSaved = BulletinConstants.TAGLASTSAVED;
		String labelSaved ="saved";
		specsTopSection = addFieldSpec(specsTopSection, LegacyCustomFields.createFromLegacy(tagSaved+","+labelSaved));

		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have found 3 errors", 3 , errors.size());
		verifyExpectedError("Reserved Fields", CustomFieldError.CODE_RESERVED_TAG, tagStatus, labelStatus, null, (CustomFieldError)errors.get(0));
		verifyExpectedError("Reserved Fields", CustomFieldError.CODE_RESERVED_TAG, tagSent, labelSent, null, (CustomFieldError)errors.get(1));
		verifyExpectedError("Reserved Fields", CustomFieldError.CODE_RESERVED_TAG, tagSaved, labelSaved, null, (CustomFieldError)errors.get(2));
		
		
		specsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		specsBottomSection = addFieldSpec(specsBottomSection, LegacyCustomFields.createFromLegacy(tagStatus+","+labelStatus));
		specsBottomSection = addFieldSpec(specsBottomSection, LegacyCustomFields.createFromLegacy(tagSent+","+labelSent));
		specsBottomSection = addFieldSpec(specsBottomSection, LegacyCustomFields.createFromLegacy(tagSaved+","+labelSaved));

		checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		errors = checker.getAllErrors();
		assertEquals("Should have found 3 errors", 3 , errors.size());
		verifyExpectedError("Reserved Fields Bottom Section", CustomFieldError.CODE_RESERVED_TAG, tagStatus, labelStatus, null, (CustomFieldError)errors.get(0));
		verifyExpectedError("Reserved Fields Bottom Section", CustomFieldError.CODE_RESERVED_TAG, tagSent, labelSent, null, (CustomFieldError)errors.get(1));
		verifyExpectedError("Reserved Fields Bottom Section", CustomFieldError.CODE_RESERVED_TAG, tagSaved, labelSaved, null, (CustomFieldError)errors.get(2));
	}

	public void testMartusFieldsInBottomSection() throws Exception
	{
		FieldSpec[] specsTopSection = getRequiredOnlyTopSectionFieldSpecs();
		FieldSpec[] specsBottomSection = getAllNonRequiredMartusFieldSpecs();
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("Valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		int numberOfMartusFields = 6;
		assertEquals("Should require 6 fields", numberOfMartusFields, errors.size());
		for (int i = 0; i<numberOfMartusFields; ++i)
		{
			assertEquals("Incorrect Error code required "+i, CustomFieldError.CODE_MARTUS_FIELD_IN_BOTTOM_SECTION, ((CustomFieldError)errors.get(i)).getCode());
		}
		Vector errorFields = new Vector();
		for (int i = 0; i<numberOfMartusFields; ++i)
		{
			errorFields.add(((CustomFieldError)errors.get(i)).getTag());
		}
		assertContains(BulletinConstants.TAGORGANIZATION, errorFields);
		assertContains(BulletinConstants.TAGLOCATION, errorFields);
		assertContains(BulletinConstants.TAGEVENTDATE, errorFields);
		assertContains(BulletinConstants.TAGKEYWORDS, errorFields);
		assertContains(BulletinConstants.TAGSUMMARY, errorFields);
		assertContains(BulletinConstants.TAGPUBLICINFO, errorFields);
	}

	public void testPrivateFieldInTopSection() throws Exception
	{
		FieldSpec[] specsTopSection = getRequiredOnlyTopSectionFieldSpecs();
		FieldSpec[] specsBottomSection = {};
		
		specsTopSection = addFieldSpec(specsTopSection, FieldSpec.createStandardField(BulletinConstants.TAGPRIVATEINFO, new FieldTypeMultiline()));

		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("Valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals(1, errors.size());
		assertEquals("Incorrect Error code required ", CustomFieldError.CODE_PRIVATE_FIELD_IN_TOP_SECTION, ((CustomFieldError)errors.get(0)).getCode());
		assertEquals(BulletinConstants.TAGPRIVATEINFO, ((CustomFieldError)errors.get(0)).getTag());
	}

	public void testMissingTag() throws Exception
	{
		FieldSpec[] specsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpec[] specsBottomSection = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		String label = "my Label";
		specsTopSection = addFieldSpec(specsTopSection, LegacyCustomFields.createFromLegacy(","+label));
		specsBottomSection = addFieldSpec(specsBottomSection, LegacyCustomFields.createFromLegacy(","+label));
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 2 errors", 2, errors.size());
		verifyExpectedError("Missing Tags", CustomFieldError.CODE_MISSING_TAG, null, label, new FieldTypeNormal(), (CustomFieldError)errors.get(0));
		verifyExpectedError("Missing Tags Bottom Section", CustomFieldError.CODE_MISSING_TAG, null, label, new FieldTypeNormal(), (CustomFieldError)errors.get(1));
	}

	public void testDuplicateTags() throws Exception
	{
		FieldSpec[] specsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpec[] specsBottomSection = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		String tag = "a";
		String label ="b";
		specsTopSection = addFieldSpec(specsTopSection, LegacyCustomFields.createFromLegacy(tag+","+label));
		specsTopSection = addFieldSpec(specsTopSection, LegacyCustomFields.createFromLegacy(tag+","+label));
		String tag2 = "a2";
		String label2 ="b2";
		specsBottomSection = addFieldSpec(specsBottomSection, LegacyCustomFields.createFromLegacy(tag2+","+label2));
		specsBottomSection = addFieldSpec(specsBottomSection, LegacyCustomFields.createFromLegacy(tag2+","+label2));
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 2 error", 2, errors.size());
		verifyExpectedError("Duplicate Tags", CustomFieldError.CODE_DUPLICATE_FIELD, tag, label, null, (CustomFieldError)errors.get(0));
		verifyExpectedError("Duplicate Tags Bottom Section", CustomFieldError.CODE_DUPLICATE_FIELD, tag2, label2, null, (CustomFieldError)errors.get(1));

		//TODO duplicate tag from top found in bottom
		specsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		specsBottomSection = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		specsTopSection = addFieldSpec(specsTopSection, LegacyCustomFields.createFromLegacy(tag+","+label));
		specsBottomSection = addFieldSpec(specsBottomSection, LegacyCustomFields.createFromLegacy(tag+","+label));
		checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		errors = checker.getAllErrors();
		assertEquals("Should have 1 error", 1, errors.size());
		verifyExpectedError("Duplicate Tags not found in Bottom?", CustomFieldError.CODE_DUPLICATE_FIELD, tag, label, null, (CustomFieldError)errors.get(0));
		
	}

	public void testDuplicateDropDownEntry() throws Exception
	{
		FieldSpec[] specsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpec[] specsBottomSection = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		String tag = "dd";
		String label ="cc";
		ChoiceItem[] choicesNoDups = {new ChoiceItem("no Dup", "first item"), new ChoiceItem("second", "second item")};
		DropDownFieldSpec dropDownSpecNoDuplicates = new DropDownFieldSpec(choicesNoDups);
		dropDownSpecNoDuplicates.setTag(tag);
		dropDownSpecNoDuplicates.setLabel(label);
		specsTopSection = addFieldSpec(specsTopSection, dropDownSpecNoDuplicates);
		
		String tag2 = "dd2";
		String label2 ="cc2";
		ChoiceItem[] choicesNoDups2 = {new ChoiceItem("no Dup2", "first item2"), new ChoiceItem("second", "second item")};
		DropDownFieldSpec dropDownSpecNoDuplicates2 = new DropDownFieldSpec(choicesNoDups2);
		dropDownSpecNoDuplicates2.setTag(tag2);
		dropDownSpecNoDuplicates2.setLabel(label2);
		specsBottomSection = addFieldSpec(specsBottomSection, dropDownSpecNoDuplicates2);

		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertTrue("invalid?", checker.isValid());
		
		specsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		specsBottomSection = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();

		ChoiceItem[] choicesWithDuplicate = {new ChoiceItem("duplicate", "duplicate"), new ChoiceItem("duplicate", "duplicate")};
		DropDownFieldSpec dropDownSpecWithDuplicates = new DropDownFieldSpec(choicesWithDuplicate);
		dropDownSpecWithDuplicates.setTag(tag);
		dropDownSpecWithDuplicates.setLabel(label);
		specsTopSection = addFieldSpec(specsTopSection, dropDownSpecWithDuplicates);

		ChoiceItem[] choicesWithDuplicate2 = {new ChoiceItem("duplicate2", "duplicate2"), new ChoiceItem("duplicate2", "duplicate2")};
		DropDownFieldSpec dropDownSpecWithDuplicates2 = new DropDownFieldSpec(choicesWithDuplicate2);
		dropDownSpecWithDuplicates2.setTag(tag2);
		dropDownSpecWithDuplicates2.setLabel(label2);
		specsBottomSection = addFieldSpec(specsBottomSection, dropDownSpecWithDuplicates2);
		
		checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 2 error", 2, errors.size());
		verifyExpectedError("Duplicate Dropdown Entry", CustomFieldError.CODE_DUPLICATE_DROPDOWN_ENTRY, tag, label, null, (CustomFieldError)errors.get(0));
		verifyExpectedError("Duplicate Dropdown Entry Bottom Section", CustomFieldError.CODE_DUPLICATE_DROPDOWN_ENTRY, tag2, label2, null, (CustomFieldError)errors.get(1));
	}

	public void testDuplicateDropDownEntryInSideOfAGrid() throws Exception
	{
		String tag = "dd";
		String label ="cc";
		FieldSpec[] specsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		String tag2 = "dd2";
		String label2 ="cc2";
		FieldSpec[] specsBottomSection = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();

		ChoiceItem[] choicesNoDups = {new ChoiceItem("no Dup", "first item"), new ChoiceItem("second", "second item")};
		DropDownFieldSpec dropDownSpecNoDuplicates = new DropDownFieldSpec(choicesNoDups);
		dropDownSpecNoDuplicates.setLabel("dropdown column label");
		GridFieldSpec gridWithNoDuplicateDropdownEntries = new GridFieldSpec();
		gridWithNoDuplicateDropdownEntries.setTag(tag);
		gridWithNoDuplicateDropdownEntries.setLabel(label);
		gridWithNoDuplicateDropdownEntries.addColumn(dropDownSpecNoDuplicates);
		specsTopSection = addFieldSpec(specsTopSection, gridWithNoDuplicateDropdownEntries);

		ChoiceItem[] choicesNoDups2 = {new ChoiceItem("no Dup2", "first item2"), new ChoiceItem("second2", "second item2")};
		DropDownFieldSpec dropDownSpecNoDuplicates2 = new DropDownFieldSpec(choicesNoDups2);
		dropDownSpecNoDuplicates2.setLabel("label");
		GridFieldSpec gridWithNoDuplicateDropdownEntries2 = new GridFieldSpec();
		gridWithNoDuplicateDropdownEntries2.setTag(tag2);
		gridWithNoDuplicateDropdownEntries2.setLabel(label2);
		gridWithNoDuplicateDropdownEntries2.addColumn(dropDownSpecNoDuplicates2);
		specsBottomSection = addFieldSpec(specsBottomSection, gridWithNoDuplicateDropdownEntries2);

		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertTrue("invalid?", checker.isValid());
		
		specsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		ChoiceItem[] choicesWithDuplicate = {new ChoiceItem("duplicate", "duplicate"), new ChoiceItem("duplicate", "duplicate")};
		DropDownFieldSpec dropDownSpecWithDuplicates = new DropDownFieldSpec(choicesWithDuplicate);
		dropDownSpecWithDuplicates.setLabel("dropdown column label with dups");
		GridFieldSpec gridWithDuplicateDropdownEntries = new GridFieldSpec();
		gridWithDuplicateDropdownEntries.setTag(tag);
		gridWithDuplicateDropdownEntries.setLabel(label);
		gridWithDuplicateDropdownEntries.addColumn(dropDownSpecWithDuplicates);
		specsTopSection = addFieldSpec(specsTopSection, gridWithDuplicateDropdownEntries);
		
		specsBottomSection = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		ChoiceItem[] choicesWithDuplicate2 = {new ChoiceItem("duplicate2", "duplicate2"), new ChoiceItem("duplicate2", "duplicate2")};
		DropDownFieldSpec dropDownSpecWithDuplicates2 = new DropDownFieldSpec(choicesWithDuplicate2);
		dropDownSpecWithDuplicates2.setLabel("Dropdown label");
		GridFieldSpec gridWithDuplicateDropdownEntries2 = new GridFieldSpec();
		gridWithDuplicateDropdownEntries2.setTag(tag2);
		gridWithDuplicateDropdownEntries2.setLabel(label2);
		gridWithDuplicateDropdownEntries2.addColumn(dropDownSpecWithDuplicates2);
		specsBottomSection = addFieldSpec(specsBottomSection, gridWithDuplicateDropdownEntries2);

		checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 2 error", 2, errors.size());
		verifyExpectedError("Duplicate Dropdown Entry", CustomFieldError.CODE_DUPLICATE_DROPDOWN_ENTRY, tag, label, null, (CustomFieldError)errors.get(0));
		verifyExpectedError("Duplicate Dropdown Entry Bottom Section", CustomFieldError.CODE_DUPLICATE_DROPDOWN_ENTRY, tag2, label2, null, (CustomFieldError)errors.get(1));
	}
	
	public void testNoDropDownEntries() throws Exception
	{
		FieldSpec[] specsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		String tag = "dd";
		String label ="cc";
		FieldSpec[] specsBottomSection = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		String tag2 = "dd2";
		String label2 ="cc2";

		DropDownFieldSpec dropDownSpecNoEntries = new DropDownFieldSpec();
		dropDownSpecNoEntries.setTag(tag);
		dropDownSpecNoEntries.setLabel(label);
		specsTopSection = addFieldSpec(specsTopSection, dropDownSpecNoEntries);

		DropDownFieldSpec dropDownSpecNoEntries2 = new DropDownFieldSpec();
		dropDownSpecNoEntries2.setTag(tag2);
		dropDownSpecNoEntries2.setLabel(label2);
		specsBottomSection = addFieldSpec(specsBottomSection, dropDownSpecNoEntries2);
		
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 2 error", 2, errors.size());
		verifyExpectedError("No Dropdown Entries", CustomFieldError.CODE_NO_DROPDOWN_ENTRIES, tag, label, null, (CustomFieldError)errors.get(0));
		verifyExpectedError("No Dropdown Entries Bottom Section", CustomFieldError.CODE_NO_DROPDOWN_ENTRIES, tag2, label2, null, (CustomFieldError)errors.get(1));
	}

	public void testNoDropDownEntriesInsideOfAGrid() throws Exception
	{
		String tag = "dd";
		String label ="cc";
		FieldSpec[] specsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		String tag2 = "dd2";
		String label2 ="cc2";
		FieldSpec[] specsBottomSection = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();

		DropDownFieldSpec dropDownSpecNoEntries = new DropDownFieldSpec();
		dropDownSpecNoEntries.setLabel("dropdown label");
		GridFieldSpec gridWithNoDropdownEntries = new GridFieldSpec();
		gridWithNoDropdownEntries.setTag(tag);
		gridWithNoDropdownEntries.setLabel(label);
		gridWithNoDropdownEntries.addColumn(dropDownSpecNoEntries);
		specsTopSection = addFieldSpec(specsTopSection, gridWithNoDropdownEntries);

		DropDownFieldSpec dropDownSpecNoEntries2 = new DropDownFieldSpec();
		dropDownSpecNoEntries2.setLabel("dropdown label 2");
		GridFieldSpec gridWithNoDropdownEntries2 = new GridFieldSpec();
		gridWithNoDropdownEntries2.setTag(tag2);
		gridWithNoDropdownEntries2.setLabel(label2);
		gridWithNoDropdownEntries2.addColumn(dropDownSpecNoEntries2);
		specsBottomSection = addFieldSpec(specsBottomSection, gridWithNoDropdownEntries2);

		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 2 error", 2, errors.size());
		verifyExpectedError("No Dropdown Entries In Grids", CustomFieldError.CODE_NO_DROPDOWN_ENTRIES, tag, label, null, (CustomFieldError)errors.get(0));
		verifyExpectedError("No Dropdown Entries In Grids Bottom Section", CustomFieldError.CODE_NO_DROPDOWN_ENTRIES, tag2, label2, null, (CustomFieldError)errors.get(1));
	}
	
	public void testBlankLabelsInsideOfAGrid() throws Exception
	{
		String columnTag = "dd";
		String columnEmptyLabel ="";
		FieldSpec[] specsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpec[] specsBottomSection = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();

		
		String gridTag = "Grid";
		String gridLabel = "Grid Label";
		GridFieldSpec gridWithEmptyColumnLabel = new GridFieldSpec();
		gridWithEmptyColumnLabel.setTag(gridTag);
		gridWithEmptyColumnLabel.setLabel(gridLabel);
		
		gridWithEmptyColumnLabel.addColumn(LegacyCustomFields.createFromLegacy(columnTag+","+columnEmptyLabel));
		specsTopSection = addFieldSpec(specsTopSection, gridWithEmptyColumnLabel);

		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 1 error", 1, errors.size());
		verifyExpectedError("Empty Column Label in Grids", CustomFieldError.CODE_MISSING_LABEL, gridTag, columnEmptyLabel, null, (CustomFieldError)errors.get(0));
	}

	public void testMissingCustomLabel() throws Exception
	{
		FieldSpec[] specsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		specsTopSection = addFieldSpec(specsTopSection, LegacyCustomFields.createFromLegacy("a,label"));
		FieldSpec[] specsBottomSection = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		specsBottomSection = addFieldSpec(specsBottomSection, LegacyCustomFields.createFromLegacy("a2,label2"));
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertTrue("not valid?", checker.isValid());
		String tag = "b";
		specsTopSection = addFieldSpec(specsTopSection, LegacyCustomFields.createFromLegacy(tag));
		String tag1 = "ab";
		String spaceLabel = " ";
		specsTopSection = addFieldSpec(specsTopSection, LegacyCustomFields.createFromLegacy(tag1+","+spaceLabel));
		String tag2 = "b2";
		specsBottomSection = addFieldSpec(specsBottomSection, LegacyCustomFields.createFromLegacy(tag2));
		CustomFieldSpecValidator checker2 = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker2.isValid());
		Vector errors = checker2.getAllErrors();
		assertEquals("Should have 3 error", 3, errors.size());
		verifyExpectedError("Missing Label", CustomFieldError.CODE_MISSING_LABEL, tag, null, null, (CustomFieldError)errors.get(0));
		verifyExpectedError("Label with spaces Only", CustomFieldError.CODE_MISSING_LABEL, tag1, null, null, (CustomFieldError)errors.get(1));
		verifyExpectedError("Missing Label Bottom Section", CustomFieldError.CODE_MISSING_LABEL, tag2, null, null, (CustomFieldError)errors.get(2));
	}
	
	public void testUnknownType() throws Exception
	{
		String tag = "weirdTag";
		String label = "weird Label";
		String xmlFieldUnknownType = "<CustomFields><Field><Tag>"+tag+"</Tag>" +
			"<Label>" + label + "</Label><Type>xxx</Type>" +
			"</Field></CustomFields>";
		FieldSpec badSpecTopSection = FieldCollection.parseXml(xmlFieldUnknownType)[0]; 
		FieldSpec[] specsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		specsTopSection = addFieldSpec(specsTopSection, badSpecTopSection);
		
		String tag2 = "weirdTag2";
		String label2 = "weird Label2";
		String xmlFieldUnknownType2 = "<CustomFields><Field><Tag>"+tag2+"</Tag>" +
			"<Label>" + label2 + "</Label><Type>xxx</Type>" +
			"</Field></CustomFields>";
		FieldSpec badSpecBottomSection = FieldCollection.parseXml(xmlFieldUnknownType2)[0]; 
		FieldSpec[] specsBottomSection = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		specsBottomSection = addFieldSpec(specsBottomSection, badSpecBottomSection);

		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("didn't detect unknown?", checker.isValid());

		Vector errors = checker.getAllErrors();
		assertEquals("Should have 2 error", 2, errors.size());
		verifyExpectedError("Unknown Type", CustomFieldError.CODE_UNKNOWN_TYPE, tag, label, null, (CustomFieldError)errors.get(0));
		verifyExpectedError("Unknown Type Bottom Section", CustomFieldError.CODE_UNKNOWN_TYPE, tag2, label2, null, (CustomFieldError)errors.get(1));
	}

	public void testStandardFieldWithLabel() throws Exception
	{
		FieldSpec[] specsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		String tag = specsTopSection[3].getTag();
		String illegal_label = "Some Label";
		specsTopSection[3] = LegacyCustomFields.createFromLegacy(tag + ","+ illegal_label);

		FieldSpec[] specsBottomSection = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		String tag2 = specsBottomSection[0].getTag();
		String illegal_label2 = "Some Label2";
		specsBottomSection[0] = LegacyCustomFields.createFromLegacy(tag2 + ","+ illegal_label2);

		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());

		Vector errors = checker.getAllErrors();
		assertEquals("Should have 2 error", 2, errors.size());
		verifyExpectedError("StandardField with Label", CustomFieldError.CODE_LABEL_STANDARD_FIELD, tag, illegal_label, null, (CustomFieldError)errors.get(0));
		verifyExpectedError("StandardField with Label Bottom Section", CustomFieldError.CODE_LABEL_STANDARD_FIELD, tag2, illegal_label2, null, (CustomFieldError)errors.get(1));
	}

	public void testParseXmlError() throws Exception
	{
		CustomFieldError xmlError = CustomFieldError.errorParseXml();
		assertEquals("Incorrect Error code for parse XML error", CustomFieldError.CODE_PARSE_XML, xmlError.getCode());
	}

	public void testIOError() throws Exception
	{
		String errorMessage = "io message";
		CustomFieldError xmlError = CustomFieldError.errorIO(errorMessage);
		assertEquals("Incorrect Error code for IO error", CustomFieldError.CODE_IO_ERROR, xmlError.getCode());
		assertEquals("Incorrect error message for IO error", errorMessage, xmlError.getType());
	}

	public void testSignatureError() throws Exception
	{
		CustomFieldError xmlError = CustomFieldError.errorSignature();
		assertEquals("Incorrect Error code for signature error", CustomFieldError.CODE_SIGNATURE_ERROR, xmlError.getCode());
	}

	public void testUnauthorizedKeyError() throws Exception
	{
		CustomFieldError xmlError = CustomFieldError.errorUnauthorizedKey();
		assertEquals("Incorrect Error code for parse XML error", CustomFieldError.CODE_UNAUTHORIZED_KEY, xmlError.getCode());
	}

	static public FieldSpec[] addFieldSpec(FieldSpec[] existingFieldSpecs, FieldSpec newFieldSpec)
	{
		int oldCount = existingFieldSpecs.length;
		FieldSpec[] tempFieldTags = new FieldSpec[oldCount + 1];
		System.arraycopy(existingFieldSpecs, 0, tempFieldTags, 0, oldCount);
		tempFieldTags[oldCount] = newFieldSpec;
		return tempFieldTags;
	}

	private void verifyExpectedError(String reportingErrorMsg, String expectedErrorCode, String expectedTag, String expectedLabel, FieldType expectedType, CustomFieldError errorToVerify) 
	{
		assertEquals("Incorrect Error code: " + reportingErrorMsg, expectedErrorCode, (errorToVerify).getCode());
		if(expectedTag != null)
			assertEquals("Incorrect tag: " + reportingErrorMsg, expectedTag, errorToVerify.getTag());
		if(expectedLabel != null)
			assertEquals("Incorrect label: " + reportingErrorMsg, expectedLabel, errorToVerify.getLabel());
		if(expectedType != null)
			assertEquals("Incorrect type: " + reportingErrorMsg , FieldSpec.getTypeString(expectedType), errorToVerify.getType());
	}

	public static FieldSpec[] getRequiredOnlyTopSectionFieldSpecs()
	{
		FieldSpec[] requiredOnlyTopSectionFieldSpecs = new FieldSpec[] 
			{
				FieldSpec.createStandardField(BulletinConstants.TAGLANGUAGE, new FieldTypeLanguage()),
				FieldSpec.createStandardField(BulletinConstants.TAGAUTHOR, new FieldTypeNormal()),
				FieldSpec.createStandardField(BulletinConstants.TAGTITLE, new FieldTypeNormal()),
				FieldSpec.createStandardField(BulletinConstants.TAGENTRYDATE, new FieldTypeDate()),
			};
		
		return requiredOnlyTopSectionFieldSpecs;
		
	}
	
	public static FieldSpec[] getAllNonRequiredMartusFieldSpecs()
	{
		FieldSpec[] allNonRequiredMartusFieldSpecs = new FieldSpec[] 
			{
				FieldSpec.createStandardField(BulletinConstants.TAGORGANIZATION, new FieldTypeLanguage()),
				FieldSpec.createStandardField(BulletinConstants.TAGLOCATION, new FieldTypeNormal()),
				FieldSpec.createStandardField(BulletinConstants.TAGKEYWORDS, new FieldTypeNormal()),
				FieldSpec.createStandardField(BulletinConstants.TAGEVENTDATE, new FieldTypeDate()),
				FieldSpec.createStandardField(BulletinConstants.TAGSUMMARY, new FieldTypeDate()),
				FieldSpec.createStandardField(BulletinConstants.TAGPUBLICINFO, new FieldTypeDate()),
			};
		
		return allNonRequiredMartusFieldSpecs;
		
	}
	
}
