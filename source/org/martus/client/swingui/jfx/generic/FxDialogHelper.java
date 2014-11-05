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
package org.martus.client.swingui.jfx.generic;

import java.util.Map;

import org.martus.client.swingui.UiMainWindow;
import org.martus.clientside.UiLocalization;
import org.martus.common.MartusLogger;

public class FxDialogHelper
{
	public static void showNotificationDialog(UiMainWindow mainWindow, String baseTag)
	{
		showNotificationDialog(mainWindow, baseTag, null);
	}
	
	public static void showNotificationDialog(UiMainWindow mainWindow, String baseTag, Map map)
	{
		String causeTag = UiLocalization.createNotifyCauseTag(baseTag);
		FxController mainNotificationAreaController = new SimpleTextContentController(mainWindow, causeTag, map);
		DialogShellController dialogWithCloseShellController = new DialogWithCloseShellController(mainWindow, mainNotificationAreaController);
		createAndShowDialog(mainWindow, dialogWithCloseShellController);
	}

	public static boolean showConfirmationDialog(UiMainWindow mainWindow, String baseTag)
	{
		String causeTag = UiLocalization.createConfirmEffectTag(baseTag);
		FxController mainNotificationAreaController = new SimpleTextContentController(mainWindow, causeTag);
		DialogWithYesNoShellController dialogWithCloseShellController = new DialogWithYesNoShellController(mainWindow, mainNotificationAreaController);
		createAndShowDialog(mainWindow, dialogWithCloseShellController);
		
		return dialogWithCloseShellController.didConfirm();
	}
	
	private static void createAndShowDialog(UiMainWindow mainWindow, DialogShellController dialogShellController)
	{
		try
		{
			DialogStage stage = new DialogStage(mainWindow, dialogShellController);
			FxModalDialog.createAndShowConfirmationSizedDialog(mainWindow, stage);
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}
}
