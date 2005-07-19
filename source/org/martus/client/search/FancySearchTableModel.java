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
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;

public class FancySearchTableModel extends GridTableModel implements TableModelListener
{

	public FancySearchTableModel(GridFieldSpec fieldSpecToUse)
	{
		super(fieldSpecToUse);
		addTableModelListener(this);
	}

	public int getCellType(int row, int column)
	{
		if(column != valueColumn)
			return super.getCellType(row, column);
		
		String selectedFieldTag = (String)getValueAt(row, fieldColumn);
		DropDownFieldSpec spec = (DropDownFieldSpec)getFieldSpec(fieldColumn);
		ChoiceItem selectedFieldChoiceItem = spec.getChoice(spec.findCode(selectedFieldTag));
		return selectedFieldChoiceItem.getType();
	}
	
	public void tableChanged(TableModelEvent event)
	{
		if(event.getColumn() == fieldColumn)
			this.fireTableRowsUpdated(event.getFirstRow(), event.getLastRow());
	}

	public static int fieldColumn = 1;
	public static int valueColumn = 3;
}

