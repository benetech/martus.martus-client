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
import java.util.Vector;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.UiMainWindowInterface;
import org.martus.client.swingui.jfx.generic.data.ArrayObservableList;
import org.martus.common.BulletinSummary;
import org.martus.common.MartusUtilities;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;

public class SyncRecordsTableProvider extends ArrayObservableList<ServerSyncTableRowData>
{
	public SyncRecordsTableProvider(UiMainWindowInterface mainWindowToUse)
	{
		super(INITIAL_CAPACITY);
		mainWindow = mainWindowToUse;
		allRows = new ArrayObservableList(INITIAL_CAPACITY);
	}
	
	public void show(int location)
	{
		clear();
		if(location != ServerSyncTableRowData.LOCATION_ANY)
		{
			for (Iterator iterator = allRows.iterator(); iterator.hasNext();)
			{
				ServerSyncTableRowData rowData = (ServerSyncTableRowData) iterator.next();
				if(rowData.getRawLocation() == location)
					add(rowData);
			}
			return;
		}
		addAll(allRows);
	}
		
	public void addBulletinsAndSummaries(Set localUidsToUse, Vector myDraftSummaries, Vector mySealedSummaries, Vector hqDraftSummaries, Vector hqSealedSummaries) throws Exception
	{
		localUids = localUidsToUse;
		addAllServerSummariesMutable(myDraftSummaries);
		addAllServerSummariesImmutable(mySealedSummaries);
		addAllServerSummariesImmutable(hqDraftSummaries);
		addAllServerSummariesImmutable(hqSealedSummaries);
		addLocalBulletions();
	}

	private void addAllServerSummariesMutable(Vector summaries) throws Exception
	{
		addAllServerSummaries(summaries, true);
	}

	private void addAllServerSummariesImmutable(Vector summaries) throws Exception
	{
		addAllServerSummaries(summaries, false);
	}
	
	private void addAllServerSummaries(Vector summaries, boolean mutable) throws Exception
	{
		for (Iterator iterator = summaries.iterator(); iterator.hasNext();)
		{
			BulletinSummary summary = (BulletinSummary) iterator.next();
			ServerSyncTableRowData bulletinData = new ServerSyncTableRowData(summary, mutable, mainWindow.getApp());
			addServerRecord(bulletinData);	
		}
	}
	
	private void addServerRecord(ServerSyncTableRowData bulletinData)
	{
		UniversalId serverUid = bulletinData.getUniversalId();
		if(localUids.contains(serverUid))
		{
			localUids.remove(serverUid);
			bulletinData.setLocation(mainWindow.getApp(), ServerSyncTableRowData.LOCATION_BOTH);
		}
		if(!isInTrash(serverUid))
			allRows.add(bulletinData);
	}
	
	private void addLocalBulletions() throws Exception
	{
		for(Iterator iter = localUids.iterator(); iter.hasNext();)
		{
			UniversalId leafBulletinUid = (UniversalId) iter.next();
			if(isInTrash(leafBulletinUid))
				continue;
			ServerSyncTableRowData bulletinData = getLocalBulletinData(leafBulletinUid);
			allRows.add(bulletinData);		
		}
	}

	private boolean isInTrash(UniversalId uid)
	{
		BulletinFolder discardedFolder = mainWindow.getApp().getFolderDiscarded();
		return discardedFolder.contains(uid);
	}

	protected ServerSyncTableRowData getLocalBulletinData(UniversalId leafBulletinUid) throws Exception
	{
		ClientBulletinStore clientBulletinStore = mainWindow.getStore();
		Bulletin bulletin = clientBulletinStore.getBulletinRevision(leafBulletinUid);
		int bulletinSizeBytes = MartusUtilities.getBulletinSize(clientBulletinStore.getDatabase(), bulletin.getBulletinHeaderPacket());
		int location = ServerSyncTableRowData.LOCATION_LOCAL;  //TODO compare with whats on server first.
		ServerSyncTableRowData bulletinData = new ServerSyncTableRowData(bulletin, bulletinSizeBytes, location, mainWindow.getApp());
		return bulletinData;
	}

	private UiMainWindowInterface mainWindow;
	private static final int INITIAL_CAPACITY = 500;
	private ArrayObservableList<ServerSyncTableRowData> allRows;
	private Set localUids;
}
