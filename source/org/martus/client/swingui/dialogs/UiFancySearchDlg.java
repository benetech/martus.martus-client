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

import javax.swing.Box;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiGridEditor;
import org.martus.common.clientside.ChoiceItem;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.GridFieldSpec.UnsupportedFieldTypeException;
import org.martus.swing.UiButton;
import org.martus.swing.UiTextField;
import org.martus.swing.UiWrappedTextArea;

public class UiFancySearchDlg extends UiSearchDlg
{
	public UiFancySearchDlg(UiMainWindow owner)
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

		GridFieldSpec spec = new GridFieldSpec();

		ChoiceItem[] opChoices = 
		{
			new ChoiceItem(":", "contains"),
			new ChoiceItem(":>", ">"),
			new ChoiceItem(":>=", ">="),
			new ChoiceItem(":<", "<"),
			new ChoiceItem(":<=", "<="),
		};
		                                  
		DropDownFieldSpec opSpec = new DropDownFieldSpec();
		opSpec.setChoices(opChoices);
		try
		{
			spec.addColumn(FieldSpec.createCustomField("field", "Field", FieldSpec.TYPE_NORMAL));
			spec.addColumn(opSpec);
			spec.addColumn(FieldSpec.createCustomField("value", "Value", FieldSpec.TYPE_NORMAL));
		}
		catch (UnsupportedFieldTypeException e)
		{
			// TODO: better error handling?
			e.printStackTrace();
			throw new RuntimeException();
		}

		UiGridEditor grid = new UiGridEditor(spec);
		grid.setText("");

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
	
	void memorizeSearch()
	{
		// TODO: implement this
	}
	
	public String getSearchString()
	{
		return searchField.getText(); 		
	}

	// This class is NOT intended to be serialized!!!
	private static final long serialVersionUID = 1;
	private void writeObject(java.io.ObjectOutputStream stream) throws IOException
	{
		throw new NotSerializableException();
	}
	
	protected UiTextField searchField;

}
