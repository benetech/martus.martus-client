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
package org.martus.client.swingui.jfx.landing.general;

import java.util.Iterator;
import java.util.Set;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.UiMainWindowInterface;
import org.martus.client.swingui.jfx.generic.data.ArrayObservableList;
import org.martus.common.MartusUtilities;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;

public class SyncRecordsTableProvider extends ArrayObservableList<ServerSyncTableRowData>
{
	public SyncRecordsTableProvider(UiMainWindowInterface mainWindowToUse)
	{
		super(INITIAL_CAPACITY);
		mainWindow = mainWindowToUse;
	}
	
	public void addLocalBulletin(Set localUids) throws Exception
	{
		clear();
		for(Iterator iter = localUids.iterator(); iter.hasNext();)
		{
			UniversalId leafBulletinUid = (UniversalId) iter.next();
			ServerSyncTableRowData bulletinData = getCurrentBulletinData(leafBulletinUid);
			add(bulletinData);		
		}
	}

	protected ServerSyncTableRowData getCurrentBulletinData(UniversalId leafBulletinUid) throws Exception
	{
		ClientBulletinStore clientBulletinStore = mainWindow.getStore();
		Bulletin bulletin = clientBulletinStore.getBulletinRevision(leafBulletinUid);
		MiniLocalization localization = mainWindow.getLocalization();
		int bulletinSize = MartusUtilities.getBulletinSize(clientBulletinStore.getDatabase(), bulletin.getBulletinHeaderPacket());
		Integer size = new Integer(bulletinSize);
		int location = ServerSyncTableRowData.LOCATION_LOCAL;  //TODO compare with whats on server first.
		
		ServerSyncTableRowData bulletinData = new ServerSyncTableRowData(bulletin, size, location, localization);
		return bulletinData;
	}

	private UiMainWindowInterface mainWindow;
	private static final int INITIAL_CAPACITY = 100;
}
