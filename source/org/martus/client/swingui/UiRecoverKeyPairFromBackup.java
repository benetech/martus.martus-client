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
package org.martus.client.swingui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.dialogs.UiSigninDlg;
import org.martus.common.clientside.UiPasswordField;
import org.martus.swing.UiFileChooser;
import org.martus.util.Base64.InvalidBase64Exception;


public class UiRecoverKeyPairFromBackup
{
	public UiRecoverKeyPairFromBackup(UiMainWindow windowToUse)
	{
		super();
		mainWindow = windowToUse;
		app = mainWindow.getApp();
		localization = mainWindow.getLocalization();
	}

	public boolean recoverPrivateKey()
	{
		mainWindow.notifyDlg("RecoveryProcessBackupFile");
		File startingDirectory = UiFileChooser.getHomeDirectoryFile(UiFileChooser.NO_FILE_SELECTED);
		while(true)
		{
			String windowTitle = localization.getWindowTitle("RecoverKeyPair");
			boolean showCancelDlg = true;
			boolean showUnableToRecoverDlg = false;
			UiFileChooser.FileDialogResults results = UiFileChooser.displayFileOpenDialog(mainWindow, windowTitle, startingDirectory);
			if (!results.wasCancelChoosen())
			{
				File backupFile = results.getFileChoosen();
				try
				{
					attemptSignIn(backupFile);
					return saveKeyPairToAccount();
				}
				catch (IOException e)
				{
					showCancelDlg = false;
					showUnableToRecoverDlg = true;
				}
				catch (AttemptedSignInFailedException e)
				{
					showCancelDlg = false;
					showUnableToRecoverDlg = true;
				}
				catch (AbortedSignInException e)
				{
					showCancelDlg = true;
					showUnableToRecoverDlg = false;
				}
				
			}
			startingDirectory = results.getCurrentDirectory();
			if(showUnableToRecoverDlg)
			{
				if(!mainWindow.confirmDlg("UnableToRecoverFromBackupFile"))
					return false;
			}
			if(showCancelDlg)
			{	
				if(mainWindow.confirmDlg("CancelBackupRecovery"))
					return false;
			}
		}
	}
	
	class AttemptedSignInFailedException extends Exception {}
	class AbortedSignInException extends Exception {}
	
	private void attemptSignIn(File backupFile) throws AttemptedSignInFailedException, AbortedSignInException, IOException
	{
		if(backupFile == null || !backupFile.isFile())
			throw new AttemptedSignInFailedException();
		
		UiSigninDlg signinDlg = null;
		int userChoice = UiSigninDlg.LANGUAGE_CHANGED;
		while(userChoice == UiSigninDlg.LANGUAGE_CHANGED)
		{	
			signinDlg = new UiSigninDlg(localization, mainWindow.getCurrentUiState(), mainWindow, UiSigninDlg.SECURITY_VALIDATE, userName, userPassword);
			userChoice = signinDlg.getUserChoice();
			userName = signinDlg.getName();
			userPassword = signinDlg.getPassword();
		}
		if(userChoice != UiSigninDlg.SIGN_IN)
			throw new AbortedSignInException();
		
		FileInputStream inputStream = new FileInputStream(backupFile);
		try
		{
			app.getSecurity().readKeyPair(inputStream, app.getCombinedPassPhrase(userName, userPassword));
		}
		catch (Exception e)
		{
			throw new AttemptedSignInFailedException();
		}
		finally
		{
			inputStream.close();
			UiPasswordField.scrubData(userPassword);
		}
	}
	
	private boolean saveKeyPairToAccount()
	{
		String accountId = app.getAccountId();
		File accountDirectory;
		try
		{
			accountDirectory = app.getAccountDirectory(accountId);
		}
		catch (InvalidBase64Exception e)
		{
			e.printStackTrace();
			mainWindow.notifyDlg("ErrorRecoveringAccountDirectory");
			return false;
		}
		File keyPairFile = app.getKeyPairFile(accountDirectory);
		if(keyPairFile.exists())
		{
			if(!mainWindow.confirmDlg("KeyPairFileExistsOverWrite"))
				return false;
		}
		
		File accountsHashOfUserNameFile = app.getUserNameHashFile(keyPairFile.getParentFile());
		accountsHashOfUserNameFile.delete();
		if(!mainWindow.saveKeyPairFile(keyPairFile,userName, userPassword))
			return false;

		mainWindow.notifyDlg("RecoveryOfKeyPairComplete");
		return true;
		
	}
	private MartusApp app;
	private UiMainWindow mainWindow;
	private UiLocalization localization;
	
	private String userName;
	private char[] userPassword;
}
