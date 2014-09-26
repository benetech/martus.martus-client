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

import java.util.Arrays;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
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
import org.martus.common.MartusLogger;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.MessageFieldSpec;

public class FxFieldCreator
{
	public Node createFieldForSpec(FxBulletin bulletin, FieldSpec spec, Property<String> property)
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
			return createDropdownField(property, spec);
		
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

	private Node createDropdownField(Property<String> property, FieldSpec rawSpec)
	{
		DropDownFieldSpec spec = (DropDownFieldSpec) rawSpec;
		String dataSourceGridTag = spec.getDataSourceGridTag();
		if(dataSourceGridTag != null && dataSourceGridTag.length() > 0)
		{
			MartusLogger.log("Skipping DataDrivenDropDown");
			return createFieldNotAvailable();
		}
		
		String[] reusableChoicesCodes = spec.getReusableChoicesCodes();
		if(reusableChoicesCodes != null && reusableChoicesCodes.length > 0)
		{
			MartusLogger.log("Skipping ReusableChoicesDropDown");
			return createFieldNotAvailable();
		}

		ChoiceItem[] rawChoices = spec.getAllChoices();
		List<ChoiceItem> choicesList = Arrays.asList(rawChoices);
		ObservableList<ChoiceItem> choices = FXCollections.observableArrayList();
		choices.addAll(choicesList);
		ChoiceBox<ChoiceItem> choiceBox = new ChoiceBox<ChoiceItem>(choices);
		String currentSelectedCode = property.getValue();
		ChoiceItem currentSelectedItem = new ChoiceItemStringConverter(rawChoices).fromString(currentSelectedCode);
		choiceBox.getSelectionModel().select(currentSelectedItem);

		ReadOnlyObjectProperty<ChoiceItem> selectedItemProperty = choiceBox.getSelectionModel().selectedItemProperty();
		selectedItemProperty.addListener(
			(observable, oldValue, newValue) ->	property.setValue(newValue.getCode())
		);
		return choiceBox;
	}

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
