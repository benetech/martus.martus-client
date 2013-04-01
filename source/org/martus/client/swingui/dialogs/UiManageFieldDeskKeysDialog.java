/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2013, Beneficent
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
import java.io.File;

import javax.swing.filechooser.FileFilter;

import org.martus.client.core.ConfigInfo;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.ExternalPublicKeysTableModel;
import org.martus.client.swingui.FieldDeskManagementTableModel;
import org.martus.client.swingui.SelectableFieldDeskEntry;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.FieldDeskKey;
import org.martus.common.FieldDeskKeys;
import org.martus.common.HeadquartersKeys;
import org.martus.common.crypto.MartusCrypto;
import org.martus.swing.UiFileChooser;
import org.martus.util.StreamableBase64.InvalidBase64Exception;

public class UiManageFieldDeskKeysDialog extends UiManageExternalPublicKeysDialog
{
	public UiManageFieldDeskKeysDialog(UiMainWindow owner) throws Exception
	{
		super(owner, owner.getLocalization().getWindowTitle("ManageFieldDeskKeys"));
		
	}

	@Override
	void addExistingKeysToTable() throws Exception
	{
		String fieldDeskKeysXml = mainWindow.getApp().getConfigInfo().getFieldDeskKeysXml();
		FieldDeskKeys local = new FieldDeskKeys(fieldDeskKeysXml);
		for(int i = 0; i<local.size();++i)
			addFieldDeskKeyToTable(local.get(i));
	}

	@Override
	ExternalPublicKeysTableModel createModel()
	{
		return new FieldDeskManagementTableModel(mainWindow.getApp());
	}

	@Override
	RemoveHandler createRemoveHandler()
	{
		return new RemoveHandler();
	}

	@Override
	AddHandler createAddHandler()
	{
		return new AddHandler();
	}

	@Override
	String getEditLabelButtonName()
	{
		return localization.getButtonLabel("EditFieldDeskLabel");
	}

	@Override
	String[] getDialogText()
	{
		String[] dialogText = new String[]
		{
			localization.getFieldLabel("**************** Need text here")
		};
		return dialogText;
	}
	
	@Override
	void notifyNoneSelected()
	{
		mainWindow.notifyDlg("NoFieldDesksSelected");
	}
	
	
	class AddHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			try
			{
				FieldDeskKey publicKey = importPublicKey();
				if(publicKey==null)
					return;
				addFieldDeskKeyToTable(publicKey);
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
				notifyNoneSelected();
				return;
			}
			if(!mainWindow.confirmDlg("RemoveFieldDeskKeys"))
				return;
			
			int rowCount = model.getRowCount();
			for(int i = rowCount-1; i >=0 ; --i)
			{
				if(table.isRowSelected(i))
					model.removeRow(i);
			}
		}
	}

	void addFieldDeskKeyToTable(FieldDeskKey publicKey)
	{
		try
		{
			String publicCode = publicKey.getPublicCode();
			for(int i = 0; i < table.getRowCount(); ++i)
			{
				if(model.getPublicCode(i).equals(publicCode))
				{
					mainWindow.notifyDlg("FieldDeskKeyAlreadyExists");
					return;
				}
			}
			SelectableFieldDeskEntry entry = new SelectableFieldDeskEntry(publicKey);
			HeadquartersKeys defaultHQKeys = mainWindow.getApp().getDefaultHQKeysWithFallback();
			boolean isDefault = defaultHQKeys.containsKey(publicKey.getPublicKey());
			entry.setSelected(isDefault);
			getFieldDeskModel().addNewFieldDeskEntry(entry);
		}
		catch (InvalidBase64Exception e)
		{
			e.printStackTrace();
		}
	}
	
	void updateConfigInfo()
	{
		enableDisableButtons();
		String fieldDeskKeysXml = getFieldDeskModel().getAllKeys().toStringWithLabel();
		ConfigInfo configInfo = mainWindow.getApp().getConfigInfo();
		configInfo.setFieldDeskKeysXml(fieldDeskKeysXml);
		mainWindow.saveConfigInfo();
	}
	
	public FieldDeskKey importPublicKey() throws Exception
	{
		String windowTitle = localization.getWindowTitle("ImportFieldDeskPublicKey");
		String buttonLabel = localization.getButtonLabel("inputImportPublicCodeok");
		
		File currentDirectory = new File(mainWindow.getApp().getCurrentAccountDirectoryName());
		FileFilter filter = new PublicInfoFileFilter();
		UiFileChooser.FileDialogResults results = UiFileChooser.displayFileOpenDialog(mainWindow, windowTitle, null, currentDirectory, buttonLabel, filter);
		if (results.wasCancelChoosen())
			return null;
		
		File importFile = results.getChosenFile();
		String publicKeyString = mainWindow.getApp().extractPublicInfo(importFile);

		String publicCode = MartusCrypto.computePublicCode(publicKeyString);
		if(confirmPublicCode(publicCode, "ImportPublicKey", "AccountCodeWrong"))
		{
			if(!mainWindow.confirmDlg("ImportFieldDeskPublicKey"))
				return null;
		}
		else
			return null;
		String label = askUserForNewLabel(MartusCrypto.computeFormattedPublicCode(publicKeyString), "");
		FieldDeskKey newKey = new FieldDeskKey(publicKeyString, label);
		return newKey;
	}

	String askUserForNewLabel(String publicCode, String previousValue)
	{
		String label = mainWindow.getStringInput("GetFieldDeskLabel", "", publicCode, previousValue);
		if(label == null)
			return null;
		return getUniqueLabel(publicCode, label);
	}

	private String getUniqueLabel(String publicCode, String label) 
	{
		FieldDeskKeys keys = getFieldDeskModel().getAllKeys();
		for(int i = 0; i < keys.size(); ++i)
		{
			FieldDeskKey key = keys.get(i);
			try 
			{
				if(key.getPublicCode().equals(publicCode))
					continue;
			} 
			catch (InvalidBase64Exception e) 
			{
			}
			String configuredLabel = key.getLabel();
			if(configuredLabel.length() >0 && label.equals(configuredLabel))
			{
				mainWindow.notifyDlg("FieldDeskLabelDuplicate");
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

	FieldDeskManagementTableModel getFieldDeskModel()
	{
		return (FieldDeskManagementTableModel) getModel();
	}

}
