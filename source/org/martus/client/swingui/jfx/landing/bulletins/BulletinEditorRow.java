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
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleStringProperty;
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

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.jfx.generic.controls.ScrollFreeTextArea;
import org.martus.client.swingui.jfx.generic.data.BooleanStringConverter;
import org.martus.client.swingui.jfx.generic.data.ChoiceItemStringConverter;
import org.martus.common.MartusLogger;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.MessageFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;

public class BulletinEditorRow
{
	public BulletinEditorRow(MartusLocalization localizationToUse)
	{
		localization = localizationToUse;
		
		labelNode = new HBox();
		labelNode.getStyleClass().add("bulletin-editor-label-cell");
		fieldsNode = new HBox();
		fieldsNode.getStyleClass().add("bulletin-editor-field-cell");
	}
	
	public Node getLabelNode()
	{
		return labelNode;
	}
	
	public Node getFieldsNode()
	{
		return fieldsNode;
	}
	
	public void addFieldToRow(FieldSpec fieldSpec, SimpleStringProperty property)
	{
		Node label = createLabel(fieldSpec);
		HBox.setHgrow(label, Priority.ALWAYS);
		getLabelDestination().getChildren().add(label);
		
		Node fieldNode = createFieldForSpec(fieldSpec, property);
		fieldsNode.getChildren().add(fieldNode);
	}

	public HBox getLabelDestination()
	{
		if(labelNode.getChildren().isEmpty())
			return labelNode;
		
		return fieldsNode;
	}
	
	public Node createLabel(FieldSpec spec)
	{
		String tag = spec.getTag();
		String labelText = spec.getLabel();
		if(StandardFieldSpecs.isStandardFieldTag(tag))
			labelText = getLocalization().getFieldLabel(tag);
		Text text = new Text(labelText);
		TextFlow flow = new TextFlow(text);
		if(spec.isRequiredField())
		{
			Label asterisk = new Label("*");
			asterisk.getStyleClass().add("requiredAsterisk");
			flow.getChildren().add(asterisk);
		}
		return flow;
	}
	
	private Node createFieldForSpec(FieldSpec spec, SimpleStringProperty property)
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
		
		return createFieldNotAvailable();
	}

	private Node createDropdownField(SimpleStringProperty property, FieldSpec rawSpec)
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

	private Node createBooleanField(SimpleStringProperty property)
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

	public Node createStringField(SimpleStringProperty property)
	{
		ScrollFreeTextArea textField = new ScrollFreeTextArea();
		textField.textProperty().bindBidirectional(property);
		HBox.setHgrow(textField, Priority.SOMETIMES);
		
		return textField;
	}
	
	private Node createMultilineField(SimpleStringProperty property)
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

	private MartusLocalization getLocalization()
	{
		return localization;
	}

	private static final int MINIMUM_REASONABLE_COLUMN_COUNT = 10;
	private static final int MULTILINE_FIELD_HEIGHT_IN_ROWS = 5;

	private MartusLocalization localization;
	private HBox labelNode;
	private HBox fieldsNode;
}