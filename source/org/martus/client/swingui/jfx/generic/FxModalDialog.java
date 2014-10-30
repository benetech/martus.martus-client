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

import java.awt.Dimension;

import javafx.application.Platform;

import javax.swing.JDialog;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.WindowObscurer;
import org.martus.swing.Utilities;

public class FxModalDialog extends JDialog
{
	public static void createAndShow(UiMainWindow owner, FxInSwingDialogStage stage) throws Exception
	{
		createAndShow(owner, stage, "");
	}
	
	public static void createAndShow(UiMainWindow owner, FxInSwingDialogStage stage, String titleTag) throws Exception
	{
		FxModalDialog dialog = new FxModalDialog(owner);
		if (titleTag.length() > 0)
			dialog.setTitle(owner.getLocalization().getWindowTitle(titleTag));
		
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.getContentPane().setPreferredSize(new Dimension(960, 640));
		dialog.pack();
		dialog.getContentPane().add(stage);
		stage.setDialog(dialog);
		Platform.runLater(new FxRunner(stage));

		Utilities.centerDlg(dialog);
		owner.setCurrentActiveDialog(dialog);
		dialog.setVisible(true);
		owner.setCurrentActiveDialog(null);
	}
	
	private FxModalDialog(UiMainWindow owner)
	{
		super(owner);

		setModal(true);
		
		setGlassPane(new WindowObscurer());
	}
}
