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
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.SortableBulletinList;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionMenuModifyFxBulletin;
import org.martus.client.swingui.fields.attachments.ViewAttachmentHandler;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.generic.SimpleHtmlContentController;
import org.martus.client.swingui.jfx.generic.controls.FxButtonTableCellFactory;
import org.martus.client.swingui.jfx.landing.AbstractFxLandingContentController;
import org.martus.client.swingui.jfx.landing.FxLandingShellController;
import org.martus.client.swingui.jfx.landing.cases.CaseListItem;
import org.martus.client.swingui.jfx.landing.cases.CaseListProvider;
import org.martus.client.swingui.jfx.landing.cases.FxFolderSettingsController;
import org.martus.client.swingui.jfx.setupwizard.tasks.AbstractExportTask;
import org.martus.client.swingui.jfx.setupwizard.tasks.BulletinExportEncryptedMbaTask;
import org.martus.client.swingui.jfx.setupwizard.tasks.BulletinExportUnencryptedXmlTask;
import org.martus.common.ContactKey;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.MartusLogger;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinHtmlGenerator;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.packet.UniversalId;
import org.martus.util.TokenReplacement;

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
		authorVerifiedColumn.setCellValueFactory(new PropertyValueFactory<BulletinTableRowData, Integer>(BulletinTableRowData.AUTHOR_VERIFIED_PROPERTY_NAME));
		authorVerifiedColumn.setCellFactory(new AuthorVerifiedColumnHandler());
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
		editBulletinColumn.visibleProperty().bind(getTrashNotBeingDisplayedBinding());
	}
	
	private void initalizeButtons()
	{
		BooleanBinding noItemsBinding = Bindings.isEmpty(itemsTable.getItems());
		emptyTrashButton.disableProperty().bind(noItemsBinding);
		
		BooleanBinding noItemsSelectedBinding = itemsTable.getSelectionModel().selectedItemProperty().isNull();
		trashButton.disableProperty().bind(noItemsSelectedBinding);
		exportButton.disableProperty().bind(noItemsSelectedBinding);
		moveButton.disableProperty().bind(noItemsSelectedBinding);

		BooleanBinding onlyOneItemSelectedBinding = Bindings.equal(1, Bindings.size(itemsTable.getSelectionModel().getSelectedItems()));
		copyButton.disableProperty().bind(onlyOneItemSelectedBinding.not());
		
		BooleanProperty trashBeingDisplayedProperty = bulletinTableProvider.getTrashFolderBeingDisplayedBooleanProperty();
		emptyTrashButton.visibleProperty().bind(trashBeingDisplayedProperty);

		BooleanBinding trashNotBeingDisplayedBinding = getTrashNotBeingDisplayedBinding();
		exportButton.visibleProperty().bind(trashNotBeingDisplayedBinding);
		copyButton.visibleProperty().bind(trashNotBeingDisplayedBinding);
		trashButton.visibleProperty().bind(trashNotBeingDisplayedBinding);
	}

	public BooleanBinding getTrashNotBeingDisplayedBinding()
	{
		return bulletinTableProvider.getTrashFolderBeingDisplayedBooleanProperty().not();
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
		BulletinTableRowData selectedItem = itemsTable.getSelectionModel().getSelectedItem();
		if(selectedItem == null)
		{
			MartusLogger.log("Attempted to view with nothing selected");
			return;
		}

		try
		{
			UniversalId bulletinUid = selectedItem.getUniversalId();
			FxController htmlViewer = getViewControllerForBulletin(bulletinUid, getMainWindow());
			showDialogWithClose("ViewBulletin", htmlViewer);
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	protected void editSelectedBulletin()
	{
		ActionMenuModifyFxBulletin actionDoer = new ActionMenuModifyFxBulletin(getMainWindow());
		performActionOnSelectedBulletin(actionDoer);
	}

	private void performActionOnSelectedBulletin(ActionMenuModifyFxBulletin actionDoer)
	{
		BulletinTableRowData selectedItem = itemsTable.getSelectionModel().getSelectedItem();
		if(selectedItem == null)
		{
			MartusLogger.log("Attempted to access bulletin with nothing selected");
			return;
		}
		
		UniversalId bulletinUid = selectedItem.getUniversalId();
		Bulletin bulletinSelected = getApp().getStore().getBulletinRevision(bulletinUid);
		try
		{
			notifyIfBulletinIsNotOursAndHasExternallyViewedAttachments(bulletinSelected);
			actionDoer.setBulletinToBeModified(bulletinSelected);
			getStage().doAction(actionDoer);
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	static public FxController getViewControllerForBulletin(UniversalId bulletinUid, UiMainWindow mainWindow)
		throws Exception
	{
		ClientBulletinStore store = mainWindow.getApp().getStore();
		Bulletin bulletin = store.getBulletinRevision(bulletinUid);
		
		ReadableDatabase database = store.getDatabase();
		BulletinHtmlGenerator generator = new BulletinHtmlGenerator(mainWindow.getLocalization());
		String html = generator.getHtmlString(bulletin, database, true, true);
		FxController htmlViewer = new SimpleHtmlContentController(mainWindow, html);
		return htmlViewer;
	}

	private void notifyIfBulletinIsNotOursAndHasExternallyViewedAttachments(Bulletin bulletinSelected) throws Exception
	{
		Integer status = getApp().getKeyVerificationStatus(bulletinSelected.getAccount());
		if(!ContactKey.isVerified(status))
		{
			if(wouldAnyAttachmentBeViewedExternally(bulletinSelected))
				showNotifyDialog("BulletinWithAnUnverifiedExternalAttachment");
		}
	}

	private boolean wouldAnyAttachmentBeViewedExternally(Bulletin original) throws Exception
	{
		AttachmentProxy[] publicAttachments = original.getPublicAttachments();
		if(wouldAnyAttachmentBeViewedExternally(publicAttachments))
			return true;
		AttachmentProxy[] privateAttachments = original.getPrivateAttachments();
		if(wouldAnyAttachmentBeViewedExternally(privateAttachments))
			return true;
		return false;		
	}

	private boolean wouldAnyAttachmentBeViewedExternally(AttachmentProxy[] attachments) throws Exception
	{
		int numberOfAttachments = attachments.length;
		if(numberOfAttachments == 0)
			return false;
		for (int i = 0; i < numberOfAttachments; i++)
		{
			AttachmentProxy attachmentProxy = attachments[i];
			File attachmentFileToView = ViewAttachmentHandler.obtainFileForAttachment(attachmentProxy, getMainWindow().getStore());
			AttachmentViewController attachmentViewer = new AttachmentViewController(getMainWindow(), attachmentFileToView);
			if(!attachmentViewer.canViewInProgram())
				return true;
		}
		return false;
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
			try
			{
				boolean shouldReSortTable = bulletinTableProvider.updateBulletin(bulletin);
				if(shouldReSortTable)
					sortByMostRecentBulletins();
			} 
			catch (Exception e)
			{
				logAndNotifyUnexpectedError(e);
			}
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
	
	private Vector<UniversalId> getAllUniversalIdsInFolder()
	{
		ObservableList<BulletinTableRowData> allItems = itemsTable.getItems();
		Vector<UniversalId> allIds = new Vector(allItems.size());
		for (BulletinTableRowData bulletinTableRowData : allItems)
		{
			allIds.add(bulletinTableRowData.getUniversalId());
		}
		return allIds;
		
	}
	
	@FXML
	private void onTrashSelectedItems(javafx.event.ActionEvent event)
	{
		Vector<UniversalId> bulletinsIDsToDiscard = getSelectedBulletinIds();
		BulletinFolder folderToDiscardFrom = bulletinTableProvider.getFolder();
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
	private void onEmptyTrash(javafx.event.ActionEvent event)
	{
		try
		{
			Vector<UniversalId> universalIDsToDiscard = getAllUniversalIdsInFolder();
			UniversalId[] arrayUniversalIdsToDiscard = universalIDsToDiscard.toArray(new UniversalId[0]);
			Vector otherCasesRecordsFoundIn = getApp().getNonDiscardedFoldersForBulletins(arrayUniversalIdsToDiscard);
			MartusLocalization localization = getLocalization();
			String confirmationMessage = localization.getFieldLabel("EmptyTrashConfirmation");
			
			if(otherCasesRecordsFoundIn.size() > 0)
			{
				String otherCasesRawMessage = localization.getFieldLabel("EmptyTrashConfirmationItemsInOtherFolders");
				String foldersLabel = FxFolderSettingsController.getCurrentFoldersHeading(getApp().getConfigInfo(), getLocalization());
				String idsFoundInCasesOtherthanTrashWarningMessage = TokenReplacement.replaceToken(otherCasesRawMessage, "#Cases#", foldersLabel);
				confirmationMessage += "\n\n";
				confirmationMessage += idsFoundInCasesOtherthanTrashWarningMessage;
			}

			if(showConfirmationDialog("EmptyTrash", EnglishCommonStrings.CONTINUE, EnglishCommonStrings.NO, confirmationMessage))
			{
				BulletinFolder trashFolder = bulletinTableProvider.getFolder();
				getApp().discardBulletinsFromFolder(trashFolder,arrayUniversalIdsToDiscard);
				bulletinTableProvider.updateContents();
			}
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
		
		
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
		CaseListProvider currentCases = ((FxLandingShellController)getShellController()).getAllCaseListProvider();
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
		UniversalId selectedId = itemsTable.getSelectionModel().getSelectedItem().getUniversalId();
		ClientBulletinStore store = getMainWindow().getStore();
		Bulletin bulletinToCopy = store.getBulletinRevision(selectedId);
		if(bulletinToCopy.hasUnknownTags() || bulletinToCopy.hasUnknownCustomField())
		{
			MartusLocalization localization = getLocalization();
			String copyingBulletinWithUnknownContentMessage = localization.getFieldLabel("confirmEditBulletinWithUnknownTagscause");
			copyingBulletinWithUnknownContentMessage += "\n\n";
			copyingBulletinWithUnknownContentMessage += localization.getFieldLabel("confirmEditBulletinWithUnknownTagseffect");
			
			if(!showConfirmationDialog("CopyItem", "YesCopyWithUnknownContent", EnglishCommonStrings.CANCEL, copyingBulletinWithUnknownContentMessage))
				return;
		}
	
		String currentBulletinName = bulletinToCopy.get(Bulletin.TAGTITLE);
		FxCopyItemNewNameController editNameController = new FxCopyItemNewNameController(getMainWindow(), currentBulletinName);
		if(showModalYesNoDialog("CopyItem", "CopyItem", EnglishCommonStrings.CANCEL, editNameController))
		{
			try
			{
				Bulletin copy = store.copyBulletinWithoutContacts(selectedId, editNameController.getNewItemName());
				BulletinFolder currentCase = bulletinTableProvider.getFolder();
				getApp().saveBulletin(copy, currentCase);
			} 
			catch (Exception e)
			{
				logAndNotifyUnexpectedError(e);
			}
		}
	}

	final private String VIEW_BULLETIN_IMAGE_PATH = "/org/martus/client/swingui/jfx/images/view_bulletin.png";
	final private String EDIT_BULLETIN_IMAGE_PATH = "/org/martus/client/swingui/jfx/images/edit_bulletin.png";
	
	@FXML 
	protected TableView<BulletinTableRowData> itemsTable;

	@FXML
	protected TableColumn<BulletinTableRowData, Boolean> onServerColumn;

	@FXML
	protected TableColumn<BulletinTableRowData, Integer> authorVerifiedColumn;

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
	private Button emptyTrashButton;
	
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
