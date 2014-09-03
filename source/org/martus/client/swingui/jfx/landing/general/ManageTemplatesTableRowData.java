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
package org.martus.client.swingui.jfx.landing.general;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;

import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.FormTemplate;

public class ManageTemplatesTableRowData
{
	public ManageTemplatesTableRowData(String rawTemplateNameToUse, MiniLocalization localizationToUse)
	{
		localization = localizationToUse;

		rawTemplateNameProperty = new SimpleStringProperty(rawTemplateNameToUse);
		String displayableTemplateName = FormTemplate.getDisplayableTemplateName(rawTemplateNameToUse, localization);
		displayableTemplateNameProperty = new SimpleStringProperty(displayableTemplateName);
	}
	
	public Property<String> rawTemplateNameProperty()
	{
		return rawTemplateNameProperty;
	}
	
    public Property<String> displayableTemplateNameProperty() 
    { 
        return displayableTemplateNameProperty; 
    }
    
    public String getRawTemplateName()
    {
    	return rawTemplateNameProperty().getValue();
    }
    
    public String getDisplayableTemplateName()
    {
    	return displayableTemplateNameProperty().getValue();
    }
    
    // NOTE: This is required in order to be sortable using SaneComparator
    @Override
    public String toString()
    {
    	return getDisplayableTemplateName();
    }

	public static final String RAW_TEMPLATE_NAME = "rawTemplateName";
	public static final String DISPLAYABLE_TEMPLATE_NAME = "displayableTemplateName";

	private Property<String> rawTemplateNameProperty;
	private Property<String> displayableTemplateNameProperty;
	private MiniLocalization localization;
}
