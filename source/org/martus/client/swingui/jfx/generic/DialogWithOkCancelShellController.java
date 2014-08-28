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
import javafx.scene.control.Button;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionDoer;

public class DialogWithOkCancelShellController extends DialogShellController implements ActionDoer
{
	public DialogWithOkCancelShellController(UiMainWindow mainWindowToUse, FxController contentController)
	{
		super(mainWindowToUse, contentController);
	}

	@Override
	public void doAction()
	{
		UiMainWindow mainWindow = getMainWindow();
		try
		{
			FxModalDialog.createAndShow(mainWindow, new DialogWithOkCancelStage(mainWindow, this));
		} 
		catch (Exception e)
		{
			mainWindow.unexpectedErrorDlg(e);
		}
	}

	@Override
	public String getFxmlLocation()
	{
		return LOCATION_DIALOG_WITH_OK_CANCEL_SHELL;
	}
	
	@FXML
	public void onOkClicked()
	{
		getContentController().save();
		close();
	}
	
	@FXML
	public void onCancelClicked()
	{
		close();
	}
	
	protected void setOkButtonText(String newText)
	{
		ok.setText(newText);
	}
	
	protected void setOkButtonSetDisabled(boolean isDisabled)
	{
		ok.setDisable(isDisabled);
	}
	
	private static final String LOCATION_DIALOG_WITH_OK_CANCEL_SHELL = "generic/DialogWithOkCancelShell.fxml";
	
	@FXML
	private Button ok;
}
