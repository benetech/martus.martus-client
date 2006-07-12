/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005, Beneficent
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

import org.json.JSONObject;


public class ReportFormat
{
	public ReportFormat()
	{
		setStartSection("");
		setDetailSection("");
		setEndSection("");
	}
	
	public ReportFormat(JSONObject json)
	{
		setStartSection(json.getString(TAG_START_SECTION));
		setDetailSection(json.getString(TAG_DETAIL_SECTION));
		setEndSection(json.getString(TAG_END_SECTION));
	}
	
	public void setStartSection(String section)
	{
		startSection = section;
	}
	
	public String getStartSection()
	{
		return startSection;
	}
	
	public void setEndSection(String section)
	{
		endSection = section;
	}
	
	public String getEndSection()
	{
		return endSection;
	}
	
	public void setDetailSection(String section)
	{
		detailSection = section;
	}
	
	public String getDetailSection()
	{
		return detailSection;
	}
	
	public JSONObject toJson()
	{
		JSONObject json = new JSONObject();
		json.put(TAG_START_SECTION, getStartSection());
		json.put(TAG_DETAIL_SECTION, getDetailSection());
		json.put(TAG_END_SECTION, getEndSection());
		return json;
	}
	
	final static String TAG_START_SECTION = "StartSection";
	final static String TAG_DETAIL_SECTION = "DetailSection";
	final static String TAG_END_SECTION = "EndSection";

	private String startSection;
	private String detailSection;
	private String endSection;
}
