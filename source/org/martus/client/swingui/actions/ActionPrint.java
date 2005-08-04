/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005, Beneficent
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
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Vector;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.swing.JComponent;
import javax.swing.JDialog;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiPrintBulletinDlg;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinHtmlGenerator;
import org.martus.swing.JComponentVista;
import org.martus.swing.PrintPageFormat;
import org.martus.swing.UiLabel;
import org.martus.swing.UiScrollPane;

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
		if (!dlg.isContinueButtonPressed())
			return;							
		boolean includePrivateData = dlg.isIncludePrivateChecked();
		
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
				showPreview(view);
			printJComponent(view, job, format, attributes);
		}
	}

	private void printJComponent(JComponent view, PrinterJob job, PrintPageFormat format, HashPrintRequestAttributeSet attributes)
	{
		JComponentVista vista = new JComponentVista(view, format);
		vista.scaleToFitX();
		job.setPageable(vista);

		try
		{
			job.print(attributes);
		}
		catch (PrinterException e)
		{
			System.out.println(e);
			e.printStackTrace();
		}
	}

	private void showPreview(JComponent view)
	{
		JDialog previewDlg = new JDialog();
		UiScrollPane scroller = new UiScrollPane();
		scroller.getViewport().add(view);
		previewDlg.getContentPane().add(scroller);
		previewDlg.pack();
		previewDlg.setModal(true);
		previewDlg.setVisible(true);
	}

	private JComponent createBulletinView(Bulletin bulletin, boolean includePrivateData)
	{
		getApp().addHQLabelsWherePossible(bulletin.getAuthorizedToReadKeys());
		boolean yourBulletin = bulletin.getAccount().equals(getApp().getAccountId());	
		int width = mainWindow.getPreviewWidth();		
		BulletinHtmlGenerator generator = new BulletinHtmlGenerator(width, getLocalization() );
		String html = generator.getHtmlString(bulletin, getStore().getDatabase(), includePrivateData, yourBulletin);
		JComponent view = new UiLabel(html);
		return view;
	}

	
	static final boolean previewForDebugging = false; 
}
