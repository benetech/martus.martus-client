/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2007, Beneficent
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.database.ReadableDatabase;
import org.martus.swing.UiFileChooser;

class SaveHandler implements ActionListener
{
	public SaveHandler(UiMainWindow mainWindowToUse, AttachmentProxy proxyToUse)
	{
		mainWindow = mainWindowToUse;
		proxy = proxyToUse;
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		String fileName = proxy.getLabel();

		File last = UiAttachmentComponent.getLastAttachmentSaveDirectory();
		if(last == null)
			last = UiFileChooser.getHomeDirectoryFile();
		File attachmentFileToSave = new File(last, fileName);
		UiFileChooser.FileDialogResults results = UiFileChooser.displayFileSaveDialog(mainWindow, null, attachmentFileToSave);
		if(results.wasCancelChoosen())
			return;
		UiAttachmentComponent.setLastAttachmentSaveDirectory(results.getCurrentDirectory());
		File outputFile = results.getChosenFile();
		if(outputFile.exists())
		{
			if(!mainWindow.confirmDlg("OverWriteExistingFile"))
				return;
		}
		mainWindow.setWaitingCursor();
		try
		{
			ClientBulletinStore store = mainWindow.getApp().getStore();
			ReadableDatabase db = store.getDatabase();
			BulletinLoader.extractAttachmentToFile(db, proxy, store.getSignatureVerifier(), outputFile);
		}
		catch(Exception e)
		{
			mainWindow.notifyDlg("UnableToSaveAttachment");
			System.out.println("Unable to save file :" + e);
		}
		mainWindow.resetCursor();
	}

	UiMainWindow mainWindow;
	AttachmentProxy proxy;
}