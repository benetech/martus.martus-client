/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2005, Beneficent
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
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.martus.client.swingui.UiFocusListener;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.tablemodels.AttachmentTableModel;
import org.martus.clientside.UiLocalization;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.swing.UiButton;
import org.martus.swing.UiFileChooser;
import org.martus.swing.UiParagraphPanel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiTable;
import org.martus.swing.UiVBox;



public class UiAttachmentEditor extends UiParagraphPanel
{
	public UiAttachmentEditor(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;

		UiLocalization localization = mainWindowToUse.getLocalization();
		
		TableRemoveButton remove = new TableRemoveButton(localization.getButtonLabel("removeattachment"));
		remove.addFocusListener(new UiFocusListener(this));		
		remove.addActionListener(new RemoveHandler());
		remove.setEnabled(false);

		model = new AttachmentTableModel(mainWindow, attachmentTable);
		model.addTableModelListener(remove);

		attachmentTable = new UiTable(model);
		new DropTarget(this, new attachmentDropAdapter());
		attachmentTable.setFocusable(false);
		attachmentTable.createDefaultColumnsFromModel();
		attachmentTable.setColumnSelectionAllowed(false);
		attachmentTable.setMaxColumnWidthToHeaderWidth(1);

		UiScrollPane scrollPane = new UiScrollPane(attachmentTable);
		scrollPane.getHorizontalScrollBar().setFocusable(false);
		scrollPane.getVerticalScrollBar().setFocusable(false);

		JButton add = new UiButton(localization.getButtonLabel("addattachment"));
		add.addFocusListener(new UiFocusListener(this));		
		add.addActionListener(new AddHandler());

		UiVBox vbox = new UiVBox();
		vbox.add(scrollPane);
		vbox.add(new Component[]{add, remove});
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


	class AddHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			File last = getLastAttachmentLoadDirectory();
			if(last == null)
				last = UiFileChooser.getHomeDirectoryFile();
			
			UiLocalization localization = mainWindow.getLocalization();
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
	AttachmentTableModel model;
	UiMainWindow mainWindow;
	
	private static File lastAttachmentLoadDirectory;
	
	static final int VISIBLE_ROW_COUNT = 4;
}

class TableRemoveButton extends UiButton implements TableModelListener
{
	public TableRemoveButton(String label)
	{
		super(label);
	}

	public void tableChanged(TableModelEvent event)
	{
		AttachmentTableModel model = (AttachmentTableModel)event.getSource();
		setEnabled(model.getRowCount() > 0);
	}

};

