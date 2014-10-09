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

import org.junit.Test;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeNormal;
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
		assertEquals("", field.valueProperty().getValue());
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
		FieldValidator validator = new FieldValidator(spec, localization);
		field.setValidator(validator);
		ObservableBooleanValue fieldIsValidProperty = field.fieldIsValidProperty();
		assertFalse(fieldIsValidProperty.getValue());
	}

	private MiniLocalization localization;

}
