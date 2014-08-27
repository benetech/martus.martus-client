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
package org.martus.client.swingui.jfx.landing.bulletins;

import java.awt.event.ActionListener;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;


final class ViewEditBulletinTableColumnButton implements Callback<TableColumn<BulletinTableRowData, String>, TableCell<BulletinTableRowData, String>>
{
	public ViewEditBulletinTableColumnButton(ActionListener listenerToUse, String pathToButtonImage)
	{
		super();
		listener = listenerToUse;
	    buttonImagePath = pathToButtonImage; 
	}

	final class ButtonCellUpdateHandler extends TableCell
	{
		
		ButtonCellUpdateHandler(TableColumn tableColumn)
		{
			this.tableColumn = tableColumn;
		}
		
		final class ViewEditBulletinHandler implements EventHandler<ActionEvent>
		{
			@Override
			public void handle(ActionEvent event) 
			{
				tableColumn.getTableView().getSelectionModel().clearAndSelect(getIndex());
				notifyListener();
			}
		}
		
		protected void notifyListener()
		{
			listener.actionPerformed(null);
		}
		
		@Override
		public void updateItem(Object item, boolean empty) 
		{
		    super.updateItem(item, empty);
		    if (empty) 
		    {
		        setText(null);
		        setGraphic(null);
		    } 
		    else 
		    {
		    	
		        final Button button = new Button("", new ImageView(new Image(buttonImagePath)));
		        button.getStyleClass().add(BULLETIN_VIEW_EDIT_BUTTON_CSS_STYLE);
		        button.setOnAction(new ViewEditBulletinHandler());
		        setGraphic(button);
		    	}
		}
		protected final TableColumn tableColumn;
	}

	@Override
	public TableCell call(final TableColumn param) 
	{
		return new ButtonCellUpdateHandler(param);
	}
	
	final private String BULLETIN_VIEW_EDIT_BUTTON_CSS_STYLE = "";
	protected ActionListener listener;
	protected String buttonImagePath; 

}
