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
import java.io.FileOutputStream;
import java.util.Vector;
import org.martus.client.core.CustomFieldError;
import org.martus.client.core.CustomFieldTemplate;
import org.martus.common.FieldCollection;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
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
		if(security == null)
		{
			security = new MockMartusSecurity();
			security.createKeyPair(512);
		}
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testValidateXml() throws Exception
	{
		FieldCollection fields = new FieldCollection(StandardFieldSpecs.getDefaultPublicFieldSpecs());
		CustomFieldTemplate template = new CustomFieldTemplate();
		assertTrue("not valid?", template.isvalidTemplateXml(fields.toString()));
		assertEquals(0, template.getErrors().size());
		
		FieldSpec invalidField = FieldSpec.createCustomField("myTag", "myLabel", 55);
		fields.add(invalidField);
		assertFalse("Should not be a valid template", template.isvalidTemplateXml(fields.toString()));
		assertEquals(1, template.getErrors().size());
		assertEquals(CustomFieldError.CODE_UNKNOWN_TYPE,((CustomFieldError)template.getErrors().get(0)).getCode());
	}
	
	public void testExportXml() throws Exception
	{
		FieldCollection fields = new FieldCollection(StandardFieldSpecs.getDefaultPublicFieldSpecs());
		CustomFieldTemplate template = new CustomFieldTemplate();
		File exportFile = createTempFileFromName("$$$testExportXml");
		exportFile.delete();
		assertFalse(exportFile.exists());
		assertTrue(template.ExportTemplate(security, exportFile, fields.toString()));
		assertTrue(exportFile.exists());
		exportFile.delete();

		FieldSpec invalidField = FieldSpec.createCustomField("myTag", "myLabel", 55);
		fields.add(invalidField);
		assertFalse(exportFile.exists());
		assertFalse(template.ExportTemplate(security, exportFile, fields.toString()));
		assertFalse(exportFile.exists());
		exportFile.delete();
	}
	
	public void testImportXml() throws Exception
	{
		FieldCollection fields = new FieldCollection(StandardFieldSpecs.getDefaultPublicFieldSpecs());
		CustomFieldTemplate template = new CustomFieldTemplate();
		File exportFile = createTempFileFromName("$$$testExportXml");
		exportFile.delete();
		template.ExportTemplate(security, exportFile, fields.toString());
		assertEquals("", template.getImportedText());
		Vector authorizedKeys = new Vector();
		authorizedKeys.add(security.getPublicKeyString());
		assertTrue(template.importTemplate(security, exportFile, authorizedKeys));
		assertEquals(fields.toString(), template.getImportedText());
		assertEquals(0, template.getErrors().size());
		
		Vector unKnownKey = new Vector();
		unKnownKey.add("unknown");
		assertFalse(template.importTemplate(security, exportFile, unKnownKey));
		assertEquals(1, template.getErrors().size());
		assertEquals(CustomFieldError.CODE_UNAUTHORIZED_KEY, ((CustomFieldError)template.getErrors().get(0)).getCode());
		
		UnicodeWriter writer = new UnicodeWriter(exportFile,UnicodeWriter.APPEND);
		writer.write("unauthorizedTextAppended Should not be read.");
		writer.close();
		
		assertTrue(template.importTemplate(security, exportFile, authorizedKeys));
		assertEquals(fields.toString(), template.getImportedText());
		assertEquals(0, template.getErrors().size());

		exportFile.delete();
		FileOutputStream out = new FileOutputStream(exportFile);
		byte[] tamperedBundle = security.createSignedBundle(fields.toString().getBytes("UTF-8"));
		tamperedBundle[tamperedBundle.length-2] = 'j';
		out.write(tamperedBundle);
		out.flush();
		out.close();
		
		assertFalse(template.importTemplate(security, exportFile, authorizedKeys));
		assertEquals("", template.getImportedText());
		assertEquals(1, template.getErrors().size());
		assertEquals(CustomFieldError.CODE_SIGNATURE_ERROR, ((CustomFieldError)template.getErrors().get(0)).getCode());
		
		exportFile.delete();
		assertFalse(template.importTemplate(security, exportFile, authorizedKeys));
		assertEquals("", template.getImportedText());
		assertEquals(1, template.getErrors().size());
		assertEquals(CustomFieldError.CODE_IO_ERROR, ((CustomFieldError)template.getErrors().get(0)).getCode());
	}

	static MockMartusSecurity security;
	
}
