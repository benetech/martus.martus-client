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
import javax.swing.JCheckBox;
import javax.swing.JDialog;

import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiChoiceEditor;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.common.utilities.DateUtilities;
import org.martus.swing.UiButton;
import org.martus.swing.UiCheckBox;
import org.martus.swing.UiLabel;
import org.martus.swing.UiParagraphPanel;
import org.martus.swing.Utilities;



public class UiPreferencesDlg extends JDialog implements ActionListener
{
	public UiPreferencesDlg(UiMainWindow mainWindow)
	{
		super(mainWindow, "", true);
		owner = mainWindow;
		UiBasicLocalization localization = owner.getLocalization();
		
		setTitle(localization.getMenuLabel("Preferences"));
		
		dateFormatDropdown = new UiChoiceEditor(DateUtilities.getDateFormats());
		dateFormatDropdown.setText(localization.getCurrentDateFormatCode());
		
		languageDropdown = new UiChoiceEditor(localization.getUiLanguages());
		languageDropdown.setText(localization.getCurrentLanguageCode());
		
		allPrivate = new UiCheckBox();
		allPrivate.setText(localization.getFieldLabel("preferencesAllPrivate"));
		allPrivate.setSelected(owner.getBulletinsAlwaysPrivate());
		
		UiParagraphPanel preferences = new UiParagraphPanel();
		preferences.addComponents(new UiLabel(localization.getFieldLabel("language")), languageDropdown.getComponent());
		preferences.addComponents(new UiLabel(localization.getFieldLabel("dateformat")), dateFormatDropdown.getComponent());
		
		preferences.addBlankLine();
		preferences.addOnNewLine(allPrivate);
		preferences.addBlankLine();
		
		ok = new UiButton(localization.getButtonLabel("ok"));
		ok.addActionListener(this);
		cancel = new UiButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(this);
		preferences.addComponents(ok, cancel);
		
		getContentPane().add(preferences);
		getRootPane().setDefaultButton(ok);
		
		Utilities.centerDlg(this);
		setResizable(true);
		show();
	}

	public void actionPerformed(ActionEvent ae)
	{
		
		if(ae.getSource() == ok)
		{
			UiLocalization localization = owner.getLocalization();
			String languageCodeSelected = languageDropdown.getText();
			if(!localization.isOfficialTranslation(languageCodeSelected))
				UiMainWindow.displayDefaultUnofficialTranslationMessage(owner);
			localization.setCurrentDateFormatCode(dateFormatDropdown.getText());
			localization.setCurrentLanguageCode(languageDropdown.getText());
			owner.setBulletinsAlwaysPrivate(allPrivate.isSelected());
		}
		dispose();
	}

	UiMainWindow owner;
	UiChoiceEditor languageDropdown;
	private UiChoiceEditor dateFormatDropdown;
	private JCheckBox allPrivate;
	private JButton ok;
	private JButton cancel;
}
