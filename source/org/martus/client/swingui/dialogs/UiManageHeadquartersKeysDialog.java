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
package org.martus.client.swingui.dialogs;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.HeadquartersManagementTableModel;
import org.martus.client.swingui.SelectableHeadquartersEntry;
import org.martus.client.swingui.UiMainWindow;
import org.martus.clientside.UiLocalization;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MartusLogger;
import org.martus.common.crypto.MartusCrypto;
import org.martus.swing.UiButton;
import org.martus.swing.UiFileChooser;
import org.martus.swing.UiLabel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiVBox;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;
import org.martus.util.StreamableBase64.InvalidBase64Exception;


public class UiManageHeadquartersKeysDialog extends UiManageExternalPublicKeysDialog
{
	public UiManageHeadquartersKeysDialog(UiMainWindow owner)
	{
		super(owner, owner.getLocalization().getWindowTitle("ConfigureHQs"));
		mainWindow = owner;
		localization = mainWindow.getLocalization();
		
		JButton add = new UiButton(localization.getButtonLabel("ConfigureHQsAdd"));
		add.addActionListener(new AddHandler());
		remove = new UiButton(localization.getButtonLabel("ConfigureHQsRemove"));
		remove.addActionListener(new RemoveHandler());
		renameLabel = new UiButton(localization.getButtonLabel("ConfigureHQsReLabel"));
		renameLabel.addActionListener(new RenameHandler());

		String[] dialogText = getDialogText();
		UiVBox vBox = new UiVBox();
		for (String text : dialogText)
		{
			vBox.addCentered(new UiWrappedTextArea(text));
			vBox.addSpace();
		}

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10,10,10,10));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(vBox);
		
		model = new HeadquartersManagementTableModel(mainWindow.getApp());
		table = createHeadquartersTable(model);
		
		try
		{
			HeadquartersKeys local = mainWindow.getApp().getAllHQKeys();
			for(int i = 0; i<local.size();++i)
				addHQKeyToTable(local.get(i));
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
		enableDisableButtons();
		
		UiScrollPane scroller = new UiScrollPane(table);
		panel.add(scroller);
		panel.add(new UiLabel(" "));
		
		Box hBox = Box.createHorizontalBox();
		JButton save = new UiButton(localization.getButtonLabel("save"));
		save.addActionListener(new SaveHandler());
		JButton cancel = new UiButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(new CancelHandler());
		Utilities.addComponentsRespectingOrientation(hBox, new Component[]{add,remove,renameLabel,Box.createHorizontalGlue(),save,cancel});
		panel.add(hBox);
		
		getContentPane().add(panel);
		getRootPane().setDefaultButton(cancel);
		Utilities.centerDlg(this);
		setResizable(true);
	}

	private String[] getDialogText()
	{
		String[] dialogText = new String[]
		{
			localization.getFieldLabel("HQsSetAsProxyUploader"),
			localization.getFieldLabel("HQsSetAsDefault"),
			localization.getFieldLabel("ConfigureHQsCurrentHQs")
		};
		return dialogText;
	}
	
	void enableDisableButtons()
	{
		boolean enableButtons = false;
		if(table.getRowCount()>0)
			enableButtons = true;
		remove.setEnabled(enableButtons);
		renameLabel.setEnabled(enableButtons);

	}
	
	
	class CancelHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			dispose();
		}
	}
	
	class SaveHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			updateConfigInfo();
			dispose();
		}
	}
	
	class RenameHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			if(table.getSelectedRowCount()==0)
			{
				mainWindow.notifyDlg("NoHQsSelected");
				return;
			}
			int rowCount = model.getRowCount();
			for(int i = rowCount-1; i >=0 ; --i)
			{
				if(table.isRowSelected(i))
				{
					String newLabel = getHQLabel(model.getPublicCode(i), model.getLabel(i));
					if(newLabel== null)
						break;
					model.setLabel(i, newLabel);
				}
			}
		}
	}
	
	class AddHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			try
			{
				HeadquartersKey publicKey = getPublicKey();
				if(publicKey==null)
					return;
				addHQKeyToTable(publicKey);
			}
			catch (Exception e)
			{
				mainWindow.notifyDlg("PublicInfoFileError");
			}
		}
	}

	class RemoveHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			if(table.getSelectedRowCount()==0)
			{
				mainWindow.notifyDlg("NoHQsSelected");
				return;
			}
			if(!mainWindow.confirmDlg("ClearHQInformation"))
				return;
			
			int rowCount = model.getRowCount();
			for(int i = rowCount-1; i >=0 ; --i)
			{
				if(table.isRowSelected(i))
					model.removeRow(i);
			}
		}
	}

	void addHQKeyToTable(HeadquartersKey publicKey)
	{
		try
		{
			String publicCode = publicKey.getPublicCode();
			for(int i = 0; i < table.getRowCount(); ++i)
			{
				if(model.getPublicCode(i).equals(publicCode))
				{
					mainWindow.notifyDlg("HQKeyAlradyExists");
					return;
				}
			}
			SelectableHeadquartersEntry entry = new SelectableHeadquartersEntry(publicKey);
			HeadquartersKeys defaultHQKeys = mainWindow.getApp().getDefaultHQKeysWithFallback();
			boolean isDefault = defaultHQKeys.containsKey(publicKey.getPublicKey());
			entry.setSelected(isDefault);
			model.addNewHeadQuarterEntry(entry);
		}
		catch (InvalidBase64Exception e)
		{
			e.printStackTrace();
		}
	}
	
	void updateConfigInfo()
	{
		enableDisableButtons();
		mainWindow.setAndSaveHQKeysInConfigInfo(model.getAllKeys(), model.getAllSelectedHeadQuarterKeys());
	}
	
	public HeadquartersKey getPublicKey() throws Exception
	{
		String windowTitle = localization.getWindowTitle("ImportHQPublicKey");
		String buttonLabel = localization.getButtonLabel("inputImportPublicCodeok");
		
		File currentDirectory = new File(mainWindow.getApp().getCurrentAccountDirectoryName());
		FileFilter filter = new PublicInfoFileFilter();
		UiFileChooser.FileDialogResults results = UiFileChooser.displayFileOpenDialog(mainWindow, windowTitle, null, currentDirectory, buttonLabel, filter);
		if (results.wasCancelChoosen())
			return null;
		
		File importFile = results.getChosenFile();
		String publicKeyString = mainWindow.getApp().extractPublicInfo(importFile);

		String publicCode = MartusCrypto.computePublicCode(publicKeyString);
		if(confirmPublicCode(publicCode, "ImportPublicCode", "AccountCodeWrong"))
		{
			if(!mainWindow.confirmDlg("SetImportPublicKey"))
				return null;
		}
		else
			return null;
		String label = getHQLabel(MartusCrypto.computeFormattedPublicCode(publicKeyString), "");
		HeadquartersKey newKey = new HeadquartersKey(publicKeyString, label);
		return newKey;
	}

	public String getHQLabel(String publicCode, String previousValue)
	{
		String label = mainWindow.getStringInput("GetHQLabel", "", publicCode, previousValue);
		if(label == null)
			return null;
		return getUniqueLabel(publicCode, label);
	}

	private String getUniqueLabel(String publicCode, String label) 
	{
		HeadquartersKeys hQKeys = model.getAllKeys();
		for(int i = 0; i < hQKeys.size(); ++i)
		{
			HeadquartersKey hqKey = hQKeys.get(i);
			try 
			{
				if(hqKey.getPublicCode().equals(publicCode))
					continue;
			} 
			catch (InvalidBase64Exception e) 
			{
			}
			String hqConfiguredLabel = hqKey.getLabel();
			if(hqConfiguredLabel.length() >0 && label.equals(hqConfiguredLabel))
			{
				mainWindow.notifyDlg("HeadquarterLabelDuplicate");
				return null;
			}
		}
		return label;
	}
	
	class PublicInfoFileFilter extends FileFilter
	{
		public boolean accept(File pathname)
		{
			if(pathname.isDirectory())
				return true;
			return(pathname.getName().endsWith(MartusApp.PUBLIC_INFO_EXTENSION));
		}

		public String getDescription()
		{
			return localization.getFieldLabel("PublicInformationFiles");
		}
	}


	boolean confirmPublicCode(String rawPublicCode, String baseTag, String errorBaseTag)
	{
		String userEnteredPublicCode = "";
		while(true)
		{
			userEnteredPublicCode = mainWindow.getStringInput(baseTag, "", "", userEnteredPublicCode);
			if(userEnteredPublicCode == null)
				return false; // user hit cancel
			String normalizedPublicCode = MartusCrypto.removeNonDigits(userEnteredPublicCode);

			if(rawPublicCode.equals(normalizedPublicCode))
				return true;

			mainWindow.notifyDlg(errorBaseTag);
		}
	}
	

	HeadquartersManagementTableModel model;
	JButton remove;
	JButton renameLabel;
	UiLocalization localization;
}
