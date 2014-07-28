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

import java.util.Iterator;
import java.util.Set;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.bulletinstore.FolderContentsListener;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.jfx.generic.data.ArrayObservableList;
import org.martus.client.swingui.jfx.landing.FolderSelectionListener;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;

public class BulletinListProvider extends ArrayObservableList<BulletinTableRowData> implements FolderSelectionListener, FolderContentsListener
{
	public BulletinListProvider(MartusApp mainApp)
	{
		super(INITIAL_CAPACITY);
		app = mainApp;
		setFolder(null);
	}
	
	@Override
	public void folderWasSelected(BulletinFolder newFolder)
	{
		setFolder(newFolder);
	}

	public void setFolder(BulletinFolder newFolder)
	{
		if(folder != null)
			folder.removeFolderContentsListener(this);
		folder = newFolder;
		if(folder != null)
			folder.addFolderContentsListener(this);
		updateContents();
	}

	private void updateContents()
	{
		loadBulletinData(getUniversalIds());
	}

	private Set getUniversalIds()
	{
		if(folder == null)
			return app.getStore().getAllBulletinLeafUids();
		return folder.getAllUniversalIdsUnsorted();
	}

	@Override
	public void folderWasRenamed(String newName)
	{
	}

	@Override
	public void bulletinWasAdded(UniversalId added)
	{
		updateContents();
	}

	@Override
	public void bulletinWasRemoved(UniversalId removed)
	{
		updateContents();
	}

	@Override
	public void folderWasSorted()
	{
		updateContents();
	}

	protected void loadAllBulletins()
	{
		setFolder(null);
	}

	public void loadBulletinData(Set bulletinUids)
	{
		// FIXME: To avoid the bulletin list flickering, 
		// we should just add or remove as needed, instead of 
		// clearing and re-populating from scratch
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

	protected int findBulletinIndexInTable(UniversalId uid)
	{
		for (int currentIndex = 0; currentIndex < size(); currentIndex++)
		{
			if(uid.equals(get(currentIndex).getUniversalId()))
				return currentIndex;
		}
		return BULLETIN_NOT_IN_TABLE;
	}

	public boolean updateBulletin(Bulletin bulletin)
	{
		boolean shouldReSortTable = false;
		UniversalId bulletinId = bulletin.getUniversalId();
		BulletinTableRowData updatedBulletinData = getCurrentBulletinData(bulletinId);
		int bulletinIndexInTable = findBulletinIndexInTable(bulletinId);
		if(bulletinIndexInTable <= BULLETIN_NOT_IN_TABLE)
		{
			loadAllBulletins();
			shouldReSortTable = true;
		}
		else
		{
			set(bulletinIndexInTable, updatedBulletinData);
			shouldReSortTable = false;
		}
		return shouldReSortTable;
	}

	private static final int BULLETIN_NOT_IN_TABLE = -1;
	private static final int INITIAL_CAPACITY = 1000;
	
	private MartusApp app;
	private BulletinFolder folder;
}
