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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.martus.client.core.DateUtilities;
import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiChoiceEditor;
import org.martus.swing.ParagraphLayout;
import org.martus.swing.Utilities;



public class UiLocalizeDlg extends JDialog implements ActionListener, ChangeListener
{
	public UiLocalizeDlg(UiMainWindow mainWindow)
	{
		super(mainWindow, "", true);
		owner = mainWindow;
		UiLocalization localization = owner.getLocalization();

		setTitle(localization.getMenuLabel("Preferences"));

		dateFormatDropdown = new UiChoiceEditor(DateUtilities.getDateFormats());
		dateFormatDropdown.setText(localization.getCurrentDateFormatCode());

		languageDropdown = new UiChoiceEditor(localization.getUiLanguages());
		languageDropdown.setText(localization.getCurrentLanguageCode());

		ok = new JButton(localization.getButtonLabel("ok"));
		ok.addActionListener(this);
		cancel = new JButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(this);

		getContentPane().setLayout(new ParagraphLayout());

		getContentPane().add(new JLabel(localization.getFieldLabel("language")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(languageDropdown.getComponent());

		getContentPane().add(new JLabel(localization.getFieldLabel("dateformat")), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(dateFormatDropdown.getComponent());

		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		getContentPane().add(ok);
		getContentPane().add(cancel);

		getRootPane().setDefaultButton(ok);

		Utilities.centerDlg(this);
		setResizable(true);
		show();
	}

	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource() == ok)
		{
			owner.getLocalization().setCurrentDateFormatCode(dateFormatDropdown.getText());
			owner.getLocalization().setCurrentLanguageCode(languageDropdown.getText());
		}
		dispose();
	}

	// ChangeListener interface
	public void stateChanged(ChangeEvent event) {}

	private UiMainWindow owner;
	private UiChoiceEditor languageDropdown;
	private UiChoiceEditor dateFormatDropdown;
	private JButton ok;
	private JButton cancel;
}
