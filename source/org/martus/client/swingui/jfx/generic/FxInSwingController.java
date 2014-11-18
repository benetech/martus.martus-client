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

import java.awt.Component;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javafx.application.Platform;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.TranslucentWindowObscurer;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.filefilters.BulletinXmlFileFilter;
import org.martus.client.swingui.filefilters.MCTFileFilter;
import org.martus.client.swingui.filefilters.MartusBulletinArchiveFileFilter;
import org.martus.clientside.FormatFilter;

public abstract class FxInSwingController extends FxController
{
	public FxInSwingController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);

		glassPaneInstaller = new GlassPaneInstaller(this);
	}
	
	public void installGlassPane(Component glassPane)
	{
		glassPaneInstaller.installGlassPane(glassPane);
	}

	protected void showModalPopupStage(Stage popupStage)
	{
		Runnable fronter = new Fronter(popupStage);
	
		Window window = getWindow();
		DialogWindowHandler windowHandler = new DialogWindowHandler(fronter);
		window.addWindowListener(windowHandler);
		window.addWindowFocusListener(windowHandler);
		
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
	
			window.removeWindowFocusListener(windowHandler);
			window.removeWindowListener(windowHandler);
			
			glassPane.setVisible(false);
		}
	}

	public Window getWindow()
	{
		return getSwingStage().getWindow();
	}

	public FxInSwingStage getSwingStage()
	{
		return (FxInSwingStage) getStage();
	}
	
	protected boolean isMctFileFilterSelected(ExtensionFilter chosenExtensionFilter, File file)
	{
		FormatFilter mctFileFilter = new MCTFileFilter(getLocalization());
		String[] extensions = chosenExtensionFilter.getExtensions().toArray(new String[0]);
		return isExtensionSelected(extensions, file, mctFileFilter);
	}

	public static boolean isMctFileFilterSelected(MartusLocalization localization, FormatFilter chosenExtensionFilter, File file)
	{
		FormatFilter mctFileFilter = new MCTFileFilter(localization);
		return isExtensionSelected(chosenExtensionFilter, file, mctFileFilter);
	}
	
	protected boolean isMctFileFilterSelected(FormatFilter chosenExtensionFilter, File file)
	{
		FormatFilter mctFileFilter = new MCTFileFilter(getLocalization());
		return isExtensionSelected(chosenExtensionFilter, file, mctFileFilter);
	}

	protected boolean isXmlExtensionSelected(ExtensionFilter chosenExtensionFilter, File file)
	{
		FormatFilter xmlFileFilter = new BulletinXmlFileFilter(getLocalization());
		String[] extensions = chosenExtensionFilter.getExtensions().toArray(new String[0]);
		return isExtensionSelected(extensions, file, xmlFileFilter);
	}
	
	public static boolean isXmlExtensionSelected(MartusLocalization localization, FormatFilter chosenExtensionFilter, File file)
	{
		FormatFilter xmlFileFilter = new BulletinXmlFileFilter(localization);
		return isExtensionSelected(chosenExtensionFilter, file, xmlFileFilter);
	}
	
	public static boolean isMbaExtensionSelected(MartusLocalization localization, FormatFilter chosenExtensionFilter, File file)
	{
		FormatFilter xmlFileFilter = new MartusBulletinArchiveFileFilter(localization);
		return isExtensionSelected(chosenExtensionFilter, file, xmlFileFilter);
	}
	
	private static boolean isExtensionSelected(FormatFilter chosenExtensionFilter, File file, FormatFilter mctFileFilter)
	{
		return isExtensionSelected(chosenExtensionFilter.getExtensions(), file, mctFileFilter);
	}

	public static boolean isExtensionSelected(String[] extensions, File file, FormatFilter mctFileFilter)
	{
		if (mctFileFilter.accept(file))
			return true;
		
		for (String extension : extensions)
		{
			if (extension.contains(mctFileFilter.getExtension()))
				return true;
		}
		
		return false;
	}
	
	protected static class DialogWindowHandler extends WindowAdapter implements MouseMotionListener
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

	protected static class GlassPaneMouseHandler extends MouseAdapter
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

	protected static class Fronter implements Runnable
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

	private GlassPaneInstaller glassPaneInstaller;
}
