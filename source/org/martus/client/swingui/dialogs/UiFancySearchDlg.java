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

import javax.swing.Box;

import org.martus.client.search.FancySearchHelper;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiGridEditor;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.swing.UiButton;
import org.martus.swing.UiWrappedTextArea;

public class UiFancySearchDlg extends UiSearchDlg
{
	public UiFancySearchDlg(UiMainWindow owner)
	{
		super(owner);
	}
	
	UiButton createBody(UiBasicLocalization localization)
	{
		helper = new FancySearchHelper(localization);
		setTitle(localization.getWindowTitle("search"));
		
		UiButton search = new UiButton(localization.getButtonLabel("search"));
		search.addActionListener(this);

		UiButton cancel = new UiButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(this);

		grid = new UiGridEditor(helper.getSearchTableModel());
		grid.setText(getPreviousSearch());

		Box panel = Box.createVerticalBox();
		panel.add(new UiWrappedTextArea(localization.getFieldLabel("SearchBulletinRules")));
		panel.add(grid.getComponent());
		Box buttonBox = Box.createHorizontalBox();
		buttonBox.add(search);
		buttonBox.add(cancel);
		panel.add(buttonBox);
	
		getContentPane().add(panel);
		getRootPane().setDefaultButton(search);
		
		return search;
	}

	public String getSearchString()
	{
		return helper.getSearchString(grid.getGridData());
	}
	
	void memorizeSearch()
	{
		previousSearch = grid.getText();
	}
	
	String getPreviousSearch()
	{
		return previousSearch;
	}

	UiGridEditor grid;
	FancySearchHelper helper;
	private static String previousSearch = "";
}
