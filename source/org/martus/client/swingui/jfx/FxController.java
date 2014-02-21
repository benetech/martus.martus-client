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
import java.net.MalformedURLException;
import java.net.URL;

import javafx.scene.Parent;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.MartusLogger;

abstract public class FxController implements FxControllerInterface
{
	public FxController(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
	}
	
	public Parent createContents() throws Exception
	{
		return (Parent)createLoader().load();
	}
	
	private FxmlLoaderWithController createLoader() throws Exception
	{
		URL resourceUrl = getBestFxmlLocation();
		
		return new FxmlLoaderWithController(this, resourceUrl);
	}

	private URL getBestFxmlLocation() throws Exception
	{
		File fxmlDir = getApp().getFxmlDirectory();
		return getBestFile(fxmlDir, getFxmlLocation());
	}

	public static URL getBestFile(File fxmlDir, String fileLocation) throws MalformedURLException
	{
		File fxmlFile = new File(fxmlDir, fileLocation);
		if (fxmlFile.exists())
		{
			MartusLogger.log("Loading FX file from disk:" + fileLocation);
			return fxmlFile.toURI().toURL();
		}

		return FxScene.class.getResource(fileLocation);
	}		
	
	public void setStage(FxStage stageToUse)
	{
		stage = stageToUse;
	}
	
	public FxStage getStage()
	{
		return stage;
	}
	
	public UiMainWindow getMainWindow()
	{
		return mainWindow;
	}
	
	public MartusLocalization getLocalization()
	{
		return getMainWindow().getLocalization();
	}
	
	public MartusApp getApp()
	{
		return getMainWindow().getApp();
	}

	private FxStage stage;
	private UiMainWindow mainWindow;
}
