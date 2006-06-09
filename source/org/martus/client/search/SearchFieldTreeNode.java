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

import java.util.Collections;

import javax.swing.tree.DefaultMutableTreeNode;

import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.SearchableFieldChoiceItem;

public class SearchFieldTreeNode extends DefaultMutableTreeNode
{
	public SearchFieldTreeNode(String label)
	{
		super(label);
	}
	
	public SearchFieldTreeNode(SearchableFieldChoiceItem choiceItem)
	{
		super(choiceItem);
	}
	
	public boolean isSelectable()
	{
		return (getChildCount() == 0);
	}
	
	public void sortChildren(String languageCode)
	{
		if(children == null)
			return;
		
		Collections.sort(children, new SearchFieldTreeNodeComparator(languageCode));
	}
	
	public String toString()
	{
		if(getParent() == null)
			return "";
		
		if(getParent().getParent() == null)
			return getUserObject().toString();
		
		SearchableFieldChoiceItem choice = (SearchableFieldChoiceItem)getUserObject();
		return FieldSpec.getTypeString(choice.getType()) + ": " + choice.getSpec().getTag();
			
	}
	
	public String getSortValue()
	{
		if(getChildCount() > 0)
			return (String)getUserObject();

		SearchableFieldChoiceItem choice = (SearchableFieldChoiceItem)getUserObject();
		return choice.getSpec().getLabel();
	}
	
	static class SearchFieldTreeNodeComparator extends SaneCollator
	{
		public SearchFieldTreeNodeComparator(String languageCode)
		{
			super(languageCode);
		}
		
		public int compare(Object o1, Object o2)
		{
			SearchFieldTreeNode node1 = (SearchFieldTreeNode)o1;
			SearchFieldTreeNode node2 = (SearchFieldTreeNode)o2;
			
			return super.compare(node1.getSortValue(), node2.getSortValue());
		}
		
	}
}
