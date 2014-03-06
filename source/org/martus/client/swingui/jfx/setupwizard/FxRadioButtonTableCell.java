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
package org.martus.client.swingui.jfx.setupwizard;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.ToggleGroup;

public class FxRadioButtonTableCell extends TableCell
{
	public FxRadioButtonTableCell(ToggleGroup groupToUse)
	{
		group = groupToUse;
		radioButton = new RadioButton();
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
			radioButton.setToggleGroup(group);
			bindToOurBooleanPropertyRadioButton();
			setGraphic(radioButton);
		}
	}

	private void bindToOurBooleanPropertyRadioButton()
	{
		ObservableValue<Boolean> cellObservableValue = getTableColumn().getCellObservableValue(getIndex());
		SimpleBooleanProperty cellBooleanProperty = (SimpleBooleanProperty)cellObservableValue;

		if(cellBooleanProperty == cellBooleanPropertyBoundToCurrently)
			return;
		
		if(cellBooleanProperty.equals(cellBooleanPropertyBoundToCurrently))
			return;
		
		if(cellBooleanPropertyBoundToCurrently != cellBooleanProperty) 
			radioButton.selectedProperty().unbindBidirectional(cellBooleanProperty);

		cellBooleanPropertyBoundToCurrently = cellBooleanProperty;
		radioButton.selectedProperty().bindBidirectional(cellBooleanPropertyBoundToCurrently);
	}

	private ToggleGroup group;
	private RadioButton radioButton;
	private BooleanProperty cellBooleanPropertyBoundToCurrently;
}
