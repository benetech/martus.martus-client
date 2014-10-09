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

import java.util.Vector;

import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ObservableList;

import org.junit.Test;
import org.martus.client.swingui.jfx.generic.data.ObservableChoiceItemList;
import org.martus.client.swingui.jfx.landing.bulletins.GridRowData;
import org.martus.common.FieldSpecCollection;
import org.martus.common.GridData;
import org.martus.common.GridRow;
import org.martus.common.MiniLocalization;
import org.martus.common.ReusableChoices;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.CustomDropDownFieldSpec;
import org.martus.common.fieldspec.DropDownFieldSpec;
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
		security = MockMartusSecurity.createClient();
		fsc = new FieldSpecCollection();

		statesChoices = new ReusableChoices(STATES_CHOICES_TAG, "States");
		statesChoices.add(new ChoiceItem("WA", "Washington"));
		statesChoices.add(new ChoiceItem("OR", "Oregon"));
		fsc.addReusableChoiceList(statesChoices);

		citiesChoices = new ReusableChoices(CITIES_CHOICES_TAG, "Cities");
		citiesChoices.add(new ChoiceItem("SEA", "Seattle"));
		citiesChoices.add(new ChoiceItem("PDX", "Portland"));
		fsc.addReusableChoiceList(citiesChoices);
	}
	
	@Test
	public void testBasics() throws Exception
	{
		final String SAMPLE = "test";
		FieldSpec fieldSpec = FieldSpec.createCustomField("tag", "Label", new FieldTypeNormal());
		FxBulletinField field = new FxBulletinField(createFxBulletin(), fieldSpec, localization);
		assertEquals("tag", field.getTag());
		assertEquals("Label", field.getLabel());
		assertFalse(field.isRequiredField());
		assertFalse(field.isGrid());
		assertFalse(field.isSectionStart());
		assertFalse(field.isDropdown());
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
	
	public void testAddListener() throws Exception
	{
		final String SAMPLE = "test";
		FieldSpec fieldSpec = FieldSpec.createCustomField("tag", "Label", new FieldTypeNormal());
		FxBulletinField field = new FxBulletinField(createFxBulletin(), fieldSpec, localization);
		assertEquals("", field.valueProperty().getValue());
		field.addValueListener((observable, oldValue, newValue) -> 
		{
			assertEquals("", oldValue);
			assertEquals(SAMPLE, newValue);
		}); 
		field.valueProperty().setValue(SAMPLE);
	}
	
	public void testValidation() throws Exception
	{
		FieldSpec spec = FieldSpec.createCustomField("tag", "Label", new FieldTypeNormal());
		spec.setRequired();
		FxBulletinField field = new FxBulletinField(createFxBulletin(), spec, localization);
		assertTrue(field.isRequiredField());
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

		FxBulletin fxb = createFxBulletin();
		FxBulletinField gridField = new FxBulletinField(fxb, gridSpec2Colunns, localization);
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
		
		GridData data = createSampleGridData(gridSpec2Colunns);
		gridField.setGridData(data.getXmlRepresentation());

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
		FxBulletinField field = new FxBulletinField(createFxBulletin(), spec, localization);
		assertTrue(field.isSectionStart());
	}
	
	public void testSimpleDropdown() throws Exception
	{
		String simpleDropDownTag = "simple";
		ChoiceItem[] simpleChoices = new ChoiceItem[] {new ChoiceItem("a", "A"), new ChoiceItem("b", "B")};
		FieldSpec simpleDropDown = new DropDownFieldSpec(simpleChoices);
		simpleDropDown.setTag(simpleDropDownTag);
		
		FxBulletinField field = new FxBulletinField(createFxBulletin(), simpleDropDown, localization);
		assertTrue(field.isDropdown());

		Vector<ObservableChoiceItemList> simpleLists = field.getChoiceItemLists();
		assertEquals(1, simpleLists.size());
		ObservableChoiceItemList simpleList = simpleLists.get(0);
		assertEquals(simpleChoices.length, simpleList.size());
		assertEquals(simpleChoices[0], simpleList.get(0));
	}
	
	public void testReusableDropdown() throws Exception
	{
		String reusableDropDownTag = "reusable";
		CustomDropDownFieldSpec reusableDropDown = new CustomDropDownFieldSpec();
		reusableDropDown.setTag(reusableDropDownTag);
		reusableDropDown.addReusableChoicesCode(CITIES_CHOICES_TAG);

		fsc.add(reusableDropDown);
		FxBulletin fxb = createFxBulletin();
		
		FxBulletinField field = new FxBulletinField(fxb, reusableDropDown, localization);
		assertTrue(field.isDropdown());

		Vector<ObservableChoiceItemList> reusableLists = field.getChoiceItemLists();
		assertEquals(1, reusableLists.size());
		ObservableChoiceItemList reusableList = reusableLists.get(0);
		assertEquals(citiesChoices.size()+1, reusableList.size());
		assertEquals("", reusableList.get(0).getCode());
		assertEquals(citiesChoices.get(0), reusableList.get(1));
	}
	
	public void testNestedDropdowns() throws Exception
	{
		String nestedDropDownTag = "nested";
		CustomDropDownFieldSpec nestedDropDown = new CustomDropDownFieldSpec();
		nestedDropDown.setTag(nestedDropDownTag);
		nestedDropDown.addReusableChoicesCode(STATES_CHOICES_TAG);
		nestedDropDown.addReusableChoicesCode(CITIES_CHOICES_TAG);

		fsc.add(nestedDropDown);
		FxBulletin fxb = createFxBulletin();
		
		Vector<ObservableChoiceItemList> nestedLists = fxb.getChoiceItemLists(nestedDropDownTag);
		assertEquals(2, nestedLists.size());
		ObservableChoiceItemList nestedStatesList = nestedLists.get(0);
		assertEquals(statesChoices.size()+1, nestedStatesList.size());
		assertEquals("", nestedStatesList.get(0).getCode());
		assertEquals(statesChoices.get(0), nestedStatesList.get(1));
		ObservableChoiceItemList nestedCitiesList = nestedLists.get(1);
		assertEquals(citiesChoices.size()+1, nestedCitiesList.size());
		assertEquals("", nestedCitiesList.get(0).getCode());
		assertEquals(citiesChoices.get(0), nestedCitiesList.get(1));
		
	}

	public FxBulletin createFxBulletin() throws Exception
	{
		Bulletin b = new Bulletin(security, fsc, new FieldSpecCollection());
		FxBulletin fxb = new FxBulletin(localization);
		fxb.copyDataFromBulletin(b, null);
		return fxb;
	}

	private GridData createSampleGridData(GridFieldSpec gridSpec2Columns)
	{
		GridData gridData = new GridData(gridSpec2Columns, fsc.getAllReusableChoiceLists());
		GridRow gridRowSample = new GridRow(gridSpec2Columns, fsc.getAllReusableChoiceLists());
		gridRowSample.setCellText(0, "Apple");
		gridRowSample.setCellText(1, "Balloon");
		gridData.addRow(gridRowSample);
		return gridData;
	}
	
	private static final String STATES_CHOICES_TAG = "states";
	private static final String CITIES_CHOICES_TAG = "cities";

	private MiniLocalization localization;
	private MartusSecurity security;
	private FieldSpecCollection fsc;
	private ReusableChoices statesChoices;
	private ReusableChoices citiesChoices;
}
