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

package org.martus.client.swingui.actions;

import java.awt.event.ActionEvent;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiQuickEraseConfirmDlg;

public class ActionMenuQuickErase extends UiMenuAction 
{
	public ActionMenuQuickErase (UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse, "QuickErase");
	}
	
	public void actionPerformed(ActionEvent arg0) 
	{			
		if(!mainWindow.reSignIn())
			return;
		
		UiQuickEraseConfirmDlg dlg = new UiQuickEraseConfirmDlg(mainWindow, mainWindow.getLocalization(), "DoQuickErase");
		dlg.show();
		
		if (!dlg.isOkayPressed())
			return;

		boolean scrubSelected = dlg.isScrubCheckBoxSelected();
		boolean deletKeyPairSelected = dlg.isDeleteKeypairSelected();
					
		QuickEraseOptions options = new QuickEraseOptions();			
		options.setScrubOption(scrubSelected);
		options.setDeleteKeyPairOption(deletKeyPairSelected);	
		
		if(mainWindow.getApp().deleteAllBulletinsAndUserFolders(options))
		{	
			String baseTag = (scrubSelected)? "QuickEraseScrubWorked":"QuickEraseWorked";		
			mainWindow.notifyDlg(mainWindow, baseTag);
		}						
		else
			mainWindow.notifyDlg(mainWindow, "QuickEraseFailed");
			
		mainWindow.folderTreeContentsHaveChanged();		
	}
}
