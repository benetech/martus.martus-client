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

import java.awt.Component;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

import org.martus.client.core.BulletinLanguageChangeListener;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.bulletincomponent.UiBulletinComponentInterface;
import org.martus.client.swingui.jfx.generic.FxNonWizardShellController;
import org.martus.client.swingui.jfx.landing.general.BulletinEditorHeaderShellController;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto.EncryptionException;
import org.martus.common.fieldspec.DataInvalidException;

public class FxBulletinEditorShellController extends FxNonWizardShellController implements UiBulletinComponentInterface
{
	public FxBulletinEditorShellController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		try
		{
			super.initialize(location, bundle);

			BulletinEditorHeaderShellController headerController = new BulletinEditorHeaderShellController(getMainWindow());
			loadControllerAndEmbedInPane(headerController, headerPane);
		}
		catch(Exception e)
		{
			unexpectedError(e);
		}
	}

	@Override
	public Component getComponent()
	{
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void scrollToTop()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void copyDataToBulletin(Bulletin bulletin) throws IOException,
			EncryptionException
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void copyDataFromBulletin(Bulletin bulletinToShow) throws Exception
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void validateData() throws DataInvalidException
	{
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isBulletinModified() throws Exception
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void updateEncryptedIndicator(boolean allPrivate)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void setLanguageChangeListener(BulletinLanguageChangeListener listener)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void bulletinLanguageHasChanged(String newBulletinLanguageCode)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/bulletins/BulletinEditor.fxml";
	}
	
	@FXML
	private Pane headerPane;
}
