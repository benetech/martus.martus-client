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
		andKeywords = new String[] {andKeyword, ENGLISH_AND_KEYWORD};
		orKeywords = new String[] {orKeyword, ENGLISH_OR_KEYWORD};
	}
	
	public TokenList tokenize(String expression)
	{
		char[] toTokenize = expression.toCharArray();
		
		boolean inQuotedString = false;
		
		TokenList result = new TokenList();
		StringBuffer thisToken = new StringBuffer();
		for(int i = 0; i < expression.length(); ++i)
		{
			char thisCharacter = toTokenize[i];
			
			if(thisCharacter == '"')
			{
				inQuotedString = !inQuotedString;
			}
			
			if(!inQuotedString && Character.isSpaceChar(thisCharacter))
			{
				result.add(new String(thisToken));
				thisToken.setLength(0);
			}
			else
			{
				thisToken.append(thisCharacter);
			}
		}
		result.add(new String(thisToken));

		return result;
	}

	public SearchTreeNode parse(String expression)
	{
		int nextOp = SearchTreeNode.AND;
		SearchTreeNode left = null;
		
		TokenList tokens = tokenize(expression);
		for(int i=0; i < tokens.size(); ++i)
		{
			String thisToken = tokens.get(i);
			if(isKeyword(thisToken, andKeywords))
			{
				continue;
			}
			else if(isKeyword(thisToken, orKeywords))
			{
				nextOp = SearchTreeNode.OR;
				continue;
			}
			
			SearchTreeNode thisNode = new SearchTreeNode(thisToken);
			
			if(left == null)
			{
				left = thisNode;
			}
			else
			{
				SearchTreeNode right = thisNode;
				left = new SearchTreeNode(nextOp, left, right);
				nextOp = SearchTreeNode.AND;
			}
		}
		
		if(left == null)
			left = new SearchTreeNode("");
		
		return left;
	}
	
	public boolean isKeyword(String candidate, String[] keywords)
	{
		String comparableCandidate = candidate.toLowerCase();
		for(int i=0; i < keywords.length; ++i)
			if(comparableCandidate.equals(keywords[i]))
				return true;
		
		return false;
	}

	private static final String ENGLISH_AND_KEYWORD = "and";
	private static final String ENGLISH_OR_KEYWORD = "or";
	private final String[] orKeywords;
	private final String[] andKeywords;
}
