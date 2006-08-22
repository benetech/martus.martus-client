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

import org.martus.client.core.SafeReadableBulletin;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;

public class TabularReportBuilder
{
	public TabularReportBuilder(MiniLocalization localizationToUse)
	{
		localization = localizationToUse;
	}
	
	public ReportFormat createTabular(MiniFieldSpec[] specs)
	{
		ReportFormat rf = new ReportFormat();
		rf.setStartSection(createStartSection());
		rf.setHeaderSection(createHeaderSection(specs));
		rf.setDetailSection(createDetailSection(specs));
		rf.setBreakSection(createBreakSection());
		rf.setTotalSection(createTotalSection());
		rf.setEndSection(createEndSection());

		return rf;
	}

	private String createStartSection()
	{
		StringBuffer startBuffer = new StringBuffer();
		startBuffer.append("<html>");
		startBuffer.append("<meta http-equiv='Content-Type' content='text/html;charset=UTF-8'>");
		startBuffer.append("<table border='3' cellpadding='5' cellspacing='0'>");
		return startBuffer.toString();
	}

	private String createHeaderSection(MiniFieldSpec[] specs)
	{
		StringBuffer headerBuffer = new StringBuffer();
		headerBuffer.append("<tr>");
		for(int i = 0; i < specs.length; ++i)
		{
			headerBuffer.append("<th>");
			MiniFieldSpec spec = specs[i];
			String label = StandardFieldSpecs.getLocalizedLabel(spec.getTag(), spec.getLabel(), localization);
			headerBuffer.append(label);
			headerBuffer.append("</th>");
		}
		headerBuffer.append("</tr>");
		return headerBuffer.toString();
	}

	private String createDetailSection(MiniFieldSpec[] specs)
	{
		StringBuffer detailBuffer = new StringBuffer();
		detailBuffer.append("<tr>");
		for(int i = 0; i < specs.length; ++i)
		{
			detailBuffer.append("<td>");
			detailBuffer.append(getFieldCall(specs[i]));
			detailBuffer.append(".html($localization)");
			detailBuffer.append("</td>");
		}
		detailBuffer.append("</tr>");
		return detailBuffer.toString();
	}

	private String getFieldCall(MiniFieldSpec spec)
	{
		String[] tags = SafeReadableBulletin.parseNestedTags(spec.getTag());
		String topLevelTag = tags[0];
		
		StringBuffer result = new StringBuffer();
		result.append("$bulletin.field('");
		result.append(topLevelTag);
		result.append("', '");
		result.append(spec.getTopLevelLabel());
		result.append("', '");
		result.append(spec.getTopLevelType().getTypeName());
		result.append("')");
		
		for(int i = 1; i < tags.length; ++i)
		{
			result.append(".getSubField('");
			result.append(tags[i]);
			result.append("', $localization)");
		}
			
		return result.toString();
	}
	
	private String createBreakSection()
	{
		return "<tr><td colspan='999'>" +
				"#foreach( $foo in [0..$BreakLevel] )\n" +
				INDENT + "\n" +
				"#end " +
				"$BreakFields.get($BreakLevel).getLocalizedLabel($localization): " +
				"$BreakFields.get($BreakLevel).html($localization) = " +
				"$BreakCount: " +
				"</td></tr>";
	}
	
	private String createTotalSection()
	{
		return "<p>$localization.getFieldLabel('ReportNumberOfBulletins') $totals.count()</p>\n" +
				"#foreach($summary1 in $totals.children())\n" +
				"<p>$summary1.label(): $summary1.value() = $summary1.count()</p>\n" +
				"#foreach($summary2 in $summary1.children())\n" +
				"<p>" + INDENT + "$summary2.label(): $summary2.value() = $summary2.count()\n" +
				"</p>#foreach($summary3 in $summary2.children())\n" +
				"<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;$summary3.label(): $summary3.value() = $summary3.count()</p>\n" +
				"#end\n" +
				"<p></p>\n" +
				"#end\n" +
				"<p></p>\n" +
				"#end\n";
	}

	private String createEndSection()
	{
		StringBuffer endBuffer = new StringBuffer();
		endBuffer.append("</table></html>");
		return endBuffer.toString();
	}
	
	private static final String INDENT = "&nbsp;&nbsp;&nbsp;&nbsp;";

	MiniLocalization localization;
}
