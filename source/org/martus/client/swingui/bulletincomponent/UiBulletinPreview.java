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

import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import org.martus.client.swingui.UiMainWindow;
import org.martus.common.bulletin.Bulletin;

public class UiBulletinPreview extends JScrollPane
{
    public UiBulletinPreview(UiMainWindow mainWindow)
	{
		view = new UiBulletinView(mainWindow);

		getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		getViewport().add(view);
	}

	public Bulletin getCurrentBulletin()
	{
		return currentBulletin;
	}

	public JComponent getView()
	{
		return view;
	}

	public void setCurrentBulletin(Bulletin b)
	{
		if(currentBulletin != null && b != null &&
				b.getUniversalId().equals(currentBulletin.getUniversalId()))
		{
			//System.out.println("UiBulletinPreview.refresh: skipping");
			return;
		}

		currentBulletin = b;
		try
		{
			view.copyDataFromBulletin(b);
		}
		catch(IOException e)
		{
			System.out.println("UiBulletinPreview.refresh: " + e);
		}

		boolean isEncrypted = false;
		if(b != null && b.isAllPrivate())
			isEncrypted = true;
		indicateEncrypted(isEncrypted);
	}

	public void bulletinContentsHaveChanged(Bulletin b)
	{
		if(currentBulletin == null)
			return;

		if(b.getLocalId().equals(currentBulletin.getLocalId()))
			setCurrentBulletin(b);
	}

	private void indicateEncrypted(boolean isEncrypted)
	{
		view.updateEncryptedIndicator(isEncrypted);
	}

	Bulletin currentBulletin;
	UiBulletinView view = null;
}
