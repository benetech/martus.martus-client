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

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;

import org.martus.client.core.FxBulletin;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxController;

public class BulletinViewerController extends FxController
{
	public BulletinViewerController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		FxFormCreator creator = new FxFormViewCreator(getLocalization());
		StackPane attachmentsPane = createAttachmentsPane(bulletin);
		StackPane detailsPane = createDetailsPane(bulletin);
		Node root = creator.createFormFromBulletin(bulletin, attachmentsPane, detailsPane);
		scrollPane.setContent(root);
		scrollPane.setFitToWidth(true);
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/bulletins/BulletinViewerBody.fxml";
	}

	public void setBulletin(FxBulletin bulletinToShow) throws RuntimeException
	{
		bulletin = bulletinToShow;
	}

	private StackPane createAttachmentsPane(FxBulletin bulletinToShow)
	{
		StackPane attachmentsPane = new StackPane();
		try
		{
			BulletinAttachmentsController attachmentsController = new BulletinAttachmentsController(getMainWindow(), bulletinToShow);
			loadControllerAndEmbedInPane(attachmentsController, attachmentsPane);
			attachmentsController.setViewingAttachmentsOnly();
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
		return attachmentsPane;
	}
	
	private StackPane createDetailsPane(FxBulletin bulletinToShow)
	{
		StackPane detailsPane = new StackPane();
		try
		{
			BulletinDetailsController detailsController = new BulletinDetailsController(getMainWindow(), bulletinToShow);
			loadControllerAndEmbedInPane(detailsController, detailsPane);
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
		return detailsPane;
	}

	public void scrollToTop()
	{
		scrollPane.vvalueProperty().set(0);
	}

		
	@FXML
	private ScrollPane scrollPane;

	private FxBulletin bulletin;
}
