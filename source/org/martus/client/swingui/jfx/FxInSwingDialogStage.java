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
package org.martus.client.swingui.jfx;

import java.io.File;

import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;

import javax.swing.JDialog;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;

abstract public class FxInSwingDialogStage extends JFXPanel
{
	public FxInSwingDialogStage(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
	}

	abstract protected FxScene createScene() throws Exception;
	abstract public void showCurrentScene() throws Exception;
	
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
	
	public void setDialog(JDialog dialogToUse)
	{
		dialog = dialogToUse;
	}
	
	public JDialog getDialog()
	{
		return dialog;
	}
	
	public void handleNavigationEvent(String navigationNext)
	{
	}

	public UiMainWindow getMainWindow()
	{
		return mainWindow;
	}

	public MartusLocalization getLocalization()
	{
		return getMainWindow().getLocalization();
	}

	public void close()
	{
		getDialog().setVisible(false);
	}

	public File getExternalFxmlDirectory()
	{
		return getMainWindow().getApp().getFxmlDirectory();
	}

	public ShellController getShellController()
	{
		return shellController;
	}
	
	public void setShellController(ShellController controller)
	{
		shellController = controller;
	}
	
	public ContentController getCurrentController() throws Exception
	{
		return currentContentController;
	}

	public void setCurrentController(ContentController contentControllerToUse)
	{
		currentContentController = contentControllerToUse;
	}

	public void showCurrentPage(ContentController contentPaneController) throws Exception
	{
		ensureSceneExists();
		Parent shellContents = getShellController().createContents();
		contentPaneController.setStage(this);
		getShellController().setContentPane(contentPaneController);
		getShellController().setStage(this);
		setSceneRoot(shellContents);
	}

	private JDialog dialog;
	private UiMainWindow mainWindow;
	private FxScene scene;
	private ShellController shellController;
	private ContentController currentContentController;
}
