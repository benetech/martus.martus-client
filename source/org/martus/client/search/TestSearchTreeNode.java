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

		node = new SearchTreeNode(" nostripping ");
		assertEquals(" nostripping ", node.getValue());
    }
    
    public void testQuotedValues()
    {
    	String phrase = "search for this";
    	
    	SearchTreeNode withoutField = new SearchTreeNode("\"" + phrase + "\"");
    	assertEquals("without field parsed wrong?", phrase, withoutField.getValue());
    	
    	SearchTreeNode withField = new SearchTreeNode("field:\"" + phrase + "\"");
    	assertEquals("with field parsed wrong?", phrase, withField.getValue());
    	
    	String thisThat = "this:that";
    	SearchTreeNode quotedColon = new SearchTreeNode("\"" + thisThat + "\"");
    	assertEquals("split quoted string?", thisThat, quotedColon.getValue());
    }
    
    public void testComparisons()
    {
    	String field = "field";
    	String basicValue = "stuff";

    	assertEquals("thought it was a comparison op?", "", SearchTreeNode.getComparisonOp("one"));
    	SearchTreeNode noOp = new SearchTreeNode(field + ":" + basicValue);
    	assertEquals("wrong default op?", SearchTreeNode.CONTAINS, noOp.getComparisonOperator());
    	
    	SearchTreeNode opWithoutField = new SearchTreeNode(">" + basicValue);
    	assertEquals("allowed compareop without field?", SearchTreeNode.CONTAINS, opWithoutField.getComparisonOperator());

    	String[] comparisonOps = {"=", ">", ">=", "<", "<="};
    	int[] comparisonOpValues = {
    		SearchTreeNode.EQUAL,
    		SearchTreeNode.GREATER, 
    		SearchTreeNode.GREATER_EQUAL,
    		SearchTreeNode.LESS, 
    		SearchTreeNode.LESS_EQUAL,
    	};
    	for(int i=0; i < comparisonOps.length; ++i)
    	{
    		SearchTreeNode node = new SearchTreeNode(field + ":" + comparisonOps[i] + basicValue);
    		assertEquals("wrong compare op value?", comparisonOpValues[i], node.getComparisonOperator());
    		assertEquals("didn't strip op?", basicValue, node.getValue());
    	}
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
