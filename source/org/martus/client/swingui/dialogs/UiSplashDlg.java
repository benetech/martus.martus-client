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

package org.martus.client.swingui.dialogs;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.martus.client.swingui.UiConstants;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.swing.Utilities;

public class UiSplashDlg extends JDialog implements ActionListener
{
	public UiSplashDlg(Frame owner, UiBasicLocalization localization, String text)
	{
		super(owner, owner.getTitle(), true);
		Container contents = getContentPane();
		
		JLabel body = new JLabel(text);
		body.setBorder(new EmptyBorder(20, 40, 20, 20));
		String versionInfo = UiMainWindow.getDisplayVersionInfo(localization);
		String copyrightInfo = UiConstants.copyright;
		String websiteInfo = UiConstants.website;
		String htmlBreak = "<BR></BR>";
		String htmlVersionInfo = "<html><center>" + versionInfo + htmlBreak + 
				copyrightInfo +htmlBreak+ websiteInfo + htmlBreak + "</center></html>";
		
		contents.add(body,BorderLayout.NORTH);
		contents.add(new JLabel(htmlVersionInfo),BorderLayout.CENTER);
		
		contents.add(new JLabel("   "), BorderLayout.EAST);
		contents.add(new JLabel("   "), BorderLayout.WEST);
		Box hbox = Box.createHorizontalBox();
		hbox.add(Box.createHorizontalGlue());
		JButton ok = new JButton(localization.getButtonLabel("ok"));
		ok.addActionListener(this);
		hbox.add(ok);
		hbox.add(Box.createHorizontalGlue());
		hbox.setBorder(new EmptyBorder(5, 5, 5, 5));
		contents.add(hbox, BorderLayout.SOUTH);
		Utilities.centerDlg(this);
		show();
	}

	public void actionPerformed(ActionEvent ae)
	{
		dispose();
	}

}