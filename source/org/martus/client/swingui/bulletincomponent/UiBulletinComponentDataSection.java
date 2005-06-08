/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2005, Beneficent
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

package org.martus.client.swingui.bulletincomponent;

import java.awt.Color;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.border.LineBorder;

import org.martus.client.core.LanguageChangeListener;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiDateEditor;
import org.martus.client.swingui.fields.UiDateViewer;
import org.martus.client.swingui.fields.UiField;
import org.martus.client.swingui.fields.UiFlexiDateEditor;
import org.martus.client.swingui.fields.UiField.DataInvalidException;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.clientside.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.FieldDataPacket;
import org.martus.swing.UiLabel;


abstract public class UiBulletinComponentDataSection extends UiBulletinComponentSection
{
	UiBulletinComponentDataSection(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	public void createLabelsAndFields(FieldSpec[] specs, LanguageChangeListener listener)
	{
		fieldSpecs = specs;
		languageChangeListener = listener;

		fields = new UiField[specs.length];
		for(int fieldNum = 0; fieldNum < specs.length; ++fieldNum)
		{
			fields[fieldNum] = createAndAddLabelAndField(specs[fieldNum]);
		}
		addComponents(createLabel(getLocalization().getFieldLabel("attachments")), createAttachmentTable());
	}

	public UiField createAndAddLabelAndField(FieldSpec spec)
	{
		UiField field = createField(spec);
		field.initalize();
		addComponents(createLabel(spec), field.getComponent());
		return field;
	}

	public UiField[] getFields()
	{
		return fields;
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

 	public JComponent createLabel(FieldSpec spec)
	{
		String labelText = spec.getLabel();
		if(labelText.equals(""))
			labelText = getLocalization().getFieldLabel(spec.getTag());
		return createLabel(labelText);
	}

	public JComponent createLabel(String labelText) 
	{
		//TODO: For wrapped labels, we need to take into consideration font size, text alignment, rtol and printing. 
		//UiWrappedTextArea label = new UiWrappedTextArea(labelText, 30);
		UiLabel label = new UiLabel(labelText);
		return label;
	}

	private UiField createField(FieldSpec fieldSpec)
	{
		UiField field = null;

		if(fieldSpec.getTag().equals(Bulletin.TAGENTRYDATE))
			field = createReadOnlyDateField();
		else
			field = createNormalField(fieldSpec);
		field.getComponent().setBorder(new LineBorder(Color.black));
		return field;
	}


	private UiField createNormalField(FieldSpec fieldSpec)
	{
		UiField field;
		switch(fieldSpec.getType())
		{
			case FieldSpec.TYPE_MULTILINE:
				field = createMultilineField();
				break;
			case FieldSpec.TYPE_DATE:
				field = createDateField(fieldSpec);
				break;
			case FieldSpec.TYPE_DATERANGE:
				field = createFlexiDateField(fieldSpec);
				break;
			case FieldSpec.TYPE_LANGUAGE:
				ChoiceItem[] languages =
					getLocalization().getLanguageNameChoices();
				field = createChoiceField(languages);
				field.setLanguageListener(languageChangeListener);
				break;
			case FieldSpec.TYPE_DROPDOWN:
				Vector stringChoices = ((DropDownFieldSpec)fieldSpec).getChoices();
				field = createChoiceField(stringChoices);
				break;
			case FieldSpec.TYPE_NORMAL:
				field = createNormalField();
				break;
			case FieldSpec.TYPE_MESSAGE:
				field = createMessageField(fieldSpec);
				break;
			case FieldSpec.TYPE_BOOLEAN:
				field = createBoolField();
				break;
			case FieldSpec.TYPE_GRID:
				field = createGridField((GridFieldSpec)fieldSpec);
				break;
			case FieldSpec.TYPE_UNKNOWN:
			default:
				field = createUnknownField();
				break;
		}
		return field;
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

		private static final long serialVersionUID = 1;
	}
	
	abstract public UiField createNormalField();
	abstract public UiField createMultilineField();
	abstract public UiField createMessageField(FieldSpec spec);

	abstract public UiField createChoiceField(ChoiceItem[] choices);
	abstract public UiField createChoiceField(Vector choices);
	abstract public UiField createDateField(FieldSpec spec);
	abstract public UiField createFlexiDateField(FieldSpec spec);
	abstract public UiField createUnknownField();
	abstract public UiField createBoolField();
	abstract public UiField createGridField(GridFieldSpec fieldSpec);

	abstract public JComponent createAttachmentTable();
	abstract public void addAttachment(AttachmentProxy a);
	abstract public void clearAttachments();
	abstract public void validateAttachments() throws DataInvalidException;

	public UiField createReadOnlyDateField()
	{
		return new UiDateViewer(getLocalization());
	}

	LanguageChangeListener languageChangeListener;

}
