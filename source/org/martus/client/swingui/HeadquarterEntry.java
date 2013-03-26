/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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

package org.martus.client.swingui;

import org.martus.common.HeadquartersKey;
import org.martus.util.StreamableBase64.InvalidBase64Exception;

public class HeadquarterEntry 
{
	public HeadquarterEntry(HeadquartersKey keyToUse)
	{
		key = keyToUse;
	}
	
	public boolean isSelected()
	{
		return isSelected;
	}

	public void setSelected(boolean selected)
	{
		isSelected = selected;
	}
	
	public String getLabel()
	{
		return key.getLabel();
	}
	
	public void setLabel(String newLabel)
	{
		key.setLabel(newLabel);
	}
	
	public String getPublicCode()
	{
		try
		{
			return key.getPublicCode();
		}
		catch(InvalidBase64Exception e)
		{
			e.printStackTrace();
			return "";
		}
	}

	public HeadquartersKey getKey()
	{
		return key;
	}
	
	public int hashCode()
	{
		return key.hashCode();
	}
	
	public boolean equals(Object rawOther)
	{
		if(! (rawOther instanceof HeadquarterEntry))
			return false;
		
		HeadquarterEntry other = (HeadquarterEntry)rawOther;
		return key.equals(other.key);
	}
	
	HeadquartersKey key;
	boolean isSelected;
}
