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
package org.martus.client.reports;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Vector;

public class ReportOutput extends Writer
{
	public ReportOutput()
	{
		pages = new Vector();
		currentPage = new StringWriter();
	}
	
	public void close() throws IOException
	{
		pages.add(currentPage.toString());
	}

	public void flush() throws IOException
	{
	}

	public void write(char[] cbuf, int off, int len) throws IOException
	{
		currentPage.write(cbuf, off, len);
	}
	
	public void startNewPage()
	{
	}
	
	public String getPageText(int pageIndex)
	{
		return (String)pages.get(pageIndex);
	}
	
	Vector pages;
	Writer currentPage;
}