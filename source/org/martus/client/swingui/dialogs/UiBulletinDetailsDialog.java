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

package org.martus.client.swingui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.HQKey;
import org.martus.common.HQKeys;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.UniversalId;
import org.martus.swing.UiLabel;
import org.martus.swing.UiParagraphPanel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiTable;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;
import org.martus.util.Base64.InvalidBase64Exception;


public class UiBulletinDetailsDialog extends JDialog
{
	public UiBulletinDetailsDialog(UiMainWindow mainWindowToUse, Bulletin bulletinToShow, String tagQualifierToUse)
	{
		super(mainWindowToUse.getCurrentActiveFrame(), true);
		
		mainWindow = mainWindowToUse;
		bulletin = bulletinToShow;
		tagQualifier = tagQualifierToUse;
		
		setTitle(getLocalization().getWindowTitle("BulletinDetailsDialog"));
		UiParagraphPanel panel = new UiParagraphPanel();
		panel.addComponents(new UiLabel(getLabel("AuthorPublicCode")), createField(getPublicCode()));
		panel.addComponents(new UiLabel(getLabel("BulletinId")),createField(bulletin.getLocalId()));

		HQKeys hqKeys = bulletin.getAuthorizedToReadKeys();
		if(hqKeys.size() > 0)
		{
			String hqText = getLabel("HQInfoFor" + tagQualifier); 
			JComponent hqInfo = createField(hqText);
			UiScrollPane hqScroller = createHeadquartersTable(hqKeys);

			panel.addBlankLine();
			panel.addOnNewLine(hqInfo);
			panel.addComponents(new UiLabel(getLabel("Headquarters")), hqScroller);
		}
		
		UiScrollPane historyScroller = createHistoryTable();
		panel.addComponents(new UiLabel(getLabel("History")), historyScroller);
		
		JButton closeButton = new JButton(getLocalization().getButtonLabel("close"));
		closeButton.addActionListener(new CloseHandler());
		getRootPane().setDefaultButton(closeButton);
		closeButton.requestFocus(true);
		previewVersionButton = new JButton(getLocalization().getButtonLabel("ViewPreviousBulletinVersion"));
		previewVersionButton.addActionListener(new previewListener());
		if(versionTable.getRowCount() < 2)
			previewVersionButton.setEnabled(false);
		panel.addComponents(closeButton, previewVersionButton);

		getContentPane().add(new UiScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		
		Utilities.centerDlg(this);
		setResizable(true);
		
	}
	
	public void hidePreviewButton()
	{
		previewVersionButton.setVisible(false);
	}
	
	class previewListener implements ActionListener
	{

		public void actionPerformed(ActionEvent e)
		{
			int selectedRow = versionTable.getSelectedRow();
			if(selectedRow < 0)
				return;
			if(selectedRow == versionTable.getRowCount()-1)
			{
				mainWindow.notifyDlg(mainWindow, "AlreadyViewingThisVersion");
				return;
			}
			String localId = (String)versionTable.getValueAt(selectedRow, 1);
			UniversalId uid = UniversalId.createFromAccountAndLocalId(bulletin.getAccount(), localId);
			Bulletin previousBulletinVersion = mainWindow.getStore().getBulletinRevision(uid);
			if(previousBulletinVersion == null)
			{
				mainWindow.notifyDlg(mainWindow, "BulletinVersionNotInSystem");
				return;
			}
			new UiBulletinVersionPreviewDlg(mainWindow, previousBulletinVersion);
		}
		
	}

	private UiScrollPane createHistoryTable()
	{
		BulletinHistory history = bulletin.getHistory();
		DefaultTableModel versionModel = new DetailsTableModel(); 
		versionModel.addColumn(getLabel("VersionNumber"));
		versionModel.addColumn(getLabel("VersionId"));
		versionModel.addColumn(getLabel("VersionDate"));
		versionModel.addColumn(getLabel("VersionTitle"));
		versionModel.setRowCount(history.size() + 1);

		for(int i=0; i < history.size(); ++i)
		{
			String localId = history.get(i);
			UniversalId uid = UniversalId.createFromAccountAndLocalId(bulletin.getAccount(), localId);
			populateVersionRow(versionModel, i, uid);
		}
		populateVersionRow(versionModel, history.size(), bulletin.getUniversalId());
		versionTable = new UiTable(versionModel);
		versionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		versionTable.setColumnSelectionAllowed(false);
		versionTable.setShowGrid(true);
		versionTable.resizeTable();

		UiScrollPane versionScroller = new UiScrollPane(versionTable);
		return versionScroller;
	}
	
	class DetailsTableModel extends DefaultTableModel
	{
		
		public boolean isCellEditable(int row, int column)
		{
			return false;
		}
		
	}
	
	
	private UiScrollPane createHeadquartersTable(HQKeys hqKeys)
	{
		DefaultTableModel hqModel = new DefaultTableModel();
		hqModel.addColumn(getLabel("HQLabel"));
		hqModel.addColumn(getLabel("HQPublicCode"));
		hqModel.setRowCount(hqKeys.size());
		
		for(int i=0; i < hqKeys.size(); ++i)
		{
			HQKey key = hqKeys.get(i);
			String publicCode = key.getPublicKey();
			try
			{
				publicCode = key.getPublicCode();
			}
			catch (InvalidBase64Exception e)
			{
				e.printStackTrace();
			}
			
			hqModel.setValueAt(mainWindow.getApp().getHQLabelIfPresent(key), i, 0);
			hqModel.setValueAt(publicCode, i, 1);
		}
		UiTable hqTable = new UiTable(hqModel);
		hqTable.setColumnSelectionAllowed(false);
		hqTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		hqTable.setShowGrid(true);
		hqTable.resizeTable();
		hqTable.setEnabled(false);
		UiScrollPane hqScroller = new UiScrollPane(hqTable);
		return hqScroller;
	}

	private void populateVersionRow(DefaultTableModel versionModel, int i, UniversalId uid)
	{
		int column = 0;
		versionModel.setValueAt(new Integer(1+i), i, column++);
		versionModel.setValueAt(uid.getLocalId(), i, column++);
		versionModel.setValueAt(getSavedDateToDisplay(uid), i, column++);
		versionModel.setValueAt(getTitleToDisplay(uid), i, column++);
	}
	
	private String getSavedDateToDisplay(UniversalId uid)
	{
		Bulletin b = mainWindow.getStore().getBulletinRevision(uid);
		if(b != null)
			return getLocalization().formatDateTime(b.getLastSavedTime());
		
		if(uid.equals(bulletin.getUniversalId()))
			return getLabel("InProgressDate");
		
		return getLabel("UnknownDate");
	}

	private String getTitleToDisplay(UniversalId uid)
	{
		Bulletin b = mainWindow.getStore().getBulletinRevision(uid);
		if(b != null)
			return b.get(Bulletin.TAGTITLE);
		
		if(uid.equals(bulletin.getUniversalId()))
			return getLabel("InProgressTitle");
		
		return getLabel("UnknownTitle");
	}

	private JComponent createField(String text)
	{
		UiWrappedTextArea component = new UiWrappedTextArea(text);
		component.setEditable(false);
		return component;
	}
	
	private UiLocalization getLocalization()
	{
		return mainWindow.getLocalization();
	}

	private String getPublicCode()
	{
		try
		{
			return MartusCrypto.computeFormattedPublicCode(bulletin.getAccount());
		}
		catch (InvalidBase64Exception e)
		{
			e.printStackTrace();
			return "";
		}		
	}

	private String getLabel(String tag)
	{
		return getLocalization().getFieldLabel("BulletinDetails" + tag);
	}
	
	class CloseHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			UiBulletinDetailsDialog.this.dispose();
		}
	}
	
	UiMainWindow mainWindow;
	Bulletin bulletin;
	String tagQualifier;
	private JButton previewVersionButton;
	UiTable versionTable;

}
