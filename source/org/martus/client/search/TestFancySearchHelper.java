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

import java.io.File;

import org.martus.common.EnglishCommonStrings;
import org.martus.common.GridData;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.clientside.Localization;
import org.martus.common.field.MartusDateRangeField;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.util.TestCaseEnhanced;

public class TestFancySearchHelper extends TestCaseEnhanced
{
	public TestFancySearchHelper(String name)
	{
		super(name);
	}
	
	public void setUp() throws Exception
	{
		tempDir = createTempDirectory();
		localization = new Localization(tempDir);
		helper = new FancySearchHelper(localization);
	}
	
	public void tearDown()
	{
		tempDir.delete();
	}
	
	public void testCreateFieldColumnSpec()
	{
		String languageCode = "en";
		localization.addEnglishTranslations(EnglishCommonStrings.strings);
		localization.setCurrentLanguageCode(languageCode);
		
		DropDownFieldSpec spec = helper.createFieldColumnSpec();
		assertEquals("no empty first entry?", "", spec.getChoice(0).getCode());
		assertTrue("no author?", spec.findCode(BulletinConstants.TAGAUTHOR) >= 0);
		assertTrue("no private?", spec.findCode(BulletinConstants.TAGPRIVATEINFO) >= 0);
		assertTrue("no eventdate.begin?", spec.findCode(BulletinConstants.TAGEVENTDATE + "." + MartusDateRangeField.SUBFIELD_BEGIN) >= 0);
		assertTrue("no eventdate.end?", spec.findCode(BulletinConstants.TAGEVENTDATE + "." + MartusDateRangeField.SUBFIELD_END) >= 0);
		assertFalse("has raw eventdate?", spec.findCode(BulletinConstants.TAGEVENTDATE) >= 0);
	}
	
	public void testCreateOpColumnSpec()
	{
		DropDownFieldSpec spec = helper.createOpColumnSpec();
		assertTrue("no contains?", spec.findCode(":") >= 0);
		assertTrue("no >?", spec.findCode(":>") >= 0);
		assertTrue("no <?", spec.findCode(":<") >= 0);
		assertTrue("no >=?", spec.findCode(":>=") >= 0);
		assertTrue("no <=?", spec.findCode(":<=") >= 0);
	}
	
	public void testCreateGridSpec()
	{
		GridFieldSpec spec = helper.getGridSpec();
		assertEquals(3, spec.getColumnCount());
		assertEquals("no field column?", FieldSpec.TYPE_DROPDOWN, spec.getColumnType(0));
		assertEquals("no op column?", FieldSpec.TYPE_DROPDOWN, spec.getColumnType(1));
		assertEquals("no value column?", FieldSpec.TYPE_MORPHIC, spec.getColumnType(2));
	}
	
	public void testGetSearchString() throws Exception
	{
		GridFieldSpec spec = new GridFieldSpec();
		spec.addColumn(FieldSpec.createStandardField("field", FieldSpec.TYPE_NORMAL));
		spec.addColumn(FieldSpec.createStandardField("op", FieldSpec.TYPE_NORMAL));
		spec.addColumn(FieldSpec.createStandardField("value", FieldSpec.TYPE_MORPHIC));
		GridData data = new GridData(spec);
		addRow(data, "", ":", "whiz");
		addRow(data, "a", "b", "c");
		addRow(data, "d", "e", " f");
		addRow(data, "g", "h", "\"ii\"");
		
		assertEquals("\"whiz\" ab\"c\" de\"f\" gh\"ii\" ", helper.getSearchString(data));
	}
	
	private void addRow(GridData data, String field, String op, String value)
	{
		int row = data.getRowCount();
		data.addEmptyRow();
		data.setValueAt(field, row, 0);
		data.setValueAt(op, row, 1);
		data.setValueAt(value, row, 2);
	}
	
	File tempDir;
	Localization localization;
	FancySearchHelper helper;
}
