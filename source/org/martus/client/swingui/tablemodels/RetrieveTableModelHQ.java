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

package org.martus.client.swingui.tablemodels;

import org.martus.client.core.BulletinSummary;
import org.martus.client.core.MartusApp;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.clientside.UiBasicLocalization;

public abstract class RetrieveTableModelHQ extends RetrieveTableModel {

	public RetrieveTableModelHQ(MartusApp appToUse, UiBasicLocalization localizationToUse)
	{
		super(appToUse, localizationToUse);
	}

	public String getColumnName(int column)
	{
		switch(column)
		{
			case COLUMN_RETRIEVE_FLAG:
				return getLocalization().getFieldLabel("retrieveflag");
			case COLUMN_TITLE:
				return getLocalization().getFieldLabel(Bulletin.TAGTITLE);
			case COLUMN_AUTHOR:
				return getLocalization().getFieldLabel(Bulletin.TAGAUTHOR);
			case COLUMN_DATE:
				return getLocalization().getFieldLabel("BulletinDateSaved");
			case COLUMN_SIZE:
				return getLocalization().getFieldLabel("BulletinSize");
			default:
				return "";
		}
	}

	public int getColumnCount()
	{
		return 5;
	}

	public Object getValueAt(int row, int column)
	{
		BulletinSummary summary = (BulletinSummary)currentSummaries.get(row);
		switch(column)
		{
			case COLUMN_RETRIEVE_FLAG:
				return new Boolean(summary.isChecked());
			case COLUMN_TITLE:
				return summary.getTitle();
			case COLUMN_AUTHOR:
				return summary.getAuthor();
			case COLUMN_DATE:
				return getLocalization().convertStoredDateTimeToDisplay(summary.getDateTimeSaved());
			case COLUMN_SIZE:
				return  getSizeInKbytes(summary.getSize());
			default:
				return "";
		}
	}


	public void setValueAt(Object value, int row, int column)
	{
		BulletinSummary summary = (BulletinSummary)currentSummaries.get(row);
		if(column == COLUMN_RETRIEVE_FLAG)
		{
			summary.setChecked(((Boolean)value).booleanValue());
		}
	}

	public Class getColumnClass(int column)
	{
		switch(column)
		{
			case COLUMN_RETRIEVE_FLAG:
				return Boolean.class;
			case COLUMN_TITLE:
				return String.class;
			case COLUMN_AUTHOR:
				return String.class;
			case COLUMN_DATE:
				return String.class;
			case COLUMN_SIZE:
				return Integer.class;
			default:
				return null;
		}
	}
	public static final int COLUMN_AUTHOR = 2;
	public static final int COLUMN_DATE = 3;
	public static final int COLUMN_SIZE = 4;
}
