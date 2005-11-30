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

package org.martus.client.swingui;

public class FontHandler
{

	// NOTE: The following is just a quick hack for Kurdish testing,
	// but is harmless and powerful so it's worth keeping 
	// until we decide what to do in the long run. 2005-11-30 kbs
	public static final int defaultFontSize = getAsInteger(System.getProperty("inputfontsize"), 13);
	public static final String defaultFontName = getAsString(System.getProperty("inputfontname"), "SansSerif");

	public static String getAsString(String candidate, String defaultValue)
	{
		if(candidate != null && candidate.length() > 0)
			return candidate;
		
		return defaultValue;
	}

	public static int getAsInteger(String candidate, int defaultValue)
	{
		try
		{
			return Integer.parseInt(candidate);
		}
		catch (NumberFormatException e)
		{
			return defaultValue;
		}
	}
	// NOTE: End of Kurdish testing hack

}
