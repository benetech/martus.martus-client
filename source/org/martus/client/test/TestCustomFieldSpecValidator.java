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

import org.martus.client.core.CustomFieldError;
import org.martus.client.core.CustomFieldSpecValidator;
import org.martus.common.CustomFields;
import org.martus.common.StandardFieldSpecs;
import org.martus.common.FieldSpec;
import org.martus.common.LegacyCustomFields;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.util.TestCaseEnhanced;

public class TestCustomFieldSpecValidator extends TestCaseEnhanced
{
	public TestCustomFieldSpecValidator(String name)
	{
		super(name);
	}
	
	public void testAllValid() throws Exception
	{
		FieldSpec[] specs = StandardFieldSpecs.getDefaultPublicFieldSpecs();
		String tag = "A.-_AllValid0123456789";
		String label = "my Label";
		specs = addFieldSpec(specs, LegacyCustomFields.createFromLegacy(tag+","+label));
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specs);
		assertTrue("not valid?", checker.isValid());
	}

	public void testNull() throws Exception
	{
		FieldSpec[] specs = null;
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specs);
		assertFalse("valid?", checker.isValid());
		CustomFieldError error = (CustomFieldError)checker.getAllErrors().get(0);
		assertEquals("Incorrect Error code?", CustomFieldError.CODE_NULL_SPECS, error.getCode());
	}
	
	public void testIllegalTagCharacters() throws Exception
	{
		FieldSpec[] specs = StandardFieldSpecs.getDefaultPublicFieldSpecs();
		String label = "anything";
		String[] variousIllegalTags = {"a tag", "a&amp;b", "a=b", "a'b", ".a"};
		for(int i=0; i < variousIllegalTags.length; ++i)
		{
			String thisTag = variousIllegalTags[i];
			FieldSpec thisSpec = FieldSpec.createCustomField(thisTag, label, FieldSpec.TYPE_NORMAL);
			specs = addFieldSpec(specs, thisSpec);
		}
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specs);
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
		FieldSpec[] specs = {};
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specs);
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

	public void testMissingTag() throws Exception
	{
		FieldSpec[] specs = StandardFieldSpecs.getDefaultPublicFieldSpecs();
		String label = "my Label";
		specs = addFieldSpec(specs, LegacyCustomFields.createFromLegacy(","+label));
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specs);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 1 error", 1, errors.size());
		assertEquals("Incorrect Error code Missing Tags", CustomFieldError.CODE_MISSING_TAG, ((CustomFieldError)errors.get(0)).getCode());
		assertEquals("Incorrect label for Missing Tags", label, ((CustomFieldError)errors.get(0)).getLabel());
		assertEquals("Incorrect type for Missing Tags", FieldSpec.getTypeString(FieldSpec.TYPE_NORMAL), ((CustomFieldError)errors.get(0)).getType());
	}

	public void testDuplicateTags() throws Exception
	{
		FieldSpec[] specs = StandardFieldSpecs.getDefaultPublicFieldSpecs();
		String tag = "a";
		String label ="b";
		specs = addFieldSpec(specs, LegacyCustomFields.createFromLegacy(tag+","+label));
		specs = addFieldSpec(specs, LegacyCustomFields.createFromLegacy(tag+","+label));
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specs);
		assertFalse("valid?", checker.isValid());
		Vector errors = checker.getAllErrors();
		assertEquals("Should have 1 error", 1, errors.size());
		assertEquals("Incorrect Error code Duplicate Tags", CustomFieldError.CODE_DUPLICATE_FIELD, ((CustomFieldError)errors.get(0)).getCode());
		assertEquals("Incorrect tag Duplicate Tags", tag, ((CustomFieldError)errors.get(0)).getTag());
		assertEquals("Incorrect label Duplicate Tags", label, ((CustomFieldError)errors.get(0)).getLabel());
	}

	public void testMissingCustomLabel() throws Exception
	{
		FieldSpec[] specs = StandardFieldSpecs.getDefaultPublicFieldSpecs();
		specs = addFieldSpec(specs, LegacyCustomFields.createFromLegacy("a,label"));
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specs);
		assertTrue("not valid?", checker.isValid());
		String tag = "b";
		specs = addFieldSpec(specs, LegacyCustomFields.createFromLegacy(tag));
		CustomFieldSpecValidator checker2 = new CustomFieldSpecValidator(specs);
		assertFalse("valid?", checker2.isValid());
		Vector errors = checker2.getAllErrors();
		assertEquals("Should have 1 error", 1, errors.size());
		assertEquals("Incorrect Error code Duplicate Tags", CustomFieldError.CODE_MISSING_LABEL, ((CustomFieldError)errors.get(0)).getCode());
		assertEquals("Incorrect tag Duplicate Tags", tag, ((CustomFieldError)errors.get(0)).getTag());
	}
	
	public void testUnknownType() throws Exception
	{
		FieldSpec[] specs = StandardFieldSpecs.getDefaultPublicFieldSpecs();
		CustomFields fields = new CustomFields(specs);
		String tag = "weirdTag";
		String label = "weird Label";
		String xmlFieldUnknownType = "<CustomFields><Field><Tag>"+tag+"</Tag>" +
			"<Label>" + label + "</Label><Type>xxx</Type>" +
			"</Field></CustomFields>";
		FieldSpec badSpec = CustomFields.parseXml(xmlFieldUnknownType)[0]; 
		fields.add(badSpec);
		specs = addFieldSpec(specs, badSpec);
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specs);
		assertFalse("didn't detect unknown?", checker.isValid());

		Vector errors = checker.getAllErrors();
		assertEquals("Should have 1 error", 1, errors.size());
		assertEquals("Incorrect Error code Unknown Type", CustomFieldError.CODE_UNKNOWN_TYPE, ((CustomFieldError)errors.get(0)).getCode());
		assertEquals("Incorrect tag Unknown Type", tag, ((CustomFieldError)errors.get(0)).getTag());
		assertEquals("Incorrect label Unknown Type", label, ((CustomFieldError)errors.get(0)).getLabel());
	}

	public void testStandardFieldWithLabel() throws Exception
	{
		FieldSpec[] specs = StandardFieldSpecs.getDefaultPublicFieldSpecs();
		String tag = specs[3].getTag();
		String illegal_label = "Some Label";
		specs[3] = LegacyCustomFields.createFromLegacy(tag + ","+ illegal_label);
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specs);
		assertFalse("valid?", checker.isValid());

		Vector errors = checker.getAllErrors();
		assertEquals("Should have 1 error", 1, errors.size());
		assertEquals("Incorrect Error code StandardField with Label", CustomFieldError.CODE_LABEL_STANDARD_FIELD, ((CustomFieldError)errors.get(0)).getCode());
		assertEquals("Incorrect tag StandardField with Label", tag, ((CustomFieldError)errors.get(0)).getTag());
		assertEquals("Incorrect label StandardField with Label", illegal_label, ((CustomFieldError)errors.get(0)).getLabel());
	}

	public void testParseXmlError() throws Exception
	{
		CustomFieldError xmlError = CustomFieldError.errorParseXml();
		assertEquals("Incorrect Error code for parse XML error", CustomFieldError.CODE_PARSE_XML, xmlError.getCode());
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
