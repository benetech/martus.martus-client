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

import javax.swing.JDialog;
import javax.swing.JLabel;

public class TextWrappingSample
{

	public static void main(String[] args)
	{
		String text = "This is some text that has a very long paragraph. " +
				"I mean it goes on forever and ever. " +
				"If it doesn't get wrapped and/or isn't scrollable, " +
				"it will be very difficult for users to use it. ";
		
		JLabel label = new JLabel("<html>" + text + "</html>");
		
		JDialog sample = new JDialog();
		sample.setTitle("Text Wrapping Sample");
		sample.getContentPane().add(label);
		sample.pack();
		sample.setModal(true);
		sample.setVisible(true);
	}

}
