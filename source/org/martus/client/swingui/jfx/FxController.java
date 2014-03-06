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

import javafx.concurrent.Task;
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

abstract public class FxController implements Initializable
{
	public FxController(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
	}
	
	abstract public String getFxmlLocation();
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
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
			showControllerInsideModalDialog(popupController);
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}
	
	public boolean showConfirmationDlg(String title, String message)
	{
		try
		{
			PopupConfirmationController popupController = new PopupConfirmationController(getMainWindow(), title, message);
			showControllerInsideModalDialog(popupController);
			return popupController.wasYesPressed();
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
		return false;
	}
	
	public void showBusyDlg(String title, Task task)
	{
		try
		{
			FxPopupController popupController = new FxBusyController(getMainWindow(), title, task);
			showControllerInsideModalDialog(popupController);
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			System.exit(1);
		}
		
	}

	public static class PopupNotifyController extends FxPopupController implements Initializable
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

		@Override
		public String getDialogTitle()
		{
			return getLocalization().getWindowTitle("notify"+ baseTag); 
		}

		@FXML
		public void okPressed()
		{
			getStage().close();
		}

		@FXML
		private Label fxLabel;

		@FXML
		private Button fxOkButton;

		private String baseTag;
	}

	public static class PopupConfirmationController extends FxPopupController implements Initializable
	{
		public PopupConfirmationController(UiMainWindow mainWindowToUse, String title, String message)
		{
			super(mainWindowToUse);
			this.title = title;
			this.message = message;
		}
		
		@Override
		public void initialize(URL arg0, ResourceBundle arg1)
		{
			MartusLocalization localization = getLocalization();
			fxYesButton.setText(localization.getButtonLabel("yes"));
			fxNoButton.setText(localization.getButtonLabel("no"));
			fxLabel.setText(message);
		}
		
		@Override
		public String getFxmlLocation()
		{
			return "setupwizard/ConfirmationPopup.fxml";
		}

		@Override
		public String getDialogTitle()
		{
			return title; 
		}

		@FXML
		public void yesPressed()
		{
			yesWasPressed = true;
			getStage().close();
		}

		@FXML
		public void noPressed()
		{
			getStage().close();
		}

		public boolean wasYesPressed()
		{
			return yesWasPressed;
		}

		@FXML
		private Label fxLabel;

		@FXML
		private Button fxYesButton;
		
		@FXML
		private Button fxNoButton;
		
		private String title;
		private String message;
		private boolean yesWasPressed;
	}
	
	public void showControllerInsideModalDialog(FxPopupController controller) throws Exception
	{
		Stage popupStage = new Stage();
		controller.setStage(popupStage);
		popupStage.setTitle(controller.getDialogTitle());
		popupStage.initModality(Modality.WINDOW_MODAL);

		FXMLLoader fl = new FXMLLoader();
		fl.setController(controller);
		fl.setLocation(FxInSwingDialogStage.class.getResource(controller.getFxmlLocation()));
		fl.load();
		Parent root = fl.getRoot();

		Scene scene = new Scene(root);
		popupStage.setScene(scene);
	    popupStage.showAndWait();
	}

	private UiMainWindow mainWindow;
}
