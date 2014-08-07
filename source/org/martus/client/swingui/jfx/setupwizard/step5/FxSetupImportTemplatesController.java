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
package org.martus.client.swingui.jfx.setupwizard.step5;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.util.StringConverter;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.setupwizard.AbstractFxSetupWizardContentController;
import org.martus.client.swingui.jfx.setupwizard.step6.FxSetupBackupYourKeyController;
import org.martus.common.MartusLogger;
import org.martus.common.fieldspec.FormTemplate;
import org.martus.common.fieldspec.FormTemplate.FutureVersionException;
import org.martus.util.TokenReplacement;
import org.martus.util.inputstreamwithseek.ByteArrayInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;

public class FxSetupImportTemplatesController extends FxStep5Controller
{
	public FxSetupImportTemplatesController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/step5/SetupImportTemplate.fxml";
	}
	
	@Override
	public String getSidebarFxmlLocation()
	{
		return "setupwizard/step5/SetupImportTemplateSidebar.fxml";
	}

	@Override
	public AbstractFxSetupWizardContentController getNextController()
	{
		return new FxSetupBackupYourKeyController(getMainWindow());
	}

	@Override
	public void initializeMainContentPane()
	{
		genericTemplatesChoiceBox.setConverter(new FormTemplateToStringConverter(getLocalization()));
		genericTemplatesChoiceBox.setItems(FXCollections.observableArrayList(getDefaultFormTemplateChoices()));
		genericTemplatesChoiceBox.getSelectionModel().selectedItemProperty().addListener(new GenericTemplatesSelectionChangedHandler());

		customTemplatesChoiceBox.setItems(FXCollections.observableArrayList(getImportTemplateChoices()));
		customTemplatesChoiceBox.setConverter(new ControllerToStringConverter());
		customTemplatesChoiceBox.getSelectionModel().selectedItemProperty().addListener(new CustomTemplatesSelectionChangedHandler());
		
		genericTemplatesChoiceBox.setVisible(false);
		customTemplatesChoiceBox.setVisible(false);
		
		selectedTemplateLabel.setVisible(false);
		switchFormsLaterLabel.setVisible(false);
		safelyInitializeCustomTemplateRadioVisibility();
		getWizardNavigationHandler().getNextButton().addEventHandler(ActionEvent.ACTION, new NextButtonHandler());
	}

	private void safelyInitializeCustomTemplateRadioVisibility()
	{
		try
		{
			downloadCustomRadioButton.setVisible(isCurrentServerAvailable());
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			showNotifyDialog("UnexpectedError");
		}
	} 
	
	private ObservableList<AbstractFxImportFormTemplateController> getImportTemplateChoices()
	{
		Vector<AbstractFxImportFormTemplateController> choices = new Vector<AbstractFxImportFormTemplateController>();
		choices.add(new FxImportFormTemplateFromMyContactsPopupController(getMainWindow(), getWizardStage()));
		choices.add(new FxSetupFormTemplateFromNewContactPopupController(getMainWindow(), getWizardStage()));

		return FXCollections.observableArrayList(choices);
	}

	private ObservableList<FormTemplate> getDefaultFormTemplateChoices()
	{
		try
		{
			Vector<FormTemplate> customTemplates = loadFormTemplates();

			return FXCollections.observableArrayList(customTemplates);
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
			return FXCollections.observableArrayList();
		}
	}
	
	private Vector<FormTemplate> loadFormTemplates() throws Exception
	{
		String[] formTemplateFileNames = new String[]
		{
			"formtemplates/Amnesty-Urgent-Actions.mct", 
			"formtemplates/Journalist-Example.mct", 
			"formtemplates/Martus-Customization-Example.mct", 
			"formtemplates/UN-Disappearances.mct", 
			"formtemplates/UN-Special-Rapporteur-Executions.mct", 
		};
		Vector<FormTemplate> formTemplates = new Vector<FormTemplate>();
		for (String formTemplateFileName : formTemplateFileNames)
		{
			InputStream resourceAsStream = getClass().getResourceAsStream(formTemplateFileName);
			FormTemplate formTemplate = importFormTemplate(resourceAsStream);
			formTemplates.add(formTemplate);
		}
		
		return formTemplates;
	}

	private FormTemplate importFormTemplate(InputStream resourceAsStream) throws Exception, FutureVersionException, IOException
	{
		InputStreamWithSeek withSeek = new ByteArrayInputStreamWithSeek(convertToInputStreamWithSeek(resourceAsStream));
		try
		{
			FormTemplate formTemplate = new FormTemplate();
			formTemplate.importTemplate(getApp().getSecurity(), withSeek);

			return formTemplate;
		}
		finally
		{
			withSeek.close();
		}
	}

	private byte[] convertToInputStreamWithSeek(InputStream resourceAsStream) throws Exception
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try
		{
			int readBytes = -1;
			while ((readBytes = resourceAsStream.read()) != -1)
			{
				outputStream.write(readBytes);
			}

			return outputStream.toByteArray();
		}
		finally
		{
			outputStream.close();
		}
	}
	
	@FXML
	private void genericComboBoxSelectionChanged() throws Exception
	{
		if (genericTemplatesChoiceBox.getSelectionModel().isEmpty())
			return;
		
		FormTemplate genericFormTemplate = genericTemplatesChoiceBox.getSelectionModel().getSelectedItem();
		updateSelectedFormTemplateComponents(genericFormTemplate);
		genericTemplatesChoiceBox.getSelectionModel().clearSelection();
	}

	@FXML
	private void customDropDownSelectionChanged() throws Exception
	{
		if (customTemplatesChoiceBox.getSelectionModel().isEmpty())
			return;
		
		AbstractFxImportFormTemplateController selectedController = customTemplatesChoiceBox.getSelectionModel().getSelectedItem();
		importFromContacts(selectedController);
		customTemplatesChoiceBox.getSelectionModel().clearSelection();
	}
	
	@FXML
	private void radioButtonSelectionChanged()
	{
		genericTemplatesChoiceBox.getSelectionModel().clearSelection();
		customTemplatesChoiceBox.getSelectionModel().clearSelection();

		genericTemplatesChoiceBox.setVisible(genericRadioButton.isSelected());
		customTemplatesChoiceBox.setVisible(downloadCustomRadioButton.isSelected());
	}

	protected void importFromContacts(AbstractFxImportFormTemplateController controller) throws Exception
	{
		showControllerInsideModalDialog(controller);
		FormTemplate selectedTemplate = controller.getSelectedFormTemplate();
		updateSelectedFormTemplateComponents(selectedTemplate);
	}
	
	protected void updateSelectedFormTemplateComponents(FormTemplate template) throws Exception
	{
		selectedFormTemplateToSave = template;
		boolean shouldAllowFormTemplate = false;
		String loadFormTemplateMessage = "";
		if (template != null)
		{
			loadFormTemplateMessage = TokenReplacement.replaceToken(getLocalization().getFieldLabel("SuccessfullyImportedForm"), "#templateName", template.getTitle());
			shouldAllowFormTemplate = true;
		}
		
		selectedTemplateLabel.setText(loadFormTemplateMessage);
		selectedTemplateLabel.setVisible(shouldAllowFormTemplate);
		switchFormsLaterLabel.setVisible(shouldAllowFormTemplate);
	}
	
	protected void saveFormTemplate(FormTemplate template)
	{
		try
		{
			getApp().updateFormTemplate(template);
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}
	
	protected class CustomTemplatesSelectionChangedHandler implements ChangeListener<AbstractFxImportFormTemplateController>
	{
		@Override
		public void changed(ObservableValue<? extends AbstractFxImportFormTemplateController> observable, AbstractFxImportFormTemplateController oldValue, AbstractFxImportFormTemplateController newValue)
		{
			if (customTemplatesChoiceBox.getSelectionModel().isEmpty())
				return;
			
			try
			{
				AbstractFxImportFormTemplateController selectedController = customTemplatesChoiceBox.getSelectionModel().getSelectedItem();
				importFromContacts(selectedController);
				customTemplatesChoiceBox.getSelectionModel().clearSelection();
			}
			catch (Exception e)
			{
				MartusLogger.logException(e);
			}
		}
	}
	
	protected class GenericTemplatesSelectionChangedHandler implements ChangeListener<FormTemplate>
	{
		@Override
		public void changed(ObservableValue<? extends FormTemplate> observable, FormTemplate oldValue, FormTemplate newValue)
		{
			if (genericTemplatesChoiceBox.getSelectionModel().isEmpty())
				return;

			try
			{
				FormTemplate genericFormTemplate = genericTemplatesChoiceBox.getSelectionModel().getSelectedItem();
				updateSelectedFormTemplateComponents(genericFormTemplate);
				genericTemplatesChoiceBox.getSelectionModel().clearSelection();
			}
			catch (Exception e)
			{
				MartusLogger.logException(e);
			}
		}
	}
	
	private class NextButtonHandler implements EventHandler<ActionEvent>
	{
		public NextButtonHandler()
		{
		}

		@Override
		public void handle(ActionEvent event)
		{
			if (selectedFormTemplateToSave != null)
				saveFormTemplate(selectedFormTemplateToSave);
		}
	}
	
	protected class ControllerToStringConverter extends StringConverter<AbstractFxImportFormTemplateController>
	{
		@Override
		public String toString(AbstractFxImportFormTemplateController object)
		{
			return object.getLabel();
		}

		@Override
		public AbstractFxImportFormTemplateController fromString(String string)
		{
			return null;
		}
	}
	
	@FXML 
	protected ChoiceBox<FormTemplate> genericTemplatesChoiceBox;
	
	@FXML
	protected ChoiceBox<AbstractFxImportFormTemplateController> customTemplatesChoiceBox;
	
	@FXML
	protected RadioButton genericRadioButton;
	
	@FXML
	private RadioButton downloadCustomRadioButton;
	
	@FXML
	private Label switchFormsLaterLabel;
	
	@FXML
	private Label selectedTemplateLabel;
	
	@FXML
	private Label sidebarHintTemplates;
	
	protected FormTemplate selectedFormTemplateToSave;
}
