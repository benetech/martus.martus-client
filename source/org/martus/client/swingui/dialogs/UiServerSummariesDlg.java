/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2004, Beneficent
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.UiScrollPane;
import org.martus.client.swingui.tablemodels.RetrieveTableModel;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.common.clientside.UiLanguageDirection;
import org.martus.common.packet.FieldDataPacket;
import org.martus.swing.ParagraphLayout;
import org.martus.swing.UiTable;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;

public abstract class UiServerSummariesDlg extends JDialog
{
	public UiServerSummariesDlg(UiMainWindow owner, RetrieveTableModel tableModel, String windowTitleTag)
	{
		super(owner, owner.getLocalization().getWindowTitle(windowTitleTag), true);
		mainWindow = owner;
		model = tableModel;
	}
	
	abstract public void initialize();

	void initialize(String topMessageTag, String okButtonTag)
	{
		UiBasicLocalization localization = mainWindow.getLocalization();

		disabledBackgroundColor = getBackground();

		String topMessageText = localization.getFieldLabel(topMessageTag);
		UiWrappedTextArea retrieveMessage = new UiWrappedTextArea(topMessageText, UiLanguageDirection.getComponentOrientation());
		tableBox = Box.createVerticalBox();
		table = new RetrieveJTable(model);
		oldBooleanRenderer = table.getDefaultRenderer(Boolean.class);
		oldIntegerRenderer = table.getDefaultRenderer(Integer.class);
		table.setDefaultRenderer(Boolean.class, new BooleanRenderer());
		table.setDefaultRenderer(Integer.class, new IntegerRenderer());
		table.setDefaultRenderer(String.class, new StringRenderer());

		table.createDefaultColumnsFromModel();
		tableBox.add(table.getTableHeader());
		tableBox.add(new UiScrollPane(table, UiLanguageDirection.getComponentOrientation()));

		JPanel topPanel = new JPanel();
		topPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		topPanel.setLayout(new BorderLayout());
		topPanel.add(retrieveMessage, BorderLayout.NORTH);
		topPanel.add(tableBox, BorderLayout.CENTER);
		topPanel.add(createActionsPanel(localization, okButtonTag), BorderLayout.SOUTH);

		getContentPane().add(topPanel);	
		setScreenSize();				
		Utilities.centerDlg(this);
		show();
	}


	private void setScreenSize()
	{
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		double width = dim.getWidth()- (dim.getWidth()* 0.25);
		dim.setSize(width, getSize().getHeight());
		setSize(dim);
	}

	private JPanel createActionsPanel(UiBasicLocalization localization, String okButtonTag)
	{
		JButton ok = new JButton(localization.getButtonLabel(okButtonTag));
		ok.addActionListener(new OkHandler());
		JButton cancel = new JButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(new CancelHandler());
		JButton preview = new JButton(localization.getButtonLabel("Preview"));
		preview.addActionListener(new PreviewHandler());

		JButton checkAll = new JButton(localization.getButtonLabel("checkall"));
		checkAll.addActionListener(new CheckAllHandler());
		JButton unCheckAll = new JButton(localization.getButtonLabel("uncheckall"));
		unCheckAll.addActionListener(new UnCheckAllHandler());

		JPanel southPanel = new JPanel();
		southPanel.setLayout(new ParagraphLayout());		
		southPanel.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		southPanel.add(createSummariesPanel(localization));
		southPanel.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		southPanel.add(checkAll);
		southPanel.add(unCheckAll);
		southPanel.add(preview);
		southPanel.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		southPanel.add(ok);
		southPanel.add(cancel);

		getRootPane().setDefaultButton(ok);
		return southPanel;
	}

	private JPanel createSummariesPanel(UiBasicLocalization localization)
	{
		JRadioButton downloadableSummaries = new JRadioButton(localization.getButtonLabel("DownloadableSummaries"), true);
		downloadableSummaries.addActionListener(new ChangeDownloadableSummariesHandler());
		JRadioButton allSummaries = new JRadioButton(localization.getButtonLabel("AllSummaries"), false);
		allSummaries.addActionListener(new ChangeAllSummariesHandler());
		ButtonGroup summariesGroup = new ButtonGroup();
		summariesGroup.add(downloadableSummaries);
		summariesGroup.add(allSummaries);
		JPanel radioPanel = new JPanel();		
		radioPanel.setLayout(new GridLayout(0, 1));
		radioPanel.add(downloadableSummaries);
		radioPanel.add(allSummaries);
		return radioPanel;
	}

	public boolean getResult()
	{
		return result;
	}

	public Vector getUniversalIdList()
	{
		return model.getUniversalIdList();
	}
	
	abstract String getNoneSelectedTag();
	
	boolean confirmIntentionsBeforeClosing()
	{
		Vector uidList = getUniversalIdList();
		if( uidList.size() == 0)
		{
			mainWindow.notifyDlg(getNoneSelectedTag());
			return false;
		}
		
		return true;
	}
	
	class RetrieveJTable extends JTable
	{
		public RetrieveJTable(TableModel model)
		{
			super(model);
		}

		public void doLayout()
		{
			UiTable.setColumnWidthToHeaderWidth(this,0);
			int numberOfColumns = getColumnModel().getColumnCount();
			UiTable.setColumnWidthToHeaderWidth(this,numberOfColumns-1);
			super.doLayout();
		}
	}

	class BooleanRenderer extends DefaultTableCellRenderer
	{
		public Component getTableCellRendererComponent(
				JTable tableToUse, Object value,
				boolean isSelected, boolean hasFocus,
				int row, int column)
		{
			Component cell = oldBooleanRenderer.getTableCellRendererComponent(tableToUse, value, isSelected, hasFocus, row, column);
			if(enabledBackgroundColor == null)
				enabledBackgroundColor = cell.getBackground();
			if(model.isDownloadable(row))
			{
				cell.setEnabled(true);
				if(!isSelected)
					cell.setBackground(enabledBackgroundColor);
			}
			else
			{
				cell.setEnabled(false);
				if(!isSelected)
					cell.setBackground(disabledBackgroundColor);
			}
			return cell;
		}
		Color enabledBackgroundColor;
	}

	class IntegerRenderer extends DefaultTableCellRenderer
	{
		public Component getTableCellRendererComponent(
				JTable tableToUse, Object value,
				boolean isSelected, boolean hasFocus,
				int row, int column)
		{
			Component cell = oldIntegerRenderer.getTableCellRendererComponent(tableToUse, value, isSelected, hasFocus, row, column);
			if(enabledBackgroundColor == null)
				enabledBackgroundColor = cell.getBackground();
			if(model.isDownloadable(row))
			{
				cell.setEnabled(true);
				if(!isSelected)
					cell.setBackground(enabledBackgroundColor);
			}
			else
			{
				cell.setEnabled(false);
				if(!isSelected)
					cell.setBackground(disabledBackgroundColor);
			}
			return cell;
		}
		Color enabledBackgroundColor;
	}

	class StringRenderer extends DefaultTableCellRenderer
	{
		public Component getTableCellRendererComponent(
				JTable tableToUse, Object value,
				boolean isSelected, boolean hasFocus,
				int row, int column)
		{
			if(normalBackgroundColor == null)
			{
				Component cell = super.getTableCellRendererComponent(tableToUse, value, isSelected, hasFocus, row, column);
				normalBackgroundColor = cell.getBackground();
			}

			if(!model.isDownloadable(row))
				setBackground(disabledBackgroundColor);
			else
				setBackground(normalBackgroundColor);
			return super.getTableCellRendererComponent(tableToUse, value, isSelected, hasFocus, row, column);
		}
		Color normalBackgroundColor;
	}


	class OkHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			if(!confirmIntentionsBeforeClosing())
				return;
			
			result = true;
			dispose();
		}
	}

	class CancelHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			dispose();
		}
	}

	class PreviewHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{

			int[] row = table.getSelectedRows();
			if(row.length <= 0)
			{
				mainWindow.notifyDlg("PreviewNoBulletinsSelected");
			}
			else if(row.length==1)
			{
				FieldDataPacket fdp = model.getBulletinSummary(row[0]).getFieldDataPacket();
				new UiBulletinPreviewDlg(mainWindow, fdp);
			}
			else
			{
				mainWindow.notifyDlg("PreviewOneBulletinOnly");
			}
		}
	}

	class CheckAllHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			model.setAllFlags(true);
		}
	}

	class UnCheckAllHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			model.setAllFlags(false);
		}
	}

	class ChangeDownloadableSummariesHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			model.changeToDownloadableSummaries();
			model.fireTableStructureChanged();
		}
	}

	class ChangeAllSummariesHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			model.changeToAllSummaries();
			model.fireTableStructureChanged();
		}
	}

	private Box tableBox;
	UiMainWindow mainWindow;
	boolean result;
	RetrieveJTable table;
	RetrieveTableModel model;
	TableCellRenderer oldBooleanRenderer;
	TableCellRenderer oldIntegerRenderer;
	Color disabledBackgroundColor;
}
