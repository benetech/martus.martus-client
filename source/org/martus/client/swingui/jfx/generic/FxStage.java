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

import javafx.stage.Modality;
import javafx.stage.Stage;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionDoer;


public class FxStage extends Stage implements VirtualStage
{
	public FxStage(UiMainWindow mainWindowToUse, FxPopupController controller)
	{
		mainWindow = mainWindowToUse;
		controller.setStage(this);

		setTitle(controller.getDialogTitle());
		initModality(Modality.APPLICATION_MODAL);
	}

	@Override
	public void doAction(ActionDoer doer)
	{
		// NOTE: We are already on the JavaFX thread
		doer.doAction();
	}

	@Override
	public void logAndNotifyUnexpectedError(Exception e)
	{
		mainWindow.unexpectedErrorDlg(e);
	}
	
	@Override
	public double getWidthAsDouble()
	{
		return getWidth();
	}
	
	@Override
	public void showCurrentPage() throws Exception
	{
		throw new RuntimeException("Not implemented yet");
	}
	
	@Override
	public void unexpectedErrorDlg(Exception e)
	{
		mainWindow.unexpectedErrorDlg(e);
	}

	private UiMainWindow mainWindow;
}
