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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import org.martus.client.core.FxBulletin;
import org.martus.client.swingui.jfx.generic.controls.ScrollFreeTextArea;
import org.martus.client.swingui.jfx.generic.data.BooleanStringConverter;
import org.martus.client.swingui.jfx.generic.data.ChoiceItemStringConverter;
import org.martus.client.swingui.jfx.generic.data.ObservableChoiceItemList;
import org.martus.common.MartusLogger;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.MessageFieldSpec;

public class FxFieldCreator
{
	public Node createFieldForSpec(FxBulletin bulletin, FieldSpec spec, Property<String> property) throws Exception
	{
		if(spec.getType().isString())
			return createStringField(property);
		
		if(spec.getType().isMultiline())
			return createMultilineField(property);
		
		if(spec.getType().isMessage())
			return createMessageField(spec);
		
		if(spec.getType().isBoolean())
			return createBooleanField(property);
		
		if(spec.getType().isDropdown())
			return createDropdownField(bulletin, property, spec);
		
		if(spec.getType().isDate())
			return createDateField(property, spec);
		
		return createFieldNotAvailable();
	}

	private Node createDateField(Property<String> property, FieldSpec spec)
	{
		return createFieldNotAvailable();
		// FIXME: The following is spike code which will either become real, or be deleted
//		DatePicker picker = new DatePicker();
//		picker.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
//		picker.getStylesheets().add("org/martus/client/swingui/jfx/generic/controls/DatePicker.css");
//		return picker;
	}

	private Node createDropdownField(FxBulletin bulletin, Property<String> property, FieldSpec rawSpec) throws Exception
	{
		DropDownFieldSpec spec = (DropDownFieldSpec) rawSpec;
		String dataSourceGridTag = spec.getDataSourceGridTag();
		if(dataSourceGridTag != null && dataSourceGridTag.length() > 0)
		{
			MartusLogger.log("Skipping DataDrivenDropDown");
			return createFieldNotAvailable();
		}
		
		if(spec.hasReusableCodes())
			return createNestedDropDown(bulletin, spec);

		return createSingleDropDown(bulletin, property, spec);
	}

	private Node createSingleDropDown(FxBulletin bulletin, Property<String> property, DropDownFieldSpec spec) throws Exception
	{
		Vector<ObservableChoiceItemList> listOfChoiceItemLists = bulletin.getChoiceItemLists(spec.getTag());
		ObservableChoiceItemList choiceItemList = listOfChoiceItemLists.get(0);
		ChoiceBox choiceBox = createSingleDropDown(property, choiceItemList);
		ReadOnlyObjectProperty<ChoiceItem> selectedItemProperty = choiceBox.getSelectionModel().selectedItemProperty();
		selectedItemProperty.addListener(
			(observable, oldValue, newValue) ->	property.setValue(newValue.getCode())
		);
		return choiceBox;
	}

	private ChoiceBox createSingleDropDown(Property<String> property, ObservableList<ChoiceItem> choices)
	{
		ChoiceBox<ChoiceItem> choiceBox = new ChoiceBox<ChoiceItem>(choices);
		String currentSelectedCode = property.getValue();
		ChoiceItem[] choicesAsArray = choices.toArray(new ChoiceItem[0]);
		ChoiceItemStringConverter converter = new ChoiceItemStringConverter(choicesAsArray);
		ChoiceItem currentSelectedItem = converter.fromString(currentSelectedCode);
		choiceBox.getSelectionModel().select(currentSelectedItem);

		return choiceBox;
	}

	private Node createNestedDropDown(FxBulletin bulletin, DropDownFieldSpec spec)
	{
		return createFieldNotAvailable();
		// FIXME: The following is work in progress, so disabled for now
//		HBox nestedDropDown = new HBox();
//		for(int level = 0; level < spec.getReusableChoicesCodes().length; ++level)
//		{
//			SimpleStringProperty thisProperty = new SimpleStringProperty();
//			Node dropDown = createSingleDropDown(thisProperty, getReusableChoices(bulletin, spec, level));
//			nestedDropDown.getChildren().add(dropDown);
//		}
//		return nestedDropDown;
	}

	// FIXME: The following is work in progress, so disabled for now
//	private ObservableList<ChoiceItem> getReusableChoices(FxBulletin bulletin, DropDownFieldSpec spec, int level)
//	{
//		String tag = spec.getReusableChoicesCodes()[level];
//		ObservableList<ChoiceItem> choices = bulletin.reusableChoicesProperty(tag);
//		return choices;
//	}

	private Node createBooleanField(Property<String> property)
	{
		CheckBox checkBox = new CheckBox();
		BooleanStringConverter converter = new BooleanStringConverter();
		checkBox.selectedProperty().setValue(converter.fromString(property.getValue()));

		BooleanProperty selectedStateProperty = checkBox.selectedProperty();
		selectedStateProperty.addListener(
			(observable, oldValue, newValue) -> property.setValue(converter.toString(newValue))
		);
		return checkBox;
	}

	private Node createMessageField(FieldSpec spec)
	{
		String messageText = ((MessageFieldSpec)(spec)).getMessage();
		Text text = new Text(messageText);
		TextFlow flow = new TextFlow(text);
		flow.getStyleClass().add("messageText");
		return flow;
	}

	public Node createStringField(Property<String> property)
	{
		ScrollFreeTextArea textField = new ScrollFreeTextArea();
		textField.textProperty().bindBidirectional(property);
		HBox.setHgrow(textField, Priority.SOMETIMES);
		
		return textField;
	}
	
	private Node createMultilineField(Property<String> property)
	{
		TextArea textArea = new TextArea();
		textArea.setPrefColumnCount(MINIMUM_REASONABLE_COLUMN_COUNT);
		textArea.setPrefRowCount(MULTILINE_FIELD_HEIGHT_IN_ROWS);
		textArea.setWrapText(true);
		textArea.textProperty().bindBidirectional(property);
		HBox.setHgrow(textArea, Priority.SOMETIMES);
		
		return textArea;
	}
	
	private Node createFieldNotAvailable()
	{
		return new Label("(n/a)");
	}

	private static final int MINIMUM_REASONABLE_COLUMN_COUNT = 10;
	private static final int MULTILINE_FIELD_HEIGHT_IN_ROWS = 5;
}
