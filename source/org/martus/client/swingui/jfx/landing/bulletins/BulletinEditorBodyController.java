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
import java.util.Vector;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import org.martus.client.core.FxBulletin;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.generic.data.BooleanStringConverter;
import org.martus.client.swingui.jfx.generic.data.ChoiceItemStringConverter;
import org.martus.common.MartusLogger;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.MessageFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;

public class BulletinEditorBodyController extends FxController
{
	public BulletinEditorBodyController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/bulletins/BulletinEditorBody.fxml";
	}

	public void showBulletin(FxBulletin bulletinToShow) throws RuntimeException
	{
		FxFormCreator creator = new FxFormCreator(getLocalization());
		Node root = creator.createFormFromBulletin(bulletinToShow);
		scrollPane.setContent(root);
	}
	
	protected static class FxFormCreator
	{
		public FxFormCreator(MartusLocalization localizationToUse)
		{
			localization = localizationToUse;
		}
		
		public Node createFormFromBulletin(FxBulletin bulletinToShow)
		{
			bulletin = bulletinToShow;
			root = new StackPane();
			
			Vector<FieldSpec> fieldSpecs = bulletin.getFieldSpecs();
			fieldSpecs.forEach(fieldSpec -> addField(fieldSpec));
			root.getChildren().add(currentSection);
			return root;
		}
		
		private void addField(FieldSpec fieldSpec)
		{
			if(shouldOmitField(fieldSpec))
				return;
			
			// FIXME: This should also create a new section when encountering a section spec
			if(currentSection == null)
				currentSection = new BulletinEditorSection(getLocalization());
			
			SimpleStringProperty property = bulletin.getFieldProperty(fieldSpec.getTag());
			currentSection.addField(fieldSpec, property);
		}

		private boolean shouldOmitField(FieldSpec spec)
		{
			Vector<String> tagsToOmit = new Vector<String>();
			tagsToOmit.add(Bulletin.TAGTITLE);
			tagsToOmit.add(Bulletin.TAGWASSENT);
			
			return tagsToOmit.contains(spec.getTag());
		}

		private MartusLocalization getLocalization()
		{
			return localization;
		}

		private MartusLocalization localization;
		private FxBulletin bulletin;
		private StackPane root;
		private BulletinEditorSection currentSection;
	}
	
	protected static class BulletinEditorSection extends GridPane
	{
		public BulletinEditorSection(MartusLocalization localizationToUse)
		{
			localization = localizationToUse;
			
			currentRowIndex = 0;
		}
		
		public void addField(FieldSpec fieldSpec, SimpleStringProperty property)
		{
			BulletinEditorRow currentRow = new BulletinEditorRow(getLocalization());
			currentRow.addFieldToRow(fieldSpec, property);
			Node label = currentRow.getLabelNode();
			Node fields = currentRow.getFieldsNode();
			if(label == null || fields == null)
				return;
			
			add(label, LABEL_COLUMN, currentRowIndex);
			add(fields, DATA_COLUMN, currentRowIndex);
			
			++currentRowIndex;
		}
		
		private MartusLocalization getLocalization()
		{
			return localization;
		}

		private static final int LABEL_COLUMN = 0;
		private static final int DATA_COLUMN = 1;

		private MartusLocalization localization;
		private int currentRowIndex;
	}
	
	protected static class BulletinEditorRow
	{
		public BulletinEditorRow(MartusLocalization localizationToUse)
		{
			localization = localizationToUse;
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
			labelNode = createLabel(fieldSpec);
			fieldsNode = createFieldForSpec(fieldSpec, property);
		}
		
		public Label createLabel(FieldSpec spec)
		{
			String tag = spec.getTag();
			String labelText = spec.getLabel();
			if(StandardFieldSpecs.isStandardFieldTag(tag))
				labelText = getLocalization().getFieldLabel(tag);
			Label label = new Label(labelText);
			return label;
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

			Property<ChoiceItem> choiceItemCodeProperty = createChoiceItemStringIntermediaryProperty(property, rawChoices);
			choiceBox.valueProperty().bindBidirectional(choiceItemCodeProperty);
			
			return choiceBox;
		}

		private Property<ChoiceItem> createChoiceItemStringIntermediaryProperty(SimpleStringProperty property, ChoiceItem[] rawChoices)
		{
			ChoiceItemStringConverter converter = new ChoiceItemStringConverter(rawChoices);
			ChoiceItem existingValue = converter.fromString(property.getValue());
			Property<ChoiceItem> choiceItemStringProperty = new SimpleObjectProperty<ChoiceItem>(existingValue);
			property.bindBidirectional(choiceItemStringProperty, converter);
			return choiceItemStringProperty;
		}

		private Node createBooleanField(SimpleStringProperty property)
		{
			SimpleBooleanProperty booleanProperty = createBooleanStringIntermediaryProperty(property);
			CheckBox checkBox = new CheckBox();
			checkBox.selectedProperty().bindBidirectional(booleanProperty);
			
			return checkBox;
		}

		private SimpleBooleanProperty createBooleanStringIntermediaryProperty(SimpleStringProperty property)
		{
			BooleanStringConverter booleanStringConverter = new BooleanStringConverter();
			Boolean existingValue = booleanStringConverter.fromString(property.getValue());
			SimpleBooleanProperty booleanProperty = new SimpleBooleanProperty(existingValue);
			property.bindBidirectional(booleanProperty, booleanStringConverter);
			return booleanProperty;
		}
		
		private Node createMessageField(FieldSpec spec)
		{
			String message = ((MessageFieldSpec)(spec)).getMessage();
			TextArea textArea = new TextArea(message);
			textArea.setPrefColumnCount(NORMAL_TEXT_FIELD_WIDTH_IN_CHARACTERS);
			textArea.setPrefRowCount(1);
			textArea.setFocusTraversable(false);
			textArea.setWrapText(true);
			textArea.setEditable(false);
			
			return textArea;
		}

		public Node createStringField(SimpleStringProperty property)
		{
			TextField textField = new TextField();
			textField.setPrefColumnCount(NORMAL_TEXT_FIELD_WIDTH_IN_CHARACTERS);
			textField.textProperty().bindBidirectional(property);
			
			return textField;
		}
		
		private Node createMultilineField(SimpleStringProperty property)
		{
			TextArea textArea = new TextArea();
			textArea.setPrefColumnCount(NORMAL_TEXT_FIELD_WIDTH_IN_CHARACTERS);
			textArea.setPrefRowCount(MULTILINE_FIELD_HEIGHT_IN_ROWS);
			textArea.setWrapText(true);
			textArea.textProperty().bindBidirectional(property);
			
			return textArea;
		}
		
		private Node createFieldNotAvailable()
		{
			return new Label("*");
		}

		private MartusLocalization getLocalization()
		{
			return localization;
		}

		private static final int NORMAL_TEXT_FIELD_WIDTH_IN_CHARACTERS = 60;
		private static final int MULTILINE_FIELD_HEIGHT_IN_ROWS = 5;

		private MartusLocalization localization;
		private Node labelNode;
		private Node fieldsNode;
	}

	public void scrollToTop()
	{
		scrollPane.vvalueProperty().set(0);
	}

	@FXML
	private ScrollPane scrollPane;
	
}
