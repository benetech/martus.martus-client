/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2015, Beneficent
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
package org.martus.client.swingui;

import java.awt.Cursor;

import javafx.application.Platform;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.martus.client.swingui.jfx.generic.FxRunner;
import org.martus.client.swingui.jfx.generic.FxStatusBar;
import org.martus.client.swingui.jfx.landing.FxInSwingMainStage;
import org.martus.client.swingui.jfx.landing.FxMainStage;

public class SwingMainWindow extends UiMainWindow
{
	public SwingMainWindow() throws Exception
	{
		super();
	}
	
	@Override
	protected void initializeFrame()
	{
		swingFrame = new MainSwingFrame(this);
		UiMainWindow.updateIcon(getSwingFrame());
		setCurrentActiveFrame(getSwingFrame());
		getSwingFrame().setVisible(true);
		updateTitle();
		setWindowSizeAndState();

		if(UiSession.isJavaFx())
		{
			FxInSwingMainStage fxInSwingMainStage = new FxInSwingMainStage(this);
			mainStage = fxInSwingMainStage;
			FxRunner fxRunner = new FxRunner(fxInSwingMainStage);
			fxRunner.setAbortImmediatelyOnError();
			Platform.runLater(fxRunner);
			getSwingFrame().setContentPane(fxInSwingMainStage.getPanel());
		}
		else
		{
			mainPane = new UiMainPane(this, getUiState());
			getSwingFrame().setContentPane(mainPane);
		}
	}

	@Override
	public StatusBar createStatusBar()
	{
		if(UiSession.isJavaFx())
			return new FxStatusBar(getLocalization());
		
		return new UiStatusBar(getLocalization());
	}

	@Override
	public JFrame getSwingFrame()
	{
		return swingFrame;
	}

	@Override
	public UiMainPane getMainPane()
	{
		return mainPane;
	}
	
	@Override
	public FxMainStage getMainStage()
	{
		return mainStage;
	}

	private void updateTitle() 
	{
		getSwingFrame().setTitle(getLocalization().getWindowTitle("main"));
	}
	
	@Override
	public void rawError(String errorText)
	{
		JOptionPane.showMessageDialog(null, errorText, "ERROR", JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void rawSetCursor(Object newCursor)
	{
		getSwingFrame().setCursor((Cursor)newCursor);
	}

	@Override
	public Object getWaitCursor()
	{
		return Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
	}

	@Override
	public Object getExistingCursor()
	{
		return getSwingFrame().getCursor();
	}
	
	@Override
	protected void showMainWindow()
	{
		getSwingFrame().setVisible(true);
		getSwingFrame().toFront();
	}

	@Override
	protected void obscureMainWindow()
	{
		getSwingFrame().setLocation(100000, 0);
		getSwingFrame().setSize(0,0);
		getSwingFrame().setEnabled(false);
	}

	private JFrame swingFrame;
	private UiMainPane mainPane;
	private FxMainStage mainStage;
}
