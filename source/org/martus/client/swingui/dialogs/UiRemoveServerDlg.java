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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.martus.client.swingui.UiMainWindow;
import org.martus.common.ConfigInfo;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.swing.ParagraphLayout;
import org.martus.swing.Utilities;


public class UiRemoveServerDlg extends JDialog implements ActionListener
{
	public UiRemoveServerDlg(UiMainWindow owner,ConfigInfo info)
	{
		super(owner, "", true);
		UiBasicLocalization localization = owner.getLocalization();			
		
		setTitle(localization.getWindowTitle("RemoveServer"));
				
		String serverName = info.getServerName();		
		JTextField serverField = new JTextField(serverName);
		serverField.setEditable(false);		
		
		JLabel msgLabel1 = new JLabel(localization.getFieldLabel("RemoveServerLabel1"));		
		JLabel msgLabel2 = new JLabel(localization.getFieldLabel("RemoveServerLabel2"));
		
		String serverIPAddress = info.getServerName();
								
		JTextField serversField = new JTextField(serverIPAddress);
		serversField.setPreferredSize(new Dimension(10,20));
		serversField.setEditable(false);						

		yes = new JButton(localization.getButtonLabel("yes"));		
		yes.addActionListener(this);
		JButton no = new JButton(localization.getButtonLabel("no"));
		no.addActionListener(this);

		getContentPane().setLayout(new ParagraphLayout());	
		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);		
		getContentPane().add(msgLabel1);	
		getContentPane().add(serverField);
		getContentPane().add(msgLabel2, ParagraphLayout.NEW_LINE);			
	
		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);		
		getContentPane().add(yes);
		getContentPane().add(no);

		getRootPane().setDefaultButton(yes);

		Utilities.centerDlg(this);
		show();
	}	

	public void actionPerformed(ActionEvent ae)
	{	
		action=false;
		if(ae.getSource() == yes)
		{			
			action = true;
		}			
		dispose();
	}
		
	public boolean isYesButtonPressed()
	{
		return action;
	}
	
	boolean action;
	JButton yes;		
}
