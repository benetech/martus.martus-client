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

import javax.swing.JFileChooser;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.dialogs.UiSigninDlg;
import org.martus.common.crypto.MartusCrypto;
import org.martus.swing.UiFileChooser;


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
		mainWindow.notifyDlg(mainWindow, "RecoveryProcessBackupFile");
		File startingDirectory = new File("");
		while(true)
		{
			UiFileChooser chooser = new UiFileChooser();
			String windowTitle = localization.getWindowTitle("RecoverKeyPair");
			chooser.setDialogTitle(windowTitle);
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			chooser.setSelectedFile(new File("", " "));
			chooser.setCurrentDirectory(startingDirectory);
			boolean showCancelDlg = true;
			boolean showUnableToRecoverDlg = false;
			if (chooser.showOpenDialog(mainWindow) == JFileChooser.APPROVE_OPTION)
			{
				File backupFile = chooser.getSelectedFile();
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
			startingDirectory = chooser.getCurrentDirectory();
			if(showUnableToRecoverDlg)
			{
				if(!mainWindow.confirmDlg(mainWindow, "UnableToRecoverFromBackupFile"))
					return false;
			}
			if(showCancelDlg)
			{	
				if(mainWindow.confirmDlg(mainWindow, "CancelBackupRecovery"))
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
			 signinDlg = new UiSigninDlg(localization, mainWindow.getCurrentUiState(), mainWindow, UiSigninDlg.SECURITY_VALIDATE);
			userChoice = signinDlg.getUserChoice();
		}
		if(userChoice != UiSigninDlg.SIGN_IN)
			throw new AbortedSignInException();
		
		FileInputStream inputStream = new FileInputStream(backupFile);
		try
		{
			userName = signinDlg.getName();
			userPassword = signinDlg.getPassword();
			app.getSecurity().readKeyPair(inputStream, app.getCombinedPassPhrase(userName, userPassword));
		}
		catch (Exception e)
		{
			throw new AttemptedSignInFailedException();
		}
		finally
		{
			inputStream.close();
		}
	}
	
	private boolean saveKeyPairToAccount()
	{
		String digestOfAccountsPublicCode = MartusCrypto.getHexDigest(app.getAccountId());
		File keyPairFile = app.getKeyPairFile(app.getAccountDirectory(digestOfAccountsPublicCode));
		if(keyPairFile.exists())
		{
			if(!mainWindow.confirmDlg(mainWindow, "KeyPairFileExistsOverWrite"))
				return false;
		}
		
		File accountsHashOfUserNameFile = app.getUserNameHashFile(keyPairFile.getParentFile());
		accountsHashOfUserNameFile.delete();
		if(!mainWindow.saveKeyPairFile(keyPairFile,userName, userPassword))
			return false;

		mainWindow.notifyDlg(mainWindow, "RecoveryOfKeyPairComplete");
		return true;
		
	}
	private MartusApp app;
	private UiMainWindow mainWindow;
	private UiLocalization localization;
	
	private String userName;
	private char[] userPassword;

}
