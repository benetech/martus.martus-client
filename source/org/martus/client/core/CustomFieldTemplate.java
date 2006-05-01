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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import org.martus.common.FieldCollection;
import org.martus.common.FieldCollection.CustomFieldsParseException;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.AuthorizationFailedException;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.fieldspec.CustomFieldError;
import org.martus.common.fieldspec.CustomFieldSpecValidator;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;

public class CustomFieldTemplate
{
	public CustomFieldTemplate()
	{
		super();
		clearData();
	}

	private void clearData()
	{
		errors = new Vector();
		xmlImportedText = "";
	}
	
	public boolean importTemplate(MartusCrypto security, File fileToImport, Vector authroizedKeys)
	{
		try
		{
			clearData();
			FileInputStream in = new FileInputStream(fileToImport);
			byte[] dataBundle = new byte[(int)fileToImport.length()];
			in.read(dataBundle);
			in.close();
			byte[] xmlBytes = security.extractFromSignedBundle(dataBundle, authroizedKeys);
			String templateXMLToImport = new String(xmlBytes, "UTF-8");
			//TODO: Use real private section
			FieldCollection defaultBottomFields = new FieldCollection(StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
			if(isvalidTemplateXml(templateXMLToImport, defaultBottomFields.toString()))
			{
				xmlImportedText = templateXMLToImport;
				return true;
			}
		}
		catch(IOException e)
		{
			errors.add(CustomFieldError.errorIO(e.getMessage()));
			
		}
		catch(MartusSignatureException e)
		{
			errors.add(CustomFieldError.errorSignature());
		}
		catch(AuthorizationFailedException e)
		{
			errors.add(CustomFieldError.errorUnauthorizedKey());
		}
		return false;
	}
	
	public boolean ExportTemplate(MartusCrypto security, File fileToExportXml, String xmlToExport)
	{
		clearData();
		//TODO: Export real private section
		FieldCollection defaultBottomFields = new FieldCollection(StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		if(!isvalidTemplateXml(xmlToExport, defaultBottomFields.toString()))
			return false;
		try
		{
			FileOutputStream out = new FileOutputStream(fileToExportXml);
			byte[] signedBundle = security.createSignedBundle(xmlToExport.getBytes("UTF-8"));
			out.write(signedBundle);
			out.flush();
			out.close();
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean isvalidTemplateXml(String xmlToValidateTopSection, String xmlToValidateBottomSection)
	{
		try
		{
			FieldSpec[] newSpecsTopSection = FieldCollection.parseXml(xmlToValidateTopSection);
			FieldSpec[] newSpecsBottomSection = FieldCollection.parseXml(xmlToValidateBottomSection);
			CustomFieldSpecValidator checker = new CustomFieldSpecValidator(newSpecsTopSection, newSpecsBottomSection);
			if(checker.isValid())
				return true;
			errors.addAll(checker.getAllErrors());
		}
		catch (CustomFieldsParseException e)
		{
			e.printStackTrace();
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
