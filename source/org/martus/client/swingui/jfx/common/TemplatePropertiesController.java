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
package org.martus.client.swingui.jfx.common;

import java.awt.Dimension;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.generic.FxModalDialog;
import org.martus.common.fieldspec.FormTemplate;

public class TemplatePropertiesController extends FxController
{
	public TemplatePropertiesController(UiMainWindow mainWindowToUse, FormTemplate templateToEdit)
	{
		super(mainWindowToUse);
		template = templateToEdit;
	}

	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		String existingTitle = template.getTitle();
		templateTitle.setText(existingTitle);
		templateTitle.textProperty().addListener(new TitleChangeHandler());
		String existingDescription = template.getDescription();
		templateDescription.setText(existingDescription);
	}
	
	protected class TitleChangeHandler implements ChangeListener
	{
		@Override
		public void changed(ObservableValue observable, Object oldValue, Object newValue)
		{
			updateOkButtonStatus();
		}

	}
	
	protected void updateOkButtonStatus()
	{
		try
		{
			String oldTitle = template.getTitle();
			String newTitle = getTemplateTitle();
			boolean doesTitleAlreadyExist = getApp().getStore().doesFormTemplateExist(newTitle);
			boolean isTitleChanged = !newTitle.equals(oldTitle);
			boolean isNewTitleIllegal = isTitleChanged && doesTitleAlreadyExist;

			setOkButtonDisabled(isNewTitleIllegal);
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	private void setOkButtonDisabled(boolean shouldBeDisabled)
	{
		FxController topLevelController = getTopLevelController();
		Button okButton = topLevelController.getOkButton();
		okButton.setDisable(shouldBeDisabled);
	}
	
	public String getTemplateTitle()
	{
		return templateTitle.getText();
	}

	public String getTemplateDescription()
	{
		return templateDescription.getText();
	}

	@Override
	public String getFxmlLocation()
	{
		return "common/TemplateProperties.fxml";
	}

	@Override
	protected Dimension getPreferredDimension()
	{
		//TODO fix this is not getting called
		return FxModalDialog.MEDIUM_SMALL_PREFERRED_DIALOG_SIZE;
	}

	@FXML
	private TextField templateTitle;
	
	@FXML
	private TextField templateDescription;
	
	private FormTemplate template;

}
