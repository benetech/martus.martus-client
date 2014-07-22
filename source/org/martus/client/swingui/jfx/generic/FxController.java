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

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.data.MartusResourceBundle;
import org.martus.client.swingui.jfx.setupwizard.tasks.AbstractAppTask;
import org.martus.client.swingui.jfx.setupwizard.tasks.TaskWithTimeout;
import org.martus.common.MartusLogger;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;

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
		String fxmlLocation = getFxmlLocation();
		return getBestFxmlLocation(fxmlLocation);
	}

	public URL getBestFxmlLocation(String fxmlLocation) throws Exception
	{
		File fxmlDir = getApp().getFxmlDirectory();
		URL fxmlURL = getBestFile(fxmlDir, fxmlLocation);
		if(fxmlURL == null)		
			throw new ResourceNotFoundException("Couldn't find " + fxmlLocation);
		return fxmlURL;
	}

	public static URL getBestFile(File fxmlDir, String fileLocation) throws Exception
	{
		File fxmlFile = new File(fxmlDir, fileLocation);
		if (fxmlFile.exists())
		{
			MartusLogger.log("Loading FX file from disk:" + fileLocation);
			return fxmlFile.toURI().toURL();
		}

		return FxController.class.getResource("/org/martus/client/swingui/jfx/" + fileLocation);
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
	
	public void showNotifyDialog(String baseTag)
	{
		String noExtraMessage = "";
		Map tokenReplacement = null;
		showNotifyDialog(baseTag, noExtraMessage, tokenReplacement);
	}
	
	public void showNotifyDialog(String baseTag, Map tokenReplacement)
	{
		String noExtraMessage = "";
		showNotifyDialog(baseTag, noExtraMessage, tokenReplacement);
	}

	public void showNotifyDialog(String baseTag, String extraMessage)
	{
		Map noTokenReplacement = null;
		showNotifyDialog(baseTag, extraMessage, noTokenReplacement);
	}

	public void showNotifyDialog(String baseTag, String extraMessage, Map tokenReplacement)
	{
		++notifyDialogDepth;
		try
		{
			PopupNotifyController popupController = new PopupNotifyController(getMainWindow(), baseTag);
			popupController.setExtraMessage(extraMessage);
			popupController.setTokenReplacement(tokenReplacement);
			showControllerInsideModalDialog(popupController);
			--notifyDialogDepth;
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			if(notifyDialogDepth > 3)
			{
				getMainWindow().exitWithoutSavingState();
			}
			showNotifyDialog("UnexpectedError");
		}
	}
	
	public boolean showConfirmationDialog(String title, String message)
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
			showNotifyDialog("UnexpectedError");
		}
		return false;
	}
	
	public void showBusyDialog(String message, Task task, FxInSwingStage wizardPanel) throws Exception
	{
		FxPopupController popupController = new FxBusyController(getMainWindow(), message, task);
		showControllerInsideModalDialog(popupController);
	}

	public void showTimeoutDialog(String message, TaskWithTimeout task) throws Exception
	{
		FxTimeoutController popupController = new FxTimeoutController(getMainWindow(), message, task, task.getMaxSeconds());
		showControllerInsideModalDialog(popupController);
		if(popupController.didUserCancel())
			throw new UserCancelledException();
	}

	public void showProgressDialog(String message, AbstractAppTask task) throws Exception
	{
		FxProgressController popupController = new FxProgressController(getMainWindow(), message, task);
		showControllerInsideModalDialog(popupController);
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
		public void initialize()
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

	public void showControllerInsideModalDialog(FxPopupController controller) throws Exception
	{
		FxStage popupStage = new FxStage(mainWindow, controller);
		FXMLLoader fl = new FXMLLoader();
		fl.setResources(new MartusResourceBundle(getLocalization()));
		fl.setController(controller);
		URL fxmlUrl = getBestFxmlLocation(controller.getFxmlLocation());
		fl.setLocation(fxmlUrl);
		fl.load();
		Parent root = fl.getRoot();

		Scene scene = new Scene(root);
		scene.setNodeOrientation(FxScene.getNodeOrientationBasedOnLanguage());
		File fxmlDir = getApp().getFxmlDirectory();
		FxController.applyStyleSheets(scene.getStylesheets(), fxmlDir, getLocalization().getCurrentLanguageCode(), POPUP_CSS);
		popupStage.setScene(scene);
	    showModalPopupStage(popupStage);
	    if(controller.getThrownException() != null)
	    	throw (Exception)controller.getThrownException();
	}

	protected void showModalPopupStage(Stage popupStage)
	{
		popupStage.showAndWait();
	}
	
	static public void applyStyleSheets(ObservableList<String> stylesheets, File directory, String languageCode, String cssLocation) throws Exception
	{
		applyMasterMartusStyleSheets(stylesheets, directory, languageCode);
		if(cssLocation == null)
			return;
		applyPageSpecificStyleSheets(stylesheets, directory, languageCode, cssLocation);
	}

	private static void applyMasterMartusStyleSheets(ObservableList<String> stylesheets, File directory,
			String languageCode) throws Exception
	{
		applyPageSpecificStyleSheets(stylesheets, directory, languageCode, MARTUS_CSS);
	}

	private static void applyPageSpecificStyleSheets( ObservableList<String> stylesheets, File directory,
			String languageCode, String cssLocation) throws Exception
	{
		URL englishCssUrl = getBestCss(directory, MartusLocalization.ENGLISH, cssLocation);
		if(englishCssUrl == null)
			throw new ResourceNotFoundException(
					"Couldn't find :" + directory + "/" + cssLocation);
		
		stylesheets.add(englishCssUrl.toExternalForm());
		
		if(!languageCode.equals(MartusLocalization.ENGLISH))
		{
			URL languageCssUrl = getBestCss(directory, languageCode, cssLocation);
			if(languageCssUrl != null)
				stylesheets.add(languageCssUrl.toExternalForm());
		}
	}
	
	public static URL getBestCss(File directory, String languageCode,
			String cssLocation) throws Exception
	{
		if(languageCode.equals(MartusLocalization.ENGLISH))
			return getBestFile(directory, "css/" + cssLocation);
		return getBestFile(directory, "css/" + languageCode + "/" + cssLocation);
	}

	public static class UserCancelledException extends Exception
	{
	}

	private static final String POPUP_CSS = "Popup.css";
	private static final String MARTUS_CSS = "Martus.css";

	private UiMainWindow mainWindow;
	private static int notifyDialogDepth;
}
