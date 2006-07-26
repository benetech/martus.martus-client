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
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.tree.TreeNode;

import org.martus.clientside.UiLocalization;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.SearchableFieldChoiceItem;

public class FieldChoicesByLabel
{
	public FieldChoicesByLabel()
	{
		allChoices = new Vector();
	}
	
	public void add(ChoiceItem itemToAdd)
	{
		allChoices.add(itemToAdd);
	}
	
	public void addAll(Set itemsToAdd)
	{
		Iterator iter = itemsToAdd.iterator();
		while(iter.hasNext())
		{
			ChoiceItem choice = (ChoiceItem)iter.next();
			add(choice);
		}
	}
	
	public FieldSpec[] asArray(UiLocalization localization)
	{
		Collections.sort(allChoices, new ChoiceItemSorterByMiniFieldSpec());
		FieldSpec[] specs = new FieldSpec[allChoices.size()]; 
		for(int i = 0; i < allChoices.size(); ++i)
		{
			ChoiceItem choice = (ChoiceItem)allChoices.get(i);
			specs[i] = choice.getSpec();
		}
		
		return specs;
	}
	
	public TreeNode asTree(UiLocalization localization)
	{
		SearchFieldTreeNode root = new SearchFieldTreeNode("", localization);

		Collections.sort(allChoices, new ChoiceItemSorterByMiniFieldSpec());
		
		mergeSimilarDropdowns();

		SearchableFieldChoiceItem[] choices = getChoicesAsArray();
		int index = 0;
		while(index < choices.length)
		{
			String label = choices[index].getSpec().getLabel();
			SearchFieldTreeNode node = new SearchFieldTreeNode(label, localization);
			node.add(new SearchFieldTreeNode(choices[index], localization));
			addSimilarNodes(node, choices, index + 1, localization);
			index += node.getChildCount();
			node = pullUpIfOnlyOneChild(node);
			root.add(node);
		}

		return root;
	}
	
	void mergeSimilarDropdowns()
	{
		int mergeInto = 0;
		while(mergeInto + 1 < allChoices.size())
		{
			int mergeFrom = mergeInto + 1;
			SearchableFieldChoiceItem into = ((SearchableFieldChoiceItem)allChoices.get(mergeInto));
			SearchableFieldChoiceItem from = ((SearchableFieldChoiceItem)allChoices.get(mergeFrom));
			if(areDropDownChoicesMergeable(into, from))
			{
				SearchableFieldChoiceItem result = mergeDropDownChoices(into, from);
				allChoices.set(mergeInto, result);
				allChoices.remove(mergeFrom);
			}
			else
			{
				++mergeInto;
			}
		}
	}
	
	public static boolean areDropDownChoicesMergeable(SearchableFieldChoiceItem choice1, SearchableFieldChoiceItem choice2)
	{
		FieldSpec spec1 = choice1.getSpec();
		FieldSpec spec2 = choice2.getSpec();
		if(!spec1.getType().isDropdown())
			return false;
		if(!spec2.getType().isDropdown())
			return false;
		if(!spec1.getTag().equals(spec2.getTag()))
			return false;
		if(!spec1.getLabel().equals(spec2.getLabel()))
			return false;
		return true;
	}
	
	public static SearchableFieldChoiceItem mergeDropDownChoices(SearchableFieldChoiceItem mergeInto, SearchableFieldChoiceItem mergeFrom)
	{
		if(!areDropDownChoicesMergeable(mergeInto, mergeFrom))
			throw new RuntimeException("Attempted to merge unmergeable fieldspecs");
		
		DropDownFieldSpec specInto = (DropDownFieldSpec)mergeInto.getSpec();
		DropDownFieldSpec specFrom = (DropDownFieldSpec)mergeFrom.getSpec();
		
		Vector choices = new Vector(Arrays.asList(specInto.getAllChoices()));
		ChoiceItem[] moreChoices = specFrom.getAllChoices();
		for(int i = 0; i < moreChoices.length; ++i)
		{
			if(!choices.contains(moreChoices[i]))
				choices.add(moreChoices[i]);
		}
		
		DropDownFieldSpec resultSpec = new DropDownFieldSpec((ChoiceItem[])choices.toArray(new ChoiceItem[0]));
		resultSpec.setTag(mergeInto.getSpec().getSubFieldTag());
		resultSpec.setLabel(mergeInto.getSpec().getLabel());
		resultSpec.setParent(mergeInto.getSpec().getParent());
		return new SearchableFieldChoiceItem(resultSpec);
	}

	private SearchableFieldChoiceItem[] getChoicesAsArray()
	{
		return (SearchableFieldChoiceItem[])allChoices.toArray(new SearchableFieldChoiceItem[0]);
	}
	
	private SearchFieldTreeNode pullUpIfOnlyOneChild(SearchFieldTreeNode node)
	{
		if(node.getChildCount() == 1)
			node = (SearchFieldTreeNode)node.getChildAt(0);
		return node;
	}
	
	private void addSimilarNodes(SearchFieldTreeNode parent, SearchableFieldChoiceItem[] choices, int startAt, MiniLocalization localization)
	{
		String label = parent.toString();
		int index = startAt;
		while(index < choices.length && choices[index].getSpec().getLabel().equals(label))
		{
			SearchableFieldChoiceItem choice = choices[index];
			parent.add(new SearchFieldTreeNode(choice, localization));
			++index;
		}
		
	}
	
	Vector allChoices;
}