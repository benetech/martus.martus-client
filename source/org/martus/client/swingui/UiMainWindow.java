/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2014, Beneficent
Technology, Inc. (The Benetech Initiative).

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

package org.martus.client.swingui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileLock;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.TimerTask;
import java.util.Vector;

import javafx.application.Platform;

import javax.crypto.Cipher;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.bouncycastle.crypto.engines.RSAEngine;
import org.json.JSONObject;
import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.BulletinGetterThread;
import org.martus.client.core.ConfigInfo;
import org.martus.client.core.FontSetter;
import org.martus.client.core.MartusApp;
import org.martus.client.core.MartusApp.LoadConfigInfoException;
import org.martus.client.core.MartusApp.MartusAppInitializationException;
import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.client.core.MartusJarVerification;
import org.martus.client.core.SortableBulletinList;
import org.martus.client.core.TransferableBulletinList;
import org.martus.client.core.templates.FormTemplateManager.UnableToLoadCurrentTemplateException;
import org.martus.client.network.BackgroundUploader;
import org.martus.client.network.RetrieveCommand;
import org.martus.client.search.SearchTreeNode;
import org.martus.client.swingui.bulletincomponent.UiBulletinPreviewPane;
import org.martus.client.swingui.bulletintable.UiBulletinTablePane;
import org.martus.client.swingui.dialogs.UiAboutDlg;
import org.martus.client.swingui.dialogs.UiBulletinModifyDlg;
import org.martus.client.swingui.dialogs.UiCreateNewAccountProcess;
import org.martus.client.swingui.dialogs.UiFancySearchDlg;
import org.martus.client.swingui.dialogs.UiInitialSigninDlg;
import org.martus.client.swingui.dialogs.UiModelessBusyDlg;
import org.martus.client.swingui.dialogs.UiOnlineHelpDlg;
import org.martus.client.swingui.dialogs.UiProgressWithCancelDlg;
import org.martus.client.swingui.dialogs.UiServerSummariesDlg;
import org.martus.client.swingui.dialogs.UiServerSummariesRetrieveDlg;
import org.martus.client.swingui.dialogs.UiShowScrollableTextDlg;
import org.martus.client.swingui.dialogs.UiSigninDlg;
import org.martus.client.swingui.dialogs.UiSplashDlg;
import org.martus.client.swingui.dialogs.UiStringInputDlg;
import org.martus.client.swingui.dialogs.UiTemplateDlg;
import org.martus.client.swingui.dialogs.UiWarningMessageDlg;
import org.martus.client.swingui.filefilters.KeyPairFormatFilter;
import org.martus.client.swingui.foldertree.UiFolderTreePane;
import org.martus.client.swingui.jfx.generic.FxDialogHelper;
import org.martus.client.swingui.jfx.generic.FxModalDialog;
import org.martus.client.swingui.jfx.generic.FxRunner;
import org.martus.client.swingui.jfx.generic.FxStatusBar;
import org.martus.client.swingui.jfx.landing.FxMainStage;
import org.martus.client.swingui.jfx.setupwizard.SetupWizardStage;
import org.martus.client.swingui.jfx.welcome.WelcomeStage;
import org.martus.client.swingui.spellcheck.SpellCheckerManager;
import org.martus.client.swingui.tablemodels.RetrieveTableModel;
import org.martus.clientside.ClientSideNetworkGateway;
import org.martus.clientside.ClientSideNetworkHandlerUsingXmlRpc;
import org.martus.clientside.CurrentUiState;
import org.martus.clientside.FileDialogHelpers;
import org.martus.clientside.FormatFilter;
import org.martus.clientside.MtfAwareLocalization;
import org.martus.clientside.UiUtilities;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.Exceptions.NetworkOfflineException;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MartusAccountAccessToken;
import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.MartusUtilities.ServerErrorException;
import org.martus.common.MiniLocalization;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.EncryptionException;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.crypto.MartusCrypto.NoKeyPairException;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.database.FileDatabase.MissingAccountMapException;
import org.martus.common.database.FileDatabase.MissingAccountMapSignatureException;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.OrchidTransportWrapper;
import org.martus.common.packet.Packet;
import org.martus.common.packet.Packet.WrongAccountException;
import org.martus.common.packet.UniversalId;
import org.martus.common.packet.XmlPacketLoader;
import org.martus.swing.FontHandler;
import org.martus.swing.UiNotifyDlg;
import org.martus.swing.UiOptionPane;
import org.martus.swing.UiPasswordField;
import org.martus.swing.UiPopupMenu;
import org.martus.swing.Utilities;
import org.martus.swing.Utilities.Delay;
import org.martus.util.FileVerifier;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;
import org.martus.util.UnicodeReader;
import org.martus.util.language.LanguageOptions;
import org.martus.util.xml.XmlUtilities;

public class UiMainWindow implements ClipboardOwner, UiMainWindowInterface
{
	public UiMainWindow() throws Exception
	{

		try
		{
			warnIfThisJarNotSigned();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error attempting to verify jar");
			throw new RuntimeException(e);
		}

		try
		{
			warnIfCryptoJarsNotLoaded();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Unknown error attempting to locate crypto jars");
			throw new RuntimeException(e);
		}
		
		cursorStack = new Stack();
		
		UiModelessBusyDlg splashScreen = new UiModelessBusyDlg(new ImageIcon(UiAboutDlg.class.getResource("Martus-logo-black-text-160x72.png")));
		try
		{
			session = new UiSession();
			getSession().initalizeUiState();

			// Pop up a nag screen if this is an unofficial private release
			// NOTE NAG screen now could be localized
//			new UiNotifyDlg(this, "Martus - Test Version", 
//					new String[] {"THIS IS A PRE-RELEASE TEST VERSION OF MARTUS.",
//					"Please contact martus@bentech.org with any feedback or questions."}, 
//					new String[] {"OK"});
			
			// Uncomment the call to restrictToOnlyTestServers for test builds which might
			// generate bad data that we don't want cluttering up production servers
//			restrictToOnlyTestServers();
			
		}
		catch(MartusApp.MartusAppInitializationException e)
		{
			initializationErrorExitMartusDlg(e.getMessage());
		}
		finally
		{
			splashScreen.endDialog();
		}
	}
	
	public JFrame getSwingFrame()
	{
		return swingFrame;
	}
	
	protected void restrictToOnlyTestServers()
	{
		// NOTE: For now, only allow connecting to servers which we can completely 
		// delete all user data from if necessary. So NOT .29 or .114.
		// Visibility 'protected' only so we don't get a warning when we don't call this method for releases.
		ClientSideNetworkHandlerUsingXmlRpc.addAllowedServer("127.0.0.1");
		ClientSideNetworkHandlerUsingXmlRpc.addAllowedServer("127.0.0.2");
		ClientSideNetworkHandlerUsingXmlRpc.addAllowedServer("localhost");
		ClientSideNetworkHandlerUsingXmlRpc.addAllowedServer("sl1-dev");
		ClientSideNetworkHandlerUsingXmlRpc.addAllowedServer("54.213.152.140"); // sl1-dev
		ClientSideNetworkHandlerUsingXmlRpc.addAllowedServer("aws-dev");
		ClientSideNetworkHandlerUsingXmlRpc.addAllowedServer("54.245.101.104"); // aws-dev
	}
	
	@Override
	public boolean isServerAccessible(String address)
	{
		return ClientSideNetworkHandlerUsingXmlRpc.isServerAllowed(address);
	}

	public boolean run()
	{
		String currentLanguageCode = getLocalization().getCurrentLanguageCode();
		FontSetter.setDefaultFont(currentLanguageCode.equals(MtfAwareLocalization.BURMESE));
		displayDefaultUnofficialTranslationMessageIfNecessary(currentActiveFrame, getLocalization(), currentLanguageCode);
		displayIncompatibleMtfVersionWarningMessageIfNecessary(currentActiveFrame, getLocalization(), getLocalization().getCurrentLanguageCode());
		
		preventTwoInstances();
		if(!UiSession.isJavaFx())
			notifyClientCompliance();

		mainWindowInitalizing = true;

		inactivityDetector = new UiInactivityDetector();
		timeoutTimerTask = new TimeoutTimerTask();

		if (getApp().hasNoAccounts())
		{
			startAccountSetupWizard();
		}

		if(!getApp().isSignedIn())
		{
			if(!sessionSignIn())
				return false;

			startInactivityTimeoutDetection();
			loadConfigInfo();
			doPostSigninAppInitialization();
		}
		
		initalizeUiState(getLocalization().getCurrentLanguageCode());

		try
		{
			String accountId = getApp().getSecurity().getPublicKeyString();
			MartusLogger.log("Old public code: " + MartusSecurity.computeFormattedPublicCode(accountId) + "\n");
			MartusLogger.log("New public code: " + MartusCrypto.computeFormattedPublicCode40(accountId));
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			// NOTE: This was just informational output, so keep going
		}
		
		if(!createdNewAccount && !justRecovered)
			askAndBackupKeypairIfRequired();
		
		UiModelessBusyDlg waitingForBulletinsToLoad = new UiModelessBusyDlg(getLocalization().getFieldLabel("waitingForBulletinsToLoad"));
		try
		{
			if(!loadFoldersAndBulletins())
				return false;
	
			initializeViews();
			restoreState();
		}
		catch(Exception e)
		{
			unexpectedErrorDlg(e);
			System.exit(1);
		}
		finally
		{
			waitingForBulletinsToLoad.endDialog();
		}

		MartusLogger.log("reloadPendingRetrieveQueue");
		reloadPendingRetrieveQueue();
		
		try
		{
			SpellCheckerManager.initializeSpellChecker(this);
		} 
		catch (MalformedURLException e)
		{
			MartusLogger.logException(e);
			notifyDlg("ErrorInitializingSpellChecker");
			System.exit(1);
		}
		
		MartusLogger.log("Ready to show main window");
		if(timeoutTimerTask.waitingForSignin)
		{
			getSwingFrame().setLocation(100000, 0);
			getSwingFrame().setSize(0,0);
			getSwingFrame().setEnabled(false);
		}
		else
		{
			MartusLogger.log("Showing main window");
			getSwingFrame().setVisible(true);
			getSwingFrame().toFront();
			mainWindowInitalizing = false;
		}
		

		try
		{
			createBackgroundUploadTasks();
		} 
		catch (Exception e)
		{
			unexpectedErrorDlg(e);
			System.exit(1);
		}

		MartusLogger.log("Initialization complete");
		return true;
    }

	public void displayIncorrectVersionJava(String highVersionJava, String expectedVersionJava)
	{
		String title = getLocalization().getWindowTitle("IncompatibleJavaVersion");
		String warningMessage = getLocalization().getFieldLabel("IncompatibleJavaVersion");
		String buttonMessage = getLocalization().getButtonLabel(EnglishCommonStrings.OK);
		Toolkit.getDefaultToolkit().beep();
		HashMap map = new HashMap();
		map.put("#HighVersion#", highVersionJava);
		map.put("#ExpectedVersion#", expectedVersionJava);
		new UiNotifyDlg(title, new String[]{warningMessage}, new String[]{buttonMessage}, map);
	}

	private void warnIfCryptoJarsNotLoaded() throws Exception
	{
		URL jceJarURL = MartusJarVerification.getJarURL(Cipher.class);
		String urlString = jceJarURL.toString();
		int foundAt = urlString.indexOf("bc-jce");
		boolean foundBcJce = (foundAt >= 0);
		MartusLogger.log("warnIfCryptoJarsNotLoaded Cipher: " + urlString);

		if(foundBcJce)
		{
			String hintsToSolve = "Make sure Xbootclasspath does not contain bc-jce.jar";
			JOptionPane.showMessageDialog(null, "bc-jce.jar cannot be used\n\n" + hintsToSolve);
		}
		
		try
		{
			URL bcprovJarURL = MartusJarVerification.getJarURL(RSAEngine.class);
			String bcprovJarName = MartusJarVerification.BCPROV_JAR_FILE_NAME;
			if(bcprovJarURL.toString().indexOf(bcprovJarName) < 0)
			{
				String hintsToSolve = "Make sure " + bcprovJarName + " is the only bcprov file in Martus/lib/ext";
				JOptionPane.showMessageDialog(null, "Didn't load " + bcprovJarName + "\n\n" + hintsToSolve);
			}
		} 
		catch (MartusCrypto.InvalidJarException e)
		{
			String hintsToSolve = "Xbootclasspath might be incorrect; " + MartusJarVerification.BCPROV_JAR_FILE_NAME + " might be missing from Martus/lib/ext";
			JOptionPane.showMessageDialog(null, "Didn't load bc-jce.jar\n\n" + hintsToSolve);
		}

	}

	private void warnIfThisJarNotSigned() throws Exception
	{
		if(!MartusApp.isRunningFromJar())
		{
			System.out.println("Skipping jar verification because we are not running from a jar");
			return;
		}

		if(!MartusApp.isJarSigned())
		{
			JOptionPane.showMessageDialog(null, "This Martus Jar is not signed, so cannot be verified");
		}
	}

	static public void displayDefaultUnofficialTranslationMessageIfNecessary(JFrame owner, MtfAwareLocalization localization, String languageCodeToTest)
	{
		if(localization.isOfficialTranslation(languageCodeToTest))
			return;
		
		URL untranslatedURL = UiMainWindow.class.getResource("UnofficialTranslationMessage.txt");
		URL untranslatedRtoLURL = UiMainWindow.class.getResource("UnofficialTranslationMessageRtoL.txt");
		
		try
		{
			InputStream in = untranslatedURL.openStream();
			UnicodeReader reader = new UnicodeReader(in);
			String message = reader.readAll();
			reader.close();
			in = untranslatedRtoLURL.openStream();
			reader = new UnicodeReader(in);
			String messageRtoL = reader.readAll();
			reader.close();

			updateIcon(owner);
			String warningMessageLtoR = getWarningMessageAboutUnofficialTranslations(message);
			String warningMessageRtoL = getWarningMessageAboutUnofficialTranslations(messageRtoL);
			Toolkit.getDefaultToolkit().beep();
			new UiWarningMessageDlg(owner, "", localization.getButtonLabel(EnglishCommonStrings.OK), warningMessageLtoR, warningMessageRtoL);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException();
		}
		
	}
	
	public static void displayIncompatibleMtfVersionWarningMessageIfNecessary(JFrame owner, MtfAwareLocalization localization, String languageCodeToTest)
	{
		if(localization.doesTranslationVersionMatchProgramVersion(languageCodeToTest, UiConstants.versionLabel))
			return;
		updateIcon(owner);
		String langCode = localization.getCurrentLanguageCode();
		String title = localization.getLabel(langCode, "wintitle", "IncompatibleMtfVersion");
		String warningMessage = localization.getLabel(langCode, "field", "IncompatibleMtfVersion");
		String mtfVersion = localization.getLabel(langCode, "field", "IncompatibleMtfVersionTranslation");
		String programVersion = localization.getLabel(langCode, "field", "IncompatibleMtfVersionProgram");
		String buttonMessage = localization.getLabel(langCode, "button", "ok");
		Toolkit.getDefaultToolkit().beep();
		HashMap map = new HashMap();
		String mtfVersionNumber = localization.getTranslationVersionNumber(languageCodeToTest);		
		map.put("#MtfVersionNumber#", mtfVersionNumber);
		map.put("#ProgramVersionNumber#", localization.extractVersionNumber(UiConstants.versionLabel));
		map.put("#MtfLanguage#", localization.getLanguageName(languageCodeToTest));
		new UiNotifyDlg(owner, title, new String[]{warningMessage, "", mtfVersion, programVersion}, new String[]{buttonMessage}, map);
	}

	private static String getWarningMessageAboutUnofficialTranslations(String originalMessage)
	{
		String token = "#UseUnofficialTranslationFiles#";
		String replacementValue = "\"" + MartusApp.USE_UNOFFICIAL_TRANSLATIONS_NAME + "\"";
		originalMessage = replaceToken(originalMessage, token, replacementValue);
		return originalMessage;
	}

	private static String replaceToken(String originalMessage, String token, String replacementValue)
	{
		try
		{
			return TokenReplacement.replaceToken(originalMessage, token, replacementValue);
		}
		catch(TokenInvalidException e)
		{
			e.printStackTrace();
		}
		return originalMessage;
	}

	public void startInactivityTimeoutDetection()
	{
		if(timeoutChecker == null)
		{
			timeoutChecker = new java.util.Timer(true);
			timeoutChecker.schedule(timeoutTimerTask, 0, BACKGROUND_TIMEOUT_CHECK_EVERY_X_MILLIS);
			MartusLogger.log("Inactivity timer started");
		}
	}

	private void startAccountSetupWizard()
	{
		try
		{
			// NOTE: Prevent implicit JavaFX shutdown when the only JFX window is closed
		    Platform.setImplicitExit(false);

		    FxModalDialog.createAndShow(this, new WelcomeStage(this));
		    
		    FxModalDialog.createAndShow(this, new SetupWizardStage(this));
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}
	
	private void loadFieldSpecCache() throws Exception
	{
		MartusLogger.logBeginProcess("loadFieldSpecCache");
		if(!getStore().loadFieldSpecCache())
		{
			if(!createdNewAccount)
				notifyDlg(getSwingFrame(), "CreatingFieldSpecCache");

			getStore().createFieldSpecCacheFromDatabase();
		}
		MartusLogger.logEndProcess("loadFieldSpecCache");
	}
	
	private void createBackgroundUploadTasks() throws Exception
	{
		uploader = new java.util.Timer(true);
		backgroundUploadTimerTask = new BackgroundTimerTask(this, getStatusBar());
		uploader.schedule(backgroundUploadTimerTask, 0, BACKGROUND_UPLOAD_CHECK_MILLIS);

		errorChecker = new javax.swing.Timer(10*1000, new UploadErrorChecker());
		errorChecker.start();
	}

	private void loadConfigInfo()
	{
		try
		{
			getApp().loadConfigInfo();
			displayPossiblePublicBulletinRemovalNotification();
			
			if(getApp().getConfigInfo().isNewVersion())
			{
				if(!confirmDlg("NewerConfigInfoFileFound"))
					exitWithoutSavingState();
				getApp().saveConfigInfo();
			}			
		}
		catch (LoadConfigInfoException e)
		{
			notifyDlg("corruptconfiginfo");
		}
		catch(SaveConfigInfoException e)
		{
			notifyDlg("ErrorSavingConfig");
		}
		catch (Exception e)
		{
			notifyDlg("ErrorSavingConfig");
		}
		
		if(createdNewAccount)
		{
			File bulletinDefaultDetailsFile = getApp().getBulletinDefaultDetailsFile();
			if(bulletinDefaultDetailsFile.exists())
				updateBulletinDetails(bulletinDefaultDetailsFile);
		}
	}

	private void displayPossiblePublicBulletinRemovalNotification() throws Exception
	{
		if (getApp().getConfigInfo().shouldShowOneTimeNoticeFortheRemovalOfPublicBulletins())
		{
			FxDialogHelper.showNotificationDialog(this, "LegacyPublicIsPrivateMessage");
		}
	}
	
	private void reloadPendingRetrieveQueue()
	{
		try
		{
			getApp().loadRetrieveCommand();
			return;
		}
		catch(RetrieveCommand.DataVersionException e)
		{
			notifyDlg("RetrieveFileDataVersionError");
		}
		catch (Exception e)
		{
			notifyDlg("RetrieveFileError");
		}

		try
		{
			getApp().cancelBackgroundRetrieve();
		} 
		catch (Exception notMuchWeCanDoAboutIt)
		{
			notMuchWeCanDoAboutIt.printStackTrace();
		}
	}

	private boolean sessionSignIn()
	{
		int mode = UiSigninDlg.INITIAL_NEW_RECOVER_ACCOUNT;
		if(getApp().doesAnyAccountExist())
			mode = UiSigninDlg.INITIAL;
		
		while(!isAlreadySignedIn())
		{
			int result = signIn(mode); 
			if(result== UiSigninDlg.CANCEL)
				return false;
			if(result == UiSigninDlg.SIGN_IN)
				return true;
			
			switch(result)
			{
				case UiSigninDlg.NEW_ACCOUNT:
				{
					setCreatedNewAccount(false);
					startAccountSetupWizard();
					if(isAlreadySignedIn())
						setCreatedNewAccount(true);
					break;
				}
				case UiSigninDlg.RECOVER_ACCOUNT_BY_SHARE:
				{	
					UiBackupRecoverSharedKeyPair recover = new UiBackupRecoverSharedKeyPair(this);
					if(recover.recoverKeyPairFromMultipleUnencryptedFiles())
						justRecovered = true;
					break;
				}
				case UiSigninDlg.RECOVER_ACCOUNT_BY_BACKUP_FILE:
				{
					UiRecoverKeyPairFromBackup recover = new UiRecoverKeyPairFromBackup(this);
					if(recover.recoverPrivateKey())
						justRecovered = true;
					break;
				}
				default:
				{
					throw new RuntimeException("Unknown signin result: " + result);
				}
			}
		}
		
		return true;
	}

	public void doPostSigninAppInitialization()
	{
		try
		{
			startInactivityTimeoutDetection();
			getApp().doAfterSigninInitalization();
		}
		catch (UnableToLoadCurrentTemplateException e)
		{
			MartusLogger.logException(e);
			notifyDlg("UnableToLoadCurrentTemplate");
		}
		catch (FileVerificationException e1)
		{
			askToRepairMissingOrCorruptAccountMapSignature(); 
		}
		catch (MissingAccountMapSignatureException e1)
		{
			askToRepairMissingOrCorruptAccountMapSignature(); 
		}
		catch (MissingAccountMapException e1)
		{
			askToRepairMissingAccountMapFile();
		}
		catch (MartusAppInitializationException e1)
		{
			initializationErrorExitMartusDlg(e1.getMessage());
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
			initializationErrorExitMartusDlg(e.getMessage());
		}
	}
    
 	private boolean isAlreadySignedIn()
	{
		return (getApp().getAccountId() != null);
	}

	private void askToRepairMissingOrCorruptAccountMapSignature()
	{
		if(!confirmDlgBeep("WarnMissingOrCorruptAccountMapSignatureFile"))
			exitWithoutSavingState();
		try 
		{
			getApp().getStore().signAccountMap();
			getApp().doAfterSigninInitalization();
			
		} 
		catch (Exception e) 
		{
			initializationErrorExitMartusDlg(e.getMessage());
		}
	}

	private void askToRepairMissingAccountMapFile()
	{
		if(!confirmDlgBeep("WarnMissingAccountMapFile"))
			exitWithoutSavingState();
		try 
		{
			getApp().getStore().deleteAllBulletins();
			getApp().doAfterSigninInitalization();
		} 
		catch (Exception e) 
		{
			initializationErrorExitMartusDlg(e.getMessage());
		}
	}
	
	private void preventTwoInstances()
	{
		try
		{
			File lockFile = getLockFile();
			lockStream = new FileOutputStream(lockFile);
			lockToPreventTwoInstances = lockStream.getChannel().tryLock();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if(lockToPreventTwoInstances == null)
		{
			notifyDlg("AlreadyRunning");
			System.exit(1);
		}
	}

	private File getLockFile()
	{
		return new File(getApp().getMartusDataRootDirectory(), "lock");
	}
	
	public void unLock()
	{
		try
		{
			lockToPreventTwoInstances.release();
			lockStream.close();
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void prepareToExitMartus()
	{
		preparingToExitMartus = true;
	}

	private boolean loadFoldersAndBulletins() throws Exception
	{
		MartusLogger.logBeginProcess("quarantineUnreadableBulletins");
		int quarantineCount = getApp().quarantineUnreadableBulletins();
		MartusLogger.logEndProcess("quarantineUnreadableBulletins");
		if(quarantineCount > 0)
			notifyDlg("FoundDamagedBulletins");

		loadFieldSpecCache();

		MartusLogger.logBeginProcess("loadFolders");
		getApp().loadFolders();
		MartusLogger.logEndProcess("loadFolders");
		if(getStore().needsFolderMigration())
		{
			if(!confirmDlg("NeedsFolderMigration"))
				return false;
			
			try
			{
				getStore().migrateFolders();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				notifyDlg("FolderMigrationFailed");
			}
		}
		
		MartusLogger.logBeginProcess("repairOrphans");
		int orphanCount = getApp().repairOrphans();
		MartusLogger.logEndProcess("repairOrphans");
		if(orphanCount > 0)
			notifyDlg("FoundOrphans");

		ConfigInfo configInfo = getApp().getConfigInfo();
		if(!configInfo.isBulletinVersioningAware())
		{
			if(!confirmDlg("NeedsBulletinVersioningMigration"))
				return false;
			
			configInfo.setBulletinVersioningAware(true);
			saveConfigInfo();
		}
		
		return true;
	}
	
	private void askAndBackupKeypairIfRequired()
	{

		ConfigInfo info = getApp().getConfigInfo();
		boolean hasBackedUpEncrypted = info.hasUserBackedUpKeypairEncrypted();
		boolean hasBackedUpShare = info.hasUserBackedUpKeypairShare();
		boolean hasBackedUpImprovedShare = info.hasBackedUpImprovedKeypairShare();
		boolean askForBackupAgainInSevenDays = false;
		boolean dontAskForBackupAgain = false;
		if(!hasBackedUpEncrypted || !hasBackedUpShare || !hasBackedUpImprovedShare)
		{
			if(info.getDateLastAskedUserToBackupKeypair().isEmpty())
				askForBackupAgainInSevenDays = true;
			if(getApp().shouldWeAskForKeypairBackup())
			{
				askForBackupAgainInSevenDays = true;

				String generalMsg = getLocalization().getFieldLabel("confirmgeneralBackupKeyPairMsgcause");
				String generalMsgEffect = getLocalization().getFieldLabel("confirmgeneralBackupKeyPairMsgeffect");
				String backupEncrypted = "";
				String backupShare = "";
				String backupImprovedShare = "";
				if(!hasBackedUpEncrypted)
					backupEncrypted = getLocalization().getFieldLabel("confirmbackupIncompleteEncryptedNeeded");
				if(!hasBackedUpShare)
					backupShare = getLocalization().getFieldLabel("confirmbackupIncompleteShareNeeded");
				if (hasBackedUpShare && !hasBackedUpImprovedShare)
					backupImprovedShare = getLocalization().getFieldLabel("confirmbackupIncompleteImprovedShareNeeded");
				String[] contents = new String[] {generalMsg, "", backupEncrypted, "", getBackupShareText(backupImprovedShare, backupShare), "", generalMsgEffect};
				if(confirmDlg(getCurrentActiveFrame(), getLocalization().getWindowTitle("askToBackupKeyPair"), contents))
				{
					if(!hasBackedUpEncrypted)
						askToBackupKeyPairEncryptedSingleFile();
					if(!hasBackedUpShare || !hasBackedUpImprovedShare)
						askToBackupKeyPareToSecretShareFiles();
				}
			}
		}
		else
		{
			dontAskForBackupAgain = true;
		}

		try
		{
			if(askForBackupAgainInSevenDays)
				getApp().startClockToAskForKeypairBackup();
			if(dontAskForBackupAgain)
				getApp().clearClockToAskForKeypairBackup();
		}	
		catch (SaveConfigInfoException e)
		{
			MartusLogger.logException(e);
		}
	}

	private String getBackupShareText(String backupImprovedShareText, String backupShareText)
	{
		if (backupImprovedShareText.length() > 0)
			return backupImprovedShareText;
		return backupShareText;
	}

	void notifyClientCompliance()
	{
		String productDescription = XmlUtilities.getXmlEncoded(getLocalization().getFieldLabel("SplashProductDescription"));
		// NOTE: If this program contains ANY changes that have 
		// not been officially released by Benetech, you MUST 
		// change the splash screen text as required by the 
		// Martus source code license. The easiest way to do 
		// this is to set modified=true and edit the text below. 
		final boolean modified = false;
		
		String complianceStatementAlwaysEnglish = "";
		if(modified)
		{
			complianceStatementAlwaysEnglish =
			BEGIN_HTML_TAGS + 
			"[*your product name*].  <br></br>" +
			productDescription + "<br></br>" +
			"This software is not a standard Martus(TM) program, <br></br>" +
			"because it has been modified by someone other than Benetech, <br></br>" +
			"the copyright owner and original author of the Martus software.  <br></br>" +
			"For details of what has been changed, see [*here*]." +
			END_HTML_TAGS;
		}
		else
		{
			complianceStatementAlwaysEnglish =
			BEGIN_HTML_TAGS +
			"Martus(TM)<br></br>" +
			productDescription +
			END_HTML_TAGS;
		}
		new UiSplashDlg(getLocalization(), complianceStatementAlwaysEnglish);
	}
	public final static String BEGIN_HTML_TAGS = "<font size='5'>";
	public final static String END_HTML_TAGS = "</font>";
	
    public boolean isMainWindowInitalizing()
    {
    	return mainWindowInitalizing;
    }

	@Override
    public MartusApp getApp()
    {
		return getSession().getApp();
	}
	
	@Override
	public MartusLocalization getLocalization()
	{
		return getSession().getLocalization();
	}

	@Override
	public ClientBulletinStore getStore()
	{
		return getApp().getStore();
	}

	public void setCreatedNewAccount(boolean didCreateNewAccount)
	{
		createdNewAccount = didCreateNewAccount;
	}

	@Override
	public void resetCursor()
	{
		getSwingFrame().setCursor((Cursor)cursorStack.pop());
	}

	@Override
	public void setWaitingCursor()
	{
		cursorStack.push(getSwingFrame().getCursor());
		getSwingFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		return;
	}
	
	public void allBulletinsInCurrentFolderHaveChanged()
	{
		UiBulletinTablePane bulletinsTablePane = getBulletinsTablePane();
		if(bulletinsTablePane != null)
			bulletinsTablePane.allBulletinsInCurrentFolderHaveChanged();
	}

	public void bulletinSelectionHasChanged()
	{
		UiBulletinTablePane bulletinsTablePane = getBulletinsTablePane();
		if(bulletinsTablePane == null)
			return;
		Bulletin b = bulletinsTablePane.getSingleSelectedBulletin();
		if(mainPane == null)
			return;
		getMainPane().updateEnabledStatuses();
		getPreviewPane().setCurrentBulletin(b);
	}

	public void bulletinContentsHaveChanged(Bulletin b)
	{
		FxMainStage stage = getMainStage();
		if(stage != null)
		{
			try
			{
				//TODO this is really for preview, also we shouldn't have to do this, the FX class should be the observer
				//     for folderContents and BulletinContents HasChanged.
				stage.getBulletinsListController().bulletinContentsHaveChanged(b);
			}
			catch (Exception e)
			{
				MartusLogger.logException(e);
			}
			return;
		}
		UiBulletinTablePane bulletinsTablePane = getBulletinsTablePane();
		if(bulletinsTablePane == null)
			return;
		bulletinsTablePane.bulletinContentsHaveChanged(b);
		UiBulletinPreviewPane previewPane = getPreviewPane();
		if(previewPane != null)
			previewPane.bulletinContentsHaveChanged(b);
	}
	
	public void allFolderContentsHaveChanged()
	{
		Vector allFolders = getStore().getAllFolders();
		for (int i = 0; i < allFolders.size(); i++)
		{	
			folderContentsHaveChanged((BulletinFolder)allFolders.get(i));
		}
		folderTreeContentsHaveChanged();
		selectSentFolder();
	}

	public void folderSelectionHasChanged(BulletinFolder f)
	{
		setWaitingCursor();
		if(UiSession.defaultFoldersUnsorted)
			f.sortBy("");
		UiBulletinTablePane bulletinsTablePane = getBulletinsTablePane();
		if(bulletinsTablePane == null)
			return;
		bulletinsTablePane.setFolder(f);
		resetCursor();
	}

	public void folderContentsHaveChanged(BulletinFolder f)
	{
		UiFolderTreePane folderTreePane = getFolderTreePane();
		if(folderTreePane != null)
			folderTreePane.folderContentsHaveChanged(f);
		UiBulletinTablePane bulletinsTablePane = getBulletinsTablePane();
		if(bulletinsTablePane == null)
			return;
		bulletinsTablePane.folderContentsHaveChanged(f);
	}

	public void folderTreeContentsHaveChanged()
	{
		UiFolderTreePane folderTreePane = getFolderTreePane();
		if(folderTreePane != null)
			folderTreePane.folderTreeContentsHaveChanged();
		if(UiSession.isJavaFx())
			getMainStage().getCaseManager().folderContentsHaveChanged();
	}

	public boolean isDiscardedFolderSelected()
	{
		if(getFolderTreePane() == null)
			return false;
		
		return getSelectedFolderName().equals(getApp().getStore().getFolderDiscarded().getName());
	}

	public boolean isCurrentFolderEmpty()
	{
		UiBulletinTablePane bulletinsTablePane = getBulletinsTablePane();
		if(bulletinsTablePane == null)
			return true;
		if(bulletinsTablePane.getBulletinCount() == 0)
			return true;
		return false;
	}

	public boolean canPaste()
	{
		if(UiClipboardUtilities.getClipboardTransferableBulletin() != null)
			return true;

		if(UiClipboardUtilities.getClipboardTransferableFiles() != null)
			return true;

		return false;
	}

	public boolean canModifyCurrentFolder()
	{
		BulletinFolder folder = getSelectedFolder();
		return canModifyFolder(folder);
	}

	boolean canModifyFolder(BulletinFolder folder)
	{
		if(folder == null)
			return false;
		return folder.canRename();
	}

	public void selectSentFolder()
	{
		ClientBulletinStore store = getStore();
		BulletinFolder folder = store.getFolderSaved();
		selectFolder(folder);
	}

	public void selectFolder(BulletinFolder folder)
	{
		UiFolderTreePane folderTreePane = getFolderTreePane();
		if(folderTreePane != null)
			folderTreePane.selectFolder(folder.getName());
	}

	public void selectSearchFolder()
	{
		UiFolderTreePane folderTreePane = getFolderTreePane();
		if(folderTreePane != null)
			folderTreePane.selectFolder(getStore().getSearchFolderName());
	}

	public void selectNewCurrentBulletin(int currentPosition)
	{
		UiBulletinTablePane bulletinsTablePane = getBulletinsTablePane();
		if(bulletinsTablePane == null)
			return;
		if(currentPosition == -1)
			bulletinsTablePane.selectLastBulletin();
		else
			bulletinsTablePane.setCurrentBulletinIndex(currentPosition);
	}

	@Override
	public boolean confirmDlgBeep(String baseTag)
	{			
		Toolkit.getDefaultToolkit().beep();
		return confirmDlg(baseTag);
	}
	
	@Override
	public boolean confirmDlg(String baseTag)
	{
		return confirmDlg(getCurrentActiveFrame(), baseTag);
	}
	
	@Override
	public boolean confirmDlg(JFrame parent, String baseTag)
	{
		return UiUtilities.confirmDlg(getLocalization(), parent, baseTag);
	}

	public boolean confirmDlg(String baseTag, Map tokenReplacement)
	{
		return confirmDlg(getCurrentActiveFrame(), baseTag, tokenReplacement);
	}

	@Override
	public boolean confirmDlg(JFrame parent, String baseTag, Map tokenReplacement)
	{
		return UiUtilities.confirmDlg(getLocalization(), parent, baseTag, tokenReplacement);
	}

	public boolean confirmDlg(String title, String[] contents)
	{
		return confirmDlg(getCurrentActiveFrame(), title, contents);
	}

	@Override
	public boolean confirmDlg(JFrame parent, String title, String[] contents)
	{
		return UiUtilities.confirmDlg(getLocalization(), parent, title, contents);
	}

	public boolean confirmDlg(String title, String[] contents, String[] buttons)
	{
		return confirmDlg(getCurrentActiveFrame(), title, contents, buttons);
	}

	@Override
	public boolean confirmDlg(JFrame parent, String title, String[] contents, String[] buttons)
	{
		return UiUtilities.confirmDlg(parent, title, contents, buttons);
	}

	public boolean confirmDlg(String title, String[] contents, String[] buttons, Map tokenReplacement)
	{
		return UiUtilities.confirmDlg(getCurrentActiveFrame(), title, contents, buttons, tokenReplacement);
	}

	@Override
	public boolean confirmCustomButtonsDlg(JFrame parent,String baseTag, String[] buttons, Map tokenReplacement)
	{
		String title = getConfirmDialogTitle(baseTag);
		String cause = getConfirmCauseText(baseTag);
		String effect = getConfirmEffectText(baseTag);
		String[] contents = {cause, "", effect};

		return confirmDlg(parent, title, contents, buttons, tokenReplacement);
	}

	public String getConfirmEffectText(String baseTag)
	{
		String effect = getLocalization().getFieldLabel("confirm" + baseTag + "effect");
		return effect;
	}

	public String getConfirmCauseText(String baseTag)
	{
		String cause = getLocalization().getFieldLabel("confirm" + baseTag + "cause");
		return cause;
	}

	public String getConfirmDialogTitle(String baseTag)
	{
		String title = getLocalization().getWindowTitle("confirm" + baseTag);
		return title;
	}

	private boolean confirmDlg(JFrame parent, String title, String[] contents, String[] buttons, Map tokenReplacement)
	{
		return UiUtilities.confirmDlg(parent, title, contents, buttons, tokenReplacement);
	}

	@Override
	public void notifyDlgBeep(String baseTag)
	{			
		Toolkit.getDefaultToolkit().beep();
		notifyDlg(baseTag);
	}
	
	@Override
	public void notifyDlgBeep(JFrame parent, String baseTag)
	{			
		Toolkit.getDefaultToolkit().beep();
		notifyDlg(parent, baseTag);
	}
	
	@Override
	public void unexpectedErrorDlg(Exception e)
	{
		MartusLogger.logException(e);
		notifyDlg("UnexpectedError");
	}
	
	private static class Notifier implements Runnable
	{
		public Notifier(UiMainWindow mainWindowToUse, String baseTagToUse)
		{
			mainWindow = mainWindowToUse;
			baseTag = baseTagToUse;
		}
		
		public void run()
		{
			mainWindow.notifyDlg(baseTag); 
		}

		private UiMainWindow mainWindow;
		private String baseTag;
	}

	public static void showNotifyDlgOnSwingThread(UiMainWindow mainWindowToUse, String baseTag)
	{
		SwingUtilities.invokeLater(new Notifier(mainWindowToUse, baseTag));
	}

	@Override
	public void notifyDlg(String baseTag)
	{
		HashMap emptyTokenReplacement = new HashMap();
		notifyDlg(getCurrentActiveFrame(), baseTag, emptyTokenReplacement);
	}
	
	@Override
	public void notifyDlg(String baseTag, Map tokenReplacement)
	{
		notifyDlg(getCurrentActiveFrame(), baseTag, tokenReplacement);
	}

	@Override
	public void notifyDlg(JFrame parent, String baseTag)
	{
		HashMap emptyTokenReplacement = new HashMap();
		notifyDlg(parent, baseTag, emptyTokenReplacement);
	}

	private void notifyDlg(JFrame parent, String baseTag, Map tokenReplacement)
	{
		notifyDlg(parent, baseTag, "notify" + baseTag, tokenReplacement);
	}

	@Override
	public void notifyDlg(JFrame parent, String baseTag, String titleTag)
	{
		HashMap emptyTokenReplacement = new HashMap();
		notifyDlg(parent, baseTag, titleTag, emptyTokenReplacement);
	}

	private void notifyDlg(JFrame parent, String baseTag, String titleTag, Map tokenReplacement)
	{
		UiUtilities.notifyDlg(getLocalization(), parent, baseTag, titleTag, tokenReplacement);
	}

	public void notifyDlg(String title, String[] contents, String[] buttons)
	{
		new UiNotifyDlg(getCurrentActiveFrame(), title, contents, buttons);  
	}

	public void messageDlg(String baseTag, String message, Map tokenReplacement)
	{
		messageDlg(getCurrentActiveFrame(), baseTag, message, tokenReplacement);
	}

	@Override
	public void messageDlg(JFrame parent, String baseTag, String message)
	{
		messageDlg(parent, baseTag, message, new HashMap());
	}

	@Override
	public void messageDlg(JFrame parent, String baseTag, String message, Map tokenReplacement)
	{
		UiUtilities.messageDlg(getLocalization(), parent, baseTag, message, tokenReplacement);
	}

	private void initializationErrorExitMartusDlg(String message)
	{
		String title = "Error Starting Martus";
		String cause = "Unable to start Martus: \n" + message;
		String ok = "OK";
		String[] buttons = { ok };
		UiOptionPane pane = new UiOptionPane(cause, UiOptionPane.INFORMATION_MESSAGE, UiOptionPane.DEFAULT_OPTION,
								null, buttons);
		JDialog dialog = pane.createDialog(null, title);
		dialog.setVisible(true);
		System.exit(1);
	}

	public String getStringInput(String baseTag, String descriptionTag, String rawDescriptionText, String defaultText)
	{
		UiStringInputDlg inputDlg = new UiStringInputDlg(this, baseTag, descriptionTag, rawDescriptionText, defaultText);
		inputDlg.setFocusToInputField();
		inputDlg.setVisible(true);
		return inputDlg.getResult();
	}

	public UiPopupMenu getPopupMenu()
	{
		return getMainPane().getPopupMenu();
	}
	
	public AbstractAction getActionMenuPaste()
	{
		return getMainPane().getActionMenuPaste();
	}


	//ClipboardOwner Interface
	//TODO: This doesn't seem to be called right now--can we delete it?
	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents)
	{
		System.out.println("UiMainWindow: ClipboardOwner.lostOwnership");
		TransferableBulletinList tb = TransferableBulletinList.extractFrom(contents);
		if(tb != null)
			tb.dispose();
	}

	public Dimension getBulletinEditorDimension()
	{
		return getUiState().getCurrentEditorDimension();
	}

	public Point getBulletinEditorPosition()
	{
		return getUiState().getCurrentEditorPosition();
	}

	public boolean isBulletinEditorMaximized()
	{
		return getUiState().isCurrentEditorMaximized();
	}

	public void setBulletinEditorDimension(Dimension size)
	{
		getUiState().setCurrentEditorDimension(size);
	}

	public void setBulletinEditorPosition(Point position)
	{
		getUiState().setCurrentEditorPosition(position);
	}

	public void setBulletinEditorMaximized(boolean maximized)
	{
		getUiState().setCurrentEditorMaximized(maximized);
	}

	public void saveCurrentUiState()
	{
		getSession().saveCurrentUiState();
	}

	public void saveState()
	{
		try
		{
			saveStateWithoutPrompting();
		}
		catch(Exception e)
		{
			MartusLogger.logException(e);
			notifyDlg("ErrorSavingState");
		}
	}

	void saveStateWithoutPrompting() throws Exception
	{
		getApp().saveStateWithoutPrompting();
		
		CurrentUiState uiState = getUiState();

		String folderName = getSelectedFolderName();
		BulletinFolder folder = getStore().findFolder(folderName);
		if(folder == null)
			folderName = "";
		
		uiState.setCurrentFolder(folderName);
		
		getSession().copyLocalizationSettingsToUiState();
		if(folder != null)
		{
			uiState.setCurrentSortTag(folder.sortedBy());
			uiState.setCurrentSortDirection(folder.getSortDirection());
			UiBulletinTablePane bulletinsTablePane = getBulletinsTablePane();
			if(bulletinsTablePane != null)
				uiState.setCurrentBulletinPosition(bulletinsTablePane.getCurrentBulletinIndex());
		}
		uiState.setCurrentPreviewSplitterPosition(getPreviewSplitterDividerLocation());
		uiState.setCurrentFolderSplitterPosition(getFolderSplitterDividerLocation());
		uiState.setCurrentAppDimension(getSwingFrame().getSize());
		uiState.setCurrentAppPosition(getSwingFrame().getLocation());
		boolean isMaximized = getSwingFrame().getExtendedState()==JFrame.MAXIMIZED_BOTH;
		uiState.setCurrentAppMaximized(isMaximized);
		saveCurrentUiState();
	}

	private static final int ARBITRARY_FALLBACK_SPLITTER_LOCATION = 100;
	
	public int getFolderSplitterDividerLocation()
	{
		FolderSplitPane folderSplitter = getFolderSplitter();
		if(folderSplitter == null)
			return ARBITRARY_FALLBACK_SPLITTER_LOCATION;
		
		return folderSplitter.getDividerLocation();
	}

	public int getPreviewSplitterDividerLocation()
	{
		JSplitPane previewSplitter = getPreviewSplitter();
		if(previewSplitter == null)
		{
			return ARBITRARY_FALLBACK_SPLITTER_LOCATION;
		}
		
		return previewSplitter.getDividerLocation();
	}

	public String getSelectedFolderName()
	{
		UiFolderTreePane folderTreePane = getFolderTreePane();
		if(folderTreePane == null)
			return null;
		return folderTreePane.getSelectedFolderName();
	}

	public void restoreState()
	{
		String folderName = getUiState().getCurrentFolder();
		BulletinFolder folder = getStore().findFolder(folderName);

		if(folder == null)
		{
			selectSentFolder();
			return;
		}

		try
		{
			String sortTag = getUiState().getCurrentSortTag();
			if(UiSession.defaultFoldersUnsorted)
				sortTag = "";
			folder.sortBy(sortTag);
			if(folder.getSortDirection() != getUiState().getCurrentSortDirection())
				folder.sortBy(sortTag);
			UiFolderTreePane folderTreePane = getFolderTreePane();
			if(folderTreePane != null)
				folderTreePane.selectFolder(folderName);
		}
		catch(Exception e)
		{
			System.out.println("UiMainWindow.restoreState: " + e);
		}
	}

	public void selectBulletinInCurrentFolderIfExists(UniversalId id)
	{
		String selectedFolderName = getSelectedFolderName();
		BulletinFolder currentFolder = getApp().getStore().findFolder(selectedFolderName);
		if(currentFolder == null)
		{
			System.out.println("Current folder is null: " + selectedFolderName);
			return;
		}
		int position = currentFolder.find(id);
		if(position != -1)
		{
			UiBulletinTablePane bulletinsTablePane = getBulletinsTablePane();
			if(bulletinsTablePane == null)
				return;
			bulletinsTablePane.setCurrentBulletinIndex(position);
		}
	}

	public void forceRebuildOfPreview()
	{
		UiBulletinPreviewPane previewPane = getPreviewPane();
		if(previewPane == null)
			previewPane.setCurrentBulletin(null);
		UiBulletinTablePane bulletinsTablePane = getBulletinsTablePane();
		if(bulletinsTablePane == null)
			return;
		bulletinsTablePane.currentFolderContentsHaveChanged();
		bulletinsTablePane.selectFirstBulletin();
	}
	
	public void doBackgroundWork(WorkerProgressThread worker, UiProgressWithCancelDlg progressDialog) throws Exception
	{
		setWaitingCursor();
		try
		{
			worker.start(progressDialog);
			progressDialog.pack();
			Utilities.centerDlg(progressDialog);
			progressDialog.setVisible(true);
			worker.cleanup();
		}
		finally
		{
			resetCursor();
		}
	}
	
	
	public void doBackgroundWork(WorkerThread worker, String dialogTag) throws Exception
	{
		setWaitingCursor();
		try
		{
			ModalBusyDialog dlg = new ModalBusyDialog(this, dialogTag);
			worker.start(dlg);
			dlg.setVisible(true);
			worker.cleanup();
		}
		finally
		{
			resetCursor();
		}
	}
	
	public SearchTreeNode askUserForSearchCriteria() throws ParseException
	{
		UiFancySearchDlg searchDlg = new UiFancySearchDlg(this);
		searchDlg.setSearchFinalBulletinsOnly(getUiState().searchFinalBulletinsOnly());
		searchDlg.setSearchSameRowsOnly(getUiState().searchSameRowsOnly());
		String searchString = getUiState().getSearchString();
		JSONObject search = new JSONObject();
		if(searchString.startsWith("{"))
			search = new JSONObject(searchString);
		searchDlg.setSearchAsJson(search);
		searchDlg.setVisible(true);
		if(!searchDlg.getResults())
			return null;
		

		getUiState().setSearchFinalBulletinsOnly(searchDlg.searchFinalBulletinsOnly());
		getUiState().setSearchSameRowsOnly(searchDlg.searchSameRowsOnly());
		getUiState().setSearchString(searchDlg.getSearchAsJson().toString());
		return searchDlg.getSearchTree();
	}

	public void updateSearchFolderAndNotifyUserOfTheResults(SortableBulletinList matchedBulletinsFromSearch)
	{
		if(matchedBulletinsFromSearch == null)
			return;
		getApp().updateSearchFolder(matchedBulletinsFromSearch);
		ClientBulletinStore store = getStore();
		BulletinFolder searchFolder = store.findFolder(store.getSearchFolderName());
		UiFolderTreePane folderTreePane = getFolderTreePane();
		if(folderTreePane == null)
			return;
		folderTreePane.folderTreeContentsHaveChanged();
		folderTreePane.folderContentsHaveChanged(searchFolder);
		int bulletinsFound = searchFolder.getBulletinCount();
		if(bulletinsFound > 0)
		{
			selectSearchFolder();
			showNumberOfBulletinsFound(bulletinsFound, "SearchFound");
		}
		else
		{
			notifyDlg("SearchFailed");
		}
	}

	public void showNumberOfBulletinsFound(int bulletinsFound,String messageTag)
	{
		String title = getLocalization().getWindowTitle("notifySearchFound");
		String message = getLocalization().getFieldLabel(messageTag);
		String ok = getLocalization().getButtonLabel(EnglishCommonStrings.OK);
		String[] buttons = { ok };
		message = replaceToken(message , "#NumberBulletinsFound#", (new Integer(bulletinsFound)).toString());
		UiOptionPane pane = new UiOptionPane(message, UiOptionPane.INFORMATION_MESSAGE, UiOptionPane.DEFAULT_OPTION,
								null, buttons);
		JDialog dialog = pane.createDialog(getSwingFrame(), title);
		dialog.setVisible(true);
	}

	public void aboutMartus()
	{
		new UiAboutDlg(this);
	}

	public void showAccountInfo()
	{
		String title = getLocalization().getWindowTitle("AuthorInformation");
		String userName = getLocalization().getFieldLabel("AccountInfoUserName")
						  + getApp().getUserName();
		String keyDescription = getLocalization().getFieldLabel("AccountInfoPublicKey");
		String keyContents = getApp().getAccountId();
		String codeDescriptionOld = getLocalization().getFieldLabel("AccountInfoPublicCode");
		String codeDescriptionNew = getLocalization().getFieldLabel("AccountInfoPublicCode40");
		String formattedCodeContentsOld = null;
		String formattedCodeContentsNew = null;
		try
		{
			formattedCodeContentsOld = MartusCrypto.computeFormattedPublicCode(keyContents);
			formattedCodeContentsNew = MartusCrypto.computeFormattedPublicCode40(keyContents);
		}
		catch(Exception e)
		{
		}

		
		String martusAccountAccessToken = "";
		String martusAccountAccessTokenDescription = getLocalization().getFieldLabel("AccountAccessToken");
		try
		{
			MartusAccountAccessToken accountToken = getApp().getConfigInfo().getCurrentMartusAccountAccessToken();
			martusAccountAccessToken = accountToken.getToken();
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
		} 
		
		String accountDirectory = getLocalization().getFieldLabel("AccountInfoDirectory") + getApp().getCurrentAccountDirectory();
		
		
		String ok = getLocalization().getButtonLabel(EnglishCommonStrings.OK);
		String[] contents = {userName, " ", keyDescription, keyContents," ", codeDescriptionOld, formattedCodeContentsOld, " ", codeDescriptionNew, formattedCodeContentsNew, " ", martusAccountAccessTokenDescription, martusAccountAccessToken, " ", accountDirectory};
		String[] buttons = {ok};

		notifyDlg(title, contents, buttons);
	}

	public void displayHelpMessage()
	{
		InputStream helpStream = null;
		InputStream helpStreamTOC = null;
		String currentLanguage = getLocalization().getCurrentLanguageCode();

		helpStream = getApp().getHelpMain(currentLanguage);
		if(helpStream != null)
			helpStreamTOC = getApp().getHelpTOC(currentLanguage);
		else
		{
			helpStream = getApp().getHelpMain(MtfAwareLocalization.ENGLISH);
			helpStreamTOC = getApp().getHelpTOC(MtfAwareLocalization.ENGLISH);
		}

		UiOnlineHelpDlg dlg = new UiOnlineHelpDlg(this, "Help", helpStream, "OnlineHelpMessage", helpStreamTOC, "OnlineHelpTOCMessage");
		dlg.setVisible(true);
		
		try 
		{
			if(helpStream != null)
				helpStream.close();
			
			if(helpStreamTOC != null)
				helpStreamTOC.close();
		} 
		catch (IOException e) 
		{
			System.out.println("UiMainWindow: DisplayHelpMessage:"+e.getMessage());
		}
	}

	public int getPreviewWidth()
	{
		UiBulletinPreviewPane previewPane = getPreviewPane();
		if(previewPane == null)
			return 0;
		return previewPane.getView().getWidth();
	}

	public void respondToPreferencesChanges()
	{
		initializeViews();
		restoreState();
		getTransport().updateStatus();
		backgroundUploadTimerTask.setWaitingForServer();
		getSwingFrame().setVisible(true);
	}

	
	public void forceRecheckOfUidsOnServer()
	{
		if(backgroundUploadTimerTask != null)
			backgroundUploadTimerTask.forceRecheckOfUidsOnServer();
	}
	
	private OrchidTransportWrapper getTransport()
	{
		return getApp().getTransport();
	}

	public void offerToCancelRetrieveInProgress()
	{
		if(!isRetrieveInProgress())
			return;
		
		if(!confirmDlg(getSwingFrame(), "CancelRetrieve"))
			return;
		
		try
		{
			cancelRetrieve();
		}
		catch (Exception e)
		{
			notifyDlg("UnexpectedError");
		}
	}
	
	private void cancelRetrieve() throws Exception
	{
		getApp().cancelBackgroundRetrieve();
		setStatusMessageReady();
	}

	public boolean isRetrieveInProgress()
	{
		return getApp().getCurrentRetrieveCommand().getRemainingToRetrieveCount() > 0;
	}
	
	public String getServerCompliance(ClientSideNetworkGateway gateway)
	{
		try
		{
			return getApp().getServerCompliance(gateway);
		}
		catch (Exception e)
		{
			return "";
		}
	}

	public boolean confirmServerCompliance(String descriptionTag, String newServerCompliance)
	{
		if(newServerCompliance.equals(""))
			return confirmDlg("ServerComplianceFailed");
			
		UiShowScrollableTextDlg dlg = new UiShowScrollableTextDlg(this, "ServerCompliance", "ServerComplianceAccept", "ServerComplianceReject", descriptionTag, newServerCompliance, null);
		return dlg.getResult();
	}
	
	@Override
	public void saveConfigInfo()
	{
		try
		{
			getApp().saveConfigInfo();
		}
		catch (MartusApp.SaveConfigInfoException e)
		{
			notifyDlg("ErrorSavingConfig");
		}
	}

	@Override
	public boolean isServerConfigured()
	{
		return getApp().getConfigInfo().isServerConfigured();
	}

	public boolean reSignIn()
	{
		int result = signIn(UiSigninDlg.SECURITY_VALIDATE);
		if(!getApp().isSignedIn())
			exitWithoutSavingState();
		if(result == UiSigninDlg.SIGN_IN)
			return true;
		return false;
	}


	public boolean getAndSaveUserNamePassword(File keyPairFile) 
	{
		String originalUserName = getApp().getUserName();
		UiCreateNewAccountProcess newUserInfo = new UiCreateNewAccountProcess(this, originalUserName);
		if(!newUserInfo.isDataValid())
			return false;
		File accountsHashOfUserNameFile = getApp().getUserNameHashFile(keyPairFile.getParentFile());
		accountsHashOfUserNameFile.delete();
		return saveKeyPairFile(keyPairFile, newUserInfo.getUserName(), newUserInfo.getPassword());
	}

	public boolean saveKeyPairFile(File keyPairFile, String userName, char[] userPassword)
	{
		try
		{
			getApp().writeKeyPairFileWithBackup(keyPairFile, userName, userPassword);
			getApp().attemptSignIn(userName, userPassword);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			notifyDlg(currentActiveFrame, "RewriteKeyPairFailed");
			return false;
			//TODO eventually try to restore keypair from backup.
		}
		return true;
	}

	public void updateBulletinDetails(File defaultFile)
	{
		ConfigInfo info = getApp().getConfigInfo();
		File details = getApp().getBulletinDefaultDetailsFile();
		UiTemplateDlg templateDlg = new UiTemplateDlg(this, info, details);
		try
		{
			if(defaultFile != null)
			{
				templateDlg.loadFile(defaultFile);
				notifyDlg("ConfirmCorrectDefaultDetailsData");
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return;
		}

		templateDlg.setVisible(true);
		if(templateDlg.getResult())
		{
			try
			{
				getApp().saveConfigInfo();
			}
			catch (MartusApp.SaveConfigInfoException e)
			{
				System.out.println("doContactInfo: Unable to Save ConfigInfo" + e);
			}
		}
	}

	public void retrieveBulletins(RetrieveTableModel model, String folderName,
						String dlgTitleTag, String summariesProgressTag, String retrieverProgressTag)
	{
		if(isRetrieveInProgress())
		{
			notifyDlg("RetrieveInProgress");
			return;
		}
		
		try
		{
			UiServerSummariesDlg summariesDlg = new UiServerSummariesRetrieveDlg(this, model, dlgTitleTag);
			Vector uidList = displaySummariesDialog(model, dlgTitleTag, summariesProgressTag, summariesDlg);
			if(uidList == null)
				return;
			
			retrieveRecordsFromServer(folderName, uidList);
		}
		catch(ServerErrorException e)
		{
			notifyDlg("ServerError");
			return;
		}
		catch(Exception e)
		{
			notifyDlg("UnexpectedError");
		}
	}

	public void retrieveRecordsFromServer(String folderName, Vector uidList)
			throws MartusSignatureException, NoKeyPairException,
			EncryptionException, IOException
	{
		getApp().createOrFindFolder(folderName);
		getApp().getStore().saveFolders();
		folderTreeContentsHaveChanged();

		RetrieveCommand command = new RetrieveCommand(folderName, uidList);
		getApp().startBackgroundRetrieve(command);
		
		setStatusMessageTag(STATUS_RETRIEVING);
	}


	public Vector displaySummariesDialog(RetrieveTableModel model, String dlgTitleTag, String summariesProgressTag, UiServerSummariesDlg summariesDlg) throws ServerErrorException
	{
		RetrieveSummariesProgressMeter progressHandler = new RetrieveSummariesProgressMeter();
		setWaitingCursor();	
		boolean retrievedSummaries = retrieveSummaries(model, dlgTitleTag, progressHandler);
		resetCursor();

		if(!retrievedSummaries)
			return null;
		summariesDlg.initialize();
		progressHandler.requestCancel();
		if(!summariesDlg.getResult())
			return null;
		return summariesDlg.getUniversalIdList();
	}

	private boolean retrieveSummaries(RetrieveTableModel model, String dlgTitleTag, RetrieveSummariesProgressMeter progressHandler) throws ServerErrorException
	{
		try
		{
			if(!getApp().isSSLServerAvailable())
			{
				notifyDlg(getSwingFrame(), "retrievenoserver", dlgTitleTag);
				return false;
			}
			model.initialize(progressHandler);
		} 
		catch (NetworkOfflineException e)
		{
			notifyDlg("ErrorNetworkOffline");
			return false;
		}
		catch (Exception e)
		{
			unexpectedErrorDlg(e);
			return false;
		}

		if(progressHandler.shouldExit())
			return false;
		try
		{
			model.checkIfErrorOccurred();
		}
		catch (Exception e)
		{
			notifyDlg(getSwingFrame(), "RetrievedOnlySomeSummaries", dlgTitleTag);
		}
		return true;
	}

	public void deleteMutableRecordsFromServer(Vector uidList)
			throws MartusSignatureException, WrongAccountException, Exception
	{
		setWaitingCursor();
		try
		{
			String result = getApp().deleteServerDraftBulletins(uidList);
			if (!result.equals(NetworkInterfaceConstants.OK))
			{
				if(UiSession.isJavaFx())
					FxDialogHelper.showNotificationDialog(this, "DeleteServerDraftsFailed");
				else
					notifyDlg("DeleteServerDraftsFailed");
				return;
			}

			if(UiSession.isJavaFx())
				folderTreeContentsHaveChanged();
			else
				notifyDlg("DeleteServerDraftsWorked");
		} 
		finally
		{
			resetCursor();
		}
	}

	public void askToBackupKeyPairEncryptedSingleFile()
	{
		if(confirmDlg("BackupKeyPairInformation"))
			doBackupKeyPairToSingleEncryptedFile();
	}

	public void askToBackupKeyPareToSecretShareFiles()
	{
		UiBackupRecoverSharedKeyPair backup = new UiBackupRecoverSharedKeyPair(this);
		backup.backupKeyPairToMultipleUnencryptedFiles();
	}

	public void doBackupKeyPairToSingleEncryptedFile() 
	{
		File keypairFile = getApp().getCurrentKeyPairFile();
		if(keypairFile.length() > MAX_KEYPAIRFILE_SIZE)
		{
			System.out.println("keypair file too large!");
			notifyDlg("ErrorBackingupKeyPair");
			return;
		}
		
		String defaultBackupFilename = "MartusKeyPairBackup.dat";
		File newBackupFile = showFileSaveDialog("SaveKeyPair", defaultBackupFilename, new KeyPairFormatFilter(getLocalization()));
		if(newBackupFile == null)
			return;

		try
		{
			FileInputStream input = new FileInputStream(keypairFile);
			FileOutputStream output = new FileOutputStream(newBackupFile);
	
			int originalKeyPairFileSize = (int) keypairFile.length();
			byte[] inputArray = new byte[originalKeyPairFileSize];
	
			input.read(inputArray);
			output.write(inputArray);
			input.close();
			output.close();
			if(FileVerifier.verifyFiles(keypairFile, newBackupFile))
			{
				notifyDlg("OperationCompleted");
				getApp().getConfigInfo().setBackedUpKeypairEncrypted(true);
				getApp().saveConfigInfo();
			}
			else
			{
				notifyDlg("ErrorBackingupKeyPair");
			}
		}
		catch (SaveConfigInfoException e)
		{
			e.printStackTrace();
			notifyDlg("ErrorSavingConfig");
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
			notifyDlg("ErrorSavingFile");
		}
	}
	
	public KeyPairFormatFilter getKeyPairFormatFilter()
	{
		return new KeyPairFormatFilter(getLocalization());
	}
	
	public void displayScrollableMessage(String titleTag, String message, String okButtonTag, Map tokenReplacement) 
	{
		new UiShowScrollableTextDlg(this, titleTag, okButtonTag, MtfAwareLocalization.UNUSED_TAG, MtfAwareLocalization.UNUSED_TAG, message, tokenReplacement, null);
	}
	
	public void setAndSaveHQKeysInConfigInfo(HeadquartersKeys allHQKeys, HeadquartersKeys defaultHQKeys)
	{
		try
		{
			getApp().setAndSaveHQKeys(allHQKeys, defaultHQKeys);
		}
		catch(MartusApp.SaveConfigInfoException e)
		{
			notifyDlg("ErrorSavingConfig");
		}
	}

	void initializeViews()
	{
		MartusLogger.logBeginProcess("Initializing views");

		swingFrame = new MainSwingFrame(this);
		UiMainWindow.updateIcon(getSwingFrame());
		setCurrentActiveFrame(getSwingFrame());
		getSwingFrame().setVisible(true);
		updateTitle();
		setWindowSizeAndState();

		if(UiSession.isJavaFx())
		{
			mainStage = new FxMainStage(this);
			statusBar = new FxStatusBar(getLocalization());
			FxRunner fxRunner = new FxRunner(mainStage);
			fxRunner.setAbortImmediatelyOnError();
			Platform.runLater(fxRunner);
			getSwingFrame().setContentPane(mainStage);
		}
		else
		{
			statusBar = new UiStatusBar(getLocalization());		
			mainPane = new UiMainPane(this, getUiState());
			getSwingFrame().setContentPane(mainPane);
		}

		getTransport().setProgressMeter(getTorProgressMeter());
		// NOTE: re-start Tor here in case it was turned on in the wizard
		getApp().startOrStopTorAsRequested();
		
		MartusLogger.logEndProcess("Initializing views");

		MartusLogger.logBeginProcess("Checking server status");
		setWaitingCursor();
		updateServerStatusInStatusBar();
		resetCursor();
		MartusLogger.logEndProcess("Checking server status");
	}

	public ProgressMeterInterface getTorProgressMeter()
	{
		StatusBar torStatusBar = getStatusBar();
		if(torStatusBar == null)
			return null;
		
		return torStatusBar.getTorProgressMeter();
	}

	public void setWindowSizeAndState()
	{
		Dimension screenSize = Utilities.getViewableScreenSize();
		Dimension appDimension = getUiState().getCurrentAppDimension();
		Point appPosition = getUiState().getCurrentAppPosition();
		boolean showMaximized = false;
		if(Utilities.isValidScreenPosition(screenSize, appDimension, appPosition))
		{
			getSwingFrame().setLocation(appPosition);
			getSwingFrame().setSize(appDimension);
			if(getUiState().isCurrentAppMaximized())
				showMaximized = true;
		}
		else
			showMaximized = true;
		
		if(showMaximized)
		{
			getSwingFrame().setSize(screenSize.width - 50 , screenSize.height - 50);
			Utilities.maximizeWindow(getSwingFrame());
		}
		
		getUiState().setCurrentAppDimension(getSwingFrame().getSize());
	}

	private void updateTitle() {
		getSwingFrame().setTitle(getLocalization().getWindowTitle("main"));
	}

	public void updateServerStatusInStatusBar()
	{		
		if (!getApp().isServerConfigured())
		{	
			setStatusMessageTag(STATUS_SERVER_NOT_CONFIGURED);
			return;
		}
		
		if(!getApp().getTransport().isOnline())
		{
			setStatusMessageTag(STATUS_SERVER_OFFLINE_MODE);
			return;
		}
	
		ClientSideNetworkGateway gateway = getApp().getCurrentNetworkInterfaceGateway();
		try
		{
			if(getApp().isSSLServerAvailable(gateway))
			{
				setStatusMessageReady();
				return;
			}
		}
		catch(NetworkOfflineException e)
		{
			setStatusMessageTag(STATUS_SERVER_OFFLINE_MODE);
			return;
		}
		catch(Exception e)
		{
			MartusLogger.logException(e);
		}

		setStatusMessageTag(STATUS_NO_SERVER_AVAILABLE);
	}
	
	public void clearStatusMessage()
	{
		setStatusMessageTag("");
	}
	
	public void setStatusMessageTag(String tag)
	{
		if(getStatusBar() != null)
			getStatusBar().setStatusMessageTag(tag);
	}
	
	public void setStatusMessageReady()
	{
		setStatusMessageTag(UiMainWindow.STATUS_READY);
	}

	int signIn(int mode)
	{
		int seconds = 0;
		UiModelessBusyDlg busyDlg = null;
		while(true)
		{
			Delay delay = new Delay(seconds);
			delay.start();
			Utilities.waitForThreadToTerminate(delay);
			if( busyDlg != null )
				busyDlg.endDialog();

			seconds = seconds * 2 + 1;
			if(mode == UiSigninDlg.TIMED_OUT)
			{
				//Forces this dialog to the top of all windows in system by switching from iconified to normal, then just make the main window not visible
				//cml caused problem when retrieving bulletin summaries when it times out	
				//currentActiveFrame.setState(NORMAL);
				//currentActiveFrame.setVisible(false);
			}
			UiSigninDlg signinDlg = null;
			int userChoice = UiSigninDlg.LANGUAGE_CHANGED;
			String userName = "";
			char[] userPassword = "".toCharArray();
			while(userChoice == UiSigninDlg.LANGUAGE_CHANGED)
			{	
				if(mode==UiSigninDlg.INITIAL || mode == UiSigninDlg.INITIAL_NEW_RECOVER_ACCOUNT)
					signinDlg = new UiInitialSigninDlg(getLocalization(), getCurrentUiState(), mode, userName, userPassword);
				else
				{
					if(getCurrentActiveDialog() != null)
						signinDlg = new UiSigninDlg(getLocalization(), getCurrentUiState(), (JFrame)null, mode, userName, userPassword);
					else
						signinDlg = new UiSigninDlg(getLocalization(), getCurrentUiState(), getCurrentActiveFrame(), mode, userName, userPassword);
				}
				userChoice = signinDlg.getUserChoice();
				userName = signinDlg.getNameText();
				userPassword = signinDlg.getPassword();
			}
			if (userChoice != UiSigninDlg.SIGN_IN)
				return userChoice;
			try
			{
				if(mode == UiSigninDlg.INITIAL)
				{	
					getApp().attemptSignIn(userName, userPassword);
				}
				else
				{	
					getApp().attemptReSignIn(userName, userPassword);
				}
				return UiSigninDlg.SIGN_IN;
			}
			catch (Exception e)
			{
				notifyDlg(getCurrentActiveFrame(), "incorrectsignin");
				busyDlg = new UiModelessBusyDlg(getLocalization().getFieldLabel("waitAfterFailedSignIn"));
			}
			finally
			{
				UiPasswordField.scrubData(userPassword);
			}
		}
	}

	private boolean showRelevantUploadReminder()
	{
		boolean dontExitApplication = false;
		if(!getApp().isSealedOutboxEmpty())
		{
			if(confirmDlg("UploadReminder"))
				getApp().resetLastUploadRemindedTime();
			else
				dontExitApplication = true;
		}
		else if(!getApp().isDraftOutboxEmpty())
		{
			if(!confirmDlg("DraftUploadReminder"))
				dontExitApplication = true;
		}
		return dontExitApplication;
	}

	@Override
	public void exitNormally()
	{
		if(showRelevantUploadReminder())
			return;
		
		try
		{
			MartusLogger.logBeginProcess("saveState");
			saveState();
			MartusLogger.logEndProcess("saveState");
			getStore().prepareToExitNormally();
			System.out.println("exitNormally:");
			System.out.println("    verifyPacket: " + Packet.callsToVerifyPacketSignature + 
					" calls took total " + Packet.millisInVerifyPacketSignature + " ms");
			System.out.println("    loadPacket:   " + XmlPacketLoader.callsToXmlPacketLoader + 
					" calls took total " + XmlPacketLoader.millisInXmlPacketLoader + " ms");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			notifyDlg("ErrorDuringExit");
		}

		MartusLogger.logMemoryStatistics();

		exitWithoutSavingState();
	}

	@Override
	public void exitWithoutSavingState()
	{
		getStore().prepareToExitWithoutSavingState();
		try
		{
			lockToPreventTwoInstances.release();
		} 
		catch (IOException e)
		{
			MartusLogger.logException(e);
		}
		try
		{
			lockStream.close();
		} 
		catch (IOException e)
		{
			MartusLogger.logException(e);
		}
		try
		{
			getLockFile().delete();
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
		System.exit(0);
	}

	public void createBulletin()
	{
		try
		{
			Bulletin b = getApp().createBulletin();
			modifyBulletin(b);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			notifyDlg("UnexpectedError");
		}
	}

	public void modifyBulletin(Bulletin b) throws Exception
	{
		getCurrentUiState().setModifyingBulletin(true);
		getSwingFrame().setEnabled(false);
		UiBulletinModifyDlg dlg = null;
		try
		{
			dlg = new UiBulletinModifyDlg(b, this);
			setCurrentActiveFrame(dlg);
			getSwingFrame().setVisible(false);
			dlg.setVisible(true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			if(dlg != null)
				dlg.dispose();
			doneModifyingBulletin();
			throw(e);
		}
	}

	public void doneModifyingBulletin()
	{
		getCurrentUiState().setModifyingBulletin(false);
		getSwingFrame().setEnabled(true);
		getSwingFrame().setVisible(true);
		setCurrentActiveFrame(getSwingFrame());
	}

	public BulletinFolder getSelectedFolder()
	{
		UiFolderTreePane folderTreePane = getFolderTreePane();
		if(folderTreePane == null)
			return null;
		
		return folderTreePane.getSelectedFolder();
	}

	
	public Vector getSelectedBulletins(String tagZeroBulletinsSelected) throws Exception
	{
		UiBulletinTablePane bulletinsTablePane = getBulletinsTablePane();
		if(bulletinsTablePane == null)
			return null;
		UniversalId[] uids = bulletinsTablePane.getSelectedBulletinUids();
		if(uids.length == 0)
		{
			notifyDlg(tagZeroBulletinsSelected);
			return new Vector();
		}
		return getBulletins(uids);
	}
	
	public Vector getBulletins(UniversalId[] uids) throws Exception
	{
		BulletinGetterThread thread = new BulletinGetterThread(getStore(), uids);
		doBackgroundWork(thread, "PreparingBulletins");
		return thread.getBulletins();
	}
	
	public boolean getBulletinsAlwaysPrivate()
	{
		return getApp().getConfigInfo().shouldForceBulletinsAllPrivate();
	}

    public boolean getUseZawgyiFont()
	{
		return getApp().getConfigInfo().getUseZawgyiFont();
	}

	@Override
	public boolean getDoZawgyiConversion()
	{
	 	return getApp().getConfigInfo().getDoZawgyiConversion();
	}
	
	@Override
	public boolean getUseInternalTor()
	{
		return getApp().getConfigInfo().useInternalTor();
	}

	public boolean isAnyBulletinSelected()
	{
		UiBulletinTablePane bulletinsTablePane = getBulletinsTablePane();
		if(bulletinsTablePane == null)
			return false;
		
		return (bulletinsTablePane.getSelectedBulletinUids().length > 0);
	}

	public boolean isOnlyOneBulletinSelected()
	{
		UiBulletinTablePane bulletinsTablePane = getBulletinsTablePane();
		if(bulletinsTablePane == null)
			return false;
		
		return (bulletinsTablePane.getSelectedBulletinUids().length == 1);
	}
	
	static public String getDisplayVersionInfo(MiniLocalization localization)
	{
		String versionInfo = UiConstants.programName;
		versionInfo += " " + localization.getFieldLabel("aboutDlgVersionInfo");
		versionInfo += " " + UiConstants.versionLabel;
		return versionInfo;
	}
	
	
	private class TimeoutTimerTask extends TimerTask
	{
		public TimeoutTimerTask()
		{
		}

		@Override
		public void run()
		{
			try 
			{
				if(!hasTimedOut() || waitingForSignin)
					return;
				
				MartusLogger.log(MartusLogger.getMemoryStatistics());
				MartusLogger.logBeginProcess("Save before timeout");
				try
				{
					getStore().prepareToExitNormally();
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
				MartusLogger.logEndProcess("Save before timeout");
				System.gc();
				MartusLogger.log(MartusLogger.getMemoryStatistics());

				waitingForSignin = true;
				SwingUtilities.invokeLater(new ThreadedSignin());
			} 
			catch (Throwable e) 
			{
				// No problem, even out of memory, should kill this thread!
				e.printStackTrace();
			} 
		}

		boolean hasTimedOut()
		{
			if(inactivityDetector.secondsSinceLastActivity() > Martus.timeoutInXSeconds)
				return true;

			return false;
		}

		class ThreadedSignin implements Runnable
		{
			@Override
			public void run()
			{
				JFrame frame = getCurrentActiveFrame();
				if(frame != null)
				{
					frame.setGlassPane(new WindowObscurer());
					frame.getGlassPane().setVisible(true);
				}
				JDialog dialog = getCurrentActiveDialog();
				if(dialog != null)
				{
					dialog.setGlassPane(new WindowObscurer());
					dialog.getGlassPane().setVisible(true);
				}
				if(signIn(UiSigninDlg.TIMED_OUT) != UiSigninDlg.SIGN_IN)
				{
					System.out.println("Cancelled from timeout signin");
					exitWithoutSavingState();
				}
				MartusLogger.log("Restoring active frame");
				if(frame != null)
				{
					frame.getGlassPane().setVisible(false);
				}
				if(dialog != null)
				{
					dialog.getGlassPane().setVisible(false);
				}
				waitingForSignin = false;
			}
		}
		
		boolean waitingForSignin;
	}

	class UploadErrorChecker extends AbstractAction
	{
		@Override
		public void actionPerformed(ActionEvent evt)
		{
			if(uploadResult == null)
				return;

			if(uploadResult.equals(NetworkInterfaceConstants.REJECTED) && !rejectedErrorShown)
			{
				notifyDlg("uploadrejected");
				rejectedErrorShown = true;
			}
			if(uploadResult.equals(MartusApp.AUTHENTICATE_SERVER_FAILED) && !authenticationErrorShown)
			{
				notifyDlg("AuthenticateServerFailed");
				authenticationErrorShown = true;
			}
			if(uploadResult.equals(BackgroundUploader.CONTACT_INFO_NOT_SENT) && !contactInfoErrorShown)
			{
				notifyDlg("contactRejected");
				contactInfoErrorShown = true;
			}
		}

		boolean authenticationErrorShown;
		boolean rejectedErrorShown;
		boolean contactInfoErrorShown;
	}
	
	public CurrentUiState getCurrentUiState()
	{
		return getUiState();
	}
	
	static public Image getMartusIconImage()
	{
		URL imageURL = UiMainWindow.class.getResource("dialogs/Martus-swoosh-64x64.png");
		if(imageURL == null)
			return null;
		ImageIcon imageicon = new ImageIcon(imageURL);
		return imageicon.getImage();
	}
	
	static public void updateIcon(JFrame window)
	{
		Image image = getMartusIconImage();
		if(image != null)
		{
			window.setIconImage(image);
			//com.apple.eawt.Application.getApplication().setDockIconImage(image);		
		}
	}

	@Override
	public void setCurrentActiveFrame(JFrame currentActiveFrame)
	{
		this.currentActiveFrame = currentActiveFrame;
	}

	@Override
	public JFrame getCurrentActiveFrame()
	{
		return currentActiveFrame;
	}
	
	@Override
	public void setCurrentActiveDialog(JDialog newActiveDialog)
	{
		currentActiveDialog = newActiveDialog;
	}
	
	@Override
	public JDialog getCurrentActiveDialog()
	{
		return currentActiveDialog;
	}
	
	private int getTextFieldColumns(int windowWidth) 
	{
		if(windowWidth < MINIMUM_SCREEN_WIDTH)
			return MINIMUM_TEXT_FIELD_WIDTH;
		windowWidth -= MINIMUM_SCREEN_WIDTH;
		
		int veryApproximateCharWidthInPixels = FontHandler.defaultFontSize;
		int widthToUse = MINIMUM_TEXT_FIELD_WIDTH + (windowWidth / veryApproximateCharWidthInPixels);
		return widthToUse;
	}
	
	public int getPreviewTextFieldColumns()
	{
		int dividerLocation = getFolderSplitterDividerLocation();
		int previewWindowWidth = Utilities.getViewableScreenSize().width - dividerLocation;
		if(LanguageOptions.isRightToLeftLanguage())
			previewWindowWidth = dividerLocation;
		return getTextFieldColumns(previewWindowWidth);
	}

	public int getEditingTextFieldColumns()
	{
		return getTextFieldColumns(Utilities.getViewableScreenSize().width);
	}
	
	public File showFileOpenDialog(String fileDialogCategory, FileFilter filter)
	{
		return internalShowFileOpenDialog(fileDialogCategory, null, filter);
	}
	
	public File showFileOpenDialogWithDirectoryMemory(String fileDialogCategory)
	{
		return showFileOpenDialogWithDirectoryMemory(fileDialogCategory, (FileFilter)null);
	}

	public File showFileOpenDialogWithDirectoryMemory(String fileDialogCategory, FileFilter filter)
	{
		File directory = UiSession.getMemorizedFileOpenDirectories().get(fileDialogCategory);
		File file = internalShowFileOpenDialog(fileDialogCategory, directory, filter);
		if(file != null)
			UiSession.getMemorizedFileOpenDirectories().put(fileDialogCategory, file.getParentFile());
		return file;
	}
	
	private File internalShowFileOpenDialog(String fileDialogCategory, File directory, FileFilter filter)
	{
		String title = getLocalization().getWindowTitle("FileDialog" + fileDialogCategory);
		String okButtonLabel = getLocalization().getButtonLabel("FileDialogOk" + fileDialogCategory);
		if(directory == null)
			directory = getApp().getCurrentAccountDirectory();
		return FileDialogHelpers.doFileOpenDialog(getCurrentActiveFrame(), title, okButtonLabel, directory, filter);
	}
	
	public File showFileSaveDialogNoFilterWithDirectoryMemory(String fileDialogCategory, String defaultFilename)
	{
		File directory = UiSession.getMemorizedFileOpenDirectories().get(fileDialogCategory);
		File file = internalShowFileSaveDialog(fileDialogCategory, directory, defaultFilename, null);
		if(file != null)
			UiSession.getMemorizedFileOpenDirectories().put(fileDialogCategory, file.getParentFile());
		return file;
	}

	public File showFileSaveDialog(String fileDialogCategory, FormatFilter filter)
	{
		return showFileSaveDialog(fileDialogCategory, "", filter);
	}
	
	public File showFileSaveDialog(String fileDialogCategory, String defaultFilename, FormatFilter filter)
	{
		return internalShowFileSaveDialog(fileDialogCategory, null, defaultFilename, filter);
	}
	
	private File internalShowFileSaveDialog(String fileDialogCategory, File defaultDirectory, String defaultFilename, FormatFilter filter)
	{
		String title = getLocalization().getWindowTitle("FileDialog" + fileDialogCategory);
		if(defaultDirectory == null)
			defaultDirectory = getApp().getCurrentAccountDirectory();
		return FileDialogHelpers.doFileSaveDialog(getCurrentActiveFrame(), title, defaultDirectory, defaultFilename, filter, getLocalization());
	}

	void setLocalization(MartusLocalization localization)
	{
		getSession().getLocalization();
	}
	
	private UiSession getSession()
	{
		return session;
	}
	
	public CurrentUiState getUiState()
	{
		return getSession().getUiState();
	}

	public void initalizeUiState()
	{
		getSession().initalizeUiState();
	}

	public void initalizeUiState(String defaultLanguageCode)
	{
		getSession().initalizeUiState(defaultLanguageCode);
	}

	public UiMainPane getMainPane()
	{
		return mainPane;
	}
	
	public FxMainStage getMainStage()
	{
		return mainStage;
	}

	private UiBulletinTablePane getBulletinsTablePane()
	{
		if(getMainPane() == null)
			return null;
		
		return getMainPane().getBulletinsTable();
	}

	private UiBulletinPreviewPane getPreviewPane()
	{
		if(getMainPane() == null)
			return null;
		return getMainPane().getPreviewPane();
	}

	private JSplitPane getPreviewSplitter()
	{
		if(getMainPane() == null)
			return null;
		
		return getMainPane().getPreviewSplitter();
	}

	public StatusBar getStatusBar()
	{
		return statusBar;
	}

	private FolderSplitPane getFolderSplitter()
	{
		if(getMainPane() == null)
			return null;
		
		return getMainPane().getFolderSplitter();
	}

	private UiFolderTreePane getFolderTreePane()
	{
		if(getMainPane() == null)
			return null;
		
		return getMainPane().getFolderTreePane();
	}

	public void setNeedToGetAccessToken()
	{
		backgroundUploadTimerTask.setNeedToGetAccessToken();
	}

	public static final String STATUS_RETRIEVING = "StatusRetrieving";
	public static final String STATUS_READY = "StatusReady";
	public static final String STATUS_CONNECTING = "StatusConnecting";
	public static final String STATUS_NO_SERVER_AVAILABLE = "NoServerAvailableProgressMessage";
	public static final String STATUS_SERVER_NOT_CONFIGURED = "ServerNotConfiguredProgressMessage";
	public static final String STATUS_SERVER_OFFLINE_MODE = "OfflineModeProgressMessage";

	public static final int MINIMUM_TEXT_FIELD_WIDTH = 30;
	private static final int MINIMUM_SCREEN_WIDTH = 700;
	public static final int MAX_KEYPAIRFILE_SIZE = 32000;
	private static final int BACKGROUND_UPLOAD_CHECK_MILLIS = 5*1000;
	private static final int BACKGROUND_TIMEOUT_CHECK_EVERY_X_MILLIS = 5*1000;

	private UiSession session;

	private UiMainPane mainPane;
	private FxMainStage mainStage;

	private java.util.Timer uploader;
	private java.util.Timer timeoutChecker;
	private javax.swing.Timer errorChecker;
	private BackgroundTimerTask backgroundUploadTimerTask;
	private TimeoutTimerTask timeoutTimerTask;
	String uploadResult;
	UiInactivityDetector inactivityDetector;

	private JFrame currentActiveFrame;
	private JDialog currentActiveDialog;
	
	public boolean inConfigServer;
	boolean preparingToExitMartus;
	private boolean createdNewAccount;
	private boolean justRecovered;
	boolean mainWindowInitalizing;

	private FileLock lockToPreventTwoInstances; 
	private FileOutputStream lockStream;
	private Stack cursorStack;
	private StatusBar statusBar;

	private JFrame swingFrame;

}
