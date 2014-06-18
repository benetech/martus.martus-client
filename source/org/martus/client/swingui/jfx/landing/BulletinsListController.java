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
package org.martus.client.swingui.jfx.landing;

import java.util.Iterator;
import java.util.Set;

import javafx.collections.FXCollections;
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

import javax.swing.SwingUtilities;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.SortableBulletinList;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionMenuModifyFxBulletin;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;

public class BulletinsListController extends AbstractFxLandingContentController
{

	public BulletinsListController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	@Override
	public void initializeMainContentPane()
	{
		onServerColumn.setCellValueFactory(new PropertyValueFactory<BulletinTableData, Boolean>(BulletinTableData.ON_SERVER_PROPERTY_NAME));
		onServerColumn.setCellFactory(CheckBoxTableCell.<BulletinTableData>forTableColumn(onServerColumn));
		authorColumn.setCellValueFactory(new PropertyValueFactory<BulletinTableData, String>(BulletinTableData.AUTHOR_PROPERTY_NAME));
		authorColumn.setCellFactory(TextFieldTableCell.<BulletinTableData>forTableColumn());
		titleColumn.setCellValueFactory(new PropertyValueFactory<BulletinTableData, String>(BulletinTableData.TITLE_PROPERTY_NAME));
		titleColumn.setCellFactory(TextFieldTableCell.<BulletinTableData>forTableColumn());
		dateSavedColumn.setCellValueFactory(new PropertyValueFactory<BulletinTableData, String>(BulletinTableData.DATE_SAVDED_PROPERTY_NAME));
		dateSavedColumn.setCellFactory(TextFieldTableCell.<BulletinTableData>forTableColumn());

		Label noBulletins = new Label(getLocalization().getFieldLabel("NoBulletinsInTable"));
		itemsTable.setPlaceholder(noBulletins);
		itemsTable.setItems(data);
		loadBulletinData();
	}

	private void sortByMostRecentBulletins()
	{
		dateSavedColumn.setSortType(SortType.DESCENDING);
		itemsTable.getSortOrder().add(dateSavedColumn);
	}

	protected void loadBulletinData()
	{
		data.clear();
		Set allBulletinUids = getApp().getStore().getAllBulletinLeafUids();
		for(Iterator iter = allBulletinUids.iterator(); iter.hasNext();)
		{
			UniversalId leafBulletinUid = (UniversalId) iter.next();
			BulletinTableData bulletinData = getCurrentBulletinData(leafBulletinUid);
			data.add(bulletinData);		
		}
		sortByMostRecentBulletins();
	}

	protected BulletinTableData getCurrentBulletinData(UniversalId leafBulletinUid)
	{
		ClientBulletinStore clientBulletinStore = getApp().getStore();
		Bulletin bulletin = clientBulletinStore.getBulletinRevision(leafBulletinUid);
		boolean onServer = clientBulletinStore.isProbablyOnServer(leafBulletinUid);
		MiniLocalization localization = getLocalization();
		BulletinTableData bulletinData = new BulletinTableData(bulletin, onServer, localization);
		return bulletinData;
	}
	
	protected void editBulletin()
	{
		TableViewSelectionModel<BulletinTableData> selectionModel = itemsTable.getSelectionModel();
		UniversalId bulletinUid = selectionModel.getSelectedItem().getUniversalId();
		Bulletin bulletinSelected = getApp().getStore().getBulletinRevision(bulletinUid);
		getShellController().getStage().doAction(new ActionMenuModifyFxBulletin(getMainWindow(), bulletinSelected));
	}

	public void updateSearchResultsTable(SortableBulletinList searchResults)
	{
		SwingUtilities.invokeLater(new UpdateSearchResultsHandler(searchResults));
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
			data.clear();
			UniversalId[] foundUids = results.getUniversalIds();
			for (int i = 0; i < foundUids.length; i++)
			{
				BulletinTableData bulletinData = getCurrentBulletinData(foundUids[i]);
				data.add(bulletinData);		
			}
			itemsTable.sort();
		}
		
		private SortableBulletinList results;
	}
	
	
	public void bulletinContentsHaveChanged(Bulletin bulletinUpdated)
	{
		SwingUtilities.invokeLater(new BulletinTableChangeHandler(bulletinUpdated));
	}
	
	private class BulletinTableChangeHandler implements Runnable
	{
		public BulletinTableChangeHandler(Bulletin bulletinToUpdate)
		{
			bulletin = bulletinToUpdate;
		}
		
		public void run()
		{
			UniversalId bulletinId = bulletin.getUniversalId();
			BulletinTableData updatedBulletinData = getCurrentBulletinData(bulletinId);
			int bulletinIndexInTable = getBulletinIndexInTable(bulletinId);
			if(bulletinIndexInTable == BULLETIN_NOT_IN_TABLE)
			{
				loadBulletinData();
			}
			else
			{
				data.set(bulletinIndexInTable, updatedBulletinData);
			}
		}
		public Bulletin bulletin;
	}
	
	protected int getBulletinIndexInTable(UniversalId id)
	{
		for (int currentIndex = 0; currentIndex < data.size(); currentIndex++)
		{
			if(id.equals(data.get(currentIndex).getUniversalId()))
				return currentIndex;
		}
		return BULLETIN_NOT_IN_TABLE;
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
		return "landing/FxTableViewItems.fxml";
	}
	
	final int BULLETIN_NOT_IN_TABLE = -1;
	
	@FXML 
	protected TableView<BulletinTableData> itemsTable;

	@FXML
	protected TableColumn<BulletinTableData, Boolean> onServerColumn;

	@FXML
	protected TableColumn<BulletinTableData, String> authorColumn;

	@FXML
	protected TableColumn<BulletinTableData, String> titleColumn;

	@FXML
	protected TableColumn<BulletinTableData, String> dateSavedColumn;	

	protected ObservableList<BulletinTableData> data = FXCollections.observableArrayList();
}
