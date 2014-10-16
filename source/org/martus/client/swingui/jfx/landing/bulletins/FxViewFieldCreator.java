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

import java.util.Vector;

import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import org.martus.client.core.FxBulletin;
import org.martus.client.core.FxBulletinField;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.jfx.generic.controls.DateRangePicker;
import org.martus.client.swingui.jfx.generic.controls.MartusDatePicker;
import org.martus.client.swingui.jfx.generic.controls.NestedChoiceBox;
import org.martus.client.swingui.jfx.generic.data.ObservableChoiceItemList;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.MessageFieldSpec;

public class FxViewFieldCreator extends FxFieldCreator
{
	public FxViewFieldCreator(MartusLocalization localizationToUse)
	{
		super(localizationToUse);
	}

	@Override
	protected Node createDateField(FxBulletinField field)
	{
		MartusDatePicker picker = new MartusDatePicker(localization);
	
		if(field.getTag().equals(Bulletin.TAGENTRYDATE))
			picker.setDisable(true);
	
		Property<String> property = field.valueProperty();
		String existingDateString = property.getValue();
		picker.setValue(existingDateString);
		property.bind(picker.overallValueProperty());
		return picker;
	}

	@Override
	protected Node createDateRangeField(FxBulletinField field)
	{
		DateRangePicker picker = new DateRangePicker(localization);
	
		Property<String> property = field.valueProperty();
		String existingDateRangeString = property.getValue();
		picker.setValue(existingDateRangeString);
		property.bind(picker.valueProperty());
		
		return picker;
	}

	@Override
	protected Node createDropdownField(FxBulletin bulletin, FxBulletinField field) throws Exception
	{
		Vector<ObservableChoiceItemList> listOfChoiceItemLists = field.getChoiceItemLists();
		if(listOfChoiceItemLists.size() == 0)
			return createFieldNotAvailable();
	
		NestedChoiceBox choiceBoxes = new NestedChoiceBox();
		choiceBoxes.setChoiceItemLists(listOfChoiceItemLists);
	
		Property<String> property = field.valueProperty();
		choiceBoxes.setValue(property.getValue());
		property.bind(choiceBoxes.valueProperty());
	
		return choiceBoxes;
	}

	@Override
	protected Node createBooleanField(Property<String> property)
	{
		Text text = new Text(FieldTypeBoolean.getViewableData(property.getValue(), localization));
		return responsiveTextFlowNode(text);
	}

	@Override
	protected Node createMessageField(FieldSpec spec)
	{
		String messageText = ((MessageFieldSpec)(spec)).getMessage();
		Text text = new Text(messageText);
		return responsiveTextFlowNode(text);
	}

	@Override
	protected Node createStringField(Property<String> property)
	{
		return responsiveTextFlowNode(property);
	}

	@Override
	protected Node createMultilineField(Property<String> property)
	{
		return responsiveTextFlowNode(property);
	}

	@Override
	protected Node createFieldNotAvailable()
	{
		return new Label("(n/a)");
	}

	private Node responsiveTextFlowNode(Property<String> property)
	{
		return responsiveTextFlowNode(getText(property));
	}

	private Node responsiveTextFlowNode(Text text)
	{
		TextFlow flow = new TextFlow(text);
		flow.prefWidthProperty().bind(fieldWidthProperty);
		return flow;
	}

	private Text getText(Property<String> property)
	{
		Text text = new Text(property.getValue());
		return text;
	}
}
