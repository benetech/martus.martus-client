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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.swing.ParagraphLayout;
import org.martus.swing.UiTextArea;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;


public class UiCustomFieldsDlg extends JDialog
{
	public UiCustomFieldsDlg(UiMainWindow owner, String xmlFieldSpecs)
	{
		super(owner, "", true);
		String baseTag = "CustomFields";
		UiLocalization localization = owner.getLocalization();
		setTitle(localization.getWindowTitle("input" + baseTag));

		UiWrappedTextArea label = new UiWrappedTextArea(localization.getFieldLabel("input" + baseTag + "entry"));
		text = new UiTextArea(15, 60);
		text.setText(xmlFieldSpecs);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		JScrollPane textPane = new JScrollPane(text, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
										JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		textPane.getVerticalScrollBar().setFocusable(false);

		JButton ok = new JButton(localization.getButtonLabel("input" + baseTag + "ok"));
		ok.addActionListener(new OkHandler());
		JButton cancel = new JButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(new CancelHandler());

		getContentPane().setLayout(new ParagraphLayout());

		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(label);
		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(text);
		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(ok);
		getContentPane().add(cancel);

		getRootPane().setDefaultButton(ok);

		Utilities.centerDlg(this);
		setResizable(false);
	}
	
	public void setFocusToInputField()
	{
		text.requestFocus();
	}

	class OkHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			result = text.getText();
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

	public String getResult()
	{
		return result;
	}

	JTextArea text;
	String result = null;

}
