/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

import com.inet.jortho.UserDictionaryProvider;

public class MartusUserDictionary implements UserDictionaryProvider, Iterator<String>
{
	public MartusUserDictionary(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
		
		original = new HashSet<String>().iterator();
		
		HashSet<String> extraWords = new HashSet<String>();
		extraWords.add("Martus");
		extraWords.add("Miradi");
		extraWords.add("Benetech");
		extras = extraWords.iterator();
	}
	
	@Override
	public Iterator<String> getWords(Locale locale)
	{
		return this;
	}
	
	public boolean hasNext()
	{
		boolean result = original.hasNext();
		if(result)
			return result;
		
		return extras.hasNext();
	}

	public String next()
	{
		if(original.hasNext())
			return original.next();

		return extras.next();
	}

	public void remove()
	{
		original.remove();
	}
	

	@Override
	public void addWord(String newWord)
	{
		// FIXME: Needs to be implemented
		throw new RuntimeException("Not Implemented Yet!");
	}

	@Override
	public void setUserWords(String words)
	{
		// FIXME: Needs to be implemented
		throw new RuntimeException("Not Implemented Yet!");
	}
	
	private UiMainWindow mainWindow;
	private Iterator<String> original;
	private Iterator<String> extras;
}
