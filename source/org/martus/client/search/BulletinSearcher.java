/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2006, Beneficent
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

import org.martus.client.core.SafeReadableBulletin;
import org.martus.common.MiniLocalization;
import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.FieldSpec;

public class BulletinSearcher
{
	public BulletinSearcher(SearchTreeNode nodeToMatch)
	{
		node = nodeToMatch;
	}

	public boolean doesMatch(SafeReadableBulletin b, MiniLocalization localization)
	{
		if(node.getOperation() == SearchTreeNode.VALUE)
			return doesValueMatch(b, localization);

		BulletinSearcher left = new BulletinSearcher(node.getLeft());
		BulletinSearcher right = new BulletinSearcher(node.getRight());

		if(node.getOperation() == SearchTreeNode.AND)
			return left.doesMatch(b, localization) && right.doesMatch(b, localization);

		if(node.getOperation() == SearchTreeNode.OR)
			return left.doesMatch(b, localization) || right.doesMatch(b, localization);

		return false;
	}

	private boolean doesValueMatch(SafeReadableBulletin b, MiniLocalization localization)
	{
		String searchForValue = node.getValue();

		FieldSpec fieldToSearch = node.getField();
		if(fieldToSearch.getTag().length() == 0)
			return b.contains(searchForValue);

		int compareOp = node.getComparisonOperator();
		MartusField field = b.getPossiblyNestedField(fieldToSearch);
		if(field == null)
			return false;
		
		return field.doesMatch(compareOp, searchForValue, localization);
	}
	
	SearchTreeNode node;
}
