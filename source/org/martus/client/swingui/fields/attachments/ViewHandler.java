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
import org.martus.client.swingui.fields.attachments.UiAttachmentComponent.ViewSingleAttachmentPanel;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.ReadableDatabase;
import org.martus.swing.Utilities;

class ViewHandler implements ActionListener
{
	public ViewHandler(UiMainWindow mainWindowToUse, ViewSingleAttachmentPanel panelToUse)
	{
		mainWindow = mainWindowToUse;
		panel = panelToUse;
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		panel.showImageInline();
		if(panel.isImageInline)
			return;
		
		if(!Utilities.isMSWindows())
		{
			mainWindow.notifyDlg("ViewAttachmentNotAvailable");
			return;
		}
		
		AttachmentProxy proxy = panel.getAttachmentProxy();
		String author = proxy.getUniversalId().getAccountId();
		if(!author.equals(mainWindow.getApp().getAccountId()))
		{
			if(!mainWindow.confirmDlg("NotYourBulletinViewAttachmentAnyways"))
				return;
		}
		mainWindow.setWaitingCursor();
		try
		{
			ClientBulletinStore store = mainWindow.getApp().getStore();
			ReadableDatabase db = store.getDatabase();
			MartusCrypto security = store.getSignatureVerifier();
			File temp = UiAttachmentComponent.extractAttachmentToTempFile(db, proxy, security);

			Runtime runtimeViewer = Runtime.getRuntime();
			String tempFileFullPathName = temp.getPath();
			Process processView=runtimeViewer.exec("rundll32"+" "+"url.dll,FileProtocolHandler"+" "+tempFileFullPathName);
			processView.waitFor();
		}
		catch(Exception e)
		{
			mainWindow.notifyDlg("UnableToViewAttachment");
			System.out.println("Unable to view file :" + e);
		}
		mainWindow.resetCursor();
	}
	
	UiMainWindow mainWindow;
	ViewSingleAttachmentPanel panel;
}