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
import java.util.Comparator;
import java.util.ResourceBundle;

import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.templates.FormTemplateManager;
import org.martus.client.search.SaneCollator;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxInSwingController;
import org.martus.client.swingui.jfx.generic.data.ObservableChoiceItemList;
import org.martus.common.fieldspec.ChoiceItem;

public class SelectTemplateController extends FxInSwingController
{
	public SelectTemplateController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		
		ClientBulletinStore store = getBulletinStore();
		ObservableSet<String> templateNames = store.getAvailableTemplates();
		ObservableChoiceItemList templateChoiceItems = new ObservableChoiceItemList();
		templateNames.forEach(name -> templateChoiceItems.add(createTemplateChoiceItem(name)));
		Comparator<ChoiceItem> sorter = new SaneCollator(getLocalization().getCurrentLanguageCode());
		templateChoiceItems.sort(sorter);
		availableTemplates.setItems(templateChoiceItems);
		updateSelectionFromReality();
	}

	public void updateSelectionFromReality()
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
			unexpectedError(e);
		}
	}

	public ClientBulletinStore getBulletinStore()
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

	@Override
	public String getFxmlLocation()
	{
		return "landing/general/SelectTemplate.fxml";
	}
	
	@FXML
	public void onSelect(ActionEvent event)
	{
		ChoiceItem selected = availableTemplates.getSelectionModel().getSelectedItem();
		try
		{
			getBulletinStore().setFormTemplate(selected.getCode());
		}
		catch(Exception e)
		{
			unexpectedError(e);
		}
	}

	@FXML
	private ListView<ChoiceItem> availableTemplates;
}
