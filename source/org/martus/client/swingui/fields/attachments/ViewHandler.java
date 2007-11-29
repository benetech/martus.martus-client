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
import java.io.IOException;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.BulletinXmlExporter;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.packet.UniversalId;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.Packet.WrongPacketTypeException;
import org.martus.swing.Utilities;
import org.martus.util.StreamableBase64.InvalidBase64Exception;

class ViewHandler implements ActionListener
{
	public ViewHandler(UiMainWindow mainWindowToUse, AbstractAttachmentPanel panelToUse)
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
		String proxyAuthor = getProxyAuthor(proxy);
		if(proxyAuthor != null && !mainWindow.getApp().getAccountId().equals(proxyAuthor))
		{
			if(!mainWindow.confirmDlg("NotYourBulletinViewAttachmentAnyways"))
				return;
		}
		mainWindow.setWaitingCursor();
		try
		{
			File temp = getAttachmentAsFile(proxy);
			launchExternalAttachmentViewer(temp);
		}
		catch(Exception e)
		{
			System.out.println("Unable to view attachment:" + e);
			notifyUnableToView();
		}
		mainWindow.resetCursor();
	}

	private String getProxyAuthor(AttachmentProxy proxy) 
	{
		UniversalId uid = proxy.getUniversalId();
		if(uid == null)
			return null;

		return uid.getAccountId();
	}

	private File getAttachmentAsFile(AttachmentProxy proxy) throws IOException, InvalidBase64Exception, InvalidPacketException, SignatureVerificationException, WrongPacketTypeException, CryptoException 
	{
		if(proxy.getFile() != null)
			return proxy.getFile();
		
		ClientBulletinStore store = mainWindow.getApp().getStore();
		ReadableDatabase db = store.getDatabase();
		MartusCrypto security = store.getSignatureVerifier();
		File temp = extractAttachmentToTempFile(db, proxy, security);
		return temp;
	}

	private void launchExternalAttachmentViewer(File temp) throws IOException, InterruptedException 
	{
		Runtime runtimeViewer = Runtime.getRuntime();
		String tempFileFullPathName = temp.getPath();
		Process processView=runtimeViewer.exec("rundll32"+" "+"url.dll,FileProtocolHandler"+" "+tempFileFullPathName);
		int exitCode = processView.waitFor();
		if(exitCode != 0)
		{
			System.out.println("Error viewing attachment: " + exitCode);
			notifyUnableToView();
		}
	}

	private void notifyUnableToView()
	{
		mainWindow.notifyDlg("UnableToViewAttachment");
	}
	
	static File extractAttachmentToTempFile(ReadableDatabase db, AttachmentProxy proxy, MartusCrypto security) throws IOException, InvalidBase64Exception, InvalidPacketException, SignatureVerificationException, WrongPacketTypeException, CryptoException
	{
		String fileName = proxy.getLabel();
		File temp = File.createTempFile(BulletinXmlExporter.extractFileNameOnly(fileName), BulletinXmlExporter.extractExtentionOnly(fileName));
		temp.deleteOnExit();
	
		BulletinLoader.extractAttachmentToFile(db, proxy, security, temp);
		return temp;
	}

	UiMainWindow mainWindow;
	AbstractAttachmentPanel panel;
}