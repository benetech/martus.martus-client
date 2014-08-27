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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionDoer;
import org.martus.client.swingui.jfx.generic.DialogWithCloseShellController;
import org.martus.client.swingui.jfx.generic.DialogWithOkCancelShellController;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.landing.AbstractFxLandingContentController;
import org.martus.client.swingui.jfx.landing.FolderSelectionListener;
import org.martus.client.swingui.jfx.landing.cases.FxFolderDeleteController.FolderDeletedListener;
import org.martus.client.swingui.jfx.landing.general.SelectTemplateController;
import org.martus.common.fieldspec.ChoiceItem;

public class FxCaseManagementController extends AbstractFxLandingContentController
{
	public FxCaseManagementController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		
		listeners = new HashSet<FolderSelectionListener>();
		caseListProviderAll = new CaseListProvider();
		caseListProviderOpen = new CaseListProvider();
		caseListProviderClosed = new CaseListProvider();
	}

	@Override
	public void initializeMainContentPane()
	{
		updateCasesSelectDefaultCase();
		CaseListChangeListener caseListChangeListener = new CaseListChangeListener();
		casesListViewAll.getSelectionModel().selectedItemProperty().addListener(caseListChangeListener);
		casesListViewAll.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		casesListViewOpen.getSelectionModel().selectedItemProperty().addListener(caseListChangeListener);
		casesListViewOpen.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		casesListViewClosed.getSelectionModel().selectedItemProperty().addListener(caseListChangeListener);
		casesListViewClosed.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		casesTabPane.getSelectionModel().selectedItemProperty().addListener(new caseTabeListener());
		currentSelectedCase = currentCasesListView.getSelectionModel().selectedItemProperty();
	}

	@Override
	public String getFxmlLocation()
	{
		return LOCATION_CASE_MANAGEMENT_FXML;
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
		if(currentlyViewedCaseTab.equals(tabCaseOpen))
		{
			currentCasesListView = casesListViewOpen;
			currentCaseListProvider = caseListProviderOpen;
		}
		else if (currentlyViewedCaseTab.equals(tabCaseClosed))
		{
			currentCasesListView = casesListViewClosed;
			currentCaseListProvider = caseListProviderClosed;
		}
		else
		{
			currentCasesListView = casesListViewAll;
			currentCaseListProvider = caseListProviderAll;
		}		
		updateCaseList();
	}


	protected void updateCases(String caseNameToSelect)
	{
		String foldersLabel = FxFolderSettingsController.getCurrentFoldersHeading(getApp().getConfigInfo(), getLocalization());
		updateFolderLabelName(foldersLabel);

		caseListProviderAll.clear();
		caseListProviderOpen.clear();
		caseListProviderClosed.clear();
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
			if(folder.isClosed())
				caseListProviderClosed.add(caseList);
			else
				caseListProviderOpen.add(caseList);
		}
		casesListViewAll.setItems(caseListProviderAll);
		casesListViewOpen.setItems(caseListProviderOpen);
		casesListViewClosed.setItems(caseListProviderClosed);
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
	
	protected void updateCaseList()
	{
		try
		{
			int selectedIndex = currentCasesListView.getSelectionModel().getSelectedIndex();
			if(selectedIndex == INVALID_INDEX)
				return;
			CaseListItem selectedCase = currentCaseListProvider.get(selectedIndex);
			BulletinFolder folder = getApp().findFolder(selectedCase.getName());
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

	private void selectCase(String caseName)
	{
		for (Iterator iterator = currentCaseListProvider.iterator(); iterator.hasNext();)
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
		java.util.Collections.sort(casesListViewAll.getItems(), new CaseComparitor());		
		java.util.Collections.sort(casesListViewOpen.getItems(), new CaseComparitor());		
		java.util.Collections.sort(casesListViewClosed.getItems(), new CaseComparitor());		
	}
	
	private final class CaseComparitor implements java.util.Comparator<CaseListItem>
	{
		public CaseComparitor()
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
			updateCaseList();
		}
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
			FxController controller = new SelectTemplateController(getMainWindow());
			ActionDoer shellController = new DialogWithCloseShellController(getMainWindow(), controller);
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
	private ListView<CaseListItem> casesListViewOpen;

	@FXML
	private ListView<CaseListItem> casesListViewClosed;
	
	@FXML
	private TabPane casesTabPane;
	
	@FXML
	private Tab tabCaseAll;
	
	@FXML
	private Tab tabCaseOpen;

	@FXML
	private Tab tabCaseClosed;


	@FXML
	private Label folderNameLabel;

	@FXML
	private Button deleteFolderButton;

	private 	ReadOnlyObjectProperty<CaseListItem> currentSelectedCase;
	
	private CaseListProvider caseListProviderAll;
	private CaseListProvider caseListProviderOpen;
	private CaseListProvider caseListProviderClosed;
	
	private ListView<CaseListItem> currentCasesListView;
	private CaseListProvider currentCaseListProvider;
	private Set<FolderSelectionListener> listeners;
}
