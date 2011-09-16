/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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

package org.martus.client.search;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.json.JSONObject;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.client.swingui.dialogs.UiProgressWithCancelDlg;
import org.martus.client.swingui.fields.UiEditableGrid;
import org.martus.client.swingui.fields.UiFieldContext;
import org.martus.client.swingui.fields.UiPopUpTreeEditor;
import org.martus.client.swingui.grids.GridPopUpTreeCellEditor;
import org.martus.client.swingui.grids.GridTable;
import org.martus.client.swingui.grids.SearchGridTable;
import org.martus.common.FieldSpecCollection;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.swing.UiButton;
import org.martus.swing.UiTextArea;
import org.martus.swing.Utilities;

public class FancySearchGridEditor extends UiEditableGrid
{
	public static FancySearchGridEditor create(UiMainWindow mainWindowToUse, UiDialogLauncher dlgLauncher)
	{
		ClientBulletinStore store = mainWindowToUse.getStore();
		FancySearchHelper helper = new FancySearchHelper(store, dlgLauncher);
		UiFieldContext contextToUse = new UiFieldContext();
		FieldSpecCollection allSpecs = new FieldSpecCollection();
		allSpecs.addAllSpecs(store.getAllKnownFieldSpecs());
		allSpecs.addAllReusableChoicesLists(store.getAllReusableChoiceLists());
		contextToUse.setSectionFieldSpecs(allSpecs);
		FancySearchGridEditor gridEditor = new FancySearchGridEditor(mainWindowToUse, helper, contextToUse);
		gridEditor.initalize();
		return gridEditor;
	}
	
	private FancySearchGridEditor(UiMainWindow mainWindowToUse, FancySearchHelper helperToUse, UiFieldContext contextToUse)
	{
		super(mainWindowToUse, contextToUse, helperToUse.getModel(), helperToUse.getDialogLauncher(), NUMBER_OF_COLUMNS_FOR_GRID);
		mainWindow = mainWindowToUse;
		helper = helperToUse;
		getTable().setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		getTable().addRowSelectionListener(new SelectionChangeHandler());
		setSearchForColumnWideEnoughForDates();
		setGridTableSize();
		addListenerSoFieldChangeCanTriggerRepaintOfValueColumn();
		updateLoadValuesButtonStatus();
	}
	
	class SelectionChangeHandler implements ListSelectionListener
	{
		public void valueChanged(ListSelectionEvent e)
		{
			updateLoadValuesButtonStatus();
		}

	}

	protected void updateLoadValuesButtonStatus()
	{
		boolean canLoadValues = false;
		int row = getTable().getSelectedRow();
		if(row >= 0 && row < getTable().getRowCount())
		{
			FieldSpec spec = helper.getModel().getSelectedFieldSpec(row);
			canLoadValues = helper.getModel().canUseMemorizedPossibleValues(spec);
			if(helper.getModel().hasMemorizedPossibleValues(spec))
				canLoadValues = false;
		}
		loadValuesButton.setEnabled(canLoadValues);
	}

	protected GridTable createGridTable(UiDialogLauncher dlgLauncher, UiFieldContext context)
	{
		return new SearchGridTable(model, dlgLauncher, context);
	}
	
	protected Vector createButtons()
	{
		Vector buttons = super.createButtons();
		loadValuesButton = new UiButton(getLocalization().getButtonLabel("LoadFieldValuesFromAllBulletins"));
		loadValuesButton.addActionListener(new LoadValuesListener(table.getDialogLauncher()));
		buttons.add(loadValuesButton);
		return buttons;
	}
	
	class LoadValuesListener implements ActionListener
	{
		LoadValuesListener(UiDialogLauncher dlgLauncherToUse)
		{
			dlgLauncher = dlgLauncherToUse;
		}

		public void actionPerformed(ActionEvent e)
		{
			if(!isRowSelected())
			{
				dlgLauncher.ShowNotifyDialog("NoGridRowSelected");
				return;
			}

			int row = getTable().getSelectedRow();
			FieldSpec spec = helper.getModel().getSelectedFieldSpec(row);
			if(!helper.getModel().canUseMemorizedPossibleValues(spec))
			{
				dlgLauncher.ShowNotifyDialog("NonStringFieldRowSelected");
				return;
			}
			UiProgressWithCancelDlg progressDlg = new LoadValuesProgressDlg(mainWindow);
			LoadValuesThread thread = new LoadValuesThread(progressDlg, spec);
			thread.start();
			progressDlg.setVisible(true);
			// NOTE: by the time we get here, the thread has terminated
			if(thread.errorOccured)
				throw new RuntimeException(thread.exception);
			HashSet loadedValues = thread.getLoadedValues();
			helper.getModel().setAvailableFieldValues(spec, loadedValues);
			helper.getModel().fireTableDataChanged();
			updateLoadValuesButtonStatus();
		}		
		UiDialogLauncher dlgLauncher;
	}
	
	class LoadValuesProgressDlg extends UiProgressWithCancelDlg
	{
		public LoadValuesProgressDlg(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse, "LoadingFieldValuesFromAllBulletins");

			getContentPane().setLayout(new BorderLayout());
			UiTextArea explanation = new UiTextArea(4, 50);
			explanation.setEditable(false);
			explanation.setText(getLocalization().getFieldLabel("LoadingFieldValuesFromAllBulletinsExplanation"));
			
			JPanel cancelPanel = new JPanel();
			cancelPanel.add(cancel);
			
			JPanel meterPanel = new JPanel();
			meterPanel.add(progressMeter);
			
			getContentPane().add(explanation, BorderLayout.NORTH);
			getContentPane().add(meterPanel, BorderLayout.CENTER);
			getContentPane().add(cancelPanel, BorderLayout.SOUTH);
			Utilities.centerDlg(this);
		}
		
	}
	
	class LoadValuesThread extends Thread
	{
		public LoadValuesThread(UiProgressWithCancelDlg progressRetrieveDlgToUse, FieldSpec specToUse)
		{
			progressMeter = progressRetrieveDlgToUse;
			spec = specToUse;
		}

		public void run()
		{
			try
			{
				FieldValuesLoader loader = new FieldValuesLoader(getStore(), getLocalization());
				loadedValues = loader.loadFieldValuesFromAllBulletinRevisions(progressMeter, spec);
			}
			catch (Exception e)
			{
				errorOccured = true;
				exception = e;
			}
			finally
			{
				progressMeter.finished();
			}
		}
		
		public HashSet getLoadedValues()
		{
			return loadedValues;
		}

		private ClientBulletinStore getStore()
		{
			return mainWindow.getApp().getStore();
		}

		private UiProgressWithCancelDlg progressMeter;
		boolean errorOccured;
		Exception exception;
		private FieldSpec spec;
		private HashSet loadedValues;
	}

	
	private void setGridTableSize()
	{
		Dimension searchGridSize = Utilities.getViewableScreenSize();
		searchGridSize.setSize(searchGridSize.getWidth() * 0.9, 200);
		getComponent().setPreferredSize(searchGridSize);
	}

	private void setSearchForColumnWideEnoughForDates()
	{
		GridTable searchTable = getTable();
		int searchForColumn = FancySearchHelper.COLUMN_VALUE;
		int widthToHoldDates = searchTable.getDateColumnWidth(searchForColumn);
		searchTable.setColumnWidth(searchForColumn, widthToHoldDates);
	}

	private GridPopUpTreeCellEditor getFieldColumnEditor()
	{
		int column = FancySearchTableModel.fieldColumn;
		return (GridPopUpTreeCellEditor)getTable().getCellEditor(0, column);
	}
	
	public void setFromJson(JSONObject json)
	{
		helper.setSearchFromJson(getGridData(), json);
	}
	
	public JSONObject getSearchAsJson() throws Exception
	{
		return helper.getSearchAsJson(getGridData());
	}
	
	public SearchTreeNode getSearchTree()
	{
		return helper.getSearchTree(getGridData());		
	}

	private void addListenerSoFieldChangeCanTriggerRepaintOfValueColumn()
	{
		UiPopUpTreeEditor fieldChoiceEditor = getFieldColumnEditor().getPopUpTreeEditor();
		fieldChoiceEditor.addActionListener(new PopUpActionHandler());
	}

	class PopUpActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			stopCellEditing();
			updateLoadValuesButtonStatus();
		}
	}

	private static final int NUMBER_OF_COLUMNS_FOR_GRID = 80;

	UiMainWindow mainWindow;
	FancySearchHelper helper;
	UiButton loadValuesButton;
}
