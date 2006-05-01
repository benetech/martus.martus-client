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
			CustomFieldError error = (CustomFieldError)errors.get(i);
			assertEquals("wrong code?", CustomFieldError.CODE_ILLEGAL_TAG, error.getCode());
			assertEquals("wrong tag?", variousIllegalTags[i], error.getTag());
			assertEquals("wrong label?", label, error.getLabel());
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
			CustomFieldError error = (CustomFieldError)errors.get(i);
			assertEquals("wrong code?", CustomFieldError.CODE_ILLEGAL_TAG, error.getCode());
			assertEquals("wrong tag?", variousIllegalTags[i], error.getTag());
			assertEquals("wrong label?", label, error.getLabel());
		}
	}

	public void testMissingRequiredFields() throws Exception
	{
		FieldSpec[] emptySpecs = {};
		
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(emptySpecs, emptySpecs);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		int numberOfRequiredFields = 4;
		assertEquals("Should require 4 fields", numberOfRequiredFields, errors.size());
		assertEquals("Incorrect Error code required 1?", CustomFieldError.CODE_REQUIRED_FIELD, ((CustomFieldError)errors.get(0)).getCode());
		assertEquals("Incorrect Error code required 2?", CustomFieldError.CODE_REQUIRED_FIELD, ((CustomFieldError)errors.get(1)).getCode());
		assertEquals("Incorrect Error code required 3?", CustomFieldError.CODE_REQUIRED_FIELD, ((CustomFieldError)errors.get(2)).getCode());
		assertEquals("Incorrect Error code required 4?", CustomFieldError.CODE_REQUIRED_FIELD, ((CustomFieldError)errors.get(3)).getCode());
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
	
	//TODO Martus Field in bottom Section

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
		assertEquals("Incorrect Error code Missing Tags", CustomFieldError.CODE_MISSING_TAG, ((CustomFieldError)errors.get(0)).getCode());
		assertEquals("Incorrect label for Missing Tags", label, ((CustomFieldError)errors.get(0)).getLabel());
		assertEquals("Incorrect type for Missing Tags", FieldSpec.getTypeString(new FieldTypeNormal()), ((CustomFieldError)errors.get(0)).getType());
		assertEquals("Incorrect Error code Missing Tags Bottom Section", CustomFieldError.CODE_MISSING_TAG, ((CustomFieldError)errors.get(1)).getCode());
		assertEquals("Incorrect label for Missing Tags Bottom Section", label, ((CustomFieldError)errors.get(1)).getLabel());
		assertEquals("Incorrect type for Missing Tags Bottom Section", FieldSpec.getTypeString(new FieldTypeNormal()), ((CustomFieldError)errors.get(1)).getType());
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
		assertEquals("Incorrect Error code Duplicate Tags", CustomFieldError.CODE_DUPLICATE_FIELD, ((CustomFieldError)errors.get(0)).getCode());
		assertEquals("Incorrect tag Duplicate Tags", tag, ((CustomFieldError)errors.get(0)).getTag());
		assertEquals("Incorrect label Duplicate Tags", label, ((CustomFieldError)errors.get(0)).getLabel());
		assertEquals("Incorrect Error code Duplicate Tags Bottom", CustomFieldError.CODE_DUPLICATE_FIELD, ((CustomFieldError)errors.get(1)).getCode());
		assertEquals("Incorrect tag Duplicate Tags Bottom", tag2, ((CustomFieldError)errors.get(1)).getTag());
		assertEquals("Incorrect label Duplicate Tags Bottom", label2, ((CustomFieldError)errors.get(1)).getLabel());
		
		//TODO duplicate tag from top found in bottom
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
		assertEquals("Incorrect Error code Duplicate Dropdown Entry", CustomFieldError.CODE_DUPLICATE_DROPDOWN_ENTRY, ((CustomFieldError)errors.get(0)).getCode());
		assertEquals("Incorrect tag Duplicate Tags", tag, ((CustomFieldError)errors.get(0)).getTag());
		assertEquals("Incorrect label Duplicate Tags", label, ((CustomFieldError)errors.get(0)).getLabel());
		assertEquals("Incorrect Error code Duplicate Dropdown Entry Bottom", CustomFieldError.CODE_DUPLICATE_DROPDOWN_ENTRY, ((CustomFieldError)errors.get(1)).getCode());
		assertEquals("Incorrect tag Duplicate Tags Bottom", tag2, ((CustomFieldError)errors.get(1)).getTag());
		assertEquals("Incorrect label Duplicate Tags Bottom", label2, ((CustomFieldError)errors.get(1)).getLabel());
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
		GridFieldSpec gridWithNoDuplicateDropdownEntries = new GridFieldSpec();
		gridWithNoDuplicateDropdownEntries.setTag(tag);
		gridWithNoDuplicateDropdownEntries.setLabel(label);
		gridWithNoDuplicateDropdownEntries.addColumn(dropDownSpecNoDuplicates);
		specsTopSection = addFieldSpec(specsTopSection, gridWithNoDuplicateDropdownEntries);

		ChoiceItem[] choicesNoDups2 = {new ChoiceItem("no Dup2", "first item2"), new ChoiceItem("second2", "second item2")};
		DropDownFieldSpec dropDownSpecNoDuplicates2 = new DropDownFieldSpec(choicesNoDups2);
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
		GridFieldSpec gridWithDuplicateDropdownEntries = new GridFieldSpec();
		gridWithDuplicateDropdownEntries.setTag(tag);
		gridWithDuplicateDropdownEntries.setLabel(label);
		gridWithDuplicateDropdownEntries.addColumn(dropDownSpecWithDuplicates);
		specsTopSection = addFieldSpec(specsTopSection, gridWithDuplicateDropdownEntries);
		
		specsBottomSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		ChoiceItem[] choicesWithDuplicate2 = {new ChoiceItem("duplicate2", "duplicate2"), new ChoiceItem("duplicate2", "duplicate2")};
		DropDownFieldSpec dropDownSpecWithDuplicates2 = new DropDownFieldSpec(choicesWithDuplicate2);
		GridFieldSpec gridWithDuplicateDropdownEntries2 = new GridFieldSpec();
		gridWithDuplicateDropdownEntries2.setTag(tag2);
		gridWithDuplicateDropdownEntries2.setLabel(label2);
		gridWithDuplicateDropdownEntries2.addColumn(dropDownSpecWithDuplicates2);
		specsBottomSection = addFieldSpec(specsBottomSection, gridWithDuplicateDropdownEntries2);

		checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 2 error", 2, errors.size());
		assertEquals("Incorrect Error code Duplicate Dropdown Entry", CustomFieldError.CODE_DUPLICATE_DROPDOWN_ENTRY, ((CustomFieldError)errors.get(0)).getCode());
		assertEquals("Incorrect tag Duplicate Tags", tag, ((CustomFieldError)errors.get(0)).getTag());
		assertEquals("Incorrect label Duplicate Tags", label, ((CustomFieldError)errors.get(0)).getLabel());
		assertEquals("Incorrect Error code Duplicate Dropdown Entry Bottom", CustomFieldError.CODE_DUPLICATE_DROPDOWN_ENTRY, ((CustomFieldError)errors.get(1)).getCode());
		assertEquals("Incorrect tag Duplicate Tags Bottom", tag2, ((CustomFieldError)errors.get(1)).getTag());
		assertEquals("Incorrect label Duplicate Tags Bottom", label2, ((CustomFieldError)errors.get(1)).getLabel());
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
		assertEquals("Incorrect Error code No Dropdown Entries", CustomFieldError.CODE_NO_DROPDOWN_ENTRIES, ((CustomFieldError)errors.get(0)).getCode());
		assertEquals("Incorrect tag Duplicate Tags", tag, ((CustomFieldError)errors.get(0)).getTag());
		assertEquals("Incorrect label Duplicate Tags", label, ((CustomFieldError)errors.get(0)).getLabel());
		assertEquals("Incorrect Error code No Dropdown Entries Bottom", CustomFieldError.CODE_NO_DROPDOWN_ENTRIES, ((CustomFieldError)errors.get(1)).getCode());
		assertEquals("Incorrect tag Duplicate Tags Bottom", tag2, ((CustomFieldError)errors.get(1)).getTag());
		assertEquals("Incorrect label Duplicate Tags Bottom", label2, ((CustomFieldError)errors.get(1)).getLabel());
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
		GridFieldSpec gridWithNoDropdownEntries = new GridFieldSpec();
		gridWithNoDropdownEntries.setTag(tag);
		gridWithNoDropdownEntries.setLabel(label);
		gridWithNoDropdownEntries.addColumn(dropDownSpecNoEntries);
		specsTopSection = addFieldSpec(specsTopSection, gridWithNoDropdownEntries);

		DropDownFieldSpec dropDownSpecNoEntries2 = new DropDownFieldSpec();
		GridFieldSpec gridWithNoDropdownEntries2 = new GridFieldSpec();
		gridWithNoDropdownEntries2.setTag(tag2);
		gridWithNoDropdownEntries2.setLabel(label2);
		gridWithNoDropdownEntries2.addColumn(dropDownSpecNoEntries2);
		specsBottomSection = addFieldSpec(specsBottomSection, gridWithNoDropdownEntries2);

		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 2 error", 2, errors.size());
		assertEquals("Incorrect Error code No Dropdown Entries", CustomFieldError.CODE_NO_DROPDOWN_ENTRIES, ((CustomFieldError)errors.get(0)).getCode());
		assertEquals("Incorrect tag Duplicate Tags", tag, ((CustomFieldError)errors.get(0)).getTag());
		assertEquals("Incorrect label Duplicate Tags", label, ((CustomFieldError)errors.get(0)).getLabel());
		assertEquals("Incorrect Error code No Dropdown Entries Bottom", CustomFieldError.CODE_NO_DROPDOWN_ENTRIES, ((CustomFieldError)errors.get(1)).getCode());
		assertEquals("Incorrect tag Duplicate Tags Bottom", tag2, ((CustomFieldError)errors.get(1)).getTag());
		assertEquals("Incorrect label Duplicate Tags Bottom", label2, ((CustomFieldError)errors.get(1)).getLabel());
	}
	

	public void testMissingCustomLabel() throws Exception
	{
		FieldSpec[] specsTopSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		specsTopSection = addFieldSpec(specsTopSection, LegacyCustomFields.createFromLegacy("a,label"));
		FieldSpec[] specsBottomSection = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		specsBottomSection = addFieldSpec(specsBottomSection, LegacyCustomFields.createFromLegacy("a2,label2"));
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertTrue("not valid?", checker.isValid());
		String tag = "b";
		specsTopSection = addFieldSpec(specsTopSection, LegacyCustomFields.createFromLegacy(tag));
		String tag2 = "b2";
		specsBottomSection = addFieldSpec(specsBottomSection, LegacyCustomFields.createFromLegacy(tag2));
		CustomFieldSpecValidator checker2 = new CustomFieldSpecValidator(specsTopSection, specsBottomSection);
		assertFalse("valid?", checker2.isValid());
		Vector errors = checker2.getAllErrors();
		assertEquals("Should have 2 error", 2, errors.size());
		assertEquals("Incorrect Error code Duplicate Tags", CustomFieldError.CODE_MISSING_LABEL, ((CustomFieldError)errors.get(0)).getCode());
		assertEquals("Incorrect tag Duplicate Tags", tag, ((CustomFieldError)errors.get(0)).getTag());
		assertEquals("Incorrect Error code Duplicate Tags Bottom", CustomFieldError.CODE_MISSING_LABEL, ((CustomFieldError)errors.get(1)).getCode());
		assertEquals("Incorrect tag Duplicate Tags Bottom", tag2, ((CustomFieldError)errors.get(1)).getTag());
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
		assertEquals("Incorrect Error code Unknown Type", CustomFieldError.CODE_UNKNOWN_TYPE, ((CustomFieldError)errors.get(0)).getCode());
		assertEquals("Incorrect tag Unknown Type", tag, ((CustomFieldError)errors.get(0)).getTag());
		assertEquals("Incorrect label Unknown Type", label, ((CustomFieldError)errors.get(0)).getLabel());
		assertEquals("Incorrect Error code Unknown Type Bottom", CustomFieldError.CODE_UNKNOWN_TYPE, ((CustomFieldError)errors.get(1)).getCode());
		assertEquals("Incorrect tag Unknown Type Bottom", tag2, ((CustomFieldError)errors.get(1)).getTag());
		assertEquals("Incorrect label Unknown Type Bottom", label2, ((CustomFieldError)errors.get(1)).getLabel());
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
		assertEquals("Incorrect Error code StandardField with Label", CustomFieldError.CODE_LABEL_STANDARD_FIELD, ((CustomFieldError)errors.get(0)).getCode());
		assertEquals("Incorrect tag StandardField with Label", tag, ((CustomFieldError)errors.get(0)).getTag());
		assertEquals("Incorrect label StandardField with Label", illegal_label, ((CustomFieldError)errors.get(0)).getLabel());
		assertEquals("Incorrect Error code StandardField with Label Bottom", CustomFieldError.CODE_LABEL_STANDARD_FIELD, ((CustomFieldError)errors.get(1)).getCode());
		assertEquals("Incorrect tag StandardField with Label Bottom", tag2, ((CustomFieldError)errors.get(1)).getTag());
		assertEquals("Incorrect label StandardField with Label Bottom", illegal_label2, ((CustomFieldError)errors.get(1)).getLabel());
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

}
