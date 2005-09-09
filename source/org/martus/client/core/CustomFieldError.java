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
package org.martus.client.core;

import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeDropdown;


public class CustomFieldError
{
	static public CustomFieldError errorNoSpecs()
	{
		return new CustomFieldError(CODE_NULL_SPECS, UNUSED_FIELD, UNUSED_FIELD, UNUSED_FIELD);
	}
	
	static public CustomFieldError errorRequiredField(String fieldTagMissing)
	{
		return new CustomFieldError(CODE_REQUIRED_FIELD, fieldTagMissing, UNUSED_FIELD, UNUSED_FIELD);
	}
	
	static public CustomFieldError errorBlankTag(String label, String type)
	{
		return new CustomFieldError(CODE_MISSING_TAG, UNUSED_FIELD, label, type);
	}
	
	static public CustomFieldError errorIllegalTag(String tag, String label, String type)
	{
		return new CustomFieldError(CODE_ILLEGAL_TAG, tag, label, type);
	}
	
	
	static public CustomFieldError errorDuplicateFields(String tag, String label, String type)
	{
		return new CustomFieldError(CODE_DUPLICATE_FIELD, tag, label, type);
	}

	static public CustomFieldError errorMissingLabel(String tag, String type)
	{
		return new CustomFieldError(CODE_MISSING_LABEL, tag, UNUSED_FIELD, type);
	}
	
	static public CustomFieldError errorUnknownType(String tag, String label)
	{
		return new CustomFieldError(CODE_UNKNOWN_TYPE, tag, label, UNUSED_FIELD);
	}
	
	static public CustomFieldError errorLabelOnStandardField(String tag, String label, String type)
	{
		return new CustomFieldError(CODE_LABEL_STANDARD_FIELD, tag, label, type);
	}
	
	static public CustomFieldError errorDuplicateDropDownEntry(String tag, String label)
	{
		return new CustomFieldError(CODE_DUPLICATE_DROPDOWN_ENTRY, tag, label, FieldSpec.getTypeString(new FieldTypeDropdown()));
	}

	static public CustomFieldError noDropDownEntries(String tag, String label)
	{
		return new CustomFieldError(CODE_NO_DROPDOWN_ENTRIES, tag, label, FieldSpec.getTypeString(new FieldTypeDropdown()));
	}

	static public CustomFieldError errorParseXml()
	{
		return new CustomFieldError(CODE_PARSE_XML, UNUSED_FIELD, UNUSED_FIELD, UNUSED_FIELD);
	}
	
	static public CustomFieldError errorUnauthorizedKey()
	{
		return new CustomFieldError(CODE_UNAUTHORIZED_KEY, UNUSED_FIELD, UNUSED_FIELD, UNUSED_FIELD);
	}
	
	static public CustomFieldError errorSignature()
	{
		return new CustomFieldError(CODE_SIGNATURE_ERROR, UNUSED_FIELD, UNUSED_FIELD, UNUSED_FIELD);
	}

	static public CustomFieldError errorIO()
	{
		return new CustomFieldError(CODE_IO_ERROR, UNUSED_FIELD, UNUSED_FIELD, UNUSED_FIELD);
	}

	private CustomFieldError(String code, String tag, String label, String type)
	{
		this.code = code;
		this.tag = tag;
		this.label = label;
		this.type = type;
	}
	
	public String getCode()
	{
		return code;
	}
	public String getLabel()
	{
		return label;
	}
	public String getTag()
	{
		return tag;
	}
	public String getType()
	{
		return type;
	}

	static public final String CODE_REQUIRED_FIELD = "100";
	static public final String CODE_MISSING_TAG = "101";
	static public final String CODE_DUPLICATE_FIELD = "102";
	static public final String CODE_MISSING_LABEL = "103";
	static public final String CODE_UNKNOWN_TYPE = "104";
	static public final String CODE_LABEL_STANDARD_FIELD = "105";
	static public final String CODE_PARSE_XML = "106";
	static public final String CODE_ILLEGAL_TAG = "107";
	static public final String CODE_DUPLICATE_DROPDOWN_ENTRY = "108";
	static public final String CODE_NO_DROPDOWN_ENTRIES = "109";
	static public final String CODE_NULL_SPECS = "200";
	static public final String CODE_UNAUTHORIZED_KEY = "201";
	static public final String CODE_SIGNATURE_ERROR = "202";
	static public final String CODE_IO_ERROR = "203";
	
	static private final String UNUSED_FIELD = "";

	private String code;
	private String tag;
	private String label;
	private String type;
}
