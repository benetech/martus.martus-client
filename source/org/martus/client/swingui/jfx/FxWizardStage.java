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
import org.martus.client.swingui.jfx.setupwizard.AbstractFxSetupWizardContentController;
import org.martus.client.swingui.jfx.setupwizard.FxSetupWizardTemplateController;
import org.martus.common.MartusLogger;

abstract public class FxWizardStage extends FxInSwingDialogStage
{
	public FxWizardStage(UiMainWindow mainWindowToUse) throws Exception
	{
		super(mainWindowToUse);
	
		visitedWizardPagesStack = new Stack<FxInSwingDialogController>();
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
		AbstractFxSetupWizardContentController contentPaneController = (AbstractFxSetupWizardContentController) getCurrentController();
		contentPaneController.setFxStage(this);
		wizardTemplateController.setContentPane(contentPaneController);
		wizardTemplateController.setFxStage(this);
		scene.setRoot(wizardTemplateContents);
		wizardTemplateController.getNextButton().setDefaultButton(true);
	}
	
	@Override
	public FxInSwingDialogController getCurrentController() throws Exception
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
	
	public void next()
	{
		try
		{
			AbstractFxSetupWizardContentController contentPaneController = (AbstractFxSetupWizardContentController) getCurrentController();
			FxInSwingDialogController nextController = contentPaneController.getNextControllerClassName();

			visitedWizardPagesStack.push(currentController);
			if(nextController == null)
			{
				close();
			}
			else
			{
				currentController = nextController;
				showCurrentScene();
			}
		}
		catch(Exception e)
		{
			MartusLogger.logException(e);
			getMainWindow().exitWithoutSavingState();
		}
	}

	private void back()
	{
		try
		{
			if(visitedWizardPagesStack.isEmpty())
			{
				close();
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
			getMainWindow().exitWithoutSavingState();
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
	
	abstract protected FxInSwingDialogController getFirstController();

	abstract protected FxScene createScene() throws Exception;
	
	public static final String NAVIGATION_NEXT = "Next";
	public static final String NAVIGATION_BACK = "Back";

	private FxInSwingDialogController currentController;
	private FxScene scene;
	private Stack<FxInSwingDialogController> visitedWizardPagesStack;
	
	private FxSetupWizardTemplateController wizardTemplateController;
}
