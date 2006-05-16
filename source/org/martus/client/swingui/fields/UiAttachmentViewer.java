/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2006, Beneficent
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

package org.martus.client.swingui.fields;

import java.awt.Component;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.ListSelectionModel;
import org.martus.client.core.MartusApp;
import org.martus.client.core.TransferableAttachmentList;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.tablemodels.AttachmentTableModel;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.database.ReadableDatabase;
import org.martus.swing.UiButton;
import org.martus.swing.UiFileChooser;
import org.martus.swing.UiParagraphPanel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiTable;
import org.martus.swing.UiVBox;
import org.martus.swing.Utilities;

public class UiAttachmentViewer extends UiParagraphPanel  implements DragGestureListener, DragSourceListener
{
	public UiAttachmentViewer(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
		app = mainWindow.getApp();
		model = new AttachmentTableModel(mainWindow, attachmentTable);

		attachmentTable = new UiTable(model);
		attachmentTable.setMaxGridWidth(UiMainWindow.MINIMUM_TEXT_FIELD_WIDTH);
		attachmentTable.useMaxWidth();
		attachmentTable.createDefaultColumnsFromModel();
		attachmentTable.setColumnSelectionAllowed(false);
		attachmentTable.setMaxColumnWidthToHeaderWidth(1);

		MartusLocalization localization = mainWindowToUse.getLocalization();

		attachmentPane = new UiScrollPane(attachmentTable, UiScrollPane.VERTICAL_SCROLLBAR_NEVER, UiScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		saveButton = new UiButton(localization.getButtonLabel("saveattachment"));
		saveButton.addActionListener(new SaveHandler());
		saveButton.setEnabled(false);
		
		viewButton = new UiButton(localization.getButtonLabel("viewattachment"));
		viewButton.addActionListener(new ViewHandler());
		viewButton.setEnabled(false);
		if(!Utilities.isMSWindows())
			viewButton.setVisible(false);

		UiVBox vbox = new UiVBox();
		vbox.add(attachmentPane);
		vbox.add(new Component[] {saveButton, viewButton});
		add(vbox);

		updateTable();
		attachmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		dragSource.createDefaultDragGestureRecognizer(attachmentTable,
							DnDConstants.ACTION_COPY_OR_MOVE, this);
	}

	public void updateTable()
	{
		attachmentTable.resizeTable();
		int rowCount = model.getRowCount();
		saveButton.setEnabled(rowCount > 0);
		viewButton.setEnabled(rowCount > 0);
	}
	public void addAttachment(AttachmentProxy a)
	{
		model.add(a);
		updateTable();
	}

	public void clearAttachments()
	{
		model.clear();
		updateTable();
	}


	public int GetSelection()
	{
		int selection = attachmentTable.getSelectedRow();
		int rowCount = attachmentTable.getRowCount();
		if(selection > rowCount || rowCount <= 0)
			return -1;

		if(selection == -1)
		{
			if(rowCount == 1)
				selection = 0;
			else
			{
				getToolkit().beep();
				return -1;
			}
		}
		return selection;
	}

	public String extractFileNameOnly(String fullName)
	{
		int index = fullName.lastIndexOf('.');
		if(index == -1)
			index = fullName.length();
		String fileNameOnly = fullName.substring(0, index);
		while(fileNameOnly.length() < 3)
		{
			fileNameOnly += "_";	
		}
		return fileNameOnly;
	}

	public String extractExtentionOnly(String fullName)
	{
		int index = fullName.lastIndexOf('.');
		if(index == -1)
			return null;
		return fullName.substring(index, fullName.length());
	}

	static void setLastAttachmentSaveDirectory(File lastAttachmentSaveDirectory)
	{
		UiAttachmentViewer.lastAttachmentSaveDirectory =
			lastAttachmentSaveDirectory;
	}

	static File getLastAttachmentSaveDirectory()
	{
		return lastAttachmentSaveDirectory;
	}

	class ViewHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			int selectedRow = GetSelection();
			if(selectedRow == -1)
				return;
			AttachmentProxy proxy = model.getAttachmentProxyAt(selectedRow);
			String author = proxy.getUniversalId().getAccountId();
			if(!author.equals(mainWindow.getApp().getAccountId()))
			{
				if(!mainWindow.confirmDlg("NotYourBulletinViewAttachmentAnyways"))
					return;
			}
			String fileName = model.getFilenameAt(selectedRow);
			mainWindow.setWaitingCursor();
			try
			{
				File temp = File.createTempFile(extractFileNameOnly(fileName), extractExtentionOnly(fileName));
				temp.deleteOnExit();
			
				ReadableDatabase db = mainWindow.getApp().getStore().getDatabase();
				BulletinLoader.extractAttachmentToFile(db, proxy, app.getSecurity(), temp);

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
	}
	
	class SaveHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			int selectedRow = GetSelection();
			if(selectedRow == -1)
				return;
			String fileName = model.getFilenameAt(selectedRow);

			File last = getLastAttachmentSaveDirectory();
			if(last == null)
				last = UiFileChooser.getHomeDirectoryFile();
			File attachmentFileToSave = new File(last, fileName);
			UiFileChooser.FileDialogResults results = UiFileChooser.displayFileSaveDialog(mainWindow, null, attachmentFileToSave);
			if(results.wasCancelChoosen())
				return;
			setLastAttachmentSaveDirectory(results.getCurrentDirectory());
			File outputFile = results.getChosenFile();
			if(outputFile.exists())
			{
				if(!mainWindow.confirmDlg("OverWriteExistingFile"))
					return;
			}
			mainWindow.setWaitingCursor();
			AttachmentProxy proxy = model.getAttachmentProxyAt(selectedRow);
			try
			{
				ReadableDatabase db = mainWindow.getApp().getStore().getDatabase();
				BulletinLoader.extractAttachmentToFile(db, proxy, app.getSecurity(), outputFile);
			}
			catch(Exception e)
			{
				mainWindow.notifyDlg("UnableToSaveAttachment");
				System.out.println("Unable to save file :" + e);
			}
			mainWindow.resetCursor();
		}
	}

	public void dragGestureRecognized(DragGestureEvent dge)
	{
		AttachmentProxy[] attachments = model.getSelectedAttachments();
		if(attachments == null)
			return;
		TransferableAttachmentList dragable = new TransferableAttachmentList(mainWindow.getStore().getDatabase(), mainWindow.getApp().getSecurity(), attachments);
		dge.startDrag(DragSource.DefaultCopyDrop, dragable, this);
	}

	public void dragEnter(DragSourceDragEvent dsde)
	{
	}

	public void dragOver(DragSourceDragEvent dsde)
	{
	}

	public void dropActionChanged(DragSourceDragEvent dsde)
	{
	}

	public void dragDropEnd(DragSourceDropEvent dsde)
	{
	}

	public void dragExit(DragSourceEvent dse)
	{
	}
	

	
	UiMainWindow mainWindow;
	MartusApp app;
	AttachmentTableModel model;
	UiTable attachmentTable;
	public JButton saveButton;
	public JButton viewButton;
	UiScrollPane attachmentPane;

	private static File lastAttachmentSaveDirectory;
	private DragSource dragSource = DragSource.getDefaultDragSource();

	static final int VISIBLE_ROW_COUNT = 4;

}
