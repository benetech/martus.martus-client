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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.clientside.Localization;
import org.martus.swing.UiTextArea;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;


public class UiCustomFieldsDlg extends JDialog
{
	public UiCustomFieldsDlg(UiMainWindow owner, String xmlFieldSpecs)
	{
		super(owner, "", true);
		mainWindow = owner; 
		String baseTag = "CustomFields";
		UiLocalization localization = owner.getLocalization();
		setTitle(localization.getWindowTitle("input" + baseTag));

		UiWrappedTextArea label = new UiWrappedTextArea(localization.getFieldLabel("input" + baseTag + "Info"));
		text = new UiTextArea(20, 80);
		text.setText(xmlFieldSpecs);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		JScrollPane textPane = new JScrollPane(text, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
										JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		textPane.getVerticalScrollBar().setFocusable(false);

		JButton ok = new JButton(localization.getButtonLabel("input" + baseTag + "ok"));
		ok.addActionListener(new OkHandler());
		JButton cancel = new JButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(new CancelHandler());
		JButton defaults = new JButton(localization.getButtonLabel("customDefault"));
		defaults.addActionListener(new CustomDefaultHandler());
		JButton help = new JButton(localization.getButtonLabel("customHelp"));
		help.addActionListener(new CustomHelpHandler());

		Box buttons = Box.createHorizontalBox();
		Dimension preferredSize = textPane.getPreferredSize();
		preferredSize.height = ok.getPreferredSize().height;				
		buttons.setPreferredSize(preferredSize);	
		buttons.add(defaults);

		buttons.add(Box.createHorizontalGlue());
		buttons.add(ok);				
		buttons.add(cancel);
		buttons.add(help);

		JPanel customFieldsPanel = new JPanel();
		customFieldsPanel.setBorder(new EmptyBorder(10,10,10,10));
		customFieldsPanel.setLayout(new BoxLayout(customFieldsPanel, BoxLayout.Y_AXIS));
		customFieldsPanel.add(label);
		customFieldsPanel.add(new JLabel(" "));
		customFieldsPanel.add(textPane);
		customFieldsPanel.add(new JLabel(" "));
		customFieldsPanel.add(buttons);
	
		getContentPane().add(customFieldsPanel);
		getRootPane().setDefaultButton(ok);
		Utilities.centerDlg(this);
		setResizable(true);
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
	
	
	class CustomDefaultHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			result = "";
			dispose();
		}
	}
	
	class CustomHelpHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			UiLocalization localization = mainWindow.getLocalization();
			String title = localization.getWindowTitle("CreateCustomFieldsHelp");
			String message = localization.getFieldLabel("CreateCustomFieldsHelp1");
			message += localization.getFieldLabel("CreateCustomFieldsHelp2");
			message += localization.getFieldLabel("CreateCustomFieldsHelp3");

			new UiShowScrollableTextDlg(mainWindow, title, "ok", Localization.UNUSED_TAG, Localization.UNUSED_TAG, message);
		}
	}

	public String getResult()
	{
		return result;
	}

	JTextArea text;
	String result = null;
	UiMainWindow mainWindow;
}
