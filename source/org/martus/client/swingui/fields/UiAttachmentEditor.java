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

package org.martus.client.swingui.fields;

import java.awt.Dimension;
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
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.martus.client.swingui.UiFocusListener;
import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.swing.ParagraphLayout;
import org.martus.swing.UiFileChooser;



public class UiAttachmentEditor extends JPanel
{
	public UiAttachmentEditor(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
		ParagraphLayout layout = new ParagraphLayout();
		setLayout(layout);

		model = new EditorAttachmentTableModel(mainWindow, table);

		table = new JTable(model);
		new DropTarget(this, new attachmentDropAdapter());
		table.setFocusable(false);
		table.createDefaultColumnsFromModel();
		table.setColumnSelectionAllowed(false);
		Box hbox = Box.createHorizontalBox();
		Box vbox = Box.createVerticalBox();

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.getHorizontalScrollBar().setFocusable(false);
		scrollPane.getVerticalScrollBar().setFocusable(false);
		vbox.add(scrollPane);

		UiLocalization localization = mainWindowToUse.getLocalization();
		JButton add = new JButton(localization.getButtonLabel("addattachment"));
	
		add.addFocusListener(new UiFocusListener(this));		
		add.addActionListener(new AddHandler());
		hbox.add(add);
		remove = new JButton(localization.getButtonLabel("removeattachment"));
		remove.addFocusListener(new UiFocusListener(this));		
		remove.addActionListener(new RemoveHandler());
		remove.setEnabled(false);
		hbox.add(remove);
		hbox.add(Box.createHorizontalGlue());
		vbox.add(hbox);
		add(vbox);

		Dimension d = table.getPreferredScrollableViewportSize();
		int rowHeight = table.getRowHeight() + table.getRowMargin() ;
		d.height = VISIBLE_ROW_COUNT * rowHeight;
		table.setPreferredScrollableViewportSize(d);
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
		return new JComponent[]{table};
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

		public EditorAttachmentTableModel(UiMainWindow window, JTable table)
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
			UiFileChooser chooser = new UiFileChooser();
			File last = getLastAttachmentLoadDirectory();
			if(last != null)
				chooser.setCurrentDirectory(last);
			UiLocalization localization = mainWindow.getLocalization();
			chooser.setApproveButtonText(localization.getButtonLabel("addattachment"));
			int returnVal = chooser.showOpenDialog(UiAttachmentEditor.this);
			if(returnVal == JFileChooser.APPROVE_OPTION)
			{
				File newAttachmentFile = chooser.getSelectedFile();
				setLastAttachmentLoadDirectory(chooser.getCurrentDirectory());
				AttachmentProxy a = new AttachmentProxy(newAttachmentFile);
				model.add(a);
			}
		}
	}

	class RemoveHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			int[] rows = table.getSelectedRows();
			if(rows.length == 0)
				rows = new int[]{0};
			if(!mainWindow.confirmDlg(null,"RemoveAttachment"))
				return;
			for(int i = rows.length-1; i >= 0; --i)
			{
				model.remove(rows[i]);
			}
		}
	}

	JTable table;
	JButton remove;
	EditorAttachmentTableModel model;
	UiMainWindow mainWindow;
	
	private static File lastAttachmentLoadDirectory;
	
	static final int VISIBLE_ROW_COUNT = 4;
}
