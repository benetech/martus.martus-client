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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.common.ContactKeys;
import org.martus.common.HeadquartersKey;
import org.martus.util.StreamableBase64.InvalidBase64Exception;

public class BulletinContactsController extends FxController
{

	public BulletinContactsController(UiMainWindow mainWindowToUse, Vector currentAuthorizedKeysToUse)
	{
		super(mainWindowToUse);
		currentAuthorizedKeys = currentAuthorizedKeysToUse;
		availableListAuthorizedToReadKeys = FXCollections.observableArrayList();
	}
	
	public Vector getCurrentAuthorizedKeys()
	{
		return currentAuthorizedKeys;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		try
		{
			ContactKeys ourContacts = getApp().getContactKeys();
			currentAuthorizedKeys.forEach(key -> addKeyToAvailableList(key, ourContacts));
			contactsList.setItems(availableListAuthorizedToReadKeys);
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	private void addKeyToAvailableList(HeadquartersKey key, ContactKeys ourContacts)
	{
		try
		{
			String contactsName = BulletinEditorHeaderController.getContactsName(getLocalization(), key, ourContacts);
			CheckBox contactCheckbox = new CheckBox(contactsName);
			availableListAuthorizedToReadKeys.add(contactCheckbox);
		} 
		catch (InvalidBase64Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	@Override
	public String getFxmlLocation()
	{
		return "/landing/bulletins/FxBulletinContacts.fxml";
	}

	@FXML
	private void onSelectAll(ActionEvent event) 
	{
		
	}

	@FXML
	private CheckBox selectAll;
	
	@FXML
	private ListView contactsList;
	
	private Vector<HeadquartersKey> currentAuthorizedKeys;
	private ObservableList<CheckBox> availableListAuthorizedToReadKeys;
	
}
