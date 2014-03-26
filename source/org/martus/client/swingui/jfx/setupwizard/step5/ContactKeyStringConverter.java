/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
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
package org.martus.client.swingui.jfx.setupwizard.step5;

import javafx.util.StringConverter;

import org.martus.common.ContactKey;
import org.martus.common.MartusLogger;
import org.martus.util.StreamableBase64.InvalidBase64Exception;

public class ContactKeyStringConverter extends StringConverter<ContactKey>
{
	@Override
	public String toString(ContactKey contactKey)
	{
		if (contactKey == null)
			return "";
		
		String label = contactKey.getLabel();
		if (label.length() > 0)
			return label;
					
		try
		{
			return contactKey.getFormattedPublicCode40();
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			return "[Error]";
		}
	}

	@Override
	public ContactKey fromString(String string)
	{
		return null;
	}
}