/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2005, Beneficent
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

package org.martus.client.swingui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.BackgroundUploader;
import org.martus.client.core.ConfigInfo;
import org.martus.client.core.MartusApp;
import org.martus.client.core.TransferableBulletinList;
import org.martus.client.core.MartusApp.LoadConfigInfoException;
import org.martus.client.core.MartusApp.MartusAppInitializationException;
import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.client.swingui.bulletincomponent.UiBulletinPreviewPane;
import org.martus.client.swingui.bulletintable.UiBulletinTablePane;
import org.martus.client.swingui.dialogs.UiAboutDlg;
import org.martus.client.swingui.dialogs.UiBulletinModifyDlg;
import org.martus.client.swingui.dialogs.UiConfigServerDlg;
import org.martus.client.swingui.dialogs.UiConfigureHQs;
import org.martus.client.swingui.dialogs.UiContactInfoDlg;
import org.martus.client.swingui.dialogs.UiCreateNewAccountProcess;
import org.martus.client.swingui.dialogs.UiExportBulletinsDlg;
import org.martus.client.swingui.dialogs.UiFancySearchDlg;
import org.martus.client.swingui.dialogs.UiInitialSigninDlg;
import org.martus.client.swingui.dialogs.UiModelessBusyDlg;
import org.martus.client.swingui.dialogs.UiOnlineHelpDlg;
import org.martus.client.swingui.dialogs.UiPreferencesDlg;
import org.martus.client.swingui.dialogs.UiProgressRetrieveBulletinsDlg;
import org.martus.client.swingui.dialogs.UiProgressRetrieveSummariesDlg;
import org.martus.client.swingui.dialogs.UiRemoveServerDlg;
import org.martus.client.swingui.dialogs.UiServerSummariesDeleteDlg;
import org.martus.client.swingui.dialogs.UiServerSummariesDlg;
import org.martus.client.swingui.dialogs.UiServerSummariesRetrieveDlg;
import org.martus.client.swingui.dialogs.UiSetFolderOrderDlg;
import org.martus.client.swingui.dialogs.UiShowScrollableTextDlg;
import org.martus.client.swingui.dialogs.UiSigninDlg;
import org.martus.client.swingui.dialogs.UiSplashDlg;
import org.martus.client.swingui.dialogs.UiStringInputDlg;
import org.martus.client.swingui.dialogs.UiTemplateDlg;
import org.martus.client.swingui.dialogs.UiWarningMessageDlg;
import org.martus.client.swingui.foldertree.UiFolderTreePane;
import org.martus.client.swingui.tablemodels.DeleteMyServerDraftsTableModel;
import org.martus.client.swingui.tablemodels.RetrieveHQDraftsTableModel;
import org.martus.client.swingui.tablemodels.RetrieveHQTableModel;
import org.martus.client.swingui.tablemodels.RetrieveMyDraftsTableModel;
import org.martus.client.swingui.tablemodels.RetrieveMyTableModel;
import org.martus.client.swingui.tablemodels.RetrieveTableModel;
import org.martus.clientside.ClientSideNetworkGateway;
import org.martus.clientside.CurrentUiState;
import org.martus.clientside.MtfAwareLocalization;
import org.martus.clientside.UiLocalization;
import org.martus.clientside.UiUtilities;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.HQKeys;
import org.martus.common.MartusUtilities;
import org.martus.common.Version;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.MartusUtilities.ServerErrorException;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.FileDatabase.MissingAccountMapException;
import org.martus.common.database.FileDatabase.MissingAccountMapSignatureException;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.packet.Packet;
import org.martus.common.packet.UniversalId;
import org.martus.common.utilities.DateUtilities;
import org.martus.swing.UiFileChooser;
import org.martus.swing.UiLanguageDirection;
import org.martus.swing.UiNotifyDlg;
import org.martus.swing.UiOptionPane;
import org.martus.swing.UiPasswordField;
import org.martus.swing.UiPopupMenu;
import org.martus.swing.Utilities;
import org.martus.swing.Utilities.Delay;
import org.martus.util.FileVerifier;
import org.martus.util.OneEntryMap;
import org.martus.util.TokenReplacement;
import org.martus.util.UnicodeReader;
import org.martus.util.Base64.InvalidBase64Exception;
import org.martus.util.TokenReplacement.TokenInvalidException;
import org.martus.util.language.LanguageOptions;
import org.martus.util.xml.XmlUtilities;

public class UiMainWindow extends JFrame implements ClipboardOwner
{
	public UiMainWindow()
	{
		super();
		cursorStack = new Stack();
		UiModelessBusyDlg splashScreen = new UiModelessBusyDlg(new ImageIcon(UiAboutDlg.class.getResource("MartusLogo.gif")));
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setCurrentActiveFrame(this);
		try
		{
			localization = new MartusLocalization(MartusApp.getTranslationsDirectory(), getAllEnglishStrings());
			app = new MartusApp(localization);
			initializeCurrentLanguage();
		}
		catch(MartusApp.MartusAppInitializationException e)
		{
			initializationErrorExitMartusDlg(e.getMessage());
		}
		UiMainWindow.updateIcon(this);
		
		timeoutInXSeconds = TIMEOUT_SECONDS;
		File timeoutDebug = new File("C:/Martus/timeout.1min");
		if(timeoutDebug !=null & timeoutDebug.exists())
			timeoutInXSeconds = TESTING_TIMEOUT_60_SECONDS;
		splashScreen.endDialog();
		initalizeUiState();
	}

	public static String[] getAllEnglishStrings()
	{
		String[] clientStrings = EnglishStrings.strings;
		int lenghtClient = clientStrings.length;
		String[] commonStrings = EnglishCommonStrings.strings;
		int lenghtCommon = commonStrings.length;
		String[] allEnglishStrings = new String[lenghtCommon+lenghtClient];
		System.arraycopy(clientStrings,0,allEnglishStrings,0,lenghtClient);
		System.arraycopy(commonStrings,0,allEnglishStrings,lenghtClient,lenghtCommon);
		return allEnglishStrings;
	}

	private void initializeCurrentLanguage()
	{
		CurrentUiState previouslySavedState = new CurrentUiState();
		previouslySavedState.load(getUiStateFile());
		
		if(previouslySavedState.getCurrentLanguage() != "")
		{	
			localization.setCurrentLanguageCode(previouslySavedState.getCurrentLanguage());
			localization.setCurrentDateFormatCode(previouslySavedState.getCurrentDateFormat());
		}
		
		if(localization.getCurrentLanguageCode()== null)
			MartusApp.setInitialUiDefaultsFromFileIfPresent(localization, new File(app.getMartusDataRootDirectory(),"DefaultUi.txt"));
		
		if(localization.getCurrentLanguageCode()== null)
		{
			localization.setCurrentLanguageCode(MtfAwareLocalization.ENGLISH);
			localization.setCurrentDateFormatCode(DateUtilities.getDefaultDateFormatCode());
		}
	}

	public File getUiStateFile()
	{
		if(app.isSignedIn())
			return app.getUiStateFileForAccount(app.getCurrentAccountDirectory());
		return new File(app.getMartusDataRootDirectory(), "UiState.dat");
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
			new UiWarningMessageDlg(owner, "",warningMessageLtoR, warningMessageRtoL);
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
		String mtfVersionNumber = localization.getTranslationVersion(languageCodeToTest);		
		map.put("#MtfVersionNumber#", mtfVersionNumber);
		map.put("#ProgramVersionNumber#", localization.extractVersion(UiConstants.versionLabel));
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

	static class HiddenFrame extends JFrame
	{
		HiddenFrame(MartusLocalization localization, String programName)
		{
			super(programName);
			UiMainWindow.updateIcon(this);
			setTitle(UiSigninDlg.getInitialSigninTitle(localization));
			if(Version.isRunningUnderWindows())
			{
				setState(Frame.ICONIFIED);
				setVisible(true);
			}
			else
			{
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				setLocation(screenSize.width + 1, screenSize.height + 1);
				setVisible(true);
				toFront();
			}
		}

	}

	public boolean run()
	{
		JFrame hiddenFrame = new HiddenFrame(getLocalization(), UiConstants.programName);
		setCurrentActiveFrame(hiddenFrame);
		{
			String currentLanguageCode = localization.getCurrentLanguageCode();
			displayDefaultUnofficialTranslationMessageIfNecessary(currentActiveFrame, localization, currentLanguageCode);
			displayIncompatibleMtfVersionWarningMessageIfNecessary(currentActiveFrame, localization, localization.getCurrentLanguageCode());
			
			preventTwoInstances();
			notifyClientCompliance();
	
			mainWindowInitalizing = true;
	
			if(!sessionSignIn())
				return false;

			loadFieldSpecCache();
			
			loadConfigInfo();
			if(!createdNewAccount && !justRecovered)
				askAndBackupKeypairIfRequired();
			
			UiModelessBusyDlg waitingForBulletinsToLoad = new UiModelessBusyDlg(getLocalization().getFieldLabel("waitingForBulletinsToLoad"));
			{
				if(!loadFoldersAndBulletins())
					return false;
		
				initializeViews();
				restoreState();
			}
			waitingForBulletinsToLoad.endDialog();

			requestContactInfo();
			
			addWindowListener(new WindowEventHandler());
			setVisible(true);
			toFront();
		}

		setCurrentActiveFrame(this);
		hiddenFrame.dispose();

		createBackgroundUploadTasks();

		mainWindowInitalizing = false;
		return true;
    }
	
	private void loadFieldSpecCache()
	{
		if(!getStore().loadFieldSpecCache())
		{
			notifyDlg(this, "CreatingFieldSpecCache");
			getStore().createFieldSpecCacheFromDatabase();
		}
	}
	
	private void createBackgroundUploadTasks()
	{
		uploader = new java.util.Timer(true);
		backgroundUploadTimerTask = new BackgroundUploadTimerTask(this);
		uploader.schedule(backgroundUploadTimerTask, 0, BACKGROUND_UPLOAD_CHECK_MILLIS);

		errorChecker = new javax.swing.Timer(10*1000, new UploadErrorChecker());
		errorChecker.start();
	}

	private void loadConfigInfo()
	{
		try
		{
			app.loadConfigInfo();
			if(app.getConfigInfo().isNewVersion())
			{
				if(!confirmDlg("NewerConfigInfoFileFound"))
					exitWithoutSavingState();
				app.saveConfigInfo();
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
		
		if(createdNewAccount)
		{
			File bulletinDefaultDetailsFile = app.getBulletinDefaultDetailsFile();
			if(bulletinDefaultDetailsFile.exists())
				updateBulletinDetails(bulletinDefaultDetailsFile);
		}
	}

	private void requestContactInfo()
	{
		ConfigInfo info = app.getConfigInfo();
		if(!info.hasEnoughContactInfo())
			doContactInfo();
		else if(info.promptUserRequestSendToServer())
		{
			requestToUpdateContactInfoOnServerAndSaveInfo();
			info.clearPromptUserRequestSendToServer();
		}
	}

	private boolean sessionSignIn()
	{
		boolean wantsNewAccount = false;
		int signInType = UiSigninDlg.INITIAL;
		if(!app.doesAnyAccountExist())
			signInType = UiSigninDlg.INITIAL_NEW_RECOVER_ACCOUNT;
		
		int result = signIn(signInType); 
		if(result == UiSigninDlg.CANCEL)
			return false;
		if(result == UiSigninDlg.NEW_ACCOUNT)
			wantsNewAccount = true;
		if(result == UiSigninDlg.RECOVER_ACCOUNT_BY_SHARE)
		{	
			UiBackupRecoverSharedKeyPair recover = new UiBackupRecoverSharedKeyPair(this);
			if(!recover.recoverKeyPairFromMultipleUnencryptedFiles())
				return false;
			justRecovered = true;
		}
		if(result == UiSigninDlg.RECOVER_ACCOUNT_BY_BACKUP_FILE)
		{
			UiRecoverKeyPairFromBackup recover = new UiRecoverKeyPairFromBackup(this);
			if(!recover.recoverPrivateKey())
				return false;
			justRecovered = true;
		}

		createdNewAccount = false;
		if(wantsNewAccount)
		{
			if(!createAccount())
				return false;
			createdNewAccount = true;
		}
		
		initalizeUiState();
		
		try
		{
			app.doAfterSigninInitalization();
		}
		catch (MartusAppInitializationException e1)
		{
			initializationErrorExitMartusDlg(e1.getMessage());
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

		inactivityDetector = new UiInactivityDetector();
		timeoutChecker = new java.util.Timer(true);
		timeoutChecker.schedule(new TimeoutTimerTask(), 0, BACKGROUND_TIMEOUT_CHECK_EVERY_X_MILLIS);

		return true;
	}
    
 	private void askToRepairMissingOrCorruptAccountMapSignature()
	{
		if(!confirmDlgBeep("WarnMissingOrCorruptAccountMapSignatureFile"))
			exitWithoutSavingState();
		try 
		{
			app.getStore().signAccountMap();
			app.doAfterSigninInitalization();
			
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
			app.getStore().deleteAllBulletins();
			app.doAfterSigninInitalization();
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
			File lockFile = new File(app.getMartusDataRootDirectory(), "lock");
			lockStream = new FileOutputStream(lockFile);
			lockToPreventTwoInstances = lockStream.getChannel().tryLock();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(lockToPreventTwoInstances == null)
		{
			notifyDlg("AlreadyRunning");
			System.exit(1);
		}
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

	private boolean loadFoldersAndBulletins()
	{
		int quarantineCount = app.quarantineUnreadableBulletins();
		if(quarantineCount > 0)
			notifyDlg("FoundDamagedBulletins");

		app.loadFolders();
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
		
		int orphanCount = app.repairOrphans();
		if(orphanCount > 0)
			notifyDlg("FoundOrphans");

		ConfigInfo configInfo = app.getConfigInfo();
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
		ConfigInfo info = app.getConfigInfo();
		boolean hasBackedUpEncrypted = info.hasUserBackedUpKeypairEncrypted();
		boolean hasBackedUpShare = info.hasUserBackedUpKeypairShare();
		if(!hasBackedUpEncrypted || !hasBackedUpShare)
		{
			String generalMsg = localization.getFieldLabel("confirmgeneralBackupKeyPairMsgcause");
			String generalMsgEffect = localization.getFieldLabel("confirmgeneralBackupKeyPairMsgeffect");
			String backupEncrypted = "";
			String backupShare = "";
			if(!hasBackedUpEncrypted)
				backupEncrypted = localization.getFieldLabel("confirmbackupIncompleteEncryptedNeeded");
			if(!hasBackedUpShare)
				backupShare = localization.getFieldLabel("confirmbackupIncompleteShareNeeded");

			String[] contents = {generalMsg, "", backupEncrypted, backupShare, "", generalMsgEffect}; 
			if(confirmDlg(getCurrentActiveFrame(), localization.getWindowTitle("askToBackupKeyPair"), contents))
			{
				if(!hasBackedUpEncrypted)
					askToBackupKeyPairEncryptedSingleFile();
				if(!hasBackedUpShare)
					askToBackupKeyPareToSecretShareFiles();
			}
		}
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
		new UiSplashDlg(getCurrentActiveFrame(), getLocalization(), complianceStatementAlwaysEnglish);
	}
	public final static String BEGIN_HTML_TAGS = "<font size='5'>";
	public final static String END_HTML_TAGS = "</font>";
	
    public boolean isMainWindowInitalizing()
    {
    	return mainWindowInitalizing;
    }

    public MartusApp getApp()
    {
		return app;
	}
	
	public MartusLocalization getLocalization()
	{
		return localization;
	}

	public ClientBulletinStore getStore()
	{
		return getApp().getStore();
	}

	public void resetCursor()
	{
		setCursor((Cursor)cursorStack.pop());
	}

	public void setWaitingCursor()
	{
		cursorStack.push(getCursor());
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		return;
	}
	
	public void allBulletinsInCurrentFolderHaveChanged()
	{
		table.allBulletinsInCurrentFolderHaveChanged();
	}

	public void bulletinSelectionHasChanged()
	{
		Bulletin b = table.getSingleSelectedBulletin();
		toolBar.updateEnabledStatuses();
		preview.setCurrentBulletin(b);
	}

	public void bulletinContentsHaveChanged(Bulletin b)
	{
		table.bulletinContentsHaveChanged(b);
		preview.bulletinContentsHaveChanged(b);
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
		table.setFolder(f);
		resetCursor();
	}

	public void folderContentsHaveChanged(BulletinFolder f)
	{
		folders.folderContentsHaveChanged(f);
		table.folderContentsHaveChanged(f);
	}

	public void folderTreeContentsHaveChanged()
	{
		folders.folderTreeContentsHaveChanged();
	}

	public boolean isDiscardedFolderSelected()
	{
		return folders.getSelectedFolderName().equals(app.getStore().getFolderDiscarded().getName());
	}

	public boolean isCurrentFolderEmpty()
	{
		if(table.getBulletinCount() == 0)
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
		BulletinFolder folder = folders.getSelectedFolder();
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
		folders.selectFolder(folder.getName());
	}

	public void selectSearchFolder()
	{
		folders.selectFolder(getStore().getSearchFolderName());
	}

	public void selectNewCurrentBulletin(int currentPosition)
	{
		if(currentPosition == -1)
			table.selectLastBulletin();
		else
			table.setCurrentBulletinIndex(currentPosition);
	}

	public boolean confirmDlgBeep(String baseTag)
	{			
		Toolkit.getDefaultToolkit().beep();
		return confirmDlg(baseTag);
	}
	
	public boolean confirmDlg(String baseTag)
	{
		return confirmDlg(getCurrentActiveFrame(), baseTag);
	}
	
	public boolean confirmDlg(JFrame parent, String baseTag)
	{
		return UiUtilities.confirmDlg(getLocalization(), parent, baseTag);
	}

	public boolean confirmDlg(JFrame parent, String baseTag, Map tokenReplacement)
	{
		return UiUtilities.confirmDlg(getLocalization(), parent, baseTag, tokenReplacement);
	}

	public boolean confirmDlg(JFrame parent, String title, String[] contents)
	{
		return UiUtilities.confirmDlg(getLocalization(), parent, title, contents);
	}

	public boolean confirmDlg(JFrame parent, String title, String[] contents, String[] buttons)
	{
		return UiUtilities.confirmDlg(parent, title, contents, buttons);
	}

	public void notifyDlgBeep(String baseTag)
	{			
		Toolkit.getDefaultToolkit().beep();
		notifyDlg(baseTag);
	}
	
	public void notifyDlgBeep(JFrame parent, String baseTag)
	{			
		Toolkit.getDefaultToolkit().beep();
		notifyDlg(parent, baseTag);
	}
	
	public void notifyDlg(String baseTag)
	{
		HashMap emptyTokenReplacement = new HashMap();
		notifyDlg(getCurrentActiveFrame(), baseTag, emptyTokenReplacement);
	}
	
	public void notifyDlg(JFrame parent, String baseTag)
	{
		HashMap emptyTokenReplacement = new HashMap();
		notifyDlg(parent, baseTag, emptyTokenReplacement);
	}

	public void notifyDlg(JFrame parent, String baseTag, Map tokenReplacement)
	{
		notifyDlg(parent, baseTag, "notify" + baseTag, tokenReplacement);
	}

	public void notifyDlg(JFrame parent, String baseTag, String titleTag)
	{
		HashMap emptyTokenReplacement = new HashMap();
		notifyDlg(parent, baseTag, titleTag, emptyTokenReplacement);
	}

	public void notifyDlg(JFrame parent, String baseTag, String titleTag, Map tokenReplacement)
	{
		UiUtilities.notifyDlg(getLocalization(), parent, baseTag, titleTag, tokenReplacement);
	}

	public void messageDlg(JFrame parent, String baseTag, String message)
	{
		messageDlg(parent, baseTag, message, new HashMap());
	}

	public void messageDlg(JFrame parent, String baseTag, String message, Map tokenReplacement)
	{
		UiUtilities.messageDlg(getLocalization(), parent, baseTag, message, tokenReplacement);
	}

	private void initializationErrorExitMartusDlg(String message)
	{
		String title = "Error Starting Martus";
		String cause = "Unable to start Martus: " + message;
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
		UiStringInputDlg inputDlg = new UiStringInputDlg(this, getLocalization(), baseTag, descriptionTag, rawDescriptionText, defaultText);
		inputDlg.setFocusToInputField();
		inputDlg.setVisible(true);
		return inputDlg.getResult();
	}

	public UiPopupMenu getPopupMenu()
	{
		UiPopupMenu menu = new UiPopupMenu();
		menu.add(menuBar.actionMenuModifyBulletin);
		menu.addSeparator();
		menu.add(menuBar.actionMenuCutBulletins);
		menu.add(menuBar.actionMenuCopyBulletins);
		menu.add(menuBar.actionMenuPasteBulletins);
		menu.add(menuBar.actionMenuSelectAllBulletins);
		menu.addSeparator();
		menu.add(menuBar.actionMenuDiscardBulletins);
		return menu;
	}
	
	public AbstractAction getActionMenuPaste()
	{
		return menuBar.actionMenuPasteBulletins;
	}


	//ClipboardOwner Interface
	//TODO: This doesn't seem to be called right now--can we delete it?
	public void lostOwnership(Clipboard clipboard, Transferable contents)
	{
		System.out.println("UiMainWindow: ClipboardOwner.lostOwnership");
		TransferableBulletinList tb = TransferableBulletinList.extractFrom(contents);
		if(tb != null)
			tb.dispose();
	}


	public void setCurrentDefaultKeyboardVirtual(boolean keyboard)
	{
		uiState.setCurrentDefaultKeyboardVirtual(keyboard);
	}

	public boolean isCurrentDefaultKeyboardVirtual()
	{
		return uiState.isCurrentDefaultKeyboardVirtual();
	}

	public Dimension getBulletinEditorDimension()
	{
		return uiState.getCurrentEditorDimension();
	}

	public Point getBulletinEditorPosition()
	{
		return uiState.getCurrentEditorPosition();
	}

	public boolean isBulletinEditorMaximized()
	{
		return uiState.isCurrentEditorMaximized();
	}

	public void setBulletinEditorDimension(Dimension size)
	{
		uiState.setCurrentEditorDimension(size);
	}

	public void setBulletinEditorPosition(Point position)
	{
		uiState.setCurrentEditorPosition(position);
	}

	public void setBulletinEditorMaximized(boolean maximized)
	{
		uiState.setCurrentEditorMaximized(maximized);
	}

	public void saveCurrentUiState()
	{
		uiState.save(getUiStateFile());
	}

	public void saveState()
	{
		try
		{
			saveStateWithoutPrompting();
		}
		catch(IOException e)
		{
			notifyDlg("ErrorSavingState");
		}
	}

	void saveStateWithoutPrompting() throws IOException
	{
		String folderName = folders.getSelectedFolderName();
		BulletinFolder folder = getStore().findFolder(folderName);
		uiState.setCurrentFolder(folderName);
		uiState.setCurrentDateFormat(getLocalization().getCurrentDateFormatCode());
		uiState.setCurrentLanguage(getLocalization().getCurrentLanguageCode());
		if(folder != null)
		{
			uiState.setCurrentSortTag(folder.sortedBy());
			uiState.setCurrentSortDirection(folder.getSortDirection());
			uiState.setCurrentBulletinPosition(table.getCurrentBulletinIndex());
		}
		uiState.setCurrentPreviewSplitterPosition(previewSplitter.getDividerLocation());
		uiState.setCurrentFolderSplitterPosition(folderSplitter.getDividerLocation());
		uiState.setCurrentAppDimension(getSize());
		uiState.setCurrentAppPosition(getLocation());
		boolean isMaximized = getExtendedState()==MAXIMIZED_BOTH;
		uiState.setCurrentAppMaximized(isMaximized);
		saveCurrentUiState();
	}

	public void restoreState()
	{
		String folderName = uiState.getCurrentFolder();
		BulletinFolder folder = getStore().findFolder(folderName);

		if(folder == null)
		{
			selectSentFolder();
			return;
		}

		try
		{
			String sortTag = uiState.getCurrentSortTag();
			folder.sortBy(sortTag);
			if(folder.getSortDirection() != uiState.getCurrentSortDirection())
				folder.sortBy(sortTag);
			folders.selectFolder(folderName);
		}
		catch(Exception e)
		{
			System.out.println("UiMainWindow.restoreState: " + e);
		}
	}

	private void initalizeUiState()
	{
		uiState = new CurrentUiState();
		File uiStateFile = getUiStateFile();
		if(!uiStateFile.exists())
		{
			uiState.setCurrentLanguage(localization.getCurrentLanguageCode());
			uiState.setCurrentDateFormat(localization.getCurrentDateFormatCode());
			uiState.save(uiStateFile);
			return;
		}
		uiState.load(uiStateFile);
		localization.setCurrentDateFormatCode(uiState.getCurrentDateFormat());
	}

	public void selectBulletinInCurrentFolderIfExists(UniversalId id)
	{
		BulletinFolder currentFolder = app.getStore().findFolder(folders.getSelectedFolderName());
		int position = currentFolder.find(id);
		if(position != -1)
			table.setCurrentBulletinIndex(position);
	}

	private JComponent createTopStuff()
	{
		JPanel topStuff = new JPanel(false);
		topStuff.setLayout(new GridLayout(2, 1));

		menuBar = new UiMenuBar(this);
		topStuff.add(menuBar);

		toolBar = new UiToolBar(this);
		topStuff.add(toolBar);

		return topStuff;
	}

	public void doModifyBulletin()
	{
		table.doModifyBulletin();
	}

	public void doSelectAllBulletins()
	{
		table.doSelectAllBulletins();	
	}

	public void doCutBulletins()
	{
		table.doCutBulletins();
	}

	public void doCopyBulletins()
	{
		table.doCopyBulletins();
	}

	public void doPasteBulletins()
	{
		table.doPasteBulletins();
	}
	
	public void doResendBulletins()
	{		
		if (!isServerConfigured())
		{
			notifyDlg(this, "retrievenoserver", "ResendBulletins");
			return;
		}	
		
		table.doResendBulletins();
	}

	public void doDiscardBulletins()
	{
		table.doDiscardBulletins();
	}
	
	public void doCreateFolder()
	{
		folders.createNewFolder();
	}
	
	public void doRenameFolder()
	{
		folders.renameCurrentFolder();
	}
	
	public void doDeleteFolder()
	{
		folders.deleteCurrentFolderIfPossible();
	}
	
	public void doOrganizeFolders()
	{
		Vector originalOrderFolders = folders.getAllFolders();
		UiSetFolderOrderDlg dlg = new UiSetFolderOrderDlg(this, originalOrderFolders);
		dlg.setVisible(true);
		if(!dlg.okPressed())
			return;

		Vector reOrderedFolders = new Vector();
		for(int i = originalOrderFolders.size()-1; i >=0; --i)
		{
			reOrderedFolders.add(originalOrderFolders.get(i));
		}
		try
		{
			folders.setFolderOrder(dlg.getNewFolderOrder());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void doSearch()
	{
		// TODO: Allow either the old UiSimpleSearchDlg or the new UiFancySearchDlg
		UiFancySearchDlg searchDlg = new UiFancySearchDlg(this);
		//UiSimpleSearchDlg searchDlg = new UiSimpleSearchDlg(this);
		if(!searchDlg.getResults())
			return;
		setWaitingCursor();

		String andKeyword = getLocalization().getKeyword("and");
		String orKeyword = getLocalization().getKeyword("or");
		app.search(searchDlg.getSearchString(), andKeyword, orKeyword);
		ClientBulletinStore store = getStore();
		BulletinFolder searchFolder = store.findFolder(store.getSearchFolderName());
		folders.folderTreeContentsHaveChanged();
		folders.folderContentsHaveChanged(searchFolder);
		int bulletinsFound = searchFolder.getBulletinCount();
		resetCursor();
		if(bulletinsFound > 0)
		{
			selectSearchFolder();
			String title = getLocalization().getWindowTitle("notifySearchFound");
			String cause = getLocalization().getFieldLabel("notifySearchFoundcause");
			String ok = getLocalization().getButtonLabel("ok");
			String[] buttons = { ok };
			cause = replaceToken(cause , "#N#", (new Integer(bulletinsFound)).toString());
			UiOptionPane pane = new UiOptionPane(cause, UiOptionPane.INFORMATION_MESSAGE, UiOptionPane.DEFAULT_OPTION,
									null, buttons);
			JDialog dialog = pane.createDialog(this, title);
			dialog.setVisible(true);
		}
		else
		{
			notifyDlg("SearchFailed");
		}
	}

	public void aboutMartus()
	{
		new UiAboutDlg(this);
	}

	public void showAccountInfo()
	{
		String title = getLocalization().getWindowTitle("AccountInfo");
		String userName = getLocalization().getFieldLabel("AccountInfoUserName")
						  + app.getUserName();
		String keyDescription = getLocalization().getFieldLabel("AccountInfoPublicKey");
		String keyContents = app.getAccountId();
		String codeDescription = getLocalization().getFieldLabel("AccountInfoPublicCode");
		String formattedCodeContents = null;
		try
		{
			formattedCodeContents = MartusCrypto.computeFormattedPublicCode(keyContents);
		}
		catch(InvalidBase64Exception e)
		{
		}
		String accountDirectory = getLocalization().getFieldLabel("AccountInfoDirectory") + app.getCurrentAccountDirectory();
		
		String ok = getLocalization().getButtonLabel("ok");
		String[] contents = {userName, " ", keyDescription, keyContents," ", codeDescription, formattedCodeContents, " ", accountDirectory};
		String[] buttons = {ok};

		new UiNotifyDlg(this, title, contents, buttons);
	}

	public void displayHelpMessage()
	{
		InputStream helpStream = null;
		InputStream helpStreamTOC = null;
		String currentLanguage = getLocalization().getCurrentLanguageCode();

		helpStream = app.getHelpMain(currentLanguage);
		if(helpStream != null)
			helpStreamTOC = app.getHelpTOC(currentLanguage);
		else
		{
			helpStream = app.getHelpMain(MtfAwareLocalization.ENGLISH);
			helpStreamTOC = app.getHelpTOC(MtfAwareLocalization.ENGLISH);
		}

		new UiOnlineHelpDlg(this, "Help", helpStream, "OnlineHelpMessage", helpStreamTOC, "OnlineHelpTOCMessage");
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
		return preview.getView().getWidth();
	}

	public void doLocalize()
	{
		saveState();
		new UiPreferencesDlg(this);
		initializeViews();
		restoreState();
		setVisible(true);
	}

	public boolean doContactInfo()
	{
		ConfigInfo info = app.getConfigInfo();
		UiContactInfoDlg setupContactDlg = new UiContactInfoDlg(this, info);
		boolean pressedOk = setupContactDlg.getResult();
		if(pressedOk)
			requestToUpdateContactInfoOnServerAndSaveInfo();
		// the following is required (for unknown reasons)
		// to get the window to redraw after the dialog
		// is closed. Yuck! kbs.
		repaint();
		return pressedOk;
	}
	
	public void doRemoveServer()
	{
		if(!reSignIn())
			return;
		
		ConfigInfo info = app.getConfigInfo();
		UiRemoveServerDlg removeDlg = new UiRemoveServerDlg(this, info);
		if (removeDlg.isYesButtonPressed())
		{
			app.setServerInfo("","","");
			repaint();
		}			
	}

	
	public void doConfigureServer()
	{
		if(!reSignIn())
			return;
		inConfigServer = true;
		try
		{
			ConfigInfo previousServerInfo = app.getConfigInfo();
			UiConfigServerDlg serverInfoDlg = new UiConfigServerDlg(this, previousServerInfo);
			if(!serverInfoDlg.getResult())
				return;		
			String serverIPAddress = serverInfoDlg.getServerIPAddress();
			String serverPublicKey = serverInfoDlg.getServerPublicKey();
			ClientSideNetworkGateway gateway = ClientSideNetworkGateway.buildGateway(serverIPAddress, serverPublicKey);
			
			if(!app.isSSLServerAvailable(gateway))
			{
				notifyDlg("ServerSSLNotResponding");
				return;
			}
		
			String newServerCompliance = getServerCompliance(gateway);
			if(!confirmServerCompliance("ServerComplianceDescription", newServerCompliance))
			{
				//TODO:The following line shouldn't be necessary but without it, the trustmanager 
				//will reject the old server, we don't know why.
				ClientSideNetworkGateway.buildGateway(previousServerInfo.getServerName(), previousServerInfo.getServerPublicKey()); 
				notifyDlg("UserRejectedServerCompliance");
				if(serverIPAddress.equals(previousServerInfo.getServerName()) &&
				   serverPublicKey.equals(previousServerInfo.getServerPublicKey()))
					app.setServerInfo("","","");
				return;
			}
			getStore().clearOnServerLists();
			boolean magicAccepted = false;
			app.setServerInfo(serverIPAddress, serverPublicKey, newServerCompliance);
			if(app.requestServerUploadRights(""))
				magicAccepted = true;
			else
			{
				while (true)
				{
					String magicWord = getStringInput("servermagicword", "", "", "");
					if(magicWord == null)
						break;
					if(app.requestServerUploadRights(magicWord))
					{
						magicAccepted = true;
						break;
					}
					notifyDlg("magicwordrejected");
				}
			}
		
			String title = getLocalization().getWindowTitle("ServerSelectionResults");
			String serverSelected = getLocalization().getFieldLabel("ServerSelectionResults") + serverIPAddress;
			String uploadGranted = "";
			if(magicAccepted)
				uploadGranted = getLocalization().getFieldLabel("ServerAcceptsUploads");
			else
				uploadGranted = getLocalization().getFieldLabel("ServerDeclinesUploads");
		
			String ok = getLocalization().getButtonLabel("ok");
			String[] contents = {serverSelected, uploadGranted};
			String[] buttons = {ok};
		
			new UiNotifyDlg(getCurrentActiveFrame(), title, contents, buttons);
			if(magicAccepted)
				requestToUpdateContactInfoOnServerAndSaveInfo();
			
			backgroundUploadTimerTask.forceRecheckOfUidsOnServer();
			getStore().clearOnServerLists();
			repaint();
		}
		finally
		{
			inConfigServer = false;
		}
	}
	
	private String getServerCompliance(ClientSideNetworkGateway gateway)
	{
		try
		{
			return app.getServerCompliance(gateway);
		}
		catch (Exception e)
		{
			return "";
		}
	}

	boolean confirmServerCompliance(String descriptionTag, String newServerCompliance)
	{
		if(newServerCompliance.equals(""))
			return confirmDlg("ServerComplianceFailed");
			
		UiShowScrollableTextDlg dlg = new UiShowScrollableTextDlg(this, "ServerCompliance", "ServerComplianceAccept", "ServerComplianceReject", descriptionTag, newServerCompliance, null);
		return dlg.getResult();
	}

	private void requestToUpdateContactInfoOnServerAndSaveInfo()
	{
		saveConfigInfo();
		
		ConfigInfo configInfo = app.getConfigInfo();
		if(!configInfo.isServerConfigured())
			return;
		
		boolean sendInfo = confirmDlg("RequestToSendContactInfoToServer");
		configInfo.setSendContactInfoToServer(sendInfo);
		saveConfigInfo();
	}
	
	private void saveConfigInfo()
	{
		try
		{
			app.saveConfigInfo();
		}
		catch (MartusApp.SaveConfigInfoException e)
		{
			notifyDlg("ErrorSavingConfig");
		}
	}

	public boolean isServerConfigured()
	{
		return app.getConfigInfo().isServerConfigured();
	}

	public boolean reSignIn()
	{
		int result = signIn(UiSigninDlg.SECURITY_VALIDATE);
		if(!app.isSignedIn())
			exitWithoutSavingState();
		if(result == UiSigninDlg.SIGN_IN)
			return true;
		return false;
	}


	public void doChangeUserNamePassword()
	{
		if(!reSignIn())
			return;
		if(!getAndSaveUserNamePassword(app.getCurrentKeyPairFile()))
			return;
		try
		{
			app.getConfigInfo().setBackedUpKeypairEncrypted(false);
			app.saveConfigInfo();
		}
		catch (SaveConfigInfoException e)
		{
			notifyDlg("ErrorSavingConfig");
			e.printStackTrace();
		}
		notifyDlg("RewriteKeyPairSaved");
		askToBackupKeyPairEncryptedSingleFile();
	}

	boolean getAndSaveUserNamePassword(File keyPairFile) 
	{
		String originalUserName = app.getUserName();
		UiCreateNewAccountProcess newUserInfo = new UiCreateNewAccountProcess(this, originalUserName);
		if(!newUserInfo.isDataValid())
			return false;
		File accountsHashOfUserNameFile = app.getUserNameHashFile(keyPairFile.getParentFile());
		accountsHashOfUserNameFile.delete();
		return saveKeyPairFile(keyPairFile, newUserInfo.getUserName(), newUserInfo.getPassword());
	}

	public boolean saveKeyPairFile(File keyPairFile, String userName, char[] userPassword)
	{
		try
		{
			app.writeKeyPairFileWithBackup(keyPairFile, userName, userPassword);
			app.attemptSignIn(userName, userPassword);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			notifyDlg(currentActiveFrame, "RewriteKeyPairFailed", new OneEntryMap("#S#", keyPairFile.getAbsolutePath()));
			return false;
			//TODO eventually try to restore keypair from backup.
		}
		return true;
	}

	public void updateBulletinDetails(File defaultFile)
	{
		ConfigInfo info = app.getConfigInfo();
		File details = app.getBulletinDefaultDetailsFile();
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
				app.saveConfigInfo();
			}
			catch (MartusApp.SaveConfigInfoException e)
			{
				System.out.println("doContactInfo: Unable to Save ConfigInfo" + e);
			}
		}
	}

	public void doRetrieveMySealedBulletins()
	{
		String dlgTitleTag = "RetrieveMySealedBulletins";
		String summariesProgressTag = "RetrieveMySealedBulletinSummaries";
		String retrieverProgressTag = "RetrieveMySealedBulletinProgress";
		String folderName = app.getNameOfFolderRetrievedSealed();

		RetrieveTableModel model = new RetrieveMyTableModel(app, getLocalization());
		retrieveBulletins(model, folderName, dlgTitleTag, summariesProgressTag, retrieverProgressTag);
	}

	public void doRetrieveMyDraftBulletins()
	{
		String dlgTitleTag = "RetrieveMyDraftBulletins";
		String summariesProgressTag = "RetrieveMyDraftBulletinSummaries";
		String retrieverProgressTag = "RetrieveMyDraftBulletinProgress";
		String folderName = app.getNameOfFolderRetrievedDraft();

		RetrieveTableModel model = new RetrieveMyDraftsTableModel(app, getLocalization());
		retrieveBulletins(model, folderName, dlgTitleTag, summariesProgressTag, retrieverProgressTag);
	}

	public void doRetrieveHQBulletins()
	{
		String dlgTitleTag = "RetrieveHQSealedBulletins";
		String summariesProgressTag = "RetrieveHQSealedBulletinSummaries";
		String retrieverProgressTag = "RetrieveHQSealedBulletinProgress";
		String folderName = app.getNameOfFolderRetrievedFieldOfficeSealed();

		RetrieveTableModel model = new RetrieveHQTableModel(app, getLocalization());
		retrieveBulletins(model, folderName, dlgTitleTag, summariesProgressTag, retrieverProgressTag);
	}

	public void doRetrieveHQDraftsBulletins()
	{
		String dlgTitleTag = "RetrieveHQDraftBulletins";
		String summariesProgressTag = "RetrieveHQDraftBulletinSummaries";
		String retrieverProgressTag = "RetrieveHQDraftBulletinProgress";
		String folderName = app.getNameOfFolderRetrievedFieldOfficeDraft();

		RetrieveTableModel model = new RetrieveHQDraftsTableModel(app, getLocalization());
		retrieveBulletins(model, folderName, dlgTitleTag, summariesProgressTag, retrieverProgressTag);
	}

	public void doDeleteServerDraftBulletins()
	{
		String dlgTitleTag = "DeleteMyDraftsFromServer";
		String summariesProgressTag = "RetrieveMyDraftBulletinSummaries";

		RetrieveTableModel model = new DeleteMyServerDraftsTableModel(app, getLocalization());
		deleteServerDrafts(model, dlgTitleTag, summariesProgressTag);
	}

	private void retrieveBulletins(RetrieveTableModel model, String folderName,
						String dlgTitleTag, String summariesProgressTag, String retrieverProgressTag)
	{
		try
		{
			UiServerSummariesDlg summariesDlg = new UiServerSummariesRetrieveDlg(this, model, dlgTitleTag);
			Vector uidList = displaySummariesDialog(model, dlgTitleTag, summariesProgressTag, summariesDlg);
			if(uidList == null)
				return;
			
			BulletinFolder retrievedFolder = app.createOrFindFolder(folderName);
			app.getStore().saveFolders();

			UiProgressRetrieveBulletinsDlg progressDlg = new UiProgressRetrieveBulletinsDlg(this, retrieverProgressTag);
			Retriever retriever = new Retriever(app, progressDlg);
			retriever.retrieveBulletins(uidList, retrievedFolder);
			retriever.progressDlg.setVisible(true);
			if(progressDlg.shouldExit())
				notifyDlg("RetrieveCanceled");
			else
			{
				String result = retriever.getResult();
				if(!result.equals(NetworkInterfaceConstants.OK))
					notifyDlg(this, "retrievefailed", dlgTitleTag);
				else
					notifyDlg(this, "retrieveworked", dlgTitleTag);
			}

			folderTreeContentsHaveChanged();
			folders.folderContentsHaveChanged(retrievedFolder);
			folders.selectFolder(folderName);
		}
		catch(ServerErrorException e)
		{
			notifyDlg("ServerError");
			return;
		}
	}

	private void deleteServerDrafts(RetrieveTableModel model,
						String dlgTitleTag, String summariesProgressTag)
	{

		try
		{
			UiServerSummariesDlg summariesDlg = new UiServerSummariesDeleteDlg(this, model, dlgTitleTag);
			Vector uidList = displaySummariesDialog(model, dlgTitleTag, summariesProgressTag, summariesDlg);
			if(uidList == null)
				return;

			setWaitingCursor();
			try
			{
				String result = app.deleteServerDraftBulletins(uidList);
				if(!result.equals(NetworkInterfaceConstants.OK))
				{
					notifyDlg("DeleteServerDraftsFailed");
					return;
				}

				notifyDlg("DeleteServerDraftsWorked");
			}
			finally
			{
				resetCursor();
			}
		}
		catch (MartusCrypto.MartusSignatureException e)
		{
			notifyDlg("UnexpectedError");
			return;
		}
		catch (Packet.WrongAccountException e)
		{
			notifyDlg("UnexpectedError");
			return;
		}
		catch(ServerErrorException e)
		{
			notifyDlg("ServerError");
			return;
		}
	}


	private Vector displaySummariesDialog(RetrieveTableModel model, String dlgTitleTag, String summariesProgressTag, UiServerSummariesDlg summariesDlg) throws ServerErrorException
	{
		if(!retrieveSummaries(model, dlgTitleTag, summariesProgressTag))
			return null;
	
		summariesDlg.initialize();
		if(!summariesDlg.getResult())
			return null;
		
		return summariesDlg.getUniversalIdList();
	}

	private boolean retrieveSummaries(RetrieveTableModel model, String dlgTitleTag, String summariesProgressTag) throws ServerErrorException
	{
		if(!app.isSSLServerAvailable())
		{
			notifyDlg(this, "retrievenoserver", dlgTitleTag);
			return false;
		}
		UiProgressRetrieveSummariesDlg progressDlg = new UiProgressRetrieveSummariesDlg(this, summariesProgressTag);
		model.initialize(progressDlg);
		if(progressDlg.shouldExit())
			return false;
		try
		{
			model.checkIfErrorOccurred();
		}
		catch (ServerErrorException e)
		{
			notifyDlg(this, "RetrievedOnlySomeSummaries", dlgTitleTag);
		}
		return true;
	}

	public void doExportMyPublicKey()
	{
		try
		{
			File export;
			do
			{
				String fileName = getStringInput("ExportMyPublicKey", "NameOfExportedFile", "", "");
				if(fileName == null)
					return;
				export = app.getPublicInfoFile(fileName);
				if(export.exists())
				{
					if(confirmDlg("OverWriteExistingFile"))
						export.delete();
				}
			}while(export.exists());
			
			app.exportPublicInfo(export);
			String title = getLocalization().getWindowTitle("notifyExportMyPublicKey");
			String msg = getLocalization().getFieldLabel("notifyExportMyPublicKeycause");
			String ok = getLocalization().getButtonLabel("ok");
			String[] contents = {msg, export.getCanonicalPath()};
			String[] buttons = {ok};
			new UiNotifyDlg(getCurrentActiveFrame(), title, contents, buttons);
		}
		catch(Exception e)
		{
			System.out.println("UiMainWindow.doExportMyPublicKey :" + e);
		}
	}

	public void askToBackupKeyPairEncryptedSingleFile()
	{
		if(confirmDlg("BackupKeyPairInformation"))
			doBackupKeyPairToSingleEncryptedFile();
	}

	public void askToBackupKeyPareToSecretShareFiles()
	{
		if(confirmDlg(this,"BackupKeyPairSecretShare", UiBackupRecoverSharedKeyPair.getTokenReplacement()))
		{
			UiBackupRecoverSharedKeyPair backup = new UiBackupRecoverSharedKeyPair(this);
			backup.backupKeyPairToMultipleUnencryptedFiles();
		}
	}

	public void doBackupKeyPairToSingleEncryptedFile() 
	{
		File keypairFile = app.getCurrentKeyPairFile();
		if(keypairFile.length() > MAX_KEYPAIRFILE_SIZE)
		{
			System.out.println("keypair file too large!");
			notifyDlg("ErrorBackingupKeyPair");
			return;
		}
		
		String windowTitle = getLocalization().getWindowTitle("saveBackupKeyPair");
		UiFileChooser.FileDialogResults results = UiFileChooser.displayFileSaveDialog(this, windowTitle, MartusApp.KEYPAIR_FILENAME);
		
		if (results.wasCancelChoosen())
			return;
		File newBackupFile = results.getFileChoosen();
		if(newBackupFile.exists())
			if(!confirmDlg("OverWriteExistingFile"))
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
				app.getConfigInfo().setBackedUpKeypairEncrypted(true);
				app.saveConfigInfo();
			}
			else
			{
				notifyDlg("ErrorBackingupKeyPair");
			}
		}
		catch (FileNotFoundException fnfe)
		{
			notifyDlg("ErrorBackingupKeyPair");
		}
		catch (IOException ioe)
		{
			System.out.println(ioe.getMessage());
			notifyDlg("ErrorBackingupKeyPair");
		}
		catch (SaveConfigInfoException e)
		{
			e.printStackTrace();
			notifyDlg("ErrorSavingConfig");
		}
	}
	
	public void displayScrollableMessage(String titleTag, String message, String okButtonTag, Map tokenReplacement) 
	{
		new UiShowScrollableTextDlg(this, titleTag, okButtonTag, MtfAwareLocalization.UNUSED_TAG, MtfAwareLocalization.UNUSED_TAG, message, tokenReplacement, null);
	}
	
	public void doConfigureHQs()
	{
		if(!reSignIn())
			return;
		new UiConfigureHQs(this);
	}
	
	public void setAndSaveHQKeysInConfigInfo(HQKeys allHQKeys, HQKeys defaultHQKeys)
	{
		try
		{
			app.setAndSaveHQKeys(allHQKeys, defaultHQKeys);
		}
		catch(MartusApp.SaveConfigInfoException e)
		{
			notifyDlg("ErrorSavingConfig");
		}
		
	}


	void initializeViews()
	{
		getContentPane().removeAll();
		getContentPane().setComponentOrientation(UiLanguageDirection.getComponentOrientation());
		setTitle(getLocalization().getWindowTitle("main"));

		preview = new UiBulletinPreviewPane(this);
		table = new UiBulletinTablePane(this);
		folders = new UiFolderTreePane(this);
		getContentPane().add(createTopStuff(), BorderLayout.NORTH);

		previewSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, table, preview);
		previewSplitter.setDividerLocation(uiState.getCurrentPreviewSplitterPosition());

		if(LanguageOptions.isRightToLeftLanguage())
			folderSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, previewSplitter, folders);
		else
			folderSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, folders, previewSplitter);

		Dimension screenSize = Utilities.getViewableScreenSize();
		Dimension appDimension = uiState.getCurrentAppDimension();
		Point appPosition = uiState.getCurrentAppPosition();
		boolean showMaximized = false;
		if(Utilities.isValidScreenPosition(screenSize, appDimension, appPosition))
		{
			setLocation(appPosition);
			setSize(appDimension);
			if(uiState.isCurrentAppMaximized())
				showMaximized = true;
		}
		else
			showMaximized = true;
		if(showMaximized)
		{
			setSize(screenSize.width - 50 , screenSize.height - 50);
			Utilities.maximizeWindow(this);
		}

		uiState.setCurrentAppDimension(getSize());
		folderSplitter.setDividerLocation(uiState.getCurrentFolderSplitterPosition());

		getContentPane().add(folderSplitter);
		statusBar = new UiStatusBar(getLocalization());		
		checkServerStatus();	
		getContentPane().add(statusBar, BorderLayout.SOUTH ); 
	}
	
	public void checkServerStatus()
	{		
		if (!app.isServerConfigured())
		{	
			setStatusMessageTag("ServerNotConfiguredProgressMessage");
			return;
		}
	
		ClientSideNetworkGateway gateway = getApp().getCurrentNetworkInterfaceGateway();		
		if(app.isSSLServerAvailable(gateway))
			setStatusMessageTag("StatusReady");	
		else
			setStatusMessageTag("NoServerAvailableProgressMessage");			
	}
	
	public void setStatusMessageTag(String tag)
	{
		UiProgressMeter r = statusBar.getBackgroundProgressMeter();	
		r.setStatusMessage(tag);
		r.hideProgressMeter();
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
					signinDlg = new UiInitialSigninDlg(getLocalization(), getCurrentUiState(), getCurrentActiveFrame(), mode, userName, userPassword);
				else
					signinDlg = new UiSigninDlg(getLocalization(), getCurrentUiState(), getCurrentActiveFrame(), mode, userName, userPassword);
				userChoice = signinDlg.getUserChoice();
				userName = signinDlg.getName();
				userPassword = signinDlg.getPassword();
			}
			if (userChoice != UiSigninDlg.SIGN_IN)
				return userChoice;
			try
			{
				if(mode == UiSigninDlg.INITIAL)
				{	
					app.attemptSignIn(userName, userPassword);
				}
				else
				{	
					app.attemptReSignIn(userName, userPassword);
					getCurrentActiveFrame().setState(NORMAL);
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

	private boolean createAccount()
	{
		notifyDlg("WelcomeToMartus");
		UiCreateNewAccountProcess newUserInfo = new UiCreateNewAccountProcess(this, "");
		if(!newUserInfo.isDataValid())
			return false;
		String userName = newUserInfo.getUserName();
		char[] userPassword = newUserInfo.getPassword();

		UiModelessBusyDlg waitingForKeyPair = new UiModelessBusyDlg(getLocalization().getFieldLabel("waitingForKeyPairGeneration"));
		try
		{
			app.createAccount(userName ,userPassword);
		}
		catch(Exception e)
		{
			waitingForKeyPair.endDialog();
			notifyDlg("CreateAccountFailed");
			return false;
		}
		waitingForKeyPair.endDialog();
		return true;
	}

	private boolean doUploadReminderOnExit()
	{
		boolean dontExitApplication = false;
		if(!app.isSealedOutboxEmpty())
		{
			if(confirmDlg("UploadReminder"))
				app.resetLastUploadRemindedTime();
			else
				dontExitApplication = true;
		}
		else if(!app.isDraftOutboxEmpty())
		{
			if(!confirmDlg("DraftUploadReminder"))
				dontExitApplication = true;
		}
		return dontExitApplication;
	}

	public void exitNormally()
	{
		if(createdNewAccount)
			askAndBackupKeypairIfRequired();
		if(doUploadReminderOnExit())
			return;
		saveState();
		getStore().prepareToExitNormally();
		exitWithoutSavingState();
	}

	public void exitWithoutSavingState()
	{
		getStore().prepareToExitWithoutSavingState();
		System.exit(0);
	}

	public void createBulletin()
	{
		Bulletin b = app.createBulletin();
		modifyBulletin(b);
	}

	public boolean modifyBulletin(Bulletin b)
	{
		getCurrentUiState().setModifyingBulletin(true);
		setEnabled(false);
		UiBulletinModifyDlg dlg = new UiBulletinModifyDlg(b, this);
		setCurrentActiveFrame(dlg);
		setVisible(false);
		dlg.setVisible(true);
		return dlg.wasBulletinSaved();
	}

	public void doneModifyingBulletin()
	{
		getCurrentUiState().setModifyingBulletin(false);
		setEnabled(true);
		setVisible(true);
		setCurrentActiveFrame(this);
	}

	public void doExportFolder()
	{
	
		BulletinFolder selectedFolder = folders.getSelectedFolder();
		int bulletinCount = selectedFolder.getBulletinCount();
		if(bulletinCount == 0)
		{
			notifyDlg("ExportFolderEmpty");
			return;
		}
		Vector bulletins = new Vector();
		for (int i = 0; i < bulletinCount; ++i)
		{
			bulletins.add(selectedFolder.getBulletinSorted(i));
		}
		String defaultFileName = MartusUtilities.createValidFileName(selectedFolder.getLocalizedName(localization));
		new UiExportBulletinsDlg(this, bulletins, defaultFileName);
	}

	public void doExportBulletins()
	{
		Vector bulletins = getSelectedBulletins("ExportZeroBulletins");
		if(bulletins == null)
			return;
		String defaultFileName = localization.getFieldLabel("ExportedBulletins");
		if(bulletins.size()==1)
			defaultFileName = ((Bulletin)bulletins.get(0)).toFileName();
		new UiExportBulletinsDlg(this, bulletins, defaultFileName);
	}
	
	public Vector getSelectedBulletins(String tagZeroBulletinsSelected)
	{
		UniversalId[] uids = table.getSelectedBulletinUids();
		if(uids.length == 0)
		{
			notifyDlg(tagZeroBulletinsSelected);
			return null;
		}
		return getBulletins(uids);
	}
	
	Vector getBulletins(UniversalId[] uids)
	{
		Vector bulletins = new Vector();
		for (int i = 0; i < uids.length; i++)
		{
			UniversalId uid = uids[i];
			Bulletin b = getStore().getBulletinRevision(uid);
			bulletins.add(b);
		}
		return bulletins;
	}
	
	
	public boolean getBulletinsAlwaysPrivate()
	{
		return app.getConfigInfo().shouldForceBulletinsAllPrivate();
	}

	public void setBulletinsAlwaysPrivate(boolean newAllPrivateState)
	{
		app.getConfigInfo().setForceBulletinsAllPrivate(newAllPrivateState);
		try
		{
			app.saveConfigInfo();
		}
		catch (SaveConfigInfoException e)
		{
			notifyDlg("ErrorSavingConfig");
		}
		
	}
	
	public boolean isAnyBulletinSelected()
	{
		return (table.getSelectedBulletinUids().length > 0);
	}

	public boolean isOnlyOneBulletinSelected()
	{
		return (table.getSingleSelectedBulletin() != null);
	}
	
	static public String getDisplayVersionInfo(UiLocalization localization)
	{
		String versionInfo = UiConstants.programName;
		versionInfo += " " + localization.getFieldLabel("aboutDlgVersionInfo");
		versionInfo += " " + UiConstants.versionLabel;
		return versionInfo;
	}
	
	
	class WindowEventHandler extends WindowAdapter
	{
		public void windowClosing(WindowEvent event)
		{
			exitNormally();
		}
	}

	class TimeoutTimerTask extends TimerTask
	{
		public TimeoutTimerTask()
		{
		}

		public void run()
		{
			try
			{
				if(hasTimedOut())
				{
					getStore().prepareToExitNormally();
					System.out.println("Inactive");
					ThreadedSignin signin = new ThreadedSignin();
					SwingUtilities.invokeAndWait(signin);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		boolean hasTimedOut()
		{
			if(inactivityDetector.secondsSinceLastActivity() > timeoutInXSeconds)
				return true;

			return false;
		}

		class ThreadedSignin implements Runnable
		{
			public void run()
			{
				getCurrentActiveFrame().setState(ICONIFIED);
				if(signIn(UiSigninDlg.TIMED_OUT) != UiSigninDlg.SIGN_IN)
					exitWithoutSavingState();
				getStore().loadFieldSpecCache();
				getCurrentActiveFrame().setState(NORMAL);
//cml			initializeViews();
			}
		}
	}

	class UploadErrorChecker extends AbstractAction
	{
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
		return uiState;
	}
	
	static public Image getMartusIconImage()
	{
		URL imageURL = UiMainWindow.class.getResource("Martus.png");
		if(imageURL == null)
			return null;
		ImageIcon imageicon = new ImageIcon(imageURL);
		if(imageicon == null)
			return null;
		
		return imageicon.getImage();
	}
	
	static public void updateIcon(JFrame window)
	{
		Image image = getMartusIconImage();
		if(image != null)
			window.setIconImage(image);
	}

	public void setCurrentActiveFrame(JFrame currentActiveFrame)
	{
		this.currentActiveFrame = currentActiveFrame;
	}

	public JFrame getCurrentActiveFrame()
	{
		return currentActiveFrame;
	}


	
	private MartusApp app;
	private CurrentUiState uiState;
	private UiBulletinPreviewPane preview;
	private JSplitPane previewSplitter;
	private JSplitPane folderSplitter;
	private UiBulletinTablePane table;
	private UiFolderTreePane folders;
	private java.util.Timer uploader;
	private java.util.Timer timeoutChecker;
	private javax.swing.Timer errorChecker;
	String uploadResult;
	UiInactivityDetector inactivityDetector;

	private UiMenuBar menuBar;
	private UiToolBar toolBar;
	UiStatusBar statusBar;
	MartusLocalization localization;

	private JFrame currentActiveFrame;
	boolean inConfigServer;
	boolean preparingToExitMartus;
	private FileLock lockToPreventTwoInstances; 
	private FileOutputStream lockStream;
	int timeoutInXSeconds;
	private static final int TIMEOUT_SECONDS = (10 * 60);
	private static final int TESTING_TIMEOUT_60_SECONDS = 60;

	private static final int MAX_KEYPAIRFILE_SIZE = 32000;
	private static final int BACKGROUND_UPLOAD_CHECK_MILLIS = 5*1000;
	private static final int BACKGROUND_TIMEOUT_CHECK_EVERY_X_MILLIS = 5*1000;
	private boolean mainWindowInitalizing;
	private boolean createdNewAccount;
	private boolean justRecovered;
	private BackgroundUploadTimerTask backgroundUploadTimerTask;
	private Stack cursorStack;
}
