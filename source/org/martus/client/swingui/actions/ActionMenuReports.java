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
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;

import org.json.JSONObject;
import org.martus.client.core.PartialBulletin;
import org.martus.client.core.SortableBulletinList;
import org.martus.client.reports.PageReportBuilder;
import org.martus.client.reports.ReportFormat;
import org.martus.client.reports.ReportOutput;
import org.martus.client.reports.ReportRunner;
import org.martus.client.reports.RunReportOptions;
import org.martus.client.reports.TabularReportBuilder;
import org.martus.client.search.SearchTreeNode;
import org.martus.client.search.SortFieldChooserSpecBuilder;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.WorkerThread;
import org.martus.client.swingui.dialogs.UiReportFieldChooserDlg;
import org.martus.client.swingui.dialogs.UiIncludePrivateDataDlg;
import org.martus.client.swingui.dialogs.UiPrintPreviewDlg;
import org.martus.client.swingui.fields.UiPopUpTreeEditor;
import org.martus.clientside.UiLocalization;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.fieldspec.PopUpTreeFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.swing.PrintUtilities;
import org.martus.swing.UiButton;
import org.martus.swing.UiComboBox;
import org.martus.swing.UiFileChooser;
import org.martus.swing.UiLabel;
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
			String createTabularReportButtonLabel = localization.getButtonLabel("CreateTabularReport");
			String createPageReportButtonLabel = localization.getButtonLabel("CreatePageReport");
			String cancelButtonLabel = localization.getButtonLabel("cancel");
			String[] buttonLabels = {runButtonLabel, createTabularReportButtonLabel, createPageReportButtonLabel, cancelButtonLabel, };
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
		File chosenFile = null;
		String title = getLocalization().getWindowTitle("ChooseReportToRun");
		File directory = mainWindow.getApp().getCurrentAccountDirectory();
		String buttonLabel = getLocalization().getButtonLabel("SelectReport");
		FileFilter filter = new ReportFormatFilter(getLocalization());
		while(true)
		{
			FileDialogResults results = UiFileChooser.displayFileOpenDialog(mainWindow, 
					title, directory, buttonLabel, filter);
			if(results.wasCancelChoosen())
				return null;
			
			chosenFile = results.getChosenFile();
			if(!chosenFile.exists())
				continue;
			
			if(!chosenFile.isDirectory())
				break;
			
			directory = chosenFile;
		}
		
		UnicodeReader reader = new UnicodeReader(chosenFile);
		String reportFormatText = reader.readAll();
		reader.close();
		
		return new ReportFormat(new JSONObject(reportFormatText));
	}
	
	ReportFormat createTabularReport() throws Exception
	{
		TabularReportBuilder builder = new TabularReportBuilder(getLocalization());
		FieldSpec[] specs = askUserWhichFieldsToInclude();
		if(specs == null)
			return null;
		
		ReportFormat rf = builder.createTabular(specs);
		
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
		FieldSpec[] specs = askUserWhichFieldsToInclude();
		if(specs == null)
			return null;
		
		MiniFieldSpec[] miniSpecs = new MiniFieldSpec[specs.length];
		for(int i = 0; i < miniSpecs.length; ++i)
		{
			FieldSpec spec = specs[i];
			if(StandardFieldSpecs.isStandardFieldTag(spec.getTag()))
				spec = FieldSpec.createStandardField(spec.getTag(), spec.getType());
			miniSpecs[i] = new MiniFieldSpec(spec);
		}
		
		ReportFormat rf = builder.createPageReport(miniSpecs);
		
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
		FileFilter filter = new ReportFormatFilter(getLocalization());
		File file = null;
		while(true)
		{
			FileDialogResults results = UiFileChooser.displayFileSaveDialog(mainWindow, 
					title, directory, filter);
			if(results.wasCancelChoosen())
				return null;
			file = results.getChosenFile();
			if(!file.getName().toLowerCase().endsWith(MRF_FILE_EXTENSION))
				file = new File(file.getAbsolutePath() + MRF_FILE_EXTENSION);
			if(!file.exists())
				break;
			if(mainWindow.confirmDlg(mainWindow, "OverWriteExistingFile"))
				break;
		}
		
		return file;
	}
	
	void runReport(ReportFormat rf) throws Exception
	{
		SearchTreeNode searchTree = mainWindow.askUserForSearchCriteria();
		if(searchTree == null)
			return;
		
		SortFieldsDialog sortDlg = new SortFieldsDialog(mainWindow);
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

	FieldSpec[] askUserWhichFieldsToInclude()
	{
		while(true)
		{
			UiReportFieldChooserDlg dlg = new UiReportFieldChooserDlg(mainWindow);
			dlg.setVisible(true);
			FieldSpec[] selectedSpecs = dlg.getSelectedSpecs();
			if(selectedSpecs == null)
				return null;
			if(selectedSpecs.length == 0)
			{
				mainWindow.notifyDlg(mainWindow, "NoReportFieldsSelected");
				continue;
			}
			return selectedSpecs;
		}
	}

	
	static public class SpecTableModel implements TableModel
	{
		public SpecTableModel(FieldSpec[] specsToUse, MiniLocalization localizationToUse)
		{
			specs = specsToUse;
			localization = localizationToUse;
		}
		
		public FieldSpec getSpec(int row)
		{
			return specs[row];
		}
		
		public int getColumnCount()
		{
			return columnTags.length;
		}

		public String getColumnName(int column)
		{
			return localization.getButtonLabel(columnTags[column]);
		}

		public int getRowCount()
		{
			return specs.length;
		}

		public Object getValueAt(int row, int column)
		{
			FieldSpec spec = getSpec(row);
			switch(column)
			{
				case 0: return spec.getLabel();
				case 1: return localization.getFieldLabel("FieldType" + spec.getType().getTypeName());
				case 2: return spec.getTag();
				default: throw new RuntimeException("Unknown column: " + column);
			}
		}

		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return false;
		}

		public Class getColumnClass(int columnIndex)
		{
			return String.class;
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			throw new RuntimeException("Not supported");
		}

		public void addTableModelListener(TableModelListener l)
		{
			return;
		}

		public void removeTableModelListener(TableModelListener l)
		{
			return;
		}
		
		static final String[] columnTags = {"FieldLabel", "FieldType", "FieldTag"};

		FieldSpec[] specs;
		MiniLocalization localization;
	}
	
	static class RunOrCreateReportDialog extends JDialog implements ActionListener
	{
		public RunOrCreateReportDialog(UiMainWindow mainWindow, String[] buttonLabels)
		{
			super(mainWindow);
			setModal(true);
			setTitle(mainWindow.getLocalization().getWindowTitle("RunOrCreateReport"));
			Container panel = getContentPane();
			Box buttonBox = Box.createVerticalBox();
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
			
			if(sortMiniSpecs == null)
				sortMiniSpecs = new Vector();
			
			setModal(true);
			Container contentPane = getContentPane();
			contentPane.setLayout(new BorderLayout());
			UiLocalization localization = mainWindow.getLocalization();
			setTitle(localization.getWindowTitle("ReportChooseSortFields"));

			String text = localization.getFieldLabel("ReportChooseSortFields");

			SortFieldChooserSpecBuilder builder = new SortFieldChooserSpecBuilder(localization);
			PopUpTreeFieldSpec spec = builder.createSpec(mainWindow.getStore());
			
			contentPane.add(new UiWrappedTextArea(text), BorderLayout.BEFORE_FIRST_LINE);

			Box multiSortBox = Box.createVerticalBox();
			
			sortChooser = new UiPopUpTreeEditor[MAX_SORT_LEVELS];
			for(int i = 0; i < sortChooser.length; ++i)
			{
				sortChooser[i] = createSortChooser(mainWindow, spec);
				multiSortBox.add(sortChooser[i].getComponent());
			}
			
			for(int i = 0; i < sortChooser.length; ++i)
			{
				MiniFieldSpec selectedSpec = null;
				if(i < sortMiniSpecs.size())
					selectedSpec = (MiniFieldSpec)sortMiniSpecs.get(i);
				sortChooser[i].select(selectedSpec);
			}
			
			detailOnlyChoice = createChoiceItem("ReportDetailOnly", localization);
			detailAndBreaksChoice = createChoiceItem("ReportDetailWithSummaries", localization);
			breaksOnlyChoice = createChoiceItem("ReportSummariesOnly", localization);
			ChoiceItem[] breakChoices = {
				detailOnlyChoice,
				detailAndBreaksChoice,
				breaksOnlyChoice,
			};
			breakChoice = new UiComboBox(breakChoices);

			if(savedBreakChoice == null)
				breakChoice.setSelectedIndex(0);
			else
				breakChoice.setSelectedItem(savedBreakChoice);
				
			Box mainArea = Box.createVerticalBox();
			mainArea.add(multiSortBox);
			mainArea.add(breakChoice);
			
			contentPane.add(mainArea, BorderLayout.CENTER);
			okButton = new UiButton(localization.getButtonLabel("ok"));
			okButton.addActionListener(this);
			UiButton cancelButton = new UiButton(localization.getButtonLabel("cancel"));
			cancelButton.addActionListener(this);
			Box buttonBar = Box.createHorizontalBox();
			Component[] buttons = new Component[] {Box.createHorizontalGlue(), okButton, cancelButton};
			Utilities.addComponentsRespectingOrientation(buttonBar, buttons);
			contentPane.add(buttonBar, BorderLayout.AFTER_LAST_LINE);
			getRootPane().setDefaultButton(okButton);
			
			pack();
			Utilities.centerDlg(this);
		}
		
		private ChoiceItem createChoiceItem(String tag, MiniLocalization localization)
		{
			return new ChoiceItem(tag, localization.getFieldLabel(tag));
		}

		private UiPopUpTreeEditor createSortChooser(UiMainWindow mainWindow, PopUpTreeFieldSpec spec)
		{
			UiLocalization localization = mainWindow.getLocalization();
			UiPopUpTreeEditor chooser = new UiPopUpTreeEditor(localization);
			chooser.setSpec(spec);
			return chooser;
		}
		
		void memorizeSortFields()
		{			
			sortMiniSpecs.clear();
			for(int i = 0; i < sortChooser.length; ++i)
			{
				MiniFieldSpec spec = sortChooser[i].getSelectedMiniFieldSpec();
				if(spec.getTag().length() == 0)
					break;
				sortMiniSpecs.add(spec);
				
				System.out.println("ActionMenuReport.getSortTags: " + spec);
			}
			savedBreakChoice = (ChoiceItem)breakChoice.getSelectedItem();
		}
		
		public MiniFieldSpec[] getSelectedMiniFieldSpecs() throws Exception
		{
			return (MiniFieldSpec[])sortMiniSpecs.toArray(new MiniFieldSpec[0]);
		}
		
		public boolean getPrintBreaks()
		{
			return !detailOnlyChoice.equals(breakChoice.getSelectedItem());
		}
		
		public boolean getHideDetail()
		{
			return breaksOnlyChoice.equals(breakChoice.getSelectedItem());
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

		UiPopUpTreeEditor[] sortChooser;
		boolean hitOk;
		UiButton okButton;
		UiComboBox breakChoice;
		ChoiceItem detailOnlyChoice;
		ChoiceItem detailAndBreaksChoice;
		ChoiceItem breaksOnlyChoice;

		private static Vector sortMiniSpecs;
		private static ChoiceItem savedBreakChoice;
	}

	class ReportFormatFilter extends FileFilter
	{
		public ReportFormatFilter(MiniLocalization localizationToUse)
		{
			localization = localizationToUse;
		}
		
		public boolean accept(File f)
		{
			if(f.isDirectory())
				return true;
			return (f.getName().toLowerCase().endsWith(MRF_FILE_EXTENSION));
		}

		public String getDescription()
		{
			return localization.getFieldLabel("MartusReportFormat");
		}

		MiniLocalization localization;
	}

	private static final String MRF_FILE_EXTENSION = ".mrf";
	private static final int MAX_SORT_LEVELS = 3;
}

