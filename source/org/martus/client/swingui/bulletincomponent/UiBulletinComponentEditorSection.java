/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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

package org.martus.client.swingui.bulletincomponent;

import java.io.File;

import javax.swing.JComponent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.martus.client.core.LanguageChangeListener;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiAttachmentEditor;
import org.martus.client.swingui.fields.UiChoiceEditor;
import org.martus.client.swingui.fields.UiEditableFieldCreator;
import org.martus.client.swingui.fields.UiField;
import org.martus.client.swingui.fields.UiGrid;
import org.martus.client.swingui.fields.UiGridEditor;
import org.martus.client.swingui.grids.GridTableModel;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.GridFieldSpec;

public class UiBulletinComponentEditorSection extends UiBulletinComponentDataSection
{

	public UiBulletinComponentEditorSection(UiMainWindow ownerToUse, String sectionName)
	{
		super(ownerToUse, sectionName);
		setFieldCreator(new UiEditableFieldCreator(ownerToUse));
	}

	public void addAttachment(AttachmentProxy a)
	{
		attachmentEditor.addAttachment(a);
	}

	public void clearAttachments()
	{
		attachmentEditor.clearAttachments();
	}

	public JComponent createAttachmentTable()
	{
		attachmentEditor = new UiAttachmentEditor(getMainWindow());
		return attachmentEditor;
	}

	public void validateAttachments() throws AttachmentMissingException
	{
		AttachmentProxy[] publicAttachments = attachmentEditor.getAttachments();
		for(int aIndex = 0; aIndex < publicAttachments.length; ++aIndex)
		{
			File file = publicAttachments[aIndex].getFile();
			if (file != null)
			{
				if(!file.exists())
					throw new AttachmentMissingException(file.getAbsolutePath());
			}
		}
	}

	UiField createField(FieldSpec spec, LanguageChangeListener listener) 
	{
		UiField newField = super.createField(spec, listener);
		
		if(spec.getType().isLanguageDropdown())
			newField.setLanguageListener(listener);
		if(spec.getType().isGrid())
		{
			UiGrid grid = (UiGrid)newField;
			grid.getGridTableModel().addTableModelListener(new GridChangeHandler(grid));
		}
		return newField;
	}


	class GridChangeHandler implements TableModelListener
	{
		public GridChangeHandler(UiGrid gridToMonitor) 
		{
			modifiedGrid = gridToMonitor;
		}

		public void tableChanged(TableModelEvent event) 
		{
			updateDataDrivenDropdowns();
		}

		private void updateDataDrivenDropdowns() 
		{
			for(int i = 0; i < fieldSpecs.length; ++i)
			{
				FieldSpec spec = fieldSpecs[i];
				FieldType type = spec.getType();
				UiField field = fields[i];
				
				if(type.isGrid())
					blankOutInvalidDataDrivenDropdowns((GridFieldSpec)spec, (UiGrid)field);

				if(type.isDropdown())
					updateDataDrivenDropdown((DropDownFieldSpec)spec, field);
			}
		}

		private void updateDataDrivenDropdown(DropDownFieldSpec spec, UiField field) 
		{
			if(!isDataSourceThisGrid(spec))
				return;
			
			UiGridEditor dataSourceGrid = fieldCreator.getEditableGridField(spec.getDataSourceGridTag());
			if(dataSourceGrid == null)
				return;
			
			String existingValue = field.getText();
			UiChoiceEditor choiceField = (UiChoiceEditor)field;
			choiceField.setChoices(dataSourceGrid.buildChoicesFromColumnValues(spec.getDataSourceGridColumn()));
			field.setText(ensureValid(spec, existingValue));
		}

		private void blankOutInvalidDataDrivenDropdowns(GridFieldSpec gridSpecToBlankOut, UiGrid gridToBlankOut) 
		{
			GridTableModel modelToBlankOut = gridToBlankOut.getGridTableModel();
			for(int column = 0; column < modelToBlankOut.getColumnCount(); ++column)
			{
				FieldSpec columnSpec = modelToBlankOut.getFieldSpecForColumn(column);
				if(!columnSpec.getType().isDropdown())
					continue;
				
				DropDownFieldSpec dropdownSpec = (DropDownFieldSpec)columnSpec;
				if(!isDataSourceThisGrid(dropdownSpec))
					continue;
				
				for(int row = 0; row < modelToBlankOut.getRowCount(); ++row)
				{
					String oldValue = (String)modelToBlankOut.getValueAt(row, column);
					String newValue = ensureValid(dropdownSpec, oldValue);
					if(!newValue.equals(oldValue))
						modelToBlankOut.setValueAt(newValue, row, column);
				}
			}
		}

		private boolean isDataSourceThisGrid(DropDownFieldSpec spec) 
		{
			if(spec.getDataSourceGridTag() == null)
				return false;
			
			String dataSourceGridTag = spec.getDataSourceGridTag();
			String modifiedGridTag = modifiedGrid.getGridData().getSpec().getTag();
			return (dataSourceGridTag.equals(modifiedGridTag));
		}
		
		private String ensureValid(DropDownFieldSpec spec, String text) 
		{
			UiGridEditor dataSourceGrid = fieldCreator.getEditableGridField(spec.getDataSourceGridTag());
			if(dataSourceGrid == null)
				return text;
			
			ChoiceItem[] choices = dataSourceGrid.buildChoicesFromColumnValues(spec.getDataSourceGridColumn());
			for(int i = 0; i < choices.length; ++i)
				if(choices[i].getCode().equals(text))
					return text;

			return "";
		}

		UiGrid modifiedGrid;
	}


	UiAttachmentEditor attachmentEditor;
}
