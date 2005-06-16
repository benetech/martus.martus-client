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

import java.io.IOException;
import java.io.NotSerializableException;

import org.martus.client.swingui.UiMainWindow;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiParagraphPanel;
import org.martus.swing.UiTextField;
import org.martus.swing.UiWrappedTextArea;

public class UiSimpleSearchDlg extends UiSearchDlg
{
	public UiSimpleSearchDlg(UiMainWindow owner)
	{
		super(owner);
	}

	UiButton createBody(UiBasicLocalization localization)
	{
		setTitle(localization.getWindowTitle("search"));
		
		UiButton search = new UiButton(localization.getButtonLabel("search"));
		search.addActionListener(this);

		UiButton cancel = new UiButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(this);
		
		searchField = new UiTextField(40);
		searchField.setText(getPreviousSearch());

		UiParagraphPanel panel = new UiParagraphPanel();
		panel.addOnNewLine(new UiWrappedTextArea(localization.getFieldLabel("SearchBulletinRules")));
		panel.addComponents(new UiLabel(localization.getFieldLabel("SearchEntry")), searchField);
		panel.addBlankLine();
		panel.addComponents(search, cancel);
	
		getContentPane().add(panel);
		getRootPane().setDefaultButton(search);
		
		return search;
	}
	
	public String getSearchString()
	{
		return searchField.getText(); 		
	}

	void memorizeSearch()
	{
		previousSearch = getSearchString();
	}
	
	String getPreviousSearch()
	{
		return previousSearch;
	}

	// This class is NOT intended to be serialized!!!
	private static final long serialVersionUID = 1;
	private void writeObject(java.io.ObjectOutputStream stream) throws IOException
	{
		throw new NotSerializableException();
	}
	
	protected UiTextField searchField;
	private static String previousSearch = "";
}
