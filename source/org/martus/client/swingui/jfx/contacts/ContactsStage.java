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
package org.martus.client.swingui.jfx.contacts;

import javax.swing.JOptionPane;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxNonWizardStage;

public class ContactsStage extends FxNonWizardStage
{
	public ContactsStage(UiMainWindow mainWindow) throws Exception
	{
		super(mainWindow);
		
		setShellController(new ContactsShellController(getMainWindow()));
		setCurrentController(new FxManageContactsController(getMainWindow()));
	}

	@Override
	protected String getCssName()
	{
		return "Contacts.css";
	}
	
	@Override
	protected boolean confirmExit()
	{
		MartusLocalization localization = getMainWindow().getLocalization();
		String title = localization.getWindowTitle("ExitManageContacts");
		String message = localization.getFieldLabel("ExitManageContacts");
		int result = JOptionPane.showConfirmDialog(getDialog(), message, title, JOptionPane.YES_NO_OPTION);
		if (result == JOptionPane.YES_OPTION)
			return true;
		return false;
	}
	
}
