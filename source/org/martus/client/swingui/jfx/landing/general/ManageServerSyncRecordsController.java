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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.landing.AbstractFxLandingContentController;
import org.martus.client.swingui.tablemodels.RetrieveHQDraftsTableModel;
import org.martus.client.swingui.tablemodels.RetrieveHQTableModel;
import org.martus.client.swingui.tablemodels.RetrieveMyDraftsTableModel;
import org.martus.client.swingui.tablemodels.RetrieveMyTableModel;
import org.martus.client.swingui.tablemodels.RetrieveTableModel;
import org.martus.common.packet.UniversalId;


public class ManageServerSyncRecordsController extends AbstractFxLandingContentController
{
	public ManageServerSyncRecordsController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	@Override
	public void initializeMainContentPane()
	{
		initalizeColumns();
		initalizeItemsTable();

		RecordSelectedListener recordSelectedListener = new RecordSelectedListener();
		allRecordsTable.getSelectionModel().selectedItemProperty().addListener(recordSelectedListener);
		updateButtons();
	}
	
	private void initalizeItemsTable()
	{
		Label noRecords = new Label(getLocalization().getFieldLabel("NoServerSyncDataInTable"));
		allRecordsTable.setPlaceholder(noRecords);
		allRecordsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		syncRecordsTableProvider = new SyncRecordsTableProvider(getMainWindow());
		allRecordsTable.setItems(syncRecordsTableProvider);
		try
		{
			Set localRecords = getLocalRecords();
			Vector serverMyDrafts = getServerMyDrafts();
			Vector serverMySealeds = getServerMySealeds();
			Vector serverHQDrafts = getServerHQDrafts();
			Vector serverHQSealeds = getServerHQSealeds();
			syncRecordsTableProvider.addBulletinsAndSummaries(localRecords, serverMyDrafts, serverMySealeds, serverHQDrafts, serverHQSealeds);
			onShowAll(null);
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	private Vector getServerMyDrafts() throws Exception
	{
		//TODO should show a progress dialog that user can abort.
		RetrieveTableModel model = new RetrieveMyDraftsTableModel(getApp(), getLocalization());
		model.initialize(null);
		return model.getAllSummaries();
	}

	private Vector getServerMySealeds() throws Exception
	{
		//TODO should show a progress dialog that user can abort.
		RetrieveTableModel model = new RetrieveMyTableModel(getApp(), getLocalization());
		model.initialize(null);
		return model.getAllSummaries();
	}

	private Vector getServerHQDrafts() throws Exception
	{
		//TODO should show a progress dialog that user can abort.
		RetrieveTableModel model = new RetrieveHQDraftsTableModel(getApp(), getLocalization());
		model.initialize(null);
		return model.getAllSummaries();
	}

	private Vector getServerHQSealeds() throws Exception
	{
		//TODO should show a progress dialog that user can abort.
		RetrieveTableModel model = new RetrieveHQTableModel(getApp(), getLocalization());
		model.initialize(null);
		return model.getAllSummaries();
	}

	private Set getLocalRecords()
	{
		return getApp().getStore().getAllBulletinLeafUids();
	}

	private void initalizeColumns()
	{
		recordLocationColumn.setCellValueFactory(new PropertyValueFactory<ServerSyncTableRowData, String>(ServerSyncTableRowData.LOCATION_PROPERTY_NAME));
		recordLocationColumn.setCellFactory(TextFieldTableCell.<ServerSyncTableRowData>forTableColumn());
		recordTitleColumn.setCellValueFactory(new PropertyValueFactory<ServerSyncTableRowData, String>(ServerSyncTableRowData.TITLE_PROPERTY_NAME));
		recordTitleColumn.setCellFactory(TextFieldTableCell.<ServerSyncTableRowData>forTableColumn());
		recordAuthorColumn.setCellValueFactory(new PropertyValueFactory<ServerSyncTableRowData, String>(ServerSyncTableRowData.AUTHOR_PROPERTY_NAME));
		recordAuthorColumn.setCellFactory(TextFieldTableCell.<ServerSyncTableRowData>forTableColumn());
		recordLastSavedColumn.setCellValueFactory(new PropertyValueFactory<ServerSyncTableRowData, String>(ServerSyncTableRowData.DATE_SAVDED_PROPERTY_NAME));
		recordLastSavedColumn.setCellFactory(TextFieldTableCell.<ServerSyncTableRowData>forTableColumn());
		recordSizeColumn.setCellValueFactory(new PropertyValueFactory<ServerSyncTableRowData, Integer>(ServerSyncTableRowData.SIZE_PROPERTY_NAME));
		recordSizeColumn.setCellFactory(new RecordSizeColumnHandler());
	}

	private class RecordSelectedListener implements ChangeListener<ServerSyncTableRowData>
	{
		public RecordSelectedListener()
		{
		}

		@Override
		public void changed(ObservableValue<? extends ServerSyncTableRowData> observalue	,
				ServerSyncTableRowData previousRecord, ServerSyncTableRowData newRecord)
		{
			updateButtons();
		}
	}

	protected void updateButtons()
	{
		ObservableList<ServerSyncTableRowData> rowsSelected = allRecordsTable.getSelectionModel().getSelectedItems();
		boolean isAnythingMutable = false;
		boolean isAnythingLocal = false;
		boolean isAnythingRemote = false;
		for (Iterator iterator = rowsSelected.iterator(); iterator.hasNext();)
		{
			ServerSyncTableRowData data = (ServerSyncTableRowData) iterator.next();
			if(data.canDeleteFromServerProperty().getValue())
				isAnythingMutable = true;
			if(data.isLocal().getValue())
				isAnythingLocal = true;
			if(data.isRemote().getValue())
				isAnythingRemote = true;
		}
		deleteButton.setDisable(!isAnythingMutable);
		uploadButton.setDisable(!isAnythingLocal);
		downloadButton.setDisable(!isAnythingRemote);
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/general/ManageServerSyncRecords.fxml";
	}
	

	@FXML 	
	private void onUpload(ActionEvent event)
	{
		
	}

	@FXML 	
	private void onDownload(ActionEvent event)
	{
		ObservableList<ServerSyncTableRowData> selectedRows = allRecordsTable.getSelectionModel().getSelectedItems();
		StringBuilder localOnlyRecords = new StringBuilder();
		Vector<UniversalId> uidsToDownload = new Vector(selectedRows.size());
		for (Iterator iterator = selectedRows.iterator(); iterator.hasNext();)
		{
			ServerSyncTableRowData recordData = (ServerSyncTableRowData) iterator.next();
			if(recordData.getRawLocation() == ServerSyncTableRowData.LOCATION_LOCAL)
			{
				localOnlyRecords.append(TITLE_SEPARATOR);
				localOnlyRecords.append(recordData.getTitle());
			}
			else
			{
				uidsToDownload.add(recordData.getUniversalId());
			}
		}
		if(localOnlyRecords.length()>1)
			DisplayWarningDialog("SyncUnableToDownloadLocalFiles", localOnlyRecords);
		try
		{
			getMainWindow().retrieveRecordsFromServer(getApp().getNameOfFolderForAllRetrieved(), uidsToDownload);
			closeDialog();
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	private void closeDialog()
	{
		getSwingStage().close();
	}

	private void DisplayWarningDialog( String warningTag, StringBuilder titlesInQuestion)
	{
		showNotifyDialog(warningTag, titlesInQuestion.toString());
	}

	@FXML 	
	private void onDelete(ActionEvent event)
	{
		
	}
	
	private void updateTable(int TableToShow)
	{
		syncRecordsTableProvider.show(TableToShow);
		updateButtons();
	}
		
	@FXML 	
	private void onShowAll(ActionEvent event)
	{
		updateTable(ServerSyncTableRowData.LOCATION_ANY);
	}

	@FXML 	
	private void onShowLocalOnly(ActionEvent event)
	{
		updateTable(ServerSyncTableRowData.LOCATION_LOCAL);
	}

	@FXML 	
	private void onShowServerOnly(ActionEvent event)
	{
		updateTable(ServerSyncTableRowData.LOCATION_SERVER);
	}

	@FXML 	
	private void onShowBoth(ActionEvent event)
	{
		updateTable(ServerSyncTableRowData.LOCATION_BOTH);
	}

	private final String TITLE_SEPARATOR = "\n";
	
	@FXML
	private TableView<ServerSyncTableRowData> allRecordsTable;
	
	@FXML
	private TableColumn<ServerSyncTableRowData, String> recordLocationColumn;

	@FXML
	private TableColumn<ServerSyncTableRowData, String> recordTitleColumn;

	@FXML
	private TableColumn<ServerSyncTableRowData, String> recordAuthorColumn;

	@FXML
	private TableColumn<ServerSyncTableRowData, String> recordLastSavedColumn;

	@FXML
	private TableColumn<ServerSyncTableRowData, Integer> recordSizeColumn;
	
	@FXML 
	private Button uploadButton;
	
	@FXML 
	private Button downloadButton;

	@FXML 
	private Button deleteButton;
	
	private SyncRecordsTableProvider syncRecordsTableProvider;
}
