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

package org.martus.client.swingui.fields;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.martus.client.core.TransferableAttachmentList;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.tablemodels.AttachmentTableModel;
import org.martus.common.MartusLogger;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.packet.UniversalId;
import org.martus.swing.UiButton;
import org.martus.swing.UiFileChooser;
import org.martus.swing.UiLabel;
import org.martus.swing.Utilities;

import com.jhlabs.awt.Alignment;
import com.jhlabs.awt.GridLayoutPlus;

public class UiAttachmentViewer extends JPanel
{
	public UiAttachmentViewer(UiMainWindow mainWindowToUse)
	{
		GridLayoutPlus layout = new GridLayoutPlus(0, 1, 0, 0, 0, 0);
		setLayout(layout);
		
		mainWindow = mainWindowToUse;
		model = new AttachmentTableModel(mainWindow);

		updateTable();
	}

	MartusLocalization getLocalization()
	{
		return mainWindow.getLocalization();
	}
	
	MartusCrypto getSecurity()
	{
		return mainWindow.getApp().getSecurity();
	}

	public void updateTable()
	{
		removeAll();
		for(int row = 0; row < model.getRowCount(); ++row)
		{
			add(new ViewSingleAttachmentPanel(model.getAttachment(row)));
		}
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


	public static String extractFileNameOnly(String fullName)
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

	public static String extractExtentionOnly(String fullName)
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
		public ViewHandler(AttachmentProxy proxyToUse)
		{
			proxy = proxyToUse;
		}
		
		public void actionPerformed(ActionEvent ae)
		{
			String author = proxy.getUniversalId().getAccountId();
			if(!author.equals(mainWindow.getApp().getAccountId()))
			{
				if(!mainWindow.confirmDlg("NotYourBulletinViewAttachmentAnyways"))
					return;
			}
			String fileName = proxy.getLabel();
			mainWindow.setWaitingCursor();
			try
			{
				File temp = File.createTempFile(extractFileNameOnly(fileName), extractExtentionOnly(fileName));
				temp.deleteOnExit();
			
				ReadableDatabase db = mainWindow.getApp().getStore().getDatabase();
				BulletinLoader.extractAttachmentToFile(db, proxy, getSecurity(), temp);

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
		
		AttachmentProxy proxy;
	}
	
	class SaveHandler implements ActionListener
	{
		public SaveHandler(AttachmentProxy proxyToUse)
		{
			proxy = proxyToUse;
		}
		
		public void actionPerformed(ActionEvent ae)
		{
			String fileName = proxy.getLabel();

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
			try
			{
				ReadableDatabase db = mainWindow.getApp().getStore().getDatabase();
				BulletinLoader.extractAttachmentToFile(db, proxy, getSecurity(), outputFile);
			}
			catch(Exception e)
			{
				mainWindow.notifyDlg("UnableToSaveAttachment");
				System.out.println("Unable to save file :" + e);
			}
			mainWindow.resetCursor();
		}

		AttachmentProxy proxy;
	}

	class AttachmentDragHandler implements DragGestureListener, DragSourceListener
	{
		public AttachmentDragHandler(AttachmentProxy proxyToUse)
		{
			proxy = proxyToUse;
		}
		
		public void dragGestureRecognized(DragGestureEvent dge)
		{
			MartusLogger.log("Dragging: " + proxy.getLabel());
			AttachmentProxy[] attachments = new AttachmentProxy[] {proxy};
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
		
		AttachmentProxy proxy;
	}
	
	class ViewSingleAttachmentPanel extends JPanel
	{
		public ViewSingleAttachmentPanel(AttachmentProxy proxyToUse)
		{
			super(new BorderLayout());
			proxy = proxyToUse;
			ViewAttachmentHeader header = new ViewAttachmentHeader(proxy);
			add(header, BorderLayout.BEFORE_FIRST_LINE);
			setBorder(BorderFactory.createLineBorder(Color.BLACK));

			DragSource dragSource = DragSource.getDefaultDragSource();
			dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, 
					new AttachmentDragHandler(proxy));
		}
		
		AttachmentProxy proxy;
	}
	
	class ViewAttachmentHeader extends JPanel
	{
		public ViewAttachmentHeader(AttachmentProxy proxy)
		{
			GridLayoutPlus layout = new GridLayoutPlus(1, 0, 0, 0, 0, 0);
			layout.setFill(Alignment.FILL_VERTICAL);
			setLayout(layout);

			addCell(new UiLabel(proxy.getLabel()), 400);
			addCell(new UiLabel(model.getSize(proxy)), 80);
			if(Utilities.isMSWindows())
			{
				UiButton viewButton = new UiButton(getLocalization().getButtonLabel("viewattachment"));
				if(isAttachmentAvailable(proxy))
					viewButton.addActionListener(new ViewHandler(proxy));
				else
					viewButton.setEnabled(false);
				addCell(viewButton);
			}

			UiButton saveButton = new UiButton(getLocalization().getButtonLabel("saveattachment"));
			if(isAttachmentAvailable(proxy))
				saveButton.addActionListener(new SaveHandler(proxy));
			else
				saveButton.setEnabled(false);
			addCell(saveButton);
		}
		
		boolean isAttachmentAvailable(AttachmentProxy proxy)
		{
			UniversalId uid = proxy.getUniversalId();
			DatabaseKey key = DatabaseKey.createLegacyKey(uid);
			return mainWindow.getStore().doesBulletinRevisionExist(key);
		}
		
		JPanel addCell(JComponent contents, int preferredWidth)
		{
			JPanel cell = addCell(contents);
			cell.setPreferredSize(new Dimension(preferredWidth, 1));
			return cell;
		}
		
		JPanel addCell(JComponent contents)
		{
			Border outsideBorder = BorderFactory.createLineBorder(Color.BLACK);
			Border insideBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
			JPanel cell = new JPanel();
			cell.setBorder(BorderFactory.createCompoundBorder(outsideBorder, insideBorder));
			cell.add(contents);
			add(cell);
			return cell;
		}
	}
	
	UiMainWindow mainWindow;
	AttachmentTableModel model;

	private static File lastAttachmentSaveDirectory;
}
