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

package org.martus.client.swingui.bulletintable;

import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;

import org.martus.client.core.BulletinFolder;
import org.martus.client.core.ClientBulletinStore;
import org.martus.client.core.MartusApp;
import org.martus.client.core.TransferableBulletinList;
import org.martus.client.core.ClientBulletinStore.BulletinAlreadyExistsException;
import org.martus.client.swingui.UiClipboardUtilities;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiBulletinModifyDlg;
import org.martus.client.swingui.dialogs.UiBulletinModifyDlg.DeleteBulletinOnCancel;
import org.martus.client.swingui.dialogs.UiBulletinModifyDlg.DoNothingOnCancel;
import org.martus.client.swingui.foldertree.FolderNode;
import org.martus.common.FieldSpec;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.common.packet.UniversalId;
import org.martus.swing.UiNotifyDlg;
import org.martus.swing.UiTable;


public class UiBulletinTable extends UiTable implements ListSelectionListener, DragGestureListener, DragSourceListener
{
    public UiBulletinTable(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
		model = new BulletinTableModel(mainWindow.getLocalization());
		setModel(model);

		// set widths for first three columns (status, wassent, and date)
		UiTable.setColumnWidthToHeaderWidth(this, COLUMN_STATUS);
		UiTable.setColumnWidthToHeaderWidth(this, COLUMN_SENT);
		UiTable.setColumnWidthToHeaderWidth(this, COLUMN_EVENTDATE);
//		setColumnWidthToHeaderWidth(this, 4);
		
		addMouseListener(new TableMouseAdapter());
		keyListener = new TableKeyAdapter();
		addKeyListener(keyListener);
		getTableHeader().setReorderingAllowed(false);
		getTableHeader().addMouseListener(new TableHeaderMouseAdapter());

		int mode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
		getSelectionModel().setSelectionMode(mode);
		getSelectionModel().addListSelectionListener(this);

		dragSource.createDefaultDragGestureRecognizer(this,
							DnDConstants.ACTION_COPY_OR_MOVE, this);

		dropAdapter = new UiBulletinTableDropAdapter(this, mainWindow);
	}

	public UiBulletinTableDropAdapter getDropAdapter()
	{
		return dropAdapter;
	}

	public ClientBulletinStore getStore()
	{
		return mainWindow.getStore();
	}

	public BulletinFolder getFolder()
	{
		return model.getFolder();
	}

	public void setFolder(BulletinFolder folder)
	{
		model.setFolder(folder);
	}

	public Bulletin[] getSelectedBulletins()
	{
		int[] selectedRows = getSelectedRows();
		Bulletin[] bulletins = new Bulletin[selectedRows.length];

		for (int row = 0; row < selectedRows.length; row++)
		{
			bulletins[row] = model.getBulletin(selectedRows[row]);
		}

		return bulletins;
	}
	
	public int getBulletinCount()
	{
		return getRowCount();
	}


	public UniversalId[] getSelectedBulletinUids()
	{
		int[] selectedRows = getSelectedRows();
		UniversalId[] bulletinUids = new UniversalId[selectedRows.length];

		for (int row = 0; row < selectedRows.length; row++)
		{
			bulletinUids[row] = model.getBulletinUid(selectedRows[row]);
		}

		return bulletinUids;
	}

	public Bulletin getSingleSelectedBulletin()
	{
		Bulletin[] selected = getSelectedBulletins();
		Bulletin b = null;
		if(selected.length == 1)
		{
			b = selected[0];
		}
		return b;
	}

	public void selectBulletin(Bulletin b)
	{
		selectRow(model.findBulletin(b.getUniversalId()));
	}

	public void selectBulletins(UniversalId[] uids)
	{
		clearSelection();
		for (int i = 0; i < uids.length; i++)
		{
			int row = model.findBulletin(uids[i]);
			if(row >= 0)
				addRowSelectionInterval(row, row);
		}
	}

	public void doSelectAllBulletins()
	{
		selectAll();
	}


	public void bulletinContentsHaveChanged(Bulletin b)
	{
		UniversalId[] selected = getSelectedBulletinUids();
		tableChanged(new TableModelEvent(model));
		selectBulletins(selected);
	}
	

	public Object getValueAt(int row, int column)
	{
		if(column == COLUMN_SENT && !mainWindow.getApp().isServerConfigured())
			return "";
		return super.getValueAt(row, column);
	}
	
	// ListSelectionListener interface
	public void valueChanged(ListSelectionEvent e)
	{
		if(!e.getValueIsAdjusting() && model != null)
		{
			mainWindow.bulletinSelectionHasChanged();
		}
		repaint();
	}

	// DragGestureListener interface
	public void dragGestureRecognized(DragGestureEvent dragGestureEvent)
	{
		if (getSelectedRowCount() == 0)
			return;

		Bulletin[] bulletinsBeingDragged = getSelectedBulletins();
		TransferableBulletinList dragable = new TransferableBulletinList(getStore(), bulletinsBeingDragged, getFolder());
		dragGestureEvent.startDrag(DragSource.DefaultCopyDrop, dragable, this);
	}

	// DragSourceListener interface
	// we don't care when we enter or exit a drop target
	public void dragEnter (DragSourceDragEvent dsde)						{}
	public void dragExit(DragSourceEvent DragSourceEvent)					{}
	public void dragOver(DragSourceDragEvent DragSourceDragEvent)			{}
	public void dropActionChanged(DragSourceDragEvent DragSourceDragEvent)	{}
	public void dragDropEnd(DragSourceDropEvent dsde)						{}

	public void doModifyBulletin()
	{
		Bulletin original = getSingleSelectedBulletin();
		if(original == null)
			return;

		String myAccountId = mainWindow.getApp().getAccountId();
		boolean isMine = myAccountId.equals(original.getAccount());
		boolean isSealed = original.isSealed();
		
		if(!isMine)
		{
			if(!mainWindow.confirmDlg("CloneBulletinAsMine"))
				return;
		}
		
		if(isMine && isSealed)
		{
			if(!mainWindow.confirmDlg("CloneMySealedAsDraft"))
				return;
		}
		
		if(original.hasUnknownTags() || original.hasUnknownCustomField())
		{
			if(!mainWindow.confirmDlg("EditBulletinWithUnknownTags"))
				return;
		}
		
		Bulletin bulletinToModify = original;
		UiBulletinModifyDlg.CancelHandler handler = new DoNothingOnCancel();
		
		if(isSealed || !isMine)
		{
			handler = new DeleteBulletinOnCancel();

			ClientBulletinStore store = mainWindow.getApp().getStore();
			FieldSpec[] publicFieldSpecsToUse = store.getPublicFieldSpecs();
			FieldSpec[] privateFieldSpecsToUse = store.getPrivateFieldSpecs();

			if(store.bulletinHasExtraFields(original))
			{
				if(mainWindow.confirmDlg(mainWindow, "UseBulletinsCustomFields"))
				{
					publicFieldSpecsToUse = original.getPublicFieldSpecs();
					privateFieldSpecsToUse = original.getPrivateFieldSpecs();
				}
			}

			try
			{
				bulletinToModify = store.createClone(original, publicFieldSpecsToUse, privateFieldSpecsToUse);
			}
			catch (Exception e)
			{
				mainWindow.notifyDlg("UnexpectedError");
				return;
			}
		}

		mainWindow.modifyBulletin(bulletinToModify, handler);
	}

	public void doCutBulletins()
	{
		doCopyBulletins();
		doDiscardBulletins();
	}

	public void doCopyBulletins()
	{				
		Cursor cursor = mainWindow.setWaitingCursor();
					
		Bulletin[] selected = getSelectedBulletins();
		BulletinFolder folder = getFolder();
		TransferableBulletinList tb = new TransferableBulletinList(getStore(), selected, folder);

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Clipboard clipboard = toolkit.getSystemClipboard();
		Transferable contents = clipboard.getContents(this);
		mainWindow.lostOwnership(clipboard, contents);

		clipboard.setContents(tb, mainWindow);
		mainWindow.resetCursor(cursor);		
	}

	public void doPasteBulletins()
	{				 
		Cursor cursor = mainWindow.setWaitingCursor();
		
		BulletinFolder folder = getFolder();
		TransferableBulletinList tb = UiClipboardUtilities.getClipboardTransferableBulletin();

		boolean worked = false;
		String resultMessageTag = null;
			
		if(tb == null)
		{			
			File[] files = UiClipboardUtilities.getClipboardTransferableFiles();
			try
			{
				if(files != null)
					dropAdapter.attemptDropFiles(files, folder);
				worked = true;
				mainWindow.notifyDlg("OperationCompleted");
				mainWindow.notifyDlg("FilesWillNotBeDeleted");
				//if(confirmDeletionOfFile(file.getPath()))
					//file.delete();
			}
			catch (BulletinAlreadyExistsException e)
			{
				resultMessageTag = "PasteErrorBulletinAlreadyExists";
			}
			catch (Exception e)
			{
				resultMessageTag = "PasteError";
			}
		}
		else
		{
			try
			{
				dropAdapter.attemptDropBulletins(tb.getBulletins(), folder);
				worked = true;
			}
			catch (BulletinAlreadyExistsException e)
			{
				resultMessageTag = "PasteErrorBulletinAlreadyExists";
			}
			catch (IOException e)
			{
				resultMessageTag = "PasteError";
			}
		}

		if(!worked)
			mainWindow.notifyDlgBeep(resultMessageTag);
		
		mainWindow.resetCursor(cursor);	
	}
	
	public void doResendBulletins()
	{
		BulletinFolder draftOutBox = getStore().getFolderDraftOutbox();
		BulletinFolder sealedOutBox = getStore().getFolderSealedOutbox();

		Bulletin[] selected = getSelectedBulletins();
		boolean notAllowedToSend = false;
		boolean errorIO = false;
		for (int i = 0; i < selected.length; i++)
		{
			try
			{
				Bulletin bulletin = selected[i];
				String accountId = getStore().getAccountId();
				if(!bulletin.getBulletinHeaderPacket().isAuthorizedToUpload(accountId))
				{
					notAllowedToSend = true;
					continue;
				}

				if(bulletin.isDraft())
					draftOutBox.add(bulletin);
				if(bulletin.isSealed())
					sealedOutBox.add(bulletin);
				
			}
			catch (BulletinAlreadyExistsException harmless)
			{
			}
			catch (IOException e)
			{
				errorIO = true;
				e.printStackTrace();
			}
		}
		if(notAllowedToSend)
			mainWindow.notifyDlg("ResendErrorNotAuthorizedToSend");
		if(errorIO)
			mainWindow.notifyDlg("ResendError");
	}

	public boolean confirmDeletionOfFile(String filePath)
	{
		UiBasicLocalization localization = mainWindow.getLocalization();
		String title = localization.getWindowTitle("DeleteBulletinFile");
		String msg1 = localization.getFieldLabel("DeleteBulletinFileMsg1");
		String msg2 = localization.getFieldLabel("DeleteBulletinFileMsg2");
		String[] contents = {msg1, filePath, msg2};

		String delete = localization.getButtonLabel("Delete");
		String leave = localization.getButtonLabel("Leave");
		String[] buttons = {delete, leave};

		UiNotifyDlg notify = new UiNotifyDlg(mainWindow, title, contents, buttons);
		String result = notify.getResult();
		if(result != null && result.equals(delete))
			return true;
		return false;
	}

	class TableHeaderMouseAdapter extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
	
			if(!e.isPopupTrigger())
			{
				JTableHeader header = (JTableHeader)e.getSource();
				int col = header.columnAtPoint(e.getPoint());
				model.sortByColumn(col);
				mainWindow.folderContentsHaveChanged(getFolder());
			}
		}
	}

	class TableKeyAdapter extends KeyAdapter
	{
		public void keyReleased(KeyEvent e)
		{
			if(e.getKeyCode() == KeyEvent.VK_DELETE)
			{
				removeKeyListener(keyListener);
				doDiscardBulletins();
				DelayAddListener delay = new DelayAddListener();
				delay.start();
			}
			if(e.isControlDown())
			{

				if(e.getKeyCode() == KeyEvent.VK_A)
					doSelectAllBulletins();
				if(e.getKeyCode() == KeyEvent.VK_X)
					doCutBulletins();
				if(e.getKeyCode() == KeyEvent.VK_C)
					doCopyBulletins();
				if(e.getKeyCode() == KeyEvent.VK_V)
					doPasteBulletins();		
			}
		}
	}

	class DelayAddListener extends Thread
	{
		public DelayAddListener()
		{
		}

		public void run()
		{
			try
			{
				sleep(500);
			}
			catch(InterruptedException e)
			{
			}
			finally
			{
				addKeyListener(keyListener);
			}
		}
	}



	class TableMouseAdapter extends MouseAdapter
	{
		public void mousePressed(MouseEvent e)
		{
			if(e.isPopupTrigger())
				handleRightClick(e);
		}

		public void mouseReleased(MouseEvent e)
		{
			if(e.isPopupTrigger())
				handleRightClick(e);
		}

		public void mouseClicked(MouseEvent e)
		{
			if(e.getClickCount() == 2)
				handleDoubleClick(e);
		}


		private void handleRightClick(MouseEvent e)
		{
			int thisRow = rowAtPoint(e.getPoint());
			boolean isInsideSelection = isRowSelected(thisRow);
			if(!isInsideSelection && !e.isShiftDown() && !e.isControlDown())
				selectRow(thisRow);

			JPopupMenu menu = mainWindow.getPopupMenu();
			menu.show(UiBulletinTable.this, e.getX(), e.getY());
		}

		private void handleDoubleClick(MouseEvent e)
		{
			doModifyBulletin();
		}
	}



	public void doDiscardBulletins()
	{
		boolean okToDiscard = true;
		Bulletin[] bulletins = getSelectedBulletins();
		if(bulletins.length == 0 || bulletins[0] == null)
			return;
					
		if(bulletins.length == 1)
		{
			okToDiscard = confirmDiscardSingleBulletin(bulletins[0]);
		}
		else
		{
			okToDiscard = confirmDiscardMultipleBulletins();
		}

		if(okToDiscard)
		{						
			discardAllSelectedBulletins();			
		}
		
	}

	private void discardAllSelectedBulletins()
	{		
		Cursor cursor = mainWindow.setWaitingCursor();
		Bulletin[] bulletinsToDiscard = getSelectedBulletins();
		BulletinFolder folderToDiscardFrom = getFolder();
		MartusApp app = mainWindow.getApp();
		try
		{
			app.discardBulletinsFromFolder(folderToDiscardFrom, bulletinsToDiscard);
		}
		catch (IOException e)
		{
			// TODO Notify user of an error
			e.printStackTrace();
		}

		BulletinFolder discardedFolder = app.getFolderDiscarded(); 
		folderToDiscardFrom.getStore().saveFolders();
		mainWindow.folderContentsHaveChanged(folderToDiscardFrom);
		mainWindow.folderContentsHaveChanged(discardedFolder);
		mainWindow.selectNewCurrentBulletin(getSelectedRow());
		mainWindow.resetCursor(cursor);			
	}

	private boolean confirmDiscardSingleBulletin(Bulletin b)
	{
		BulletinFolder folderToDiscardFrom = getFolder();
		if(!isDiscardedFolder(folderToDiscardFrom))
			return true;

		MartusApp app = mainWindow.getApp();
		BulletinFolder draftOutBox = app.getFolderDraftOutbox();
		BulletinFolder sealedOutBox = app.getFolderSealedOutbox();

		Vector visibleFoldersContainingThisBulletin = app.findBulletinInAllVisibleFolders(b);
		visibleFoldersContainingThisBulletin.remove(folderToDiscardFrom);


		String tagUnsent = null;
		String tagInOtherFolders = null;
		if(visibleFoldersContainingThisBulletin.size() > 0)
		{
			tagInOtherFolders = "warningDeleteSingleBulletinWithCopies";
		}
		else
		{
			if(draftOutBox.contains(b) || sealedOutBox.contains(b))
				tagUnsent = "warningDeleteSingleUnsentBulletin";
		}
		
		String tagMain = "warningDeleteSingleBulletin";

		return confirmDeleteBulletins(tagMain, tagUnsent, tagInOtherFolders, visibleFoldersContainingThisBulletin);
	}

	private boolean confirmDiscardMultipleBulletins()
	{
		BulletinFolder folderToDiscardFrom = getFolder();
		if(!isDiscardedFolder(folderToDiscardFrom))
			return true;

		MartusApp app = mainWindow.getApp();

		BulletinFolder draftOutBox = app.getFolderDraftOutbox();
		BulletinFolder sealedOutBox = app.getFolderSealedOutbox();

		boolean aBulletinIsUnsent = false;
		Vector visibleFoldersContainingAnyBulletin = new Vector();
		Bulletin[] bulletins = getSelectedBulletins();
		for (int i = 0; i < bulletins.length; i++)
		{
			Bulletin b = bulletins[i];
			Vector visibleFoldersContainingThisBulletin = app.findBulletinInAllVisibleFolders(b);
			visibleFoldersContainingThisBulletin.remove(folderToDiscardFrom);
			addUniqueEntriesOnly(visibleFoldersContainingAnyBulletin, visibleFoldersContainingThisBulletin);
			
			if(draftOutBox.contains(b) || sealedOutBox.contains(b))
				aBulletinIsUnsent = true;
		}

		String tagUnsent = null;
		if(aBulletinIsUnsent)
			tagUnsent = "warningDeleteMultipleUnsentBulletins";

		String tagInOtherFolders = null;
		if(visibleFoldersContainingAnyBulletin.size() > 0)
			tagInOtherFolders = "warningDeleteMultipleBulletinsWithCopies";
		
		String tagMain = "warningDeleteMultipleBulletins";

		return confirmDeleteBulletins(tagMain, tagUnsent, tagInOtherFolders, visibleFoldersContainingAnyBulletin);
	}

	private void addUniqueEntriesOnly(Vector to, Vector from)
	{
		for(int i = 0 ; i < from.size(); ++i)
		{
			Object elementToAdd = from.get(i);
			if(!to.contains(elementToAdd))
				to.add(elementToAdd);
		}
	}

	private boolean confirmDeleteBulletins(String tagMain, String tagUnsent, String tagInOtherFolders, Vector foldersToList)
	{
		UiBasicLocalization localization = mainWindow.getLocalization();
		String title = localization.getWindowTitle(tagMain);

		Vector strings = new Vector();
		strings.add(localization.getFieldLabel(tagMain));
		strings.add("");
		if(tagUnsent != null)
		{
			strings.add(localization.getFieldLabel(tagUnsent));
			strings.add("");
		}
		if(tagInOtherFolders != null)
		{
			strings.add(localization.getFieldLabel(tagInOtherFolders));
			strings.add(buildFolderNameList(foldersToList));
			strings.add("");
		}
		strings.add(localization.getFieldLabel("confirmquestion"));
		String[] contents = new String[strings.size()];
		strings.toArray(contents);
		return mainWindow.confirmDlg(mainWindow, title, contents);
	}

	private String buildFolderNameList(Vector visibleFoldersContainingThisBulletin)
	{
		UiBasicLocalization localization = mainWindow.getLocalization();
		String names = "";
		for(int i = 0 ; i < visibleFoldersContainingThisBulletin.size() ; ++i)
		{
			BulletinFolder thisFolder = (BulletinFolder)visibleFoldersContainingThisBulletin.get(i);
			FolderNode node = new FolderNode(thisFolder.getName(), localization);
			names += " - " + node.getLocalizedName() + "\n";
		}
		return names;
	}

	private boolean isDiscardedFolder(BulletinFolder f)
	{
		return f.equals(f.getStore().getFolderDiscarded());
	}

	void selectRow(int rowIndex)
	{
		if(rowIndex >= 0 && rowIndex < getRowCount())
			setRowSelectionInterval(rowIndex, rowIndex);
	}
	
	static final int COLUMN_STATUS = 0;
	static final int COLUMN_SENT = 1;
	static final int COLUMN_EVENTDATE = 2;

	UiMainWindow mainWindow;
	BulletinTableModel model;
	private DragSource dragSource = DragSource.getDefaultDragSource();
	private UiBulletinTableDropAdapter dropAdapter;
	TableKeyAdapter keyListener;
}
