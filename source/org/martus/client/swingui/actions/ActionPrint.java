/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2006, Beneficent
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

package org.martus.client.swingui.actions;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import javax.swing.JComponent;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiPrintBulletinDlg;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinHtmlGenerator;
import org.martus.swing.HtmlViewer;
import org.martus.swing.PrintPage;
import org.martus.swing.PrintUtilities;
import org.martus.swing.UiFileChooser;
import org.martus.util.UnicodeWriter;

public class ActionPrint extends UiMenuAction
{
	public static ActionPrint createWithMenuLabel(UiMainWindow mainWindowToUse)
	{
		return new ActionPrint(mainWindowToUse, "printBulletin");
	}
	
	public static ActionPrint createWithButtonLabel(UiMainWindow mainWindowToUse)
	{
		return new ActionPrint(mainWindowToUse, "printButton");
	}
	
	public ActionPrint(UiMainWindow mainWindowToUse, String label)
	{
		super(mainWindowToUse, label);
	}
	
	public boolean isEnabled()
	{
		return mainWindow.isAnyBulletinSelected();
	}

	public void actionPerformed(ActionEvent ae)
	{
		Vector selectedBulletins = mainWindow.getSelectedBulletins("PrintZeroBulletins");
		if(selectedBulletins == null)
			return;
		printBulletins(selectedBulletins);
		mainWindow.requestFocus();
	}

	void printBulletins(Vector currentSelectedBulletins)
	{
		UiPrintBulletinDlg dlg = new UiPrintBulletinDlg(mainWindow, currentSelectedBulletins);
		dlg.setVisible(true);		
		if (!dlg.wasContinueButtonPressed())
			return;							
		boolean includePrivateData = dlg.wantsPrivateData();
		boolean sendToDisk = dlg.wantsToPrintToDisk();

		if(sendToDisk)
		{
			printToDisk(currentSelectedBulletins, includePrivateData);
		}
		else
		{
			printToPrinter(currentSelectedBulletins, includePrivateData);
		}
	}

	File chooseDestinationFile()
	{
		String title = getLocalization().getWindowTitle("PrintToWhichFile");
		File destination = new File(getLocalization().getFieldLabel("DefaultPrintToDiskFileName"));
		
		while(true)
		{
			UiFileChooser.FileDialogResults results = UiFileChooser.displayFileSaveDialog(mainWindow, title, destination);
			if(results.wasCancelChoosen())
				return null;
			
			destination = results.getChosenFile();
			if(!destination.getName().toLowerCase().endsWith(HTML_FILE_EXTENSION))
				destination = new File(destination.getAbsolutePath() + HTML_FILE_EXTENSION);
			if(!destination.exists())
				break;
			if(mainWindow.confirmDlg(mainWindow, "OverWriteExistingFile"))
				break;
		}
		
		return destination;
	}
	
	private void printToDisk(Vector currentSelectedBulletins, boolean includePrivateData)
	{
		File destination = chooseDestinationFile();
		
		try
		{
			UnicodeWriter writer = new UnicodeWriter(destination);
			try
			{
				writer.writeln("<html>");
				String characterEncoding = "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\">";
				writer.writeln(characterEncoding);
				for(int i=0; i < currentSelectedBulletins.size(); ++i)
				{
					Bulletin bulletin = (Bulletin)currentSelectedBulletins.get(i);
					if(bulletin.isAllPrivate() && !includePrivateData)
						continue;

					int width = 0;
					String html = getBulletinHtml(bulletin, includePrivateData, width);
					writer.write(html);
					writer.writeln("<hr/>");
				}
				writer.writeln("</html>");
			}
			finally
			{
				writer.close();
			}
			mainWindow.notifyDlg(mainWindow, "PrintToDiskComplete");
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void printToPrinter(Vector currentSelectedBulletins, boolean includePrivateData)
	{
		for(int i=0; i < currentSelectedBulletins.size(); ++i)
		{
			Bulletin bulletin = (Bulletin)currentSelectedBulletins.get(i);
			if(bulletin.isAllPrivate() && !includePrivateData)
				continue;

			JComponent view = createBulletinView(bulletin, includePrivateData);
			if(previewForDebugging)
				PrintPage.showPreview(view);
			PrintUtilities.printComponent(view);
		}
	}

	private JComponent createBulletinView(Bulletin bulletin, boolean includePrivateData)
	{
		int width = mainWindow.getPreviewWidth();		
		String html = "<html>" + getBulletinHtml(bulletin, includePrivateData, width) + "</html>";
		JComponent view = getHtmlViewableComponent(html);
		return view;
	}

	static public JComponent getHtmlViewableComponent(String html)
	{
		JComponent view = new HtmlViewer(html,null);
		Dimension preferredSize = view.getPreferredSize();
		// NOTE: you have to set the size of the component first before printing
		// JAVA Bug: We need to also pad the width to prevent clipping this bug was finally fixed in Java 1.5.0-Beta-b32c
		//			 http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4352983
		int fivePercentPadding = (int)(preferredSize.width * 0.05);
		preferredSize.width += fivePercentPadding;
		view.setSize(preferredSize);
		return view;
	}

	private String getBulletinHtml(Bulletin bulletin, boolean includePrivateData, int width)
	{
		getApp().addHQLabelsWherePossible(bulletin.getAuthorizedToReadKeys());
		boolean yourBulletin = bulletin.getAccount().equals(getApp().getAccountId());	
		BulletinHtmlGenerator generator = new BulletinHtmlGenerator(width, getLocalization() );
		String html = generator.getHtmlFragment(bulletin, getStore().getDatabase(), includePrivateData, yourBulletin);
		return html;
	}

	
	static final boolean previewForDebugging = false;
	private static final String HTML_FILE_EXTENSION = ".html";
}
