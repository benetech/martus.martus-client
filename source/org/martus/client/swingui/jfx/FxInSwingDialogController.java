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

import java.awt.Component;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javafx.application.Platform;
import javafx.stage.Stage;

import javax.swing.JDialog;

import org.martus.client.swingui.TranslucentWindowObscurer;
import org.martus.client.swingui.UiMainWindow;

abstract public class FxInSwingDialogController extends FxInSwingController
{
	public FxInSwingDialogController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	public void setStage(FxInSwingDialogStage stageToUse)
	{
		stage = stageToUse;
	}

	public FxInSwingDialogStage getStage()
	{
		return stage;
	}
	
	public FxScene getScene()
	{
		return getStage().getFxScene();
	}
	
	protected void showModalPopupStage(Stage popupStage)
	{
		Runnable fronter = new Fronter(popupStage);

		JDialog dialog = getDialog();
		DialogWindowHandler windowHandler = new DialogWindowHandler(fronter);
		dialog.addWindowListener(windowHandler);
		dialog.addWindowFocusListener(windowHandler);
		
		Component glassPane = new TranslucentWindowObscurer();
		installGlassPane(glassPane);
		GlassPaneMouseHandler glassPaneMouseHandler = new GlassPaneMouseHandler(fronter);
		glassPane.addMouseListener(glassPaneMouseHandler);
		glassPane.addMouseMotionListener(glassPaneMouseHandler);
		
		glassPane.setVisible(true);
		try
		{
			popupStage.showAndWait();
		}
		finally
		{
			glassPane.removeMouseMotionListener(glassPaneMouseHandler);
			glassPane.removeMouseListener(glassPaneMouseHandler);

			dialog.removeWindowFocusListener(windowHandler);
			dialog.removeWindowListener(windowHandler);
			
			glassPane.setVisible(false);
		}
	}

	public JDialog getDialog()
	{
		return (JDialog) getWindow();
	}

	public Window getWindow()
	{
		return getStage().getDialog();
	}

	public void installGlassPane(Component glassPane)
	{
		getDialog().setGlassPane(glassPane);
	}
	
	private static class DialogWindowHandler extends WindowAdapter implements MouseMotionListener
	{
		public DialogWindowHandler(Runnable runOnFocusGained)
		{
			task = runOnFocusGained;
		}
		
		@Override
		public void windowDeiconified(WindowEvent e)
		{
			Platform.runLater(task);
		}

		@Override
		public void windowActivated(WindowEvent e)
		{
			Platform.runLater(task);
		}

		@Override
		public void windowOpened(WindowEvent e)
		{
			Platform.runLater(task);
		}

		@Override
		public void windowGainedFocus(WindowEvent e)
		{
			Platform.runLater(task);
		}

		@Override
		public void mouseDragged(MouseEvent e)
		{
		}

		@Override
		public void mouseMoved(MouseEvent e)
		{
			Platform.runLater(task);
		}

		private Runnable task;
	}

	private static class GlassPaneMouseHandler extends MouseAdapter
	{
		public GlassPaneMouseHandler(Runnable runOnClick)
		{
			task = runOnClick;
		}
		
		@Override
		public void mouseClicked(MouseEvent e)
		{
			super.mouseClicked(e);
			Platform.runLater(task);
		}
		
		@Override
		public void mouseMoved(MouseEvent e)
		{
			super.mouseMoved(e);
			Platform.runLater(task);
		}
		
		private Runnable task;
	}
	
	private static class Fronter implements Runnable
	{
		public Fronter(Stage popupStageToUse)
		{
			popupStage = popupStageToUse;
		}
		
		@Override
		public void run()
		{
			popupStage.toFront();
		}
		
		private Stage popupStage;
	}

	private FxInSwingDialogStage stage;
}
