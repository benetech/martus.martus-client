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

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import org.martus.client.core.FxBulletin;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.fieldspec.FieldSpec;
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
		fieldsGrid.getChildren().clear();
		
		Vector<FieldSpec> fieldSpecs = bulletinToShow.getFieldSpecs();
		for(int row = 0; row < fieldSpecs.size(); ++row)
		{
			FieldSpec spec = fieldSpecs.get(row);
			if(shouldOmitField(spec))
				continue;

			String tag = spec.getTag();
			SimpleStringProperty property = bulletinToShow.getFieldProperty(tag);
			createFieldForSpec(row, spec, property);
		}
	}

	public void scrollToTop()
	{
		scrollPane.vvalueProperty().set(0);
	}

	private boolean shouldOmitField(FieldSpec spec)
	{
		Vector<String> tagsToOmit = new Vector<String>();
		tagsToOmit.add(Bulletin.TAGTITLE);
		tagsToOmit.add(Bulletin.TAGWASSENT);
		
		return tagsToOmit.contains(spec.getTag());
	}

	private void createFieldForSpec(int row, FieldSpec spec, SimpleStringProperty property)
	{
		String tag = spec.getTag();
		String labelText = spec.getLabel();
		if(StandardFieldSpecs.isStandardFieldTag(tag))
			labelText = getLocalization().getFieldLabel(tag);
		Label label = new Label(labelText + ":");
		final int LABEL_COLUMN = 0;
		final int DATA_COLUMN = 1;
		fieldsGrid.add(label, LABEL_COLUMN, row);
		if(spec.getType().isString())
		{
			TextField textField = new TextField();
			textField.textProperty().bindBidirectional(property);
			fieldsGrid.add(textField, DATA_COLUMN, row);
		}
	}

	@FXML
	private ScrollPane scrollPane;
	
	@FXML
	private GridPane fieldsGrid;

}
