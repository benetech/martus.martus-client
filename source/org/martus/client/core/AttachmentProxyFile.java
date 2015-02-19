/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2015, Beneficent
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
package org.martus.client.core;

import java.io.File;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.fields.attachments.ViewAttachmentHandler;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.ReadableDatabase;

public class AttachmentProxyFile
{
	public static AttachmentProxyFile wrapFile(File pretendNonTemp)
	{
		return new AttachmentProxyFile(pretendNonTemp, false);
	}

	public static AttachmentProxyFile extractAttachment(ClientBulletinStore storeToUse, AttachmentProxy proxyFromDatabase) throws Exception
	{
		File fileToWrap = obtainFileForAttachment(proxyFromDatabase, storeToUse);
		return new AttachmentProxyFile(fileToWrap, true);
	}

	public AttachmentProxyFile(File fileToWrap, boolean shouldDeleteOnRelease)
	{
		file = fileToWrap;
		shouldDelete = shouldDeleteOnRelease;
	}
	
	public File getFile()
	{
		return file;
	}
	
	public void release()
	{
		if(shouldDelete)
			file.delete();
		
		file = null;
	}

	static public File obtainFileForAttachment(AttachmentProxy proxy, ClientBulletinStore store) throws Exception
	{
		ReadableDatabase db = store.getDatabase();
		MartusCrypto security = store.getSignatureVerifier();
	
		return ViewAttachmentHandler.obtainFileForAttachment(proxy, db, security);
	}

	private File file;
	private boolean shouldDelete;
}
