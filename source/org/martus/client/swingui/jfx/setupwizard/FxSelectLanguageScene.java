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
package org.martus.client.swingui.jfx.setupwizard;

import javafx.fxml.FXMLLoader;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.FxScene;
import org.martus.client.swingui.jfx.FxmlScene;
import org.martus.client.swingui.jfx.FxmlSceneFactory;
import org.martus.clientside.UiLocalization;
import org.martus.common.fieldspec.ChoiceItem;

public class FxSelectLanguageScene extends FxmlScene
{
	public static class Factory extends FxmlSceneFactory
	{
		public Factory(UiMainWindow mainWindowToUse) throws Exception
		{
			super(new FxSelectLanguageController(mainWindowToUse), "MartusCreateAccount.fxml");

			localization = mainWindowToUse.getLocalization();
		}
		
		@Override
		public FxScene createScene() throws Exception
		{
			FxScene scene = new FxSelectLanguageScene(createLoader(), localization);
			return scene;
		}

		private UiLocalization localization;
	}
	
	public FxSelectLanguageScene(FXMLLoader loader, UiLocalization localizationToUse) throws Exception
	{
		super(loader);

		localization = localizationToUse;

        String css = this.getClass().getResource("background.css").toExternalForm();
		getStylesheets().add(css);			

	}

	String languageCodeChosen;
	UiLocalization localization;
	ChoiceItem[] allUILanguagesSupported;
}
