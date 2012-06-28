/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2012, Beneficent
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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.DateTitle;
import org.jfree.chart.title.ShortTextTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.martus.client.core.PartialBulletin;
import org.martus.client.core.SortableBulletinList;
import org.martus.client.reports.ChartAnswers;
import org.martus.client.search.FieldChooserSpecBuilder;
import org.martus.client.search.SaneCollator;
import org.martus.client.search.SearchFieldTreeNode;
import org.martus.client.search.SearchTreeNode;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiChartPreviewDlg;
import org.martus.client.swingui.dialogs.UiPushbuttonsDlg;
import org.martus.client.swingui.fields.UiPopUpFieldChooserEditor;
import org.martus.common.MartusLogger;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.fieldspec.PopUpTreeFieldSpec;
import org.martus.common.fieldspec.SearchFieldTreeModel;
import org.martus.common.fieldspec.SearchableFieldChoiceItem;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.swing.UiFileChooser;
import org.martus.util.TokenReplacement;

public class ActionMenuCharts extends UiMenuAction
{
	public ActionMenuCharts(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse, "Charts");
	}

	@Override
	public void actionPerformed(ActionEvent events)
	{
		try
		{
			MartusLocalization localization = mainWindow.getLocalization();
			
			String runButtonLabel = localization.getButtonLabel("RunChart");
//			String createChartButtonLabel = localization.getButtonLabel("CreateChart");
			String cancelButtonLabel = localization.getButtonLabel("cancel");
			String[] buttonLabels = {runButtonLabel, /*createChartButtonLabel,*/ cancelButtonLabel, };
			String title = mainWindow.getLocalization().getWindowTitle("RunOrCreateChart");
			UiPushbuttonsDlg runOrCreate = new UiPushbuttonsDlg(mainWindow, title, buttonLabels);
			runOrCreate.setVisible(true);
			String pressed = runOrCreate.getPressedButtonLabel();
			if(pressed == null || pressed.equals(cancelButtonLabel))
				return;
			
			FieldChooserSpecBuilder specBuilder = new FieldChooserSpecBuilder(getLocalization());
			PopUpTreeFieldSpec treeSpec = specBuilder.createSpec(getStore());
			removeGridFields(treeSpec);
			FieldSpec dateEnteredSpec = StandardFieldSpecs.findStandardFieldSpec(BulletinConstants.TAGENTRYDATE);
			SearchableFieldChoiceItem initialChoice = new SearchableFieldChoiceItem(dateEnteredSpec);
			String initialCode = initialChoice.getCode();
			
			JDialog testDialog = new JDialog();
			testDialog.setSize(200,200);
			DefaultMutableTreeNode selectedNode = UiPopUpFieldChooserEditor.askUserForField(testDialog, new Point(0,0), treeSpec, initialCode, localization);
			if(selectedNode == null)
				return;
			
			SearchableFieldChoiceItem selectedItem = (SearchableFieldChoiceItem)selectedNode.getUserObject();
//			String label = selectedNode.toString();

			FieldSpec selectedSpec = selectedItem.getSpec();
			MiniFieldSpec fieldToCount = new MiniFieldSpec(selectedSpec);
			ChartAnswers answers = new ChartAnswers(fieldToCount, getLocalization());
			answers.setSubtitle("User-entered subtitle here");
			
//			if(pressed.equals(runButtonLabel))
//			{
//				answers = chooseAndLoad();
//			}
//			if(pressed.equals(createChartButtonLabel))
//			{
//				answers = createAndSave();
//			}
//
//			if(answers == null)
//				return;
			
			runChart(answers);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			mainWindow.notifyDlgBeep("UnexpectedError");
		}
	}

	private void removeGridFields(PopUpTreeFieldSpec treeSpec)
	{
		SearchFieldTreeModel model = treeSpec.getTreeModel();
		SearchFieldTreeNode rootNode = (SearchFieldTreeNode) model.getRoot();
		for(int i = rootNode.getChildCount() - 1; i >= 0; --i)
		{
			SearchFieldTreeNode fieldNode = (SearchFieldTreeNode) rootNode.getChildAt(i);
			SearchableFieldChoiceItem fieldChoiceItem = fieldNode.getChoiceItem();
			FieldSpec spec = fieldChoiceItem.getSpec();
			if(spec.getParent() != null)
				rootNode.remove(i);
		}
	}

	private void runChart(ChartAnswers answers)
	{
		try
		{
			SearchTreeNode searchTree = getMainWindow().askUserForSearchCriteria();
			if(searchTree == null)
				return;
			
			MiniFieldSpec fieldToCount = answers.getFieldToCount();
			MiniFieldSpec[] extraSpecs = new MiniFieldSpec[] { fieldToCount };
			SortableBulletinList sortableList = getMainWindow().doSearch(searchTree, extraSpecs, new MiniFieldSpec[]{}, "ReportSearchProgress");
			if(sortableList == null)
				return;

			HashMap<String, Integer> counts = extractBulletinCounts(fieldToCount, sortableList);

			String selectedFieldLabel = fieldToCount.getLabel();
			if(selectedFieldLabel.equals(""))
				selectedFieldLabel = getLocalization().getFieldLabel(fieldToCount.getTag());

			// TODO: Use or delete these
//			ChartRenderingInfo info = new ChartRenderingInfo();
//			EntityCollection entities = new StandardEntityCollection();
			
//			JFreeChart bar3dChart = create3DBarChart(counts, labelText);
			
			JFreeChart chart = createBarChart(counts, selectedFieldLabel);

			chart.addSubtitle(new TextTitle(answers.getSubtitle()));
			
			String today = getLocalization().formatDateTime(new Date().getTime());
			String chartCreatedOnLabel = getLocalization().getFieldLabel("ChartCreatedOn");
			chartCreatedOnLabel = TokenReplacement.replaceToken(chartCreatedOnLabel, "#Date#", today);
			chart.addSubtitle(new ShortTextTitle(chartCreatedOnLabel));
			
			chart.removeSubtitle(new DateTitle());
			
			UiChartPreviewDlg preview = new UiChartPreviewDlg(getMainWindow(), chart);
			preview.setVisible(true);		
			if(preview.wasCancelButtonPressed())
				return;			
			boolean sendToDisk = preview.wantsPrintToDisk();
			
			boolean didPrint = false;
			if(sendToDisk)
				didPrint = printToDisk(chart);
//			else
//				didPrint = printToPrinter(chart);
				
			if(didPrint)
				mainWindow.notifyDlg("ChartCompleted");
		}
		catch(Exception e)
		{
			MartusLogger.logException(e);
		}
	}

	private HashMap<String, Integer> extractBulletinCounts(MiniFieldSpec selectedSpec, SortableBulletinList sortableList)
	{
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		
		PartialBulletin[] partialBulletins = sortableList.getUnsortedPartialBulletins();
		for (PartialBulletin partialBulletin : partialBulletins)
		{
			String data = partialBulletin.getData(selectedSpec.getTag());
			Integer oldCount = counts.get(data);
			if(oldCount == null)
				oldCount = 0;
			int newCount = oldCount + 1;
			counts.put(data, newCount);
		}
		return counts;
	}

	// FIXME: Not implemented yet
//	private boolean printToPrinter(JFreeChart chart)
//	{
//		PrinterJob printJob = PrinterJob.getPrinterJob();
//		printJob.setPrintable(chart);
//		HashPrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
//		if(!printJob.printDialog(attributes))
//			return false;
//		
//		printJob.print(attributes);
//		return true;
//	}

	private boolean printToDisk(JFreeChart chart) throws IOException
	{
		File destFile = chooseDestinationFile();
		if(destFile == null)
			return false;

		int CHART_WIDTH_IN_PIXELS = 800;
		int CHART_HEIGHT_IN_PIXELS = 600;
		ChartUtilities.saveChartAsJPEG(destFile, chart, CHART_WIDTH_IN_PIXELS, CHART_HEIGHT_IN_PIXELS);
		return true;
	}

	File chooseDestinationFile()
	{
		String title = getLocalization().getWindowTitle("PrintToWhichFile");
		File destination = new File(getLocalization().getFieldLabel("DefaultPrintChartToDiskFileName"));
		
		while(true)
		{
			UiFileChooser.FileDialogResults results = UiFileChooser.displayFileSaveDialog(mainWindow, title, destination);
			if(results.wasCancelChoosen())
				return null;
			
			destination = results.getChosenFile();
			if(!destination.getName().toLowerCase().endsWith(JPEG_EXTENSION))
				destination = new File(destination.getAbsolutePath() + JPEG_EXTENSION);
			if(!destination.exists())
				break;
			if(mainWindow.confirmDlg(mainWindow, "OverWriteExistingFile"))
				break;
		}
		
		return destination;
	}
	
	private JFreeChart createBarChart(HashMap<String, Integer> counts, String selectedFieldLabel) throws Exception
	{
		String chartTitle = getLocalization().getFieldLabel("ChartTitle");
		chartTitle = TokenReplacement.replaceToken(chartTitle, "#SelectedField#", selectedFieldLabel);
		String seriesTitle = getLocalization().getFieldLabel("ChartSeriesTitle");
		String yAxisTitle = getLocalization().getFieldLabel("ChartYAxisTitle");

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		Vector<String> keys = new Vector<String>(counts.keySet());
		Collections.sort(keys, new SaneCollator(getLocalization().getCurrentLanguageCode()));
		for (String value : keys)
		{
			dataset.addValue(counts.get(value), seriesTitle, value);
		}

		boolean showLegend = true;
		boolean showTooltips = true;
		boolean showUrls = false;
		JFreeChart barChart = ChartFactory.createBarChart(
			chartTitle, selectedFieldLabel, yAxisTitle, 
			dataset, PlotOrientation.VERTICAL,
			showLegend, showTooltips, showUrls);
		
		CategoryPlot plot = (CategoryPlot) barChart.getPlot();
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		TickUnitSource units = NumberAxis.createIntegerTickUnits();
		rangeAxis.setStandardTickUnits(units);
		
		return barChart;
	}

	// FIXME: Enable or delete these not-yet-used methods
//	private JFreeChart createDateCountChart(HashMap<String, Integer> counts,
//			String labelText) throws IOException
//	{
//		TimeTableXYDataset dataset = new TimeTableXYDataset(); 
//		for (String value : counts.keySet())
//		{
//			MultiCalendar calendar = MultiCalendar.createFromIsoDateString(value);
//			TimePeriod timePeriod = new Day(calendar.getGregorianDay(), calendar.getGregorianMonth(), calendar.getGregorianYear());
//			dataset.add(timePeriod, counts.get(value), "Number of Martus bulletins by date entered");
//		}
//
//		JFreeChart chart = ChartFactory.createXYBarChart(
//		                     "Martus Bulletin Counts by " + labelText, // Title
//		                      labelText,              // X-Axis label
//		                      true,				// date axis
//		                      "Count",                 // Y-Axis label
//		                      dataset,         // Dataset
//		                      PlotOrientation.VERTICAL,
//		                      true,                     // Show legend
//		                      true,		// tooltips?
//		                      false		// urls
//		                     );
//		return chart;
//	}
//
//	private JFreeChart create3DBarChart(HashMap<String, Integer> counts,
//			String labelText) throws IOException
//	{
//		DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();
//		for (String value : counts.keySet())
//		{
//			categoryDataset.addValue(counts.get(value), value, "");
//		}
//
//		JFreeChart barChart = ChartFactory.createBarChart3D
//		                     ("Bulletin Counts by " + labelText, // Title
//		                      labelText,              // X-Axis label
//		                      "Count",                 // Y-Axis label
//		                      categoryDataset,         // Dataset
//		                      PlotOrientation.VERTICAL,
//		                      true,                     // Show legend
//		                      true,		// tooltips?
//		                      false		// ?????
//		                     );
//		return barChart;
//	}
//
//	private JFreeChart createPieChart(HashMap<String, Integer> counts,
//			String labelText) throws IOException
//	{
//		DefaultPieDataset pieDataset = new DefaultPieDataset();
//		for (String value : counts.keySet())
//		{
//			pieDataset.setValue(value, counts.get(value));
//		}
//
//		JFreeChart pieChart = ChartFactory.createPieChart
//		        ("Bulletin Counts by " + labelText,   // Title
//		         pieDataset,           // Dataset
//		         true,                 // Show legend
//		         true,					// tooltips
//		         new Locale(getLocalization().getCurrentLanguageCode())
//		        );
//		return pieChart;
//	}
	
	private final static String JPEG_EXTENSION = ".jpeg";
}
