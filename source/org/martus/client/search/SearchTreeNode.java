/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2004, Beneficent
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

public class SearchTreeNode
{
	public final static int VALUE = 0;
	public final static int OR = 1;
	public final static int AND = 2;

	public SearchTreeNode(String value)
	{
		nodeOp = VALUE;
		int colonAt = value.indexOf(':');
		if(colonAt >= 0)
		{
			fieldTag = value.substring(0, colonAt);
			nodeValue = value.substring(colonAt + 1);
		}
		else
		{
			nodeValue = value;
		}
	}
	
	public SearchTreeNode(int op, SearchTreeNode left, SearchTreeNode right)
	{
		nodeOp = op;
		nodeLeft = left;
		nodeRight = right;
	}
	
	public String getField()
	{
		return fieldTag;
	}

	public String getValue()
	{
		return nodeValue;
	}

	public int getOperation()
	{
		return nodeOp;
	}

	public SearchTreeNode getLeft()
	{
		return nodeLeft;
	}

	public SearchTreeNode getRight()
	{
		return nodeRight;
	}

	private String nodeValue;
	private String fieldTag;
	private int nodeOp;
	private SearchTreeNode nodeLeft;
	private SearchTreeNode nodeRight;
}
