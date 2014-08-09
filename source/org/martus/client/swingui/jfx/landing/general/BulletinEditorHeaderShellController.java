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

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.Property;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.swingui.actions.ActionDoer;
import org.martus.client.swingui.bulletincomponent.UiBulletinComponent;
import org.martus.client.swingui.jfx.generic.DialogWithCloseShellController;
import org.martus.client.swingui.jfx.generic.FxInSwingController;
import org.martus.client.swingui.jfx.generic.FxNonWizardShellController;

public class BulletinEditorHeaderShellController extends FxNonWizardShellController
{
	public BulletinEditorHeaderShellController(UiBulletinComponent view)
	{
		super(view.getMainWindow());
		
		bulletinComponent = view;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		
		ClientBulletinStore store = getApp().getStore();
		Property<String> currentTemplateName = store.getCurrentFormTemplateNameProperty();
		currentTemplateLabel.textProperty().bind(currentTemplateName);
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/general/BulletinEditorHeader.fxml";
	}

	@FXML
	private void onSelectTemplate(ActionEvent event) 
	{
		try
		{
			FxInSwingController controller = new SelectTemplateController(bulletinComponent);
			ActionDoer shellController = new DialogWithCloseShellController(getMainWindow(), controller);
			doAction(shellController);
		}
		catch (Exception e)
		{
			unexpectedError(e);
		}
	}
	
	@FXML
	private Label currentTemplateLabel;
	
	private UiBulletinComponent bulletinComponent;
}
