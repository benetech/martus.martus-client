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

package org.martus.client.swingui.fields;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.martus.client.swingui.UiFocusListener;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.swing.UiFileChooser;
import org.martus.swing.UiParagraphPanel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiTable;
import org.martus.swing.Utilities;



public class UiAttachmentEditor extends UiParagraphPanel
{
	public UiAttachmentEditor(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
		model = new EditorAttachmentTableModel(mainWindow, attachmentTable);

		attachmentTable = new UiTable(model);
		new DropTarget(this, new attachmentDropAdapter());
		attachmentTable.setFocusable(false);
		attachmentTable.createDefaultColumnsFromModel();
		attachmentTable.setColumnSelectionAllowed(false);
		UiTable.setMaxColumnWidthToHeaderWidth(attachmentTable,1);

		Box hbox = Box.createHorizontalBox();
		Box vbox = Box.createVerticalBox();

		UiBasicLocalization localization = mainWindowToUse.getLocalization();
		UiScrollPane scrollPane = new UiScrollPane(attachmentTable);
		scrollPane.getHorizontalScrollBar().setFocusable(false);
		scrollPane.getVerticalScrollBar().setFocusable(false);
		vbox.add(scrollPane);

		JButton add = new JButton(localization.getButtonLabel("addattachment"));
	
		add.addFocusListener(new UiFocusListener(this));		
		add.addActionListener(new AddHandler());
		remove = new JButton(localization.getButtonLabel("removeattachment"));
		remove.addFocusListener(new UiFocusListener(this));		
		remove.addActionListener(new RemoveHandler());
		remove.setEnabled(false);
		Component buttons[] = {add, remove, Box.createHorizontalGlue()};
		Utilities.addComponentsRespectingOrientation(hbox, buttons);
		vbox.add(hbox);
		add(vbox);

		attachmentTable.resizeTable(VISIBLE_ROW_COUNT);
	}

	class attachmentDropAdapter implements DropTargetListener
	{
		public void dragEnter(DropTargetDragEvent dtde)
		{
		}

		public void dragOver(DropTargetDragEvent dtde)
		{
			if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
				dtde.acceptDrag(dtde.getDropAction());
			else
				dtde.rejectDrag();
		}

		public void dropActionChanged(DropTargetDragEvent dtde)
		{
		}

		public void drop(DropTargetDropEvent dtde)
		{
			if(!dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			{
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

			for(int i = 0; i<list.size(); ++i)
			{	
				File file = (File)list.get(i);
				setLastAttachmentLoadDirectory(file.getAbsoluteFile());
				AttachmentProxy a = new AttachmentProxy(file);
				model.add(a);
			}
			dtde.dropComplete(true);
		}

		public void dragExit(DropTargetEvent dte)
		{
		}
	}
	
	public JComponent[] getFocusableComponents()
	{
		return new JComponent[]{attachmentTable};
	}

	public AttachmentProxy[] getAttachments()
	{
		return model.getAttachments();
	}

	public void addAttachment(AttachmentProxy a)
	{
		model.add(a);
	}

	public void clearAttachments()
	{
		model.clear();
	}

	static void setLastAttachmentLoadDirectory(File lastAttachmentLoadDirectory)
	{
		UiAttachmentEditor.lastAttachmentLoadDirectory =
			lastAttachmentLoadDirectory;
	}

	static File getLastAttachmentLoadDirectory()
	{
		return lastAttachmentLoadDirectory;
	}

	class EditorAttachmentTableModel extends AttachmentTableModel
	{

		public EditorAttachmentTableModel(UiMainWindow window, UiTable table)
		{
			super(window, table);
		}

		void clear()
		{
			super.clear();
			remove.setEnabled(false);
		}

		public void add(AttachmentProxy a)
		{
			super.add(a);
			remove.setEnabled(true);
		}
		
		public void remove(int row)
		{
			super.remove(row);
			if(getRowCount() == 0)
				remove.setEnabled(false);
		}
	}

	class AddHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			File last = getLastAttachmentLoadDirectory();
			if(last == null)
				last = UiFileChooser.getHomeDirectoryFile();
			
			UiBasicLocalization localization = mainWindow.getLocalization();
			String buttonLabel = localization.getButtonLabel("addattachment");
			UiFileChooser.FileDialogResults results = null;
			while(true)
			{	
				results = UiFileChooser.displayFileOpenDialog(UiAttachmentEditor.this, null, UiFileChooser.NO_FILE_SELECTED, last, buttonLabel, null);
				if (results.wasCancelChoosen())
					return;
				if(results.getFileChoosen().isFile())
					break;
				mainWindow.notifyDlg("AttachmentNotAFile");
			}
			setLastAttachmentLoadDirectory(results.getCurrentDirectory());
			AttachmentProxy a = new AttachmentProxy(results.getFileChoosen());
			model.add(a);
		}
	}

	class RemoveHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			int[] rows = attachmentTable.getSelectedRows();
			if(rows.length == 0)
				return;
			if(!mainWindow.confirmDlg("RemoveAttachment"))
				return;
			for(int i = rows.length-1; i >= 0; --i)
			{
				model.remove(rows[i]);
			}
		}
	}

	UiTable attachmentTable;
	JButton remove;
	EditorAttachmentTableModel model;
	UiMainWindow mainWindow;
	
	private static File lastAttachmentLoadDirectory;
	
	static final int VISIBLE_ROW_COUNT = 4;
}
