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
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.tree.TreeNode;

import org.martus.clientside.UiLocalization;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.SearchableFieldChoiceItem;

class FieldChoicesByLabel
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
	
	public TreeNode asTree(UiLocalization localization)
	{
		SearchFieldTreeNode root = new SearchFieldTreeNode("");

		SearchableFieldChoiceItem[] choices = getChoicesAsArray();
		Arrays.sort(choices, new LabelTagTypeSorter());
		int index = 0;
		while(index < choices.length)
		{
			String label = choices[index].getSpec().getLabel();
			SearchFieldTreeNode node = new SearchFieldTreeNode(label);
			node.add(new SearchFieldTreeNode(choices[index]));
			addSimilarNodes(node, choices, index + 1);
			index += node.getChildCount();
			node = pullUpIfOnlyOneChild(node);
			root.add(node);
		}

		return root;
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
	
	private void addSimilarNodes(SearchFieldTreeNode parent, SearchableFieldChoiceItem[] choices, int startAt)
	{
		String label = parent.toString();
		int index = startAt;
		while(index < choices.length && choices[index].getSpec().getLabel().equals(label))
		{
			SearchableFieldChoiceItem choice = choices[index];
			parent.add(new SearchFieldTreeNode(choice));
			++index;
		}
		
	}
	
	static class LabelTagTypeSorter implements Comparator
	{
		public int compare(Object o1, Object o2)
		{
			ChoiceItem choice1 = (ChoiceItem)o1;
			ChoiceItem choice2 = (ChoiceItem)o2;
			String value1 = choice1.getSpec().getLabel() + choice1.getSpec().getTag() + choice1.getType();
			String value2 = choice2.getSpec().getLabel() + choice2.getSpec().getTag() + choice2.getType();
			return (value1.compareTo(value2));
		}
		
	}
	
	Vector allChoices;
}