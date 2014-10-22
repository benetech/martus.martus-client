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

import java.util.Set;
import java.util.Vector;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.landing.AbstractFxLandingContentController;
import org.martus.client.swingui.tablemodels.RetrieveMyDraftsTableModel;
import org.martus.client.swingui.tablemodels.RetrieveMyTableModel;


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
			syncRecordsTableProvider.addBulletinsAndSummaries(localRecords, serverMyDrafts, serverMySealeds);
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
		RetrieveMyDraftsTableModel model = new RetrieveMyDraftsTableModel(getApp(), getLocalization());
		model.populateAllSummariesList();
		return model.getAllSummaries();
	}

	private Vector getServerMySealeds() throws Exception
	{
		//TODO should show a progress dialog that user can abort.
		RetrieveMyTableModel model = new RetrieveMyTableModel(getApp(), getLocalization());
		model.populateAllSummariesList();
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
		
	}

	@FXML 	
	private void onDelete(ActionEvent event)
	{
		
	}
		
	@FXML 	
	private void onShowAll(ActionEvent event)
	{
		syncRecordsTableProvider.show(ServerSyncTableRowData.LOCATION_ANY);
	}

	@FXML 	
	private void onShowLocalOnly(ActionEvent event)
	{
		syncRecordsTableProvider.show(ServerSyncTableRowData.LOCATION_LOCAL);
	}

	@FXML 	
	private void onShowServerOnly(ActionEvent event)
	{
		syncRecordsTableProvider.show(ServerSyncTableRowData.LOCATION_SERVER);
	}

	@FXML 	
	private void onShowBoth(ActionEvent event)
	{
		syncRecordsTableProvider.show(ServerSyncTableRowData.LOCATION_BOTH);
	}

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
	
	private SyncRecordsTableProvider syncRecordsTableProvider;
}
