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

package org.martus.client.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import org.martus.client.swingui.fields.UiFlexiDateViewer;
import org.martus.common.FieldSpec;
import org.martus.common.GridData;
import org.martus.common.GridFieldSpec;
import org.martus.common.MartusUtilities;
import org.martus.common.StandardFieldSpecs;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.database.Database.RecordHiddenException;
import org.martus.common.packet.UniversalId;
import org.martus.swing.UiLanguageDirection;

public class BulletinHtmlGenerator
{
	public BulletinHtmlGenerator(int widthToUse, UiBasicLocalization localizationToUse)
	{
		width = widthToUse;
		localization = localizationToUse;
	}

	public String getHtmlString(Bulletin b, ReadableDatabase database, boolean includePrivateData, boolean yourBulletin)
	{
		bulletin = b;
		StringBuffer html = new StringBuffer(1000);
		html.append("<html>");
		
		html.append("<table width='");
		html.append(Integer.toString(width));
		html.append("'>");		
		appendHeadHtml(html, b);
		
		if(!yourBulletin)
		{
			html.append("<tr></tr>");
			html.append(getHtmlEscapedFieldHtmlString(localization.getFieldLabel("BulletinNotYours"),""));		
			html.append("<tr></tr>");	
		}

		String publicSectionTitle =  localization.getFieldLabel("publicsection");
		String allPrivateValueTag = "no";
		if(b.isAllPrivate())
		{	
			allPrivateValueTag = "yes";				
			publicSectionTitle =  localization.getFieldLabel("privatesection");
			if (!includePrivateData)
			{
				appendTitleOfSection(html, publicSectionTitle);	
				appendTailHtml(html, b);
				return html.toString();
			}
		}
		

		appendTitleOfSection(html, publicSectionTitle);
		html.append(getHtmlEscapedFieldHtmlString(localization.getFieldLabel("allprivate"), localization.getButtonLabel(allPrivateValueTag)));

		FieldSpec[] standardFieldTags = b.getPublicFieldSpecs();
		html.append(getSectionHtmlString(b, standardFieldTags));
		html.append(getAttachmentsHtmlString(b.getPublicAttachments(), database));

		if (includePrivateData)
		{	
			html.append("<tr></tr>");
			appendTitleOfSection(html, localization.getFieldLabel("privatesection"));
			FieldSpec[] privateFieldTags = b.getPrivateFieldSpecs();
			html.append(getSectionHtmlString(b, privateFieldTags));
			html.append(getAttachmentsHtmlString(b.getPrivateAttachments(), database));
		}
		
		appendTailHtml(html, b);
		return html.toString();
	}
	
	private void appendTitleOfSection(StringBuffer html, String title)
	{
		html.append("<tr><td colspan='2'>");
		html.append(getFieldHtmlString("<u><b>"+title+"</b></u>",""));
		html.append("</td></tr>");
		html.append("\n");
	}	
	
	private void appendHeadHtml(StringBuffer html, Bulletin b )
	{
		html.append(getHtmlEscapedFieldHtmlString(localization.getFieldLabel("BulletinLastSaved"), b.getLastSavedDateTime()));
		html.append(getHtmlEscapedFieldHtmlString(localization.getFieldLabel("BulletinVersionNumber"), (new Integer(b.getVersion())).toString()));
		html.append(getHtmlEscapedFieldHtmlString(localization.getFieldLabel("BulletinStatus"), localization.getStatusLabel(b.getStatus())));
		html.append("<tr></tr>");		
	}
	
	private void appendTailHtml(StringBuffer html, Bulletin b )
	{
		html.append("<tr></tr>");
		html.append(getHtmlEscapedFieldHtmlString(localization.getFieldLabel("BulletinId"),b.getLocalId()));
		html.append("</table>");
		html.append("</html>");
	}

	private String getSectionHtmlString(Bulletin b, FieldSpec[] standardFieldTags)
	{
		String sectionHtml = "";
		for(int fieldNum = 0; fieldNum < standardFieldTags.length; ++fieldNum)
		{
			FieldSpec spec = standardFieldTags[fieldNum];
			String tag = spec.getTag();
			String label = getHTMLEscaped(spec.getLabel());			
		
			String value = getHTMLEscaped(b.get(tag));
			if(spec.getType() == FieldSpec.TYPE_DATE)
				value = localization.convertStoredDateToDisplay(value);
			else if(spec.getType() == FieldSpec.TYPE_LANGUAGE)
				value = getHTMLEscaped(localization.getLanguageName(value));
			else if(spec.getType() == FieldSpec.TYPE_MULTILINE)
				value = insertNewlines(value);
			else if(spec.getType() == FieldSpec.TYPE_DATERANGE)
				value = UiFlexiDateViewer.getViewableDateRange(value, localization);
			else if(spec.getType() == FieldSpec.TYPE_BOOLEAN)
			{
				if(value.equals(FieldSpec.TRUESTRING))
					value = getHTMLEscaped(localization.getButtonLabel("yes"));
				else
					value = getHTMLEscaped(localization.getButtonLabel("no"));
			}
			else if(spec.getType() == FieldSpec.TYPE_GRID)
			{
				GridFieldSpec grid = (GridFieldSpec)spec;
				value = "<table border='1' align='right'><tr>";
				String justification = "center";
				if(!UiLanguageDirection.isRightToLeftLanguage())
					value += getItemToAddForTable(grid.getColumnZeroLabel(),TABLE_HEADER, justification);
				int columnCount = grid.getColumnCount();
				for(int i = 0; i < columnCount; ++i)
				{
					String data = grid.getColumnLabel(i);
					if(UiLanguageDirection.isRightToLeftLanguage())
						data = grid.getColumnLabel((columnCount-1)-i);
					value += getItemToAddForTable(data,TABLE_HEADER, justification);
				}
				if(UiLanguageDirection.isRightToLeftLanguage())
					value += getItemToAddForTable(grid.getColumnZeroLabel(),TABLE_HEADER, justification);
				value += "</tr>";
				try
				{
					GridData gridData = new GridData();
					gridData.setFromXml(b.get(tag));
					int rowCount = gridData.getRowCount();

					justification = "left";
					if(UiLanguageDirection.isRightToLeftLanguage())
						justification = "right";
					
					for(int r =  0; r<rowCount; ++r)
					{
						value += "<tr>";
						if(!UiLanguageDirection.isRightToLeftLanguage())
							value += getItemToAddForTable(Integer.toString(r+1),TABLE_DATA, justification);
						for(int c = 0; c<columnCount; ++c)
						{
							String data = gridData.getValueAt(r, c);
							if(UiLanguageDirection.isRightToLeftLanguage())
								data = gridData.getValueAt(r, ((columnCount-1)-c));
							value += getItemToAddForTable(data, TABLE_DATA, justification);
						}
						if(UiLanguageDirection.isRightToLeftLanguage())
							value += getItemToAddForTable(Integer.toString(r+1),TABLE_DATA, justification);
						value += "</tr>";
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				
				value += "</table>";
				
			}
			
			if(StandardFieldSpecs.isStandardFieldTag(tag))
				label = getHTMLEscaped(localization.getFieldLabel(tag));
							
			String fieldHtml = getFieldHtmlString(label, value);
			sectionHtml += fieldHtml;
		}
		return sectionHtml;
	}

	private String getItemToAddForTable(String data, String type, String justification)
	{
		return "<"+type+" align='"+justification+"'>"+getHTMLEscaped(data)+"</"+type+">";
	}

	private String getSizeInKb(int sizeBytes)
	{
		int sizeInKb = sizeBytes / 1024;
		if (sizeInKb == 0)
			sizeInKb = 1;
		return Integer.toString(sizeInKb);
	}

	private String getAttachmentSize(ReadableDatabase db, UniversalId uid)
	{
		// TODO :This is a duplicate code from AttachmentTableModel.java. 
		// Ideally, the AttachmentProxy should self-describe of file size and file description.

		String size = "";
		try
		{
			int rawSize = 0;
			if (bulletin.getStatus().equals(Bulletin.STATUSDRAFT))
				rawSize = db.getRecordSize(DatabaseKey.createDraftKey(uid));
			else
				rawSize = db.getRecordSize(DatabaseKey.createSealedKey(uid));

			rawSize -= 1024;//Public code & overhead
			rawSize = rawSize * 3 / 4;//Base64 overhead
			size = getSizeInKb(rawSize);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (RecordHiddenException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return size;
	}

	private String getAttachmentsHtmlString(AttachmentProxy[] attachments, ReadableDatabase db)
	{
		String attachmentList = "";
	
		for(int i = 0 ; i < attachments.length ; ++i)
		{
			AttachmentProxy aProxy = attachments[i];
			String label = aProxy.getLabel();
			String size = "( " + getAttachmentSize(db, aProxy.getUniversalId())+ " " + localization.getFieldLabel("attachmentSizeForPrinting")+ " )";
			if(UiLanguageDirection.isRightToLeftLanguage())
			{
				String tmp = label;
				label = size;
				size = tmp;
			}

			attachmentList += "<p>" + getHTMLEscaped(label) + "    " + getHTMLEscaped(size) + "</p>";
		}
		return getFieldHtmlString(localization.getFieldLabel("attachments"), attachmentList);
	}

	private String getHtmlEscapedFieldHtmlString(String label, String value)
	{
		return getFieldHtmlString(getHTMLEscaped(label), getHTMLEscaped(value));
	}

	private String getFieldHtmlString(String label, String value)
	{
		StringBuffer fieldHtml = new StringBuffer(value.length() + 100);
		String width = "15%";
		if(UiLanguageDirection.isRightToLeftLanguage())
		{
			String tmp = label;
			label = value;
			value = tmp;
			width = "70%";
		}
		fieldHtml.append("<tr><td width='"+width+"' align='right' valign='top'>");		
		fieldHtml.append(label);
		fieldHtml.append("</td>");
		fieldHtml.append("<td valign='top'>");
		fieldHtml.append(value);
		fieldHtml.append("</td></tr>");
		fieldHtml.append("\n");
		return new String(fieldHtml);
	}

	private String insertNewlines(String value)
	{
		final String P_TAG_BEGIN = "<p>";
		final String P_TAG_END = "</p>";
		StringBuffer html = new StringBuffer(value.length() + 100);
		html.append(P_TAG_BEGIN);

		try
		{
			BufferedReader reader = new BufferedReader(new StringReader(getHTMLEscaped(value)));
			String thisParagraph = null;
			while((thisParagraph = reader.readLine()) != null)
			{
				html.append(thisParagraph);
				html.append(P_TAG_END);
				html.append(P_TAG_BEGIN);
			}
		}
		catch (IOException e)
		{
			html.append("...?");
		}

		html.append(P_TAG_END);
		return new String(html);
	}
	
	private String getHTMLEscaped(String text)
	{
		return MartusUtilities.getXmlEncoded(text);
	}

	int width;
	UiBasicLocalization localization;
	Bulletin bulletin;

	private static final String TABLE_HEADER = "th";
	private static final String TABLE_DATA = "td";
	
}
