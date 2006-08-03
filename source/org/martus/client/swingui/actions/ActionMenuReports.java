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
import java.io.StringWriter;
import java.io.Writer;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;

import org.json.JSONObject;
import org.martus.client.core.PartialBulletin;
import org.martus.client.core.SortableBulletinList;
import org.martus.client.reports.ReportFormat;
import org.martus.client.reports.ReportRunner;
import org.martus.client.reports.RunReportOptions;
import org.martus.client.reports.TabularReportBuilder;
import org.martus.client.search.FieldChooserSpecBuilder;
import org.martus.client.search.SearchTreeNode;
import org.martus.client.search.SortFieldChooserSpecBuilder;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.WorkerThread;
import org.martus.client.swingui.dialogs.UiPrintBulletinDlg;
import org.martus.client.swingui.fields.UiPopUpTreeEditor;
import org.martus.clientside.UiLocalization;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.PopUpTreeFieldSpec;
import org.martus.swing.PrintUtilities;
import org.martus.swing.UiButton;
import org.martus.swing.UiCheckBox;
import org.martus.swing.UiFileChooser;
import org.martus.swing.UiLabel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiTable;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.UiWrappedTextPanel;
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
				
				if(rf.getVersion() < ReportFormat.EXPECTED_VERSION)
					mainWindow.notifyDlg("ReportFormatIsOld");
				else if(rf.getVersion() > ReportFormat.EXPECTED_VERSION)
					mainWindow.notifyDlg("ReportFormatIsTooNew");
				
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
	
	ReportFormat createReport() throws Exception
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
		
		String[] sortTags = sortDlg.getSortTags();
		String[] extraTags = {Bulletin.PSEUDOFIELD_ALL_PRIVATE};
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
		
// Do we really have to tell the user here? Would be better to include the count in the following dialog
//		mainWindow.showNumberOfBulletinsFound(bulletinsMatched, "ReportFound");

		PartialBulletin[] unsortedPartialBulletins = sortableList.getUnsortedPartialBulletins();
		boolean isAnyBulletinAllPrivate = areAnyBulletinsAllPrivate(unsortedPartialBulletins);
		UiPrintBulletinDlg dlg = new UiPrintBulletinDlg(mainWindow, isAnyBulletinAllPrivate);
		dlg.setVisible(true);		
		if (!dlg.wasContinueButtonPressed())
			return;			
		
		boolean includePrivateData = dlg.wantsPrivateData();
		boolean sendToDisk = dlg.wantsToPrintToDisk();
		
		options.includePrivate = includePrivateData;

		for(int i = 0; i < unsortedPartialBulletins.length; ++i)
		{
			PartialBulletin pb = unsortedPartialBulletins[i];
			boolean isAllPrivate = FieldSpec.TRUESTRING.equals(pb.getData(Bulletin.PSEUDOFIELD_ALL_PRIVATE));
			if(isAllPrivate && !includePrivateData)
				sortableList.remove(pb);
		}

		if(sendToDisk)
			printToDisk(rf, sortableList, options);
		else
			printToPrinter(rf, sortableList, options);
	}
	
	private boolean areAnyBulletinsAllPrivate(PartialBulletin[] sortedPartialBulletins)
	{
		boolean isAnyBulletinAllPrivate = false;
		for(int i = 0; i < sortedPartialBulletins.length; ++i)
		{
			if(FieldSpec.TRUESTRING.equals(sortedPartialBulletins[i].getData(Bulletin.PSEUDOFIELD_ALL_PRIVATE)))
			{
				isAnyBulletinAllPrivate = true;
				break;
			}
		}
		return isAnyBulletinAllPrivate;
	}

	void printToDisk(ReportFormat rf, SortableBulletinList list, RunReportOptions options) throws Exception
	{
		File destFile = chooseDestinationFile();
		if(destFile == null)
			return;
		
		UnicodeWriter destination = new UnicodeWriter(destFile);
		printToWriter(destination, rf, list, options);
		destination.close();
	}
	
	static class BackgroundPrinter extends WorkerThread
	{
		public BackgroundPrinter(UiMainWindow mainWindowToUse, Writer whereToPrint, ReportFormat reportFormatToUse, 
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
		Writer destination;
		ReportFormat rf;
		SortableBulletinList list;
		RunReportOptions options;
	}

	private void printToWriter(Writer destination, ReportFormat rf, SortableBulletinList list, RunReportOptions options) throws Exception
	{
		BackgroundPrinter worker = new BackgroundPrinter(mainWindow, destination, rf, list, options);
		mainWindow.doBackgroundWork(worker, "BackgroundPrinting");
	}
	
	void printToPrinter(ReportFormat rf, SortableBulletinList list, RunReportOptions options) throws Exception
	{
		StringWriter writer = new StringWriter();
		printToWriter(writer, rf, list, options);
		writer.close();
		
		UiLabel previewText = new UiLabel(writer.toString());
		JComponent scrollablePreview = new JScrollPane(previewText);
		boolean doPreview = false;
		
		if(doPreview)
		{
			JDialog previewDlg = new JDialog(mainWindow);
			previewDlg.getContentPane().add(scrollablePreview);
			previewDlg.setModal(true);
			previewDlg.pack();
			Utilities.centerDlg(previewDlg);
			previewDlg.setVisible(true);
		}
		
		PrintUtilities.printComponent(previewText);
	}

	FieldSpec[] askUserWhichFieldsToInclude()
	{
		while(true)
		{
			ChooseTabularReportFieldsDialog dlg = new ChooseTabularReportFieldsDialog(mainWindow);
			dlg.show();
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
	
	static class ChooseTabularReportFieldsDialog extends JDialog implements ActionListener
	{
		public ChooseTabularReportFieldsDialog(UiMainWindow mainWindow)
		{
			super(mainWindow);
			setModal(true);
			
			String dialogTag = "ChooseTabularReportFields";
			MartusLocalization localization = mainWindow.getLocalization();
			setTitle(localization.getWindowTitle(dialogTag));
			
			fieldSelector = new ReportFieldSelector(mainWindow);
			
			okButton = new UiButton(localization.getButtonLabel("ok"));
			okButton.addActionListener(this);
			UiButton cancelButton = new UiButton(localization.getButtonLabel("cancel"));
			cancelButton.addActionListener(this);
			Box buttonBar = Box.createHorizontalBox();
			Component[] buttons = {Box.createHorizontalGlue(), okButton, cancelButton};
			Utilities.addComponentsRespectingOrientation(buttonBar, buttons);

			JPanel panel = new JPanel(new BorderLayout());
			panel.add(new UiWrappedTextPanel(localization.getFieldLabel(dialogTag)), BorderLayout.BEFORE_FIRST_LINE);
			panel.add(new UiScrollPane(fieldSelector), BorderLayout.CENTER);
			panel.add(buttonBar, BorderLayout.AFTER_LAST_LINE);

			getContentPane().add(panel);
			pack();
			Utilities.centerDlg(this);
		}
		
		public void actionPerformed(ActionEvent e)
		{
			if(e.getSource().equals(okButton))
			{
				selectedSpecs = fieldSelector.getSelectedItems();
			}
			dispose();
		}
		
		public FieldSpec[] getSelectedSpecs()
		{
			return selectedSpecs;
		}
		
		static class ReportFieldSelector extends JPanel
		{
			public ReportFieldSelector(UiMainWindow mainWindow)
			{
				super(new BorderLayout());
				FieldChooserSpecBuilder builder = new FieldChooserSpecBuilder(mainWindow.getLocalization());
				FieldSpec[] rawFieldSpecs = builder.createFieldSpecArray(mainWindow.getStore());
				model = new SpecTableModel(rawFieldSpecs, mainWindow.getLocalization());
				table = new UiTable(model);
				table.setMaxGridWidth(40);
				table.useMaxWidth();
				table.setFocusable(false);
				table.createDefaultColumnsFromModel();
				table.setColumnSelectionAllowed(false);
				add(new JScrollPane(table), BorderLayout.CENTER);
			}
			
			public FieldSpec[] getSelectedItems()
			{
				int[] selectedRows = table.getSelectedRows();
				FieldSpec[] selectedItems = new FieldSpec[selectedRows.length];
				for(int i = 0; i < selectedRows.length; ++i)
					selectedItems[i] = model.getSpec(selectedRows[i]);
				return selectedItems;
			}
			
			SpecTableModel model;
			UiTable table;
		}
			
		
		
		UiButton okButton;
		ReportFieldSelector fieldSelector;
		FieldSpec[] selectedSpecs;
	}
	
	static class SpecTableModel implements TableModel
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
				sortTags = new Vector();
			
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
				String selectedCode = "";
				if(i < sortTags.size())
					selectedCode = spec.findSearchTag((String)sortTags.get(i)).getCode();
				sortChooser[i].setText(selectedCode);
			}
			
			breakChoice = new UiCheckBox(localization.getFieldLabel("ReportIncludeSummaryCounts"));

			Box mainArea = Box.createVerticalBox();
			mainArea.add(multiSortBox);
			mainArea.add(breakChoice);
			
			contentPane.add(mainArea, BorderLayout.CENTER);
			okButton = new UiButton(localization.getButtonLabel("ok"));
			okButton.addActionListener(this);
			UiButton cancelButton = new UiButton(localization.getButtonLabel("cancel"));
			cancelButton.addActionListener(this);
			Box buttonBar = Box.createHorizontalBox();
			buttonBar.add(okButton);
			buttonBar.add(cancelButton);			
			contentPane.add(buttonBar, BorderLayout.AFTER_LAST_LINE);
			
			pack();
			Utilities.centerDlg(this);
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
			sortTags.clear();
			for(int i = 0; i < sortChooser.length; ++i)
			{
				String searchTag = sortChooser[i].getSelectedSearchTag();
				if(searchTag.length() == 0)
					break;
				sortTags.add(searchTag);
				
				System.out.println("ActionMenuReport.getSortTags: " + searchTag);
			}
		}
		
		public String[] getSortTags() throws Exception
		{
			return (String[])sortTags.toArray(new String[0]);
		}
		
		public boolean getPrintBreaks()
		{
			return breakChoice.isSelected();
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
		UiCheckBox breakChoice;
		private static Vector sortTags;
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

