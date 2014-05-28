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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javafx.scene.Parent;

import javax.swing.JDialog;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;

abstract public class FxInSwingDialogStage extends FxInSwingStage
{
	public FxInSwingDialogStage(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
	}

	abstract protected boolean confirmExit();
	
	public void setDialog(JDialog dialogToUse)
	{
		setWindow(dialogToUse);
		getDialog().addWindowListener(createWindowCloseHandler());
	}

	public WindowListener createWindowCloseHandler()
	{
		return new WindowCloseHandler();
	}
	
	public JDialog getDialog()
	{
		return (JDialog) getWindow();
	}

	public void handleNavigationEvent(String navigationNext)
	{
	}

	public UiMainWindow getMainWindow()
	{
		return mainWindow;
	}
	
	public MartusApp getApp()
	{
		return getMainWindow().getApp();
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
		contentPaneController.setFxInSwingDialogStage(this);
		Parent shellContents = getShellController().createContents();
		getShellController().setFxInSwingDialogStage(this);
		getShellController().setContentPane(contentPaneController);
		setSceneRoot(shellContents);
		getFxScene().applyStyleSheet(getLocalization().getCurrentLanguageCode());
	}

	protected void handleDialogClose()
	{
		close();
	}
	
	protected class WindowCloseHandler extends WindowAdapter
	{
		@Override
		public void windowClosing(WindowEvent e)
		{
			if(confirmExit())
			{
				handleDialogClose();
				super.windowClosing(e);
			}
		}
	}

	private UiMainWindow mainWindow;
	private ShellController shellController;
	private ContentController currentContentController;
}
