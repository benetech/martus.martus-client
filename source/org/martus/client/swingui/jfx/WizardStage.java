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

import java.util.Vector;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.MartusLogger;

abstract public class WizardStage extends MartusStage
{
	public WizardStage(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
		
		scenes = new Vector<MartusSceneFactory>();
		currentSceneIndex = 0;
	}
	
	public UiMainWindow getMainWindow()
	{
		return mainWindow;
	}
	
	public MartusLocalization getLocalization()
	{
		return getMainWindow().getLocalization();
	}
	
	@Override
	public void showCurrentScene() throws Exception
	{
		MartusSceneFactory sceneFactory = getCurrentSceneFactory();
		MartusScene scene = sceneFactory.createScene();
		MartusController controller = sceneFactory.getController();
		controller.setStage(this);
		setScene(scene);
	}

	protected void addSceneFactory(int i, MartusSceneFactory sceneFactory)
	{
		scenes.add(i, sceneFactory);
	}

	@Override
	public MartusSceneFactory getCurrentSceneFactory() throws Exception
	{
		return scenes.get(currentSceneIndex);
	}

	public void handleNavigationEvent(String navigationNext)
	{
		try
		{
			++currentSceneIndex;
			if(currentSceneIndex >= scenes.size())
				getShell().setVisible(false);
			else
				showCurrentScene();
		}
		catch(Exception e)
		{
			MartusLogger.logException(e);
			getShell().dispose();
		}
	}

	public static final String NAVIGATION_NEXT = "Next";

	private UiMainWindow mainWindow;
	private int currentSceneIndex;
	private Vector<MartusSceneFactory> scenes;
}