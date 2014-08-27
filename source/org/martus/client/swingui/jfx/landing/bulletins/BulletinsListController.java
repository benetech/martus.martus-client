/*

monitoring software. Copyright (C) 2014, Beneficent
The Martus(tm) free, social justice documentation and
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.core.SortableBulletinList;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionMenuExportBulletins;
import org.martus.client.swingui.actions.ActionMenuExportMba;
import org.martus.client.swingui.actions.ActionMenuModifyFxBulletin;
import org.martus.client.swingui.jfx.landing.AbstractFxLandingContentController;
import org.martus.common.MartusLogger;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;

public class BulletinsListController extends AbstractFxLandingContentController
{

	public BulletinsListController(UiMainWindow mainWindowToUse, BulletinListProvider bulletinListProviderToUse)
	{
		super(mainWindowToUse);
		bulletinTableProvider = bulletinListProviderToUse;
	}

	@Override
	public void initializeMainContentPane()
	{
		initalizeColumns();
		initalizeItemsTable();
		initalizeButtons();
	}

	private void initalizeItemsTable()
	{
		Label noBulletins = new Label(getLocalization().getFieldLabel("NoBulletinsInTable"));
		itemsTable.setPlaceholder(noBulletins);
		itemsTable.setItems(bulletinTableProvider);
		itemsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		loadAllBulletinsAndSortByMostRecent();
	}

	private void initalizeColumns()
	{
		onServerColumn.setCellValueFactory(new PropertyValueFactory<BulletinTableRowData, Boolean>(BulletinTableRowData.ON_SERVER_PROPERTY_NAME));
		onServerColumn.setCellFactory(new BulletinOnServerColumnHandler());
		authorColumn.setCellValueFactory(new PropertyValueFactory<BulletinTableRowData, String>(BulletinTableRowData.AUTHOR_PROPERTY_NAME));
		authorColumn.setCellFactory(TextFieldTableCell.<BulletinTableRowData>forTableColumn());
		titleColumn.setCellValueFactory(new PropertyValueFactory<BulletinTableRowData, String>(BulletinTableRowData.TITLE_PROPERTY_NAME));
		titleColumn.setCellFactory(TextFieldTableCell.<BulletinTableRowData>forTableColumn());
		dateSavedColumn.setCellValueFactory(new PropertyValueFactory<BulletinTableRowData, String>(BulletinTableRowData.DATE_SAVDED_PROPERTY_NAME));
		dateSavedColumn.setCellFactory(TextFieldTableCell.<BulletinTableRowData>forTableColumn());
		viewBulletinColumn.setCellValueFactory(new PropertyValueFactory<BulletinTableRowData, String>(BulletinTableRowData.VIEW_BULLETIN_PROPERTY_NAME));
		viewBulletinColumn.setCellFactory(new ViewEditBulletinTableColumnButton(new ViewBulletinListener(), VIEW_BULLETIN_IMAGE_PATH));
		editBulletinColumn.setCellValueFactory(new PropertyValueFactory<BulletinTableRowData, String>(BulletinTableRowData.EDIT_BULLETIN_PROPERTY_NAME));
		editBulletinColumn.setCellFactory(new ViewEditBulletinTableColumnButton(new EditBulletinListener(), EDIT_BULLETIN_IMAGE_PATH));
	}
	
	private void initalizeButtons()
	{
		BooleanBinding noItemsSelectedBinding = itemsTable.getSelectionModel().selectedItemProperty().isNull();
		trashButton.disableProperty().bind(noItemsSelectedBinding);
		exportButton.disableProperty().bind(noItemsSelectedBinding);
		moveButton.disableProperty().bind(noItemsSelectedBinding);
		copyButton.setDisable(true);//FIXME only one item selected for copy
	}

	public void loadAllBulletinsAndSortByMostRecent()
	{
		bulletinTableProvider.loadAllBulletinsSelectInitialCaseFolder();
		sortByMostRecentBulletins();
	}
	
	protected void sortByMostRecentBulletins()
	{
		dateSavedColumn.setSortType(SortType.DESCENDING);
		ObservableList<TableColumn<BulletinTableRowData, ?>> sortOrder = itemsTable.getSortOrder();
		sortOrder.clear();
		sortOrder.add(dateSavedColumn);
		itemsTable.sort();
	}
	
	
	private class ViewBulletinListener implements ActionListener
	{
		public ViewBulletinListener()
		{
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			viewBulletin();
		}

	}

	private class EditBulletinListener implements ActionListener
	{
		public EditBulletinListener()
		{
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			editBulletin();
		}

	}
	
	protected void viewBulletin()
	{
		//TODO implement this.
	}

	protected void editBulletin()
	{
		TableViewSelectionModel<BulletinTableRowData> selectionModel = itemsTable.getSelectionModel();
		BulletinTableRowData selectedItem = selectionModel.getSelectedItem();
		if(selectedItem == null)
		{
			MartusLogger.log("Attempted to edit with nothing selected");
			return;
		}
		UniversalId bulletinUid = selectedItem.getUniversalId();
		Bulletin bulletinSelected = getApp().getStore().getBulletinRevision(bulletinUid);
		getStage().doAction(new ActionMenuModifyFxBulletin(getMainWindow(), bulletinSelected));
	}

	public void updateSearchResultsTable(SortableBulletinList searchResults)
	{
		Platform.runLater(new UpdateSearchResultsHandler(searchResults));
	}
	
	private class UpdateSearchResultsHandler implements Runnable
	{
		public UpdateSearchResultsHandler(SortableBulletinList searchResults)
		{
			results = searchResults;
		}

		@Override
		public void run()
		{
			Set foundUids = new HashSet(Arrays.asList(results.getUniversalIds()));
			loadBulletinData(foundUids);
		}
		
		private SortableBulletinList results;
	}
	

	public void loadBulletinData(Set bulletinUids)
	{
		bulletinTableProvider.loadBulletinData(bulletinUids);
		itemsTable.sort();
	}
	
	public void bulletinContentsHaveChanged(Bulletin bulletinUpdated)
	{
		//TODO this will be for a Preview Window Update
		Platform.runLater(new BulletinTableChangeHandler(bulletinUpdated));
	}
	
	private class BulletinTableChangeHandler implements Runnable
	{
		public BulletinTableChangeHandler(Bulletin bulletinToUpdate)
		{
			bulletin = bulletinToUpdate;
		}
		
		public void run()
		{
			boolean shouldReSortTable = bulletinTableProvider.updateBulletin(bulletin);
			if(shouldReSortTable)
				sortByMostRecentBulletins();
		}
		public Bulletin bulletin;
	}
	
	@FXML
	public void onMouseClick(MouseEvent mouseEvent) 
	{
		if(isDoubleClick(mouseEvent))
			viewBulletin();
	}
	
	@Override
	public String getFxmlLocation()
	{
		return "landing/bulletins/FxTableViewItems.fxml";
	}
	
	private Vector<UniversalId> getSelectedBulletinIds()
	{
		ObservableList<BulletinTableRowData> selectedItems = itemsTable.getSelectionModel().getSelectedItems();
		Vector<UniversalId> selectedIds = new Vector(selectedItems.size());
		for (BulletinTableRowData bulletinTableRowData : selectedItems)
		{
			selectedIds.add(bulletinTableRowData.getUniversalId());
		}
		return selectedIds;
	}
	
	@FXML
	private void onTrashSelectedItems(javafx.event.ActionEvent event)
	{
		Vector<UniversalId> bulletinsIDsToDiscard = getSelectedBulletinIds();
		BulletinFolder folderToDiscardFrom = bulletinTableProvider.getFolder();
		if(folderToDiscardFrom.isDiscardedFolder())
		{
			//FIXME warn user about unsent copies / copies in other folders when deleting from discarded folder
			//Implement this when we can actually see the discarded folder and delete from it.
			//See: UiBulletinTable:confirmDiscardSingleBulletin(Bulletin b)
			//     UiBulletinTable:confirmDiscardMultipleBulletins
		}
		try
		{
			getApp().discardBulletinsFromFolder(folderToDiscardFrom,bulletinsIDsToDiscard.toArray(new UniversalId[0]));
		}
		catch (IOException e)
		{
			logAndNotifyUnexpectedError(e);
		}
		bulletinTableProvider.updateContents();
	}

	@FXML
	private void onExportSelectedItems(javafx.event.ActionEvent event)
	{
		try
		{
			Vector<UniversalId> bulletinsIDsToExport = getSelectedBulletinIds();
			if(bulletinsIDsToExport.size() == 1)
			{
				exportSingleBulletin(bulletinsIDsToExport.toArray(new UniversalId[0]));
			}
			else
			{
				exportUnencryptedXmlBulletins(bulletinsIDsToExport.toArray(new UniversalId[0]));
			}
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}		
	}

	private void exportSingleBulletin(UniversalId[] bulletinsIDsToExport)throws Exception
	{
		ConfirmEncryptedExportController exportController = new ConfirmEncryptedExportController(getMainWindow());
		if(showModalYesNoDialog("EncryptBulletin", "export", "cancel", exportController))
		{
			if(exportController.shouldExportEncrypted())
				exportEncryptedMbaBulletin(bulletinsIDsToExport[0]);
			else
				exportUnencryptedXmlBulletins(bulletinsIDsToExport);
		}
	}	
	
	@FXML
	private void onMoveSelectedItems(javafx.event.ActionEvent event)
	{
		
	}
	
	@FXML
	private void onCopySelectedItem(javafx.event.ActionEvent event)
	{
	}

	private void exportUnencryptedXmlBulletins(UniversalId[] bulletinsIdsToExport) throws Exception
	{
		doAction(new ActionMenuExportBulletins(getMainWindow(), bulletinsIdsToExport));
	}

	private void exportEncryptedMbaBulletin(UniversalId bulletinIdToExport)
	{
		Bulletin bulletinToExport = getApp().getStore().getBulletinRevision(bulletinIdToExport);
		doAction(new ActionMenuExportMba(getMainWindow(), bulletinToExport));
	}

	final private String VIEW_BULLETIN_IMAGE_PATH = "/org/martus/client/swingui/jfx/images/view_bulletin.png";
	final private String EDIT_BULLETIN_IMAGE_PATH = "/org/martus/client/swingui/jfx/images/edit_bulletin.png";
	
	@FXML 
	protected TableView<BulletinTableRowData> itemsTable;

	@FXML
	protected TableColumn<BulletinTableRowData, Boolean> onServerColumn;

	@FXML
	protected TableColumn<BulletinTableRowData, String> authorColumn;

	@FXML
	protected TableColumn<BulletinTableRowData, String> titleColumn;

	@FXML
	protected TableColumn<BulletinTableRowData, String> dateSavedColumn;	

	@FXML
	protected TableColumn<BulletinTableRowData, String> viewBulletinColumn;

	@FXML
	protected TableColumn<BulletinTableRowData, String> editBulletinColumn;
	
	@FXML
	private Button trashButton;
	
	@FXML
	private Button exportButton;
	
	@FXML
	private Button moveButton;

	@FXML
	private Button copyButton;

	protected BulletinListProvider bulletinTableProvider;
}
