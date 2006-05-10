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
package org.martus.client.core;

public class BulletinXmlConstants 
{
	public final static String ExportedBulletinsElementName = "ExportedMartusBulletins";
	public final static String VersionXMLElementName = "MartusBulletinExportFormatVersion";
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
	public final static String ATTACHMENT_TAG = "Attachment";
	public final static String FILENAME_TAG = "Filename";
	public final static String TYPE = "Type";
	public final static String TAG = "Tag";
	public final static String VALUE = "Value";
	public final static String LABEL = "Label";
	public final static String FIELD = "Field";
	public final static String NEW_LINE = "\n";

	public final static String VersionNumber = "6";

}
