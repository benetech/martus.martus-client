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

import java.io.File;

import javafx.scene.Parent;
import javafx.scene.Scene;

public class FxScene extends Scene
{
	public FxScene(Parent root, File fxmlDirToUse) throws Exception
	{
		super(root);
		
		getStylesheets().add(getBestCssLocation(fxmlDirToUse));
	}

	private String getBestCssLocation(File fxmlDirToUse) throws Exception
	{
		File backgroundCssFile = new File(fxmlDirToUse, getSetupWizardCssLocation());
		if (backgroundCssFile.exists())
			return backgroundCssFile.toURI().toURL().toExternalForm();

		return FxScene.class.getResource(getSetupWizardCssLocation()).toExternalForm();
	}

	//TODO this class is generic and this css is specific to setup wizard.  
	//The css needs to be passed in or extract a setup wizard leaf class
	private String getSetupWizardCssLocation()
	{
		return "setupwizard/background.css";
	}
}
