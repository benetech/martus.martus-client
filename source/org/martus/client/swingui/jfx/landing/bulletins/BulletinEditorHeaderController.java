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
import java.util.Vector;

import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import org.martus.client.core.FxBulletin;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionDoer;
import org.martus.client.swingui.jfx.generic.DialogWithNoButtonsShellController;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.generic.data.FxBindingHelpers;
import org.martus.client.swingui.jfx.landing.general.SelectTemplateController;
import org.martus.common.ContactKeys;
import org.martus.common.EnglishCommonStrings;
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
		try
		{
			ContactKeys ourContacts = getApp().getContactKeys();
			authorizedToContacts = bulletinToShow.authorizedToReadList();
			Vector listOfAuthorizedAccounts = new Vector();
			authorizedToContacts.forEach(key -> AddKeyToField(key, ourContacts, listOfAuthorizedAccounts));
			toField.setText(String.join(getLocalization().getFieldLabel("ContactNamesSeparator"), listOfAuthorizedAccounts));
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}
	
	private void AddKeyToField(HeadquartersKey key, ContactKeys ourContacts, Vector currentListOfAccounts)
	{
		try
		{
			currentListOfAccounts.add(getContactsName(getLocalization(), key, ourContacts));
		} 
		catch (InvalidBase64Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	static public String getContactsName(MartusLocalization localization, HeadquartersKey key, ContactKeys ourContacts) throws InvalidBase64Exception  
	{
		String contactName = ourContacts.getLabelIfPresent(key.getPublicKey());
		if(contactName.isEmpty())
		{
			contactName += addWarningIfNotInContacts(localization, key, ourContacts);
			contactName += key.getFormattedPublicCode();
		}
		return contactName;
	}

	static private String addWarningIfNotInContacts(MartusLocalization localization, HeadquartersKey key, ContactKeys ourContacts)
	{
		if(ourContacts.containsKey(key.getPublicKey()))
			return "";

		String notInContactsWarning = localization.getFieldLabel("HQNotConfigured");
		notInContactsWarning = " ";
		return notInContactsWarning;
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
		Vector currentAuthorizedKeys = new Vector();
		authorizedToContacts.forEach(key -> currentAuthorizedKeys.add(key));
		BulletinContactsController contactsController = new BulletinContactsController(getMainWindow(), currentAuthorizedKeys);
		if(showModalYesNoDialog("BulletinContacts", EnglishCommonStrings.OK, EnglishCommonStrings.CANCEL, contactsController))
		{
			
		}
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
}
