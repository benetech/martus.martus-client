/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2005, Beneficent
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

import org.martus.client.core.ClientBulletinStore;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.dialogs.UiProgressRetrieveSummariesDlg;
import org.martus.common.BulletinSummary;
import org.martus.common.MartusUtilities.ServerErrorException;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.clientside.ClientSideNetworkGateway;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.database.DatabaseKey;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.UniversalId;

abstract public class RetrieveTableModel extends AbstractTableModel
{
	public RetrieveTableModel(MartusApp appToUse, UiBasicLocalization localizationToUse)
	{
		app = appToUse;
		localization = localizationToUse;
		
		downloadableSummaries = new Vector();
		store = app.getStore();
		allSummaries = new Vector();
	}

	public void initialize(UiProgressRetrieveSummariesDlg progressDlg) throws ServerErrorException
	{
		setProgressDialog(progressDlg);
		populateAllSummariesList();
		buildDownloadableSummariesList();
		changeToDownloadableSummaries();
	}
	
	abstract protected void populateAllSummariesList() throws ServerErrorException;
	
	public MartusApp getApp()
	{
		return app;
	}
	
	public int getColumnCount()
	{
		return columnCount;
	}
	
	public String getColumnName(int column)
	{
		if(column == COLUMN_RETRIEVE_FLAG)
			return getLocalization().getFieldLabel("retrieveflag");
		if(column == COLUMN_TITLE)
			return getLocalization().getFieldLabel(Bulletin.TAGTITLE);
		if(column == COLUMN_AUTHOR)
			return getLocalization().getFieldLabel(Bulletin.TAGAUTHOR);
		if(column == COLUMN_LAST_DATE_SAVED)
			return getLocalization().getFieldLabel(Bulletin.TAGLASTSAVED);
		if(column == COLUMN_BULLETIN_SIZE)
			return getLocalization().getFieldLabel("BulletinSize");
		if(column == COLUMN_DELETE_FLAG)
			return getLocalization().getFieldLabel("DeleteFlag");
		if(column == COLUMN_VERSION_NUMBER)
			return getLocalization().getFieldLabel("BulletinVersionNumber");
		return "";
	}

	public Object getValueAt(int row, int column)
	{
		BulletinSummary summary = (BulletinSummary)currentSummaries.get(row);
		if(column == COLUMN_RETRIEVE_FLAG)
			return new Boolean(summary.isChecked());
		if(column == COLUMN_TITLE)
			return summary.getTitle();
		if(column == COLUMN_AUTHOR)
			return summary.getAuthor();
		if(column == COLUMN_LAST_DATE_SAVED)
			return getLocalization().formatDateTime(summary.getDateTimeSaved());
		if(column == COLUMN_BULLETIN_SIZE)
			return  getSizeInKbytes(summary.getSize());
		if(column == COLUMN_DELETE_FLAG)
			return new Boolean(summary.isChecked());
		if(column == COLUMN_VERSION_NUMBER)
			return new Integer(summary.getVersionNumber());
		return "";
	}

	public void setValueAt(Object value, int row, int column)
	{
		BulletinSummary summary = (BulletinSummary)currentSummaries.get(row);
		if(column == COLUMN_RETRIEVE_FLAG)
			summary.setChecked(((Boolean)value).booleanValue());
		if(column == COLUMN_DELETE_FLAG)
			summary.setChecked(((Boolean)value).booleanValue());
	}

	public Class getColumnClass(int column)
	{
		if(column == COLUMN_RETRIEVE_FLAG)
			return Boolean.class;
		if(column == COLUMN_TITLE)
			return String.class;
		if(column == COLUMN_AUTHOR)
			return String.class;
		if(column == COLUMN_LAST_DATE_SAVED)
			return String.class;
		if(column == COLUMN_BULLETIN_SIZE)
			return Integer.class;
		if(column == COLUMN_DELETE_FLAG)
			return Boolean.class;
		if(column == COLUMN_VERSION_NUMBER)
			return Integer.class;
		return null;
	}
	
	UiBasicLocalization getLocalization()
	{
		return localization;
	}

	protected void setProgressDialog(UiProgressRetrieveSummariesDlg progressDlg)
	{
		retrieverDlg = progressDlg;
	}

	public void changeToDownloadableSummaries()
	{
		currentSummaries = downloadableSummaries;
	}

	public void changeToAllSummaries()
	{
		currentSummaries = allSummaries;
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

	public Vector getSelectedUidsLatestVersion()
	{
		return getListOfRequestedUids(false);
	}

	public Vector getSelectedUidsFullHistory()
	{
		return getListOfRequestedUids(true);
	}

	private Vector getListOfRequestedUids(boolean includeEalierVerions)
	{
		Vector uidList = new Vector();
		for(int i = 0; i < currentSummaries.size(); ++i)
		{
			BulletinSummary summary = (BulletinSummary)currentSummaries.get(i);
			if(summary.isChecked())
			{
				UniversalId uid = UniversalId.createFromAccountAndLocalId(summary.getAccountId(), summary.getLocalId());
				uidList.add(uid);
				if(includeEalierVerions)
				{
					BulletinHistory history = summary.getHistory();
					for(int h = 0; h < history.size(); ++h)
					{
						uid = UniversalId.createFromAccountAndLocalId(summary.getAccountId(), history.get(h));
						uidList.add(uid);
					}
				}
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

	public void getMySealedSummaries() throws ServerErrorException
	{
		String accountId = app.getAccountId();
		SummaryRetriever retriever = new SealedSummaryRetriever(app, accountId);
		retrieveSummaries(accountId, retriever);
	}

	public void getMyDraftSummaries() throws ServerErrorException
	{
		String accountId = app.getAccountId();
		SummaryRetriever retriever = new DraftSummaryRetriever(app, accountId);
		retrieveSummaries(accountId, retriever);
	}

	public void getFieldOfficeSealedSummaries(String fieldOfficeAccountId) throws ServerErrorException
	{
		SummaryRetriever retriever = new SealedSummaryRetriever(app, fieldOfficeAccountId);
		retrieveSummaries(fieldOfficeAccountId, retriever);
	}

	public void getFieldOfficeDraftSummaries(String fieldOfficeAccountId) throws ServerErrorException
	{
		SummaryRetriever retriever = new DraftSummaryRetriever(app, fieldOfficeAccountId);
		retrieveSummaries(fieldOfficeAccountId, retriever);
	}

	private void retrieveSummaries(String fieldOfficeAccountId, SummaryRetriever retriever) throws ServerErrorException
	{
		Vector summaryStrings = getSummaryStringsFromServer(retriever);
		Vector summaries = createSummariesFromStrings(fieldOfficeAccountId, summaryStrings);
		markAsOnServer(summaries);
		updateAllDownloadableFlags(summaries);
		allSummaries.addAll(summaries);
	}

	private Vector getSummaryStringsFromServer(SummaryRetriever retriever) throws ServerErrorException
	{
		try
		{
			NetworkResponse response = retriever.getSummaries();
			if(!response.getResultCode().equals(NetworkInterfaceConstants.OK))
				throw new ServerErrorException();

			return response.getResultVector();
		}
		catch (MartusSignatureException e)
		{
			System.out.println("RetrieveTableModel.getFieldOfficeSummaryStringsFromServer: " + e);
			throw new ServerErrorException();
		}
	}

	private void updateAllDownloadableFlags(Vector summaries)
	{
		Iterator iterator = summaries.iterator();
		while(iterator.hasNext())
		{
			BulletinSummary currentSummary = (BulletinSummary)iterator.next();
			currentSummary.setDownloadable(isDownloadable(currentSummary));
		}
	}

	public boolean isDownloadable(BulletinSummary currentSummary)
	{
		String accountId = currentSummary.getAccountId();
		String localId = currentSummary.getLocalId();
		UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, localId);
		DatabaseKey key = DatabaseKey.createLegacyKey(uid);
		if(store.doesBulletinRevisionExist(key))
			return false;
		
		if(store.hasNewerRevision(uid))
			return false;
		
		return true;
	}

	protected void buildDownloadableSummariesList()
	{
		downloadableSummaries = new Vector();
		Iterator iterator = allSummaries.iterator();
		while(iterator.hasNext())
		{
			BulletinSummary currentSummary = (BulletinSummary)iterator.next();
			if(currentSummary.isDownloadable())
				downloadableSummaries.add(currentSummary);
		}
	}

	public Vector createSummariesFromStrings(String accountId, Vector summaryStrings)
	{
		RetrieveThread worker = new RetrieveThread(accountId, summaryStrings);
		worker.start();

		if(retrieverDlg == null)
			waitForThreadToTerminate(worker);
		else
			retrieverDlg.beginRetrieve();
		
		return worker.getSummaries();
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
			result = new Vector();
		}

		public void run()
		{
			retrieveAllSummaries();
			finishedRetrieve();
		}
		
		public Vector getSummaries()
		{
			return result;
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
					result.add(bulletinSummary);
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
		private Vector result;
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
	
	void markAsOnServer(Vector summaries)
	{
		for(int i=0; i < summaries.size(); ++i)
		{
			BulletinSummary summary = (BulletinSummary)summaries.get(i);
			Bulletin b = app.getStore().getBulletinRevision(summary.getUniversalId());
			if(b != null)
				app.getStore().setIsOnServer(b);
		}
	}
	
	public Vector getUidsThatWouldBeUpgrades(Vector uidsSelectedForRetrieve)
	{
		Vector uidsBeingUpgraded = new Vector();
		
		for(int i=0; i < allSummaries.size(); ++i)
		{
			BulletinSummary summary = (BulletinSummary)allSummaries.get(i);
			UniversalId uidBeingRetrieved = summary.getUniversalId();
			if(!uidsSelectedForRetrieve.contains(uidBeingRetrieved))
				continue;
			
			if(doesBulletinExist(summary))
				uidsBeingUpgraded.add(uidBeingRetrieved);
		}
		
		return uidsBeingUpgraded;
	}

	private boolean doesBulletinExist(BulletinSummary summary)
	{
		String accountId = summary.getAccountId();
		BulletinHistory history = summary.getHistory();
		for(int j = 0; j < history.size(); ++j)
		{
			String localId = history.get(j);
			UniversalId uidBeingChecked = UniversalId.createFromAccountAndLocalId(accountId, localId);
			DatabaseKey key = DatabaseKey.createLegacyKey(uidBeingChecked);
			if(store.doesBulletinRevisionExist(key))
				return true;
		}
		return false;
	}

	public static Object getSizeInKbytes(int sizeKb)
	{
		sizeKb /= 1000;
		if(sizeKb <= 0)
			sizeKb = 1;
		Integer sizeInK = new Integer(sizeKb);
		return sizeInK;
	}
	
	static abstract class SummaryRetriever
	{
		SummaryRetriever(MartusApp appToUse, String accountIdToUse)
		{
			app = appToUse;
			accountId = accountIdToUse;
		}
		
		NetworkResponse getSummaries() throws MartusSignatureException, ServerErrorException
		{
			if(!app.isSSLServerAvailable())
				throw new ServerErrorException("No server");
			return internalGetSummaries();
		}
		
		abstract NetworkResponse internalGetSummaries() throws MartusSignatureException;
		
		protected MartusCrypto getSecurity()
		{
			MartusCrypto security = app.getSecurity();
			return security;
		}

		protected Vector getSummaryTags()
		{
			return BulletinSummary.getNormalRetrieveTags();
		}

		protected ClientSideNetworkGateway getGateway()
		{
			return app.getCurrentNetworkInterfaceGateway();
		}

		MartusApp app;
		String accountId;
	}

	static class DraftSummaryRetriever extends SummaryRetriever
	{
		DraftSummaryRetriever(MartusApp appToUse, String accountIdToUse)
		{
			super(appToUse, accountIdToUse);
		}

		NetworkResponse internalGetSummaries() throws MartusSignatureException
		{
			return getGateway().getDraftBulletinIds(getSecurity(), accountId, getSummaryTags());
		}
		
	}

	static class SealedSummaryRetriever extends SummaryRetriever
	{
		SealedSummaryRetriever(MartusApp appToUse, String accountIdToUse)
		{
			super(appToUse, accountIdToUse);
		}

		NetworkResponse internalGetSummaries() throws MartusSignatureException
		{
			return getGateway().getSealedBulletinIds(getSecurity(), accountId, getSummaryTags());
		}
		
	}

	MartusApp app;
	UiBasicLocalization localization;
	int columnCount;
	
	ClientBulletinStore store;
	UiProgressRetrieveSummariesDlg retrieverDlg;
	protected Vector currentSummaries;
	private Vector downloadableSummaries;
	protected Vector allSummaries;
	ServerErrorException errorThrown;
	
	public int COLUMN_RETRIEVE_FLAG = -1;
	public int COLUMN_TITLE = -1;
	public int COLUMN_AUTHOR = -1;
	public int COLUMN_LAST_DATE_SAVED = -1;
	public int COLUMN_BULLETIN_SIZE = -1;
	public int COLUMN_DELETE_FLAG = -1;
	public int COLUMN_VERSION_NUMBER = -1;
}
