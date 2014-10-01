/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
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
package org.martus.client.swingui.jfx.landing.bulletins;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

import org.martus.client.swingui.tablemodels.AttachmentTableModel;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.database.ReadableDatabase;

public class AttachmentTableRowData
{
	public AttachmentTableRowData(AttachmentProxy attachmentToUse, ReadableDatabase database)
	{
		name = new SimpleStringProperty(attachmentToUse.getLabel());
		size =  new SimpleStringProperty(getSizeOfAttachment(attachmentToUse, database));
		remove = new SimpleBooleanProperty(true);
	}
	
	private String getSizeOfAttachment(AttachmentProxy attachmentToUse, ReadableDatabase database)
	{
		return AttachmentTableModel.getSize(attachmentToUse, database);
	}

	public SimpleStringProperty nameProperty()
	{
		return name;
	}
	
	public SimpleStringProperty sizeProperty()
	{
		return size;
	}
	
	public Property<Boolean> removeProperty() 
    { 
        return remove; 
    }
	
	static public final String ATTACHMENT_NAME_PROPERTY_NAME = "name";
	static public final String ATTACHMENT_SIZE_PROPERTY_NAME = "size";
	static public final String ATTACHMENT_REMOVE_PROPERTY_NAME = "remove";
	private SimpleStringProperty name;
	private SimpleStringProperty size;
	private SimpleBooleanProperty remove;
 }
