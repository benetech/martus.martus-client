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

package org.martus.client.swingui;

import java.util.Iterator;
import java.util.Vector;


import org.martus.client.swingui.tablemodels.UiTableModel;
import org.martus.common.HQKey;
import org.martus.common.HQKeys;
import org.martus.common.clientside.UiBasicLocalization;

public abstract class HeadQuartersTableModel extends UiTableModel 
{
	public HeadQuartersTableModel(UiBasicLocalization localizationToUse)
	{
		localization = localizationToUse;
		entries = new Vector();
	}
	
	public void setHQSelectionListener(HeadQuartersSelectionListener selectionListenerToUse)
	{
		selectionListener = selectionListenerToUse;
	}
	
	public void addNewHeadQuarterEntry(HeadQuarterEntry entryToAdd)
	{
		entries.add(entryToAdd);
		int rowAdded = entries.size();
		fireTableRowsInserted(rowAdded, rowAdded);
	}
	
	public void removeRow(int row)
	{
		entries.remove(row);
		fireTableRowsDeleted(row, row);
	}

	public HQKeys getAllSelectedHeadQuarterKeys()
	{
		HQKeys keys = new HQKeys();
		for (Iterator iter = entries.iterator(); iter.hasNext();) 
		{
			HeadQuarterEntry hqEntry = (HeadQuarterEntry) iter.next();
			if(hqEntry.isSelected())
				keys.add(hqEntry.getKey());
		}
		return keys;
	}
	
	public HQKeys getAllDefaultHeadQuarterKeys()
	{
		HQKeys keys = new HQKeys();
		for (Iterator iter = entries.iterator(); iter.hasNext();) 
		{
			HeadQuarterEntry hqEntry = (HeadQuarterEntry) iter.next();
			if(hqEntry.isDefault())
				keys.add(hqEntry.getKey());
		}
		return keys;
	}
	
	public HQKeys getAllKeys()
	{
		HQKeys keys = new HQKeys();
		for (Iterator iter = entries.iterator(); iter.hasNext();) 
		{
			keys.add(((HeadQuarterEntry) iter.next()).getKey());
		}	
		return keys;
	}
	
	public HQKey getHQKey(int row)
	{
		return ((HeadQuarterEntry)entries.get(row)).getKey();
	}

	public int getNumberOfSelectedHQs()
	{
		int numberOfSelectedHQs = 0;
		for (Iterator iter = entries.iterator(); iter.hasNext();) 
		{
			HeadQuarterEntry hqEntry = (HeadQuarterEntry) iter.next();
			if(hqEntry.isSelected())
				++numberOfSelectedHQs;
		}
		return numberOfSelectedHQs;
	}
	
	public int getRowCount() 
	{
		return entries.size();
	}

	public int getColumnCount()
	{
		return columnCount;
	}
	
	public String getColumnName(int column)
	{
		if(column == COLUMN_DEFAULT)
			return localization.getFieldLabel("ConfigureHeadQuartersDefault");
		if(column == COLUMN_PUBLIC_CODE)
			return localization.getFieldLabel("ConfigureHQColumnHeaderPublicCode");
		if(column == COLUMN_SELECTED)
			return localization.getFieldLabel("HeadQuartersSelected");
		if(column == COLUMN_LABEL)
			return localization.getFieldLabel("BulletinHeadQuartersHQLabel");
		return "";
	}

	public Object getValueAt(int row, int column)
	{
		HeadQuarterEntry entry = (HeadQuarterEntry)entries.get(row);
		if(column == COLUMN_DEFAULT)
			return new Boolean(entry.isDefault());
		if(column == COLUMN_SELECTED)
			return new Boolean(entry.isSelected());
		if(column == COLUMN_LABEL)
			return entry.getLabel();
		if(column == COLUMN_PUBLIC_CODE)
			return entry.getPublicCode();
		return "";
	}

	public void setValueAt(Object value, int row, int column)
	{
		HeadQuarterEntry entry = (HeadQuarterEntry)entries.get(row);
		if(column == COLUMN_SELECTED)
		{
			entry.setSelected(((Boolean)value).booleanValue());
			if(selectionListener != null)
				selectionListener.selectedHQsChanged(getNumberOfSelectedHQs());
		}
		else if(column == COLUMN_DEFAULT)
		{
			entry.setDefault(((Boolean)value).booleanValue());
		}
		else if(column == COLUMN_LABEL)
		{
			entry.setLabel((String)value);
		}
	}
	
	public Class getColumnClass(int column)
	{
		if(column == COLUMN_SELECTED || column == COLUMN_DEFAULT)
			return Boolean.class;
		if(column == COLUMN_LABEL || column == COLUMN_PUBLIC_CODE)
			return String.class;
		return null;
	}

	public boolean isEnabled(int row) 
	{
		return true;
	}
	
	public boolean isCellEditable(int row, int column)
	{
		if(column == COLUMN_SELECTED || column == COLUMN_DEFAULT)
			return true;
		return false;
	}
	
	Vector entries;
	public int COLUMN_DEFAULT = -1;
	public int COLUMN_SELECTED = -1;
	public int COLUMN_PUBLIC_CODE = -1;
	public int COLUMN_LABEL = -1;
	public int columnCount;
	UiBasicLocalization localization;
	HeadQuartersSelectionListener selectionListener;
}
