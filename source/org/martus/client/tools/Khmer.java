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

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JDialog;
import javax.swing.JLabel;

public class Khmer
{
	public static void main(String[] args)
	{
		char[] khmerText = {0x1781, 0x17D2, 0x1789, 0x17BB, 0x17C6, 0x200B, 0x179F, 0x17D2, 0x179A, 0x17B6, 0x200B, 0x179C, 0x17B7, 0x1791, 0x17BC};
		JLabel khmerLabel = new JLabel(new String(khmerText));
		Font khmerFont = new Font("Khmer OS", Font.PLAIN, 30);
		khmerLabel.setFont(khmerFont);

		JDialog sample = new JDialog();
		sample.setTitle(new String(khmerText));
		sample.getContentPane().add(khmerLabel, BorderLayout.PAGE_START);
		sample.setSize(350,150);
		sample.setModal(true);
		sample.setVisible(true);
	}

}
