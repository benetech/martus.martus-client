/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2004, Beneficent
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

package org.martus.client.swingui.actions;

import java.awt.event.ActionEvent;
import java.util.Vector;

import org.martus.client.core.ClientBulletinStore;
import org.martus.client.core.CustomFieldError;
import org.martus.client.core.CustomFieldSpecValidator;
import org.martus.client.core.MartusApp;
import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiCustomFieldsDlg;
import org.martus.client.swingui.dialogs.UiShowScrollableTextDlg;
import org.martus.common.CustomFields;
import org.martus.common.FieldSpec;
import org.martus.common.MartusConstants;
import org.martus.common.StandardFieldSpecs;
import org.martus.common.CustomFields.CustomFieldsParseException;
import org.martus.common.clientside.Localization;

public class ActionMenuCustomFields extends UiMenuAction
{
	public ActionMenuCustomFields(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse, "CustomFields");
	}
	
	public void actionPerformed(ActionEvent arg0)
	{
		if(!mainWindow.confirmDlg("EnterCustomFields"))
			return;
		
		MartusApp app = mainWindow.getApp();
		ClientBulletinStore store = app.getStore();
		FieldSpec[] existingSpecs = store.getPublicFieldSpecs();
		FieldSpec[] newSpecs = getCustomizedFieldsFromUser(existingSpecs);
		if(newSpecs == null)
			return;
			
		store.setPublicFieldTags(newSpecs);
		app.getConfigInfo().setCustomFieldSpecs(MartusConstants.deprecatedCustomFieldSpecs);
		app.getConfigInfo().setCustomFieldXml(new CustomFields(newSpecs).toString());

		try
		{
			app.saveConfigInfo();
		}
		catch (SaveConfigInfoException e)
		{
			mainWindow.notifyDlg("ErrorSavingConfig");
		}
	}

	private FieldSpec[] getCustomizedFieldsFromUser(FieldSpec[] existingSpecs)
	{
		CustomFields existingFields = new CustomFields(existingSpecs);
		String existingCustomFieldXml = existingFields.toString();
		while(true)
		{
			UiCustomFieldsDlg inputDlg = new UiCustomFieldsDlg(mainWindow, existingCustomFieldXml);
			inputDlg.setFocusToInputField();
			inputDlg.show();
			String newCustomFieldXml = inputDlg.getResult();
			if(newCustomFieldXml == null)
				return null;
			
			if(newCustomFieldXml.length() == 0)
			{
				if(mainWindow.confirmDlg("UndoCustomFields"))
				existingFields = new CustomFields(StandardFieldSpecs.getDefaultPublicFieldSpecs());
				existingCustomFieldXml = existingFields.toString();
			}
			else
			{
				Vector errors = null;
				try
				{
					FieldSpec[] newSpecs = CustomFields.parseXml(newCustomFieldXml);
					CustomFieldSpecValidator checker = new CustomFieldSpecValidator(newSpecs);
					if(checker.isValid())
						return newSpecs;
					errors = checker.getAllErrors();
				}
				catch (CustomFieldsParseException e)
				{
					e.printStackTrace();
					errors = new Vector();
					errors.add(CustomFieldError.errorParseXml());
				}

				String header1 = mainWindow.getLocalization().getFieldLabel("ErrorCustomFieldHeader1");
				String header2 = mainWindow.getLocalization().getFieldLabel("ErrorCustomFieldHeader2");
				String header3 = mainWindow.getLocalization().getFieldLabel("ErrorCustomFieldHeader3");
				String header4 = mainWindow.getLocalization().getFieldLabel("ErrorCustomFieldHeader4");
				String errorMessage = GetDataAndSpacing(header1, HEADER_SPACING_1);
				errorMessage += GetDataAndSpacing(header2, HEADER_SPACING_2);
				errorMessage += GetDataAndSpacing(header3, HEADER_SPACING_3);
				errorMessage += header4;
				errorMessage += '\n';
				for(int i = 0; i<errors.size(); ++i)
				{
					CustomFieldError thisError = (CustomFieldError)errors.get(i);
					String thisErrorMessage = GetDataAndSpacing(thisError.getCode(), HEADER_SPACING_1);
					thisErrorMessage += GetDataAndSpacing(thisError.getType(), HEADER_SPACING_2);
					thisErrorMessage += GetDataAndSpacing(thisError.getTag(), HEADER_SPACING_3);
					thisErrorMessage += thisError.getLabel();
					errorMessage += thisErrorMessage;
					errorMessage += '\n';
				}
				new UiShowScrollableTextDlg(mainWindow,"ErrorCustomFields", "ok", Localization.UNUSED_TAG, "ErrorCustomFields", errorMessage); 
				existingCustomFieldXml = newCustomFieldXml;
			}
		}
	}
	
	String GetDataAndSpacing(String data, int columnSpacing)
	{
		String columnData = data;
		for(int i = data.length(); i < columnSpacing; ++i)
		{
			columnData += " ";
		}
		columnData += " ";
		return columnData;
	}
	
	private int HEADER_SPACING_1 = 6;
	private int HEADER_SPACING_2 = 11;
	private int HEADER_SPACING_3 = 14;
	
	
}
