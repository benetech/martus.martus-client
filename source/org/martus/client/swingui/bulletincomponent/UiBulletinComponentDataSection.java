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

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.martus.client.core.LanguageChangeListener;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiChoice;
import org.martus.client.swingui.fields.UiDateEditor;
import org.martus.client.swingui.fields.UiField;
import org.martus.client.swingui.fields.UiFieldCreator;
import org.martus.client.swingui.fields.UiGrid;
import org.martus.client.swingui.grids.GridTableModel;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DataInvalidException;
import org.martus.common.fieldspec.DateRangeInvertedException;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.RequiredFieldIsBlankException;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.FieldDataPacket;
import org.martus.swing.UiLabel;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;


abstract public class UiBulletinComponentDataSection extends UiBulletinComponentSection
{
	UiBulletinComponentDataSection(UiMainWindow mainWindowToUse, String sectionNameToUse)
	{
		super(mainWindowToUse, sectionNameToUse);
		sectionName = sectionNameToUse;
	}
	
	void setFieldCreator(UiFieldCreator creatorToUse)
	{
		fieldCreator = creatorToUse;
	}

	public void createLabelsAndFields(FieldSpec[] specs, LanguageChangeListener listener)
	{
		fieldSpecs = specs;
		languageChangeListener = listener;

		fields = new UiField[specs.length];
		FieldRow fieldRow = null;
		for(int fieldNum = 0; fieldNum < specs.length; ++fieldNum)
		{
			FieldSpec spec = specs[fieldNum];
			fields[fieldNum] = createField(spec, listener);
			
			if(spec.getType().isSectionStart())
				startNewGroup("_Section" + spec.getTag(), spec.getLabel());
			else
			{
				// FIXME: ask spec whether to keep on line or not
				if(fieldRow == null || !spec.keepWithPrevious())
				{
					fieldRow = new FieldRow(getMainWindow());
					fieldRow.setSpec(spec);
				}
				else
				{
					fieldRow.addComponent(new LabelWithinFieldRow(spec));
				}
				fieldRow.addComponent(fields[fieldNum].getComponent());
				addFieldRow(fieldRow);
			}
		}
		
		JComponent attachmentTable = createAttachmentTable();
		String tag = "_Attachments" + sectionName;
		fieldRow = new FieldRow(getMainWindow());
		fieldRow.setTag(tag);
		fieldRow.addComponent(attachmentTable);
		addFieldRow(fieldRow);
	}
	
	class LabelWithinFieldRow extends UiLabel
	{
		public LabelWithinFieldRow(FieldSpec spec)
		{
			setVerticalAlignment(UiLabel.TOP);
			setFont(new UiWrappedTextArea("").getFont());
			setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
			
			String labelText = spec.getLabel();
			if(labelText.equals(""))
				labelText = getLocalization().getFieldLabel(spec.getTag());
			setText(labelText);
		}
	}

	UiField createField(FieldSpec spec, LanguageChangeListener listener)
	{
		UiField field = fieldCreator.createField(spec);
		field.initalize();
		if(spec.getType().isGrid())
		{
			UiGrid grid = (UiGrid)field;
			grid.getGridTableModel().addTableModelListener(new GridChangeHandler(grid));
		}
		return field;
	}
	
	UiField createAllPrivateField()
	{
		FieldSpec allPrivateFieldSpec = FieldSpec.createStandardField("allprivate", new FieldTypeBoolean());
		UiField field = createField(allPrivateFieldSpec, null);
		FieldRow fieldRow = new FieldRow(getMainWindow());
		fieldRow.setSpec(allPrivateFieldSpec);
		fieldRow.addComponent(field.getComponent());
		addFieldRow(fieldRow);
		return field;
	}

	void addFieldRow(FieldRow row)
	{
		addComponents(row.getLabel(), row.getFieldHolder());
	}
	
	public void copyDataFromPacket(FieldDataPacket fdp)
	{
		for(int fieldNum = 0; fieldNum < fields.length; ++fieldNum)
		{
			String text = "";
			if(fdp != null)
				text = fdp.get(fieldSpecs[fieldNum].getTag());
			fields[fieldNum].setText(text);
		}

		if(fdp == null)
			return;

		AttachmentProxy[] attachments = fdp.getAttachments();
		for(int i = 0 ; i < attachments.length ; ++i)
			addAttachment(attachments[i]);
	}

	static class FieldRow
	{
		public FieldRow(UiMainWindow mainWindowToUse)
		{
			app = mainWindowToUse.getApp();
			localization = mainWindowToUse.getLocalization();
			fieldHolder = new FieldHolder(localization);
		}
		
		public void setSpec(FieldSpec spec)
		{
			setTagAndLabel(spec.getTag(), spec.getLabel());
		}
		
		public void setTag(String tag)
		{
			setTagAndLabel(tag, "");
		}
		
		void addComponent(JComponent fieldComponent) 
		{
			fieldHolder.addField(fieldComponent);
		}
		
		public JComponent getLabel()
		{
			return label;
		}
		
		public FieldHolder getFieldHolder()
		{
			return fieldHolder;
		}
		
		private UiWrappedTextArea createLabelComponent(String tag, String labelText)
		{
			//TODO: For wrapped labels, we need to take into consideration font size, text alignment, rtol and printing. 
			//UiWrappedTextArea label = new UiWrappedTextArea(labelText, 30);
			//UiLabel label = new UiLabel(labelText);
			//return label;
			int fixedWidth = 14;
			UiWrappedTextArea labelComponent = new UiWrappedTextArea(labelText, fixedWidth, fixedWidth);
			labelComponent.setFocusable(false);
			return labelComponent;
		}

		private JComponent createLabelWithHider(String tag, JComponent labelComponent)
		{
			HiderButton hider = new HiderButton(app, tag, fieldHolder);
			Component spacer = Box.createHorizontalStrut(10);
				
			Box panel = Box.createHorizontalBox();
			hider.setAlignmentY(JComponent.TOP_ALIGNMENT);
			labelComponent.setAlignmentY(JComponent.TOP_ALIGNMENT);
			Utilities.addComponentsRespectingOrientation(panel, new Component[] {hider, spacer, labelComponent});
			return panel;
		}
		
		private void setTagAndLabel(String tag, String labelText)
		{
			if(labelText.equals(""))
				labelText = localization.getFieldLabel(tag);

			UiWrappedTextArea labelComponent = createLabelComponent(tag, labelText);
			label = createLabelWithHider(tag, labelComponent);
		}

		MartusApp app;
		MartusLocalization localization;
		JComponent label;
		FieldHolder fieldHolder;
	}
	
	public void updateEncryptedIndicator(boolean isEncrypted)
	{
		String iconFileName = "unlocked.jpg";
		String title = getLocalization().getFieldLabel("publicsection");
		if(isEncrypted)
		{
			iconFileName = "locked.jpg";
			title = getLocalization().getFieldLabel("privatesection");
		}

		setSectionIconAndTitle(iconFileName, title);

		updateSectionBorder(isEncrypted);
	}

	public void copyDataToBulletin(Bulletin bulletin)
	{	
		for(int fieldNum = 0; fieldNum < fields.length; ++fieldNum)
		{						
			bulletin.set(fieldSpecs[fieldNum].getTag(), fields[fieldNum].getText());													
		}
	}

	
	public void validateData() throws DataInvalidException
	{
		for(int fieldNum = 0; fieldNum < fields.length; ++fieldNum)
		{
			String tag = fieldSpecs[fieldNum].getTag();
			String label = getLocalization().getFieldLabel(tag);
			if(StandardFieldSpecs.isCustomFieldTag(tag))
				label = fieldSpecs[fieldNum].getLabel();
			try 
			{
				fields[fieldNum].validate(fieldSpecs[fieldNum], label);
			} 
			catch (UiDateEditor.DateFutureException e) 
			{
				throw new UiDateEditor.DateFutureException(label);
			}
			catch(DateRangeInvertedException e)
			{
				throw new DateRangeInvertedException(label); 
			}
			catch(RequiredFieldIsBlankException e)
			{
				throw e;
			}
		}
		
		validateAttachments();
	}

	public boolean isAnyFieldModified(Bulletin original, Bulletin newBulletin)
	{
		for(int fieldNum = 0; fieldNum < fields.length; ++fieldNum)
		{			
			String fieldTag = fieldSpecs[fieldNum].getTag();			
			String oldFieldText = original.get(fieldTag);
			String newFieldText = newBulletin.get(fieldTag);

			if (!oldFieldText.equals(newFieldText))
			{									
				return true;
			}																
		}		
		
		return false;
	}
	
	public static class AttachmentMissingException extends DataInvalidException
	{
		public AttachmentMissingException(String localizedTag)
		{
			super(localizedTag);
		}

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
					updateDataDrivenDropdownsInsideGrid((GridFieldSpec)spec, (UiGrid)field);

				if(type.isDropdown())
					updateDataDrivenDropdown((DropDownFieldSpec)spec, field);
			}
		}

		private void updateDataDrivenDropdown(DropDownFieldSpec spec, UiField field) 
		{
			if(!isDataSourceThisGrid(spec))
				return;
			
			UiGrid dataSourceGrid = fieldCreator.getGridField(spec.getDataSourceGridTag());
			if(dataSourceGrid == null)
				return;
			
			UiChoice choiceField = (UiChoice)field;
			choiceField.updateChoices();
		}

		private void updateDataDrivenDropdownsInsideGrid(GridFieldSpec gridSpecToBlankOut, UiGrid gridToBlankOut) 
		{
			boolean needsUpdate = false;
			
			GridTableModel modelToBlankOut = gridToBlankOut.getGridTableModel();
			for(int column = 0; column < modelToBlankOut.getColumnCount(); ++column)
			{
				FieldSpec columnSpec = modelToBlankOut.getFieldSpecForColumn(column);
				if(!columnSpec.getType().isDropdown())
					continue;
				
				DropDownFieldSpec dropdownSpec = (DropDownFieldSpec)columnSpec;
				if(!isDataSourceThisGrid(dropdownSpec))
					continue;
				needsUpdate = true;
				
				ChoiceItem[] choices = getCurrentChoiceItems(dropdownSpec);
				if(choices == null)
					continue;
				
				gridToBlankOut.updateDataDrivenColumnWidth(column, choices);
				
				for(int row = 0; row < modelToBlankOut.getRowCount(); ++row)
				{
					String oldValue = (String)modelToBlankOut.getValueAt(row, column);
					String newValue = ensureValid(choices, oldValue);
					if(!newValue.equals(oldValue))
						modelToBlankOut.setValueAt(newValue, row, column);
				}
			}
			
			if(needsUpdate)
				gridToBlankOut.dataDrivenDropdownInsideGridMayNeedToBeUpdated();
		}

		private boolean isDataSourceThisGrid(DropDownFieldSpec spec) 
		{
			if(spec.getDataSourceGridTag() == null)
				return false;
			
			String dataSourceGridTag = spec.getDataSourceGridTag();
			String modifiedGridTag = modifiedGrid.getGridData().getSpec().getTag();
			return (dataSourceGridTag.equals(modifiedGridTag));
		}
		
		private String ensureValid(ChoiceItem[] choices, String text) 
		{
			for(int i = 0; i < choices.length; ++i)
				if(choices[i].getCode().equals(text))
					return text;

			return "";
		}
		
		private ChoiceItem[] getCurrentChoiceItems(DropDownFieldSpec spec)
		{
			UiGrid dataSourceGrid = fieldCreator.getGridField(spec.getDataSourceGridTag());
			if(dataSourceGrid == null)
				return null;
			
			return dataSourceGrid.buildChoicesFromColumnValues(spec.getDataSourceGridColumn());
		}

		UiGrid modifiedGrid;
	}

	abstract public JComponent createAttachmentTable();
	abstract public void addAttachment(AttachmentProxy a);
	abstract public void clearAttachments();
	abstract public void validateAttachments() throws DataInvalidException;

	LanguageChangeListener languageChangeListener;
	String sectionName;
	FieldSpec[] fieldSpecs;
	UiField[] fields;
	UiFieldCreator fieldCreator;
}

