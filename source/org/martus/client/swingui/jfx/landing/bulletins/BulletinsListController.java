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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.core.SortableBulletinList;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionMenuModifyFxBulletin;
import org.martus.client.swingui.jfx.landing.AbstractFxLandingContentController;
import org.martus.client.swingui.jfx.landing.FolderSelectionListener;
import org.martus.common.MartusLogger;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;

public class BulletinsListController extends AbstractFxLandingContentController implements FolderSelectionListener
{

	public BulletinsListController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		bulletinTableProvider = new BulletinListProvider(getApp());
	}

	@Override
	public void initializeMainContentPane()
	{
		onServerColumn.setCellValueFactory(new PropertyValueFactory<BulletinTableRowData, Boolean>(BulletinTableRowData.ON_SERVER_PROPERTY_NAME));
		onServerColumn.setCellFactory(CheckBoxTableCell.<BulletinTableRowData>forTableColumn(onServerColumn));
		authorColumn.setCellValueFactory(new PropertyValueFactory<BulletinTableRowData, String>(BulletinTableRowData.AUTHOR_PROPERTY_NAME));
		authorColumn.setCellFactory(TextFieldTableCell.<BulletinTableRowData>forTableColumn());
		titleColumn.setCellValueFactory(new PropertyValueFactory<BulletinTableRowData, String>(BulletinTableRowData.TITLE_PROPERTY_NAME));
		titleColumn.setCellFactory(TextFieldTableCell.<BulletinTableRowData>forTableColumn());
		dateSavedColumn.setCellValueFactory(new PropertyValueFactory<BulletinTableRowData, String>(BulletinTableRowData.DATE_SAVDED_PROPERTY_NAME));
		dateSavedColumn.setCellFactory(TextFieldTableCell.<BulletinTableRowData>forTableColumn());

		Label noBulletins = new Label(getLocalization().getFieldLabel("NoBulletinsInTable"));
		itemsTable.setPlaceholder(noBulletins);
		itemsTable.setItems(bulletinTableProvider);

		loadAllBulletinsAndSortByMostRecent();
	}

	@Override
	public void folderWasSelected(BulletinFolder folder)
	{
		loadBulletinData(folder.getAllUniversalIdsUnsorted());
	}

	public void loadAllBulletinsAndSortByMostRecent()
	{
		bulletinTableProvider.loadAllBulletins();
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
	    if(mouseEvent.getButton().equals(MouseButton.PRIMARY))
	    {
		    final int MOUSE_DOUBLE_CLICK = 2;
	    		if(mouseEvent.getClickCount() == MOUSE_DOUBLE_CLICK)
	        {
	            editBulletin();
	        }
	    }
	}
	
	
	@Override
	public String getFxmlLocation()
	{
		return "landing/bulletins/FxTableViewItems.fxml";
	}
	
	
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

	protected BulletinListProvider bulletinTableProvider;
}
