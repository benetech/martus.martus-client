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
package org.martus.client.swingui.actions;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.json.JSONObject;
import org.martus.client.core.PartialBulletin;
import org.martus.client.core.SortableBulletinList;
import org.martus.client.reports.ReportFormat;
import org.martus.client.reports.ReportRunner;
import org.martus.client.reports.TabularReportBuilder;
import org.martus.client.search.SearchTreeNode;
import org.martus.client.search.SortFieldChooserSpecBuilder;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiPrintBulletinDlg;
import org.martus.client.swingui.fields.UiPopUpTreeEditor;
import org.martus.clientside.UiLocalization;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.database.DatabaseKey;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.PopUpTreeFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.swing.UiButton;
import org.martus.swing.UiFileChooser;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;
import org.martus.swing.UiFileChooser.FileDialogResults;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;


public class ActionMenuReports extends ActionPrint
{
	public ActionMenuReports(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse, "Reports");
	}

	public boolean isEnabled()
	{
		return true;
	}

	public void actionPerformed(ActionEvent ae)
	{
		try
		{
			MiniLocalization localization = mainWindow.getLocalization();
			
			String runButtonLabel = localization.getButtonLabel("RunReport");
			String createButtonLabel = localization.getButtonLabel("CreateReport");
			String cancelButtonLabel = localization.getButtonLabel("cancel");
			String[] buttonLabels = {runButtonLabel, createButtonLabel, cancelButtonLabel, };
			RunOrCreateReportDialog runOrCreate = new RunOrCreateReportDialog(mainWindow, buttonLabels);
			runOrCreate.setVisible(true);
			String pressed = runOrCreate.getPressedButtonLabel();
			if(pressed == null || pressed.equals(cancelButtonLabel))
				return;
			if(pressed.equals(runButtonLabel))
			{
				ReportFormat rf = chooseReport();
				if(rf == null)
					return;
				runReport(rf);
			}
			if(pressed.equals(createButtonLabel))
			{
				ReportFormat rf = createReport();
				if(rf == null)
					return;
				runReport(rf);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			mainWindow.notifyDlgBeep("UnexpectedError");
		}
	}
	
	ReportFormat chooseReport() throws Exception
	{
		String title = getLocalization().getWindowTitle("ChooseReportToRun");
		File directory = mainWindow.getApp().getCurrentAccountDirectory();
		String buttonLabel = getLocalization().getButtonLabel("Select");
		FileFilter filter = new ReportFormatFilter(getLocalization());
		FileDialogResults results = UiFileChooser.displayFileOpenDialog(mainWindow, 
				title, directory, buttonLabel, filter);
		if(results.wasCancelChoosen())
			return null;
		
		UnicodeReader reader = new UnicodeReader(results.getChosenFile());
		String reportFormatText = reader.readAll();
		reader.close();
		
		return new ReportFormat(new JSONObject(reportFormatText));
	}
	
	ReportFormat createReport() throws Exception
	{
		TabularReportBuilder builder = new TabularReportBuilder(getLocalization());
		FieldSpec[] specs = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		ReportFormat rf = builder.createTabular(specs);
		
		String title = getLocalization().getWindowTitle("SaveReportAs");
		File directory = mainWindow.getApp().getCurrentAccountDirectory();
		FileFilter filter = new ReportFormatFilter(getLocalization());
		FileDialogResults results = UiFileChooser.displayFileSaveDialog(mainWindow, 
				title, directory, filter);
		if(results.wasCancelChoosen())
			return null;
		File file = results.getChosenFile();
		if(!file.getName().toLowerCase().endsWith(MRF_FILE_EXTENSION))
			file = new File(file.getAbsolutePath() + MRF_FILE_EXTENSION);
		UnicodeWriter writer = new UnicodeWriter(file);
		writer.write(rf.toJson().toString());
		writer.close();
		return rf;
	}
	
	void runReport(ReportFormat rf) throws Exception
	{
		SearchTreeNode searchTree = mainWindow.askUserForSearchCriteria();
		if(searchTree == null)
			return;
		
		SortFieldsDialog dlg = new SortFieldsDialog(mainWindow);
		dlg.setVisible(true);
		if(!dlg.ok())
			return;
		
		String[] userSortTags = dlg.getSortTags();
		String[] sortTags = new String[userSortTags.length + 1];
		System.arraycopy(userSortTags, 0, sortTags, 0, userSortTags.length);
		sortTags[userSortTags.length] = Bulletin.PSEUDOFIELD_ALL_PRIVATE;

		SortableBulletinList sortableList = mainWindow.doSearch(searchTree, sortTags);
		if(sortableList == null)
			return;
		int bulletinsMatched = sortableList.size();
		if(sortableList.size() == 0)
		{
			mainWindow.notifyDlg("SearchFailed");
			return;
		}
		mainWindow.showNumberOfBulletinsFound(bulletinsMatched, "ReportFound");
		PartialBulletin[] sortedPartialBulletins = sortableList.getSortedPartialBulletins();
		printBulletins(rf, sortedPartialBulletins);
//			Vector bulletinsToReportOn = mainWindow.getBulletins(bulletinIds);
//			printBulletins(bulletinsToReportOn);
	}
	
	void printBulletins(ReportFormat rf, PartialBulletin[] partialBulletinsToPrint) throws Exception
	{
		boolean isAnyBulletinAllPrivate = false;
		for(int i = 0; i < partialBulletinsToPrint.length; ++i)
		{
			if(FieldSpec.TRUESTRING.equals(partialBulletinsToPrint[i].getData(Bulletin.PSEUDOFIELD_ALL_PRIVATE)))
			{
				isAnyBulletinAllPrivate = true;
				break;
			}
		}
		UiPrintBulletinDlg dlg = new UiPrintBulletinDlg(mainWindow, isAnyBulletinAllPrivate);
		dlg.setVisible(true);		
		if (!dlg.wasContinueButtonPressed())
			return;							
		boolean includePrivateData = dlg.wantsPrivateData();
		boolean sendToDisk = dlg.wantsToPrintToDisk();

//		if(sendToDisk)
//		{
//			printToDisk(currentSelectedBulletins, includePrivateData);
//		}
//		else
//		{
//			printToPrinter(currentSelectedBulletins, includePrivateData);
//		}

		if(sendToDisk)
			printToDisk(rf, partialBulletinsToPrint, includePrivateData);
	}
	
	void printToDisk(ReportFormat rf, PartialBulletin[] partialBulletinsToPrint, boolean includePrivate) throws Exception
	{
		File destFile = chooseDestinationFile();
		if(destFile == null)
			return;
		
		UnicodeWriter destination = new UnicodeWriter(destFile);
		Vector keys = new Vector();
		for(int i = 0; i < partialBulletinsToPrint.length; ++i)
		{
			boolean isAllPrivate = FieldSpec.TRUESTRING.equals(partialBulletinsToPrint[i].getData(Bulletin.PSEUDOFIELD_ALL_PRIVATE));
			if(includePrivate || !isAllPrivate)
				keys.add(DatabaseKey.createLegacyKey(partialBulletinsToPrint[i].getUniversalId()));
		}
		ReportRunner rr = new ReportRunner(mainWindow.getApp().getSecurity(), mainWindow.getLocalization());
		rr.runReport(rf, mainWindow.getStore().getDatabase(), keys, destination, includePrivate);
		destination.close();
	}
		
	static class RunOrCreateReportDialog extends JDialog implements ActionListener
	{
		public RunOrCreateReportDialog(UiMainWindow mainWindow, String[] buttonLabels)
		{
			super(mainWindow);
			setModal(true);
			setTitle(mainWindow.getLocalization().getWindowTitle("RunOrCreateReport"));
			Container panel = getContentPane();
			Box buttonBox = Box.createHorizontalBox();
			buttonBox.add(Box.createHorizontalGlue());
			for(int i = 0; i < buttonLabels.length; ++i)
			{
				UiButton button = new UiButton(buttonLabels[i]);
				button.addActionListener(this);
				buttonBox.add(button);
			}
			
			panel.add(buttonBox);
			pack();
			Utilities.centerDlg(this);
		}
		
		public String getPressedButtonLabel()
		{
			return pressedButtonLabel;
		}

		public void actionPerformed(ActionEvent event)
		{
			UiButton button = (UiButton)event.getSource();
			pressedButtonLabel = button.getText();
			dispose();
		}
		
		String pressedButtonLabel;
	}
	
	static class SortFieldsDialog extends JDialog implements ActionListener
	{
		public SortFieldsDialog(UiMainWindow mainWindow)
		{
			super(mainWindow);
			
			if(sortTags == null)
				sortTags = new String[] {Bulletin.TAGENTRYDATE};
			
			setModal(true);
			Container contentPane = getContentPane();
			contentPane.setLayout(new BorderLayout());
			UiLocalization localization = mainWindow.getLocalization();
			setTitle(localization.getWindowTitle("ReportChooseSortFields"));

			String text = localization.getFieldLabel("ReportChooseSortFields");

			SortFieldChooserSpecBuilder builder = new SortFieldChooserSpecBuilder(localization);
			PopUpTreeFieldSpec spec = builder.createSpec(mainWindow.getStore());
			String defaultCode = spec.findSearchTag(sortTags[0]).getCode();
			sortChooser = createSortChooser(mainWindow, spec, defaultCode);
			JPanel sortChooserPanel = new JPanel(new BorderLayout());
			sortChooserPanel.add(sortChooser.getComponent(), BorderLayout.BEFORE_LINE_BEGINS);
			
			okButton = new UiButton(localization.getButtonLabel("ok"));
			okButton.addActionListener(this);
			UiButton cancelButton = new UiButton(localization.getButtonLabel("cancel"));
			cancelButton.addActionListener(this);
			Box buttonBar = Box.createHorizontalBox();
			buttonBar.add(okButton);
			buttonBar.add(cancelButton);

			contentPane.add(new UiWrappedTextArea(text), BorderLayout.BEFORE_FIRST_LINE);
			contentPane.add(sortChooserPanel, BorderLayout.CENTER);
			contentPane.add(buttonBar, BorderLayout.AFTER_LAST_LINE);
			
			pack();
			Utilities.centerDlg(this);
		}

		private UiPopUpTreeEditor createSortChooser(UiMainWindow mainWindow, PopUpTreeFieldSpec spec, String defaultCode)
		{
			UiLocalization localization = mainWindow.getLocalization();
			UiPopUpTreeEditor chooser = new UiPopUpTreeEditor(localization);
			chooser.setSpec(spec);
			chooser.setText(defaultCode);
			return chooser;
		}
		
		void memorizeSortFields()
		{
			String searchTag = sortChooser.getSelectedSearchTag();
			
			System.out.println("ActionMenuReport.getSortTags: " + searchTag);
			sortTags = new String[] {searchTag};
		}
		
		public String[] getSortTags() throws Exception
		{
			return sortTags;
		}

		public void actionPerformed(ActionEvent e)
		{
			if(e.getSource().equals(okButton))
			{
				memorizeSortFields();
				hitOk = true;
			}
			dispose();
		}
		
		public boolean ok()
		{
			return hitOk;
		}

		UiPopUpTreeEditor sortChooser;
		boolean hitOk;
		UiButton okButton;
		private static String[] sortTags;
	}

	class ReportFormatFilter extends FileFilter
	{
		public ReportFormatFilter(MiniLocalization localizationToUse)
		{
			localization = localizationToUse;
		}
		
		public boolean accept(File f)
		{
			return (f.getName().toLowerCase().endsWith(MRF_FILE_EXTENSION));
		}

		public String getDescription()
		{
			return localization.getFieldLabel("MartusReportFormat");
		}

		MiniLocalization localization;
	}

	private static final String MRF_FILE_EXTENSION = ".mrf";

}

