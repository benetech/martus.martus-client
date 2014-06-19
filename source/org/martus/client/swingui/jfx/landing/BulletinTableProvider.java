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
package org.martus.client.swingui.jfx.landing;

import java.util.ArrayList;

import javafx.collections.ModifiableObservableListBase;


public class BulletinTableProvider extends ModifiableObservableListBase<BulletinTableRowData>
{

	public BulletinTableProvider()
	{
		data = new ArrayList<BulletinTableRowData>(INITIAL_NUMBER_OF_ELEMENTS);
	}
	
	@Override
	protected void doAdd(int index, BulletinTableRowData element)
	{
		data.add(index, element);
	}

	@Override
	protected BulletinTableRowData doRemove(int index)
	{
		return (BulletinTableRowData) data.remove(index);
	}

	@Override
	protected BulletinTableRowData doSet(int index, BulletinTableRowData element)
	{
		return (BulletinTableRowData) data.set(index, element);
	}

	@Override
	public BulletinTableRowData get(int index)
	{
		return (BulletinTableRowData) data.get(index);
	}

	@Override
	public int size()
	{
		return data.size();
	}
	
	final int INITIAL_NUMBER_OF_ELEMENTS = 1000;
	ArrayList data;
}
