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

package org.martus.client.swingui.bulletintable;

import javax.swing.table.AbstractTableModel;

import org.martus.client.core.BulletinFolder;
import org.martus.client.core.ClientBulletinStore;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.common.packet.UniversalId;

public class BulletinTableModel extends AbstractTableModel
{
    public BulletinTableModel(UiBasicLocalization localizationToUse)
    {
		localization = localizationToUse;
    }

	public void setFolder(BulletinFolder folderToUse)
	{
		if(folder != null)
		{
			fireTableRowsDeleted(0, folder.getBulletinCount());
		}

		folder = folderToUse;
		fireTableRowsInserted(0, folder.getBulletinCount());
	}

	public BulletinFolder getFolder()
	{
		return folder;
	}

	public int getRowCount()
	{
		if(folder == null)
			return 0;

		return folder.getBulletinCount();
	}

	public int getColumnCount()
	{
		return sortableFieldTags.length;
	}

	public Bulletin getBulletin(int rowIndex)
	{
		return folder.getBulletinSorted(rowIndex);
	}

	public UniversalId getBulletinUid(int rowIndex)
	{
		return folder.getBulletinUniversalIdSorted(rowIndex);
	}

	public int findBulletin(UniversalId uid)
	{
		if(uid == null)
			return -1;

		return folder.find(uid);
	}

	public Object getValueAt(int rowIndex, int columnIndex)
	{
		UniversalId uid = folder.getBulletinUniversalIdSorted(rowIndex);
		if(uid == null)
			return "";
		
		Bulletin b = getBulletin(rowIndex);
		String fieldTag = sortableFieldTags[columnIndex];
		
		if(fieldTag.equals(Bulletin.TAGSTATUS))
		 	return localization.getStatusLabel(b.getStatus());	
		
		if(fieldTag.equals(Bulletin.TAGWASSENT))
		{
			String tag = getSentTag(b);
			if(tag == null)
				return "";
			return localization.getFieldLabel(tag);
		}
											
		if (fieldTag.equals(Bulletin.TAGLASTSAVED))			 
			return localization.convertStoredDateTimeToDisplay(b.getLastSavedDateTime());
		
		String value = getFolder().getStore().getFieldData(uid, fieldTag);
		if (fieldTag.equals(Bulletin.TAGENTRYDATE) || 
			fieldTag.equals(Bulletin.TAGEVENTDATE))				
		{
			return localization.convertStoredDateToDisplay(value);
		}	
		
		if (value.startsWith("<"))
			return " "+value;				
			
		return value;
	}

	private String getSentTag(Bulletin b)
	{
		ClientBulletinStore store = folder.getStore();
		boolean knownNotOnServer = store.isProbablyNotOnServer(b);

		if(store.getFolderDraftOutbox().contains(b))
		{
			if(store.isMyBulletin(b.getBulletinHeaderPacket()))
				return "WasSentNo";
			if(!knownNotOnServer)
				return null;
		}

		if(knownNotOnServer)
			return "WasSentNo";

		if(store.isProbablyOnServer(b))
			return "WasSentYes";
		
		return null;
	}

	public String getColumnName(int columnIndex)
	{
		return localization.getFieldLabel(getFieldName(columnIndex));
	}

	public String getFieldName(int columnIndex)
	{
		return sortableFieldTags[columnIndex];
	}

	public void sortByColumn(int columnIndex)
	{
		folder.sortBy(getFieldName(columnIndex));
	}
	
	private static final String[] sortableFieldTags =
	{
		BulletinConstants.TAGSTATUS, 
		BulletinConstants.TAGWASSENT, 
		BulletinConstants.TAGEVENTDATE, 
		BulletinConstants.TAGTITLE, 
		BulletinConstants.TAGAUTHOR, 
		BulletinConstants.TAGLASTSAVED
	};


	UiBasicLocalization localization;
	BulletinFolder folder;
}
