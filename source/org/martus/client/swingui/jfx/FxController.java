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
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
	
	public void setStage(Stage stageToUse)
	{
		stage = stageToUse;
	}
	
	public Stage getStage()
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
	
	public void showNotifyDlg(String baseTag)
	{
		try
		{
			PopupNotifyController popupController = new PopupNotifyController(getMainWindow(), baseTag);
			showControllerInsideModalDialog(popupController, baseTag);
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}
	
	public static class PopupNotifyController extends FxController implements Initializable
	{
		public PopupNotifyController(UiMainWindow mainWindowToUse, String notificationTag)
		{
			super(mainWindowToUse);
			baseTag = notificationTag;
		}
		
		@Override
		public void initialize(URL arg0, ResourceBundle arg1)
		{
			MartusLocalization localization = getLocalization();
			fxOkButton.setText(localization.getButtonLabel("ok"));
			fxLabel.setText(localization.getFieldLabel("notify"+baseTag+"cause"));
		}
		
		@Override
		public String getFxmlLocation()
		{
			return "setupwizard/NotifyPopup.fxml";
		}

		@FXML
		public void okPressed()
		{
			getStage().close();
		}

		public void setFxStage(FxStage stageToUse)
		{
			fxStage = stageToUse;
		}

		public FxStage getFxStage()
		{
			return fxStage;
		}

		@FXML
		private Label fxLabel;
		private FxStage fxStage;
		@FXML
		private Button fxOkButton;
		private String baseTag;
	}

	
	public void showControllerInsideModalDialog(FxController controller, String dialogTitleTag) throws Exception
	{
		Stage popupStage = new Stage();
		controller.setStage(popupStage);
		popupStage.setTitle(getLocalization().getWindowTitle("notify"+dialogTitleTag));
		popupStage.initModality(Modality.WINDOW_MODAL);

		FXMLLoader fl = new FXMLLoader();
		fl.setController(controller);
		fl.setLocation(FxStage.class.getResource(controller.getFxmlLocation()));
		fl.load();
		Parent root = fl.getRoot();

		Scene scene = new Scene(root);
		popupStage.setScene(scene);
	    popupStage.showAndWait();
	}

	private Stage stage;
	private UiMainWindow mainWindow;
}
