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

package org.martus.client.core;

import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MockMartusSecurity;
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
	
	public void testFormattedData() throws Exception
	{
		MiniLocalization localization = new MiniLocalization();
		MockMartusSecurity security = MockMartusSecurity.createClient();
		Bulletin b = new Bulletin(security);
		String tagEnglish = MiniLocalization.ENGLISH;
		String tagLanguage = Bulletin.TAGLANGUAGE; 
		b.set(tagLanguage, tagEnglish);
		SafeReadableBulletin srb = new SafeReadableBulletin(b, localization);
		assertEquals(tagEnglish, srb.field(tagLanguage).getData());
		assertEquals(localization.getLanguageName(tagEnglish), srb.get(tagLanguage));
	}
	
	public void testHtmlEscaping() throws Exception
	{
		MiniLocalization localization = new MiniLocalization();
		MockMartusSecurity security = MockMartusSecurity.createClient();
		Bulletin b = new Bulletin(security);
		String tagTitle = Bulletin.TAGTITLE; 
		b.set(tagTitle, "<>&");
		SafeReadableBulletin srb = new SafeReadableBulletin(b, localization);
		assertEquals("Didn't html escape?", "&lt;&gt;&amp;", srb.html(tagTitle));
	}
	

}
