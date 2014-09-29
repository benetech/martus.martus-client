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
package org.martus.client.swingui.jfx.generic.controls;

import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.martus.client.swingui.actions.ActionDoer;

public class FxButtonTableCell extends TableCell
{
	public FxButtonTableCell(Image buttonImageToUse, ActionDoer doerToUse)
	{
		buttonImage = buttonImageToUse;
		doer = doerToUse;
	}

	@Override
	public void updateItem(Object cellObject, boolean empty) 
	{
		super.updateItem(cellObject, empty);
		
		Button button = null;
		
		boolean isValidRow = !empty;
		boolean doesRowSupportButtonAction = isValidRow && ((Boolean)cellObject).booleanValue();
		if (doesRowSupportButtonAction) 
		{
			button = new Button(null, new ImageView(buttonImage));

			FxTableCellButtonActionHandler handler = new FxTableCellButtonActionHandler(getTableView(), doer);
			handler.setTableRowIndex(getIndex());
			button.setOnAction(handler);
		}

		setText(null);
		setGraphic(button);
	}
	
	private Image buttonImage;
	private ActionDoer doer;
}
