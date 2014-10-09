/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
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
package org.martus.client.core;

import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ObservableList;

import org.junit.Test;
import org.martus.client.swingui.jfx.landing.bulletins.GridRowData;
import org.martus.common.FieldSpecCollection;
import org.martus.common.GridData;
import org.martus.common.GridRow;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.FieldTypeSectionStart;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.util.TestCaseEnhanced;

public class TestFxBulletinField extends TestCaseEnhanced
{
	public TestFxBulletinField(String name)
	{
		super(name);
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		
		localization = new MiniLocalization();
	}
	
	@Test
	public void testBasics()
	{
		final String SAMPLE = "test";
		FieldSpec fieldSpec = FieldSpec.createCustomField("tag", "Label", new FieldTypeNormal());
		FxBulletinField field = new FxBulletinField(fieldSpec, localization);
		assertEquals("tag", field.getTag());
		assertEquals("Label", field.getLabel());
		assertFalse(field.isGrid());
		assertFalse(field.isSectionStart());
		assertEquals("", field.valueProperty().getValue());
		try
		{
			field.gridDataProperty();
			fail("Should have thrown for getting grid data from non-grid field");
		}
		catch(Exception ignoreExpected)
		{
		}

		field.valueProperty().setValue(SAMPLE);
		field.clear();
		assertNull(field.valueProperty().getValue());
	}
	
	public void testAddListener()
	{
		final String SAMPLE = "test";
		FieldSpec fieldSpec = FieldSpec.createCustomField("tag", "Label", new FieldTypeNormal());
		FxBulletinField field = new FxBulletinField(fieldSpec, localization);
		assertEquals("", field.valueProperty().getValue());
		field.addValueListener((observable, oldValue, newValue) -> 
		{
			assertEquals("", oldValue);
			assertEquals(SAMPLE, newValue);
		}); 
		field.valueProperty().setValue(SAMPLE);
	}
	
	public void testValidation()
	{
		FieldSpec spec = FieldSpec.createCustomField("tag", "Label", new FieldTypeNormal());
		spec.setRequired();
		FxBulletinField field = new FxBulletinField(spec, localization);
		ObservableBooleanValue fieldIsValidProperty = field.fieldIsValidProperty();
		assertFalse(fieldIsValidProperty.getValue());
	}
	
	public void testGrid() throws Exception
	{
		String gridTag = "grid";
		GridFieldSpec gridSpec2Colunns = new GridFieldSpec();
		gridSpec2Colunns.setTag(gridTag);
		gridSpec2Colunns.setLabel("Grid");
		gridSpec2Colunns.addColumn(FieldSpec.createCustomField("a", "A", new FieldTypeNormal()));
		gridSpec2Colunns.addColumn(FieldSpec.createCustomField("b", "B", new FieldTypeDate()));

		FxBulletinField gridField = new FxBulletinField(gridSpec2Colunns, localization);
		try
		{
			gridField.valueProperty();
			fail("valueProperty should have thrown for grid");
		}
		catch(Exception ignoreExpected)
		{
		}
		try
		{
			gridField.fieldIsValidProperty();
			fail("fieldIsValidProperty should have thrown for grid");
		}
		catch(Exception ignoreExpected)
		{
		}
		
		FieldSpecCollection noReusableLists = new FieldSpecCollection();
		GridData data = createSampleGridData(gridSpec2Colunns, noReusableLists);
		gridField.setGridData(data.getXmlRepresentation(), noReusableLists.getAllReusableChoiceLists());

		ObservableList<GridRowData> gridData = gridField.gridDataProperty();
		assertEquals(1, gridData.size());
		GridRowData gridRowData = gridData.get(0);
		assertEquals(2, gridRowData.size());
		assertEquals("Apple", gridRowData.get("A"));
		assertEquals("Balloon", gridRowData.get("B"));
		
	}
	
	public void testSection() throws Exception
	{
		FieldSpec spec = FieldSpec.createCustomField("tag", "Label", new FieldTypeSectionStart());
		FxBulletinField field = new FxBulletinField(spec, localization);
		assertTrue(field.isSectionStart());
	}

	private GridData createSampleGridData(GridFieldSpec gridSpec2Colunns, FieldSpecCollection fsc)
	{
		GridData gridData = new GridData(gridSpec2Colunns, fsc.getAllReusableChoiceLists());
		GridRow gridRowSample = new GridRow(gridSpec2Colunns, fsc.getAllReusableChoiceLists());
		gridRowSample.setCellText(0, "Apple");
		gridRowSample.setCellText(1, "Balloon");
		gridData.addRow(gridRowSample);
		return gridData;
	}
	
	private MiniLocalization localization;

}
