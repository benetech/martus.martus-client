/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2006, Beneficent
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

import org.json.JSONArray;
import org.json.JSONObject;
import org.martus.common.fieldspec.MiniFieldSpec;


public class ReportFormat
{
	public ReportFormat()
	{
		setBulletinPerPage(false);
		setStartSection("");
		setHeaderSection("");
		setDetailSection("");
		setBreakSection("");
		setFooterSection("");
		setFakePageBreakSection("");
		setEndSection("");
		specsToInclude = new MiniFieldSpec[0];
		version = 0;
	}
	
	public ReportFormat(JSONObject json)
	{
		version = json.optInt(TAG_VERSION, 0);
		setBulletinPerPage(json.optBoolean(TAG_BULLETIN_PER_PAGE));
		setStartSection(json.getString(TAG_START_SECTION));
		setHeaderSection(json.optString(TAG_HEADER_SECTION, ""));
		setDetailSection(json.getString(TAG_DETAIL_SECTION));
		setBreakSection(json.optString(TAG_BREAK_SECTION, ""));
		setFooterSection(json.optString(TAG_FOOTER_SECTION, ""));
		setFakePageBreakSection(json.optString(TAG_FAKE_PAGE_BREAK_SECTION, ""));
		setEndSection(json.getString(TAG_END_SECTION));
		JSONArray specs = json.optJSONArray(TAG_SPECS);
		if(specs == null)
			specs = new JSONArray();
		specsToInclude = new MiniFieldSpec[specs.length()];
		for(int i = 0; i < specsToInclude.length; ++i)
		{
			specsToInclude[i] = new MiniFieldSpec(specs.getJSONObject(i));
		}
		
	}
	
	public int getVersion()
	{
		return version;
	}
	
	public void setBulletinPerPage(boolean newSetting)
	{
		bulletinPerPage = newSetting;
	}
	
	public boolean getBulletinPerPage()
	{
		return bulletinPerPage;
	}
	
	public void setStartSection(String section)
	{
		startSection = section;
	}
	
	public String getStartSection()
	{
		return startSection;
	}
	
	public void setHeaderSection(String section)
	{
		headerSection = section;
	}
	
	public String getHeaderSection()
	{
		return headerSection;
	}
	
	public void setDetailSection(String section)
	{
		detailSection = section;
	}
	
	public String getDetailSection()
	{
		return detailSection;
	}
	
	public void setBreakSection(String section)
	{
		breakSection = section;
	}
	
	public String getBreakSection()
	{
		return breakSection;
	}
	
	public void setFooterSection(String section)
	{
		footerSection = section;
	}
	
	public String getFooterSection()
	{
		return footerSection;
	}
	
	public void setFakePageBreakSection(String section)
	{
		fakePageBreakSection = section;
	}
	
	public String getFakePageBreakSection()
	{
		return fakePageBreakSection;
	}
	
	public void setEndSection(String section)
	{
		endSection = section;
	}
	
	public String getEndSection()
	{
		return endSection;
	}
	
	public void setSpecsToInclude(MiniFieldSpec[] specs)
	{
		specsToInclude = specs;
	}
	
	public MiniFieldSpec[] getSpecsToInclude()
	{
		return specsToInclude;
	}
	
	public JSONObject toJson()
	{
		JSONObject json = new JSONObject();
		json.put(TAG_VERSION, EXPECTED_VERSION);
		json.put(TAG_BULLETIN_PER_PAGE, getBulletinPerPage());
		json.put(TAG_START_SECTION, getStartSection());
		json.put(TAG_HEADER_SECTION, getHeaderSection());
		json.put(TAG_DETAIL_SECTION, getDetailSection());
		json.put(TAG_BREAK_SECTION, getBreakSection());
		json.put(TAG_FOOTER_SECTION, getFooterSection());
		json.put(TAG_FAKE_PAGE_BREAK_SECTION, getFakePageBreakSection());
		json.put(TAG_END_SECTION, getEndSection());
		JSONArray specs = new JSONArray();
		for(int i = 0; i < specsToInclude.length; ++i)
		{
			specs.put(specsToInclude[i].toJson());
		}
		json.put(TAG_SPECS, specs);
		return json;
	}
	
	final static String TAG_VERSION = "Version";
	final static String TAG_BULLETIN_PER_PAGE = "BulletinPerPage";
	final static String TAG_START_SECTION = "StartSection";
	final static String TAG_HEADER_SECTION = "HeaderSection";
	final static String TAG_DETAIL_SECTION = "DetailSection";
	final static String TAG_BREAK_SECTION = "BreakSection";
	final static String TAG_FOOTER_SECTION = "FooterSection";
	final static String TAG_FAKE_PAGE_BREAK_SECTION = "FakePageBreak";
	final static String TAG_END_SECTION = "EndSection";
	final static String TAG_SPECS = "Specs";
	
	public final static int EXPECTED_VERSION = 6;

	private int version;
	private String startSection;
	private String headerSection;
	private String detailSection;
	private String breakSection;
	private String footerSection;
	private String fakePageBreakSection;
	private String endSection;
	private MiniFieldSpec[] specsToInclude;
	
	/* 
	 * tabular to disk or printer:
	 *  - start <html><table>
	 *  
	 *  - header <tr><th></th></tr>
	 *  - detail, detail, detail, break,
	 *  - footer (empty)
	 *  
	 *  - end </table></html>
	 *  
	 * page to disk (multiple bulletins):
	 *  - start 
	 *  
	 *  - header <html><table>
	 *  - detail, break,
	 *  - footer </table>
	 *  
	 *  - fakePageBreak <hr/>
	 *  
	 *  - header <table>
	 *  - detail, break,
	 *  - footer </table></html>
	 *  
	 *  - end 
	 * 
	 * page to printer (for each bulletin):
	 *  - start 
	 *  
	 *  - header <html><table>
	 *  - detail, break,
	 *  - footer </table></html>
	 *  
	 *  - end 
	 * 
	 */
		
	
	private boolean bulletinPerPage;
}
