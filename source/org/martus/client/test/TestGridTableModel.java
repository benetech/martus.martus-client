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

import org.martus.client.swingui.fields.GridTableModel;
import org.martus.common.GridFieldSpec;
import org.martus.util.TestCaseEnhanced;


public class TestGridTableModel extends TestCaseEnhanced
{
	public TestGridTableModel(String name)
	{
		super(name);
	}
	
	public void testBasics() throws Exception
	{
		GridFieldSpec spec = new GridFieldSpec();
		String label1 = "column 1";
		String label2 = "column 2";
		spec.addColumn(label1);
		spec.addColumn(label2);
		GridTableModel model = new GridTableModel(spec);
		assertEquals(2, model.getColumnCount());
		assertEquals(label1, model.getColumnName(0));
		assertEquals(label2, model.getColumnName(1));
		assertEquals(0, model.getRowCount());
		model.addEmptyRow();
		assertEquals(1, model.getRowCount());
		String value = "Yeah";
		model.setValueAt(value, 0,0);
		assertEquals(value, model.getValueAt(0,0));
	}
	
	
}
