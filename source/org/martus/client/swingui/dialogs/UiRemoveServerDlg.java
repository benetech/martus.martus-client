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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.martus.client.core.ConfigInfo;
import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.swing.ParagraphLayout;
import org.martus.swing.Utilities;


public class UiRemoveServerDlg extends JDialog
{
	public UiRemoveServerDlg(UiMainWindow owner,ConfigInfo info)
		{
			super(owner, "", true);
			UiLocalization localization = owner.getLocalization();			
			
			setTitle(localization.getWindowTitle("RemoveServer"));		
			String selServer = localization.getFieldLabel("SelectServer");		
			JLabel selServerLabel = new JLabel(selServer);
			String[] serverIPAddress = {info.getServerName()};
									
			JComboBox serversCombo 	= new JComboBox(serverIPAddress);
			serversCombo.setEditable(false);						

			JButton ok = new JButton(localization.getButtonLabel("ok"));
			ok.addActionListener(new OkHandler());
			JButton cancel = new JButton(localization.getButtonLabel("cancel"));
			cancel.addActionListener(new CancelHandler());

			getContentPane().setLayout(new ParagraphLayout());	
			getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);		
			getContentPane().add(selServerLabel);	
			getContentPane().add(serversCombo);			
		
			getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);		
			getContentPane().add(ok);
			getContentPane().add(cancel);

			getRootPane().setDefaultButton(ok);

			Utilities.centerDlg(this);
			setResizable(false);
		}	
	
		class OkHandler implements ActionListener
		{
			public void actionPerformed(ActionEvent ae)
			{			
				dispose();
			}
		}

		class CancelHandler implements ActionListener
		{
			public void actionPerformed(ActionEvent ae)
			{				
				dispose();
			}
		}	
	
}
