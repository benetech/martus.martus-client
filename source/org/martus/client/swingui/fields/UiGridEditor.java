/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2007, Beneficent
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
package org.martus.client.swingui.fields;

import java.util.Map;
import java.util.Vector;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.common.fieldspec.DataInvalidException;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;

public class UiGridEditor extends UiEditableGrid 
{
	public UiGridEditor(UiMainWindow mainWindow, GridFieldSpec fieldSpec, UiDialogLauncher dlgLauncher, Map gridFields, int maxGridCharacters)
	{
		super(mainWindow, fieldSpec, dlgLauncher, gridFields, maxGridCharacters);
	}

	protected Vector createButtons()
	{
		Vector buttons = super.createButtons();
		buttons.insertElementAt(createShowExpandedButton(), 0);
		return buttons;
	}

	public void validate(FieldSpec spec, String fullLabel) throws DataInvalidException
	{
		super.validate(spec, fullLabel);
		
		GridFieldSpec gridSpec = (GridFieldSpec)spec;
		for(int row = 0; row < getGridData().getRowCount(); ++row)
		{
			for(int col = 0; col < gridSpec.getColumnCount(); ++col)
			{
				FieldSpec columnSpec = gridSpec.getFieldSpec(col);
				String value = getGridData().getValueAt(row, col);
				String fullColumnLabel = gridSpec.getLabel() + ": " + columnSpec.getLabel();
				try
				{
					columnSpec.validate(fullColumnLabel, value, getLocalization());
				}
				catch(DataInvalidException e)
				{
					table.getSelectionModel().setSelectionInterval(row, row);
					table.getColumnModel().getSelectionModel().setSelectionInterval(col, col);
					table.requestFocusInWindow();
					throw e;
				}
			}
		}
	}
}
