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

import org.martus.client.core.QuickEraseOptions;
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
			
		if (mainWindow.getApp().getFolderOutbox().getBulletinCount() > 0)
		{				
			if (!mainWindow.confirmDlgBeep(mainWindow, "QuickEraseOutboxNotEmpty"))
				return;
		}		
		
		UiQuickEraseConfirmDlg dlg = new UiQuickEraseConfirmDlg(mainWindow, mainWindow.getLocalization(), "DoQuickErase");
		dlg.show();
			
		if (!dlg.isOkayPressed())
			return;			
						
		QuickEraseOptions quickEraseOptions = dlg.getQuickEraseOptions();
		erasePacketData(quickEraseOptions);
		if (quickEraseOptions.isDeleteKeyPairSelected())
		{
			deleteKeyPairAndRelatedFiles(quickEraseOptions);
		}	
		if (quickEraseOptions.isExitWhenCompleteSelected())
		{	
			exitApp(quickEraseOptions);
		}
												
	}	

	private void erasePacketData(QuickEraseOptions options)
	{	
		String baseTag = "QuickEraseFailed";						
		if(mainWindow.getApp().deleteAllBulletinsAndUserFolders(options))
		{
			mainWindow.allFolderContentsHaveChanged();
			if(options.isScrubSelected())
				baseTag = "QuickEraseScrubWorked";
			else
				baseTag = "QuickEraseWorked";						
		}
		
		if(!options.isDonotPromptSelected())
			mainWindow.notifyDlg(mainWindow, baseTag);								

		mainWindow.folderTreeContentsHaveChanged();		
	}
	
	private void exitApp(QuickEraseOptions options)
	{
		mainWindow.getApp().cleanupWhenCompleteQuickErase(options);
		mainWindow.exitWithoutSavingState();		
	}

	private void deleteKeyPairAndRelatedFiles(QuickEraseOptions options)
	{
		if(options.isDonotPromptSelected() || mainWindow.confirmDlgBeep(mainWindow, "QuickEraseDeleteKeyPair"))
			mainWindow.getApp().deleteKeypairAndRelatedFiles(options);
	}
}
