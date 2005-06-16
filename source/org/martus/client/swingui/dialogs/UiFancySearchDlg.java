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
import java.util.Vector;

import javax.swing.Box;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiGridEditor;
import org.martus.common.clientside.ChoiceItem;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.fieldspec.GridFieldSpec.UnsupportedFieldTypeException;
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
		setTitle(localization.getWindowTitle("search"));
		
		UiButton search = new UiButton(localization.getButtonLabel("search"));
		search.addActionListener(this);

		UiButton cancel = new UiButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(this);

		GridFieldSpec spec = new GridFieldSpec();

		try
		{
			spec.addColumn(createFieldColumnSpec());
			spec.addColumn(createOpColumnSpec());
			spec.addColumn(FieldSpec.createCustomField("value", "Value", FieldSpec.TYPE_NORMAL));
		}
		catch (UnsupportedFieldTypeException e)
		{
			// TODO: better error handling?
			e.printStackTrace();
			throw new RuntimeException();
		}

		grid = new UiGridEditor(spec);
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

	private DropDownFieldSpec createFieldColumnSpec()
	{
		Vector allAvailableFields = new Vector();
		allAvailableFields.add(new ChoiceItem("", getLocalization().getFieldLabel("anyfield")));
		allAvailableFields.addAll(convertToChoiceItems(StandardFieldSpecs.getDefaultPublicFieldSpecs()));
		allAvailableFields.addAll(convertToChoiceItems(StandardFieldSpecs.getDefaultPrivateFieldSpecs()));

		ChoiceItem[] fieldChoices = (ChoiceItem[])allAvailableFields.toArray(new ChoiceItem[0]);
		                                  
		DropDownFieldSpec fieldColumnSpec = new DropDownFieldSpec();
		fieldColumnSpec.setLabel("Field--------------------");
		fieldColumnSpec.setChoices(fieldChoices);
		return fieldColumnSpec;
	}
	
	private Vector convertToChoiceItems(FieldSpec[] specs)
	{
		Vector choices = new Vector();
		for(int i=0; i < specs.length; ++i)
		{
			String tag = specs[i].getTag();
			String displayString = tag;
			if(StandardFieldSpecs.isStandardFieldTag(tag))
				displayString = getLocalization().getFieldLabel(tag);
			choices.add(new ChoiceItem(tag, displayString));
		}
			
		return choices;
	}
	
	private DropDownFieldSpec createOpColumnSpec()
	{
		ChoiceItem[] opChoices = 
		{
			new ChoiceItem(":", "contains"),
			new ChoiceItem(":>", ">"),
			new ChoiceItem(":>=", ">="),
			new ChoiceItem(":<", "<"),
			new ChoiceItem(":<=", "<="),
		};
		                                  
		DropDownFieldSpec opSpec = new DropDownFieldSpec();
		opSpec.setLabel("Comparison");
		opSpec.setChoices(opChoices);
		return opSpec;
	}
	
	public String getSearchString()
	{
		return grid.getText(); 
	}
	
	void memorizeSearch()
	{
		previousSearch = getSearchString();
		System.out.println(previousSearch);
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
	
	UiGridEditor grid;
	private static String previousSearch = "";
}
