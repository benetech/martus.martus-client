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

package org.martus.client.swingui.dialogs;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import org.martus.client.core.BulletinStore;
import org.martus.client.core.EncryptionChangeListener;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.UiUtilities;
import org.martus.client.swingui.bulletincomponent.UiBulletinComponent;
import org.martus.client.swingui.bulletincomponent.UiBulletinEditor;
import org.martus.client.swingui.fields.UiDateEditor;
import org.martus.client.swingui.fields.UiField.DataInvalidException;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.swing.Utilities;

public class UiBulletinModifyDlg extends JFrame implements ActionListener, WindowListener, EncryptionChangeListener
{
	public UiBulletinModifyDlg(Bulletin b, CancelHandler cancelHandlerToUse, UiMainWindow observerToUse)
	{
		observer = observerToUse;
		cancelHandler = cancelHandlerToUse;

		UiLocalization localization = observer.getLocalization();
		setTitle(localization.getWindowTitle("create"));
		UiUtilities.updateIcon(this);
		try
		{
			bulletin = b;

			view = new UiBulletinEditor(observer);
			view.copyDataFromBulletin(bulletin);

			view.setEncryptionChangeListener(this);

			send = new JButton(localization.getButtonLabel("send"));
			send.addActionListener(this);
			draft = new JButton(localization.getButtonLabel("savedraft"));
			draft.addActionListener(this);
			cancel = new JButton(localization.getButtonLabel("cancel"));
			cancel.addActionListener(this);

			scroller = new JScrollPane();
			scroller.getViewport().add(view);
			scroller.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

			indicateEncrypted(bulletin.isAllPrivate());

			Box box = Box.createHorizontalBox();
			box.add(send);
			box.add(draft);
			box.add(cancel);

			getContentPane().add(scroller);
			getContentPane().add(box, BorderLayout.SOUTH);

			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			addWindowListener(this);

			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Dimension screenSize = toolkit.getScreenSize();
			Dimension editorDimension = observerToUse.getBulletinEditorDimension();
			Point editorPosition = observerToUse.getBulletinEditorPosition();
			boolean showMaximized = false;
			if(Utilities.isValidScreenPosition(screenSize, editorDimension, editorPosition))
			{
				setLocation(editorPosition);
				setSize(editorDimension);
				if(observerToUse.isBulletinEditorMaximized())
					showMaximized = true;
			}
			else
				showMaximized = true;
			if(showMaximized)
			{
				setSize(screenSize.width - 50, screenSize.height - 50);
				Utilities.maximizeWindow(this);
			}
			show();
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource() == cancel)
		{
			closeWindowUponConfirmation();
			return;
		}
		try
		{
			view.validateData();
			view.copyDataToBulletin(bulletin);
		}
		catch(UiDateEditor.DateFutureException e)
		{
			observer.messageDlg(this,"ErrorDateInFuture", e.getlocalizedTag());
			return;
		}
		catch (DataInvalidException e) 
		{
			System.out.println("UiModifyBulletinDlg.actionPerformed: " + e);
			return;
		}
		catch(IOException e)
		{
			System.out.println("UiModifyBulletinDlg.actionPerformed: " + e);
			return;
		}
		catch(MartusCrypto.EncryptionException e)
		{
			System.out.println("UiModifyBulletinDlg.actionPerformed: " + e);
			return;
		} 


		Cursor originalCursor = getCursor();
		try {
			MartusApp app = observer.getApp();
			if(ae.getSource() == send)
			{
				if(!confirmSend())
					return;
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				saveSealedBulletin(app);
			}
			else
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				saveDraftBulletin(app);
			}
			observer.bulletinContentsHaveChanged(bulletin);
			app.getStore().saveFolders();
			observer.selectBulletinInCurrentFolderIfExists(bulletin.getUniversalId());
			weAreDoneSoClose();
			wasBulletinSavedFlag = true;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			observer.notifyDlg(this, "ErrorSavingBulletin");
		} 
		finally 
		{
			setCursor(originalCursor);
		}
	}

	private void saveDraftBulletin(MartusApp app)
		throws IOException, CryptoException 
	{
		bulletin.setDraft();
		app.setHQKeyInBulletin(bulletin);
		BulletinStore store = app.getStore();
		store.saveBulletin(bulletin);
		store.addBulletinToFolder(bulletin.getUniversalId(), store.getFolderDrafts());
		store.addBulletinToFolder(bulletin.getUniversalId(), store.getFolderDraftOutbox());
	}

	private void saveSealedBulletin(MartusApp app)
		throws IOException, CryptoException 
	{
		bulletin.setSealed();
		app.setHQKeyInBulletin(bulletin);
		BulletinStore store = app.getStore();
		store.saveBulletin(bulletin);
		store.moveBulletin(bulletin, store.getFolderDrafts(), store.getFolderOutbox());
		store.removeBulletinFromFolder(bulletin, store.getFolderDraftOutbox());
	}

	public boolean wasBulletinSaved()
	{
		return wasBulletinSavedFlag;
	}

	// WindowListener interface
	public void windowActivated(WindowEvent event) {}
	public void windowClosed(WindowEvent event) {}
	public void windowDeactivated(WindowEvent event) {}
	public void windowDeiconified(WindowEvent event) {}
	public void windowIconified(WindowEvent event) {}
	public void windowOpened(WindowEvent event) {}

	public void windowClosing(WindowEvent event)
	{
		closeWindowUponConfirmation();
	}
	// end WindowListener interface


	public void encryptionChanged(boolean newState)
	{
		indicateEncrypted(newState);
	}

	public void weAreDoneSoClose()
	{
		observer.folderContentsHaveChanged(observer.getStore().getFolderOutbox());
		observer.folderContentsHaveChanged(observer.getStore().getFolderDrafts());
		cleanupAndExit();
	}

	public void cleanupAndExit()
	{
		observer.doneModifyingBulletin();
		saveEditorState(getSize(), getLocation());
		dispose();
	}

	public void saveEditorState(Dimension size, Point location)
	{
		boolean maximized = getExtendedState() == MAXIMIZED_BOTH;
		observer.setBulletinEditorDimension(size);
		observer.setBulletinEditorPosition(location);
		observer.setBulletinEditorMaximized(maximized);
		observer.saveState();
	}

	public boolean confirmSend()
	{
		return observer.confirmDlg(this, "send");
	}

	private void indicateEncrypted(boolean isEncrypted)
	{
		view.updateEncryptedIndicator(isEncrypted);
	}

	private void closeWindowUponConfirmation()
	{
		if(observer.confirmDlg(this, "CancelModifyBulletin"))
		{
			cancelHandler.onCancel(observer.getStore(), bulletin);
			cleanupAndExit();
		}
	}

	public interface CancelHandler
	{
		public void onCancel(BulletinStore store,Bulletin b);
	}

	public static class DoNothingOnCancel implements CancelHandler
	{
		public void onCancel(BulletinStore store,Bulletin b)
		{
			// do nothing
		}
	}

	public static class DeleteBulletinOnCancel implements CancelHandler
	{
		public void onCancel(BulletinStore store, Bulletin b)
		{
			try
			{
				store.destroyBulletin(b);
			}
			catch (IOException e)
			{
				// TODO Notify user of the error?
				e.printStackTrace();
			}
		}
	}

	Bulletin bulletin;
	UiMainWindow observer;

	UiBulletinComponent view;
	JScrollPane scroller;

	JButton send;
	JButton draft;
	JButton cancel;

	boolean wasBulletinSavedFlag;
	CancelHandler cancelHandler;
}

