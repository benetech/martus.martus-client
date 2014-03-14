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
import org.martus.client.swingui.jfx.setupwizard.FxSetupWizardShellController;
import org.martus.common.MartusLogger;

abstract public class FxWizardStage extends FxInSwingDialogStage
{
	public FxWizardStage(UiMainWindow mainWindowToUse) throws Exception
	{
		super(mainWindowToUse);
	
		visitedWizardPagesStack = new Stack<ContentController>();
		shellController = new FxSetupWizardShellController(getMainWindow());
		
		currentController = getFirstController();
	}
	
	@Override
	public void showCurrentScene() throws Exception
	{
		AbstractFxSetupWizardContentController contentPaneController = (AbstractFxSetupWizardContentController) getCurrentController();

		showCurrentPage(contentPaneController);
		
		getShellController().getNextButton().setDefaultButton(true);
	}

	public void showCurrentPage(AbstractFxSetupWizardContentController contentPaneController) throws Exception
	{
		ensureSceneExists();
		Parent shellContents = getShellController().createContents();
		contentPaneController.setStage(this);
		getShellController().setContentPane(contentPaneController);
		getShellController().setStage(this);
		setSceneRoot(shellContents);
	}

	public ShellController getShellController()
	{
		return shellController;
	}
	
	@Override
	public ContentController getCurrentController() throws Exception
	{
		return currentController;
	}

	public void next()
	{
		try
		{
			AbstractFxSetupWizardContentController contentPaneController = (AbstractFxSetupWizardContentController) getCurrentController();
			ContentController nextController = contentPaneController.getNextController();

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

	public void back()
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
	public void setDialog(JDialog shellToUse)
	{
		super.setDialog(shellToUse);
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
			int result = JOptionPane.showConfirmDialog(getDialog(), "Wizard will now close.  Are you sure?", "Confirmation", JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION)
			{
				owner.exitWithoutSavingState();
				super.windowClosing(e);
			}
		}
		
		private UiMainWindow owner;
	}
	
	abstract protected ContentController getFirstController();

	private ContentController currentController;
	private Stack<ContentController> visitedWizardPagesStack;
	
	private ShellController shellController;
}
