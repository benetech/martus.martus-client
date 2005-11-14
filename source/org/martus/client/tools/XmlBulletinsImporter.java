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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Vector;
import javax.xml.parsers.ParserConfigurationException;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.util.UnicodeReader;
import org.xml.sax.SAXException;

public class XmlBulletinsImporter
{
	public XmlBulletinsImporter(MartusCrypto security, InputStream xmlIn) throws IOException, ParserConfigurationException, SAXException, FieldSpecVerificationException
	{
		UnicodeReader reader = new UnicodeReader(xmlIn);
		try
		{
			bulletinsLoader = new XmlBulletinsFileLoader(security);
			bulletinsLoader.parse(reader);
			if(bulletinsLoader.didFieldSpecVerificationErrorOccur())
				throw new FieldSpecVerificationException(bulletinsLoader.getErrors());
		}
		finally
		{
			reader.close();
		}
	}
	
	public class FieldSpecVerificationException extends Exception
	{
		public FieldSpecVerificationException(Vector errors)
		{
			verificationErrors = errors;
		}
		public Vector getErrors()
		{
			return verificationErrors;
		}
		Vector verificationErrors;
	}
	
	public Bulletin[] getBulletins()
	{
		return bulletinsLoader.getBulletins();
	}

	//These are currently used in tests, I think its good to keep them for now
	public FieldSpec[] getMainFieldSpecs()
	{
		return bulletinsLoader.mainFields.getSpecs();
	}

	public FieldSpec[] getPrivateFieldSpecs()
	{
		return bulletinsLoader.privateFields.getSpecs();
	}
	
	public HashMap getFieldTagValuesMap()
	{
		return bulletinsLoader.fieldTagValuesMap;
	}

	XmlBulletinsFileLoader bulletinsLoader;
}
