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

import javax.swing.JFrame;

import org.martus.client.core.LanguageChangeListener;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.clientside.CurrentUiState;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.common.clientside.UiBasicSigninDlg;

public class UiSigninDlg extends UiBasicSigninDlg implements LanguageChangeListener
{
	public UiSigninDlg(UiBasicLocalization localizationToUse, CurrentUiState uiStateToUse , JFrame owner, int mode)
	{
		this(localizationToUse, uiStateToUse, owner, mode, "", new char[0]);
	}

	public UiSigninDlg(UiBasicLocalization localizationToUse, CurrentUiState uiStateToUse, JFrame owner, int mode, String username, char[] password)
	{
		super(localizationToUse, uiStateToUse, owner, mode, username, password);
	}

	// LanguageChangeListener Interface
	public void languageChanged(String languageCode)
	{
		displayWarningOfUnofficialTranslationIfNecessary(languageCode);
		changeLanguagesAndRestartSignin(languageCode);
		dispose();
	}
	
	void changeLanguagesAndRestartSignin(String languageCode)
	{
		localization.setCurrentLanguageCode(languageCode);
		uiState.setCurrentLanguage(languageCode);
		uiState.save();
		usersChoice = LANGUAGE_CHANGED;
	}

	void displayWarningOfUnofficialTranslationIfNecessary(String languageCodeChangingTo)
	{
		if(localization.isOfficialTranslation(languageCodeChangingTo))
			return;
		UiMainWindow.displayDefaultUnofficialTranslationMessage(owner);

	}
}

