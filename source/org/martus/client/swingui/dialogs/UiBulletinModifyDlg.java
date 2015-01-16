/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
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

package org.martus.client.swingui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.BulletinLanguageChangeListener;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.TopLevelWindowInterface;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.UiSession;
import org.martus.client.swingui.WindowObscurer;
import org.martus.client.swingui.bulletincomponent.UiBulletinComponentEditorSection;
import org.martus.client.swingui.bulletincomponent.UiBulletinComponentInterface;
import org.martus.client.swingui.bulletincomponent.UiBulletinEditor;
import org.martus.client.swingui.fields.UiDateEditor;
import org.martus.client.swingui.jfx.generic.FxInSwingStage;
import org.martus.client.swingui.jfx.generic.FxRunner;
import org.martus.client.swingui.jfx.landing.bulletins.FxBulletinEditorShellController;
import org.martus.clientside.UiLocalization;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.Bulletin.BulletinState;
import org.martus.common.fieldspec.DateRangeInvertedException;
import org.martus.common.fieldspec.DateTooEarlyException;
import org.martus.common.fieldspec.DateTooLateException;
import org.martus.common.fieldspec.RequiredFieldIsBlankException;
import org.martus.swing.UiButton;
import org.martus.swing.UiScrollPane;
import org.martus.swing.Utilities;

abstract public class UiBulletinModifyDlg implements TopLevelWindowInterface
{
	public UiBulletinModifyDlg(Bulletin b, UiMainWindow observerToUse) throws Exception
	{
		setBulletin(b);
		observer = observerToUse;
		
		ClientBulletinStore store = observerToUse.getApp().getStore();
		Property<String> currentTemplateNameProperty = store.getCurrentFormTemplateNameProperty();
		currentTemplateNameProperty.addListener(new TemplateChangeHandler(observerToUse));

		realFrame = new JFrame();
		UiMainWindow.updateIcon(getSwingFrame());
		getSwingFrame().setTitle(getLocalization().getWindowTitle("create"));
		
		if(UiSession.isJavaFx())
		{
			FxBulletinEditorShellController bulletinEditorShellController = new FxBulletinEditorShellController(observerToUse, this);

			FxInSwingStage bulletinEditorStage = FxRunner.createAndActivateEmbeddedStage(observerToUse, getSwingFrame(), bulletinEditorShellController);
			setView(bulletinEditorShellController);
			Platform.runLater(() -> safelyPopulateView());
			getSwingFrame().getContentPane().add(bulletinEditorStage.getPanel(), BorderLayout.CENTER);
		}
		else
		{
			setView(new UiBulletinEditor(getMainWindow()));
			getView().copyDataFromBulletin(getBulletin());
			getView().setLanguageChangeListener(new LanguageChangeHandler());

			UiButton send = new UiButton(getLocalization().getButtonLabel("send"));
			send.addActionListener(new SendHandler());
			UiButton draft = new UiButton(getLocalization().getButtonLabel("savedraft"));
			draft.addActionListener(new SaveHandler());
			UiButton cancel = new UiButton(getLocalization().getButtonLabel(EnglishCommonStrings.CANCEL));
			cancel.addActionListener(new CancelHandler());

			addScrollerView();

			Box box = Box.createHorizontalBox();
			Component buttons[] = {send, draft, cancel, Box.createHorizontalGlue()};
			Utilities.addComponentsRespectingOrientation(box, buttons);
			getSwingFrame().getContentPane().add(box, BorderLayout.SOUTH);
		}


		getSwingFrame().setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		getSwingFrame().addWindowListener(new WindowEventHandler());

		Dimension screenSize = Utilities.getViewableScreenSize();
		Dimension editorDimension = observerToUse.getBulletinEditorDimension();
		Point editorPosition = observerToUse.getBulletinEditorPosition();
		boolean showMaximized = false;
		if(Utilities.isValidScreenPosition(screenSize, editorDimension, editorPosition))
		{
			getSwingFrame().setLocation(editorPosition);
			getSwingFrame().setSize(editorDimension);
			if(observerToUse.isBulletinEditorMaximized())
				showMaximized = true;
		}
		else
			showMaximized = true;
		if(showMaximized)
		{
			getSwingFrame().setSize(screenSize.width - 50, screenSize.height - 50);
			Utilities.maximizeWindow(getSwingFrame());
		}
		
		if(!UiSession.isJavaFx())
			getView().scrollToTop();
		
		getSwingFrame().setGlassPane(new WindowObscurer());
		
	}

	public UiLocalization getLocalization()
	{
		UiLocalization localization = getMainWindow().getLocalization();
		return localization;
	}
	
	@Override
	public void repaint()
	{
		getSwingFrame().repaint();
	}
	
	protected UiMainWindow getMainWindow()
	{
		return observer;
	}
	
	class WindowEventHandler extends WindowAdapter
	{
		public void windowClosing(WindowEvent event)
		{
			try
			{
				closeWindowIfUserConfirms();
			}
			catch (Exception e)
			{
				unexpectedErrorDlg(e);
			}
		}
	}
	
	class LanguageChangeHandler implements BulletinLanguageChangeListener
	{
		@Override
		public void bulletinLanguageHasChanged(String newLanguageCode)
		{
			//TODO add this back when its working correctly
			/*		if(observer.getLocalization().doesLanguageRequirePadding(newLanguage))
						LanguageOptions.setLanguagePaddingRequired();
					else
						LanguageOptions.setLanguagePaddingNotRequired();
					getContentPane().remove(scroller);
					addScrollerView();
			*/
		}
	}
	
	public JFrame getSwingFrame()
	{
		return realFrame;
	}
	
	public void dispose()
	{
		getSwingFrame().dispose();
	}
	
	public void setVisible(boolean newVisibility)
	{
		getSwingFrame().setVisible(newVisibility);
	}
	
	protected void unexpectedErrorDlg(Exception e)
	{
		observer.unexpectedErrorDlg(e);
	}

	private void safelyPopulateView()
	{
		try
		{
			getView().copyDataFromBulletin(getBulletin());
			getView().setLanguageChangeListener(new LanguageChangeHandler());
			getView().scrollToTop();
		} 
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	class TemplateChangeHandler implements ChangeListener<String>
	{
		public TemplateChangeHandler(UiMainWindow mainWindowToUse)
		{
			mainWindow = mainWindowToUse;
		}
		
		@Override
		public void changed(ObservableValue<? extends String> currentTemplateName, String oldValue, String newValue)
		{
			try
			{
				ClientBulletinStore store = mainWindow.getApp().getStore();
				Bulletin clonedBulletin = createClonedBulletinUsingCurrentTemplate(store);
				SwingUtilities.invokeLater(() -> showBulletin(clonedBulletin));
			} 
			catch (Exception e)
			{
				
			}
		}

		private UiMainWindow mainWindow;
	}
	
	public Bulletin createClonedBulletinUsingCurrentTemplate(ClientBulletinStore store) throws Exception
	{
		Bulletin bulletinWithOldTemplateButLatestData = getBulletin();
		getView().copyDataToBulletin(bulletinWithOldTemplateButLatestData);
		Bulletin clonedBulletin = store.createNewDraftWithCurrentTemplateButIdAndDataAndHistoryFrom(bulletinWithOldTemplateButLatestData);
		return clonedBulletin;
	}
	
	public void setBulletin(Bulletin bulletin)
	{
		this.bulletin = bulletin;
	}

	protected Bulletin getBulletin()
	{
		return bulletin;
	}

	protected void showBulletin(Bulletin bulletinToShow)
	{
		setBulletin(bulletinToShow);
		try
		{
			getView().copyDataFromBulletin(getBulletin());
			getView().scrollToTop();
		} 
		catch (Exception e)
		{
			observer.unexpectedErrorDlg(e);
		}
	}
	
	private void addScrollerView() 
	{
		scroller = new UiScrollPane();
		scroller.getVerticalScrollBar().setFocusable(false);
		scroller.getViewport().add(getView().getComponent());
		scroller.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		getSwingFrame().getContentPane().setLayout(new BorderLayout());
		getSwingFrame().getContentPane().add(scroller, BorderLayout.CENTER);
		getSwingFrame().getContentPane().invalidate();
		getSwingFrame().getContentPane().doLayout();
	}
	
	class CancelHandler implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent event)
		{
			try
			{
				closeWindowIfUserConfirms();
			} 
			catch (Exception e)
			{
				unexpectedErrorDlg(e);
			}
		}
		
	}

	class SaveHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{		
			try
			{
				if(!validateData())
					return;
	
				saveBulletin(false, BulletinState.STATE_SAVE);
			}
			catch (Exception e) 
			{
				getMainWindow().unexpectedErrorDlg(e);
			}
		}
	}

	class SendHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{		
			try
			{
				if(!validateData())
					return;
	
				String tag = "send";
				if (!getMainWindow().confirmDlg(tag))
					return;
													
				saveBulletin(true, BulletinState.STATE_SHARED);
			}
			catch (Exception e) 
			{
				getMainWindow().unexpectedErrorDlg(e);
			}
		}
	}

	protected boolean validateData()
	{
		try
		{	
			getView().validateData();
			return true;
		}
		catch(UiDateEditor.DateFutureException e)
		{
			observer.messageDlg(getSwingFrame(),"ErrorDateInFuture", e.getlocalizedTag());
		}
		catch(DateRangeInvertedException e)
		{
			HashMap map = new HashMap();
			map.put("#FieldLabel#", e.getFieldLabel());
			observer.messageDlg("ErrorDateRangeInverted", "", map);
		}
		catch(DateTooEarlyException e)
		{
			HashMap map = new HashMap();
			map.put("#FieldLabel#", e.getFieldLabel());
			map.put("#MinimumDate#", observer.getLocalization().convertStoredDateToDisplay(e.getMinimumDate()));
			observer.messageDlg("ErrorDateTooEarly", "", map);
		}
		catch(DateTooLateException e)
		{
			HashMap map = new HashMap();
			map.put("#FieldLabel#", e.getFieldLabel());
			map.put("#MaximumDate#", observer.getLocalization().convertStoredDateToDisplay(e.getMaximumDate()));
			observer.messageDlg("ErrorDateTooLate", "", map);
		}
		catch(UiBulletinComponentEditorSection.AttachmentMissingException e)
		{
			observer.messageDlg(getSwingFrame(), "ErrorAttachmentMissing", e.getlocalizedTag());
		}
		catch(RequiredFieldIsBlankException e)
		{
			HashMap map = new HashMap();
			map.put("#FieldLabel#", e.getFieldLabel());
			observer.messageDlg("ErrorRequiredFieldBlank", "", map);
		}
		catch (Exception e) 
		{
			observer.unexpectedErrorDlg(e);
		}
		return false;
	}

	public void saveBulletin(boolean neverDeleteFromServer, BulletinState bulletinState)
	{
		Cursor originalCursor = getSwingFrame().getCursor();
		getSwingFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try 
		{
			MartusApp app = observer.getApp();
			ClientBulletinStore store = app.getStore();
			BulletinFolder outboxToUse = null;
			BulletinFolder draftOutbox = store.getFolderDraftOutbox();

			// NOTE: must copyDataToBulletin before setSealed or setDraft
			// NOTE: after copyDataToBulletin, should not allow user to cancel
			getView().copyDataToBulletin(getBulletin());
			getBulletin().changeState(bulletinState);
			
			if(neverDeleteFromServer)
			{
				store.removeBulletinFromFolder(draftOutbox, getBulletin());
				getBulletin().setImmutable();
				outboxToUse = store.getFolderSealedOutbox();
			}
			else
			{
				getBulletin().setMutable();
				outboxToUse = draftOutbox;
			}
			saveBulletinAndUpdateFolders(store, outboxToUse);
			wasBulletinSavedFlag = true;
			cleanupAndExit();
		} 
		catch (Exception e) 
		{
			observer.unexpectedErrorDlg(e);
		} 
		finally 
		{
			getSwingFrame().setCursor(originalCursor);
		}
	}

	private void saveBulletinAndUpdateFolders(ClientBulletinStore store, BulletinFolder outboxToUse) throws Exception
	{
		observer.getApp().saveBulletin(getBulletin(), outboxToUse);

		observer.folderContentsHaveChanged(store.getFolderSaved());
		observer.folderContentsHaveChanged(store.getFolderDiscarded());
		observer.selectBulletinInCurrentFolderIfExists(getBulletin().getUniversalId());
		observer.bulletinContentsHaveChanged(getBulletin());
	}

	public boolean wasBulletinSaved()
	{
		return wasBulletinSavedFlag;
	}


	public void cleanupAndExit()
	{
		observer.doneModifyingBulletin();
		saveEditorState(getSwingFrame().getSize(), getSwingFrame().getLocation());
		getSwingFrame().dispose();
	}

	public void saveEditorState(Dimension size, Point location)
	{
		boolean maximized = getSwingFrame().getExtendedState() == JFrame.MAXIMIZED_BOTH;
		observer.setBulletinEditorDimension(size);
		observer.setBulletinEditorPosition(location);
		observer.setBulletinEditorMaximized(maximized);
		observer.saveState();
	}

	protected void closeWindowIfUserConfirms() throws Exception
	{	
		boolean needConfirmation = getView().isBulletinModified();
		if(needConfirmation)
		{
			if(!observer.confirmDlg("CancelModifyBulletin"))
				return;
		}
			
		cleanupAndExit();
	}
	
	protected void setView(UiBulletinComponentInterface view)
	{
		this.view = view;
	}

	protected UiBulletinComponentInterface getView()
	{
		return view;
	}

	private JFrame realFrame;
	
	private Bulletin bulletin;
	private UiMainWindow observer;

	private UiBulletinComponentInterface view;
	private UiScrollPane scroller;
	
	private boolean wasBulletinSavedFlag;
}

