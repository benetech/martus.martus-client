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

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;

import org.martus.client.swingui.UiMainWindow;
import org.martus.common.fieldspec.CustomFieldTemplate;

public class FxImportFormTemplateFromMyContactsPopupController extends AbstractFxImportFormTemplateController
{
	public FxImportFormTemplateFromMyContactsPopupController(UiMainWindow mainWindow)
	{
		super(mainWindow);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		
		continueButton.setVisible(false);
		continueLabel.setVisible(false);
	}

	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/step5/SetupImportTemplateFromMyContactsPopup.fxml";
	}

	@Override
	public String getDialogTitle()
	{
		return getLocalization().getWindowTitle("notifyImportTemplate"); 
	}

	@FXML
	private void onCancel()
	{
		getStage().close();
	}
	
	@FXML
	private void onContinue()
	{
		onCancel();
	}
	
	@Override
	public String getLabel()
	{
		return "Import from My Contacts";
	}

	@Override
	public CustomFieldTemplate getSelectedFormTemplate()
	{
		return null;
	}
	
	@FXML
	private ChoiceBox contactsChoiceBox;
	
	@FXML
	private ChoiceBox templatesChoiceBox;
	
	@FXML
	private Label continueLabel;
	
	@FXML
	private Button continueButton;
}
