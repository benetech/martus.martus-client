/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2012, Beneficent
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
package org.martus.client.reports;

import java.awt.Font;

import org.jfree.chart.StandardChartTheme;

public class MartusChartTheme extends StandardChartTheme
{
	public MartusChartTheme()
	{
		super("Martus");
		
		// NOTE: JFreeChart default is "Tahoma", which somehow maps to Dialog,
		// which for some reason (in J6) doesn't fall back to the fallback fonts, 
		// and therefore doesn't work in Armenian, Bangla, Khmer, or Nepali 
        setExtraLargeFont(new Font("SansSerif", Font.BOLD, 20));
        setLargeFont(new Font("SansSerif", Font.BOLD, 14));
        setRegularFont(new Font("SansSerif", Font.PLAIN, 12));
        setSmallFont(new Font("SansSerif", Font.PLAIN, 10));
	}

}
