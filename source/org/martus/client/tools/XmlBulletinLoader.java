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
package org.martus.client.tools;

import java.util.HashMap;
import org.martus.common.FieldCollection;
import org.martus.common.GridData;
import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.util.xml.SimpleXmlDefaultLoader;
import org.martus.util.xml.SimpleXmlStringLoader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

public class XmlBulletinLoader extends SimpleXmlDefaultLoader
{
	
	public XmlBulletinLoader()
	{
		super(MartusBulletinElementName);
	}
	
	public SimpleXmlDefaultLoader startElement(String tag)
		throws SAXParseException
	{
		if(tag.equals(MainFieldSpecsElementName))
			return new FieldCollection.XmlCustomFieldsLoader(tag, new FieldCollection());
		else if(tag.equals(PrivateFieldSpecsElementName))
			return new FieldCollection.XmlCustomFieldsLoader(tag, new FieldCollection());
		else if(tag.equals(FieldValuesElementName))
			return new FieldValuesSectionLoader(tag);
		return super.startElement(tag);
	}

	public void endElement(String tag, SimpleXmlDefaultLoader ended)
		throws SAXParseException
	{
		if(tag.equals(MainFieldSpecsElementName))
		{
			mainFieldSpecs = ((FieldCollection.XmlCustomFieldsLoader)ended).getFields();
		}
		else if(tag.equals(PrivateFieldSpecsElementName))
		{
			privateFieldSpecs = ((FieldCollection.XmlCustomFieldsLoader)ended).getFields();
		}
		else if(tag.equals(FieldValuesElementName))
		{
			fieldTagValuesMap = ((FieldValuesSectionLoader)ended).getFieldTagValueMap();
		}
		else
			super.endElement(tag, ended);
	}
	
	public HashMap getFieldTagValuesMap()
	{
		return fieldTagValuesMap;
	}
	
	public FieldCollection getMainFieldSpecs()
	{
		return mainFieldSpecs;
	}
	
	public FieldCollection getPrivateFieldSpecs()
	{
		return privateFieldSpecs;
	}
	
	class FieldValuesSectionLoader extends SimpleXmlDefaultLoader
	{
		public FieldValuesSectionLoader(String tag)
		{
			super(tag);
			fieldTagToValueMap = new HashMap();
		}
		
		public SimpleXmlDefaultLoader startElement(String tag)throws SAXParseException
		{
			if(tag.equals(FieldElementName))
				return new FieldLoader(tag);
			return super.startElement(tag);
		}
	
		public void endElement(String tag, SimpleXmlDefaultLoader ended)throws SAXParseException
		{
			if(tag.equals(FieldElementName))
			{
				FieldLoader fieldLoader = ((FieldLoader)ended);
				String fieldTag = fieldLoader.getFieldTag();
				String fieldValue = fieldLoader.getValue();
				fieldTagToValueMap.put(fieldTag, fieldValue);
			}
			else
				super.endElement(tag, ended);
		}

		public HashMap getFieldTagValueMap()
		{
			return fieldTagToValueMap;
		}
		HashMap fieldTagToValueMap;
	}
	
	class FieldLoader extends SimpleXmlDefaultLoader
	{

		public FieldLoader(String tag)
		{
			super(tag);
		}
		
		public String getValue()
		{
			if(valueLoader != null)
				return valueLoader.getText();
			if(isMessageField(tagForField))
				return getMessageValue(tagForField);
			return null;
		}
		
		public String getFieldTag()
		{
			return tagForField;
		}
		
		public void startDocument(Attributes attrs) throws SAXParseException
		{
			tagForField = attrs.getValue(TagAttributeName);
			super.startDocument(attrs);
		}
		
		public SimpleXmlDefaultLoader startElement(String tag) throws SAXParseException
		{
			if(tag.equals(ValueElementName))
			{
				valueLoader = new ValueLoader(tagForField);
				return valueLoader;
			}
			return super.startElement(tag);
		}

		public void endElement(String tag, SimpleXmlDefaultLoader ended) throws SAXParseException
		{
			super.endElement(tag, ended);
		}
		ValueLoader valueLoader;
		String tagForField;
		String value;
	}
	
	class ValueLoader extends SimpleXmlStringLoader
	{

		public ValueLoader(String currentFieldTagToUse)
		{
			super(ValueElementName);
			tagForField = currentFieldTagToUse;
		}
		
		public String getText()
		{
			if(complexData != null)
				return complexData;
			return super.getText();
		}

		public SimpleXmlDefaultLoader startElement(String tag) throws SAXParseException
		{
			if(tag.equals(GridData.GRID_DATA_TAG))
			{
				GridData gridData = new GridData(getGridFieldSpec(tagForField));
				return new GridData.XmlGridDataLoader(gridData);
			}
			return super.startElement(tag);
		}

		public void endElement(String tag, SimpleXmlDefaultLoader ended) throws SAXParseException
		{
			if(tag.equals(GridData.GRID_DATA_TAG))
				complexData = ((GridData.XmlGridDataLoader)ended).getGridData().getXmlRepresentation(); 
			super.endElement(tag, ended);
		}

		ValueLoader valueLoader;
		String tagForField;
		String complexData;
	}
	
	boolean isMessageField(String tag)
	{
		return getFieldFromSpecs(tag).getType().isMessage();
	}
	
	GridFieldSpec getGridFieldSpec(String tag)
	{
		return (GridFieldSpec)getFieldFromSpecs(tag).getFieldSpec();
	}

	String getMessageValue(String messageTag)
	{
		return getFieldFromSpecs(messageTag).getData();
	}
	
	MartusField getFieldFromSpecs(String tag)
	{
		MartusField field = mainFieldSpecs.findByTag(tag);
		if(field != null)
			return field;
		return privateFieldSpecs.findByTag(tag);
		
	}

	private FieldCollection mainFieldSpecs;
	private FieldCollection privateFieldSpecs;
	private HashMap fieldTagValuesMap;

	public static final String MartusBulletinElementName = "MartusBulletin";
	public static final String MainFieldSpecsElementName = "MainFieldSpecs";
	public static final String PrivateFieldSpecsElementName = "PrivateFieldSpecs";
	public static final String FieldValuesElementName = "FieldValues";
	public static final String FieldElementName = "Field";
	public static final String ValueElementName = "Value";
	public static final String TagAttributeName = "tag";
	
}
