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

package org.martus.client.swingui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.martus.client.search.FancySearchGridEditor;
import org.martus.client.search.SearchTreeNode;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.swing.UiButton;
import org.martus.swing.UiWrappedTextPanel;
import org.martus.swing.Utilities;

public class UiFancySearchDlg extends UiSearchDlg
{
	public UiFancySearchDlg(UiMainWindow owner)
	{
		super(owner);
	}
	
	UiButton createBody(UiMainWindow mainWindow)
	{
		MartusLocalization localization = mainWindow.getLocalization();
		setTitle(localization.getWindowTitle("search"));
		
		UiButton search = new UiButton(localization.getButtonLabel("search"));
		search.addActionListener(this);

		UiButton cancel = new UiButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(this);
		UiDialogLauncher dlgLauncher = new UiDialogLauncher(mainWindow.getCurrentActiveFrame(), localization);
		grid = FancySearchGridEditor.create(mainWindow.getStore(), dlgLauncher);
		grid.setText(getPreviousSearch());

		JPanel instructionPanel = new JPanel();
		instructionPanel.setLayout(new BorderLayout());
		instructionPanel.add(new UiWrappedTextPanel(localization.getFieldLabel("SearchBulletinRules")), BorderLayout.NORTH);
		instructionPanel.add(new UiWrappedTextPanel(localization.getFieldLabel("SearchBulletinAddingRules")), BorderLayout.SOUTH);

		Box buttonBox = Box.createHorizontalBox();
		buttonBox.setBorder(new EmptyBorder(10,0,0,0));
		Utilities.addComponentsRespectingOrientation(buttonBox, new Component[] {search, Box.createHorizontalGlue(),cancel });

		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new EmptyBorder(5,5,5,5));
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(instructionPanel,BorderLayout.NORTH);
		mainPanel.add(grid.getComponent(),BorderLayout.CENTER);
		mainPanel.add(buttonBox,BorderLayout.SOUTH);

		getContentPane().add(mainPanel);
		getRootPane().setDefaultButton(search);
		
		return search;
	}

	public SearchTreeNode getSearchTree()
	{
		return grid.getSearchTree();
	}
	
	void memorizeSearch()
	{
		previousSearch = grid.getText();
	}
	
	String getPreviousSearch()
	{
		return previousSearch;
	}

	FancySearchGridEditor grid;
	private static String previousSearch = "";
}
