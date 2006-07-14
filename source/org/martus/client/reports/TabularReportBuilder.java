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
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;

public class TabularReportBuilder
{
	public TabularReportBuilder(MiniLocalization localizationToUse)
	{
		localization = localizationToUse;
	}
	
	public ReportFormat createTabular(FieldSpec[] specs)
	{
		ReportFormat rf = new ReportFormat();
		rf.setStartSection(createStartSection(specs));
		rf.setDetailSection(createDetailSection(specs));
		rf.setEndSection(createEndSection());

		return rf;
	}

	private String createStartSection(FieldSpec[] specs)
	{
		StringBuffer startBuffer = new StringBuffer();
		startBuffer.append("<html>");
		startBuffer.append("<table>");
		startBuffer.append("<tr>");
		for(int i = 0; i < specs.length; ++i)
		{
			startBuffer.append("<td>");
			FieldSpec spec = specs[i];
			String label = spec.getLabel();
			if(StandardFieldSpecs.isStandardFieldTag(spec.getTag()))
				label = localization.getFieldLabel(spec.getTag());
			startBuffer.append(label);
			startBuffer.append("</td>");
		}
		startBuffer.append("</tr>");
		return startBuffer.toString();
	}

	private String createDetailSection(FieldSpec[] specs)
	{
		StringBuffer detailBuffer = new StringBuffer();
		detailBuffer.append("<tr>");
		for(int i = 0; i < specs.length; ++i)
		{
			detailBuffer.append("<td>");
			detailBuffer.append("$bulletin.html('" + specs[i].getTag() + "')");
			detailBuffer.append("</td>");
		}
		detailBuffer.append("</tr>");
		return detailBuffer.toString();
	}

	private String createEndSection()
	{
		StringBuffer endBuffer = new StringBuffer();
		endBuffer.append("</table></html>");
		return endBuffer.toString();
	}
	
	MiniLocalization localization;
}
