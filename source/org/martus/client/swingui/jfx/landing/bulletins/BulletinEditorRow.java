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

import javafx.beans.binding.When;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import org.martus.client.core.FxBulletin;
import org.martus.client.swingui.MartusLocalization;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;

public class BulletinEditorRow
{
	public BulletinEditorRow(FxBulletin bulletinToUse, MartusLocalization localizationToUse)
	{
		bulletin = bulletinToUse;
		localization = localizationToUse;
		
		fieldCreator = new FxFieldCreator();
		
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
	
	public void addFieldToRow(FieldSpec fieldSpec, Property<String> fieldValueProperty, ObservableBooleanValue isValidProperty) throws Exception
	{
		Node label = createLabel(fieldSpec);
		addValidationBorder(isValidProperty, label);
		HBox.setHgrow(label, Priority.ALWAYS);
		getLabelDestination().getChildren().add(label);
		
		Node fieldNode = fieldCreator.createFieldForSpec(bulletin, fieldSpec, fieldValueProperty);
		addValidationBorder(isValidProperty, fieldNode);
		fieldsNode.getChildren().add(fieldNode);
	}

	public void addValidationBorder(ObservableBooleanValue isValidProperty,	Node node)
	{
		if(isValidProperty == null)
			return;
		
		// FIXME: This really should be done with a listener that updates the css style class, 
		// and then calls applyCss(), but I'm not sure how to replace one style with another.
		// So for now, this will have to do. 
		node.styleProperty().bind(new When(isValidProperty).then("").otherwise("-fx-border-color: red;"));
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
	
	private MartusLocalization getLocalization()
	{
		return localization;
	}

	private FxBulletin bulletin;
	private MartusLocalization localization;
	private FxFieldCreator fieldCreator;
	private HBox labelNode;
	private HBox fieldsNode;
}