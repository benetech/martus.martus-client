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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import org.martus.client.swingui.UiMainWindow;
import org.martus.common.MartusLogger;
import org.martus.common.bulletin.Bulletin;

public class BulletinTableController extends AbstractFxLandingContentController
{

	public BulletinTableController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	@Override
	public void initializeMainContentPane()
	{
		onServerColumn.setCellValueFactory(new PropertyValueFactory<BulletinTableData, Boolean>("onServer"));
		onServerColumn.setCellFactory(CheckBoxTableCell.<BulletinTableData>forTableColumn(onServerColumn));
		authorColumn.setCellValueFactory(new PropertyValueFactory<Object, String>("author"));
		authorColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		titleColumn.setCellValueFactory(new PropertyValueFactory<Object, String>("title"));
		titleColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		dateSavedColumn.setCellValueFactory(new PropertyValueFactory<Object, String>("dateSaved"));
		dateSavedColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		itemsTable.setItems(data);
		try
		{
			loadBulletinData();
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			throw new RuntimeException();
		}

		Label noBulletins = new Label(getLocalization().getFieldLabel("NoBulletinsInTable"));
		itemsTable.setPlaceholder(noBulletins);
	}

	private void loadBulletinData() throws Exception
	{
		data.clear();
		//TODO Use Real Bulletins
		Bulletin test = new Bulletin(getApp().getSecurity());
		test.set(Bulletin.TAGTITLE, "Foosball just a game?");
		test.set(Bulletin.TAGAUTHOR, "Chuck");
		test.getBulletinHeaderPacket().updateLastSavedTime();
		BulletinTableData bulletinData = new BulletinTableData(test, true, getLocalization()); 
		data.add(bulletinData);
		test.set(Bulletin.TAGTITLE, "How to score with your goalie");
		test.set(Bulletin.TAGAUTHOR, "Charles");
		test.getBulletinHeaderPacket().updateLastSavedTime();
		BulletinTableData bulletinData2 = new BulletinTableData(test, false, getLocalization()); 
		data.add(bulletinData2);
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
	protected TableColumn<Object, String> authorColumn;

	@FXML
	protected TableColumn<Object, String> titleColumn;

	@FXML
	protected TableColumn<Object, String> dateSavedColumn;	

	protected ObservableList<BulletinTableData> data = FXCollections.observableArrayList();
	
}
