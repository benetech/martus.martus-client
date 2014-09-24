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
package org.martus.client.swingui.bulletintable;

import java.util.HashMap;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.FieldSpecCollection;
import org.martus.common.MartusLogger;
import org.martus.common.bulletin.Bulletin;

public class ModifyBulletinActionDoer
{
	
	public ModifyBulletinActionDoer(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
	}
	
	public void doModifyBulletin(Bulletin original)
	{
		try
		{
			if(original == null)
				return;

			String myAccountId = mainWindow.getApp().getAccountId();
			boolean isMine = myAccountId.equals(original.getAccount());
			boolean isVerifiedFieldDeskBulletin = mainWindow.getApp().isVerifiedFieldDeskAccount(original.getAccount());

			if(!isMine)
			{
				if(isVerifiedFieldDeskBulletin)
				{
					if(!mainWindow.confirmDlg("CloneBulletinAsMine"))
						return;
				}
				else
				{
					if(!mainWindow.confirmDlg("CloneUnverifiedFDBulletinAsMine"))
						return;
				}
			}
			
			if(original.hasUnknownTags() || original.hasUnknownCustomField())
			{
				if(!mainWindow.confirmDlg("EditBulletinWithUnknownTags"))
					return;
			}
			
			Bulletin bulletinToModify = original; 
			if(needsCloneToEdit(isMine, original.requiresNewCopyToEdit()))
				bulletinToModify = createCloneAndUpdateFieldSpecsIfNecessary(original);
			else if(isMyMutable(isMine, original.isMutable()))
				bulletinToModify = updateFieldSpecsIfNecessary(original);
			bulletinToModify.allowOnlyTheseAuthorizedKeysToRead(mainWindow.getApp().getAllHQKeys());
			bulletinToModify.addAuthorizedToReadKeys(mainWindow.getApp().getDefaultHQKeysWithFallback());
			mainWindow.modifyBulletin(bulletinToModify);
		}
		catch(Exception e)
		{
			MartusLogger.logException(e);			mainWindow.notifyDlg("UnexpectedError");
		}
	}

	private boolean isMyMutable(boolean isMine, boolean isDraft)
	{
		return isMine && isDraft;
	}
	
	private boolean needsCloneToEdit(boolean isMine, boolean requiresCloneToEdit)
	{
		return requiresCloneToEdit || !isMine;
	}

	private Bulletin updateFieldSpecsIfNecessary(Bulletin original) throws Exception
	{
		ClientBulletinStore store = mainWindow.getApp().getStore();
		if(store.bulletinHasCurrentFieldSpecs(original))
			return original;
		if(confirmUpdateFieldsDlg("UseBulletinsDraftCustomFields"))
			return original;
		FieldSpecCollection publicFieldSpecsToUse = store.getTopSectionFieldSpecs();
		FieldSpecCollection privateFieldSpecsToUse = store.getBottomSectionFieldSpecs();
		return store.createDraftClone(original, publicFieldSpecsToUse, privateFieldSpecsToUse);
	}

	private Bulletin createCloneAndUpdateFieldSpecsIfNecessary(Bulletin original) throws Exception
	{
		ClientBulletinStore store = mainWindow.getApp().getStore();
		FieldSpecCollection publicFieldSpecsToUse = store.getTopSectionFieldSpecs();
		FieldSpecCollection privateFieldSpecsToUse = store.getBottomSectionFieldSpecs();
		if(!store.bulletinHasCurrentFieldSpecs(original))
		{
			if(confirmUpdateFieldsDlg("UseBulletinsCustomFields"))
			{
				publicFieldSpecsToUse = original.getTopSectionFieldSpecs();
				privateFieldSpecsToUse = original.getBottomSectionFieldSpecs();
			}
		}

		Bulletin bulletinToModify = store.createNewDraft(original, publicFieldSpecsToUse, privateFieldSpecsToUse);
		return bulletinToModify;
	}
	
	private boolean confirmUpdateFieldsDlg(String baseTag)
	{
		MartusLocalization localization = mainWindow.getLocalization();
		String useOld = localization.getButtonLabel("UseOldCustomFields");
		String useNew = localization.getButtonLabel("UseNewCustomFields");
		String[] buttons = {useOld, useNew};
		HashMap tokenReplacement = new HashMap();
		tokenReplacement.put("#UseOldCustomFields#", useOld);
		tokenReplacement.put("#UseNewCustomFields#", useNew);

		return mainWindow.confirmCustomButtonsDlg(mainWindow, baseTag, buttons, tokenReplacement);
	}
	UiMainWindow mainWindow;
}
