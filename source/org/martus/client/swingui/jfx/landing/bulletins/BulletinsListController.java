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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
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
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.SortableBulletinList;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionMenuModifyFxBulletin;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.generic.SimpleHtmlContentController;
import org.martus.client.swingui.jfx.generic.controls.FxButtonTableCellFactory;
import org.martus.client.swingui.jfx.landing.AbstractFxLandingContentController;
import org.martus.client.swingui.jfx.landing.FxLandingShellController;
import org.martus.client.swingui.jfx.landing.cases.CaseListItem;
import org.martus.client.swingui.jfx.landing.cases.CaseListProvider;
import org.martus.client.swingui.jfx.setupwizard.tasks.AbstractExportTask;
import org.martus.client.swingui.jfx.setupwizard.tasks.BulletinExportEncryptedMbaTask;
import org.martus.client.swingui.jfx.setupwizard.tasks.BulletinExportUnencryptedXmlTask;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.MartusLogger;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinHtmlGenerator;
import org.martus.common.database.ReadableDatabase;
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
		
        Image viewImage = new Image(VIEW_BULLETIN_IMAGE_PATH);
        viewBulletinColumn.setCellFactory(new FxButtonTableCellFactory(viewImage, () -> viewSelectedBulletin()));
		viewBulletinColumn.setCellValueFactory(new PropertyValueFactory<Object, Boolean>(BulletinTableRowData.CAN_VIEW_PROPERTY_NAME));
		
        Image editImage = new Image(EDIT_BULLETIN_IMAGE_PATH);
        editBulletinColumn.setCellFactory(new FxButtonTableCellFactory(editImage, () -> editSelectedBulletin()));
		editBulletinColumn.setCellValueFactory(new PropertyValueFactory<Object, Boolean>(BulletinTableRowData.CAN_EDIT_PROPERTY_NAME));
	}
	
	private void initalizeButtons()
	{
		BooleanBinding noItemsSelectedBinding = itemsTable.getSelectionModel().selectedItemProperty().isNull();
		trashButton.disableProperty().bind(noItemsSelectedBinding);
		exportButton.disableProperty().bind(noItemsSelectedBinding);
		moveButton.disableProperty().bind(noItemsSelectedBinding);

		BooleanBinding onlyOneItemSelectedBinding = Bindings.equal(1, Bindings.size(itemsTable.getSelectionModel().getSelectedItems()));
		copyButton.disableProperty().bind(onlyOneItemSelectedBinding.not());
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
	
	protected void viewSelectedBulletin()
	{
		TableViewSelectionModel<BulletinTableRowData> selectionModel = itemsTable.getSelectionModel();
		BulletinTableRowData selectedItem = selectionModel.getSelectedItem();
		if(selectedItem == null)
		{
			MartusLogger.log("Attempted to view with nothing selected");
			return;
		}
		UniversalId bulletinUid = selectedItem.getUniversalId();
		ClientBulletinStore store = getApp().getStore();
		Bulletin bulletin = store.getBulletinRevision(bulletinUid);
		
		ReadableDatabase database = store.getDatabase();
		BulletinHtmlGenerator generator = new BulletinHtmlGenerator(getLocalization());
		try
		{
			String html = generator.getHtmlString(bulletin, database, true, true);
			FxController htmlViewer = new SimpleHtmlContentController(getMainWindow(), html);
			showDialogWithClose("ViewBulletin", htmlViewer);
		}
		catch(Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	protected void editSelectedBulletin()
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
			viewSelectedBulletin();
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
			exportBulletins(bulletinsIDsToExport.toArray(new UniversalId[0]));
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}		
	}

	private void exportBulletins(UniversalId[] bulletinsIdsToExport)throws Exception
	{
		String defaultFileName = getDefaultExportFileName(bulletinsIdsToExport);
		ExportItemsController exportController = new ExportItemsController(getMainWindow(), defaultFileName, bulletinsIdsToExport.length);
		if(showModalYesNoDialog("Export", "export", EnglishCommonStrings.CANCEL, exportController))
		{
			File exportFile = exportController.getExportFileOrFolder();
			if(exportController.didUserApproveOverwritingExistingFile())
				exportFile.delete();
			
			if(exportController.shouldExportEncrypted())
				doExportEncryptedMbaBulletins(bulletinsIdsToExport, exportFile);
			else
				doExportUnencryptedXmlBulletins(bulletinsIdsToExport, exportFile, exportController.includeAttachments());
		}
	}	

	private void doExportEncryptedMbaBulletins(UniversalId[] bulletinIdsToExport, File exportFile)
	{
		AbstractExportTask task = new BulletinExportEncryptedMbaTask(getMainWindow(), bulletinIdsToExport, exportFile);
		doExport(task, "ExportBulletinMba");
	}

	private void doExportUnencryptedXmlBulletins(UniversalId[] bulletinsIdsToExport, File exportFile, boolean includeAttachments)
	{
		AbstractExportTask task = new BulletinExportUnencryptedXmlTask(getMainWindow(), bulletinsIdsToExport, exportFile, includeAttachments);
		doExport(task, "ExportBulletinXml");
	}

	private void doExport(AbstractExportTask task, String progressDialogMessageTag)
	{
		try
		{
			showProgressDialog(getLocalization().getFieldLabel(progressDialogMessageTag), task);
			if(task.didErrorOccur())
			{
				String errorMessage = task.getErrorMessage();
				Map errorMessageTokens = task.getErrorMessageTokens();
				showNotifyDialog(errorMessage, errorMessageTokens);
			}
		}
		catch (UserCancelledException e)
		{
		}
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	private String getDefaultExportFileName(UniversalId[] bulletinsIdsToExport)
	{
		String defaultFileName = getLocalization().getFieldLabel("ExportedBulletins");
		if(bulletinsIdsToExport.length==1)
			defaultFileName = getMainWindow().getStore().getBulletinRevision(bulletinsIdsToExport[0]).toFileName();
		return defaultFileName;
	}
	
	private CaseListProvider getAvailableCasesForMove()
	{
		CaseListProvider currentCases = ((FxLandingShellController)getShellController()).getCurrentCaseListProvider();
		BulletinFolder currentFolder = bulletinTableProvider.getFolder();
		
		CaseListProvider casesToMoveInto = new CaseListProvider();
		for (CaseListItem caseListItem : currentCases)
		{
			if(!caseListItem.getFolder().equals(currentFolder))
				casesToMoveInto.add(caseListItem);
		}
		return casesToMoveInto;
	}

	@FXML
	private void onMoveSelectedItems(javafx.event.ActionEvent event)
	{
		try
		{
			CaseListProvider casesAvailableToMoveItemsTo = getAvailableCasesForMove();
			MoveItemsToCasesConfirmationController moveItemsController = new MoveItemsToCasesConfirmationController(getMainWindow(), casesAvailableToMoveItemsTo);
			if(showModalYesNoDialog("MoveRecords", "move", EnglishCommonStrings.CANCEL, moveItemsController))
			{
				ObservableList<CaseListItem> selectedCases = moveItemsController.getSelectedCases();
				Vector<UniversalId> bulletinIdsToMove = getSelectedBulletinIds();
				ClientBulletinStore store = getApp().getStore();
				BulletinFolder currentCase = bulletinTableProvider.getFolder();
				boolean deleteFromCurrentCase = moveItemsController.deleteFromCurrentCase();
				for (UniversalId bulletinId : bulletinIdsToMove)
				{
					Bulletin b = store.getBulletinRevision(bulletinId);
					for (CaseListItem caseItem : selectedCases)
					{
						BulletinFolder folderToMoveTo = caseItem.getFolder();
						store.linkBulletinToFolder(b, folderToMoveTo);		
					}
					if(deleteFromCurrentCase)
						store.removeBulletinFromFolder(currentCase, b);
				}
				store.saveFolders();
			}
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}
	
	@FXML
	private void onCopySelectedItem(javafx.event.ActionEvent event)
	{
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
	protected TableColumn<Object, Boolean> viewBulletinColumn;

	@FXML
	protected TableColumn<Object, Boolean> editBulletinColumn;
	
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
