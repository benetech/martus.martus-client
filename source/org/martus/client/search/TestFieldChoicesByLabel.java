/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006, Beneficent
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

import java.util.Arrays;

import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.SearchableFieldChoiceItem;
import org.martus.util.TestCaseEnhanced;

public class TestFieldChoicesByLabel extends TestCaseEnhanced
{
	public TestFieldChoicesByLabel(String name)
	{
		super(name);
	}

	public void testMergeSimilarDropdowns() throws Exception
	{
		ChoiceItem[] aChoices = new ChoiceItem[] {
			new ChoiceItem("a", "A"),
			new ChoiceItem("b", "B"),
		};
		DropDownFieldSpec a = new DropDownFieldSpec(aChoices);
		SearchableFieldChoiceItem choiceA = new SearchableFieldChoiceItem(a);
		
		ChoiceItem[] bChoices = new ChoiceItem[] {
			new ChoiceItem("b", "B"),
			new ChoiceItem("c", "C"),
		};
		DropDownFieldSpec b = new DropDownFieldSpec(bChoices);
		SearchableFieldChoiceItem choiceB = new SearchableFieldChoiceItem(b);
		
		FieldSpec nonDropDown = FieldSpec.createCustomField("tag", "Label", new FieldTypeNormal());
		SearchableFieldChoiceItem choiceNonDropDown = new SearchableFieldChoiceItem(nonDropDown);

		a.setTag("tag");
		a.setLabel("Label");
		assertFalse("into non-dropdown mergeable?", FieldChoicesByLabel.areDropDownChoicesMergeable(choiceNonDropDown, choiceA));
		assertFalse("from non-dropdown mergeable?", FieldChoicesByLabel.areDropDownChoicesMergeable(choiceA, choiceNonDropDown));
		
		a.setTag("1");
		b.setTag("2");
		a.setLabel("Label");
		b.setLabel("Label");
		assertFalse("different tags mergeable?", FieldChoicesByLabel.areDropDownChoicesMergeable(choiceA, choiceB));
		try
		{
			FieldChoicesByLabel.mergeDropDownChoices(choiceA, choiceB);
			fail("Should have thrown for merging different tags");
		}
		catch(RuntimeException ignoreExpected)
		{
		}

		a.setTag("tag");
		b.setTag("tag");
		a.setLabel("1");
		b.setLabel("2");
		assertFalse("different labels mergeable?", FieldChoicesByLabel.areDropDownChoicesMergeable(choiceA, choiceB));
		try
		{
			FieldChoicesByLabel.mergeDropDownChoices(choiceA, choiceB);
			fail("Should have thrown for merging different labels");
		}
		catch(RuntimeException ignoreExpected)
		{
		}

		a.setTag("tag");
		b.setTag("tag");
		a.setLabel("1");
		b.setLabel("2");
		a.setParent(b);
		b.setParent(null);
		assertFalse("different parents mergeable?", FieldChoicesByLabel.areDropDownChoicesMergeable(choiceA, choiceB));
		try
		{
			FieldChoicesByLabel.mergeDropDownChoices(choiceA, choiceB);
			fail("Should have thrown for merging different parents");
		}
		catch(RuntimeException ignoreExpected)
		{
		}

		a.setTag("tag");
		b.setTag("tag");
		GridFieldSpec parent = new GridFieldSpec();
		parent.setTag("grid");
		a.setParent(parent);
		b.setParent(parent);
		a.setLabel("Label");
		b.setLabel("Label");
		assertTrue("not mergeable?", FieldChoicesByLabel.areDropDownChoicesMergeable(choiceA, choiceB));
		SearchableFieldChoiceItem merged = FieldChoicesByLabel.mergeDropDownChoices(choiceA, choiceB);
		assertEquals("wrong search tag?", choiceA.getSearchTag(), merged.getSearchTag());
		assertEquals("wrong display?", a.getLabel(), merged.toString());
		assertEquals("wrong parent?", a.getParent(), merged.getSpec().getParent());
		DropDownFieldSpec mergedSpec = (DropDownFieldSpec)merged.getSpec();
		assertEquals("wrong choices?", 3, mergedSpec.getCount());
		for(int i = 0; i < a.getCount(); ++i)
			assertContains("Missing a[" + i + "]?", a.getChoice(i), Arrays.asList(mergedSpec.getAllChoices()));
		for(int i = 0; i < b.getCount(); ++i)
			assertContains("Missing b[" + i + "]?", b.getChoice(i), Arrays.asList(mergedSpec.getAllChoices()));
		assertEquals("wrong order?", b.getChoice(1), mergedSpec.getChoice(2));
	}
}
