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


public class SearchParser
{
	public static SearchParser createEnglishParser()
	{
		return new SearchParser(ENGLISH_AND_KEYWORD, ENGLISH_OR_KEYWORD);
	}

	public SearchParser(String andKeyword, String orKeyword)
	{
		andKeywords = new String[] {spacesAround(andKeyword), ENGLISH_AND_STRING};
		orKeywords = new String[] {spacesAround(orKeyword), ENGLISH_OR_STRING};
	}

	public SearchTreeNode parse(String expression)
	{
		SearchTreeNode rootNode = new SearchTreeNode(expression);
		recursiveParse(rootNode);
		return rootNode;
	}

	private void recursiveParse(SearchTreeNode node)
	{
		KeywordFinder finder = new KeywordFinder(node.getValue());

		int newOp = SearchTreeNode.OR;
		if(!finder.findFirstKeyword(orKeywords))
		{
			newOp = SearchTreeNode.AND;
			finder.findFirstKeyword(andKeywords);
		}

		if(finder.foundMatch())
		{
			node.convertToOp(newOp, finder.getLeftText(), finder.getRightText());
			recursiveParse(node.getLeft());
			recursiveParse(node.getRight());
		}
	}

	private static String spacesAround(String andKeyword)
	{
		return " " + andKeyword + " ";
	}

	private static final String ENGLISH_AND_KEYWORD = "and";
	private static final String ENGLISH_OR_KEYWORD = "or";
	private static final String ENGLISH_AND_STRING = spacesAround(ENGLISH_AND_KEYWORD);
	private static final String ENGLISH_OR_STRING = spacesAround(ENGLISH_OR_KEYWORD);
	private final String[] orKeywords;
	private final String[] andKeywords;
}

class KeywordFinder
{
	KeywordFinder(String textToSearch)
	{
		text = textToSearch.toLowerCase();
		foundAt = -1;
	}
	
	boolean findFirstKeyword(String[] keywords)
	{
		boolean foundOne = false;
		for(int i=0; i < keywords.length; ++i)
		{
			String thisKeyword = keywords[i];
			int at = text.indexOf(thisKeyword);
			if(at >= 0 && (foundAt < 0 || at < foundAt) )
			{
				foundOne = true;
				foundAt = at;
				foundWord = thisKeyword;
			}
		}
		
		return foundOne;
	}

	boolean foundMatch()
	{
		return (foundAt >= 0);
	}
	
	String getLeftText()
	{
		return text.substring(0, foundAt);
	}
	
	String getRightText()
	{
		return text.substring(foundAt + foundWord.length(), text.length());
	}
	
	private String text;
	private int foundAt;
	private String foundWord;
}
