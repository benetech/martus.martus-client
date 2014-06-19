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
import java.util.Iterator;
import java.util.Set;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.MartusApp;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;

import javafx.collections.ModifiableObservableListBase;


public class BulletinTableProvider extends ModifiableObservableListBase<BulletinTableRowData>
{

	public BulletinTableProvider(MartusApp mainApp)
	{
		app = mainApp;
		data = new ArrayList<BulletinTableRowData>(INITIAL_CAPACITY);
	}
	
	public void loadBulletinData(Set bulletinUids)
	{
		clear();
		for(Iterator iter = bulletinUids.iterator(); iter.hasNext();)
		{
			UniversalId leafBulletinUid = (UniversalId) iter.next();
			BulletinTableRowData bulletinData = getCurrentBulletinData(leafBulletinUid);
			add(bulletinData);		
		}
	}

	protected BulletinTableRowData getCurrentBulletinData(UniversalId leafBulletinUid)
	{
		ClientBulletinStore clientBulletinStore = app.getStore();
		Bulletin bulletin = clientBulletinStore.getBulletinRevision(leafBulletinUid);
		boolean onServer = clientBulletinStore.isProbablyOnServer(leafBulletinUid);
		MiniLocalization localization = app.getLocalization();
		BulletinTableRowData bulletinData = new BulletinTableRowData(bulletin, onServer, localization);
		return bulletinData;
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
	
	final int INITIAL_CAPACITY = 1000;
	private ArrayList data;
	private MartusApp app;
}
