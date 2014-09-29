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
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import org.martus.client.core.FxBulletin;
import org.martus.client.swingui.MartusLocalization;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;

public class BulletinEditorSection extends GridPane
{
	public BulletinEditorSection(FxBulletin bulletinToUse, MartusLocalization localizationToUse, String sectionTitle)
	{
		bulletin = bulletinToUse;
		localization = localizationToUse;
		title = sectionTitle;
		
		getStyleClass().add("bulletin-editor-grid");
		
		rows = new Vector<BulletinEditorRow>();
		
		ColumnConstraints labelColumnConstraints= new ColumnConstraints();
		labelColumnConstraints.fillWidthProperty().setValue(true);
		labelColumnConstraints.setMinWidth(200);

		ColumnConstraints fieldColumnConstraints = new ColumnConstraints();
		fieldColumnConstraints.fillWidthProperty().setValue(true);
		fieldColumnConstraints.hgrowProperty().set(Priority.ALWAYS);

		getColumnConstraints().add(labelColumnConstraints);
		getColumnConstraints().add(fieldColumnConstraints);
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public void addField(FieldSpec fieldSpec, Property<String> fieldValueProperty, ObservableBooleanValue isValidProperty) throws Exception
	{
		boolean wantsKeepWithPrevious = fieldSpec.keepWithPrevious();
		boolean canKeepWithPrevious = canKeepWithNextOrPrevious(fieldSpec);
		boolean keepWithPrevious = (wantsKeepWithPrevious && canKeepWithPrevious);
		if(!keepWithPrevious)
			endCurrentRow();
			
		if(currentRow == null)
		{
			currentRow = new BulletinEditorRow(bulletin, getLocalization());
			rows.add(currentRow);
		}
		
		currentRow.addFieldToRow(fieldSpec, fieldValueProperty, isValidProperty);
		
		if(!canKeepWithNextOrPrevious(fieldSpec))
			endCurrentRow();
	}
	
	void endCurrentRow()
	{
		if(currentRow == null)
			return;
		
		Node label = currentRow.getLabelNode();
		Node fields = currentRow.getFieldsNode();
		currentRow = null;

		int currentRowIndex = rows.size();
		add(label, LABEL_COLUMN, currentRowIndex);
		add(fields, DATA_COLUMN, currentRowIndex);
	}
	
	private boolean canKeepWithNextOrPrevious(FieldSpec fieldSpec)
	{
		FieldType type = fieldSpec.getType();
		
		if(type.isBoolean() || type.isDate() || type.isDateRange())
			return true;
		
		if(type.isDropdown() || type.isLanguageDropdown() || type.isNestedDropdown())
			return true;
		
		return false;
	}

	private MartusLocalization getLocalization()
	{
		return localization;
	}

	private static final int LABEL_COLUMN = 0;
	private static final int DATA_COLUMN = 1;

	private FxBulletin bulletin;
	private MartusLocalization localization;
	private String title;
	private BulletinEditorRow currentRow;
	private Vector<BulletinEditorRow> rows;
}