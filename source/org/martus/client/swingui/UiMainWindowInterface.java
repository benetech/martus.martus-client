/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
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

import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.MartusApp;

public interface UiMainWindowInterface
{
    public MartusApp getApp();
	public MartusLocalization getLocalization();
	public ClientBulletinStore getStore();
	public void unexpectedErrorDlg(Exception e);
	public void exitNormally();
	public void exitWithoutSavingState();
	public boolean getDoZawgyiConversion();
	public boolean getUseInternalTor();
	public void saveConfigInfo();

	public void setWaitingCursor();
	public void resetCursor();

	public void setCurrentActiveFrame(JFrame currentActiveFrame);
	public JFrame getCurrentActiveFrame();
	public void setCurrentActiveDialog(JDialog newActiveDialog);
	public JDialog getCurrentActiveDialog();
	
	public boolean isServerAccessible(String address);
	public boolean isServerConfigured();

	public void notifyDlg(String baseTag);
	public void notifyDlg(String baseTag, Map tokenReplacement);
	public void notifyDlgBeep(String baseTag);
	public void notifyDlg(JFrame parent, String baseTag);
	public void notifyDlg(JFrame parent, String baseTag, Map tokenReplacement);
	public void notifyDlg(JFrame parent, String baseTag, String titleTag);
	public void notifyDlg(JFrame parent, String baseTag, String titleTag, Map tokenReplacement);
	public void notifyDlgBeep(JFrame parent, String baseTag);

	public void messageDlg(JFrame parent, String baseTag, String message);
	public void messageDlg(JFrame parent, String baseTag, String message, Map tokenReplacement);

	public boolean confirmDlg(String baseTag);
	public boolean confirmDlg(JFrame parent, String baseTag);
	public boolean confirmDlg(JFrame parent, String baseTag, Map tokenReplacement);
	public boolean confirmDlg(JFrame parent, String title, String[] contents);
	public boolean confirmDlg(JFrame parent, String title, String[] contents, String[] buttons);
	public boolean confirmCustomButtonsDlg(JFrame parent,String baseTag, String[] buttons, Map tokenReplacement);
	public boolean confirmDlg(JFrame parent, String title, String[] contents, String[] buttons, Map tokenReplacement);
	public boolean confirmDlgBeep(String baseTag);
}
