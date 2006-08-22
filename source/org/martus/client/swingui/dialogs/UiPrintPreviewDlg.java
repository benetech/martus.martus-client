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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import org.martus.client.reports.ReportOutput;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionPrint;
import org.martus.clientside.UiLocalization;
import org.martus.swing.UiButton;
import org.martus.swing.Utilities;

public class UiPrintPreviewDlg extends JDialog implements ActionListener
{
	
	public UiPrintPreviewDlg(UiMainWindow mainWindowToUse, ReportOutput output)
	{
		super(mainWindowToUse, "", true);
		mainWindow = mainWindowToUse;
		init(output);	
	}
	
	private void init(ReportOutput output)
	{
		UiLocalization localization = mainWindow.getLocalization();
		setTitle(localization.getWindowTitle("PrintPreview"));
		
		printToPrinter = new UiButton(localization.getButtonLabel("PrintToPrinter"));
		printToPrinter.addActionListener(this);		
		printToFile = new UiButton(localization.getButtonLabel("PrintToFile"));
		printToFile.addActionListener(this);		
		cancel = new UiButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(this);	
		
		JComponent previewText = ActionPrint.getHtmlViewableComponent(output.getPageText(0));
		JComponent scrollablePreview = new JScrollPane(previewText);
		scrollablePreview.setBorder(new EmptyBorder(5,5,5,5));
		Box buttons = Box.createHorizontalBox();
		Utilities.addComponentsRespectingOrientation(buttons, new Component[] {Box.createHorizontalGlue(), printToPrinter, printToFile, cancel});

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(scrollablePreview, BorderLayout.CENTER);
		getContentPane().add(buttons, BorderLayout.SOUTH);
		getRootPane().setDefaultButton(printToPrinter);
		Utilities.centerDlg(this);
		setResizable(true);
	}
	
	public boolean wantsPrintToDisk()
	{
		return printToDisk;
	}
	
	public boolean wasCancelButtonPressed()
	{
		return pressedCancel;
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource().equals(printToPrinter))
		{
			printToDisk = false;
			pressedCancel = false;
		}
		if(ae.getSource().equals(printToFile))
		{
			printToDisk = true;
			pressedCancel = false;
		}
		dispose();
	}
	
	UiMainWindow mainWindow;	
	JButton printToPrinter;
	JButton printToFile;
	JButton cancel;

	boolean printToDisk = false;
	boolean pressedCancel = true;
	
	
}
