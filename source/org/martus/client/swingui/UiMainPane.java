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
package org.martus.client.swingui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.martus.client.swingui.bulletincomponent.UiBulletinPreviewPane;
import org.martus.client.swingui.bulletintable.UiBulletinTablePane;
import org.martus.client.swingui.foldertree.UiFolderTreePane;
import org.martus.swing.UiLanguageDirection;
import org.martus.swing.UiPopupMenu;
import org.martus.util.language.LanguageOptions;

public class UiMainPane extends JPanel
{
	public UiMainPane(UiMainWindow mainWindowToUse)
	{
		setLayout(new BorderLayout());
		setComponentOrientation(UiLanguageDirection.getComponentOrientation());

		previewPane = new UiBulletinPreviewPane(mainWindowToUse);
		bulletinsTable = new UiBulletinTablePane(mainWindowToUse);
		previewSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getBulletinsTable(), getPreviewPane());

		folderTreePane = new UiFolderTreePane(mainWindowToUse);

		if(LanguageOptions.isRightToLeftLanguage())
			folderSplitter = new FolderSplitPane(JSplitPane.HORIZONTAL_SPLIT, getPreviewSplitter(), getFolderTreePane());
		else
			folderSplitter = new FolderSplitPane(JSplitPane.HORIZONTAL_SPLIT, getFolderTreePane(), getPreviewSplitter());

		statusBar = new UiStatusBar(mainWindowToUse.getLocalization());		

		add(createTopStuff(mainWindowToUse), BorderLayout.NORTH);
		add(getFolderSplitter(), BorderLayout.CENTER);
		add(getStatusBar(), BorderLayout.SOUTH ); 
	}

	private JComponent createTopStuff(UiMainWindow mainWindowToUse)
	{
		JPanel topStuff = new JPanel(false);
		topStuff.setLayout(new GridLayout(2, 1));

		setMenuBar(new UiMenuBar(mainWindowToUse));
		topStuff.add(getMartusMenuBar());

		setToolBar(new UiToolBar(mainWindowToUse));
		topStuff.add(getToolBar());

		return topStuff;
	}

	public void updateEnabledStatuses()
	{
		getToolBar().updateEnabledStatuses();
	}

	public UiPopupMenu getPopupMenu()
	{
		UiPopupMenu menu = new UiPopupMenu();
		menu.add(getMartusMenuBar().actionMenuModifyBulletin);
		menu.addSeparator();
		menu.add(getMartusMenuBar().actionMenuCutBulletins);
		menu.add(getMartusMenuBar().actionMenuCopyBulletins);
		menu.add(getMartusMenuBar().actionMenuPasteBulletins);
		menu.add(getMartusMenuBar().actionMenuSelectAllBulletins);
		menu.addSeparator();
		menu.add(getMartusMenuBar().actionMenuDiscardBulletins);
		menu.addSeparator();
		menu.add(getMartusMenuBar().actionMenuResendBulletins);
		return menu;
	}
	
	public AbstractAction getActionMenuPaste()
	{
		return getMartusMenuBar().actionMenuPasteBulletins;
	}

	private UiToolBar getToolBar()
	{
		return toolBar;
	}

	private void setToolBar(UiToolBar toolBar)
	{
		this.toolBar = toolBar;
	}

	private UiMenuBar getMartusMenuBar()
	{
		return menuBar;
	}

	private void setMenuBar(UiMenuBar menuBar)
	{
		this.menuBar = menuBar;
	}
	
	public UiBulletinTablePane getBulletinsTable()
	{
		return bulletinsTable;
	}

	public UiBulletinPreviewPane getPreviewPane()
	{
		return previewPane;
	}

	public JSplitPane getPreviewSplitter()
	{
		return previewSplitter;
	}

	public UiStatusBar getStatusBar()
	{
		return statusBar;
	}

	public FolderSplitPane getFolderSplitter()
	{
		return folderSplitter;
	}

	public UiFolderTreePane getFolderTreePane()
	{
		return folderTreePane;
	}

	static class FolderSplitPane extends JSplitPane
	{
		public FolderSplitPane(int newOrientation, Component newLeftComponent, Component newRightComponent) 
		{
			super(newOrientation, newLeftComponent, newRightComponent);
		}

		public void setInitialDividerLocation(int location)
		{
			super.setDividerLocation(location);
		}

		public void setDividerLocation(int location) 
		{
			super.setDividerLocation(location);
			if(previousLocation != location)
			{
				previousLocation = location;
				getTopLevelAncestor().repaint();
			}
		}
		int previousLocation = -1;
	}
	

	private UiMenuBar menuBar;
	private UiToolBar toolBar;
	private UiStatusBar statusBar;

	private FolderSplitPane folderSplitter;
	private UiFolderTreePane folderTreePane;
	private JSplitPane previewSplitter;
	private UiBulletinTablePane bulletinsTable;
	private UiBulletinPreviewPane previewPane;
}
