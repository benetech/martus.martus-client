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

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.filechooser.FileFilter;

import org.json.JSONObject;
import org.martus.client.core.PartialBulletin;
import org.martus.client.core.SortableBulletinList;
import org.martus.client.reports.PageReportBuilder;
import org.martus.client.reports.ReportAnswers;
import org.martus.client.reports.ReportFormat;
import org.martus.client.reports.ReportFormatFilter;
import org.martus.client.reports.ReportOutput;
import org.martus.client.reports.ReportRunner;
import org.martus.client.reports.RunReportOptions;
import org.martus.client.reports.TabularReportBuilder;
import org.martus.client.search.SearchTreeNode;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.WorkerThread;
import org.martus.client.swingui.dialogs.UIReportFieldDlg;
import org.martus.client.swingui.dialogs.UiIncludePrivateDataDlg;
import org.martus.client.swingui.dialogs.UiPrintPreviewDlg;
import org.martus.client.swingui.dialogs.UiReportFieldChooserDlg;
import org.martus.client.swingui.dialogs.UiReportFieldOrganizerDlg;
import org.martus.client.swingui.dialogs.UiRunOrCreateReportDlg;
import org.martus.client.swingui.dialogs.UiSortFieldsDlg;
import org.martus.clientside.FileDialogHelpers;
import org.martus.clientside.FormatFilter;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.swing.PrintUtilities;
import org.martus.swing.UiLabel;
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
			String createTabularReportButtonLabel = localization.getButtonLabel("CreateTabularReport");
			String createPageReportButtonLabel = localization.getButtonLabel("CreatePageReport");
			String cancelButtonLabel = localization.getButtonLabel("cancel");
			String[] buttonLabels = {runButtonLabel, createTabularReportButtonLabel, createPageReportButtonLabel, cancelButtonLabel, };
			UiRunOrCreateReportDlg runOrCreate = new UiRunOrCreateReportDlg(mainWindow, buttonLabels);
			runOrCreate.setVisible(true);
			String pressed = runOrCreate.getPressedButtonLabel();
			if(pressed == null || pressed.equals(cancelButtonLabel))
				return;
			if(pressed.equals(runButtonLabel))
			{
				ReportFormat rf = chooseReport();
				if(rf == null)
					return;
				
				if(rf.getVersion() < ReportFormat.EXPECTED_VERSION)
					mainWindow.notifyDlg("ReportFormatIsOld");
				else if(rf.getVersion() > ReportFormat.EXPECTED_VERSION)
					mainWindow.notifyDlg("ReportFormatIsTooNew");
				
				runReport(rf);
			}
			if(pressed.equals(createTabularReportButtonLabel))
			{
				ReportFormat rf = createTabularReport();
				if(rf == null)
					return;
				runReport(rf);
			}
			if(pressed.equals(createPageReportButtonLabel))
			{
				ReportFormat rf = createPageReport();
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
		UiMainWindow owner = mainWindow;
		String title = getLocalization().getWindowTitle("ChooseReportToRun");
		String okButtonLabel = getLocalization().getButtonLabel("SelectReport");
		File directory = owner.getApp().getCurrentAccountDirectory();
		FileFilter filter = new ReportFormatFilter(getLocalization());
		File chosenFile = FileDialogHelpers.doFileOpenDialog(owner, title, okButtonLabel, directory, filter);
		
		if(chosenFile == null)
			return null;
		
		UnicodeReader reader = new UnicodeReader(chosenFile);
		String reportFormatText = reader.readAll();
		reader.close();
		
		return new ReportFormat(new JSONObject(reportFormatText));
	}

	ReportFormat createTabularReport() throws Exception
	{
		TabularReportBuilder builder = new TabularReportBuilder(getLocalization());
		MiniFieldSpec[] specs = askUserWhichFieldsToInclude(ReportAnswers.TABULAR_REPORT);
		if(specs == null)
			return null;
		
		ReportAnswers answers = new ReportAnswers(ReportAnswers.TABULAR_REPORT, specs);
		ReportFormat rf = builder.createTabular(answers.getSpecs());
		
		File file = askForReportFileToSaveTo();
		if(file == null)
			return null;
		
		UnicodeWriter writer = new UnicodeWriter(file);
		writer.write(rf.toJson().toString());
		writer.close();
		return rf;
	}
	
	ReportFormat createPageReport() throws Exception
	{
		PageReportBuilder builder = new PageReportBuilder(getLocalization());
		MiniFieldSpec[] specs = askUserWhichFieldsToInclude(ReportAnswers.PAGE_REPORT);
		if(specs == null)
			return null;
		
		ReportAnswers answers = new ReportAnswers(ReportAnswers.PAGE_REPORT, specs);
		ReportFormat rf = builder.createPageReport(answers.getSpecs());
		
		File file = askForReportFileToSaveTo();
		if(file == null)
			return null;
		
		UnicodeWriter writer = new UnicodeWriter(file);
		writer.write(rf.toJson().toString());
		writer.close();
		return rf;
	}
	
	File askForReportFileToSaveTo()
	{
		String title = getLocalization().getWindowTitle("SaveReportAs");
		File directory = mainWindow.getApp().getCurrentAccountDirectory();
		FormatFilter filter = new ReportFormatFilter(getLocalization());
		MartusLocalization localization = mainWindow.getLocalization();
		return FileDialogHelpers.doFileSaveDialog(mainWindow, title, directory, filter, localization);
	}

	void runReport(ReportFormat rf) throws Exception
	{
		SearchTreeNode searchTree = mainWindow.askUserForSearchCriteria();
		if(searchTree == null)
			return;
		
		UiSortFieldsDlg sortDlg = new UiSortFieldsDlg(mainWindow);
		sortDlg.setVisible(true);
		if(!sortDlg.ok())
			return;
		
		MiniFieldSpec[] sortTags = sortDlg.getSelectedMiniFieldSpecs();
		FieldSpec allPrivateSpec = FieldSpec.createStandardField(Bulletin.PSEUDOFIELD_ALL_PRIVATE, new FieldTypeBoolean());
		MiniFieldSpec allPrivateMiniSpec = new MiniFieldSpec(allPrivateSpec);
		MiniFieldSpec[] extraTags = {allPrivateMiniSpec};
		SortableBulletinList sortableList = mainWindow.doSearch(searchTree, sortTags, extraTags);
		if(sortableList == null)
			return;
		
		if(sortableList.size() == 0)
		{
			mainWindow.notifyDlg("SearchFailed");
			return;
		}
	
		RunReportOptions options = new RunReportOptions();
		options.printBreaks = sortDlg.getPrintBreaks();
		options.hideDetail = sortDlg.getHideDetail();
		
		PartialBulletin[] unsortedPartialBulletins = sortableList.getUnsortedPartialBulletins();
		int allPrivateBulletinCount = getNumberOfAllPrivateBulletins(unsortedPartialBulletins);
		UiIncludePrivateDataDlg dlg = new UiIncludePrivateDataDlg(mainWindow, unsortedPartialBulletins.length, allPrivateBulletinCount);
		dlg.setVisible(true);		
		if (dlg.wasCancelButtonPressed())
			return;			
		
		options.includePrivate = dlg.wantsPrivateData();
		if(!options.includePrivate)
		{
			for(int i = 0; i < unsortedPartialBulletins.length; ++i)
			{
				PartialBulletin pb = unsortedPartialBulletins[i];
				boolean isAllPrivate = FieldSpec.TRUESTRING.equals(pb.getData(Bulletin.PSEUDOFIELD_ALL_PRIVATE));
				if(isAllPrivate)
					sortableList.remove(pb);
			}
		}

		ReportOutput result = new ReportOutput();
		printToWriter(result, rf, sortableList, options);
		result.close();

		UiPrintPreviewDlg printPreview = new UiPrintPreviewDlg(mainWindow, result);
		printPreview.setVisible(true);		
		if(printPreview.wasCancelButtonPressed())
			return;			
		boolean sendToDisk = printPreview.wantsPrintToDisk();
		
		boolean didPrint;
		if(sendToDisk)
			didPrint = printToDisk(result);				
		else
			didPrint = printToPrinter(result);
			
		if(didPrint)
			mainWindow.notifyDlg("PrintCompleted");
			
	}
	
	private int getNumberOfAllPrivateBulletins(PartialBulletin[] sortedPartialBulletins)
	{
		int numberOfAllPrivate = 0;
		for(int i = 0; i < sortedPartialBulletins.length; ++i)
		{
			if(FieldSpec.TRUESTRING.equals(sortedPartialBulletins[i].getData(Bulletin.PSEUDOFIELD_ALL_PRIVATE)))
			{
				++numberOfAllPrivate;
			}
		}
		return numberOfAllPrivate;
	}

	boolean printToDisk(ReportOutput output) throws Exception
	{
		File destFile = chooseDestinationFile();
		if(destFile == null)
			return false;

		UnicodeWriter destination = new UnicodeWriter(destFile);
		for(int page = 0; page < output.getPageCount(); ++page)
			destination.write(output.getPageText(page));
		destination.close();
		return true;
	}
	
	static class BackgroundPrinter extends WorkerThread
	{
		public BackgroundPrinter(UiMainWindow mainWindowToUse, ReportOutput whereToPrint, ReportFormat reportFormatToUse, 
				SortableBulletinList listToPrint, RunReportOptions optionsToUse)
		{
			mainWindow = mainWindowToUse;
			destination = whereToPrint;
			rf = reportFormatToUse;
			list = listToPrint;
			options = optionsToUse;
		}
		
		public void doTheWorkWithNO_SWING_CALLS() throws Exception
		{
			ReportRunner rr = new ReportRunner(mainWindow.getApp().getSecurity(), mainWindow.getLocalization());
			rr.runReport(rf, mainWindow.getStore().getDatabase(), list, destination, options);
		}
		
		UiMainWindow mainWindow;
		ReportOutput destination;
		ReportFormat rf;
		SortableBulletinList list;
		RunReportOptions options;
	}

	private void printToWriter(ReportOutput destination, ReportFormat rf, SortableBulletinList list, RunReportOptions options) throws Exception
	{
		BackgroundPrinter worker = new BackgroundPrinter(mainWindow, destination, rf, list, options);
		mainWindow.doBackgroundWork(worker, "BackgroundPrinting");
	}
	
	boolean printToPrinter(ReportOutput output) throws Exception
	{
		UiLabel previewText = new UiLabel();
		for(int page = 0; page < output.getPageCount(); ++page)
		{
			previewText.setText(output.getPageText(0));
			// NOTE: you have to set the size of the component first before printing
			previewText.setSize(previewText.getPreferredSize());
			PrintUtilities.printComponent(previewText);
		}
		return true;
	}

	//TODO: Instead of passing in a constant pass in a Factory instead which will create the correct dialog
	MiniFieldSpec[] askUserWhichFieldsToInclude(ReportAnswers.ReportType reportType)
	{
		while(true)
		{
			UIReportFieldDlg dlg;
			if(reportType.isPage())
				dlg = new UiReportFieldChooserDlg(mainWindow);
			else if(reportType.isTabular())
				dlg = new UiReportFieldOrganizerDlg(mainWindow);
			else
				return null;
			dlg.setVisible(true);
			FieldSpec[] selectedSpecs = dlg.getSelectedSpecs();
			if(selectedSpecs == null)
				return null;
			if(selectedSpecs.length == 0)
			{
				mainWindow.notifyDlg(mainWindow, "NoReportFieldsSelected");
				continue;
			}
			MiniFieldSpec[] specs = new MiniFieldSpec[selectedSpecs.length];
			for(int i = 0; i < specs.length; ++i)
				specs[i] = new MiniFieldSpec(selectedSpecs[i]);
			return specs;
		}
	}
	
}

