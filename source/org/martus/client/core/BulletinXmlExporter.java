/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2006, Beneficent
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

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import org.martus.client.swingui.dialogs.UiImportExportProgressMeterDlg;
import org.martus.client.swingui.fields.UiAttachmentViewer;
import org.martus.common.MartusXml;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.bulletin.BulletinXmlExportImportConstants;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.packet.BulletinHistory;
import org.martus.util.xml.XmlUtilities;

public class BulletinXmlExporter
{
	public BulletinXmlExporter(MartusApp appToUse, MiniLocalization localizationToUse, UiImportExportProgressMeterDlg progressMeterToUse)
	{
		app = appToUse;
		localization = localizationToUse;
		progressMeter = progressMeterToUse;
		failingAttachments = 0;
		bulletinsExported = 0;
	}
	
	public void exportBulletins(Writer dest, Vector bulletins, boolean includePrivateData, boolean includeAttachments, File attachmentsDirectory)
		throws IOException
	{
		dest.write(MartusXml.getTagStartWithNewline(BulletinXmlExportImportConstants.MARTUS_BULLETINS));
		writeXMLVersion(dest);
		writeExportMetaData(dest, includePrivateData, includeAttachments);

		int bulletinCount = bulletins.size();
		for (int i = 0; i < bulletinCount; i++)
		{
			if(progressMeter != null)
			{
				progressMeter.updateBulletinCountMeter(i+1, bulletins.size());
				if(progressMeter.shouldExit())
					break;
			}
			Bulletin b = (Bulletin)bulletins.get(i);
			exportOneBulletin(dest, b, includePrivateData, includeAttachments, attachmentsDirectory);
			++bulletinsExported;
		}
		dest.write(MartusXml.getTagEnd(BulletinXmlExportImportConstants.MARTUS_BULLETINS));
	}
	
	public int getNumberOfBulletinsExported()
	{
		return bulletinsExported;
	}
	
	public int getNumberOfFailingAttachments()
	{
		return failingAttachments;
	}
	
	private void writeXMLVersion(Writer dest) throws IOException
	{
		dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.XML_EXPORT_VERSION,BulletinXmlExportImportConstants.XML_EXPORT_VERSION_NUMBER));
	}
	
	private void writeExportMetaData(Writer dest, boolean includePrivateData, boolean includeAttachments) throws IOException
	{
		dest.write(MartusXml.getTagStartWithNewline(BulletinXmlExportImportConstants.EXPORT_META_DATA));
		if(includePrivateData)
		{
			dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.PUBLIC_AND_PRIVATE,""));
		}
		else
		{
			dest.write("<!--  No Private FieldSpecs or Data was exported  -->\n");
			dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.PUBLIC_ONLY,""));
		}
		
		if(!includeAttachments)
			dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.NO_ATTACHMENTS_EXPORTED,""));
		
		dest.write(MartusXml.getTagEnd(BulletinXmlExportImportConstants.EXPORT_META_DATA));
		dest.write(BulletinXmlExportImportConstants.NEW_LINE);
	}

	private void writeBulletinMetaData(Writer dest, Bulletin b) throws IOException
	{
		dest.write(MartusXml.getTagStartWithNewline(BulletinXmlExportImportConstants.BULLETIN_META_DATA));
		dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.ACCOUNT_ID, b.getAccount()));
		dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.LOCAL_ID, b.getLocalId()));
		
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(new Date(b.getLastSavedTime()));
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String dateAndTime = format.format(cal.getTime());
		dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.BULLETIN_LAST_SAVED_DATE_TIME, dateAndTime));

		if(b.isAllPrivate())
			dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.ALL_PRIVATE, ""));
		writeBulletinStatus(dest, b);			
		writeBulletinHistory(dest, b);
		dest.write(MartusXml.getTagEnd(BulletinXmlExportImportConstants.BULLETIN_META_DATA));
	}

	private void writeBulletinHistory(Writer dest, Bulletin b) throws IOException
	{
		dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.BULLETIN_VERSION,Integer.toString(b.getVersion())));
		BulletinHistory history = b.getHistory();
		if(history.size() > 0)
		{
			dest.write(MartusXml.getTagStartWithNewline(BulletinXmlExportImportConstants.HISTORY));
			for(int i=0; i < history.size(); ++i)
			{
				dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.ANCESTOR, history.get(i)));
			}
			dest.write(MartusXml.getTagEnd(BulletinXmlExportImportConstants.HISTORY));
		}
	}

	private void writeBulletinStatus(Writer dest, Bulletin b) throws IOException
	{
		String statusLocalized = localization.getStatusLabel("draft");
		String status = Bulletin.STATUSDRAFT;
		if(b.isSealed())
		{
			statusLocalized = localization.getStatusLabel("sealed");
			status = Bulletin.STATUSSEALED;
		}
		dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.BULLETIN_STATUS, status));
		dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.BULLETIN_STATUS_LOCALIZED, statusLocalized));
	}
	
	private void writeBulletinFieldSpecs(Writer dest, Bulletin b, boolean includePrivateData) throws IOException
	{
		if(shouldIncludeTopSection(b, includePrivateData))
			writeFieldSpecs(dest, b.getTopSectionFieldSpecs(), BulletinXmlExportImportConstants.MAIN_FIELD_SPECS);
		if(includePrivateData)
			writeFieldSpecs(dest, b.getBottomSectionFieldSpecs(), BulletinXmlExportImportConstants.PRIVATE_FIELD_SPECS);
	}

	private boolean shouldIncludeTopSection(Bulletin b, boolean includePrivateData)
	{
		return includePrivateData || !b.isAllPrivate();
	}

	public void writeFieldSpecs(Writer dest, FieldSpec[] specs, String xmlTag) throws IOException
	{
		dest.write(MartusXml.getTagStartWithNewline(xmlTag));
		for(int i = 0; i < specs.length; i++)
		{
			dest.write(specs[i].toXml(BulletinXmlExportImportConstants.FIELD));
		}
		dest.write(MartusXml.getTagEnd(xmlTag));
		dest.write(BulletinXmlExportImportConstants.NEW_LINE);
	}

	private void exportOneBulletin(Writer dest, Bulletin b, boolean includePrivateData, boolean includeAttachments, File attachmentsDirectory) throws IOException
	{
		dest.write(MartusXml.getTagStartWithNewline(BulletinXmlExportImportConstants.BULLETIN));

		writeBulletinMetaData(dest, b);
		writeBulletinFieldSpecs(dest, b, includePrivateData);

		dest.write(MartusXml.getTagStartWithNewline(BulletinXmlExportImportConstants.FIELD_VALUES));
		if(shouldIncludeTopSection(b, includePrivateData))
		{
			writeFields(dest, b, b.getTopSectionFieldSpecs());
			if(includeAttachments)
				writeAttachments(dest, attachmentsDirectory, b.getPublicAttachments(), BulletinXmlExportImportConstants.TOP_SECTION_ATTACHMENT_LIST);
		}

		if(includePrivateData)
		{
			writeFields(dest, b, b.getBottomSectionFieldSpecs());
			if(includeAttachments)
				writeAttachments(dest, attachmentsDirectory, b.getPrivateAttachments(), BulletinXmlExportImportConstants.BOTTOM_SECTION_ATTACHMENT_LIST);
		}
		dest.write(MartusXml.getTagEnd(BulletinXmlExportImportConstants.FIELD_VALUES));

		dest.write(MartusXml.getTagEnd(BulletinXmlExportImportConstants.BULLETIN));
		dest.write(BulletinXmlExportImportConstants.NEW_LINE);
	}

	private void writeAttachments(Writer dest, File attachmentsDirectory, AttachmentProxy[] attachments, String attachmentSectionTag)
		throws IOException
	{
		if(attachments.length == 0)
			return;

		dest.write(MartusXml.getTagStartWithNewline(attachmentSectionTag));
		for (int i = 0; i < attachments.length; i++)
		{
			AttachmentProxy proxy = attachments[i];
			String fileName = proxy.getLabel();
			File attachment = new File(attachmentsDirectory, fileName);
			try
			{
				if(attachment.exists())
				{
					String nameOnly = UiAttachmentViewer.extractFileNameOnly(fileName);
					String extensionOnly = UiAttachmentViewer.extractExtentionOnly(fileName);
					
					attachment = File.createTempFile(nameOnly, extensionOnly, attachmentsDirectory);
				}
				ReadableDatabase db = app.getStore().getDatabase();
				BulletinLoader.extractAttachmentToFile(db, proxy, app.getSecurity(), attachment);

				dest.write(MartusXml.getTagStartWithNewline(BulletinXmlExportImportConstants.ATTACHMENT));
				dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.FILENAME, attachment.getName()));
				dest.write(MartusXml.getTagEnd(BulletinXmlExportImportConstants.ATTACHMENT));
			}
			catch(Exception e)
			{
				dest.write(getXmlEncodedTagWithData(BulletinXmlExportImportConstants.EXPORT_ERROR_ATTACHMENT_FILENAME, fileName));
				++failingAttachments;
				attachment.delete();
			}
		}
		dest.write(MartusXml.getTagEnd(attachmentSectionTag));
	}

	private void writeFields(Writer dest, Bulletin b, FieldSpec[] specs)
		throws IOException
	{
		for (int i = 0; i < specs.length; i++)
		{
			FieldSpec spec = specs[i];
			if(spec.hasUnknownStuff())
				continue;		
			final String tag = spec.getTag();
			MartusField field = b.getField(tag);
			String value = field.getExportableData(localization);
			if(spec.getType().isGrid())
			{
				String valueTagAndData = MartusXml.getTagWithData(BulletinXmlExportImportConstants.VALUE, value);
				writeElementDirect(dest, tag, valueTagAndData);
			}
			else
			{
				writeElement(dest, tag, value);
			}
		}
		
	}
	
	private static String getXmlEncodedTagWithData(String tagName, String data)
	{
		return MartusXml.getTagWithData(tagName, XmlUtilities.getXmlEncoded(data));
	}
	
	private static void writeElement(Writer dest, String tag, String fieldData) throws IOException
	{
		String xmlFieldTagWithData = getXmlEncodedTagWithData(BulletinXmlExportImportConstants.VALUE, fieldData);
		writeElementDirect(dest, tag, xmlFieldTagWithData);
	}

	private static void writeElementDirect(Writer dest, String tag, String xmlFieldData) throws IOException
	{
		dest.write(MartusXml.getTagStartWithNewline("Field "+BulletinXmlExportImportConstants.TAG_ATTRIBUTE+"='"+tag+"'"));
		dest.write(xmlFieldData);
		dest.write(MartusXml.getTagEnd(MartusXml.tagField));		
	}

	MiniLocalization localization;
	MartusApp app;
	int bulletinsExported;
	int failingAttachments;
	UiImportExportProgressMeterDlg progressMeter;

}
