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
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;

import org.martus.client.core.FxBulletin;
import org.martus.client.core.FxBulletinField;
import org.martus.client.swingui.MartusLocalization;
import org.martus.common.MartusLogger;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.fieldspec.FieldSpec;

public class FxFormCreator
{
	public FxFormCreator(MartusLocalization localizationToUse)
	{
		localization = localizationToUse;
	}
	
	public Node createFormFromBulletin(FxBulletin bulletinToShow, Node attachments)
	{
		bulletin = bulletinToShow;
		sections = new Vector<BulletinEditorSection>();
		
		Vector<FieldSpec> fieldSpecs = bulletin.getFieldSpecs();
		fieldSpecs.forEach(fieldSpec -> addField(bulletin.getField(fieldSpec)));

		Accordion accordion = new Accordion();
		ObservableList<TitledPane> panes = accordion.getPanes();
		sections.forEach(section -> panes.add(createTitledPane(section)));
		panes.add(createAttachmentTitledPane(attachments));
		TitledPane firstPane = panes.get(0);
		accordion.setExpandedPane(firstPane);
		return accordion;
	}
	
	private TitledPane createAttachmentTitledPane(Node attachments)
	{
		String title = getLocalization().getWindowTitle("Attachments");
		return new TitledPane(title, attachments);
	}

	private TitledPane createTitledPane(BulletinEditorSection section)
	{
		String title = section.getTitle();
		return new TitledPane(title, section);
	}

	private void addField(FxBulletinField field)
	{
		FieldSpec fieldSpec = field.getFieldSpec();
		
		if(shouldOmitField(fieldSpec))
			return;

		boolean isSectionStart = field.isSectionStart();
		
		if(isSectionStart || currentSection == null)
		{
			if(currentSection != null)
				currentSection.endCurrentRow();
			
			String sectionTitle = "";
			if(isSectionStart)
				sectionTitle = fieldSpec.getLabel();
			currentSection = new BulletinEditorSection(bulletin, getLocalization(), sectionTitle);
			sections.add(currentSection);
		}

		if(isSectionStart)
			return;
		
		try
		{
			SimpleStringProperty fieldValueProperty = getFieldValueProperty(field);
			ObservableBooleanValue isValidProperty = getIsValidProperty(field);
			
			currentSection.addField(fieldSpec, fieldValueProperty, isValidProperty);
		}
		catch(Exception e)
		{
			String errorMessage = getLocalization().getFieldLabel("notifyUnexpectedErrorcause");

			try
			{
				MartusLogger.logException(e);
				currentSection.addErrorMessage(fieldSpec.getLabel(), errorMessage);
			} 
			catch (Exception e1)
			{
				MartusLogger.logException(e1);
				throw new RuntimeException(e1);
			}
		}
	}

	public SimpleStringProperty getFieldValueProperty(FxBulletinField field)
	{
		if(field.isGrid())
			return null;
		
		return field.valueProperty();
	}

	public ObservableBooleanValue getIsValidProperty(FxBulletinField field)
	{
		if(field.isGrid())
			return null;
		
		return field.fieldIsValidProperty();
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
	private BulletinEditorSection currentSection;
	private Vector<BulletinEditorSection> sections;
}