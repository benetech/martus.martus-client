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

import java.awt.Window;
import java.io.File;

import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionDoer;

public abstract class FxInSwingStage extends JFXPanel implements VirtualStage
{
	public FxInSwingStage(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
	}

	protected FxScene createScene() throws Exception
	{
		return new FxScene(getExternalFxmlDirectory(), getCssName());
	}

	abstract protected String getCssName();
	abstract public void showCurrentPage() throws Exception;

	public UiMainWindow getMainWindow()
	{
		return mainWindow;
	}
	
	public FxScene getFxScene()
	{
		return scene;
	}
	
	public void ensureSceneExists() throws Exception
	{
		if(scene == null)
		{
			scene = createScene();
			setScene(scene);
		}
	}
	
	public void setSceneRoot(Parent contents)
	{
		scene.setRoot(contents);
	}
	

	public void setWindow(Window dialogToUse)
	{
		window = dialogToUse;
	}

	public Window getWindow()
	{
		return window;
	}

	public JDialog getDialog()
	{
		return (JDialog) getWindow();
	}

	public MartusApp getApp()
	{
		return getMainWindow().getApp();
	}

	public MartusLocalization getLocalization()
	{
		return getMainWindow().getLocalization();
	}

	public File getExternalFxmlDirectory()
	{
		return getMainWindow().getApp().getFxmlDirectory();
	}

	public FxShellController getShellController()
	{
		return shellController;
	}

	public void setShellController(FxShellController controller)
	{
		shellController = controller;
	}

	public FxInSwingContentController getCurrentController()
	{
		return currentContentController;
	}

	public void setCurrentController(FxInSwingContentController contentControllerToUse)
	{
		currentContentController = contentControllerToUse;
	}

	public void showCurrentPage(FxInSwingContentController contentPaneController) throws Exception
	{
		ensureSceneExists();
		contentPaneController.setShellController(getShellController());
		Parent shellContents = getShellController().createContents();
		getShellController().setStage(this);
		getShellController().setContentPane(contentPaneController);
		setSceneRoot(shellContents);
		getFxScene().applyStyleSheet(getLocalization().getCurrentLanguageCode());
	}
	
	public void doAction(ActionDoer doer)
	{
		try
		{
			SwingUtilities.invokeLater(new Doer(doer));
		} 
		catch (Exception e)
		{
			SwingUtilities.invokeLater(new ShowErrorDialogHandler(e));
		}
	}
	
	private class Doer implements Runnable
	{
		public Doer(ActionDoer doerToRun)
		{
			doer = doerToRun;
		}
		
		@Override
		public void run()
		{
			doer.doAction();
		}
		
		private ActionDoer doer;
	}

	private class ShowErrorDialogHandler implements Runnable
	{
		public ShowErrorDialogHandler(Exception e)
		{
			exceptionToReport = e;
		}

		public void run()
		{
			mainWindow.unexpectedErrorDlg(exceptionToReport);
		}
		Exception exceptionToReport;
	}
	
	public void 	logAndNotifyUnexpectedError(Exception e)
	{
		SwingUtilities.invokeLater(new ShowErrorDialogHandler(e));
	}


	protected UiMainWindow mainWindow;
	private FxScene scene;
	private Window window;
	private FxShellController shellController;
	private FxInSwingContentController currentContentController;
}
