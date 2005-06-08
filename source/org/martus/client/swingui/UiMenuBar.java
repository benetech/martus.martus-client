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

package org.martus.client.swingui;

import java.io.IOException;
import java.io.NotSerializableException;

import javax.swing.AbstractAction;
import javax.swing.JMenuBar;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import org.martus.client.swingui.actions.ActionMenuBackupMyKeyPair;
import org.martus.client.swingui.actions.ActionMenuCustomFields;
import org.martus.client.swingui.actions.ActionMenuQuickEraseDeleteMyData;
import org.martus.client.swingui.actions.ActionMenuQuickEraseRemoveMartus;
import org.martus.client.swingui.actions.UiActions;
import org.martus.swing.UiLanguageDirection;

public class UiMenuBar extends JMenuBar
{
	UiMenuBar(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
		UiLocalization localization = mainWindow.getLocalization();
		applyComponentOrientation(UiLanguageDirection.getComponentOrientation());
		createMenuActions();

		UiMenu file = new UiMenu(localization, "file");
		FileMenuListener fileMenuListener = new FileMenuListener();
		file.addMenuListener(fileMenuListener);
		fileMenuListener.initalize();

		file.add(UiActions.newActionMenuCreateNewBulletin(mainWindow));
		file.add(actionMenuPrint);
		file.addSeparator();
		file.add(UiActions.newActionMenuExportFolder(mainWindow));
		file.add(UiActions.newActionMenuExportBulletins(mainWindow));
		file.add(actionMenuResendBulletins);
		file.addSeparator();
		file.add(UiActions.newActionMenuExit(mainWindow));


		UiMenu edit = new UiMenu(localization ,"edit");
		EditMenuListener editMenuListener = new EditMenuListener();
		edit.addMenuListener(editMenuListener);
		editMenuListener.initalize();

		edit.add(UiActions.newActionMenuSearch(mainWindow));
		edit.addSeparator();
		edit.add(actionMenuModifyBulletin);
		edit.addSeparator();
		edit.add(actionMenuCutBulletins);
		edit.add(actionMenuCopyBulletins);
		edit.add(actionMenuPasteBulletins);
		edit.add(actionMenuSelectAllBulletins);
		edit.addSeparator();
		edit.add(actionMenuDiscardBulletins);

		UiMenu folders = new UiMenu(localization, "folders");
		FoldersMenuListener folderMenuListener = new FoldersMenuListener();
		folders.addMenuListener(folderMenuListener);
		folderMenuListener.initalize();

		folders.add(UiActions.newActionMenuCreateFolder(mainWindow));
		folders.add(actionMenuRenameFolder);
		folders.add(actionMenuDeleteFolder);


		UiMenu server = new UiMenu(localization, "server");
		server.add(UiActions.newActionMenuRetrieveMySealedBulletins(mainWindow));
		server.add(UiActions.newActionMenuRetrieveMyDraftBulletins(mainWindow));
		server.add(UiActions.newActionMenuDeleteMyServerDraftBulletins(mainWindow));
		server.addSeparator();
		server.add(UiActions.newActionMenuRetrieveHQSealedBulletins(mainWindow));
		server.add(UiActions.newActionMenuRetrieveHQDraftBulletins(mainWindow));
		server.addSeparator();
		server.add(UiActions.newActionMenuSelectServer(mainWindow));
		server.add(UiActions.newActionMenuRemoveServer(mainWindow));


		UiMenu options = new UiMenu(localization, "options");
		options.add(UiActions.newActionMenuPreferences(mainWindow));
		options.add(UiActions.newActionMenuContactInfo(mainWindow));
		options.add(UiActions.newActionMenuChangeUserNamePassword(mainWindow));
		options.addSeparator();
		options.add(UiActions.newActionMenuDefaultDetailsFieldContent(mainWindow));
		options.add(new ActionMenuCustomFields(mainWindow));
		
		UiMenu tools = new UiMenu(localization, "tools");
		tools.add(new ActionMenuQuickEraseDeleteMyData(mainWindow));
		tools.add(new ActionMenuQuickEraseRemoveMartus(mainWindow));
		tools.addSeparator();
		tools.add(new ActionMenuBackupMyKeyPair(mainWindow));
		tools.add(UiActions.newActionMenuExportMyPublicKey(mainWindow));
		tools.addSeparator();
		tools.add(UiActions.newActionMenuConfigureHQs(mainWindow));
		
		UiMenu help = new UiMenu(localization, "help");
		help.add(UiActions.newActionMenuHelp(mainWindow));
		help.add(UiActions.newActionMenuAbout(mainWindow));
		help.addSeparator();
		help.add(UiActions.newActionMenuAccountDetails(mainWindow));

		add(file);
		add(edit);
		add(folders);
		add(server);
		add(options);
		add(tools);
		add(help);
	}

	class FileMenuListener implements MenuListener
	{
		public void initalize()
		{
			//Java Bug, menu items need to be disabled before correct behavior occures.
			actionMenuPrint.setEnabled(false);
			actionMenuResendBulletins.setEnabled(false);
		}

		public void menuSelected(MenuEvent e)
		{
			actionMenuPrint.setEnabled(actionMenuPrint.isEnabled());
			actionMenuResendBulletins.setEnabled(actionMenuResendBulletins.isEnabled());
		}

		public void menuDeselected(MenuEvent e) {}
		public void menuCanceled(MenuEvent e) {}
	}

	class EditMenuListener implements MenuListener
	{
		public void menuSelected(MenuEvent e)
		{
			actionMenuModifyBulletin.setEnabled(actionMenuModifyBulletin.isEnabled());
			actionMenuSelectAllBulletins.setEnabled(actionMenuSelectAllBulletins.isEnabled());
			actionMenuCutBulletins.setEnabled(actionMenuCutBulletins.isEnabled());
			actionMenuCopyBulletins.setEnabled(actionMenuCopyBulletins.isEnabled());
			actionMenuPasteBulletins.setEnabled(actionMenuPasteBulletins.isEnabled());
			actionMenuDiscardBulletins.setEnabled(actionMenuDiscardBulletins.isEnabled());
		}

		public void initalize()
		{
			//Java Bug, menu items need to be disabled before correct behavior occures.
			actionMenuModifyBulletin.setEnabled(false);
			actionMenuSelectAllBulletins.setEnabled(false);
			actionMenuCutBulletins.setEnabled(false);
			actionMenuCopyBulletins.setEnabled(false);
			actionMenuPasteBulletins.setEnabled(false);
			actionMenuDiscardBulletins.setEnabled(false);
		}

		public void menuDeselected(MenuEvent e) {}
		public void menuCanceled(MenuEvent e) {}
	}

	class FoldersMenuListener implements MenuListener
	{
		public void menuSelected(MenuEvent e)
		{
			actionMenuRenameFolder.setEnabled(actionMenuRenameFolder.isEnabled());
			actionMenuDeleteFolder.setEnabled(actionMenuDeleteFolder.isEnabled());
		}

		public void initalize()
		{
			//Java Bug, menu items need to be disabled before correct behavior occures.
			actionMenuRenameFolder.setEnabled(false);
			actionMenuDeleteFolder.setEnabled(false);
		}

		public void menuDeselected(MenuEvent e) {}
		public void menuCanceled(MenuEvent e) {}
	}

	private void createMenuActions()
	{
		actionMenuPrint = UiActions.newActionMenuPrint(mainWindow);

		actionMenuModifyBulletin = UiActions.newActionMenuModifyBulletin(mainWindow);
		actionMenuSelectAllBulletins = UiActions.newActionMenuSelectAllBulletins(mainWindow);
		actionMenuCutBulletins = UiActions.newActionMenuCutBulletins(mainWindow);
		actionMenuCopyBulletins = UiActions.newActionMenuCopyBulletins(mainWindow);
		actionMenuPasteBulletins = UiActions.newActionMenuPasteBulletins(mainWindow);
		actionMenuDiscardBulletins = UiActions.newActionMenuDiscardBulletins(mainWindow);
		actionMenuResendBulletins = UiActions.newActionMenuResendBulletins(mainWindow);
		
		actionMenuRenameFolder = UiActions.newActionMenuRenameFolder(mainWindow);
		actionMenuDeleteFolder = UiActions.newActionMenuDeleteFolder(mainWindow);
	}


	// This class is NOT intended to be serialized!!!
	private static final long serialVersionUID = 1;
	private void writeObject(java.io.ObjectOutputStream stream) throws IOException
	{
		throw new NotSerializableException();
	}

	UiMainWindow mainWindow;

	AbstractAction actionMenuPrint;
	AbstractAction actionMenuModifyBulletin;
	AbstractAction actionMenuSelectAllBulletins;
	AbstractAction actionMenuCutBulletins;
	AbstractAction actionMenuCopyBulletins;
	AbstractAction actionMenuPasteBulletins;
	AbstractAction actionMenuDiscardBulletins;
	AbstractAction actionMenuResendBulletins;
	AbstractAction actionMenuRenameFolder;
	AbstractAction actionMenuDeleteFolder;
}
