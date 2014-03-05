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
//Original code found at http://stackoverflow.com/questions/7880494/tableview-better-editing-through-binding

package org.martus.client.swingui.jfx;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.util.Callback;

import org.martus.client.swingui.jfx.setupwizard.ContactsTableData;

public final class FxTableCellTextFieldFactory  
	implements Callback<TableColumn<ContactsTableData,String>,TableCell<ContactsTableData,String>> 
{
	
	@Override
	public TableCell<ContactsTableData, String> call(TableColumn<ContactsTableData, String> param) 
	{
	   TextFieldCell textFieldCell = new TextFieldCell();
	   return textFieldCell;
	}
	
	public static class TextFieldCell extends TableCell<ContactsTableData,String> 
	{
	   private TextField textField;
	   private StringProperty cellStringPropertyBoundToCurrently = null;
	   public TextFieldCell() 
	   {
	   		textField = new TextField();
	   		this.setGraphic(textField);
	   }
	   
		@Override
		protected void updateItem(String item, boolean empty) 
		{
			super.updateItem(item, empty);        
			if(!empty)
			{
				// Show the Text Field
				this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		
				// Retrieve the actual String Property that should be bound to the TextField
				// If the TextField is currently bound to a different StringProperty
				// Unbind the old property and rebind to the new one
				//NOTE: To use this TextField Factory the TableData's SimpleStringProperty must 
				//      be implemented.  IE:
				//		public SimpleStringProperty <variableName>Property() { return <variableName>; }
                //		without that a cast exception will occur.
				ObservableValue<String> cellObservableValue = getTableColumn().getCellObservableValue(getIndex());
				SimpleStringProperty cellsStringProperty = (SimpleStringProperty)cellObservableValue;
		    
				if(this.cellStringPropertyBoundToCurrently==null) 
				{
					this.cellStringPropertyBoundToCurrently = cellsStringProperty;
					this.textField.textProperty().bindBidirectional(cellsStringProperty);
				}
				else
				{
					if(this.cellStringPropertyBoundToCurrently != cellsStringProperty) 
					{
						this.textField.textProperty().unbindBidirectional(this.cellStringPropertyBoundToCurrently);
						this.cellStringPropertyBoundToCurrently = cellsStringProperty;
						this.textField.textProperty().bindBidirectional(this.cellStringPropertyBoundToCurrently);
					}
				}
			}
			else 
			{
				this.setContentDisplay(ContentDisplay.TEXT_ONLY);
			}
		}
	}
}
