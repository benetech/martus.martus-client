/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006, Beneficent
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

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import org.martus.client.swingui.UiMainWindow;
import org.martus.swing.UiButton;
import org.martus.swing.UiVBox;
import org.martus.swing.Utilities;

public class UiRunOrCreateReportDlg extends JDialog implements ActionListener
{
	public UiRunOrCreateReportDlg(UiMainWindow mainWindow, String[] buttonLabels)
	{
		super(mainWindow);
		setModal(true);
		setTitle(mainWindow.getLocalization().getWindowTitle("RunOrCreateReport"));
		Container panel = getContentPane();
		UiVBox buttonBox = new UiVBox();
		for(int i = 0; i < buttonLabels.length; ++i)
		{
			UiButton button = new UiButton(buttonLabels[i]);
			button.addActionListener(this);
			buttonBox.add(button);
		}
		
		panel.add(buttonBox);
		pack();
		Utilities.centerDlg(this);
	}
	
	public String getPressedButtonLabel()
	{
		return pressedButtonLabel;
	}

	public void actionPerformed(ActionEvent event)
	{
		UiButton button = (UiButton)event.getSource();
		pressedButtonLabel = button.getText();
		dispose();
	}
	
	String pressedButtonLabel;
}
