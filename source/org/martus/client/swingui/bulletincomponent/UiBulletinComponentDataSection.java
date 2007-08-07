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
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JComponent;

import org.martus.client.core.LanguageChangeListener;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiDateEditor;
import org.martus.client.swingui.fields.UiField;
import org.martus.client.swingui.fields.UiFieldCreator;
import org.martus.client.swingui.fields.UiFlexiDateEditor;
import org.martus.client.swingui.fields.UiField.DataInvalidException;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.FieldDataPacket;
import org.martus.swing.UiButton;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;


abstract public class UiBulletinComponentDataSection extends UiBulletinComponentSection
{
	UiBulletinComponentDataSection(UiMainWindow mainWindowToUse, String sectionNameToUse, UiFieldCreator fieldCreatorToUse)
	{
		super(mainWindowToUse);
		sectionName = sectionNameToUse;
		fieldCreator = fieldCreatorToUse;
	}

	public void createLabelsAndFields(FieldSpec[] specs, LanguageChangeListener listener)
	{
		fieldSpecs = specs;
		languageChangeListener = listener;

		fields = new UiField[specs.length];
		for(int fieldNum = 0; fieldNum < specs.length; ++fieldNum)
		{
			UiField thisField = createAndAddLabelAndField(specs[fieldNum]);
			if(specs[fieldNum].getType().isLanguageDropdown())
				thisField.setLanguageListener(listener);
			fields[fieldNum] = thisField;
			
		}
		JComponent attachmentTable = createAttachmentTable();
		addComponents(createLabel("_Attachments" + sectionName, "", attachmentTable), attachmentTable);
	}

	public UiField createAndAddLabelAndField(FieldSpec spec)
	{
		UiField field = fieldCreator.createField(spec);
		field.initalize();
		addComponents(createLabel(spec, field.getComponent()), field.getComponent());
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

 	public JComponent createLabel(FieldSpec spec, JComponent field)
	{
		String labelText = spec.getLabel();
		return createLabel(spec.getTag(), labelText, field);
	}

	public JComponent createLabel(String tag, String labelText, JComponent field) 
	{
		if(labelText.equals(""))
			labelText = getLocalization().getFieldLabel(tag);

		//TODO: For wrapped labels, we need to take into consideration font size, text alignment, rtol and printing. 
		//UiWrappedTextArea label = new UiWrappedTextArea(labelText, 30);
		//UiLabel label = new UiLabel(labelText);
		//return label;
		int fixedWidth = 14;
		UiWrappedTextArea label = new UiWrappedTextArea(labelText, fixedWidth, fixedWidth);
		label.setFocusable(false);
		
		HiderButton hider = new HiderButton(getMainWindow().getApp(), tag, field);
		Component spacer = Box.createHorizontalStrut(10);
			
		Box panel = Box.createHorizontalBox();
		hider.setAlignmentY(JComponent.TOP_ALIGNMENT);
		label.setAlignmentY(JComponent.TOP_ALIGNMENT);
		Utilities.addComponentsRespectingOrientation(panel, new Component[] {hider, spacer, label});
		return panel;
	}
	
	class HiderButton extends UiButton implements ActionListener
	{
		public HiderButton(MartusApp appToUse, String tagToUse, JComponent fieldToHide)
		{
			app = appToUse;
			tag = tagToUse;
			field = fieldToHide;
			
			setMargin(new Insets(0, 0, 0, 0));
			addActionListener(this);
			
			forceState(app.isFieldExpanded(tag));
		}

		public void actionPerformed(ActionEvent event) 
		{
			toggleState();
			app.setFieldExpansionState(tag, field.isVisible());
		}

		private void toggleState() 
		{
			forceState(!field.isVisible());
		}

		private void forceState(boolean newState) {
			field.setVisible(newState);
			refresh();
		}

		private void refresh() 
		{
			setIcon(getAppropriateIcon());
		}
		
		public Icon getAppropriateIcon()
		{
			int size = getFont().getSize();
			if(field.isVisible())
				return new MinusIcon(size);
			
			return new PlusIcon(size);
		}

		MartusApp app;
		String tag;
		JComponent field;
	}
	
	abstract class HiderIcon implements Icon
	{
		public HiderIcon(int fontSize)
		{
			size = fontSize/2;
			margin = size/10;
			thickness = size/6 + 1;
		}
		
		public int getIconHeight()
		{
			return size;
		}

		public int getIconWidth()
		{
			return size;
		}
		
		protected void drawVerticalLine(Graphics g, int x, int y)
		{
			g.fillRect(x + size/2 - thickness/2, y + margin, thickness, size - margin*2);
		}

		protected void drawHorizontalLine(Graphics g, int x, int y)
		{
			g.fillRect(x + margin, y + size/2 - thickness/2, size - margin*2, thickness);
		}

		int size;
		int margin;
		int thickness;
	}
	
	class PlusIcon extends HiderIcon
	{
		public PlusIcon(int fontSize)
		{
			super(fontSize);
		}

		public void paintIcon(Component component, Graphics g, int x, int y)
		{
			g.setColor(component.getForeground());
			drawHorizontalLine(g, x, y);
			drawVerticalLine(g, x, y);
		}
	}

	class MinusIcon extends HiderIcon
	{
		public MinusIcon(int fontSize)
		{
			super(fontSize);
		}

		public void paintIcon(Component component, Graphics g, int x, int y)
		{
			g.setColor(component.getForeground());
			drawHorizontalLine(g, x, y);
		}
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
	UiFieldCreator fieldCreator;
}
