/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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

import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeDate;
import org.martus.common.fieldspec.FieldTypeMultiline;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.util.TestCaseEnhanced;

public class TestSafeReadableBulletin extends TestCaseEnhanced
{
	public TestSafeReadableBulletin(String name)
	{
		super(name);
	}

	public void testParseNestedTags() throws Exception
	{
		String tags = "abc.def.gh";
		String[] result = SafeReadableBulletin.parseNestedTags(tags);
		assertEquals(3, result.length);
		assertEquals("abc", result[0]);
		assertEquals("def", result[1]);
		assertEquals("gh", result[2]);
	}

	public void testRemovePrivateFieldData() throws Exception
	{
		MiniLocalization localization = new MiniLocalization();
		MockMartusSecurity security = MockMartusSecurity.createClient();
		Bulletin b = new Bulletin(security);
		b.setAllPrivate(false);
		b.set(Bulletin.TAGPUBLICINFO, "public");
		b.set(Bulletin.TAGPRIVATEINFO, "private");
		SafeReadableBulletin srb = new SafeReadableBulletin(b, localization);
		srb.removePrivateData();
		assertEquals("removed public?", b.get(Bulletin.TAGPUBLICINFO), srb.field(Bulletin.TAGPUBLICINFO).getData());
		assertEquals("didn't remove private?", "", srb.field(Bulletin.TAGPRIVATEINFO).getData());
		
		b.setAllPrivate(true);
		SafeReadableBulletin srbAllPrivate = new SafeReadableBulletin(b, localization);
		srbAllPrivate.removePrivateData();
		assertEquals("didn't remove public?", "", srbAllPrivate.field(Bulletin.TAGPUBLICINFO).getData());
	}
	
	public void testMissingField() throws Exception
	{
		MiniLocalization localization = new MiniLocalization();
		MockMartusSecurity security = MockMartusSecurity.createClient();
		Bulletin b = new Bulletin(security);
		SafeReadableBulletin srb = new SafeReadableBulletin(b, localization);
		MartusField noSuchField = srb.field("whoop-de-doo!");
		assertNotNull("Returned null for missing field?", noSuchField);
		MartusField noSuchSubField = noSuchField.getSubField("whatever", localization);
		assertNotNull("Returned null for missing subfield?", noSuchSubField);
	}
	
	public void testFieldWrongLabel() throws Exception
	{
		String tag = "tag";
		String label = "Label";
		String sampleData = "sample data";
		String sampleAuthor = "Mark Twain";
		
		MiniLocalization localization = new MiniLocalization();
		MockMartusSecurity security = MockMartusSecurity.createClient();
		FieldSpec[] customBottomSpecs = {
			FieldSpec.createCustomField("tag", "Label", new FieldTypeNormal()),	
		};
		Bulletin b = new Bulletin(security, StandardFieldSpecs.getDefaultTopSectionFieldSpecs(), customBottomSpecs);
		b.set(tag, sampleData);
		b.set(Bulletin.TAGAUTHOR, sampleAuthor);
		
		SafeReadableBulletin srb = new SafeReadableBulletin(b, localization);
		MartusField wrongLabel = srb.field(tag, "Wrong Label", new FieldTypeNormal().getTypeName());
		assertEquals("Didn't check label?", "", wrongLabel.getData());
		MartusField similarType = srb.field(tag, label, new FieldTypeMultiline().getTypeName());
		assertEquals("Too picky about types?", sampleData, similarType.getData());
		MartusField differentType = srb.field(tag, label, new FieldTypeDate().getTypeName());
		assertEquals("Didn't check type?", "", differentType.getData());
		
		MartusField standardWithWrongLabel = srb.field(Bulletin.TAGAUTHOR, "not author", new FieldTypeNormal().getTypeName());
		assertEquals("Too picky about standard field label?", sampleAuthor, standardWithWrongLabel.getData());
		
		MartusField pseudoFieldWithWrongLabel = srb.field(Bulletin.PSEUDOFIELD_LAST_SAVED_DATE, "Last Saved", new FieldTypeDate().getTypeName());
		assertEquals("Too picky about pseudofield label?", b.getLastSavedDate(), pseudoFieldWithWrongLabel.getData());
	}
}
