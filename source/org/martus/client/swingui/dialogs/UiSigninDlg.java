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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.martus.client.core.CurrentUiState;
import org.martus.client.swingui.UiConstants;
import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.UiSigninPanel;
import org.martus.client.swingui.fields.UiChoiceEditor;
import org.martus.swing.Utilities;



public class UiSigninDlg extends JDialog
{
	public UiSigninDlg(UiLocalization localizationToUse, CurrentUiState uiStateToUse , JFrame owner, int mode)
	{
		this(localizationToUse, uiStateToUse, owner, mode, "");
	}

	public UiSigninDlg(UiLocalization localizationToUse, CurrentUiState uiStateToUse, JFrame owner, int mode, String username)
	{
		super(owner, true);
		initalize(localizationToUse, uiStateToUse, owner, mode, username);
	}

	public void initalize(UiLocalization localizationToUse, CurrentUiState uiStateToUse, JFrame owner, int mode, String username)
	{
		currentMode = mode;
		localization = localizationToUse;
		uiState = uiStateToUse;
		usersChoice = CANCEL;
		setTitle(getTextForTitle(localization, currentMode));
		
		signinPane = new UiSigninPanel(this, currentMode, username);
		
		ok = new JButton(localization.getButtonLabel("ok"));
		ok.addActionListener(new OkHandler());
		JButton cancel = new JButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(new CancelHandler());
		Box buttonBox = Box.createHorizontalBox();
		languageDropdown = new UiChoiceEditor(localization.getUiLanguages());
		languageDropdown.setText(localization.getCurrentLanguageCode());
		languageDropdown.addActionListener(new LanguageListener());
		buttonBox.add(languageDropdown.getComponent());
		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.add(ok);
		buttonBox.add(cancel);
		buttonBox.add(Box.createHorizontalGlue());
		
		buttonBox.setBorder(new EmptyBorder(5,5,5,5));
		JPanel scrolledPanel = createMainPanel();

		Container scrollingPane = new JScrollPane(scrolledPanel);
		getContentPane().add(scrollingPane);
		getContentPane().add(buttonBox, BorderLayout.SOUTH);
		
		getRootPane().setDefaultButton(ok);
		signinPane.refreshForNewVirtualMode();
		setResizable(true);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if(screenSize.width < 1000)
		{	
			setSize(screenSize.width, screenSize.height * 8 / 10);
			setLocation(0,screenSize.height/10);
		}
		else
		{	
			Utilities.centerDlg(this);
		}
		show();
	}

	JPanel createMainPanel()
	{
		JPanel scrolledPanel = new JPanel(); 
		scrolledPanel.add(signinPane);
		return scrolledPanel;
	}

	public String getTextForTitle(UiLocalization localization, int mode)
	{
		String versionInfo = UiConstants.programName;
		versionInfo += " " + localization.getFieldLabel("aboutDlgVersionInfo");
		versionInfo += " " + UiConstants.versionLabel;
		String title = ""; 
		switch (mode)
		{
			case SECURITY_VALIDATE:
				title = localization.getWindowTitle("MartusSignInValidate"); 
				break;
			case RETYPE_USERNAME_PASSWORD:
				title = localization.getWindowTitle("MartusSignInRetypePassword"); 
				break;
			default:
				title = getInitialSigninTitle(localization); 
				break;
		}
		
		String completeTitle = title +" (" + versionInfo + ")";
		return completeTitle;
	}
	
	static public String getInitialSigninTitle(UiLocalization localization)
	{
		return localization.getWindowTitle("MartusSignIn");
	}

	public int getUserChoice()
	{
		return usersChoice;
	}

	public String getName()
	{
		return signinPane.getName();
	}

	public char[] getPassword()
	{
		return signinPane.getPassword();
	}
	
	public void sizeHasChanged()
	{
		Utilities.centerDlg(this);
	}
	
	public void virtualPasswordHasChanged()
	{
		getRootPane().setDefaultButton(ok);
	}
	
	public void handleOk()
	{
		usersChoice = SIGN_IN;
		dispose();
	}
	
	class OkHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			handleOk();
		}
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

	class CancelHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			usersChoice = CANCEL;
			dispose();
		}
	}
	
	public UiLocalization getLocalization()
	{
		return localization;
	}
	
	public CurrentUiState getCurrentUiState()
	{
		return uiState;
	}
	
	UiSigninPanel signinPane;
	UiLocalization localization;
	CurrentUiState uiState;
	int usersChoice;
	boolean languageChanged;
	private JButton ok;
	UiChoiceEditor languageDropdown;
	int currentMode;
	
	public final static int INITIAL = 1;
	public final static int TIMED_OUT = 2;
	public final static int SECURITY_VALIDATE = 3;
	public final static int RETYPE_USERNAME_PASSWORD = 4;
	public final static int CREATE_NEW = 5;
	public final static int INITIAL_NEW_RECOVER_ACCOUNT = 6;
	
	public final static int CANCEL = 10;
	public final static int SIGN_IN = 11;
	public final static int NEW_ACCOUNT = 12;
	public final static int RECOVER_ACCOUNT_BY_SHARE = 13;
	public final static int RECOVER_ACCOUNT_BY_BACKUP_FILE = 14;
	public final static int LANGUAGE_CHANGED = 15;
}

