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

import org.martus.client.search.SearchParser;
import org.martus.client.search.SearchTreeNode;
import org.martus.client.search.TokenList;
import org.martus.util.TestCaseEnhanced;

public class TestSearchParser extends TestCaseEnhanced
{
    public TestSearchParser(String name)
	{
        super(name);
    }
    
    public void setUp()
    {
		englishParser = SearchParser.createEnglishParser();
    }
    
    public void testTokenizeEmpty()
    {
    	assertEquals("not empty?", 0, englishParser.tokenize("").size());
    }
    
    public void testTokenizeTrailingSpaces()
    {
    	String[]  words = {"red", "green", "blue", };
    	String toTokenize = "";
    	for(int i=0; i < words.length; ++i)
    		toTokenize += words[i] + "   ";
    	verifyTokenized(words, englishParser.tokenize(toTokenize));
    }

    public void testTokenizeLeadingSpaces()
    {
    	String[]  words = {"red", "green", "blue", };
    	String toTokenize = "";
    	for(int i=0; i < words.length; ++i)
    		toTokenize += "   " + words[i];
    	verifyTokenized(words, englishParser.tokenize(toTokenize));
    }
    
    public void testTokenizedQuotedString()
    {
    	String[] tokens = {"this", "\"here or there\"", "that", };
    	String toTokenize = "";
    	for(int i=0; i < tokens.length; ++i)
    		toTokenize += tokens[i] + " ";
    	verifyTokenized(tokens, englishParser.tokenize(toTokenize));
    	
    }
    
    public void testTokenizedSpecificFieldSimple()
    {
    	String fieldValue = "field:value";
    	TokenList tokens = englishParser.tokenize(fieldValue);
    	assertEquals(1, tokens.size());
    	assertEquals(fieldValue, tokens.get(0));
    	
    }

    public void testTokenizedSpecificFieldQuoted()
    {
    	String fieldQuotedValue = "field:\"quoted value\"";
    	TokenList tokens = englishParser.tokenize(fieldQuotedValue);
    	assertEquals(1, tokens.size());
    	assertEquals(fieldQuotedValue, tokens.get(0));
    	
    }

	private void verifyTokenized(String[] words, TokenList result)
	{
		assertEquals(3, result.size());
    	for(int i=0; i < words.length; ++i)
    		assertEquals(words[i], result.get(i));
	}
    

    public void testParseEmpty()
    {
    	SearchTreeNode rootNode = englishParser.parse("");
    	assertEquals("not empty?", "", rootNode.getValue());
    }
    
    public void testSimpleSearch()
	{
		SearchTreeNode rootNode = englishParser.parse("blah");
		assertNotNull("Null root", rootNode);
		assertEquals(SearchTreeNode.VALUE, rootNode.getOperation());
	}
	
	public void testLowerCase()
	{
		SearchTreeNode rootNode = englishParser.parse("this OR that");
		assertNotNull("Null root", rootNode);
		assertEquals(SearchTreeNode.OR, rootNode.getOperation());
	}

	public void testSimpleOr()
	{
		SearchTreeNode rootNode = englishParser.parse("this or that");
		assertNotNull("Null root", rootNode);
		assertEquals(SearchTreeNode.OR, rootNode.getOperation());

		SearchTreeNode left = rootNode.getLeft();
		assertNotNull("Null left", left);
		assertEquals("this", left.getValue());

		SearchTreeNode right = rootNode.getRight();
		assertNotNull("Null right", right);
		assertEquals("that", right.getValue());
	}

	public void testSimpleAnd()
	{
		SearchTreeNode rootNode = englishParser.parse(" tweedledee  and  tweedledum ");
		assertNotNull("Null root", rootNode);
		assertEquals(SearchTreeNode.AND, rootNode.getOperation());

		SearchTreeNode left = rootNode.getLeft();
		assertNotNull("Null left", left);
		assertEquals("tweedledee", left.getValue());

		SearchTreeNode right = rootNode.getRight();
		assertNotNull("Null right", right);
		assertEquals("tweedledum", right.getValue());
	}

	public void testComplex()
	{
		// (a AND b) AND c
		SearchTreeNode abc = englishParser.parse("a and b and c");
		assertNotNull("Null root", abc);
		assertEquals("rootNode", SearchTreeNode.AND, abc.getOperation());
		
		SearchTreeNode ab = abc.getLeft();
		assertNotNull("root Null left", ab);
		assertEquals("ab", SearchTreeNode.AND, ab.getOperation());
		assertEquals("a", ab.getLeft().getValue());
		assertEquals("b", ab.getRight().getValue());

		assertEquals("c", abc.getRight().getValue());

	}
	
	public void testMultipleWords()
	{
		SearchTreeNode ab = englishParser.parse("a b");
		assertEquals("rootNode", SearchTreeNode.AND, ab.getOperation());
		assertEquals("a", ab.getLeft().getValue());
		assertEquals("b", ab.getRight().getValue());
	}
	
	public void testQuoted()
	{
		String quotedValue = "testing quoted";
		SearchTreeNode quoted = englishParser.parse("\"" + quotedValue + "\"");
		assertEquals(quotedValue, quoted.getValue());
	}
	
	public void testSpecificField()
	{
		SearchTreeNode all = englishParser.parse("testing");
		assertNull("not searching all fields?", all.getField());
		
		SearchTreeNode name = englishParser.parse("name:smith");
		assertEquals("not searching name?", "name", name.getField());
		assertEquals("smith", name.getValue());

		String greenEggs = "green eggs and ham";
		SearchTreeNode phrase = englishParser.parse("name:\"" + greenEggs + "\"");
		assertEquals("not searching name?", "name", phrase.getField());
		assertEquals("green eggs and ham", phrase.getValue());
	}

/*	
 * This test won't be valid until we support parens
 * 
 * public void testReallyComplex()
	{
		// (a and b) OR (c AND (d and e) OR f)
		SearchTreeNode rootNode = englishParser.parse("(a b) or (c (d e) or f)");
		assertNotNull("Null root", rootNode);
		assertEquals("rootNode", SearchTreeNode.OR, rootNode.getOperation());

		SearchTreeNode ab = rootNode.getLeft();
		assertNotNull("ab Null", ab);
		assertEquals("ab", SearchTreeNode.AND, ab.getOperation());
		assertEquals("a", ab.getLeft().getValue());
		assertEquals("b", ab.getRight().getValue());

		SearchTreeNode cdef = rootNode.getRight();
		assertNotNull("cdef Null", cdef);
		assertEquals("cdef", SearchTreeNode.OR, cdef.getOperation());
		assertEquals("f", cdef.getRight().getValue());

		SearchTreeNode cde = cdef.getLeft();
		assertNotNull("cde Null", cde);
		assertEquals("cde", SearchTreeNode.AND, cde.getOperation());
		assertEquals("c", cde.getLeft().getValue());

		SearchTreeNode de = cde.getRight();
		assertNotNull("de Null", de);
		assertEquals("de", SearchTreeNode.AND, de.getOperation());
		assertEquals("d", de.getLeft().getValue());
		assertEquals("e", de.getRight().getValue());
	}
*/
	public void testSpanish()
	{
		// (a OR b) AND c
		SearchParser parser = new SearchParser("y", "o");
		SearchTreeNode abc = parser.parse("a o b y c");
		assertNotNull("Null root", abc);
		assertEquals("rootNode", SearchTreeNode.AND, abc.getOperation());

		SearchTreeNode ab = abc.getLeft();
		assertNotNull("root Null left", ab);
		assertEquals("ab", SearchTreeNode.OR, ab.getOperation());
		assertEquals("a", ab.getLeft().getValue());
		assertEquals("b", ab.getRight().getValue());

		assertEquals("c", abc.getRight().getValue());
	}
	
	public void testEnglishAndAndOrAlwaysWork()
	{
		SearchParser parser = new SearchParser("y", "o");
		SearchTreeNode o = parser.parse("a o b");
		assertEquals("'o' not OR?", SearchTreeNode.OR, o.getOperation());
		SearchTreeNode y = parser.parse("a y b");
		assertEquals("'y' not AND?", SearchTreeNode.AND, y.getOperation());
		SearchTreeNode or = parser.parse("a or b");
		assertEquals("'or' not OR?", SearchTreeNode.OR, or.getOperation());
		SearchTreeNode and = parser.parse("a and b");
		assertEquals("'and' not OR?", SearchTreeNode.AND, and.getOperation());
		
	}
	
	SearchParser englishParser;
}
