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

import java.util.Arrays;

import org.martus.client.core.MartusUserNameAndPassword;
import org.martus.client.core.Exceptions.BlankUserNameException;
import org.martus.client.core.Exceptions.PasswordMatchedUserNameException;
import org.martus.client.core.Exceptions.PasswordTooShortException;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.clientside.UiPasswordField;

/**
 * UiCreateNewAccountProcess
 *
 * Class encapusulates all aspects of creating a new username and password combo
 * - Displays the username and password entry dialog
 * - Checks the username and password to make sure they meet our requirements
 * - Confirms the username and password
 * - Reminds the user to remember his/her password
 *
 * @author dchu
 *
 */
public class UiCreateNewAccountProcess
{
	public UiCreateNewAccountProcess(
		UiMainWindow window,
		String originalUserName)
	{
		mainWindow = window;
		while (true)
		{
			UiSigninDlg signinDlg1 = getSigninResults(UiSigninDlg.CREATE_NEW, originalUserName);
			if (signinDlg1.getUserChoice() != UiSigninDlg.SIGN_IN)
				return;

			userName1 = signinDlg1.getName();
			userPassword1 = signinDlg1.getPassword();
			
			if(!userName1.equals(originalUserName))
			{	
				boolean userAlreadyExists = false;
				try
				{
					userAlreadyExists = window.getApp().doesAccountExist(userName1, userPassword1);
				}
				catch (Exception e)
				{
					userAlreadyExists = false;
				} 

				if(userAlreadyExists)
				{	
					window.notifyDlg(window, "UserAlreadyExists");
					continue;
				}
			}
				
			
			String defaultUserName = "";
			if (userName1.equals(originalUserName))
				defaultUserName = originalUserName;

			UiSigninDlg signinDlg2 = getSigninResults( UiSigninDlg.RETYPE_USERNAME_PASSWORD, defaultUserName);
			if (signinDlg1.getUserChoice() != UiSigninDlg.SIGN_IN)
				
			
			if (signinDlg2.getUserChoice() != UiSigninDlg.SIGN_IN)
				return;
			String userName2 = signinDlg2.getName();
			char[] userPassword2 = signinDlg2.getPassword();

			// make sure the passwords and usernames match
			if (!Arrays.equals(userPassword1, userPassword2))
			{
				window.notifyDlg(window, "passwordsdontmatch");
				continue;
			}
			if (!userName1.equals(userName2))
			{
				window.notifyDlg(window, "usernamessdontmatch");
				continue;
			}

			// next make sure the username and password is valid
			try
			{
				MartusUserNameAndPassword.validateUserNameAndPassword(userName1, userPassword1);
			}
			catch (BlankUserNameException bune)
			{
				window.notifyDlg(window, "UserNameBlank");
				continue;
			}
			catch (PasswordTooShortException ptse)
			{
				window.notifyDlg(window, "PasswordInvalid");
				continue;
			}
			catch (PasswordMatchedUserNameException pmune)
			{
				window.notifyDlg(window, "PasswordMatchesUserName");
				continue;
			}

			// finally warn them if its a weak password
			if(MartusUserNameAndPassword.isWeakPassword(userPassword1))
			{
				if(!window.confirmDlg("RedoWeakPassword"))
					continue;
			}

			remindUsersToRememberPassword();
			result = true;
			break;
		}
	}

	private UiSigninDlg getSigninResults(int mode, String userName)
	{
		UiSigninDlg signinDlg = null;
		int userChoice = UiSigninDlg.LANGUAGE_CHANGED;
		char[] userPassword = "".toCharArray();
		while(userChoice == UiSigninDlg.LANGUAGE_CHANGED)
		{	
			signinDlg = new UiSigninDlg(mainWindow.getLocalization(), mainWindow.getCurrentUiState(), mainWindow, mode, userName, userPassword);
			userChoice = signinDlg.getUserChoice();
			userName = signinDlg.getName();
			userPassword = signinDlg.getPassword();
		}
		UiPasswordField.scrubData(userPassword);
		return signinDlg;
	}

	private void remindUsersToRememberPassword()
	{
		mainWindow.notifyDlg("RememberPassword");
	}

	public boolean isDataValid()
	{
		return result;
	}

	public String getUserName()
	{
		return userName1;
	}

	public char[] getPassword()
	{
		return userPassword1;
	}

	private String userName1;
	private char[] userPassword1;
	private UiMainWindow mainWindow;
	private boolean result;
}
