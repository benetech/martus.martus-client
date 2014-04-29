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
import java.util.Map;
import java.util.ResourceBundle;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.setupwizard.tasks.AbstractAppTask;
import org.martus.client.swingui.jfx.setupwizard.tasks.TaskWithTimeout;
import org.martus.common.MartusLogger;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;

abstract public class FxController implements Initializable
{
	public static class UserCancelledException extends Exception
	{
	}

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
		String fxmlLocation = getFxmlLocation();
		return getBestFxmlLocation(fxmlLocation);
	}

	public URL getBestFxmlLocation(String fxmlLocation) throws Exception
	{
		File fxmlDir = getApp().getFxmlDirectory();
		return getBestFile(fxmlDir, fxmlLocation);
	}

	public static URL getBestFile(File fxmlDir, String fileLocation) throws Exception
	{
		File fxmlFile = new File(fxmlDir, fileLocation);
		if (fxmlFile.exists())
		{
			MartusLogger.log("Loading FX file from disk:" + fileLocation);
			return fxmlFile.toURI().toURL();
		}

		URL resource = FxScene.class.getResource(fileLocation);
		if(resource == null)
			throw new ResourceNotFoundException("Couldn't find " + fileLocation);
		
		return resource;
	}		
	
	public static class ResourceNotFoundException extends Exception
	{
		public ResourceNotFoundException(String message)
		{
			super(message);
		}
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
	
	public void showNotifyDialog(FxInSwingDialogStage wizardPanel, String baseTag)
	{
		String noExtraMessage = "";
		Map tokenReplacement = null;
		showNotifyDialog(wizardPanel, baseTag, noExtraMessage, tokenReplacement);
	}
	
	public void showNotifyDialog(FxInSwingDialogStage wizardPanel, String baseTag, Map tokenReplacement)
	{
		String noExtraMessage = "";
		showNotifyDialog(wizardPanel, baseTag, noExtraMessage, tokenReplacement);
	}

	public void showNotifyDialog(FxInSwingDialogStage wizardPanel, String baseTag, String extraMessage)
	{
		Map noTokenReplacement = null;
		showNotifyDialog(wizardPanel, baseTag, extraMessage, noTokenReplacement);
	}

	public void showNotifyDialog(FxInSwingDialogStage wizardPanel, String baseTag, String extraMessage, Map tokenReplacement)
	{
		++notifyDialogDepth;
		try
		{
			PopupNotifyController popupController = new PopupNotifyController(getMainWindow(), baseTag);
			popupController.setExtraMessage(extraMessage);
			popupController.setTokenReplacement(tokenReplacement);
			showControllerInsideModalDialog(wizardPanel, popupController);
			--notifyDialogDepth;
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			if(notifyDialogDepth > 3)
			{
				getMainWindow().exitWithoutSavingState();
			}
			showNotifyDialog(wizardPanel, "UnexpectedError");
		}
	}
	
	public boolean showConfirmationDialog(FxInSwingDialogStage wizardPanel, String title, String message)
	{
		try
		{
			PopupConfirmationController popupController = new PopupConfirmationController(getMainWindow(), title, message);
			showControllerInsideModalDialog(wizardPanel, popupController);
			return popupController.wasYesPressed();
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			showNotifyDialog(wizardPanel, "UnexpectedError");
		}
		return false;
	}
	
	public void showBusyDialog(String message, Task task, FxInSwingDialogStage wizardPanel) throws Exception
	{
		FxPopupController popupController = new FxBusyController(getMainWindow(), message, task);
		showControllerInsideModalDialog(wizardPanel, popupController);
	}

	public void showTimeoutDialog(FxInSwingDialogStage wizardPanel, String message, TaskWithTimeout task) throws Exception
	{
		FxTimeoutController popupController = new FxTimeoutController(getMainWindow(), message, task, task.getMaxSeconds());
		showControllerInsideModalDialog(wizardPanel, popupController);
		if(popupController.didUserCancel())
			throw new UserCancelledException();
	}

	public void showProgressDialog(FxWizardStage wizardPanel, String message, AbstractAppTask task) throws Exception
	{
		FxProgressController popupController = new FxProgressController(getMainWindow(), message, task);
		showControllerInsideModalDialog(wizardPanel, popupController);
		if(popupController.didUserCancel())
			throw new UserCancelledException();
	}

	public static class PopupNotifyController extends FxPopupController implements Initializable
	{
		public PopupNotifyController(UiMainWindow mainWindowToUse, String notificationTag)
		{
			super(mainWindowToUse);
			baseTag = notificationTag;
			extraMessage = "";
			tokenReplacement = null;
		}
		
		@Override
		public void initialize(URL arg0, ResourceBundle arg1)
		{
			MartusLocalization localization = getLocalization();
			fxOkButton.setText(localization.getButtonLabel("ok"));
			String fieldLabelRaw = localization.getFieldLabel("notify"+baseTag+"cause");
			String fieldLabel = fieldLabelRaw;
			if(tokenReplacement != null)
			{
				try
				{
					fieldLabel = TokenReplacement.replaceTokens(fieldLabelRaw, tokenReplacement);
				} 
				catch (TokenInvalidException e)
				{
					MartusLogger.logException(e);
					throw new RuntimeException(e);
				}
			}
			
			String fullMessage = fieldLabel;
			if(extraMessage.length()>0)
			{
				fullMessage += " ";
				fullMessage += extraMessage;
			}
			fxLabel.setText(fullMessage);
			
		}
		
		public void setExtraMessage(String extraMessageToUse)
		{
			extraMessage = extraMessageToUse;
		}
		
		public void setTokenReplacement(Map tokenReplacementMapToUse)
		{
			tokenReplacement = tokenReplacementMapToUse;
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
		private String extraMessage;
		private Map tokenReplacement;
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
			textArea.setText(message);
			textArea.setEditable(false);
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
		private TextArea textArea;

		@FXML
		private Button fxYesButton;
		
		@FXML
		private Button fxNoButton;
		
		private String title;
		private String message;
		private boolean yesWasPressed;
	}
	
	public void showControllerInsideModalDialog(FxInSwingDialogStage wizardPanel, FxPopupController controller) throws Exception
	{
		Stage popupStage = new Stage();
		controller.setStage(popupStage);
		popupStage.setTitle(controller.getDialogTitle());
		popupStage.initModality(Modality.APPLICATION_MODAL);
		FXMLLoader fl = new FXMLLoader();
		fl.setResources(new MartusResourceBundle(getLocalization()));
		fl.setController(controller);
		fl.setLocation(FxInSwingDialogStage.class.getResource(controller.getFxmlLocation()));
		fl.load();
		Parent root = fl.getRoot();

		Scene scene = new Scene(root);
		File fxmlDir = getApp().getFxmlDirectory();
		URL css = FxController.getBestCss(fxmlDir, getLocalization().getCurrentLanguageCode(), "popup.css");
		scene.getStylesheets().add(css.toExternalForm());
		popupStage.setScene(scene);
	    showModalPopupStage(popupStage);
	    if(controller.getThrownException() != null)
	    	throw (Exception)controller.getThrownException();
	}

	protected void showModalPopupStage(Stage popupStage)
	{
		popupStage.showAndWait();
	}

	public static URL getBestCss(File directory, String languageCode,
			String cssLocation2) throws Exception
	{
		try
		{
			return getBestFile(directory, "css/" + languageCode + "/" + cssLocation2);
		}
		catch(ResourceNotFoundException expectedForMostLanguages)
		{
			return getBestFile(directory, "css/" + cssLocation2);
		}
	}

	private UiMainWindow mainWindow;
	private static int notifyDialogDepth;
}
