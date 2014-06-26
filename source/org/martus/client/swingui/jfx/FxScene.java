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
import java.net.URL;

import javafx.collections.ObservableList;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.layout.Region;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.jfx.FxController.ResourceNotFoundException;
import org.martus.util.language.LanguageOptions;

public class FxScene extends Scene
{
	private static final String MARTUS_CSS = "Martus.css";
	public FxScene(File fxmlDirToUse, String cssLocationToUse) throws Exception
	{
		super(new Region());
		NodeOrientation orientation = getNodeOrientationBasedOnLanguage();
		setNodeOrientation(orientation);
		
		fxmlDirectory = fxmlDirToUse;
		cssLocation = cssLocationToUse;
	}

	public static NodeOrientation getNodeOrientationBasedOnLanguage()
	{
		NodeOrientation orientation = NodeOrientation.LEFT_TO_RIGHT;
		if(LanguageOptions.isRightToLeftLanguage())
			orientation = NodeOrientation.RIGHT_TO_LEFT;
		return orientation;
	}

	public void applyStyleSheet(String languageCode) throws Exception
	{
		applyLanguageStyleSheet(MartusLocalization.ENGLISH);
		if(!languageCode.equals(MartusLocalization.ENGLISH))
			applyLanguageStyleSheet(languageCode);
	}
	
	private void applyLanguageStyleSheet(String languageCode) throws Exception
	{
		ObservableList<String> stylesheets = getStylesheets();
		try
		{
			stylesheets.add(getMartusCss(languageCode).toExternalForm());
		}
		catch (ResourceNotFoundException ignoreNonExistentCssForLanguageSpecific)
		{
		}
		
		try
		{
			stylesheets.add(getBestCss(languageCode).toExternalForm());
		}
		catch (ResourceNotFoundException ignoreNonExistentCssForLanguageSpecific)
		{
		}
	}

	public URL getBestCss(String languageCode) throws Exception
	{
		return FxController.getBestCss(fxmlDirectory, languageCode, getCssLocation());
	}

	public URL getMartusCss(String languageCode) throws Exception
	{
		return FxController.getBestCss(fxmlDirectory, languageCode, MARTUS_CSS);
	}

	public String getCssLocation()
	{
		return cssLocation;
	}
	
	private File fxmlDirectory;
	private String cssLocation;
}
