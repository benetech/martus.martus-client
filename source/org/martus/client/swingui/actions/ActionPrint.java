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

import java.awt.event.ActionEvent;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.swing.JComponent;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiPrintBulletinDlg;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinHtmlGenerator;
import org.martus.swing.PrintPage;
import org.martus.swing.PrintPageFormat;
import org.martus.swing.UiFileChooser;
import org.martus.swing.UiLabel;
import org.martus.util.UnicodeWriter;

public class ActionPrint extends UiMartusAction
{
	public static ActionPrint createWithMenuLabel(UiMainWindow mainWindowToUse)
	{
		String label = mainWindowToUse.getLocalization().getMenuLabel("printBulletin");
		return new ActionPrint(mainWindowToUse, label);
	}
	
	public static ActionPrint createWithButtonLabel(UiMainWindow mainWindowToUse)
	{
		String label = mainWindowToUse.getLocalization().getButtonLabel("print");
		return new ActionPrint(mainWindowToUse, label);
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
	
	private void printToDisk(Vector currentSelectedBulletins, boolean includePrivateData)
	{
		
		String title = getLocalization().getWindowTitle("PrintToWhichFile");
		File destination = new File(getLocalization().getFieldLabel("DefaultPrintToDiskFileName"));
		while(true)
		{
			UiFileChooser.FileDialogResults result = UiFileChooser.displayFileSaveDialog(mainWindow, title, destination);
			if(result.wasCancelChoosen())
				return;
			
			destination = result.getChosenFile();
			if(!destination.exists())
				break;
			if(mainWindow.confirmDlg(mainWindow, "OverWriteExistingFile"))
				break;
		}
		
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
		PrintPageFormat format = new PrintPageFormat();
		PrinterJob job = PrinterJob.getPrinterJob();
		HashPrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
		while(true)
		{
			if (!job.printDialog(attributes))
				return;
			format.setFromAttributes(attributes);
			if(!format.possiblePaperSizeAndTrayMismatch)
				break;
			if(!mainWindow.confirmDlg("PrinterWarning"))
				break;
		}

		for(int i=0; i < currentSelectedBulletins.size(); ++i)
		{
			Bulletin bulletin = (Bulletin)currentSelectedBulletins.get(i);
			if(bulletin.isAllPrivate() && !includePrivateData)
				continue;

			JComponent view = createBulletinView(bulletin, includePrivateData);
			if(previewForDebugging)
				PrintPage.showPreview(view);
			PrintPage.printJComponent(view, job, format, attributes);
		}
	}

	private JComponent createBulletinView(Bulletin bulletin, boolean includePrivateData)
	{
		int width = mainWindow.getPreviewWidth();		
		String html = "<html>" + getBulletinHtml(bulletin, includePrivateData, width) + "</html>";
		JComponent view = new UiLabel(html);
		view.setSize(view.getPreferredSize());
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
}
