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

import java.awt.Dialog;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.WindowObscurer;
import org.martus.swing.Utilities;

public class FxInSwingModalDialog extends JDialog
{
	public static void createAndShowLargeModalDialog(UiMainWindow owner, FxInSwingDialogStage stage) throws Exception
	{
		createAndShowDialog(owner, stage, EMPTY_TITLE, LARGE_PREFERRED_DIALOG_ZIZE);
	}
	
	public static void createAndShowConfirmationSizedDialog(UiMainWindow owner, String titleTag, FxNonWizardShellController dialogShellController) throws Exception
	{
		createAndShowModalDialog(owner, dialogShellController, SMALL_PREFERRED_DIALOG_SIZE, titleTag);
	}

	public static void createAndShowModalDialog(UiMainWindow mainWindow, FxNonWizardShellController controller, Dimension preferedDimension, String titleTag)
	{
		DialogStage stage = new DialogStage(mainWindow, controller);
		createAndShowDialog(mainWindow, stage, titleTag, preferedDimension);
	}

	private static void createAndShowDialog(UiMainWindow owner, FxInSwingDialogStage stage, String titleTag, Dimension dimension)
	{
		if (dimension == null)
			dimension = LARGE_PREFERRED_DIALOG_ZIZE;
		
		FxInSwingModalDialog dialog = createDialog(owner);
		if (titleTag.length() > 0)
			dialog.setTitle(owner.getLocalization().getWindowTitle(titleTag));
		
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.getContentPane().setPreferredSize(dimension);
		dialog.pack();
		dialog.getContentPane().add(stage.getPanel());
		stage.setDialog(dialog);
		stage.runOnFxThreadMaybeLater(new FxRunner(stage));

		Utilities.packAndCenterWindow(dialog);
		owner.setCurrentActiveDialog(dialog);
		dialog.setVisible(true);
		owner.setCurrentActiveDialog(null);
	}

	private static FxInSwingModalDialog createDialog(UiMainWindow owner)
	{
		JFrame frame = owner.getSwingFrame();
		if(frame != null)
			return new FxInSwingModalDialog(frame);

		return new FxInSwingModalDialog();
	}
	
	public FxInSwingModalDialog()
	{
		// NOTE: Pass (Dialog)null to force this window to show up in the Task Bar
		super((Dialog)null);
		
		initialize();
	}
	
	public FxInSwingModalDialog(JFrame owner)
	{
		super(owner);

		initialize();
	}

	public void initialize()
	{
		setModal(true);
		setGlassPane(new WindowObscurer());
	}
	
	public static final Dimension SMALL_PREFERRED_DIALOG_SIZE = new Dimension(400, 200);
	public static final Dimension MEDIUM_SMALL_PREFERRED_DIALOG_SIZE = new Dimension(650, 200);
	public static final Dimension MEDIUM_PREFERRED_DIALOG_SIZE = new Dimension(650, 450);
	private static final Dimension LARGE_PREFERRED_DIALOG_ZIZE = new Dimension(960, 640);
	
	public static String EMPTY_TITLE = "";
}
