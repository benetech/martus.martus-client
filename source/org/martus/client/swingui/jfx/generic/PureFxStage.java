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

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionDoer;


public class PureFxStage extends VirtualStage
{
	public PureFxStage(UiMainWindow mainWindowToUse, FxPopupController controller)
	{
		this(mainWindowToUse, controller.getDialogTitle(), new Stage());
		controller.setStage(this);
		stage.initModality(Modality.APPLICATION_MODAL);
	}

	public PureFxStage(UiMainWindow mainWindowToUse, String title, Stage stageToUse)
	{
		mainWindow = mainWindowToUse;
		stage = stageToUse;
		
		stage.setTitle(title);
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
		return stage.getWidth();
	}
	
	@Override
	public void showCurrentPage() throws Exception
	{
		// FIXME: Is this even needed?
	}
	
	@Override
	public void unexpectedErrorDlg(Exception e)
	{
		mainWindow.unexpectedErrorDlg(e);
	}

	@Override
	public void close()
	{
		stage.close();
	}

	public void setOnCloseRequest(EventHandler<WindowEvent> closeEventHandler)
	{
		stage.setOnCloseRequest(closeEventHandler);
	}

	public void initStyle(StageStyle style)
	{
		stage.initStyle(style);
	}

	public void setScene(Scene scene)
	{
		stage.setScene(scene);
	}

	public Stage getActualStage()
	{
		return stage;
	}

	public void showAndWait()
	{
		stage.showAndWait();
	}
	
	public UiMainWindow getMainWindow()
	{
		return mainWindow;
	}

	private UiMainWindow mainWindow;
	private Stage stage;
}
