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

import java.io.IOException;
import java.io.Writer;
import java.util.Vector;
import org.martus.common.FieldSpec;
import org.martus.common.GridFieldSpec;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusXml;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.clientside.DateUtilities;
import org.martus.common.packet.BulletinHistory;

public class BulletinXmlExporter
{
	public static void exportBulletins(Writer dest, Vector bulletins, boolean includePrivateData)
		throws IOException
	{
		dest.write(MartusXml.getTagStartWithNewline(ExportedBulletinsElementName));
		writeXMLVersion(dest);
		
		if(includePrivateData)
			writeElement(dest, PublicAndPrivateElementName, "", "");
		else
			writeElement(dest, PublicOnlyElementName, "", "");
		dest.write("\n");

		for (int i = 0; i < bulletins.size(); i++)
		{
			Bulletin b = (Bulletin)bulletins.get(i);
			exportOneBulletin(b, dest, includePrivateData);
		}
		dest.write(MartusXml.getTagEnd(ExportedBulletinsElementName));
	}

	private static void writeXMLVersion(Writer dest) throws IOException
	{
		dest.write(MartusXml.getTagStart(VersionXMLElementName));
		dest.write(VersionNumber);
		dest.write(MartusXml.getTagEnd(VersionXMLElementName));
	}

	static void exportOneBulletin(Bulletin b, Writer dest, boolean includePrivateData) throws IOException
	{
		dest.write(MartusXml.getTagStartWithNewline(BulletinElementName));

		writeElement(dest, LocalIdElementName, "", b.getLocalId());
		writeElement(dest, AccountIdElementName, "", b.getAccount());
		if(b.isAllPrivate())
			writeElement(dest, AllPrivateElementName, "", "");
		
		BulletinHistory history = b.getHistory();
		if(history.size() > 0)
		{
			dest.write(MartusXml.getTagStartWithNewline(HistoryElementName));
			for(int i=0; i < history.size(); ++i)
			{
				dest.write(MartusXml.getTagStart(AncestorElementName));
				dest.write(history.get(i));
				dest.write(MartusXml.getTagEnd(AncestorElementName));
			}
			dest.write(MartusXml.getTagEnd(HistoryElementName));
		}

		if(includePrivateData || !b.isAllPrivate())
		{
			dest.write(MartusXml.getTagStart(PublicDataElementName));
			writeFields(dest, b, b.getPublicFieldSpecs());
			writeAttachments(dest, b.getPublicAttachments());
			dest.write(MartusXml.getTagEnd(PublicDataElementName));
		}

		if(includePrivateData)
		{
			dest.write(MartusXml.getTagStart(PrivateDataElementName));
			writeFields(dest, b, b.getPrivateFieldSpecs());
			writeAttachments(dest, b.getPrivateAttachments());
			dest.write(MartusXml.getTagEnd(PrivateDataElementName));
		}

		dest.write(MartusXml.getTagEnd(BulletinElementName));
		dest.write("\n");
	}

	static void writeAttachments(Writer dest, AttachmentProxy[] publicAttachments)
		throws IOException
	{
		if(publicAttachments.length == 0)
			return;

		dest.write(MartusXml.getTagStart(AttachmentsListElementName));
		for (int i = 0; i < publicAttachments.length; i++)
		{
			AttachmentProxy proxy = publicAttachments[i];
			writeElement(dest, AttachmentElementName, "", proxy.getLabel());
		}
		dest.write(MartusXml.getTagEnd(AttachmentsListElementName));
	}

	static void writeFields(Writer dest, Bulletin b, FieldSpec[] specs)
		throws IOException
	{		
		for (int i = 0; i < specs.length; i++)
		{			
			FieldSpec spec = specs[i];
			if(spec.hasUnknownStuff())
				continue;						
			String tag = spec.getTag();
			StringBuffer rawFieldData = new StringBuffer(b.get(tag));
			if(spec.getType() == FieldSpec.TYPE_GRID)
			{
				GridFieldSpec grid = (GridFieldSpec)spec;
				String columnLabels = grid.getDetailsXml();
				rawFieldData.insert(0, columnLabels);			
			}
			
			if(spec.getType() == FieldSpec.TYPE_DATERANGE)
			{
				String martusFlexidate = rawFieldData.toString();
				String startDate = DateUtilities.getStartDateRange(martusFlexidate);
				String endDate = DateUtilities.getEndDateRange(martusFlexidate);
				rawFieldData = new StringBuffer(startDate);
				rawFieldData.append(",");
				rawFieldData.append(endDate);
			}
			writeElement(dest,tag, spec.getLabel(), rawFieldData.toString());				
		}
		
	}

	static void writeElement(Writer dest, String tag, String rawLabel, String rawFieldData) throws IOException
	{						
		dest.write(MartusXml.getTagStartWithNewline("Field"));	
		dest.write(MartusXml.getTagWithData(TAG, MartusUtilities.getXmlEncoded(tag)));
			
		if (rawLabel.length() > 0)
			dest.write(MartusXml.getTagWithData(LABEL, MartusUtilities.getXmlEncoded(rawLabel)));
		
		if (rawFieldData.length() > 0)
			dest.write(MartusXml.getTagWithData(VALUE, MartusUtilities.getXmlEncoded(rawFieldData)));
				
		dest.write(MartusXml.getTagEnd(MartusXml.tagField));		
	}	

	public final static String ExportedBulletinsElementName = "ExportedMartusBulletins";
	public final static String VersionXMLElementName = "XMLVersionNumber";
	public final static String PublicOnlyElementName = "PublicDataOnly";
	public final static String PublicAndPrivateElementName = "PublicAndPrivateData";
	public final static String BulletinElementName = "MartusBulletin";
	public final static String PublicDataElementName = "PublicData";
	public final static String PrivateDataElementName = "PrivateData";
	public final static String LocalIdElementName = "LocalId";
	public final static String AllPrivateElementName = "AllPrivate";
	public final static String AccountIdElementName = "AuthorAccountId";
	public final static String AttachmentsListElementName = "AttachmentList";
	public final static String AttachmentElementName = "Attachment";	
	public final static String HistoryElementName = "History";
	public final static String AncestorElementName = "Ancestor";
	
	private final static String TAG = "Tag";
	private final static String VALUE = "Value";
	private final static String LABEL = "Label";

	//Version 1 had no XML tag for XMLVersionNumber
	//Version 2 added Grid column headers
	private final static String VersionNumber = "2";
}
