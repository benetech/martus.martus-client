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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.UiScrollPane;
import org.martus.common.HQKey;
import org.martus.common.HQKeys;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.UniversalId;
import org.martus.swing.UiParagraphPanel;
import org.martus.swing.UiTable;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;
import org.martus.util.Base64.InvalidBase64Exception;


public class UiBulletinDetailsDialog extends JDialog
{
	public UiBulletinDetailsDialog(UiMainWindow mainWindowToUse, Bulletin bulletinToShow, String tagQualifierToUse)
	{
		super(mainWindowToUse.getCurrentActiveFrame());
		
		mainWindow = mainWindowToUse;
		bulletin = bulletinToShow;
		tagQualifier = tagQualifierToUse;
		
		setTitle(getLocalization().getWindowTitle("BulletinDetailsDialog"));

		UiParagraphPanel panel = new UiParagraphPanel();
		panel.addComponents(new JLabel(getLabel("AuthorPublicCode")), createField(getPublicCode()));
		panel.addComponents(new JLabel(getLabel("BulletinId")),createField(bulletin.getLocalId()));

		HQKeys hqKeys = bulletin.getAuthorizedToReadKeys();
		if(hqKeys.size() > 0)
		{
			String hqText = getLabel("HQInfoFor" + tagQualifier); 
			JComponent hqInfo = createField(hqText);
			UiScrollPane hqScroller = createHeadquartersTable(hqKeys);

			panel.addBlankLine();
			panel.addOnNewLine(hqInfo);
			panel.addComponents(new JLabel(getLabel("Headquarters")), hqScroller);
		}
		
		UiScrollPane historyScroller = createHistoryTable();

		panel.addComponents(new JLabel(getLabel("History")), historyScroller);
		
		JButton closeButton = new JButton(getLocalization().getButtonLabel("close"));
		closeButton.addActionListener(new CloseHandler());
		
		Box buttonBox = Box.createHorizontalBox();
		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.add(closeButton);
		buttonBox.add(Box.createHorizontalGlue());

		getContentPane().add(panel);
		getContentPane().add(buttonBox, BorderLayout.SOUTH);
		
		Utilities.centerDlg(this);
		setResizable(true);
		
	}
	
	private UiScrollPane createHistoryTable()
	{
		BulletinHistory history = bulletin.getHistory();
		DefaultTableModel versionModel = new DefaultTableModel(); 
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
		UiTable versionTable = new UiTable(versionModel);
		versionTable.setColumnSelectionAllowed(false);
		versionTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		versionTable.setShowGrid(true);
		versionTable.resizeTable();
		versionTable.setEnabled(false);
		UiScrollPane versionScroller = new UiScrollPane(versionTable);
		return versionScroller;
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
			return b.getLastSavedDateTime();
		
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

}
