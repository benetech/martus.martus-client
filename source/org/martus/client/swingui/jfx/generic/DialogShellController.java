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

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionDoer;

abstract public class DialogShellController extends FxShellWithSingleContentController implements ActionDoer 
{
	public DialogShellController(UiMainWindow mainWindowToUse, FxController contentController)
	{
		super(mainWindowToUse, contentController);
	}

	@Override
	protected Pane getContentPane()
	{
		return contentPane;
	}
	
	@Override
	public void doAction()
	{
		UiMainWindow mainWindow = getMainWindow();
		try
		{
			DialogStageWithCss stage = new DialogStageWithCss(mainWindow, this, getContentController().getCssName());
			FxModalDialog.createAndShow(mainWindow, stage);
		} 
		catch (Exception e)
		{
			mainWindow.unexpectedErrorDlg(e);
		}
	}
	
	@FXML
	Pane contentPane;
}
