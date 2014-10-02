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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.attachments.ViewAttachmentHandler;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.common.bulletin.AttachmentProxy;

public class AttachmentViewController extends FxController
{
	public AttachmentViewController(UiMainWindow mainWindowToUse, AttachmentProxy proxyToView)
	{
		super(mainWindowToUse);
		attachmentProxy = proxyToView;
	}

	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		try
		{
			addAttachmentToView();
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	private void addAttachmentToView() throws Exception
	{
		File attachmentFileToView = ViewAttachmentHandler.getAttachmentAsFile(attachmentProxy, getApp().getStore());
		Node view = null;
		if(isAttachmentHTML(attachmentFileToView))
			view = getWebView(attachmentFileToView);
		else
			view = getImageView(attachmentFileToView);
		attachmentStackPane.getChildren().add(view);
	}

	private boolean isAttachmentHTML(File attachmentFile)
	{
		String attachmentName = attachmentFile.getName().toLowerCase();
		if(attachmentName.endsWith(".htm"))
			return true;
		if(attachmentName.endsWith(".html"))
			return true;
		return false;
	}

	private ImageView getImageView(File attachmentFileToView)
			throws MalformedURLException
	{
		Image attachmentImage = new Image(attachmentFileToView.toURI().toURL().toString());
		ImageView attachmentView = new ImageView(attachmentImage);
		return attachmentView;
	}

	private WebView getWebView(File attachmentFileToView)
	{
		WebView webView = new WebView();
		WebEngine engine = webView.getEngine();
		engine.load(attachmentFileToView.toURI().toString());
		return webView;
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/bulletins/AttachmentViewer.fxml";
	}
	
	@FXML
	private StackPane attachmentStackPane;
	private AttachmentProxy attachmentProxy;
}
