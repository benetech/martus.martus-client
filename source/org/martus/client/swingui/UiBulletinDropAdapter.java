/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2004, Beneficent
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

import java.awt.Cursor;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipFile;
import org.martus.client.core.BulletinFolder;
import org.martus.client.core.ClientBulletinStore;
import org.martus.client.core.TransferableBulletinList;
import org.martus.client.core.ClientBulletinStore.BulletinAlreadyExistsException;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.Packet.WrongPacketTypeException;
import org.martus.util.Base64.InvalidBase64Exception;

public abstract class UiBulletinDropAdapter implements DropTargetListener
{
	public UiBulletinDropAdapter(UiMainWindow mainWindow)
	{
		observer = mainWindow;
	}

	abstract public BulletinFolder getFolder(Point at);

	// DropTargetListener interface
	public void dragEnter(DropTargetDragEvent dtde) {}
	public void dragExit(DropTargetEvent dte) {}

	public void dropActionChanged(DropTargetDragEvent dtde) {}

	public void dragOver(DropTargetDragEvent dtde)
	{
		BulletinFolder folder = getFolder(dtde.getLocation());
		if(folder == null)
		{
			dtde.rejectDrag();
			return;
		}

		if(dtde.isDataFlavorSupported(TransferableBulletinList.getBulletinListDataFlavor()))
			dtde.acceptDrag(dtde.getDropAction());
		else if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			dtde.acceptDrag(dtde.getDropAction());
		else
			dtde.rejectDrag();
	}

	public void drop(DropTargetDropEvent dtde)
	{
		Cursor originalCursor = observer.setWaitingCursor();
		if(dtde.isDataFlavorSupported(TransferableBulletinList.getBulletinListDataFlavor()))
			dropTransferableBulletins(dtde);
		else if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			dropFiles(dtde);
		observer.resetCursor(originalCursor);
	}

	// private methods
	private void dropTransferableBulletins(DropTargetDropEvent dtde)
	{
		System.out.println("dropTransferableBulletin");
		BulletinFolder toFolder = getFolder(dtde.getLocation());
		if(toFolder == null)
		{
			dtde.rejectDrop();
			return;
		}
		Transferable t = dtde.getTransferable();
		TransferableBulletinList tb = TransferableBulletinList.extractFrom(t);
		if(tb == null)
		{
			dtde.rejectDrop();
			return;
		}
		BulletinFolder fromFolder = tb.getFromFolder();
		if(fromFolder.equals(toFolder))
		{
			dtde.rejectDrop();
			return;
		}
		Cursor originalCursor = observer.setWaitingCursor();
		dtde.acceptDrop(dtde.getDropAction());
		//System.out.println("dropTransferableBulletin: accepted");

		String errorTag = null;
		try
		{
			attemptDropBulletins(tb.getBulletins(), toFolder);
			
			ClientBulletinStore store = observer.getStore();
			Bulletin[] wereDropped = tb.getBulletins();
			for (int i = 0; i < wereDropped.length; i++)
			{
				Bulletin bulletin = wereDropped[i];
				UniversalId uId = bulletin.getUniversalId();
				Bulletin b = store.getBulletinRevision(uId);
				if(b == null)
					System.out.println("dropTransferableBulletin: null bulletin!!");
				else
					store.removeBulletinFromFolder(fromFolder, b);
			}
			store.saveFolders();
			observer.folderContentsHaveChanged(fromFolder);
		}
		catch (BulletinAlreadyExistsException e)
		{
			errorTag = "DropErrorBulletinExists";
		}
		catch (Exception e)
		{
			errorTag = "DropErrors";
		}
		//System.out.println("dropTransferableBulletin: Drop Complete!");
		
		boolean worked = (errorTag == null);
		tb.dispose();
		dtde.dropComplete(worked);
		observer.resetCursor(originalCursor);
		if(!worked)
			observer.notifyDlgBeep(observer, errorTag);
	}

	private void dropFiles(DropTargetDropEvent dtde)
	{
		System.out.println("dropFile");
		BulletinFolder toFolder = getFolder(dtde.getLocation());
		if(toFolder == null)
		{
			System.out.println("dropFile: toFolder null");
			dtde.rejectDrop();
			return;
		}

		dtde.acceptDrop(dtde.getDropAction());

		Transferable t = dtde.getTransferable();
		List list = null;
		try
		{
			list = (List)t.getTransferData(DataFlavor.javaFileListFlavor);
		}
		catch(Exception e)
		{
			System.out.println("dropFile exception: " + e);
			dtde.dropComplete(false);
			return;
		}

		if(list.size() == 0)
		{
			System.out.println("dropFile: list empty");
			dtde.dropComplete(false);
			return;
		}

		String resultMessageTag = null;
		int filesDropped = 0;
		for(int i = 0; i < list.size(); ++i)
		{	
			File file = (File)list.get(i);
			//System.out.println(file.getPath());
	
			try
			{
				attemptDropFile(file, toFolder);
				++filesDropped;
			}
			catch (BulletinAlreadyExistsException e)
			{
				resultMessageTag = "DropErrorBulletinExists";
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.out.println("dropFile Exception:" + e);
				resultMessageTag = "DropErrors";
			}
		}
		boolean worked = (filesDropped == list.size());
		dtde.dropComplete(worked);

		if(!worked)
			observer.notifyDlgBeep(observer, resultMessageTag);
	}
	
	public void attemptDropFiles(File[] files, BulletinFolder toFolder) throws
		BulletinAlreadyExistsException,
		Exception
	{
		int errorThrown = noError;
		for(int i = 0; i<files.length; ++i)
		{
			try
			{
				attemptDropFile(files[i], toFolder);
			}
			catch (BulletinAlreadyExistsException e)
			{
				if(errorThrown == noError)
					errorThrown = bulletinExists;
			}
			catch (Exception e)
			{
				errorThrown = otherError;
			}
		}
		if(errorThrown == bulletinExists)
			throw new BulletinAlreadyExistsException();
		if(errorThrown == otherError)
			throw new Exception();
	}
	
	public void attemptDropFile(File file, BulletinFolder toFolder) throws
		InvalidPacketException, 
		SignatureVerificationException, 
		WrongPacketTypeException, 
		CryptoException, IOException, 
		InvalidBase64Exception, 
		BulletinAlreadyExistsException
	{
		ClientBulletinStore store = toFolder.getStore();
		if(!deleteOldUnAuthoredBulletinIfRequired(file, store))
			return;
		Cursor originalCursor = observer.setWaitingCursor();
		try
		{
			store.importZipFileBulletin(file, toFolder, false);
			observer.folderContentsHaveChanged(toFolder);
		}
		finally
		{
			observer.resetCursor(originalCursor);
		}
	}
	
	private boolean deleteOldUnAuthoredBulletinIfRequired(File file, ClientBulletinStore store) throws SignatureVerificationException, IOException
	{
		ZipFile zip = new ZipFile(file);
		BulletinHeaderPacket bhp = BulletinHeaderPacket.loadFromZipFile(zip, store.getSignatureVerifier());
		UniversalId uid = bhp.getUniversalId();
		
		Bulletin old = store.getBulletinRevision(uid);
		if(!store.isMyBulletin(bhp) && old != null)
		{
			HashMap tokenReplacement = new HashMap();
			tokenReplacement.put("#Title#", old.get(Bulletin.TAGTITLE));
			if(observer.confirmDlg(observer, "UnAuthoredBulletinDeleteBeforePaste", tokenReplacement))
				store.destroyBulletin(old);
			else
				return false;
		}
		return true;
	}


	public void attemptDropBulletins(Bulletin[] bulletins, BulletinFolder toFolder) throws
		BulletinAlreadyExistsException, IOException
	{
		System.out.println("attemptDropBulletin");

		ClientBulletinStore store = toFolder.getStore();
		int errorThrown = noError;
		for (int i = 0; i < bulletins.length; i++)
		{
			Bulletin bulletin = bulletins[i];
			System.out.println("UiBulletinDropAdapter.attemptDropBulletins: " + bulletin.get(Bulletin.TAGTITLE));
		}


		for (int i = 0; i < bulletins.length; i++)
		{
			Bulletin bulletin = bulletins[i];
			try
			{
				store.addBulletinToFolder(toFolder, bulletin.getUniversalId());
			}
			catch (BulletinAlreadyExistsException e)
			{
				if(errorThrown == noError)
					errorThrown = bulletinExists;
			}
			catch (IOException e)
			{
				if(errorThrown == noError)
					errorThrown = ioError;
			}
		}
		store.saveFolders();

		observer.folderContentsHaveChanged(toFolder);
		
		if(errorThrown == ioError)
			throw new IOException();
		if(errorThrown == bulletinExists)
			throw new BulletinAlreadyExistsException();
		}

	final int noError = 0;
	final int bulletinExists = 1;
	final int ioError = 2;
	final int otherError = 3;

	UiMainWindow observer;
}

