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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;

import org.martus.common.clientside.*;
import org.martus.swing.UiWrappedTextArea;


public class UiInitialSigninDlg extends UiSigninDlg
{
	public UiInitialSigninDlg(UiBasicLocalization localizationToUse, CurrentUiState uiStateToUse, JFrame owner, int mode, String userName, char[] password)
	{
		super(localizationToUse, uiStateToUse, owner, mode, userName, password);
	}

	protected JPanel createMainPanel()
	{
		JPanel scrolledPanel = new JPanel(); 
		tabbedPane = new JTabbedPane();
		tabLabelSignIn = localization.getButtonLabel("SignIn");
		tabLabelNewAccount = localization.getButtonLabel("NewAccountTab");
		tabLabelRecoverAccount = localization.getButtonLabel("RecoverAccountTab");
		if(currentMode == INITIAL)
			tabbedPane.add(signinPane,tabLabelSignIn);
		tabbedPane.add(createNewAccountPanel(), tabLabelNewAccount);
		tabbedPane.add(createRecoverAccountPanel(), tabLabelRecoverAccount);
		scrolledPanel.add(tabbedPane);
		return scrolledPanel;
	}

	JComponent createNewAccountPanel()
	{
		String message = "HowToCreateNewAccount";
		if(currentMode == INITIAL_NEW_RECOVER_ACCOUNT)
			message = "HowToCreateInitialAccount";
		String text = localization.getFieldLabel(message);
		return new UiWrappedTextArea("\n" + text, localization.getComponentOrientation());
	}
	
	JPanel createRecoverAccountPanel()
	{
		JPanel radioButtonPanel = new JPanel();
		radioButtonPanel.setLayout(new BoxLayout(radioButtonPanel, BoxLayout.Y_AXIS));
		radioBackupFile = new JRadioButton(localization.getButtonLabel("RecoverAccountByBackup"), true);
		radioBackupFile.setActionCommand("backupFile");
		radioShare = new JRadioButton(localization.getButtonLabel("RecoverAccountByShare"), false);
		radioShare.setActionCommand("share");
		recoveryTypeGroup = new ButtonGroup();
		recoveryTypeGroup.add(radioBackupFile);
		recoveryTypeGroup.add(radioShare);

		radioButtonPanel.add(new JLabel(" "));
		radioButtonPanel.add(radioBackupFile);
		radioButtonPanel.add(radioShare);
		radioButtonPanel.add(Box.createVerticalStrut(5));
		
		JPanel recoverAccountPanel = new JPanel();
		recoverAccountPanel.setLayout(new BorderLayout());
		recoverAccountPanel.add(new JLabel(localization.getFieldLabel("RecoverAccount")),BorderLayout.NORTH);
		recoverAccountPanel.add(radioButtonPanel, BorderLayout.CENTER);
		return recoverAccountPanel;
	}
	public void handleOk()
	{
		
		int tabNumber = tabbedPane.getSelectedIndex();
		if(tabbedPane.getTitleAt(tabNumber).equals(tabLabelSignIn))
			usersChoice = SIGN_IN;
		else if(tabbedPane.getTitleAt(tabNumber).equals(tabLabelNewAccount))
			usersChoice = NEW_ACCOUNT;
		else if(tabbedPane.getTitleAt(tabNumber).equals(tabLabelRecoverAccount))
		{
			ButtonModel model = recoveryTypeGroup.getSelection();
			if(model.getActionCommand().equals("share"))
				usersChoice = RECOVER_ACCOUNT_BY_SHARE;
			else 
				usersChoice = RECOVER_ACCOUNT_BY_BACKUP_FILE;
		}
		dispose();
	}

	private ButtonGroup recoveryTypeGroup;
	private JTabbedPane tabbedPane;
	private JRadioButton radioShare;
	private JRadioButton radioBackupFile;
	
	private String tabLabelSignIn;
	private	String tabLabelNewAccount;
	private	String tabLabelRecoverAccount;

}
