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
package org.martus.client.test;

import java.io.File;
import org.martus.client.core.CustomFieldError;
import org.martus.client.core.CustomFieldTemplate;
import org.martus.common.CustomFields;
import org.martus.common.FieldSpec;
import org.martus.common.StandardFieldSpecs;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeWriter;

public class TestCustomFieldTemplate extends TestCaseEnhanced
{
	public TestCustomFieldTemplate(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testValidateXml() throws Exception
	{
		CustomFields fields = new CustomFields(StandardFieldSpecs.getDefaultPublicFieldSpecs());
		CustomFieldTemplate template = new CustomFieldTemplate();
		
		assertTrue("not valid?", template.validateXml(fields.toString()));
		assertNull(template.getErrors());
		
		FieldSpec invalidField = FieldSpec.createCustomField("myTag", "myLabel", 55);
		fields.add(invalidField);
		assertFalse("Should not be a valid template", template.validateXml(fields.toString()));
		assertEquals(1, template.getErrors().size());
		assertEquals(CustomFieldError.CODE_UNKNOWN_TYPE,((CustomFieldError)template.getErrors().get(0)).getCode());
	}
	
	public void testExportXml() throws Exception
	{
		CustomFields fields = new CustomFields(StandardFieldSpecs.getDefaultPublicFieldSpecs());
		CustomFieldTemplate template = new CustomFieldTemplate();
		File exportFile = createTempFileFromName("$$$testExportXml");
		exportFile.delete();
		assertFalse(exportFile.exists());
		assertTrue(template.ExportTemplate(exportFile, fields.toString()));
		assertTrue(exportFile.exists());
		exportFile.delete();

		FieldSpec invalidField = FieldSpec.createCustomField("myTag", "myLabel", 55);
		fields.add(invalidField);
		assertFalse(exportFile.exists());
		assertFalse(template.ExportTemplate(exportFile, fields.toString()));
		assertFalse(exportFile.exists());
		exportFile.delete();
	}
	
	public void testImportXml() throws Exception
	{
		CustomFields fields = new CustomFields(StandardFieldSpecs.getDefaultPublicFieldSpecs());
		CustomFieldTemplate template = new CustomFieldTemplate();
		File exportFile = createTempFileFromName("$$$testExportXml");
		exportFile.delete();
		template.ExportTemplate(exportFile, fields.toString());
		assertEquals("", template.getImportedText());
		assertTrue(template.importTemplate(exportFile));
		exportFile.delete();

		FieldSpec invalidField = FieldSpec.createCustomField("myTag", "myLabel", 55);
		fields.add(invalidField);
		UnicodeWriter writer = new UnicodeWriter(exportFile);
		writer.write(fields.toString());
		writer.close();
		CustomFieldTemplate template2 = new CustomFieldTemplate();
		assertFalse(template2.importTemplate(exportFile));
		exportFile.delete();
		assertEquals("", template2.getImportedText());
	}

	
}
