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
package org.martus.client.core;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import org.martus.common.CustomFields;
import org.martus.common.FieldSpec;
import org.martus.common.CustomFields.CustomFieldsParseException;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;

public class CustomFieldTemplate
{
	public CustomFieldTemplate()
	{
		super();
		errors = null;
		xmlImportedText = "";
	}
	
	public boolean importTemplate(File fileToImport)
	{
		try
		{
			UnicodeReader reader = new UnicodeReader(fileToImport);
			String templateXMLToImport = reader.readAll();
			reader.close();
			if(validateXml(templateXMLToImport))
			{
				xmlImportedText = templateXMLToImport;
				return true;
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean ExportTemplate(File fileToExportXml, String xmlToExport)
	{
		if(!validateXml(xmlToExport))
			return false;
		
		try
		{
			UnicodeWriter writer = new UnicodeWriter(fileToExportXml);
			writer.write(xmlToExport);
			writer.close();
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean validateXml(String xmlToValidate)
	{
		errors = null;
		try
		{
			FieldSpec[] newSpecs = CustomFields.parseXml(xmlToValidate);
			CustomFieldSpecValidator checker = new CustomFieldSpecValidator(newSpecs);
			if(checker.isValid())
				return true;
			errors = checker.getAllErrors();
		}
		catch (CustomFieldsParseException e)
		{
			e.printStackTrace();
			errors = new Vector();
			errors.add(CustomFieldError.errorParseXml());
		}
		return false;
	}
	
	public Vector getErrors()
	{
		return errors;
	}
	
	public String getImportedText()
	{
		return xmlImportedText;
	}
	
	private Vector errors;
	private String xmlImportedText;
	
}
