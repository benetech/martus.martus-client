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
import java.awt.GridLayout;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.martus.swing.UiLanguageDirection;
import org.martus.swing.UiPopupMenu;

public class UiMainPane extends JPanel
{
	public UiMainPane(UiMainWindow mainWindowToUse)
	{
		setLayout(new BorderLayout());
		setComponentOrientation(UiLanguageDirection.getComponentOrientation());

		add(createTopStuff(mainWindowToUse), BorderLayout.NORTH);
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
	
	private UiMenuBar menuBar;
	private UiToolBar toolBar;
}
