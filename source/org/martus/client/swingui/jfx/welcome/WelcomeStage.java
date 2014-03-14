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
package org.martus.client.swingui.jfx.welcome;

import javafx.scene.Parent;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.FxInSwingDialogController;
import org.martus.client.swingui.jfx.FxInSwingDialogStage;
import org.martus.client.swingui.jfx.FxScene;

public class WelcomeStage extends FxInSwingDialogStage
{
	public WelcomeStage(UiMainWindow mainWindow) throws Exception
	{
		super(mainWindow);
		shellController = new WelcomeShellController(getMainWindow());
		contentController = new FxWelcomeController(getMainWindow());
	}

	@Override
	public FxInSwingDialogController getCurrentController() throws Exception
	{
		return contentController;
	}

	@Override
	public void showCurrentScene() throws Exception
	{
		super.showCurrentScene();
		
		FxInSwingDialogController contentPaneController = getCurrentController();

		Parent shellContents = shellController.createContents();
		contentPaneController.setStage(this);
		shellController.setContentPane(contentPaneController);
		shellController.setStage(this);
		setSceneRoot(shellContents);
	}

	@Override
	protected FxScene createScene() throws Exception
	{
		return new WelcomeScene(getExternalFxmlDirectory());
	}

	private WelcomeShellController shellController;
	private FxInSwingDialogController contentController;
}
