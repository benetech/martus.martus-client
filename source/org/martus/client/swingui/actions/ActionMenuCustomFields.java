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
import org.martus.client.core.ClientBulletinStore;
import org.martus.client.core.MartusApp;
import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiCustomFieldsDlg;
import org.martus.common.CustomFields;
import org.martus.common.FieldSpec;
import org.martus.common.MartusConstants;
import org.martus.common.StandardFieldSpecs;
import org.martus.common.CustomFields.CustomFieldsParseException;

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
			inputDlg.setVisible(true);
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
				FieldSpec[] newSpecs = null;
				try
				{
					newSpecs = CustomFields.parseXml(newCustomFieldXml);
				}
				catch(CustomFieldsParseException e)
				{
					e.printStackTrace();
				}
				return newSpecs;
			}
		}
	}
	
	
}
