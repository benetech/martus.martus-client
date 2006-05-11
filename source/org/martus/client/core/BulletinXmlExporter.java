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

import java.io.IOException;
import java.io.Writer;
import java.util.Vector;

import org.martus.common.MartusXml;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.packet.BulletinHistory;
import org.martus.util.xml.XmlUtilities;

public class BulletinXmlExporter
{
	public BulletinXmlExporter(MiniLocalization localizationToUse)
	{
		localization = localizationToUse;
	}
	
	public void exportBulletins(Writer dest, Vector bulletins, boolean includePrivateData)
		throws IOException
	{
		dest.write(MartusXml.getTagStartWithNewline(BulletinXmlConstants.EXPORTED_BULLETINS));
		writeXMLVersion(dest);
		
		if(includePrivateData)
			writeElement(dest, "", BulletinXmlConstants.PUBLIC_AND_PRIVATE, "", "");
		else
			writeElement(dest, "", BulletinXmlConstants.PUBLIC_ONLY, "", "");
		dest.write(BulletinXmlConstants.NEW_LINE);

		for (int i = 0; i < bulletins.size(); i++)
		{
			Bulletin b = (Bulletin)bulletins.get(i);
			exportOneBulletin(b, dest, includePrivateData);
		}
		dest.write(MartusXml.getTagEnd(BulletinXmlConstants.EXPORTED_BULLETINS));
	}

	private static void writeXMLVersion(Writer dest) throws IOException
	{
		dest.write(MartusXml.getTagStart(BulletinXmlConstants.XML_EXPORT_VERSION));
		dest.write(BulletinXmlConstants.XML_EXPORT_VERSION_NUMBER);
		dest.write(MartusXml.getTagEnd(BulletinXmlConstants.XML_EXPORT_VERSION));
		dest.write("<!-- XML format Version 2: added Grid columns Labels-->");
		dest.write(BulletinXmlConstants.NEW_LINE);
		dest.write("<!-- XML format Version 3: added Dropdowns and Messages-->");
		dest.write(BulletinXmlConstants.NEW_LINE);
		dest.write("<!-- XML format Version 4: added Field Types-->");
		dest.write(BulletinXmlConstants.NEW_LINE);
		dest.write("<!-- XML format Version 5: added Grid FieldSpec Types-->");
		dest.write(BulletinXmlConstants.NEW_LINE);
		dest.write("<!-- XML format Version 6: Daterange grid cells now exported as yyyy-mm-dd,yyyy-mm-dd-->");
		dest.write(BulletinXmlConstants.NEW_LINE);
		
		dest.write(BulletinXmlConstants.NEW_LINE);
	}

	void exportOneBulletin(Bulletin b, Writer dest, boolean includePrivateData) throws IOException
	{
		dest.write(MartusXml.getTagStartWithNewline(BulletinXmlConstants.BULLETIN));

		writeElement(dest, "", BulletinXmlConstants.LOCAL_ID, "", b.getLocalId());
		writeElement(dest, "", BulletinXmlConstants.ACCOUNT_ID, "", b.getAccount());
		if(b.isAllPrivate())
			writeElement(dest, "", BulletinXmlConstants.ALL_PRIVATE, "", "");
		
		BulletinHistory history = b.getHistory();
		if(history.size() > 0)
		{
			dest.write(MartusXml.getTagStartWithNewline(BulletinXmlConstants.HISTORY));
			for(int i=0; i < history.size(); ++i)
			{
				dest.write(MartusXml.getTagStart(BulletinXmlConstants.ANCESTOR));
				dest.write(history.get(i));
				dest.write(MartusXml.getTagEnd(BulletinXmlConstants.ANCESTOR));
			}
			dest.write(MartusXml.getTagEnd(BulletinXmlConstants.HISTORY));
		}

		if(includePrivateData || !b.isAllPrivate())
		{
			dest.write(MartusXml.getTagStart(BulletinXmlConstants.PUBLIC_DATA));
			writeFields(dest, b, b.getTopSectionFieldSpecs());
			writeAttachments(dest, b.getPublicAttachments(), BulletinXmlConstants.TOP_SECTION_ATTACHMENT_LIST);
			dest.write(MartusXml.getTagEnd(BulletinXmlConstants.PUBLIC_DATA));
		}

		if(includePrivateData)
		{
			dest.write(MartusXml.getTagStart(BulletinXmlConstants.PRIVATE_DATA));
			writeFields(dest, b, b.getBottomSectionFieldSpecs());
			writeAttachments(dest, b.getPrivateAttachments(), BulletinXmlConstants.BOTTOM_SECTION_ATTACHMENT_LIST);
			dest.write(MartusXml.getTagEnd(BulletinXmlConstants.PRIVATE_DATA));
		}

		dest.write(MartusXml.getTagEnd(BulletinXmlConstants.BULLETIN));
		dest.write(BulletinXmlConstants.NEW_LINE);
	}

	static void writeAttachments(Writer dest, AttachmentProxy[] attachments, String attachmentSectionTag)
		throws IOException
	{
		if(attachments.length == 0)
			return;

		dest.write(MartusXml.getTagStart(attachmentSectionTag));
		for (int i = 0; i < attachments.length; i++)
		{
			AttachmentProxy proxy = attachments[i];
			writeElement(dest, "", BulletinXmlConstants.ATTACHMENT, "", proxy.getLabel());
		}
		dest.write(MartusXml.getTagEnd(attachmentSectionTag));
	}

	void writeFields(Writer dest, Bulletin b, FieldSpec[] specs)
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
				GridFieldSpec grid = (GridFieldSpec)spec;
				String columnLabels = grid.getDetailsXml();
				final String typeTagAndData = getXmlEncodedTagWithData(BulletinXmlConstants.TYPE, FieldSpec.getTypeString(spec.getType()));
				final String tagTagAndData = getXmlEncodedTagWithData(BulletinXmlConstants.TAG, tag);
				final String labelTagAndData = getXmlEncodedTagWithData(BulletinXmlConstants.LABEL, spec.getLabel());
				final String valueTagAndData = MartusXml.getTagWithData(BulletinXmlConstants.VALUE, value + columnLabels);
				writeElementDirect(dest, typeTagAndData, tagTagAndData, labelTagAndData, valueTagAndData);
			}
			else
			{
				writeElement(dest,FieldSpec.getTypeString(spec.getType()), tag, spec.getLabel(), value);
			}
		}
		
	}
	
	private static String getXmlEncodedTagWithData(String tagName, String data)
	{
		return MartusXml.getTagWithData(tagName, XmlUtilities.getXmlEncoded(data));
	}

	static void writeElement(Writer dest, String fieldType, String tag, String rawLabel, String rawFieldData) throws IOException
	{	
		String xmlFieldTypeAndValue = "";
		String xmlTagAndValue = "";
		String xmlLabelAndValue = "";
		String xmlValueAndFieldData = "";
		
		if(fieldType.length() > 0)
			xmlFieldTypeAndValue = getXmlEncodedTagWithData(BulletinXmlConstants.TYPE, fieldType);

		xmlTagAndValue = getXmlEncodedTagWithData(BulletinXmlConstants.TAG, tag);

		if (rawLabel.length() > 0)
			xmlLabelAndValue = getXmlEncodedTagWithData(BulletinXmlConstants.LABEL, rawLabel);
		
		if (rawFieldData.length() > 0)
			xmlValueAndFieldData = getXmlEncodedTagWithData(BulletinXmlConstants.VALUE, rawFieldData);
		writeElementDirect(dest, xmlFieldTypeAndValue, xmlTagAndValue, xmlLabelAndValue, xmlValueAndFieldData);		
	}	
	
	static void writeElementDirect(Writer dest, String xmlEncodeType, String xmlEncodedTag, String xmlEncodedLabel, String xmlEncodedFieldData) throws IOException
	{						
		dest.write(MartusXml.getTagStartWithNewline("Field"));
		dest.write(xmlEncodeType);
		dest.write(xmlEncodedTag);
		dest.write(xmlEncodedLabel);
		dest.write(xmlEncodedFieldData);
		dest.write(MartusXml.getTagEnd(MartusXml.tagField));		
	}
	
	
	
	MiniLocalization localization;
}
