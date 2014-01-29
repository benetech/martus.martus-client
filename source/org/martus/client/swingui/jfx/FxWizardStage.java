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

import javafx.scene.Parent;
import javafx.scene.layout.Region;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.setupwizard.FxWizardController;
import org.martus.client.swingui.jfx.setupwizard.WizardTemplateController;
import org.martus.common.MartusLogger;

abstract public class FxWizardStage extends FxStage
{
	public FxWizardStage(UiMainWindow mainWindowToUse) throws Exception
	{
		super(mainWindowToUse);
	
		controllers = new Vector<FxController>();
		currentControllerIndex = 0;
		wizardTemplateController = new WizardTemplateController(getMainWindow());
	}
	
	@Override
	public void showCurrentScene() throws Exception
	{
		if(scene == null)
		{
			scene = new FxScene(new Region());
			setScene(scene);
		}
		
		Parent wizardTemplateContents = wizardTemplateController.createContents();
		FxWizardController rightSideController = (FxWizardController) getCurrentController();
		rightSideController.setNavigationHandler(wizardTemplateController);
		Parent createContents = rightSideController.createContents();
		wizardTemplateController.setRightSideContentPane(createContents);
		wizardTemplateController.setStage(this);
		scene.setRoot(wizardTemplateContents);
	}
	
	protected void addController(FxController sceneFactory)
	{
		controllers.add(sceneFactory);
	}

	@Override
	public FxController getCurrentController() throws Exception
	{
		return controllers.get(currentControllerIndex);
	}

	public void handleNavigationEvent(String navigationType)
	{
		if(navigationType.equals(NAVIGATION_BACK))
			back();
		else if(navigationType.equals(NAVIGATION_NEXT))
			next();
	}
	
	private void next()
	{
		try
		{
			++currentControllerIndex;
			if(currentControllerIndex >= controllers.size())
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

	private void back()
	{
		try
		{
			--currentControllerIndex;
			if(currentControllerIndex < 0)
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
	public static final String NAVIGATION_BACK = "Back";

	private int currentControllerIndex;
	private Vector<FxController> controllers;
	private FxScene scene;
	
	private WizardTemplateController wizardTemplateController;
}
