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
package org.martus.client.test;

import java.util.Vector;

import org.martus.client.swingui.fields.GridTableModel;
import org.martus.common.fieldspec.CustomDropDownFieldSpec;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.util.TestCaseEnhanced;


public class TestGridTableModel extends TestCaseEnhanced
{
	public TestGridTableModel(String name)
	{
		super(name);
	}
	
	public void testBasics() throws Exception
	{
		GridFieldSpec gridSpec = new GridFieldSpec();
		String label1 = "column 1";
		FieldSpec column1 = new FieldSpec(label1, FieldSpec.TYPE_NORMAL);

		String label2 = "column 2";
		CustomDropDownFieldSpec column2 = new CustomDropDownFieldSpec();
		Vector choices = new Vector();
		String choice1 = "choice 1";
		String choice2 = "choice 2";
		choices.add(choice1);
		choices.add(choice2);
		
		column2.setChoices(choices);
		column2.setLabel(label2);
		gridSpec.addColumn(column1);
		gridSpec.addColumn(column2);
		GridTableModel model = new GridTableModel(gridSpec);
		int columnsIncludingRowCount = 3;
		assertEquals(columnsIncludingRowCount, model.getColumnCount());
		assertEquals(" ", model.getColumnName(0));
		assertEquals(FieldSpec.TYPE_NORMAL, model.getColumnType(0));
		assertEquals(label1, model.getColumnName(1));
		assertEquals(FieldSpec.TYPE_NORMAL, model.getColumnType(1));
		assertEquals(label2, model.getColumnName(2));
		assertEquals(FieldSpec.TYPE_DROPDOWN, model.getColumnType(2));

		DropDownFieldSpec dropDownFieldSpec = ((DropDownFieldSpec)(model.getFieldSpec(2)));
		assertEquals(CustomDropDownFieldSpec.EMPTY_FIRST_CHOICE, dropDownFieldSpec.getValue(0));
		assertEquals(choice1, dropDownFieldSpec.getValue(1));
		assertEquals(choice2, dropDownFieldSpec.getValue(2));
		
		assertEquals(0, model.getRowCount());
		model.addEmptyRow();
		assertEquals(1, model.getRowCount());
		String value = "Yeah";
		model.setValueAt(value, 0,1);
		assertEquals(value, model.getValueAt(0,1));
		int rowOne = 1;
		assertEquals(Integer.toString(rowOne), model.getValueAt(0,0));
		model.addEmptyRow();
		int rowTwo = 2;
		assertEquals(Integer.toString(rowTwo), model.getValueAt(1,0));

		GridFieldSpec spec2 = new GridFieldSpec();
		String ColumnZeroHeader = "column 0";
		spec2.setColumnZeroLabel(ColumnZeroHeader);
		GridTableModel model2 = new GridTableModel(spec2);
		assertEquals(ColumnZeroHeader, model2.getColumnName(0));
	}

}
