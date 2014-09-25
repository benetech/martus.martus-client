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
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;

import org.martus.client.core.FxBulletin;
import org.martus.client.swingui.MartusLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.fieldspec.FieldSpec;

public class FxFormCreator
{
	public FxFormCreator(MartusLocalization localizationToUse)
	{
		localization = localizationToUse;
	}
	
	public Node createFormFromBulletin(FxBulletin bulletinToShow)
	{
		bulletin = bulletinToShow;
		sections = new Vector<BulletinEditorSection>();
		
		Vector<FieldSpec> fieldSpecs = bulletin.getFieldSpecs();
		fieldSpecs.forEach(fieldSpec -> addField(fieldSpec));

		if(sections.size() == 1)
			return sections.get(0);

		Accordion accordion = new Accordion();
		sections.forEach(section -> accordion.getPanes().add(createTitledPane(section)));
		TitledPane firstPane = accordion.getPanes().get(0);
		accordion.setExpandedPane(firstPane);
		return accordion;
	}
	
	private TitledPane createTitledPane(BulletinEditorSection section)
	{
		String title = section.getTitle();
		return new TitledPane(title, section);
	}

	private void addField(FieldSpec fieldSpec)
	{
		if(shouldOmitField(fieldSpec))
			return;

		boolean isSectionStart = fieldSpec.getType().isSectionStart();
		
		if(isSectionStart || currentSection == null)
		{
			String sectionTitle = "";
			if(isSectionStart)
				sectionTitle = fieldSpec.getLabel();
			currentSection = new BulletinEditorSection(getLocalization(), sectionTitle);
			sections.add(currentSection);
		}

		if(isSectionStart)
			return;
		
		SimpleStringProperty fieldValueProperty = bulletin.fieldProperty(fieldSpec.getTag());
		ObservableBooleanValue isValidProperty = bulletin.isValidProperty(fieldSpec.getTag());
		currentSection.addField(fieldSpec, fieldValueProperty, isValidProperty);
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