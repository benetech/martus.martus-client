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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JViewport;

import org.martus.client.core.LanguageChangeListener;
import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.bulletincomponent.UiBulletinComponentViewSection;
import org.martus.common.FieldSpec;
import org.martus.common.StandardFieldSpecs;
import org.martus.common.packet.FieldDataPacket;
import org.martus.swing.UiButton;
import org.martus.swing.UiScrollPane;
import org.martus.swing.Utilities;

public class UiBulletinPreviewDlg extends JDialog implements ActionListener, LanguageChangeListener
{

	public UiBulletinPreviewDlg(UiMainWindow owner, FieldDataPacket fdp)
	{
		super(owner, owner.getLocalization().getWindowTitle("BulletinPreview"), true);	
		getContentPane().setLayout(new BorderLayout());

		UiBulletinComponentViewSection view = new UiBulletinComponentViewSection(owner);
		FieldSpec[] standardFieldTags = StandardFieldSpecs.getDefaultPublicFieldSpecs();

		UiLocalization localization = owner.getLocalization();
		
		view.createLabelsAndFields(standardFieldTags, this);
		view.copyDataFromPacket(fdp);
		view.attachmentViewer.saveButton.setVisible(false);
		view.attachmentViewer.viewButton.setVisible(false);

		view.updateEncryptedIndicator(fdp.isEncrypted());		
		UiScrollPane scrollPane = new UiScrollPane();
		scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		scrollPane.getViewport().add(view);		

		JPanel buttonPane = new JPanel();		
		JButton ok = new UiButton(localization.getButtonLabel("ok"));
		ok.addActionListener(this);
		Dimension okSize = ok.getPreferredSize();
		okSize.width += 40;
		ok.setPreferredSize(okSize);
		buttonPane.add(ok);

		getContentPane().add(scrollPane, BorderLayout.CENTER);
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		
		getRootPane().setDefaultButton(ok);
		Utilities.centerDlg(this);
		setResizable(true);		
		show();
	}

	public void actionPerformed(ActionEvent ae)
	{
		dispose();
	}

	public void languageChanged(String newLanguageCode) 
	{
		//read-only nothing to do
	}

}
