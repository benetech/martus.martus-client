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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.martus.client.swingui.UiMainWindow;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.swing.ParagraphLayout;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;


public class UiPrintBulletinDlg extends JDialog implements ActionListener
{
	public UiPrintBulletinDlg(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse, "", true);
		mainWindow = mainWindowToUse;	
		init();	
	}
	
	private void init()
	{
		UiBasicLocalization localization = mainWindow.getLocalization();
		setTitle(localization.getWindowTitle("PrintPrivateData"));
		
		includePrivate = new JCheckBox(localization.getFieldLabel("PrintPrivateData"));
		ok = new JButton(localization.getButtonLabel("Continue"));
		ok.addActionListener(this);		
		cancel = new JButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(this);		
		
		Box hBoxButtons = Box.createHorizontalBox();		
		hBoxButtons.add(Box.createHorizontalGlue());	
		hBoxButtons.add(ok);
		hBoxButtons.add(cancel);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10,10,10,10));
		panel.setLayout(new ParagraphLayout());
		panel.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		panel.add(new UiWrappedTextArea(localization.getFieldLabel("PrintPrivateDataMessage")));
		panel.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		panel.add(includePrivate);
		panel.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		panel.add(hBoxButtons);
	
		getContentPane().add(panel);
		getRootPane().setDefaultButton(ok);
		Utilities.centerDlg(this);
		setResizable(true);
	}
	
	public boolean isIncludePrivateChecked()
	{
		return includePrivate.isSelected();
	}
	
	public boolean isContinueButtonPressed()
	{
		return pressContinue;
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource().equals(ok))
			pressContinue = true;
			
		dispose();
	}
	
	
	UiMainWindow mainWindow;	
	JCheckBox includePrivate;
	JButton ok;
	JButton cancel;
	boolean pressContinue=false;
	
}
