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
import javax.swing.JViewport;
import org.martus.client.core.BulletinFolder;
import org.martus.client.core.ClientBulletinStore;
import org.martus.client.core.EncryptionChangeListener;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.UiScrollPane;
import org.martus.client.swingui.bulletincomponent.UiBulletinComponent;
import org.martus.client.swingui.bulletincomponent.UiBulletinComponentEditorSection;
import org.martus.client.swingui.bulletincomponent.UiBulletinEditor;
import org.martus.client.swingui.fields.UiDateEditor;
import org.martus.client.swingui.fields.UiFlexiDateEditor;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.crypto.MartusCrypto.EncryptionException;
import org.martus.swing.Utilities;

public class UiBulletinModifyDlg extends JFrame implements ActionListener, WindowListener, EncryptionChangeListener
{
	public UiBulletinModifyDlg(Bulletin b, CancelHandler cancelHandlerToUse, UiMainWindow observerToUse)
	{
		observer = observerToUse;
		cancelHandler = cancelHandlerToUse;

		UiBasicLocalization localization = observer.getLocalization();
		setTitle(localization.getWindowTitle("create"));
		UiMainWindow.updateIcon(this);
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

			scroller = new UiScrollPane(localization.getComponentOrientation());
			scroller.getVerticalScrollBar().setFocusable(false);
			scroller.getViewport().add(view);
			scroller.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

			if(observer.getBulletinsAlwaysPrivate())
				view.encryptAndDisableAllPrivate();
			else
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
		try
		{
			if(ae.getSource() == cancel)
			{				
				closeWindowIfUserConfirms();
				return;
			}	
	
			if(!validateData())
				return;

			boolean userChoseSeal = (ae.getSource() == send);
			
			if(userChoseSeal)
			{
				String tag = view.isAllPrivateBoxChecked() ? 
						"send" : "SendWithPublicData";
					
				if (!observer.confirmDlg(this, tag))
					return;
			}
												
			saveBulletin(userChoseSeal);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			observer.notifyDlg(this, "UnexpectedError");
		}
	}

	private boolean validateData()
	{
		try
		{	
			view.validateData();
			return true;
		}
		catch(UiDateEditor.DateFutureException e)
		{
			observer.messageDlg(this,"ErrorDateInFuture", e.getlocalizedTag());
		}
		catch(UiFlexiDateEditor.DateRangeInvertedException e)
		{
			observer.messageDlg(this,"ErrorDateRangeInverted", e.getlocalizedTag());
		}
		catch(UiBulletinComponentEditorSection.AttachmentMissingException e)
		{
			observer.messageDlg(this,"ErrorAttachmentMissing", e.getlocalizedTag());
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			observer.notifyDlg(this, "UnexpectedError");
		}
		return false;
	}

	private void saveBulletin(boolean userChoseSeal)
	{
		Cursor originalCursor = getCursor();
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try 
		{
			MartusApp app = observer.getApp();
			ClientBulletinStore store = app.getStore();
			BulletinFolder outboxToUse = null;
			BulletinFolder draftOutbox = store.getFolderDraftOutbox();

			// NOTE: must copyDataToBulletin before setSealed or setDraft
			// NOTE: after copyDataToBulletin, should not allow user to cancel
			view.copyDataToBulletin(bulletin);
			if(userChoseSeal)
			{
				store.removeBulletinFromFolder(draftOutbox, bulletin);
				
				bulletin.setSealed();
				outboxToUse = store.getFolderSealedOutbox();
			}
			else
			{
				bulletin.setDraft();
				outboxToUse = draftOutbox;
			}
			
			app.setHQKeysInBulletin(bulletin);
			saveBulletinAndUpdateFolders(store, outboxToUse);
			wasBulletinSavedFlag = true;
			cleanupAndExit();
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

	private void saveBulletinAndUpdateFolders(ClientBulletinStore store, BulletinFolder outboxToUse) throws IOException, CryptoException
	{
		observer.getApp().saveBulletin(bulletin, outboxToUse);

		observer.folderContentsHaveChanged(store.getFolderSaved());
		observer.folderContentsHaveChanged(store.getFolderDiscarded());
		observer.selectBulletinInCurrentFolderIfExists(bulletin.getUniversalId());
		observer.bulletinContentsHaveChanged(bulletin);
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
		try
		{
			closeWindowIfUserConfirms();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			observer.notifyDlg(this, "UnexpectedError");
		}
	}
	// end WindowListener interface


	public void encryptionChanged(boolean newState)
	{
		indicateEncrypted(newState);
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

	private void indicateEncrypted(boolean isEncrypted)
	{
		view.updateEncryptedIndicator(isEncrypted);
	}

	private void closeWindowIfUserConfirms() throws EncryptionException, IOException
	{	
		boolean needConfirmation = view.isBulletinModified();
		if(needConfirmation)
		{
			if(!observer.confirmDlg(this, "CancelModifyBulletin"))
				return;
		}
			
		cancelHandler.onCancel(observer.getStore(), bulletin);
		cleanupAndExit();
	}

	public interface CancelHandler
	{
		public void onCancel(ClientBulletinStore store,Bulletin b);
	}

	public static class DoNothingOnCancel implements CancelHandler
	{
		public void onCancel(ClientBulletinStore store,Bulletin b)
		{
			// do nothing
		}
	}

	public static class DeleteBulletinOnCancel implements CancelHandler
	{
		public void onCancel(ClientBulletinStore store, Bulletin b)
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
	UiScrollPane scroller;

	JButton send;
	JButton draft;
	JButton cancel;

	boolean wasBulletinSavedFlag;
	CancelHandler cancelHandler;	
}

