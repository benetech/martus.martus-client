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
package org.martus.client.swingui.jfx.landing;

import java.util.Iterator;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.SortableBulletinList;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.bulletintable.UiBulletinHelper;
import org.martus.common.MartusLogger;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;

public class BulletinTableController extends AbstractFxLandingContentController
{

	public BulletinTableController(UiMainWindow mainWindowToUse)
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
		itemsTable.setOnMouseClicked(new TableMouseEventHandler());		
		try
		{
			loadBulletinData();
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			throw new RuntimeException();
		}
	}

	private void sortByMostRecentBulletins()
	{
		dateSavedColumn.setSortType(SortType.DESCENDING);
		itemsTable.getSortOrder().add(dateSavedColumn);
	}

	private void loadBulletinData() throws Exception
	{
		data.clear();
		Set allBulletinUids = getApp().getStore().getAllBulletinLeafUids();
		for(Iterator iter = allBulletinUids.iterator(); iter.hasNext();)
		{
			UniversalId leafBulletinUid = (UniversalId) iter.next();
			BulletinTableData bulletinData = getUpdatedBulletinData(leafBulletinUid);
			data.add(bulletinData);		
		}
		sortByMostRecentBulletins();
	}

	private BulletinTableData getUpdatedBulletinData(UniversalId leafBulletinUid)
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
		BulletinTableData selectedBulletinData = itemsTable.getSelectionModel().getSelectedItem();
		UniversalId bulletinUid = selectedBulletinData.getUniversalId();
		Bulletin bulletinSelected = getApp().getStore().getBulletinRevision(bulletinUid);
		UiBulletinHelper bulletinHelper = new UiBulletinHelper(getMainWindow());
		//FIXME: If this function has to bring up a confirmation dialog (ie.to clone a sealed bulletin) the UI will freeze 
		bulletinHelper.doModifyBulletin(bulletinSelected);
	}

	//TODO this needs to be called from ActionMenuSearch but must execute within an FX application thread
	public void updateSearchResultsTable(SortableBulletinList searchResults)
	{
		data.clear();
		UniversalId[] foundUids = searchResults.getUniversalIds();
		for (int i = 0; i < foundUids.length; i++)
		{
			BulletinTableData bulletinData = getUpdatedBulletinData(foundUids[i]);
			data.add(bulletinData);		
		}
		itemsTable.sort();
	}
	
	//TODO this needs to be called from UiMainWindow but must execute within an FX application thread
	public void bulletinContentsHaveChanged(Bulletin bulletinUpdated)
	{
		int index = itemsTable.getSelectionModel().getSelectedIndex();
		BulletinTableData updatedBulletinData = getUpdatedBulletinData(bulletinUpdated.getUniversalId());
		data.set(index, updatedBulletinData);
		itemsTable.sort();
	}
	
	private final class TableMouseEventHandler implements EventHandler<MouseEvent>
	{
		private static final int MOUSE_DOUBLE_CLICK = 2;

		public TableMouseEventHandler()
		{
		}

		@Override
		public void handle(MouseEvent mouseEvent) 
		{
		    if(mouseEvent.getButton().equals(MouseButton.PRIMARY))
		    {
		    		if(mouseEvent.getClickCount() == MOUSE_DOUBLE_CLICK)
		        {
		            editBulletin();
		        }
		    }
		}
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/FxTableViewItems.fxml";
	}
	
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
