/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2003, Beneficent
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

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.swing.UiWrappedTextArea;


public class UiInitialSigninDlg extends UiSigninDlg
{
	public UiInitialSigninDlg(UiMainWindow window, JFrame owner)
	{
		super(window, owner, INITIAL);
	}

	JPanel createMainPanel(UiLocalization localization)
	{
		JPanel scrolledPanel = new JPanel(); 
		tabbedPane = new JTabbedPane();
		tabbedPane.add(signinPane);
		tabbedPane.setTitleAt(0, localization.getButtonLabel("SignIn"));  
		tabbedPane.add(createNewAccountPane());
		tabbedPane.setTitleAt(1, localization.getButtonLabel("NewAccount"));
		scrolledPanel.add(tabbedPane);
		return scrolledPanel;
	}

	JComponent createNewAccountPane()
	{
		String text = mainWindow.getLocalization().getFieldLabel("HowToCreateNewAccount");
		return new UiWrappedTextArea("\n" + text);
	}
}
