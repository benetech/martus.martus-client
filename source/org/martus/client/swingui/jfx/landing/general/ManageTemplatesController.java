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
package org.martus.client.swingui.jfx.landing.general;

import java.io.File;
import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.stage.FileChooser;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.templates.FormTemplateManager;
import org.martus.client.core.templates.GenericFormTemplates;
import org.martus.client.search.SaneCollator;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.filefilters.MCTFileFilter;
import org.martus.client.swingui.jfx.common.AbstractFxImportFormTemplateController;
import org.martus.client.swingui.jfx.generic.FxInSwingController;
import org.martus.client.swingui.jfx.generic.data.ObservableChoiceItemList;
import org.martus.client.swingui.jfx.setupwizard.step5.FxSetupImportTemplatesController;
import org.martus.common.MartusLogger;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.FormTemplate;

public class ManageTemplatesController extends FxInSwingController
{
	public ManageTemplatesController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		
		initializeSelectTab();
		initializeImportTab();
	}

	private void initializeSelectTab()
	{
		ClientBulletinStore store = getBulletinStore();
		ObservableSet<String> templateNames = store.getAvailableTemplates();
		ObservableChoiceItemList templateChoiceItems = new ObservableChoiceItemList();
		templateNames.forEach(name -> templateChoiceItems.add(createTemplateChoiceItem(name)));
		Comparator<ChoiceItem> sorter = new SaneCollator(getLocalization().getCurrentLanguageCode());
		templateChoiceItems.sort(sorter);
		availableTemplates.setItems(templateChoiceItems);
		updateSelectionFromReality();
	}
	
	private void initializeImportTab()
	{
		templateToAddProperty = new SimpleObjectProperty<FormTemplate>();
		
		ReadOnlyObjectProperty<FormTemplate> selectedGenericTemplateProperty = genericChoiceBox.getSelectionModel().selectedItemProperty();
		selectedGenericTemplateProperty.addListener(new GenericTemplateSelectedHandler());
		genericChoiceBox.visibleProperty().bind(genericRadioButton.selectedProperty());
		genericChoiceBox.setItems(GenericFormTemplates.getDefaultFormTemplateChoices(getSecurity()));

		ReadOnlyObjectProperty<AbstractFxImportFormTemplateController> selectedDownloadTypeProperty = downloadChoiceBox.getSelectionModel().selectedItemProperty();
		selectedDownloadTypeProperty.addListener(new DownloadTypeSelectedHandler());
		downloadChoiceBox.visibleProperty().bind(downloadRadioButton.selectedProperty());
		downloadChoiceBox.setItems(FxSetupImportTemplatesController.getImportTemplateChoices(getMainWindow()));
		
		chooseFileButton.visibleProperty().bind(importFileRadioButton.selectedProperty());
		
		genericRadioButton.setSelected(true);
		
		addTemplateButton.disableProperty().bind(Bindings.isNull(templateToAddProperty));
	}
	
	protected class GenericTemplateSelectedHandler implements ChangeListener<FormTemplate>
	{
		@Override
		public void changed(ObservableValue<? extends FormTemplate> observable, FormTemplate oldTemplate, FormTemplate newTemplate)
		{
			updateTemplateFromGeneric();
		}
		
	}

	protected class DownloadTypeSelectedHandler implements ChangeListener<AbstractFxImportFormTemplateController>
	{
		@Override
		public void changed(ObservableValue<? extends AbstractFxImportFormTemplateController> observable, AbstractFxImportFormTemplateController oldValue, AbstractFxImportFormTemplateController newValue)
		{
			if(newValue == null)
				return;

			try
			{
				showControllerInsideModalDialog(newValue);
				FormTemplate downloadedTemplate = newValue.getSelectedFormTemplate();
				updateTemplateFromDownloaded(downloadedTemplate);
			}
			catch(Exception e)
			{
				logAndNotifyUnexpectedError(e);
			}
		}

	}

	protected void updateTemplateFromDownloaded(FormTemplate downloadedTemplate)
	{
		templateToAddProperty.setValue(downloadedTemplate);
		logTemplateToBeAdded();
	}
	
	private void updateSelectionFromReality()
	{
		ClientBulletinStore store = getBulletinStore();
		ObservableChoiceItemList templateChoiceItems = (ObservableChoiceItemList) availableTemplates.getItems();
		try
		{
			ChoiceItem current = templateChoiceItems.findByCode(store.getCurrentFormTemplateName());
			availableTemplates.getSelectionModel().select(current);
		}
		catch(Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	private ClientBulletinStore getBulletinStore()
	{
		return getApp().getStore();
	}
	
	private ChoiceItem createTemplateChoiceItem(String name)
	{
		String displayableName = name;
		if(displayableName.equals(FormTemplateManager.MARTUS_DEFAULT_FORM_TEMPLATE_NAME))
			displayableName = getLocalization().getFieldLabel("DisplayableDefaultFormTemplateName");
		ChoiceItem choiceItem = new ChoiceItem(name, displayableName);
		return choiceItem;
	}

	private void logTemplateToBeAdded()
	{
		MartusLogger.log("Ready to add template: " + getTitleOfTemplateToBeAdded());
	}

	private String getTitleOfTemplateToBeAdded()
	{
		FormTemplate template = templateToAddProperty.getValue();
		String title = "(none)";
		if(template != null)
			title = template.getTitle();
		return title;
	}
	
	protected void updateTemplateFromGeneric()
	{
		FormTemplate selected = genericChoiceBox.getSelectionModel().getSelectedItem();
		templateToAddProperty.setValue(selected);
		logTemplateToBeAdded();
	}
		
	@Override
	public String getFxmlLocation()
	{
		return "landing/general/ManageTemplates.fxml";
	}
	
	@FXML
	private void onChooseGeneric(ActionEvent event)
	{
		updateTemplateFromGeneric();
	}

	@FXML
	private void onChooseFromServer(ActionEvent event)
	{
		templateToAddProperty.setValue(null);
		downloadChoiceBox.getSelectionModel().clearSelection();
		logTemplateToBeAdded();
	}
	
	@FXML
	private void onChooseFromFile(ActionEvent event)
	{
		templateToAddProperty.setValue(null);
		logTemplateToBeAdded();
	}
	
	@FXML
	private void onImportFromFile(ActionEvent event)
	{
		//FIXME: This Dialog can be hidden behind
		FileChooser fileChooser = new FileChooser();
		File martusRootDir = getApp().getMartusDataRootDirectory();
		fileChooser.setInitialDirectory(martusRootDir);
		fileChooser.setTitle(getLocalization().getWindowTitle("FileDialogImportCustomization"));
		MCTFileFilter templateFileFilter = new MCTFileFilter(getLocalization());
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter(templateFileFilter.getDescription(), templateFileFilter.getWildCardExtension()),
				new FileChooser.ExtensionFilter(getLocalization().getFieldLabel("AllFiles"), "*.*"));
		File templateFile = fileChooser.showOpenDialog(null);
		if(templateFile == null)
			return;
		
		FormTemplate importedTemplate = new FormTemplate();
		try
		{
			if(!importedTemplate.importTemplate(templateFile, getSecurity()))
			{
				showNotifyDialog("ErrorImportingCustomizationTemplate");
				return;
			}
			templateToAddProperty.setValue(importedTemplate);
			logTemplateToBeAdded();
		}
		catch(Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	@FXML
	private ListView<ChoiceItem> availableTemplates;
	
	@FXML
	private RadioButton genericRadioButton;
	
	@FXML
	private ChoiceBox<FormTemplate> genericChoiceBox;
	
	@FXML
	private RadioButton downloadRadioButton;
	
	@FXML
	private ChoiceBox downloadChoiceBox;
	
	@FXML
	private RadioButton importFileRadioButton;
	
	@FXML
	private Button chooseFileButton;
	
	@FXML
	private Button addTemplateButton;
	
	private SimpleObjectProperty<FormTemplate> templateToAddProperty;
}
