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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.bulletincomponent.UiBulletinComponent;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.BulletinSaver;
import org.martus.common.database.Database;
import org.martus.swing.ParagraphLayout;
import org.martus.swing.UiFileChooser;
import org.martus.swing.Utilities;



public class UiAttachmentViewer extends JPanel
{
	public UiAttachmentViewer(UiMainWindow mainWindowToUse, UiBulletinComponent bulletinComponentToUse)
	{
		mainWindow = mainWindowToUse;
		bulletinComponent = bulletinComponentToUse;
		app = mainWindow.getApp();
		model = new AttachmentTableModel();
		ParagraphLayout layout = new ParagraphLayout();
		setLayout(layout);

		attachmentTable = new JTable(model);
		attachmentTable.createDefaultColumnsFromModel();
		attachmentTable.setColumnSelectionAllowed(false);

		Box buttonBox = Box.createHorizontalBox();
		Box vbox = Box.createVerticalBox();
		attachmentPane = new JScrollPane(attachmentTable);
		attachmentPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		attachmentPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

		vbox.add(attachmentPane);

		UiLocalization localization = mainWindowToUse.getLocalization();

		saveButton = new JButton(localization.getButtonLabel("saveattachment"));
		saveButton.addActionListener(new SaveHandler());
		saveButton.setEnabled(false);
		buttonBox.add(saveButton);
		
		viewButton = new JButton(localization.getButtonLabel("viewattachment"));
		viewButton.addActionListener(new ViewHandler());
		viewButton.setEnabled(false);
		if(!Utilities.isMSWindows())
			viewButton.setVisible(false);
		buttonBox.add(viewButton);

		buttonBox.add(Box.createHorizontalGlue());
		vbox.add(buttonBox);
		add(vbox);

		resizeTable();
		attachmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	public void resizeTable()
	{
		Dimension d = attachmentTable.getPreferredScrollableViewportSize();
		int rowHeight = attachmentTable.getRowHeight() + attachmentTable.getRowMargin() ;
		int rowCount = model.getRowCount();
		d.height = rowCount * rowHeight;
		attachmentTable.setPreferredScrollableViewportSize(d);
		saveButton.setEnabled(rowCount > 0);
		viewButton.setEnabled(rowCount > 0);
	}

	public void addAttachment(AttachmentProxy a)
	{
		model.add(a);
		resizeTable();
	}

	public void clearAttachments()
	{
		model.clear();
		resizeTable();
	}

	class AttachmentTableModel extends AbstractTableModel
	{
		public AttachmentTableModel()
		{
			attachmentList = new Vector();
		}

		void clear()
		{
			attachmentList.clear();
			fireTableDataChanged();
		}

		public void add(AttachmentProxy a)
		{
			attachmentList.add(a);
			fireTableDataChanged();
		}

		public int getRowCount()
		{
			return attachmentList.size();
		}

		public int getColumnCount()
		{
			return 1;
		}

		public String getColumnName(int column)
		{
			return mainWindow.getLocalization().getButtonLabel("attachmentlabel");
		}

		public AttachmentProxy getAttachmentProxyAt(int row, int column)
		{
			return (AttachmentProxy)attachmentList.get(row);
		}

		public Object getValueAt(int row, int column)
		{
			AttachmentProxy a = (AttachmentProxy)attachmentList.get(row);
			return a.getLabel();
		}

		public void setValueAt(Object value, int row, int column)
		{
		}

		public boolean isCellEditable(int row, int column)
		{
			return false;
		}

		private Vector attachmentList;
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
			int selection = GetSelection();
			if(selection == -1)
				return;
			if(!mainWindow.getApp().isOurBulletin(bulletinComponent.getCurrentBulletin()))
			{
				if(!mainWindow.confirmDlg(mainWindow, "NotYourBulletinViewAttachmentAnyways"))
					return;
			}

			String fileName = (String)model.getValueAt(selection,1);
			Cursor originalCursor = mainWindow.setWaitingCursor();
			try
			{
				File temp = File.createTempFile(extractFileNameOnly(fileName), extractExtentionOnly(fileName));
				temp.deleteOnExit();
			
				AttachmentProxy proxy = model.getAttachmentProxyAt(selection,1);
				Database db = mainWindow.getApp().getStore().getDatabase();
				BulletinSaver.extractAttachmentToFile(db, proxy, app.getSecurity(), temp);

				Runtime runtimeViewer = Runtime.getRuntime();
				String tempFileFullPathName = temp.getPath();
				Process processView=runtimeViewer.exec("rundll32"+" "+"url.dll,FileProtocolHandler"+" "+tempFileFullPathName);
				processView.waitFor();
			}
			catch(Exception e)
			{
				mainWindow.notifyDlg(mainWindow, "UnableToViewAttachment");
				System.out.println("Unable to view file :" + e);
			}
			mainWindow.resetCursor(originalCursor);
		}
	}
	
	class SaveHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			int selection = GetSelection();
			if(selection == -1)
				return;
			String fileName = (String)model.getValueAt(selection,1);

			UiFileChooser chooser = new UiFileChooser();
			chooser.setSelectedFile(new File(fileName));
			File last = getLastAttachmentSaveDirectory();
			if(last != null)
				chooser.setCurrentDirectory(last);
			int returnVal = chooser.showSaveDialog(mainWindow);
			if(returnVal == JFileChooser.APPROVE_OPTION)
			{
				setLastAttachmentSaveDirectory(chooser.getCurrentDirectory());
				File outputFile = chooser.getSelectedFile();
				if(outputFile.exists())
				{
					if(!mainWindow.confirmDlg(mainWindow,"OverWriteExistingFile"))
						return;
				}
				Cursor originalCursor = mainWindow.setWaitingCursor();
				AttachmentProxy proxy = model.getAttachmentProxyAt(selection,1);
				try
				{
					Database db = mainWindow.getApp().getStore().getDatabase();
					BulletinSaver.extractAttachmentToFile(db, proxy, app.getSecurity(), outputFile);
				}
				catch(Exception e)
				{
					mainWindow.notifyDlg(mainWindow, "UnableToSaveAttachment");
					System.out.println("Unable to save file :" + e);
				}
				mainWindow.resetCursor(originalCursor);
			}
		}
	}

	UiMainWindow mainWindow;
	UiBulletinComponent bulletinComponent;
	MartusApp app;
	AttachmentTableModel model;
	JTable attachmentTable;
	public JButton saveButton;
	JButton viewButton;
	JScrollPane attachmentPane;

	private static File lastAttachmentSaveDirectory;

	static final int VISIBLE_ROW_COUNT = 4;
}
