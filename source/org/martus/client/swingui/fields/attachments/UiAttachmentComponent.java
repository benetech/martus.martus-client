/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2007, Beneficent
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
package org.martus.client.swingui.fields.attachments;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.tablemodels.AttachmentTableModel;
import org.martus.common.MartusLogger;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.packet.UniversalId;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.Packet.WrongPacketTypeException;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.util.StreamableBase64.InvalidBase64Exception;

import com.jhlabs.awt.Alignment;
import com.jhlabs.awt.GridLayoutPlus;

abstract public class UiAttachmentComponent extends JPanel
{
	public UiAttachmentComponent(UiMainWindow mainWindowToUse)
	{
		GridLayoutPlus layout = new GridLayoutPlus(0, 1, 0, 0, 0, 0);
		setLayout(layout);
		
		mainWindow = mainWindowToUse;
		model = new AttachmentTableModel(mainWindow);

	}
	
	protected MartusLocalization getLocalization()
	{
		return mainWindow.getLocalization();
	}

	protected MartusCrypto getSecurity()
	{
		return mainWindow.getApp().getSecurity();
	}

	public void updateTable()
	{
		removeAll();
		JPanel headerContainer = new JPanel(new BorderLayout());
		headerContainer.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		headerContainer.add(new ViewAttachmentHeaderRow());
		add(headerContainer);
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


	class ViewSingleAttachmentPanel extends JPanel
	{
		public ViewSingleAttachmentPanel(AttachmentProxy proxyToUse)
		{
			super(new BorderLayout());
			proxy = proxyToUse;

			setBorder(BorderFactory.createLineBorder(Color.BLACK));

			addHeader();

			DragSource dragSource = DragSource.getDefaultDragSource();
			dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, 
					new AttachmentDragHandler(mainWindow, proxy));
		}

		private void addHeader()
		{
			header = new ViewAttachmentSummaryRow(this);
			add(header, BorderLayout.BEFORE_FIRST_LINE);
		}
		
		public AttachmentProxy getAttachmentProxy()
		{
			return proxy;
		}
		
		public void showImageInline()
		{
			if(!addInlineImage())
				return;
			isImageInline = true;
			header.showHideButton();
			validateParent();
			repaint();
		}

		private void validateParent()
		{
			Container top = getTopLevelAncestor();
			if(top != null)
				top.validate();
		}
		
		public void hideImage()
		{
			isImageInline = false;
			JLabel emptySpace = new JLabel();
			emptySpace.setVisible(false);
			add(emptySpace, BorderLayout.CENTER);
			header.showViewButton();
			validateParent();
			repaint();
		}

		private boolean addInlineImage()
		{
			try
			{
				InlineAttachmentComponent image = new InlineAttachmentComponent(proxy);
				image.validate();
				if(!image.isValid())
					return false;
				add(image, BorderLayout.CENTER);
				return true;
			} 
			catch (Exception e)
			{
				MartusLogger.logException(e);
				return false;
			}
		}
		
		AttachmentProxy proxy;
		boolean isImageInline;
		ViewAttachmentSummaryRow header;
	}
	
	class InlineAttachmentComponent extends UiLabel
	{
		public InlineAttachmentComponent(AttachmentProxy proxy) throws Exception
		{
			ReadableDatabase db = mainWindow.getApp().getStore().getDatabase();
			MartusCrypto security = getSecurity();
			File tempFile = extractAttachmentToTempFile(db, proxy, security);
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Image image = toolkit.getImage(tempFile.getAbsolutePath());
			ImageIcon icon = new ImageIcon(image);
			setIcon(icon);
			setBorder(BorderFactory.createLineBorder(Color.BLACK));
		}
		
		public boolean isValid()
		{
			return (getIcon().getIconHeight() > 0);
		}
	}
	
	class MultiButtonPanel extends JPanel
	{
		public MultiButtonPanel(Color background)
		{
			setBackground(background);
			layout = new CardLayout();
			setLayout(layout);
			JComponent emptyPanel = new JPanel();
			emptyPanel.setBackground(getBackground());
			add(emptyPanel, "");
		}
		
		public void showCard(String cardName)
		{
			layout.show(this, cardName);
		}
		
		CardLayout layout;
	}
	
	class ViewAttachmentRow extends JPanel
	{
		public ViewAttachmentRow(Color backgroundColor)
		{
			setBackground(backgroundColor);
			GridLayoutPlus layout = new GridLayoutPlus(1, 0, 0, 0, 0, 0);
			layout.setFill(Alignment.FILL_VERTICAL);
			setLayout(layout);

			viewButton = new UiButton(getLocalization().getButtonLabel("viewattachment"));
			hideButton = new UiButton(getLocalization().getButtonLabel("hideattachment"));
			saveButton = new UiButton(getLocalization().getButtonLabel("saveattachment"));
			
			viewHidePanel = createMultiButtonPanel();
			viewHidePanel.add(viewButton, viewButton.getText());
			viewHidePanel.add(hideButton, hideButton.getText());
			
			savePanel = createMultiButtonPanel();
			savePanel.add(saveButton, saveButton.getText());
		}

		private MultiButtonPanel createMultiButtonPanel()
		{
			MultiButtonPanel panel = new MultiButtonPanel(getBackground());
			return panel;
		}
		
		public int getLabelColumnWidth()
		{
			return 400;
		}
		
		public int getSizeColumnWidth()
		{
			return 80;
		}

		void createCells(String labelColumnText, String sizeColumnText)
		{
			addCell(new UiLabel(labelColumnText), getLabelColumnWidth());
			addCell(new UiLabel(sizeColumnText), getSizeColumnWidth());
			addCell(viewHidePanel);
			addCell(savePanel);
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
			cell.setBackground(getBackground());
			cell.setForeground(getForeground());
			cell.setBorder(BorderFactory.createCompoundBorder(outsideBorder, insideBorder));
			cell.add(contents);
			add(cell);
			return cell;
		}
		
		MultiButtonPanel viewHidePanel;
		MultiButtonPanel savePanel;
		UiButton viewButton;
		UiButton hideButton;
		UiButton saveButton;
	}
	
	class ViewAttachmentHeaderRow extends ViewAttachmentRow
	{
		public ViewAttachmentHeaderRow()
		{
			super(UIManager.getColor("TableHeader.background"));
			setForeground(UIManager.getColor("TableHeader.foreground"));
			String labelHeader = getLocalization().getButtonLabel("attachmentLabel");
			String sizeHeader = getLocalization().getButtonLabel("attachmentSize");
			createCells(labelHeader, sizeHeader);
		}
	}
	
	class ViewAttachmentSummaryRow extends ViewAttachmentRow
	{
		public ViewAttachmentSummaryRow(ViewSingleAttachmentPanel panel)
		{
			super(Color.WHITE);
			AttachmentProxy proxy = panel.getAttachmentProxy();

			viewHidePanel.showCard(viewButton.getText());
			savePanel.showCard(saveButton.getText());
			if(isAttachmentAvailable(proxy))
			{
				viewButton.addActionListener(new ViewHandler(mainWindow, panel));
				hideButton.addActionListener(new HideHandler(panel));
				saveButton.addActionListener(new SaveHandler(mainWindow, proxy));
			}
			else
			{
				viewButton.setEnabled(false);
				hideButton.setEnabled(false);
				saveButton.setEnabled(false);
			}

			String labelColumnText = proxy.getLabel();
			String sizeColumnText = model.getSize(proxy);
			createCells(labelColumnText, sizeColumnText);
		}

		public void showViewButton()
		{
			viewHidePanel.showCard(viewButton.getText());
		}
		
		public void showHideButton()
		{
			viewHidePanel.showCard(hideButton.getText());
		}
		
		boolean isAttachmentAvailable(AttachmentProxy proxy)
		{
			UniversalId uid = proxy.getUniversalId();
			DatabaseKey key = DatabaseKey.createLegacyKey(uid);
			return mainWindow.getStore().doesBulletinRevisionExist(key);
		}
		
	}
	

	static File extractAttachmentToTempFile(ReadableDatabase db, AttachmentProxy proxy, MartusCrypto security) throws IOException, InvalidBase64Exception, InvalidPacketException, SignatureVerificationException, WrongPacketTypeException, CryptoException
	{
		String fileName = proxy.getLabel();
		File temp = File.createTempFile(extractFileNameOnly(fileName), extractExtentionOnly(fileName));
		temp.deleteOnExit();

		BulletinLoader.extractAttachmentToFile(db, proxy, security, temp);
		return temp;
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

	class HideHandler implements ActionListener
	{
		public HideHandler(ViewSingleAttachmentPanel panelToUse)
		{
			panel = panelToUse;
		}
		
		public void actionPerformed(ActionEvent ae)
		{
			panel.hideImage();
		}

		ViewSingleAttachmentPanel panel;
	}

	UiMainWindow mainWindow;
	AttachmentTableModel model;
}
