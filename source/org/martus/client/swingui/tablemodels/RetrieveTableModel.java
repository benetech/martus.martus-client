/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2003, Beneficent
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

import java.util.Iterator;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.martus.client.core.BulletinStore;
import org.martus.client.core.BulletinSummary;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.dialogs.UiProgressRetrieveSummariesDlg;
import org.martus.common.MartusUtilities;
import org.martus.common.MartusUtilities.ServerErrorException;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.packet.UniversalId;

abstract public class RetrieveTableModel extends AbstractTableModel
{
	public RetrieveTableModel(MartusApp appToUse, UiLocalization localizationToUse)
	{
		app = appToUse;
		localization = localizationToUse;
		
		downloadableSummaries = new Vector();
		store = app.getStore();
		allSummaries = new Vector();
	}

	abstract public void initialize(UiProgressRetrieveSummariesDlg progressDlg) throws ServerErrorException;
	
	UiLocalization getLocalization()
	{
		return localization;
	}

	protected void setProgressDialog(UiProgressRetrieveSummariesDlg progressDlg)
	{
		retrieverDlg = progressDlg;
	}

	protected void setCurrentSummaries()
	{
		downloadableSummaries = getSummariesForBulletinsNotInStore(allSummaries);
		changeToDownloadableSummaries();
	}

	public void changeToDownloadableSummaries()
	{
		currentSummaries = downloadableSummaries;
	}

	public void changeToAllSummaries()
	{
		currentSummaries = allSummaries;
	}

	public Vector getSummariesForBulletinsNotInStore(Vector allSummaries)
	{
		Vector result = new Vector();
		Iterator iterator = allSummaries.iterator();
		while(iterator.hasNext())
		{
			BulletinSummary currentSummary = (BulletinSummary)iterator.next();
			UniversalId uid = UniversalId.createFromAccountAndLocalId(currentSummary.getAccountId(), currentSummary.getLocalId());
			if(store.findBulletinByUniversalId(uid) != null)
				continue;
			currentSummary.setDownloadable(true);
			result.add(currentSummary);
		}
		return result;
	}

	public void setAllFlags(boolean flagState)
	{
		for(int i = 0; i < currentSummaries.size(); ++i)
			((BulletinSummary)currentSummaries.get(i)).setChecked(flagState);
		fireTableDataChanged();
	}

	public boolean isDownloadable(int row)
	{
		return((BulletinSummary)currentSummaries.get(row)).isDownloadable();
	}

	public Vector getUniversalIdList()
	{
		Vector uidList = new Vector();

		for(int i = 0; i < currentSummaries.size(); ++i)
		{
			BulletinSummary summary = (BulletinSummary)currentSummaries.get(i);
			if(summary.isChecked())
			{
				UniversalId uid = UniversalId.createFromAccountAndLocalId(summary.getAccountId(), summary.getLocalId());
				uidList.add(uid);
			}
		}
		return uidList;

	}

	public int getRowCount()
	{
		return currentSummaries.size();
	}

	public boolean isCellEditable(int row, int column)
	{
		if(column == 0)
			return true;

		return false;
	}

	public void getMySummaries() throws ServerErrorException
	{
		Vector summaryStrings = app.getMyServerBulletinSummaries();
		createSummariesFromStrings(app.getAccountId(), summaryStrings);
	}

	public void getMyDraftSummaries() throws ServerErrorException
	{
		Vector summaryStrings = app.getMyDraftServerBulletinSummaries();
		createSummariesFromStrings(app.getAccountId(), summaryStrings);
	}

	public void getFieldOfficeSealedSummaries(String fieldOfficeAccountId) throws ServerErrorException
	{
		try
		{
			NetworkResponse response = app.getCurrentNetworkInterfaceGateway().getSealedBulletinIds(app.security, fieldOfficeAccountId, MartusUtilities.getRetrieveBulletinSummaryTags());
			if(response.getResultCode().equals(NetworkInterfaceConstants.OK))
			{
				createSummariesFromStrings(fieldOfficeAccountId, response.getResultVector());
				return;
			}
		}
		catch (MartusSignatureException e)
		{
			System.out.println("RetrieveTableModle.getFieldOfficeSealedSummaries: " + e);
		}
		throw new ServerErrorException();
	}

	public void getFieldOfficeDraftSummaries(String fieldOfficeAccountId) throws ServerErrorException
	{
		try
		{
			NetworkResponse response = app.getCurrentNetworkInterfaceGateway().getDraftBulletinIds(app.security, fieldOfficeAccountId, MartusUtilities.getRetrieveBulletinSummaryTags());
			if(response.getResultCode().equals(NetworkInterfaceConstants.OK))
			{
				createSummariesFromStrings(fieldOfficeAccountId, response.getResultVector());
				return;
			}
		}
		catch (MartusSignatureException e)
		{
			System.out.println("MartusApp.getFieldOfficeDraftSummaries: " + e);
		}
		throw new ServerErrorException();
	}

	public void createSummariesFromStrings(String accountId, Vector summaryStrings)
	{
		RetrieveThread worker = new RetrieveThread(accountId, summaryStrings);
		worker.start();

		if(retrieverDlg == null)
			waitForThreadToTerminate(worker);
		else
			retrieverDlg.beginRetrieve();
	}

	public void waitForThreadToTerminate(RetrieveThread worker)
	{
		try
		{
			worker.join();
		}
		catch (InterruptedException e)
		{
		}
	}

	class RetrieveThread extends Thread
	{
		public RetrieveThread(String account, Vector summarys)
		{
			accountId = account;
			summaryStrings = summarys;
		}

		public void run()
		{
			retrieveAllSummaries();
			finishedRetrieve();
		}

		public void retrieveAllSummaries()
		{
			Iterator iterator = summaryStrings.iterator();
			int count = 0;
			int maxCount = summaryStrings.size();
			while(iterator.hasNext())
			{
				String pair = (String)iterator.next();
				try
				{
					BulletinSummary bulletinSummary = app.retrieveSummaryFromString(accountId, pair);
					allSummaries.add(bulletinSummary);
				}
				catch (ServerErrorException e)
				{
					errorThrown = e;
				}

				if(retrieverDlg != null)
				{
					if(retrieverDlg.shouldExit())
						break;
					retrieverDlg.updateBulletinCountMeter(++count, maxCount);
				}
			}
		}

		public void finishedRetrieve()
		{
			if(retrieverDlg != null)
				retrieverDlg.finishedRetrieve();
		}

		private String accountId;
		private Vector summaryStrings;
	}

	public void checkIfErrorOccurred() throws ServerErrorException
	{
		if(errorThrown != null)
			throw (errorThrown);
	}

	public Vector getDownloadableSummaries()
	{
		return downloadableSummaries;
	}

	public Vector getAllSummaries()
	{
		return allSummaries;
	}

	public BulletinSummary getBulletinSummary(int row)
	{
		return (BulletinSummary)currentSummaries.get(row);
	}


	Object getSizeInKbytes(int sizeKb)
	{
		sizeKb /= 1000;
		Integer sizeInK = new Integer(sizeKb);
		return sizeInK;
	}

	MartusApp app;
	UiLocalization localization;
	
	BulletinStore store;
	UiProgressRetrieveSummariesDlg retrieverDlg;
	protected Vector currentSummaries;
	private Vector downloadableSummaries;
	Vector allSummaries;
	ServerErrorException errorThrown;
}
