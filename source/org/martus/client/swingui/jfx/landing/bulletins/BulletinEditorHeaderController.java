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
package org.martus.client.swingui.jfx.landing.bulletins;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import org.martus.client.core.FxBulletin;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionDoer;
import org.martus.client.swingui.jfx.generic.DialogWithNoButtonsShellController;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.generic.data.FxBindingHelpers;
import org.martus.client.swingui.jfx.landing.general.SelectTemplateController;
import org.martus.common.ContactKeys;
import org.martus.common.HeadquartersKey;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.util.StreamableBase64.InvalidBase64Exception;

public class BulletinEditorHeaderController extends FxController
{
	public BulletinEditorHeaderController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		try
		{
			contactKeys = getApp().getContactKeys();
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}

	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/bulletins/BulletinEditorHeader.fxml";
	}

	public void showBulletin(FxBulletin bulletinToShow)
	{
		updateTitle(bulletinToShow);
		updateVersion(bulletinToShow);			
		updateFrom(bulletinToShow);
		updateTo(bulletinToShow);
	}

	private void updateTo(FxBulletin bulletinToShow)
	{
		authorizedToContacts = bulletinToShow.authorizedToReadList();
		authorizedToContacts.forEach(key -> AddKeyToField(key));
	}
	
	private void AddKeyToField(HeadquartersKey key)
	{
		String currentContacts = toField.getText();
		
		if(contactKeys.containsKey(key.getPublicKey()))
		{
			try
			{
				String contactName = contactKeys.getLabelIfPresent(key.getPublicKey());
				if(contactName.isEmpty())
					contactName = key.getFormattedPublicCode();

				if(!currentContacts.isEmpty())
					currentContacts += ", ";
				currentContacts += contactName;
				toField.setText(currentContacts);
			} 
			catch (InvalidBase64Exception e)
			{
				logAndNotifyUnexpectedError(e);
			}
		}
		else
		{
			//TODO What should we do?  Include someone we are not connected with?
		}
	}

	private void updateFrom(FxBulletin bulletinToShow)
	{
		String accountKey = bulletinToShow.universalIdProperty().get().getAccountId();
		String formattedAccountLabel = getMainWindow().getApp().getUserName();
		try
		{
			formattedAccountLabel += " (";
			formattedAccountLabel += MartusCrypto.computeFormattedPublicCode40(accountKey);
			formattedAccountLabel += ")";
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
		fromField.setText(formattedAccountLabel);
	}

	private void updateVersion(FxBulletin bulletinToShow)
	{
		versionField.setText(String.valueOf(bulletinToShow.getVersionProperty().get()));
	}

	private void updateTitle(FxBulletin bulletinToShow)
	{
		StringProperty newTitleProperty = bulletinToShow.getFieldProperty(Bulletin.TAGTITLE);
		titleProperty = FxBindingHelpers.bindToOurPropertyField(newTitleProperty, titleField.textProperty(), titleProperty);
		headerTitleLabel.textProperty().bind(titleProperty);
	}

	@FXML
	private void onSelectTemplate(ActionEvent event) 
	{
		try
		{
			FxController controller = new SelectTemplateController(getMainWindow());
			ActionDoer shellController = new DialogWithNoButtonsShellController(getMainWindow(), controller);
			doAction(shellController);
		}
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}
	
	@FXML
	private void onAddRemoveContact(ActionEvent event) 
	{

	}
	
	@FXML
	Label headerTitleLabel;

	@FXML
	TextField titleField;
	
	@FXML
	TextField toField;

	@FXML
	TextField fromField;
	
	@FXML
	Label versionField;
	
	private Property titleProperty;
	private ObservableList<HeadquartersKey> authorizedToContacts;
	private ContactKeys contactKeys;
}
