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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxInSwingController;
import org.martus.client.swingui.jfx.generic.controls.FxSwitchButton;
import org.martus.client.swingui.jfx.setupwizard.tasks.TorInitializationTask;
import org.martus.common.MartusLogger;

public class SettingsForTorController extends FxInSwingController
{
	public SettingsForTorController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		torSwitchButton = new FxSwitchButton();
		switchButtonPane.getChildren().add(torSwitchButton);

		Property<Boolean> configInfoUseInternalTorProperty = getApp().getConfigInfo().useInternalTorProperty();
		torSwitchButton.switchOnProperty().bindBidirectional(configInfoUseInternalTorProperty);
		torSwitchButton.switchOnProperty().addListener(new FxCheckboxListener());
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/general/SettingsForTor.fxml";
	}

	private final class FxCheckboxListener implements ChangeListener<Boolean>
	{
		public FxCheckboxListener()
		{
		}

		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) 
		{
			boolean didFinishInitalizing = startOrStopTorPerConfigInfo();
			if(newValue && !didFinishInitalizing)
				torSwitchButton.setSelected(false);
			getMainWindow().saveConfigInfo();
		}
	}

	protected boolean startOrStopTorPerConfigInfo()
	{
		TorInitializationTask task = new TorInitializationTask(getApp());
		try
		{
			showProgressDialog(getLocalization().getFieldLabel("SettingUpTor"), task);
			return true;
		}
		catch (UserCancelledException e)
		{
			return false;
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
			showNotifyDialog("UnexpectedError");
			return false;
		}
	}

	@FXML 
	private void OnLinkTorProject()
	{
		openLinkInDefaultBrowser("https://www.torproject.org");
	}

	@FXML
	protected Pane switchButtonPane;

	@FXML
	protected FxSwitchButton torSwitchButton;

}
