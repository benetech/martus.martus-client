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

import java.util.Vector;

import org.martus.common.MartusXml;
import org.martus.util.xml.SimpleXmlDefaultLoader;
import org.martus.util.xml.SimpleXmlMapLoader;
import org.martus.util.xml.SimpleXmlParser;
import org.xml.sax.SAXParseException;


public class HQKeys
{
	public HQKeys(Vector keysToUse)
	{
		hqKeys = keysToUse;
	}
	
	public String toString()
	{
		String xmlRepresentation = MartusXml.getTagStart(HQ_KEYS_TAG);
		for(int i = 0; i < hqKeys.size(); ++i)
		{
			xmlRepresentation += MartusXml.getTagStart(HQ_KEY_TAG);
			xmlRepresentation += MartusXml.getTagStart(HQ_PUBLIC_KEY_TAG);
			xmlRepresentation += (String)hqKeys.get(i);
			xmlRepresentation += MartusXml.getTagEndWithoutNewline(HQ_PUBLIC_KEY_TAG);
			xmlRepresentation += MartusXml.getTagEnd(HQ_KEY_TAG);
		}
		xmlRepresentation += MartusXml.getTagEnd(HQ_KEYS_TAG);
		
		return xmlRepresentation;
	}
	
	public static class HQsException extends Exception {}

	public static Vector parseXml(String xml) throws HQsException
	{
		Vector hQs = new Vector();
		if(xml.length() == 0)
			return hQs;
		XmlHQsLoader loader = new XmlHQsLoader(hQs);
		try
		{
			SimpleXmlParser.parse(loader, xml);
			return hQs;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new HQsException();
		}
	}
	
	public static class XmlHQsLoader extends SimpleXmlDefaultLoader
	{
		public XmlHQsLoader(Vector hqKeys)
		{
			super(HQ_KEYS_TAG);
			keys = hqKeys;
		}
		
		public SimpleXmlDefaultLoader startElement(String tag)
			throws SAXParseException
		{
			if(tag.equals(HQ_KEY_TAG))
				return new SimpleXmlMapLoader(tag);
			return super.startElement(tag);
		}

		public void endElement(String tag, SimpleXmlDefaultLoader ended)
			throws SAXParseException
		{
			SimpleXmlMapLoader loader = (SimpleXmlMapLoader)ended;
			keys.add(loader.get(HQ_PUBLIC_KEY_TAG));
		}

		Vector keys;
	}
	
	
	
	public static final String HQ_KEYS_TAG = "HQs";
	public static final String HQ_KEY_TAG = "HQ";
	public static final String HQ_PUBLIC_KEY_TAG = "PublicKey";
	Vector hqKeys;
}
