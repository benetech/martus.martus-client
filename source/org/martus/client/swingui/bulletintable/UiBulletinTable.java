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

package org.martus.client.swingui.bulletintable;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
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
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.martus.client.core.BulletinFolder;
import org.martus.client.core.BulletinStore;
import org.martus.client.core.MartusApp;
import org.martus.client.core.TransferableBulletinList;
import org.martus.client.core.BulletinStore.StatusNotAllowedException;
import org.martus.client.swingui.UiClipboardUtilities;
import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiBulletinModifyDlg.CancelHandler;
import org.martus.client.swingui.dialogs.UiBulletinModifyDlg.DeleteBulletinOnCancel;
import org.martus.client.swingui.dialogs.UiBulletinModifyDlg.DoNothingOnCancel;
import org.martus.client.swingui.foldertree.FolderNode;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.database.DatabaseKey;
import org.martus.common.packet.UniversalId;
import org.martus.swing.UiNotifyDlg;


public class UiBulletinTable extends JTable implements ListSelectionListener, DragGestureListener, DragSourceListener
{
    public UiBulletinTable(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
		model = new BulletinTableModel(mainWindow.getLocalization());
		setModel(model);

		// set widths for first two columns (status and date)
		setColumnWidthToHeaderWidth(0);
		setColumnWidthToHeaderWidth(1);

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

	public BulletinStore getStore()
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
		Bulletin b = getSingleSelectedBulletin();
		if(b == null)
			return;

		boolean createClone = false;
		String bulletinAccountId = b.getAccount();
		String myAccountId = mainWindow.getApp().getAccountId();
		if(myAccountId.equals(bulletinAccountId))
		{
			if(!b.isDraft())
			{
				if(!mainWindow.confirmDlg(mainWindow, "CloneMySealedAsDraft"))
					return;
				createClone = true;
			}
		}
		else
		{
			if(!mainWindow.confirmDlg(mainWindow, "CloneBulletinAsMine"))
				return;
			createClone = true;
		}
		
		if(b.hasUnknownTags() || b.hasUnknownCustomField())
			if(!mainWindow.confirmDlg(mainWindow, "EditBulletinWithUnknownTags"))
				return;

		BulletinStore store = mainWindow.getApp().getStore();
		CancelHandler handler = new DoNothingOnCancel();
		if(createClone)
		{
			Bulletin clone = store.createEmptyBulletin();
			try
			{
				clone.createDraftCopyOf(b, store.getDatabase());
				store.saveBulletin(clone);
				DatabaseKey key = DatabaseKey.createKey(clone.getUniversalId(),clone.getStatus());
				b = store.loadFromDatabase(key);
				handler = new DeleteBulletinOnCancel();
			}
			catch (Exception e)
			{
				mainWindow.notifyDlg(mainWindow, "UnexpectedError");
				return;
			}
		}

		mainWindow.modifyBulletin(b, handler);
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
			File file = UiClipboardUtilities.getClipboardTransferableFile();
			try
			{
				if(file != null)
					dropAdapter.attemptDropFile(file, folder);
				worked = true;
				if(confirmDeletionOfFile(file.getPath()))
					file.delete();
			}
			catch (StatusNotAllowedException e)
			{
				resultMessageTag = "PasteErrorNotAllowed";
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
			catch (StatusNotAllowedException e)
			{
				resultMessageTag = "PasteErrorNotAllowed";
			}
		}

		if(!worked)
		{
			Toolkit.getDefaultToolkit().beep();
			mainWindow.notifyDlg(mainWindow, resultMessageTag);
		}
		
		mainWindow.resetCursor(cursor);	
	}

	public boolean confirmDeletionOfFile(String filePath)
	{
		UiLocalization localization = mainWindow.getLocalization();
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

		Vector visibleFoldersContainingThisBulletin = app.findBulletinInAllVisibleFolders(b);
		visibleFoldersContainingThisBulletin.remove(folderToDiscardFrom);

		String dialogTag = "";
		if(visibleFoldersContainingThisBulletin.size() > 0)
			dialogTag = "confirmDeleteDiscardedBulletinWithCopies";
		else if (b.isSealed())
			dialogTag = "confirmDiscardSealedBulletins";
		else if(draftOutBox.contains(b))
			dialogTag = "confirmDeleteDiscardedDraftBulletinWithOutboxCopy";
		else
			dialogTag = "confirmDiscardDraftBulletins";

		return confirmDeleteBulletins(dialogTag, visibleFoldersContainingThisBulletin);
	}

	private boolean confirmDiscardMultipleBulletins()
	{
		BulletinFolder folderToDiscardFrom = getFolder();
		if(!isDiscardedFolder(folderToDiscardFrom))
			return true;

		MartusApp app = mainWindow.getApp();
		Vector visibleFoldersContainingAnyBulletin = new Vector();
		Bulletin[] bulletins = getSelectedBulletins();
		for (int i = 0; i < bulletins.length; i++)
		{
			Bulletin b = bulletins[i];
			Vector visibleFoldersContainingThisBulletin = app.findBulletinInAllVisibleFolders(b);
			visibleFoldersContainingThisBulletin.remove(folderToDiscardFrom);
			addUniqueEntriesOnly(visibleFoldersContainingAnyBulletin, visibleFoldersContainingThisBulletin);
		}

		String dialogTag = "";
		if(visibleFoldersContainingAnyBulletin.size() > 0)
			dialogTag = "confirmDeleteMultipleDiscardedBulletinsWithCopies";
		else
			dialogTag = "confirmDeleteMultipleDiscardedBulletins";

		return confirmDeleteBulletins(dialogTag, visibleFoldersContainingAnyBulletin);
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

	private boolean confirmDeleteBulletins(String dialogTag, Vector foldersToList)
	{
		UiLocalization localization = mainWindow.getLocalization();
		String title = localization.getWindowTitle(dialogTag);
		String cause = localization.getFieldLabel(dialogTag + "cause");
		String folders = buildFolderNameList(foldersToList);
		String effect = localization.getFieldLabel(dialogTag + "effect");
		String question = localization.getFieldLabel("confirmquestion");
		String[] contents = {cause, "", effect, folders, "", question};
		return mainWindow.confirmDlg(mainWindow, title, contents);
	}

	private String buildFolderNameList(Vector visibleFoldersContainingThisBulletin)
	{
		UiLocalization localization = mainWindow.getLocalization();
		String names = "";
		for(int i = 0 ; i < visibleFoldersContainingThisBulletin.size() ; ++i)
		{
			BulletinFolder thisFolder = (BulletinFolder)visibleFoldersContainingThisBulletin.get(i);
			FolderNode node = new FolderNode(thisFolder.getName(), localization);
			names += node.getLocalizedName() + "\n";
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

	private void setColumnWidthToHeaderWidth(int colIndex)
	{
		TableColumnModel columnModel = getColumnModel();
		TableColumn statusColumn = columnModel.getColumn(colIndex);
		String padding = "    ";
		String value = (String)statusColumn.getHeaderValue() + padding;

		TableCellRenderer renderer = statusColumn.getHeaderRenderer();
		if(renderer == null)
		{
			JTableHeader header = getTableHeader();
			renderer = header.getDefaultRenderer();
		}
		Component c = renderer.getTableCellRendererComponent(this, value, true, true, -1, colIndex);
		Dimension size = c.getPreferredSize();

		statusColumn.setPreferredWidth(size.width);
		statusColumn.setMaxWidth(size.width);
	}


	UiMainWindow mainWindow;
	BulletinTableModel model;
	private DragSource dragSource = DragSource.getDefaultDragSource();
	private UiBulletinTableDropAdapter dropAdapter;
	TableKeyAdapter keyListener;
}
