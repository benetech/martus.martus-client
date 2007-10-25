/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;

import org.martus.client.swingui.UiFocusListener;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.tablemodels.AttachmentTableModel;
import org.martus.clientside.UiLocalization;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.swing.UiButton;
import org.martus.swing.UiFileChooser;
import org.martus.swing.Utilities;



public class UiAttachmentEditor extends UiAttachmentComponent
{
	public UiAttachmentEditor(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		updateTable();
	}

	AbstractAttachmentPanel createAttachmentPanel(int row)
	{
		return new EditAttachmentPanel(mainWindow, this, model.getAttachment(row));
	}

	public AttachmentProxy[] getAttachments()
	{
		return model.getAttachments();
	}

	JComponent createAttachmentFooter()
	{
		JButton add = new UiButton(getLocalization().getButtonLabel("addattachment"));
		add.addFocusListener(new UiFocusListener(this));		
		add.addActionListener(new AddHandler(mainWindow, model, this));
		
		Box buttonBox = Box.createHorizontalBox();
		Utilities.addComponentsRespectingOrientation(buttonBox, new Component[] {add, Box.createHorizontalGlue()});
		return buttonBox;
	}

	static class AddHandler implements ActionListener
	{
		public AddHandler(UiMainWindow mainWindowToUse, AttachmentTableModel modelToUse, UiAttachmentEditor editorToUse)
		{
			mainWindow = mainWindowToUse;
			model = modelToUse;
			editor = editorToUse;
		}
		
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
				results = UiFileChooser.displayFileOpenDialog(editor, null, UiFileChooser.NO_FILE_SELECTED, last, buttonLabel, null);
				if (results.wasCancelChoosen())
					return;
				if(results.getChosenFile().isFile())
					break;
				mainWindow.notifyDlg("AttachmentNotAFile");
			}
			setLastAttachmentLoadDirectory(results.getCurrentDirectory());
			AttachmentProxy a = new AttachmentProxy(results.getChosenFile());
			editor.addAttachment(a);
			
		}

		static void setLastAttachmentLoadDirectory(File newAttachmentLoadDirectory)
		{
			lastAttachmentLoadDirectory = newAttachmentLoadDirectory;
		}
	
		static File getLastAttachmentLoadDirectory()
		{
			return lastAttachmentLoadDirectory;
		}
	
		private static File lastAttachmentLoadDirectory;
	
		UiMainWindow mainWindow;
		AttachmentTableModel model;
		UiAttachmentEditor editor;
	}

//	public UiAttachmentEditor(UiMainWindow mainWindowToUse)
//	{
//		super(new BorderLayout());
//		mainWindow = mainWindowToUse;
//
//		UiLocalization localization = mainWindowToUse.getLocalization();
//		
//		TableRemoveButton remove = new TableRemoveButton(localization.getButtonLabel("removeattachment"));
//		remove.addFocusListener(new UiFocusListener(this));		
//		remove.addActionListener(new RemoveHandler());
//		remove.setEnabled(false);
//
//		model = new AttachmentTableModel(mainWindow);
//		model.addTableModelListener(remove);
//
//		attachmentTable = new UiTable(model);
//		attachmentTable.setMaxGridWidth(40);
//		attachmentTable.useMaxWidth();
//		attachmentTable.setFocusable(false);
//		attachmentTable.createDefaultColumnsFromModel();
//		attachmentTable.setColumnSelectionAllowed(false);
//		attachmentTable.setMaxColumnWidthToHeaderWidth(1);
//
//		new DropTarget(this, new attachmentDropAdapter());
//
//		UiScrollPane scrollPane = new UiScrollPane(attachmentTable);
//		scrollPane.getHorizontalScrollBar().setFocusable(false);
//		scrollPane.getVerticalScrollBar().setFocusable(false);
//
//		JButton add = new UiButton(localization.getButtonLabel("addattachment"));
//		add.addFocusListener(new UiFocusListener(this));		
//		add.addActionListener(new AddHandler());
//
//		UiVBox vbox = new UiVBox();
//		vbox.add(scrollPane);
//		vbox.add(new Component[]{add, remove});
//		add(vbox, BorderLayout.CENTER);
//
//		attachmentTable.resizeTable(VISIBLE_ROW_COUNT);
//	}
//
//	class attachmentDropAdapter implements DropTargetListener
//	{
//		public void dragEnter(DropTargetDragEvent dtde)
//		{
//		}
//
//		public void dragOver(DropTargetDragEvent dtde)
//		{
//			if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
//				dtde.acceptDrag(dtde.getDropAction());
//			else
//				dtde.rejectDrag();
//		}
//
//		public void dropActionChanged(DropTargetDragEvent dtde)
//		{
//		}
//
//		public void drop(DropTargetDropEvent dtde)
//		{
//			if(!dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
//			{
//				dtde.rejectDrop();
//				return;
//			}
//
//			dtde.acceptDrop(dtde.getDropAction());
//			Transferable t = dtde.getTransferable();
//			List list = null;
//			try
//			{
//				list = (List)t.getTransferData(DataFlavor.javaFileListFlavor);
//			}
//			catch(Exception e)
//			{
//				System.out.println("dropFile exception: " + e);
//				dtde.dropComplete(false);
//				return;
//			}
//
//			if(list.size() == 0)
//			{
//				System.out.println("dropFile: list empty");
//				dtde.dropComplete(false);
//				return;
//			}
//
//			for(int i = 0; i<list.size(); ++i)
//			{	
//				File file = (File)list.get(i);
//				setLastAttachmentLoadDirectory(file.getAbsoluteFile());
//				AttachmentProxy a = new AttachmentProxy(file);
//				model.add(a);
//			}
//			dtde.dropComplete(true);
//		}
//
//		public void dragExit(DropTargetEvent dte)
//		{
//		}
//	}
//	
//	public JComponent[] getFocusableComponents()
//	{
//		return new JComponent[]{attachmentTable};
//	}
//
//	public void addAttachment(AttachmentProxy a)
//	{
//		model.add(a);
//	}
//
//	public void clearAttachments()
//	{
//		model.clear();
//	}
//
//
//	UiTable attachmentTable;
//	AttachmentTableModel model;
//	UiMainWindow mainWindow;
//	
//	
//	static final int VISIBLE_ROW_COUNT = 4;
}

//class TableRemoveButton extends UiButton implements TableModelListener
//{
//	public TableRemoveButton(String label)
//	{
//		super(label);
//	}
//
//	public void tableChanged(TableModelEvent event)
//	{
//		AttachmentTableModel tableModel = (AttachmentTableModel)event.getSource();
//		setEnabled(tableModel.getRowCount() > 0);
//	}
//
//};
//
