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

package org.martus.client.search;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.martus.client.swingui.grids.GridTableModel;
import org.martus.clientside.UiLocalization;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;

public class FancySearchTableModel extends GridTableModel implements TableModelListener
{

	public FancySearchTableModel(GridFieldSpec fieldSpecToUse, UiLocalization localizationToUse)
	{
		super(fieldSpecToUse);
		localization = localizationToUse;
		addTableModelListener(this);
	}
	
	public FieldSpec getFieldSpecForCell(int row, int column)
	{
		if(column != valueColumn)
			return super.getFieldSpecForCell(row, column);
		
		String selectedFieldTag = (String)getValueAt(row, fieldColumn);
		DropDownFieldSpec fieldColumnSpec = (DropDownFieldSpec)getFieldSpecForColumn(fieldColumn);
		ChoiceItem selectedFieldChoiceItem = fieldColumnSpec.getChoice(fieldColumnSpec.findCode(selectedFieldTag));
		FieldSpec selectedFieldSpec = selectedFieldChoiceItem.getSpec();
		if(selectedFieldSpec.getType() == FieldSpec.TYPE_LANGUAGE)
			selectedFieldSpec = new DropDownFieldSpec(localization.getLanguageNameChoices());

		return selectedFieldSpec;
	}

	public void tableChanged(TableModelEvent event)
	{
		if(event.getColumn() == fieldColumn)
		{
			int row = event.getFirstRow();
			String defaultValue = getFieldSpecForCell(row, valueColumn).getDefaultValue();
			setValueAt(defaultValue, row, valueColumn);
//			System.out.println("FancySearchTableModel.tableChanged: Setting value to: " + defaultValue);
			// don't need to fire if we just called setValueAt
			//fireTableRowsUpdated(event.getFirstRow(), event.getLastRow());
		}
	}

	public static int fieldColumn = 1;
	public static int valueColumn = 3;
	
	UiLocalization localization;
}

