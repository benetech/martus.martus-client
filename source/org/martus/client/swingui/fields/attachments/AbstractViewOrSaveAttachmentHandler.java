/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2013, Beneficent
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
package org.martus.client.swingui.fields.attachments;

import java.awt.event.ActionListener;
import java.util.HashMap;

import org.martus.client.swingui.UiMainWindow;
import org.martus.clientside.UiUtilities;


public abstract class AbstractViewOrSaveAttachmentHandler implements ActionListener
{
	public AbstractViewOrSaveAttachmentHandler(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
	}

	public UiMainWindow getMainWindow()
	{
		return mainWindow;
	}
	
	boolean confirmViewOrSaveNotYourAttachment(final String baseTag)
	{
		String title = mainWindow.getConfirmDialogTitle(baseTag);
		String cause = mainWindow.getConfirmCauseText(baseTag);
		String effect = mainWindow.getConfirmEffectText(baseTag);
		String question = UiUtilities.getConfirmQuestionText(mainWindow.getLocalization());
		String[] contents = { cause, "", effect, "", question};
		String[] buttons = UiUtilities.getConfirmDialogButtons(mainWindow.getLocalization());
		HashMap<String, String> replacements = new HashMap<String, String>();
		return UiUtilities.confirmDlg(mainWindow, title, contents, buttons, replacements);
	}

	private UiMainWindow mainWindow;
}
