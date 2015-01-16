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
package org.martus.client.swingui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;

import javafx.application.Platform;

import javax.swing.Box;
import javax.swing.JFrame;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.UiSession;
import org.martus.client.swingui.WindowObscurer;
import org.martus.client.swingui.bulletincomponent.UiBulletinEditor;
import org.martus.client.swingui.jfx.generic.FxInSwingStage;
import org.martus.client.swingui.jfx.generic.FxRunner;
import org.martus.client.swingui.jfx.landing.bulletins.FxBulletinEditorShellController;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.bulletin.Bulletin;
import org.martus.swing.UiButton;
import org.martus.swing.Utilities;

public class FxInSwingBulletinModifyDialog extends UiBulletinModifyDlg
{
	public FxInSwingBulletinModifyDialog(Bulletin b, UiMainWindow observerToUse) throws Exception
	{
		super(b, observerToUse);

		realFrame = new JFrame();
		UiMainWindow.updateIcon(getSwingFrame());
		getSwingFrame().setTitle(getLocalization().getWindowTitle("create"));
		
		if(UiSession.isJavaFx())
		{
			FxBulletinEditorShellController bulletinEditorShellController = new FxBulletinEditorShellController(observerToUse, this);

			FxInSwingStage bulletinEditorStage = FxRunner.createAndActivateEmbeddedStage(observerToUse, getSwingFrame(), bulletinEditorShellController);
			setView(bulletinEditorShellController);
			Platform.runLater(() -> safelyPopulateView());
			getSwingFrame().getContentPane().add(bulletinEditorStage.getPanel(), BorderLayout.CENTER);
		}
		else
		{
			setView(new UiBulletinEditor(getMainWindow()));
			getView().copyDataFromBulletin(getBulletin());
			getView().setLanguageChangeListener(new LanguageChangeHandler());

			UiButton send = new UiButton(getLocalization().getButtonLabel("send"));
			send.addActionListener(new SendHandler());
			UiButton draft = new UiButton(getLocalization().getButtonLabel("savedraft"));
			draft.addActionListener(new SaveHandler());
			UiButton cancel = new UiButton(getLocalization().getButtonLabel(EnglishCommonStrings.CANCEL));
			cancel.addActionListener(new CancelHandler());

			addScrollerView();

			Box box = Box.createHorizontalBox();
			Component buttons[] = {send, draft, cancel, Box.createHorizontalGlue()};
			Utilities.addComponentsRespectingOrientation(box, buttons);
			getSwingFrame().getContentPane().add(box, BorderLayout.SOUTH);
		}


		getSwingFrame().setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		getSwingFrame().addWindowListener(new WindowEventHandler());

		Dimension screenSize = Utilities.getViewableScreenSize();
		Dimension editorDimension = observerToUse.getBulletinEditorDimension();
		Point editorPosition = observerToUse.getBulletinEditorPosition();
		boolean showMaximized = false;
		if(Utilities.isValidScreenPosition(screenSize, editorDimension, editorPosition))
		{
			getSwingFrame().setLocation(editorPosition);
			getSwingFrame().setSize(editorDimension);
			if(observerToUse.isBulletinEditorMaximized())
				showMaximized = true;
		}
		else
			showMaximized = true;
		if(showMaximized)
		{
			getSwingFrame().setSize(screenSize.width - 50, screenSize.height - 50);
			Utilities.maximizeWindow(getSwingFrame());
		}
		
		if(!UiSession.isJavaFx())
			getView().scrollToTop();
		
		getSwingFrame().setGlassPane(new WindowObscurer());
		
	}

	public void dispose()
	{
		getSwingFrame().dispose();
	}
	
	public void setVisible(boolean newVisibility)
	{
		getSwingFrame().setVisible(newVisibility);
	}
	
	@Override
	public JFrame getSwingFrame()
	{
		return realFrame;
	}
	
	private JFrame realFrame;
	
}
