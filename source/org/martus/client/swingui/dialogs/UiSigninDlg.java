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

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.martus.client.swingui.fields.UiChoiceEditor;
import org.martus.common.clientside.CurrentUiState;
import org.martus.common.clientside.UiBasicSigninDlg;
import org.martus.common.clientside.UiBasicLocalization;



public class UiSigninDlg extends UiBasicSigninDlg
{
	public UiSigninDlg(UiBasicLocalization localizationToUse, CurrentUiState uiStateToUse , JFrame owner, int mode)
	{
		this(localizationToUse, uiStateToUse, owner, mode, "", new char[0]);
	}

	public UiSigninDlg(UiBasicLocalization localizationToUse, CurrentUiState uiStateToUse, JFrame owner, int mode, String username, char[] password)
	{
		super(localizationToUse, uiStateToUse, owner, mode, username, password);
	}

	protected JComponent getLanguageComponent()
	{
		languageDropdown = new UiChoiceEditor(localization.getUiLanguages());
		languageDropdown.setText(localization.getCurrentLanguageCode());
		languageDropdown.addActionListener(new LanguageListener());
		JComponent languageComponent = languageDropdown.getComponent();
		return languageComponent;
	}

	class LanguageListener implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			String languageCode = languageDropdown.getText();
			localization.setCurrentLanguageCode(languageCode);
			uiState.setCurrentLanguage(languageCode);
			uiState.save();
			usersChoice = LANGUAGE_CHANGED;
			dispose();
		}
	}

	UiChoiceEditor languageDropdown;
}

