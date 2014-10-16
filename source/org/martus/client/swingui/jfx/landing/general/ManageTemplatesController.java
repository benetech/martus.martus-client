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
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.templates.GenericFormTemplates;
import org.martus.client.search.SaneCollator;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiCustomFieldsDlg;
import org.martus.client.swingui.filefilters.BulletinXmlFileFilter;
import org.martus.client.swingui.filefilters.MCTFileFilter;
import org.martus.client.swingui.jfx.common.AbstractFxImportFormTemplateController;
import org.martus.client.swingui.jfx.common.TemplatePropertiesController;
import org.martus.client.swingui.jfx.generic.FxInSwingController;
import org.martus.client.swingui.jfx.generic.controls.FxButtonTableCellFactory;
import org.martus.client.swingui.jfx.setupwizard.step5.FxSetupImportTemplatesController;
import org.martus.clientside.FormatFilter;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.Exceptions.ServerNotAvailableException;
import org.martus.common.Exceptions.ServerNotCompatibleException;
import org.martus.common.MartusLogger;
import org.martus.common.XmlFormTemplateLoader;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.fieldspec.FormTemplate;
import org.martus.util.TokenReplacement;
import org.martus.util.UnicodeReader;
import org.martus.util.xml.SimpleXmlParser;

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
		
		initializeAvailableTab();
		initializeAddTab();
	}

	private void initializeAvailableTab()
	{
		templateNameColumn.setEditable(false);
        templateNameColumn.setCellValueFactory(new PropertyValueFactory<ManageTemplatesTableRowData,String>(ManageTemplatesTableRowData.DISPLAYABLE_TEMPLATE_NAME));
		Comparator<String> sorter = new SaneCollator(getLocalization().getCurrentLanguageCode());
        templateNameColumn.setComparator(sorter);

        Image trashImage = new Image(TRASH_IMAGE_PATH);
        templateDeleteColumn.setCellFactory(new FxButtonTableCellFactory(trashImage, () -> deleteSelectedTemplate()));
        templateDeleteColumn.setCellValueFactory(new PropertyValueFactory<Object,Boolean>(ManageTemplatesTableRowData.CAN_DELETE_NAME));
        
        Image uploadImage = new Image(UPLOAD_IMAGE_PATH);
        templateUploadColumn.setCellFactory(new FxButtonTableCellFactory(uploadImage, () -> uploadSelectedTemplate()));
        templateUploadColumn.setCellValueFactory(new PropertyValueFactory<Object,Boolean>(ManageTemplatesTableRowData.CAN_UPLOAD_NAME));
        
        Image exportImage = new Image(EXPORT_IMAGE_PATH);
        templateExportColumn.setCellFactory(new FxButtonTableCellFactory(exportImage, () -> exportSelectedTemplate()));
        templateExportColumn.setCellValueFactory(new PropertyValueFactory<Object,Boolean>(ManageTemplatesTableRowData.CAN_EXPORT_NAME));
        
        Image editImage = new Image(EDIT_IMAGE_PATH);
        templateEditColumn.setCellFactory(new FxButtonTableCellFactory(editImage, () -> editSelectedTemplate()));
        templateEditColumn.setCellValueFactory(new PropertyValueFactory<Object,Boolean>(ManageTemplatesTableRowData.CAN_EDIT_NAME));
        
        populateAvailableTemplatesTable();

        availableTemplatesTable.getSortOrder().clear();
		availableTemplatesTable.getSortOrder().add(templateNameColumn);
	}
	
	protected void deleteSelectedTemplate()
	{
		try
		{
			ManageTemplatesTableRowData selected = availableTemplatesTable.getSelectionModel().getSelectedItem();
			String displayableName = selected.getDisplayableTemplateName();
			
			String messageTemplate = getLocalization().getFieldLabel("confirmDeleteTemplate");
			String message = TokenReplacement.replaceToken(messageTemplate, "#Name#", displayableName);
			if(!showConfirmationDialog("Templates", message))
				return;

			getBulletinStore().deleteFormTemplate(selected.getRawTemplateName());
			
			populateAvailableTemplatesTable();
		}
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	protected void uploadSelectedTemplate()
	{
		try
		{
			ManageTemplatesTableRowData selected = availableTemplatesTable.getSelectionModel().getSelectedItem();
			String rawTitle = selected.getRawTemplateName();
			FormTemplate template = getBulletinStore().getFormTemplate(rawTitle);

			String displayableTitle = selected.getDisplayableTemplateName();
			String rawWhich = getLocalization().getFieldLabel("WhichTemplate");
			String which = TokenReplacement.replaceToken(rawWhich, "#TemplateTitle#", displayableTitle);
			String cause = getLocalization().getFieldLabel("confirmUploadPublicTemplateToServerWarningcause");
			String effect = getLocalization().getFieldLabel("confirmUploadPublicTemplateToServerWarningeffect");
			String message = which + "\n\n" + cause + "\n\n" + effect;
			if(!showConfirmationDialog("confirmUploadPublicTemplateToServerWarning", message))
				return;
			
			uploadTemplateToServer(template);
		}
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	public void uploadTemplateToServer(FormTemplate template)
	{
		try
		{
			getApp().putFormTemplateOnServer(template);
		}
		catch (ServerNotCompatibleException e)
		{
			showNotifyDialog("ServerNotCompatible");
		}
		catch (ServerNotAvailableException e)
		{
			showNotifyDialog("ServerNotAvailable");
		} 
		catch (Exception e)
		{
			showNotifyDialog("ErrorSavingTemplateToServer");
		}
	}

	protected void exportSelectedTemplate()
	{
		try
		{
			ManageTemplatesTableRowData selected = availableTemplatesTable.getSelectionModel().getSelectedItem();
			String rawTitle = selected.getRawTemplateName();
			FormTemplate template = getBulletinStore().getFormTemplate(rawTitle);
			exportTemplate(getMainWindow(), template);
		}
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}
	
	private void exportTemplate(UiMainWindow mainWindowToUse, FormTemplate template) throws Exception
	{
		FileChooser fileChooser = createFileChooser("FileDialogExportCustomization");
		File templateFile = fileChooser.showSaveDialog(null);
		if(templateFile == null)
			return;
		
		ExtensionFilter chosenExtensionFilter = fileChooser.getSelectedExtensionFilter();
		MartusCrypto securityTemp = mainWindowToUse.getApp().getSecurity();
		
		if (isExtensionSelected(chosenExtensionFilter, new MCTFileFilter(getLocalization())))
			template.exportTemplate(securityTemp, templateFile);
		
		if (isXmlExtensionSelected(chosenExtensionFilter))
			template.exportTopSection(templateFile);
	}

	private boolean isXmlExtensionSelected(ExtensionFilter chosenExtensionFilter)
	{
		return isExtensionSelected(chosenExtensionFilter, new BulletinXmlFileFilter(getLocalization()));
	}

	private boolean isExtensionSelected(ExtensionFilter chosenFileFilter, FormatFilter mctFileFilter) 
	{
		List<String> extensions = chosenFileFilter.getExtensions();
		for (String extension : extensions)
		{
			if (extension.contains(mctFileFilter.getExtension()))
				return true;
		}
		
		return false;
	}
	
	protected void editSelectedTemplate()
	{
		try
		{
			ManageTemplatesTableRowData selected = availableTemplatesTable.getSelectionModel().getSelectedItem();
			String rawTitle = selected.getRawTemplateName();
			FormTemplate template = getBulletinStore().getFormTemplate(rawTitle);
			TemplatePropertiesController controller = new TemplatePropertiesController(getMainWindow(), template);
			if(showModalYesNoDialog("TemplateProperties", EnglishCommonStrings.OK, EnglishCommonStrings.CANCEL, controller))
			{
				String oldTitle = template.getTitle();
				String newTitle = controller.getTemplateTitle();
				boolean willCreateNewCopy = !newTitle.equals(oldTitle);

				template.setTitle(newTitle);
				template.setDescription(controller.getTemplateDescription());

				ClientBulletinStore store = getApp().getStore();
				store.saveNewFormTemplate(template);
				if(willCreateNewCopy)
					store.deleteFormTemplate(oldTitle);
				
				populateAvailableTemplatesTable();
			}
			
		}
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	private void populateAvailableTemplatesTable()
	{
		TableViewSelectionModel<ManageTemplatesTableRowData> selectionModel = availableTemplatesTable.selectionModelProperty().getValue();
		ManageTemplatesTableRowData selected = selectionModel.getSelectedItem();
		
		ClientBulletinStore store = getBulletinStore();
		ObservableSet<String> templateNamesSet = store.getAvailableTemplates();
		ObservableList<ManageTemplatesTableRowData> templateRows = FXCollections.observableArrayList();
		templateNamesSet.forEach(name -> templateRows.add(new ManageTemplatesTableRowData(name, getLocalization())));

		availableTemplatesTable.setItems(templateRows);
		availableTemplatesTable.sort();
		
		selectionModel.clearSelection();
		selectionModel.select(selected);
	}
	
	private void initializeAddTab()
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
	
	private ClientBulletinStore getBulletinStore()
	{
		return getApp().getStore();
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
		FileChooser fileChooser = createFileChooser("FileDialogImportCustomization");
		File templateFile = fileChooser.showOpenDialog(null);
		if(templateFile == null)
			return;
		
		
		try
		{
			ExtensionFilter chosenExtensionFilter = fileChooser.getSelectedExtensionFilter();
			if (isXmlExtensionSelected(chosenExtensionFilter))
			{
				importXmlFormTemplate(templateFile);
				return;
			}
		
			FormTemplate importedTemplate = new FormTemplate();
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

	private void importXmlFormTemplate(File templateFile) throws Exception
	{
		String xmlAsString = importXmlAsString(templateFile);
		
		XmlFormTemplateLoader loader = new XmlFormTemplateLoader();
		SimpleXmlParser.parse(loader, xmlAsString);
		FormTemplate importedTemplate = loader.getFormTemplate();
		
		if (importedTemplate.isvalidTemplateXml())
		{
			templateToAddProperty.setValue(importedTemplate);
		}
		else
		{
			Vector errors = importedTemplate.getErrors();
			String errorsAsString = UiCustomFieldsDlg.createErrorMessage(getMainWindow(), errors).toString();
			showNotifyDialog("ErrorImportingCustomizationTemplate", errorsAsString);
		}
		
		logTemplateToBeAdded();
	}
	
	private String importXmlAsString(File tempFormTemplateFile) throws Exception 
	{
		UnicodeReader reader = new UnicodeReader(tempFormTemplateFile);
		try
		{
			return reader.readAll();
		}
		finally
		{
			reader.close();
		}
	}
	
	private FileChooser createFileChooser(String titleTag)
	{
		//FIXME: This Dialog can be hidden behind
		FileChooser fileChooser = new FileChooser();
		File martusRootDir = getApp().getMartusDataRootDirectory();
		fileChooser.setInitialDirectory(martusRootDir);
		fileChooser.setTitle(getLocalization().getWindowTitle(titleTag));
		MCTFileFilter templateFileFilter = new MCTFileFilter(getLocalization());
		BulletinXmlFileFilter xmlTemplateFileFilter = new BulletinXmlFileFilter(getLocalization());
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter(templateFileFilter.getDescription(), templateFileFilter.getWildCardExtension()),
				new FileChooser.ExtensionFilter(xmlTemplateFileFilter.getDescription(), xmlTemplateFileFilter.getWildCardExtension()),
				new FileChooser.ExtensionFilter(getLocalization().getFieldLabel("AllFiles"), "*.*"));

		return fileChooser;
	}
	
	@FXML
	private void onAvailableTabOkButton(ActionEvent event)
	{
		getStage().close();
	}
	
	@FXML
	private void onAdd(ActionEvent event)
	{
		try
		{
			FormTemplate templateToAdd = templateToAddProperty.getValue();
			if(templateToAdd == null)
			{
				showNotifyDialog("NoTemplateSelectedToAdd");
				return;
			}
			ObservableSet<String> existingTemplateTitles = getBulletinStore().getAvailableTemplates();
			boolean doesTemplateExist = existingTemplateTitles.contains(templateToAdd.getTitle());
			if(doesTemplateExist)
			{
				String title = getLocalization().getWindowTitle("AddTemplate");
				String message = getLocalization().getFieldLabel("confirmTemplateAlreadyExistscause");
				if(!showConfirmationDialog(title, message))
					return;
			}
			getBulletinStore().saveNewFormTemplate(templateToAdd);
			populateAvailableTemplatesTable();
			templateToAddProperty.setValue(null);
			
			tabPane.selectionModelProperty().get().select(availableTemplatesTab);
			availableTemplatesTable.selectionModelProperty().get().clearSelection();
			availableTemplatesTable.scrollTo(0);
		}
		catch(Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	final private String TRASH_IMAGE_PATH = "/org/martus/client/swingui/jfx/images/trash.png";
	final private String UPLOAD_IMAGE_PATH = "/org/martus/client/swingui/jfx/images/upload.png";
	final private String EXPORT_IMAGE_PATH = "/org/martus/client/swingui/jfx/images/export.png";
	final private String EDIT_IMAGE_PATH = "/org/martus/client/swingui/jfx/images/edit.png";
	
	@FXML
	private TabPane tabPane;
	
	@FXML
	private Tab availableTemplatesTab;
	
	@FXML
	private TableView<ManageTemplatesTableRowData> availableTemplatesTable;
	
	@FXML
	protected TableColumn<ManageTemplatesTableRowData, String> templateNameColumn;

	@FXML
	protected TableColumn<Object, Boolean> templateDeleteColumn;
	
	@FXML
	protected TableColumn<Object, Boolean> templateUploadColumn;
	
	@FXML
	protected TableColumn<Object, Boolean> templateExportColumn;
	
	@FXML
	protected TableColumn<Object, Boolean> templateEditColumn;
	
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
