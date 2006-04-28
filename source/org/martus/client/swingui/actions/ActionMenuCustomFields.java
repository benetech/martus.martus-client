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

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.MartusApp;
import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiCustomFieldsDlg;
import org.martus.common.FieldCollection;
import org.martus.common.MartusConstants;
import org.martus.common.FieldCollection.CustomFieldsParseException;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;

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
		BulletinFieldSpecs existingSpecs = new BulletinFieldSpecs();
		existingSpecs.setTopSectionSpecs(store.getTopSectionFieldSpecs());
		existingSpecs.setBottomSectionSpecs(store.getBottomSectionFieldSpecs());
		
		BulletinFieldSpecs newSpecs = getCustomizedFieldsFromUser(existingSpecs);
		if(newSpecs == null)
			return;
			
		store.setTopSectionFieldSpecs(newSpecs.getTopSectionSpecs());
		app.getConfigInfo().setCustomFieldLegacySpecs(MartusConstants.deprecatedCustomFieldSpecs);
		app.getConfigInfo().setCustomFieldTopSectionXml(new FieldCollection(newSpecs.getTopSectionSpecs()).toString());

		try
		{
			app.saveConfigInfo();
		}
		catch (SaveConfigInfoException e)
		{
			mainWindow.notifyDlg("ErrorSavingConfig");
		}
	}

	private BulletinFieldSpecs getCustomizedFieldsFromUser(BulletinFieldSpecs existingSpecs)
	{
		FieldCollection existingTopSectionFieldSpecs = new FieldCollection(existingSpecs.getTopSectionSpecs());
		String existingCustomTopSectionFieldXml = existingTopSectionFieldSpecs.toString();
		while(true)
		{
			UiCustomFieldsDlg inputDlg = new UiCustomFieldsDlg(mainWindow, existingCustomTopSectionFieldXml);
			inputDlg.setFocusToInputField();
			inputDlg.setVisible(true);
			String newCustomFieldXml = inputDlg.getResult();
			if(newCustomFieldXml == null)
				return null;
			
			if(newCustomFieldXml.length() == 0)
			{
				if(mainWindow.confirmDlg("UndoCustomFields"))
				existingTopSectionFieldSpecs = new FieldCollection(StandardFieldSpecs.getDefaultTopSectionFieldSpecs());
				existingCustomTopSectionFieldXml = existingTopSectionFieldSpecs.toString();
			}
			else
			{
				FieldSpec[] newTopSectionSpecs = null;
				try
				{
					newTopSectionSpecs = FieldCollection.parseXml(newCustomFieldXml);
				}
				catch(CustomFieldsParseException e)
				{
					e.printStackTrace();
				}
				BulletinFieldSpecs newFieldSpecs = new BulletinFieldSpecs();
				newFieldSpecs.setTopSectionSpecs(newTopSectionSpecs);
				//TODO implement realBottomSectionFieldSpecs
				newFieldSpecs.setBottomSectionSpecs(existingSpecs.getBottomSectionSpecs());
				return newFieldSpecs;
			}
		}
	}
	
	class BulletinFieldSpecs
	{
		public BulletinFieldSpecs()
		{
		}
		
		public FieldSpec[] getTopSectionSpecs()
		{
			return topSectionSpecs;
		}

		public FieldSpec[] getBottomSectionSpecs()
		{
			return bottomSectionSpecs;
		}
		
		public void setTopSectionSpecs(FieldSpec[] topSectionSpecsToUse)
		{
			topSectionSpecs = topSectionSpecsToUse;
		}

		public void setBottomSectionSpecs(FieldSpec[] bottomSectionSpecsToUse)
		{
			bottomSectionSpecs = bottomSectionSpecsToUse;
		}

		private FieldSpec[] topSectionSpecs;
		private FieldSpec[] bottomSectionSpecs ;
	}

	
}
