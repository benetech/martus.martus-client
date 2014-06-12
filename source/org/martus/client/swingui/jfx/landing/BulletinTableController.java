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
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.UiMainWindow;
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
		itemsTable.setItems(data);
		
		Label noBulletins = new Label(getLocalization().getFieldLabel("NoBulletinsInTable"));
		itemsTable.setPlaceholder(noBulletins);
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
		ClientBulletinStore clientBulletinStore = getApp().getStore();
		Set allBulletinUids = clientBulletinStore.getAllBulletinLeafUids();
		MiniLocalization localization = getLocalization();
		for(Iterator iter = allBulletinUids.iterator(); iter.hasNext();)
		{
			UniversalId leafBulletinUid = (UniversalId) iter.next();
			Bulletin bulletin = clientBulletinStore.getBulletinRevision(leafBulletinUid);
			boolean onServer = clientBulletinStore.isProbablyOnServer(leafBulletinUid);
			BulletinTableData bulletinData = new BulletinTableData(bulletin, onServer, localization);
			data.add(bulletinData);		
		}
		sortByMostRecentBulletins();
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
