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
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.martus.client.core.SortableBulletinList;
import org.martus.client.search.SearchTreeNode;
import org.martus.client.search.SortFieldChooserSpecBuilder;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiPopUpTreeEditor;
import org.martus.clientside.UiLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.fieldspec.PopUpTreeFieldSpec;
import org.martus.common.packet.UniversalId;
import org.martus.swing.UiButton;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;


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
			SearchTreeNode searchTree = mainWindow.askUserForSearchCriteria();
			if(searchTree == null)
				return;
			
			SortFieldsDialog dlg = new SortFieldsDialog(mainWindow);
			dlg.setVisible(true);
			if(!dlg.ok())
				return;
			
			String[] sortTags = dlg.getSortTags();

			SortableBulletinList bulletinIdsFromSearch = mainWindow.doSearch(searchTree, sortTags);
			if(bulletinIdsFromSearch == null)
				return;
			int bulletinsMatched = bulletinIdsFromSearch.size();
			if(bulletinIdsFromSearch.size() == 0)
			{
				mainWindow.notifyDlg("SearchFailed");
				return;
			}
			mainWindow.showNumberOfBulletinsFound(bulletinsMatched, "ReportFound");
			UniversalId[] bulletinIds = bulletinIdsFromSearch.getSortedUniversalIds();
			Vector bulletinsToReportOn = mainWindow.getBulletins(bulletinIds);
			printBulletins(bulletinsToReportOn);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			mainWindow.notifyDlgBeep("UnexpectedError");
		}
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

}
