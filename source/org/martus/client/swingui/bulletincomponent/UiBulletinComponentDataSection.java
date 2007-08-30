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

import javax.swing.Box;
import javax.swing.JComponent;

import org.martus.client.core.LanguageChangeListener;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiDateEditor;
import org.martus.client.swingui.fields.UiField;
import org.martus.client.swingui.fields.UiFieldCreator;
import org.martus.client.swingui.fields.UiFlexiDateEditor;
import org.martus.client.swingui.fields.UiField.DataInvalidException;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.FieldDataPacket;
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
		for(int fieldNum = 0; fieldNum < specs.length; ++fieldNum)
		{
			FieldSpec spec = specs[fieldNum];
			fields[fieldNum] = createField(spec, listener);
			
			if(spec.getType().isSectionStart())
				startNewGroup("_Section" + spec.getTag(), spec.getLabel());
			else
			{
				FieldRow fieldRow = new FieldRow(getMainWindow(), spec.getTag(), spec.getLabel(), fields[fieldNum].getComponent());
				addFieldRow(fieldRow);
			}
		}
		
		JComponent attachmentTable = createAttachmentTable();
		String tag = "_Attachments" + sectionName;
		FieldRow fieldRow = new FieldRow(getMainWindow(), tag, "", attachmentTable);
		addFieldRow(fieldRow);
	}

	UiField createField(FieldSpec spec, LanguageChangeListener listener)
	{
		UiField field = fieldCreator.createField(spec);
		field.initalize();
		return field;
	}
	
	UiField createAllPrivateField()
	{
		FieldSpec allPrivateFieldSpec = FieldSpec.createStandardField("allprivate", new FieldTypeBoolean());
		UiField field = createField(allPrivateFieldSpec, null);
		FieldRow fieldRow = new FieldRow(getMainWindow(), allPrivateFieldSpec.getTag(), allPrivateFieldSpec.getLabel(), field.getComponent());
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
		public FieldRow(UiMainWindow mainWindowToUse, String tag, String labelText, JComponent fieldComponent)
		{
			MartusLocalization localization = mainWindowToUse.getLocalization();
			fieldHolder = new FieldHolder(fieldComponent, localization);
			UiWrappedTextArea labelComponent = createLabelComponent(tag, labelText, localization);
			label = createLabel(tag, labelComponent, mainWindowToUse.getApp());
		}
		
		public JComponent getLabel()
		{
			return label;
		}
		
		public FieldHolder getFieldHolder()
		{
			return fieldHolder;
		}
		
	 	UiWrappedTextArea createLabelComponent(String tag, String labelText, MartusLocalization localization)
		{
			if(labelText.equals(""))
				labelText = localization.getFieldLabel(tag);

			//TODO: For wrapped labels, we need to take into consideration font size, text alignment, rtol and printing. 
			//UiWrappedTextArea label = new UiWrappedTextArea(labelText, 30);
			//UiLabel label = new UiLabel(labelText);
			//return label;
			int fixedWidth = 14;
			UiWrappedTextArea labelComponent = new UiWrappedTextArea(labelText, fixedWidth, fixedWidth);
			labelComponent.setFocusable(false);
			return labelComponent;
		}

		JComponent createLabel(String tag, JComponent labelComponent, MartusApp app)
		{
			HiderButton hider = new HiderButton(app, tag, fieldHolder);
			Component spacer = Box.createHorizontalStrut(10);
				
			Box panel = Box.createHorizontalBox();
			hider.setAlignmentY(JComponent.TOP_ALIGNMENT);
			labelComponent.setAlignmentY(JComponent.TOP_ALIGNMENT);
			Utilities.addComponentsRespectingOrientation(panel, new Component[] {hider, spacer, labelComponent});
			return panel;
		}
		
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
				fields[fieldNum].validate();
			} 
			catch (UiDateEditor.DateFutureException e) 
			{
				throw new UiDateEditor.DateFutureException(label);
			}
			catch(UiFlexiDateEditor.DateRangeInvertedException e)
			{
				throw new UiFlexiDateEditor.DateRangeInvertedException(label); 
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
