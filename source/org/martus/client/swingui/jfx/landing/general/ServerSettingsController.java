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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SingleSelectionModel;

import org.martus.client.core.MartusApp;
import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.generic.data.ObservableChoiceItemList;
import org.martus.common.MartusLogger;
import org.martus.common.fieldspec.ChoiceItem;

public class ServerSettingsController extends FxController
{
	public ServerSettingsController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		
		ObservableList<ChoiceItem> choices = createChoices();
		automaticSyncFrequency.setItems(choices);
		String currentCode = getApp().getConfigInfo().getSyncFrequency();
		selectByCode(automaticSyncFrequency, currentCode);
		automaticSyncFrequency.getSelectionModel().selectedItemProperty().addListener(new SyncFrequencyChangeHandler(getApp()));
	}
	
	static class SyncFrequencyChangeHandler implements ChangeListener<ChoiceItem>
	{
		public SyncFrequencyChangeHandler(MartusApp appToUse)
		{
			app = appToUse;
		}
		
		@Override
		public void changed(ObservableValue<? extends ChoiceItem> observable, ChoiceItem oldValue, ChoiceItem newValue)
		{
			try
			{
				app.getConfigInfo().setSyncFrequency(newValue.getCode());
				app.saveConfigInfo();
			} 
			catch (SaveConfigInfoException e)
			{
				// FIXME: Need to let user know this failed
				MartusLogger.logException(e);
			}
		}
		
		private MartusApp app;
	}

	private static void selectByCode(ChoiceBox choiceBox, String codeToFind)
	{
		ObservableChoiceItemList choices = new ObservableChoiceItemList(choiceBox.getItems());
		ChoiceItem current = choices.findByCode(codeToFind);
		SingleSelectionModel model = choiceBox.getSelectionModel();
		model.select(current);
	}

	private ObservableList<ChoiceItem> createChoices()
	{
		ObservableList<ChoiceItem> choices = new ObservableChoiceItemList();

		choices.add(new ChoiceItem(NEVER, getLocalization().getFieldLabel("SyncFrequencyNever")));
		choices.add(new ChoiceItem(ON_STARTUP, getLocalization().getFieldLabel("SyncFrequencyOnStartup")));
		choices.add(new ChoiceItem("60", getLocalization().getFieldLabel("SyncFrequencyOneHour")));
		choices.add(new ChoiceItem("15", getLocalization().getFieldLabel("SyncFrequencyFifteenMinutes")));
		choices.add(new ChoiceItem("2", getLocalization().getFieldLabel("SyncFrequencyTwoMinutes")));
		
		return choices;
	}
	
	@Override
	public String getFxmlLocation()
	{
		return "landing/general/SettingsForServer.fxml";
	}
	
	public final static String NEVER = "";
	public final static String ON_STARTUP = "OnStartup";

	@FXML
	private ChoiceBox automaticSyncFrequency;
}
