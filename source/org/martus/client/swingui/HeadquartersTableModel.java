/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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

import org.martus.client.core.MartusApp;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MiniLocalization;

public abstract class HeadquartersTableModel extends ExternalPublicKeysTableModel 
{
	public HeadquartersTableModel(MartusApp app)
	{
		localization = app.getLocalization();
		entries = new Vector();
		fontHelper = new UiFontEncodingHelper(app.getConfigInfo().getUseZawgyi());
	}
	
	public void setHQSelectionListener(HeadquartersSelectionListener selectionListenerToUse)
	{
		selectionListener = selectionListenerToUse;
	}
	
	public void addNewHeadQuarterEntry(HeadquarterEntry entryToAdd)
	{
		entries.add(entryToAdd);
		int rowAdded = entries.size();
		fireTableRowsInserted(rowAdded, rowAdded);
	}
	
	public void addKeys(HeadquartersKeys keys)
	{
		for(int j = 0; j < keys.size(); ++j)
		{
			HeadquartersKey hqKeyToCheck = keys.get(j);
			HeadquarterEntry headQuarterEntry = new HeadquarterEntry(hqKeyToCheck);
			if(!contains(headQuarterEntry))
				addNewHeadQuarterEntry(headQuarterEntry);
		}
	}

	public void selectKeys(HeadquartersKeys keys)
	{
		for(int row = 0; row < getRowCount(); ++row)
		{
			if(keys.contains(getHQKey(row)))
				selectRow(row);
		}
	}
	
	public void selectRow(int row)
	{
		HeadquarterEntry entry = (HeadquarterEntry)entries.get(row);
		entry.setSelected(true);
	}
	
	public void removeRow(int row)
	{
		entries.remove(row);
		fireTableRowsDeleted(row, row);
	}

	public int getNumberOfSelectedHQs()
	{
		return getAllSelectedHeadQuarterKeys().size();
	}

	public HeadquartersKeys getAllSelectedHeadQuarterKeys()
	{
		HeadquartersKeys keys = new HeadquartersKeys();
		for (Iterator iter = entries.iterator(); iter.hasNext();) 
		{
			HeadquarterEntry hqEntry = (HeadquarterEntry) iter.next();
			if(hqEntry.isSelected())
				keys.add(hqEntry.getKey());
		}
		return keys;
	}
	
	public HeadquartersKeys getAllKeys()
	{
		HeadquartersKeys keys = new HeadquartersKeys();
		for (Iterator iter = entries.iterator(); iter.hasNext();) 
		{
			keys.add(((HeadquarterEntry) iter.next()).getKey());
		}	
		return keys;
	}
	
	public HeadquartersKey getHQKey(int row)
	{
		return ((HeadquarterEntry)entries.get(row)).getKey();
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
		HeadquarterEntry entry = (HeadquarterEntry)entries.get(row);
		if(column == COLUMN_DEFAULT || column == COLUMN_SELECTED)
			return new Boolean(entry.isSelected());
		if(column == COLUMN_LABEL)
			return  fontHelper.getDisplayable(entry.getLabel());
		if(column == COLUMN_PUBLIC_CODE)
			return entry.getPublicCode();
		return "";
	}
	
	public String getLabel(int row)
	{
		HeadquarterEntry entry = (HeadquarterEntry)entries.get(row);
		return fontHelper.getDisplayable(entry.getLabel());
	}

	public String getPublicCode(int row)
	{
		HeadquarterEntry entry = (HeadquarterEntry)entries.get(row);
		return entry.getPublicCode();
	}

	public void setLabel(int row, String newLabel)
	{
		HeadquarterEntry entry = (HeadquarterEntry)entries.get(row);
		entry.setLabel(newLabel);
	}
	
	public void setValueAt(Object value, int row, int column)
	{
		HeadquarterEntry entry = (HeadquarterEntry)entries.get(row);
		if(column == COLUMN_SELECTED || column == COLUMN_DEFAULT)
		{
			entry.setSelected(((Boolean)value).booleanValue());
			if(selectionListener != null)
				selectionListener.selectedHQsChanged(getNumberOfSelectedHQs());
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
	
	public boolean contains(HeadquarterEntry entry)
	{
		return entries.contains(entry);
	}
	
	Vector entries;
	public int COLUMN_DEFAULT = -1;
	public int COLUMN_SELECTED = -1;
	public int COLUMN_PUBLIC_CODE = -1;
	public int COLUMN_LABEL = -1;
	public int columnCount;
	MiniLocalization localization;
	HeadquartersSelectionListener selectionListener;
	UiFontEncodingHelper fontHelper;
}
