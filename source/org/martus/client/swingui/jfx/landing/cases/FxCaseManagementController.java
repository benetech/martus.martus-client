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
package org.martus.client.swingui.jfx.landing.cases;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionDoer;
import org.martus.client.swingui.filefilters.BulletinXmlFileFilter;
import org.martus.client.swingui.filefilters.MartusBulletinArchiveFileFilter;
import org.martus.client.swingui.jfx.generic.DialogWithCloseShellController;
import org.martus.client.swingui.jfx.generic.DialogWithNoButtonsShellController;
import org.martus.client.swingui.jfx.generic.DialogWithOkCancelShellController;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.landing.AbstractFxLandingContentController;
import org.martus.client.swingui.jfx.landing.FolderSelectionListener;
import org.martus.client.swingui.jfx.landing.FxLandingShellController;
import org.martus.client.swingui.jfx.landing.cases.FxFolderDeleteController.FolderDeletedListener;
import org.martus.client.swingui.jfx.landing.general.ManageServerSyncRecordsController;
import org.martus.client.swingui.jfx.landing.general.ManageTemplatesController;
import org.martus.client.tools.ImporterOfXmlFilesOfBulletins;
import org.martus.clientside.FormatFilter;
import org.martus.common.fieldspec.ChoiceItem;

public class FxCaseManagementController extends AbstractFxLandingContentController
{
	public FxCaseManagementController(UiMainWindow mainWindowToUse )
	{
		super(mainWindowToUse);
		
		listeners = new HashSet<FolderSelectionListener>();
	}

	@Override
	public void initializeMainContentPane()
	{
		updateCasesSelectDefaultCase();
		CaseListChangeListener caseListChangeListener = new CaseListChangeListener();
		casesListViewAll.getSelectionModel().selectedItemProperty().addListener(caseListChangeListener);
		casesListViewAll.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		casesTabPane.getSelectionModel().selectedItemProperty().addListener(new caseTabeListener());
		currentSelectedCase = currentCasesListView.getSelectionModel().selectedItemProperty();
		showTrashFolder.visibleProperty().bind(((FxLandingShellController)getShellController()).getShowTrashBinding());
	}

	@Override
	public String getFxmlLocation()
	{
		return LOCATION_CASE_MANAGEMENT_FXML;
	}
	
	private CaseListProvider getCurrentCaseListProvider()
	{
		return (CaseListProvider) currentCasesListView.getItems();
	}

	public CaseListProvider getAllCaseListProvider()
	{
		return (CaseListProvider) casesListViewAll.getItems();
	}

	public void addFolderSelectionListener(FolderSelectionListener listener)
	{
		listeners.add(listener);
	}
	
	protected void updateCasesSelectDefaultCase()
	{
		setCurrentlyViewedCaseList(tabCaseAll);
		casesTabPane.getSelectionModel().select(tabCaseAll);
		updateCases(DEFAULT_SELECTED_CASE_NAME);
	}
	
	protected void setCurrentlyViewedCaseList(Tab currentlyViewedCaseTab)
	{
		currentCasesListView = casesListViewAll;
		updateCaseList();
	}

	protected void updateCases(String caseNameToSelect)
	{
		String foldersLabel = FxFolderSettingsController.getCurrentFoldersHeading(getApp().getConfigInfo(), getLocalization());
		updateFolderLabelName(foldersLabel);

		CaseListProvider caseListProviderAll = new CaseListProvider();

		Vector visibleFolders = getApp().getStore().getAllVisibleFolders();
		MartusLocalization localization = getLocalization();
		for(Iterator f = visibleFolders.iterator(); f.hasNext();)
		{
			BulletinFolder folder = (BulletinFolder) f.next();
			if(shouldNotShowFolder(folder))
				continue;
			if(folder.getName().equals(caseNameToSelect))
				updateButtons(folder);
			CaseListItem caseList = new CaseListItem(folder, localization);
			caseListProviderAll.add(caseList);
		}
		casesListViewAll.setItems(caseListProviderAll);
		orderCases();
		selectCase(caseNameToSelect);
	}

	private boolean shouldNotShowFolder(BulletinFolder folder)
	{
		ClientBulletinStore store = getApp().getStore();
		String searchFolder = store.getSearchFolderName();
		if(folder.getName().equals(searchFolder))
			return true;
		BulletinFolder discarded = store.getFolderDiscarded();
		if(folder.equals(discarded))
			return true;
		return false;
	}
	
	@FXML
	public void onShowTrash(ActionEvent event)
	{
		currentCasesListView.getSelectionModel().clearSelection();
		
		BulletinFolder trashFolder = getApp().getStore().getFolderDiscarded();
		MartusLocalization localization = getLocalization();
		CaseListItem trashList = new CaseListItem(trashFolder, localization);
		CaseListProvider trashProvider = new CaseListProvider();
		trashProvider.add(trashList);
		currentCasesListView = new ListView<CaseListItem>();
		currentCasesListView.setItems(trashProvider);
		currentCasesListView.getSelectionModel().select(trashList);
		updateCaseList();
	}
	
	public void folderContentsHaveChanged()
	{
		updateCases(getCurrentBulletinFolder().getName());
	}
	

	protected void updateCaseList()
	{
		try
		{
			BulletinFolder folder = getCurrentBulletinFolder();
			if(folder == null)
				return;
			updateButtons(folder);
			
			listeners.forEach(listener -> listener.folderWasSelected(folder));
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	private BulletinFolder getCurrentBulletinFolder()
	{
		int selectedIndex = currentCasesListView.getSelectionModel().getSelectedIndex();
		if(selectedIndex == INVALID_INDEX)
			return null;
		CaseListItem selectedCase = getCurrentCaseListProvider().get(selectedIndex);
		BulletinFolder folder = getApp().findFolder(selectedCase.getName());
		
		return folder;
	}

	private void selectCase(String caseName)
	{
		for (Iterator iterator = getCurrentCaseListProvider().iterator(); iterator.hasNext();)
		{
			CaseListItem caseItem = (CaseListItem) iterator.next();
			if(caseItem.getName().equals(caseName))
			{
				selectCaseAndScrollInView(caseItem);
				break;
			}
		}
	}

	private void selectCaseAndScrollInView(CaseListItem caseToSelect)
	{
		currentCasesListView.getSelectionModel().select(caseToSelect);
		currentCasesListView.scrollTo(caseToSelect);
	}

	private void updateButtons(BulletinFolder folder)
	{
		if(folder.canDelete())
			deleteFolderButton.setDisable(false);
		else
			deleteFolderButton.setDisable(true);
	}

	@FXML
	public void onFolderSettingsClicked(MouseEvent mouseEvent) 
	{
		FxFolderSettingsController folderManagement = new FxFolderSettingsController(getMainWindow(), new FolderNameChoiceBoxListener(), new FolderCustomNameListener());
		ActionDoer shellController = new DialogWithCloseShellController(getMainWindow(), folderManagement);
		doAction(shellController);
	}
	
	@FXML
	public void onFolderNewClicked(MouseEvent mouseEvent) 
	{
		FxFolderCreateController createNewFolder = new FxFolderCreateController(getMainWindow());
		createNewFolder.addFolderCreatedListener(new FolderCreatedListener());
		ActionDoer shellController = new DialogWithOkCancelShellController(getMainWindow(), createNewFolder);
		doAction(shellController);
	}
	
	
	@FXML
	public void onCaseListMouseClicked(MouseEvent mouseEvent) 
	{
	    if(mouseEvent.getButton().equals(MouseButton.PRIMARY))
	    {
		    final int MOUSE_DOUBLE_CLICK = 2;
	    		if(mouseEvent.getClickCount() == MOUSE_DOUBLE_CLICK)
	        {
	            renameCaseName();
	        }
	    }
	}
	
	private void renameCaseName()
	{
		BulletinFolder currentFolder = currentSelectedCase.get().getFolder();
		if(currentFolder == null)
			return;
		if(!currentFolder.canRename())
			return;

		FxFolderRenameController renameFolder = new FxFolderRenameController(getMainWindow(), currentFolder.getName());
		renameFolder.addFolderRenameListener(new FolderRenamedListener());
		ActionDoer shellController = new DialogWithOkCancelShellController(getMainWindow(), renameFolder);
		doAction(shellController);
	}
	
	private void orderCases()
	{
		sortCases(casesListViewAll.getItems());		
	}

	public void sortCases(ObservableList<CaseListItem> items)
	{
		java.util.Collections.sort(items, new CaseComparator());
	}
	
	@FXML
	public void onImportBulletin(ActionEvent event)
	{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(getLocalization().getWindowTitle("ImportBulletin"));
		fileChooser.setInitialDirectory(getApp().getMartusDataRootDirectory());
		FormatFilter mbaFileFilter = new MartusBulletinArchiveFileFilter(getLocalization());
		FormatFilter xmlFileFilter = new BulletinXmlFileFilter(getLocalization());
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter(xmlFileFilter.getDescription(), xmlFileFilter.getWildCardExtension()),
				new FileChooser.ExtensionFilter(mbaFileFilter.getDescription(), mbaFileFilter.getWildCardExtension()),
				new FileChooser.ExtensionFilter(getLocalization().getFieldLabel("AllFiles"), "*.*"));
		
		File fileToImport = fileChooser.showOpenDialog(null);
		if (fileToImport == null)
			return;
		
		ExtensionFilter chosenExtensionFilter = fileChooser.getSelectedExtensionFilter();
		if (isXmlExtensionSelected(chosenExtensionFilter, fileToImport))
			importBulletinFromXmlFile(fileToImport);
		
		if (isMbaExtensionSelected(chosenExtensionFilter, fileToImport))
			importBulletinFromMbaFile(fileToImport);
	}

	private void importBulletinFromXmlFile(File fileToImport)
	{
		try
		{
			ImporterOfXmlFilesOfBulletins importer = new ImporterOfXmlFilesOfBulletins(fileToImport, getApp().getStore(), getCurrentBulletinFolder(), System.out);
			importer.importFiles();
		}
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	private void importBulletinFromMbaFile(File fileToImport)
	{
		try
		{
			getApp().getStore().importZipFileBulletin(fileToImport, getCurrentBulletinFolder(), false);
		}
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}
	
	private final class CaseComparator implements java.util.Comparator<CaseListItem>
	{
		public CaseComparator()
		{
		}

		@Override
		public int compare(CaseListItem case1, CaseListItem case2) 
		{
			return case1.getNameLocalized().compareToIgnoreCase(case2.getNameLocalized());
		}
	}

	private class CaseListChangeListener implements ChangeListener<CaseListItem>
	{
		public CaseListChangeListener()
		{
		}

		@Override
		public void changed(ObservableValue<? extends CaseListItem> observalue	,
				CaseListItem previousCase, CaseListItem newCase)
		{
			selectCurrentCaseIfNothingWasPreviouslySelected(previousCase);
			updateCaseList();
		}

	}
	
	protected void selectCurrentCaseIfNothingWasPreviouslySelected(CaseListItem previousCase)
	{
		if(previousCase == null)
			setCurrentlyViewedCaseList(casesTabPane.getSelectionModel().getSelectedItem());
	}
		
	private class caseTabeListener implements ChangeListener<Tab>
	{
		public caseTabeListener()
		{
		}

		@Override
		public void changed(ObservableValue<? extends Tab> observableValue, Tab previousTab, Tab currentTab)
		{
			setCurrentlyViewedCaseList(currentTab);
		}
	}

	private class FolderCreatedListener implements ChangeListener<String>
	{
		public FolderCreatedListener()
		{
		}

		public void changed(ObservableValue<? extends String> observableValue, String oldFolderName, String newlyCreatedFoldersName)
		{
			updateCases(newlyCreatedFoldersName);
		}		
	}

	private class FolderRenamedListener implements ChangeListener<String>
	{
		public FolderRenamedListener()
		{
		}

		public void changed(ObservableValue<? extends String> observableValue, String oldFolderName, String renamedFoldersName)
		{
			updateCases(renamedFoldersName);
		}		
	}

	@FXML
	public void onFolderDeleteClicked(MouseEvent mouseEvent) 
	{
		BulletinFolder folder = currentSelectedCase.get().getFolder();
		FxFolderDeleteController deleteFolder = new FxFolderDeleteController(getMainWindow(), folder);
		deleteFolder.addFolderDeletedListener(new FolderDeletedHandler());
		ActionDoer shellController = new DialogWithOkCancelShellController(getMainWindow(), deleteFolder);
		doAction(shellController);
	}

	@FXML
	public void onManageTemplates(ActionEvent event)
	{
		try
		{
			FxController controller = new ManageTemplatesController(getMainWindow());
			ActionDoer shellController = new DialogWithNoButtonsShellController(getMainWindow(), controller);
			doAction(shellController);
		}
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}
	
	@FXML
	public void onShowAllCases(ActionEvent event)
	{
		BulletinFolder allFolders = null;
		listeners.forEach(listener -> listener.folderWasSelected(allFolders));
		currentCasesListView.getSelectionModel().clearSelection();
	}	
	
	public void onServerSync(ActionEvent event)
	{
		try
		{
			if(getMainWindow().isRetrieveInProgress())
			{
				showNotifyDialog("RetrieveInProgress");
				return;
			}
			if(!getApp().isSSLServerAvailable())
			{
				showNotifyDialog("retrievenoserver");
				return;
			}
			ManageServerSyncRecordsController controller = new ManageServerSyncRecordsController(getMainWindow());
			ActionDoer shellController = new DialogWithNoButtonsShellController(getMainWindow(), controller);
			doAction(shellController);
		}
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	class FolderDeletedHandler implements FolderDeletedListener
	{
		@Override
		public void folderWasDeleted()
		{
			updateCasesSelectDefaultCase();
		}		
	}

	private final class FolderNameChoiceBoxListener implements ChangeListener<ChoiceItem>
	{
		public FolderNameChoiceBoxListener()
		{
		}

		@Override public void changed(ObservableValue<? extends ChoiceItem> observableValue, ChoiceItem originalItem, ChoiceItem newItem) 
		{
			String code = newItem.getCode();
			String customLabel = getApp().getConfigInfo().getFolderLabelCustomName();
			String foldersLabel = FxFolderSettingsController.getFoldersHeading(code, customLabel, getLocalization());
			updateFolderLabelName(foldersLabel);
		}
	}

	
	private final class FolderCustomNameListener implements ChangeListener<String>
	{
		public FolderCustomNameListener()
		{
		}

		@Override public void changed(ObservableValue<? extends String> observableValue, String original, String newLabel) 
		{
			updateFolderLabelName(newLabel);
		}
	}

	protected void updateFolderLabelName(String newLabel)
	{
		folderNameLabel.setText(newLabel);
	}

	public static final String LOCATION_CASE_MANAGEMENT_FXML = "landing/cases/CaseManagement.fxml";
	private final int INVALID_INDEX = -1;
	private String DEFAULT_SELECTED_CASE_NAME = ClientBulletinStore.SAVED_FOLDER;

	@FXML
	private ListView<CaseListItem> casesListViewAll;
			
	@FXML
	private TabPane casesTabPane;
	
	@FXML
	private Tab tabCaseAll;
	

	@FXML
	private Label folderNameLabel;

	@FXML
	private Button deleteFolderButton;
	
	@FXML
	private Button showTrashFolder;

	private 	ReadOnlyObjectProperty<CaseListItem> currentSelectedCase;
	
	private ListView<CaseListItem> currentCasesListView;
	private Set<FolderSelectionListener> listeners;
}
