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
package org.martus.client.swingui.jfx.setupwizard;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.FxPopupController;
import org.martus.common.fieldspec.ChoiceItem;

public class FxSetupTemplateFromNewContactPopupController extends FxPopupController implements Initializable
{
	public FxSetupTemplateFromNewContactPopupController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		importFromThisConctactLabel.setVisible(false);
		importFromUserComboBox.setVisible(false);
		formsFromUserMessageLabel.setVisible(false);
		continueButton.setVisible(false);
	}

	@FXML
	private void onCancel()
	{
		getStage().close();
	}
	
	@FXML
	private void onSeeForms()
	{
	}
	
	@FXML
	private void onContinue()
	{
	}

	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/SetupTemplateFromNewContactPopup.fxml";
	}

	@Override
	public String getDialogTitle()
	{
		return getLocalization().getWindowTitle("notifyImportTemplate"); 
	}

	@FXML
	private Label formsFromUserMessageLabel;
	
	@FXML
	private ComboBox<ChoiceItem> importFromUserComboBox;
	
	
	@FXML
	private Label importFromThisConctactLabel;
	
	@FXML
	private Button continueButton;

}
