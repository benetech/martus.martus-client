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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import org.martus.client.core.BulletinXmlExporter;
import org.martus.client.core.ClientBulletinStore;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.UiScrollPane;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.common.packet.UniversalId;
import org.martus.swing.UiFileChooser;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;
import org.martus.util.UnicodeWriter;

public class UiExportBulletinsDlg extends JDialog implements ActionListener
{
	public UiExportBulletinsDlg(UiMainWindow mainWindowToUse, Vector bulletinsToExport, String defaultName)
	{
		super(mainWindowToUse, "", true);
		mainWindow = mainWindowToUse;
		bulletins = bulletinsToExport;
		defaultFileName = defaultName;
		constructDialog();
	}

	private void constructDialog()
	{
		UiBasicLocalization localization = mainWindow.getLocalization();
		setTitle(localization.getWindowTitle("ExportBulletins"));
		
		includePrivate = new JCheckBox(localization.getFieldLabel("ExportPrivateData"));
		ok = new JButton(localization.getButtonLabel("Continue"));
		ok.addActionListener(this);
		
		cancel = new JButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(this);
		
		Box hBoxButtons = Box.createHorizontalBox();
		hBoxButtons.add(ok);
		hBoxButtons.add(cancel);
		hBoxButtons.add(Box.createHorizontalGlue());
		
		String[] titles = extractTitles(mainWindow, bulletins);
		JList bulletinList = new JList(titles);
		UiScrollPane tocMsgAreaScrollPane = new UiScrollPane(bulletinList,
				UiScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				UiScrollPane.HORIZONTAL_SCROLLBAR_NEVER, localization.getComponentOrientation());
		tocMsgAreaScrollPane.setPreferredSize(new Dimension(580, 100));
		
		Box upperStuff = Box.createVerticalBox();
		upperStuff.add(new JLabel(" "));
		upperStuff.add(new UiWrappedTextArea(localization.getFieldLabel("ExportBulletinDetails")));
		upperStuff.add(new JLabel(" "));
		upperStuff.add(tocMsgAreaScrollPane);
		upperStuff.add(new JLabel(" "));
		upperStuff.add(includePrivate);
		upperStuff.add(new JLabel(" "));
		
		Box upperStuffLeftAligned = Box.createHorizontalBox();
		upperStuffLeftAligned.add(upperStuff);
		upperStuffLeftAligned.add(Box.createHorizontalGlue());
		
		Box vBoxAll = Box.createVerticalBox();
		vBoxAll.add(upperStuffLeftAligned);
		vBoxAll.add(hBoxButtons);
		getContentPane().add(vBoxAll);
		
		Utilities.centerDlg(this);
		setResizable(true);
		show();
	}

	public static Vector findBulletins(ClientBulletinStore store, UniversalId[] selectedBulletins)
	{
		Vector bulletins = new Vector();
		for (int i = 0; i < selectedBulletins.length; i++)
		{
			UniversalId uid = selectedBulletins[i];
			Bulletin b = store.getBulletinRevision(uid);
			bulletins.add(b);
		}
		return bulletins;
	}

	public static String[] extractTitles(UiMainWindow window, Vector bulletins)
	{
		String[] titles = new String[bulletins.size()];
		for (int i = 0; i < titles.length; i++)
		{
			Bulletin b = (Bulletin)bulletins.get(i);
			String bulletinTitle = b.get(BulletinConstants.TAGTITLE);
			if(bulletinTitle == null || bulletinTitle.length() == 0)
				bulletinTitle = window.getLocalization().getFieldLabel("UntitledBulletin");
			titles[i] = bulletinTitle;
		}
		return titles;
	}

	File askForDestinationFile()
	{
		String windowTitle = mainWindow.getLocalization().getWindowTitle("ExportBulletinsSaveAs");
		if(defaultFileName != null && defaultFileName.length() > 0)
			defaultFileName += ".xml";
		else
			defaultFileName = null;
	
		UiFileChooser.FileDialogResults results = UiFileChooser.displayFileSaveDialog(UiExportBulletinsDlg.this, windowTitle, defaultFileName);
		if (results.wasCancelChoosen())
			return null;

		File destFile = results.getFileChoosen();
		if(destFile.exists())
			if(!mainWindow.confirmDlg("OverWriteExistingFile"))
				return null;

		return destFile;
	}

	boolean userWantsToExportPrivate()
	{
		return includePrivate.isSelected();
	}

	void doExport(File destFile)
	{
		try
		{
			UnicodeWriter writer = new UnicodeWriter(destFile);
			BulletinXmlExporter.exportBulletins(writer, bulletins, userWantsToExportPrivate());
			writer.close();
			mainWindow.notifyDlg("ExportComplete");
		}
		catch (IOException e)
		{
			mainWindow.notifyDlg("ErrorWritingFile");
		}
	}

	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource().equals(ok))
		{
			boolean hasUnknown = false;
			for (int i = 0; i < bulletins.size(); i++)
			{
				Bulletin b = (Bulletin)bulletins.get(i);
				if(b.hasUnknownTags() || b.hasUnknownCustomField())
					hasUnknown = true;
			}
			if(hasUnknown)
			{
				if(!mainWindow.confirmDlg("ExportUnknownTags"))
					return;
			}
			
			if(userWantsToExportPrivate())
			{
				if(!mainWindow.confirmDlg("ExportPrivateData"))
					return;
			}

			File destFile = askForDestinationFile();
			if(destFile == null)
				return;

			doExport(destFile);
		}

		dispose();
	}

	UiMainWindow mainWindow;
	Vector bulletins;
	JCheckBox includePrivate;
	JButton ok;
	JButton cancel;
	String defaultFileName;
}
