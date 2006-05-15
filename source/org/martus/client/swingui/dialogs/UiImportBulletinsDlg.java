/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006, Beneficent
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
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.filechooser.FileFilter;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiNormalTextEditor;
import org.martus.swing.UiButton;
import org.martus.swing.UiFileChooser;
import org.martus.swing.UiLabel;
import org.martus.swing.Utilities;

public class UiImportBulletinsDlg extends JDialog implements ActionListener
{

	public UiImportBulletinsDlg(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse, "", true);
		mainWindow = mainWindowToUse;
		
		fileToImport = getFileToImport();
		if(fileToImport == null)
			return;
		constructDialog();
	}
	
	private File getFileToImport()
	{
		String continueLabel = mainWindow.getLocalization().getButtonLabel("Continue");
		File homeDirectory = UiFileChooser.getHomeDirectoryFile();
		BulletinImportFileFilter importFilter = new BulletinImportFileFilter();
		UiFileChooser.FileDialogResults results = UiFileChooser.displayFileOpenDialog(UiImportBulletinsDlg.this, IMPORT_BULLETINS_TITLE, null, homeDirectory, continueLabel,importFilter);
		if (results.wasCancelChoosen())
			return null;
	
		File importFile = results.getFileChoosen();
		if(!importFile.exists() || !importFile.isFile() )
			return null;
		return importFile;
	}	
	
	class BulletinImportFileFilter extends FileFilter
	{
		public boolean accept(File pathname)
		{
			if(pathname.isDirectory())
				return true;
			return(pathname.getName().endsWith(MartusApp.MARTUS_IMPORT_EXPORT_EXTENSION));
		}

		public String getDescription()
		{
			return mainWindow.getLocalization().getFieldLabel("BulletinImportFiles");
		}
	}
	

	private void constructDialog()
	{
		MartusLocalization localization = mainWindow.getLocalization();
		setTitle(localization.getWindowTitle(IMPORT_BULLETINS_TITLE));
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		
		importingFolder = new UiNormalTextEditor(localization, 40);
		contentPane.add(new UiLabel(localization.getFieldLabel("ImportBulletinsIntoWhichFolder")), BorderLayout.NORTH);
		contentPane.add(importingFolder.getComponent(), BorderLayout.CENTER);
		
		
		ok = new UiButton(localization.getButtonLabel("Continue"));
		ok.addActionListener(this);
		
		UiButton cancel = new UiButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(this);
		Box buttons = Box.createHorizontalBox();
		Utilities.addComponentsRespectingOrientation(buttons, new Component[]{ok, cancel});
		contentPane.add(buttons, BorderLayout.SOUTH);
		
		Utilities.centerDlg(this);
		setResizable(true);
		setVisible(true);
	
	}	
	
	

	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource().equals(ok))
		{
			doImport();
		}
		dispose();
	}
	
	public Map getTokenReplacement() 
	{
		HashMap map = new HashMap();
		map.put("#BulletinsImported#", Integer.toString(1));
		map.put("#ImportFolder#", importingFolder.getText());
		return map;
	}
	
	
	private void doImport()
	{
		try
		{
			mainWindow.notifyDlg(mainWindow, "ImportComplete", getTokenReplacement());
		}
		catch (Exception e)
		{
			mainWindow.notifyDlg("ErrorImportingBulletins");
		}
		
	}

	private static final String IMPORT_BULLETINS_TITLE = "ImportBulletins";
	UiMainWindow mainWindow;
	UiButton ok;
	File fileToImport;
	UiNormalTextEditor importingFolder;
}
	
	
