/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2003, Beneficent
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

package org.martus.client.swingui.bulletincomponent;

import java.util.Vector;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiBoolViewer;
import org.martus.client.swingui.fields.UiField;

public class UiBulletinView extends UiBulletinComponent
{
	UiBulletinView(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		mainWindow = mainWindowToUse;
		bulletinViewSections = new Vector();
		// ensure that attachmentViewer gets initialized
	}

	public UiBulletinComponentSection createBulletinComponentSection(boolean encrypted)
	{
		UiBulletinComponentViewSection section = new UiBulletinComponentViewSection(this, mainWindow, encrypted);
		bulletinViewSections.add(section);
		return section;
	}

	public UiField createBoolField()
	{
		return new UiBoolViewer(mainWindow.getLocalization());
	}

	private Vector bulletinViewSections;
}
