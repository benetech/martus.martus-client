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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;

import org.martus.client.core.QuickEraseOptions;
import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiQuickEraseConfirmDlg;
import org.martus.util.DirectoryTreeRemover;

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
			
		QuickEraseOptions options = loadQuickEraseOptions(dlg); 		
		checkScrubData(options);
		checkDeleteKeyPair(options);	
		checkExitWhenComplete(options);
												
	}	

	private void checkScrubData(QuickEraseOptions options)
	{	
		File packetDir = mainWindow.getApp().getPacketsDirectory();
		if (!packetDir.exists())
			return;			
								
		if(mainWindow.getApp().deleteAllBulletinsAndUserFolders(options))
		{	
			confirmDeleteSubDirectory(packetDir, options);	
			String baseTag = (options.isScrubSelected())? "QuickEraseScrubWorked":"QuickEraseWorked";						
			mainWindow.notifyDlg(mainWindow, baseTag);								
		}
		else
			mainWindow.notifyDlg(mainWindow, "QuickEraseFailed");
			
		mainWindow.folderTreeContentsHaveChanged();		
	}
	
	static private boolean containSubDir(File startingDir)
	{
		File[] files = startingDir.listFiles();
		
		if (files != null)
		{			
			for (int i = 0; i < files.length; i++)
			{				
				if (files[i].isDirectory())
					return true;
			}
		}		
		return false;
	}
	
	
	private void confirmDeleteSubDirectory(File packetDir, QuickEraseOptions options)
	{
		if (!containSubDir(packetDir))
			return;
		
		UiLocalization localization = mainWindow.getLocalization();			
		String title = localization.getWindowTitle("confirmDoQuickErase");			
		String question = localization.getFieldLabel("confirmQuestionDeleteSubDirectory");
		String[] contents = {question};
		
		Toolkit.getDefaultToolkit().beep();
		if (mainWindow.confirmDlg(mainWindow, title, contents))
		{
			if (options.isScrubSelected())	
				DirectoryTreeRemover.scrubAndDeleteEntireDirectoryTree(packetDir);
			else
				DirectoryTreeRemover.deleteEntireDirectoryTree(packetDir);
		}		
	}

	private void checkExitWhenComplete(QuickEraseOptions options)
	{
		if (options.isExitWhenCompleteSelected())
		{	
			mainWindow.getApp().cleanupWhenCompleteQuickErase(options);
			mainWindow.exitWithoutSavingState();		
		}		
	}

	private void checkDeleteKeyPair(QuickEraseOptions options)
	{
		if (options.isDeleteKeyPairSelected())
		{
			UiLocalization localization = mainWindow.getLocalization();			
			String title = localization.getFieldLabel("DeleteKeypair");
			String cause = localization.getFieldLabel("confirmQuickEraseDeleteKeyPaircause");
			String question = localization.getFieldLabel("confirmQuestionDeleteKeypair");
			String[] contents = {cause,"", question};
			
			Toolkit.getDefaultToolkit().beep();
			if (mainWindow.confirmDlg(mainWindow, title, contents))
				mainWindow.getApp().scrubAndDeleteKeypair(options);	
		}	
	}

	private QuickEraseOptions loadQuickEraseOptions(UiQuickEraseConfirmDlg dlg)
	{
		QuickEraseOptions options = new QuickEraseOptions();
			
		boolean scrubSelected = dlg.isScrubCheckBoxSelected();
		boolean deletKeyPairSelected = dlg.isDeleteKeypairSelected();
		boolean exitWhenCompleteSelected = dlg.isExitWhenCompleteSelected();
				
		options.setScrubOption(scrubSelected);
		options.setDeleteKeyPairOption(deletKeyPairSelected);
		options.setExitWhenCompleteOption(exitWhenCompleteSelected);
		
		return options;	
	}
}
