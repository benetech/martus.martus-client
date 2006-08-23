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

import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.BulletinHtmlGenerator;
import org.martus.common.fieldspec.MiniFieldSpec;

public class PageReportBuilder
{
	public PageReportBuilder(MiniLocalization localizationToUse)
	{
		
	}
	
	public ReportFormat createPageReport(MiniFieldSpec[] specs)
	{
		ReportFormat rf = new ReportFormat();
		rf.setBulletinPerPage(true);
		rf.setDocumentStartSection(createStartSection());
		rf.setHeaderSection(createHeaderSection());
		rf.setFakePageBreakSection("<hr></hr>\n");
		rf.setDetailSection(createDetailSection());
		rf.setFooterSection("</table>");
		rf.setDocumentEndSection(createEndSection());
		rf.setSpecsToInclude(specs);
		return rf;
	}
	
	public String createStartSection()
	{
		StringBuffer result = new StringBuffer();
		result.append("<html>");
		result.append("<meta http-equiv='Content-Type' content='text/html;charset=UTF-8'>");
		return result.toString();
	}

	public String createEndSection()
	{
		return "</html>";
	}
	
	public String createHeaderSection()
	{
		StringBuffer result = new StringBuffer();
		BulletinHtmlGenerator.appendTableStart(result, "width='100%'");
		return result.toString();
	}
	
	public String createDetailSection()
	{
		StringBuffer result = new StringBuffer();
		BulletinHtmlGenerator.appendTitleOfSection(result, "$localization.getFieldLabel('publicsection')");
		result.append("#foreach($field in $bulletin.getTopFields())\n");
		result.append(getFieldRow());
		result.append("#end\n");
		BulletinHtmlGenerator.appendTitleOfSection(result, "$localization.getFieldLabel('privatesection')");
		result.append("#foreach($field in $bulletin.getBottomFields())\n");
		result.append(getFieldRow());
		result.append("#end\n");

		return result.toString();
	}
	
	public String getFieldRow()
	{
		return "#if($specsToInclude.contains($field.getMiniSpec()))\n" +
				"<tr><td align='right' valign='top'>" +
				"$field.getLocalizedLabel($localization)\n" +
				"</td>" +
				"<td valign='top'>" +
				"$field.html($localization)\n" +
				"</td></tr>\n" +
				"#end\n";
	}
}
