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

package org.martus.client.test;

import org.martus.client.search.SearchTreeNode;
import org.martus.util.*;


public class TestSearchTreeNode extends TestCaseEnhanced
{
    public TestSearchTreeNode(String name)
	{
        super(name);
    }

    public void testValueNode()
    {
		SearchTreeNode node = new SearchTreeNode("text");
		assertEquals("text", node.getValue());
		assertEquals(SearchTreeNode.VALUE, node.getOperation());

		node = new SearchTreeNode(" stripped ");
		assertEquals("stripped", node.getValue());
    }

    public void testOpNode()
    {
		verifyOpNodeCreation(SearchTreeNode.OR, "or");
		verifyOpNodeCreation(SearchTreeNode.AND, "and");
	}

	private void verifyOpNodeCreation(int op, String opName)
	{
		SearchTreeNode left = new SearchTreeNode("left");
		SearchTreeNode right = new SearchTreeNode("right");
		SearchTreeNode node = new SearchTreeNode(op, left, right);
		assertEquals(op, node.getOperation());
		assertNull(opName + " didn't clear value?", node.getValue());

		assertNotNull("Left", node.getLeft());
		assertNotNull("Right", node.getRight());

		assertEquals("Left", "left", node.getLeft().getValue());
		assertEquals("Right", "right", node.getRight().getValue());
	}

}
