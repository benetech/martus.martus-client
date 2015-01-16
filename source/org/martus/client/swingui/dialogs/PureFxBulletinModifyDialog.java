/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2015, Beneficent
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
package org.martus.client.swingui.dialogs;

import javax.swing.JFrame;

import org.martus.client.swingui.UiMainWindow;
import org.martus.common.bulletin.Bulletin;

public class PureFxBulletinModifyDialog extends UiBulletinModifyDlg
{
	public PureFxBulletinModifyDialog(Bulletin b, UiMainWindow observerToUse) throws Exception
	{
		super(b, observerToUse);

		// TODO: Work in progress
//		FxBulletinEditorShellController bulletinEditorShellController = new FxBulletinEditorShellController(observerToUse, this);
//		view = bulletinEditorShellController;
//		
//		PureFxDialogStage dialogStage = new PureFxDialogStage(getMainWindow(), bulletinEditorShellController); 
//		dialogStage.showCurrentPage();
//		safelyPopulateView();
//		// FIXME: We should restore the dialog position/size here
//		dialogStage.showAndWait();
//		observer.doneModifyingBulletin();
//		// FIXME: We should save the dialog position/size here
//		saveEditorState(getSwingFrame().getSize(), getSwingFrame().getLocation());
	}

	@Override
	public JFrame getSwingFrame()
	{
		return null;
	}

	@Override
	public void dispose()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setVisible(boolean newState)
	{
		// TODO Auto-generated method stub
		
	}

}
