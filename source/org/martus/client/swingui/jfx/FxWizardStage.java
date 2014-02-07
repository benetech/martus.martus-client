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
import java.util.Stack;

import javafx.scene.Parent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.setupwizard.AbstractFxSetupWizardController;
import org.martus.client.swingui.jfx.setupwizard.FxSetupWizardTemplateController;
import org.martus.common.MartusLogger;

abstract public class FxWizardStage extends FxStage
{
	public FxWizardStage(UiMainWindow mainWindowToUse) throws Exception
	{
		super(mainWindowToUse);
	
		visitedWizardPagesStack = new Stack<FxController>();
		wizardTemplateController = new FxSetupWizardTemplateController(getMainWindow());
		
		currentController = getFirstController();
	}
	
	@Override
	public void showCurrentScene() throws Exception
	{
		if(scene == null)
		{
			scene = createScene();
			setScene(scene);
		}
		
		Parent wizardTemplateContents = wizardTemplateController.createContents();
		AbstractFxSetupWizardController contentPaneController = (AbstractFxSetupWizardController) getCurrentController();
		wizardTemplateController.setContentPane(contentPaneController);
		wizardTemplateController.setStage(this);
		scene.setRoot(wizardTemplateContents);
	}
	
	@Override
	public FxController getCurrentController() throws Exception
	{
		return currentController;
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
			visitedWizardPagesStack.push(currentController);
			AbstractFxSetupWizardController contentPaneController = (AbstractFxSetupWizardController) getCurrentController();
			if(contentPaneController.getNextControllerClassName() == null)
			{
				getShell().setVisible(false);
			}
			else
			{
				currentController = contentPaneController.getNextControllerClassName();
				showCurrentScene();
			}
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
			if(visitedWizardPagesStack.isEmpty())
			{
				getShell().setVisible(false);
			}
			else
			{
				currentController = visitedWizardPagesStack.pop();
				showCurrentScene();
			}
		}
		catch(Exception e)
		{
			MartusLogger.logException(e);
			getShell().dispose();
		}
	}
	
	@Override
	public void setShell(JDialog shellToUse)
	{
		super.setShell(shellToUse);
		shellToUse.addWindowListener(new WindowCloseHandler(getMainWindow()));
	}
	
	private class WindowCloseHandler extends WindowAdapter
	{
		public WindowCloseHandler(UiMainWindow ownerToUse)
		{
			owner = ownerToUse;
		}

		@Override
		public void windowClosing(WindowEvent e)
		{
			int result = JOptionPane.showConfirmDialog(getShell(), "Wizard will now close.  Are you sure?", "Confirmation", JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION)
			{
				owner.exitWithoutSavingState();
				super.windowClosing(e);
			}
		}
		
		private UiMainWindow owner;
	}
	
	abstract protected FxController getFirstController();

	abstract protected FxScene createScene() throws Exception;
	
	public static final String NAVIGATION_NEXT = "Next";
	public static final String NAVIGATION_BACK = "Back";

	private FxController currentController;
	private FxScene scene;
	private Stack<FxController> visitedWizardPagesStack;
	
	private FxSetupWizardTemplateController wizardTemplateController;
}
