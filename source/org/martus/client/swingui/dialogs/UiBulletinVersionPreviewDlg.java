/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2005, Beneficent
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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JViewport;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.bulletincomponent.UiBulletinVersionView;
import org.martus.common.bulletin.Bulletin;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.Utilities;

public class UiBulletinVersionPreviewDlg extends JDialog implements ActionListener
{

	public UiBulletinVersionPreviewDlg(UiMainWindow owner, Bulletin b)
	{
		super(owner, owner.getLocalization().getWindowTitle("BulletinPreview"), true);	
		getContentPane().setLayout(new BorderLayout());
		UiBulletinVersionView view = new UiBulletinVersionView(owner);
		try
		{
			view.copyDataFromBulletin(b);
			view.updateEncryptedIndicator(b.isAllPrivate());
		}
		catch(IOException e)
		{
			System.out.println("UiBulletinVersionPreviewDlg: " + e);
			dispose();
			return;
		}

		UiScrollPane scrollPane = new UiScrollPane();
		scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		scrollPane.getViewport().add(view);		

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new GridLayout(1,7));
		JButton ok = new UiButton(owner.getLocalization().getButtonLabel("ok"));
		ok.addActionListener(this);
		Dimension okSize = ok.getPreferredSize();
		okSize.width += 40;
		ok.setPreferredSize(okSize);
		buttonPane.add(new UiLabel(" "));
		buttonPane.add(new UiLabel(" "));
		buttonPane.add(new UiLabel(" "));
		buttonPane.add(ok);
		buttonPane.add(new UiLabel(" "));
		buttonPane.add(new UiLabel(" "));
		buttonPane.add(new UiLabel(" "));

		getContentPane().add(scrollPane, BorderLayout.CENTER);
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		Utilities.centerDlg(this);
		setResizable(true);		
		ok.requestFocus();
		getRootPane().setDefaultButton(ok);
		show();
	}

	public void actionPerformed(ActionEvent ae)
	{
		dispose();
	}

}
