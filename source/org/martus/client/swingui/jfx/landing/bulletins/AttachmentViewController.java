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
import java.io.IOException;
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

import javax.activation.MimetypesFileTypeMap;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.attachments.ViewAttachmentHandler;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.common.bulletin.AttachmentProxy;

public class AttachmentViewController extends FxController
{
	public AttachmentViewController(UiMainWindow mainWindowToUse, AttachmentProxy proxyToView)
	{
		super(mainWindowToUse);
		attachmentFileType = FileType.Unsupported;
		try
		{
			attachmentFileToView = ViewAttachmentHandler.getAttachmentAsFile(proxyToView, getApp().getStore());
			determineFileType();
		} 
		catch (Exception e)
		{
			errorLogNotifyAndCleanup(e);
		} 
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
			errorLogNotifyAndCleanup(e);
		}
	}

	private void errorLogNotifyAndCleanup(Exception e)
	{
		attachmentFileType = FileType.Unsupported;
		logAndNotifyUnexpectedError(e);
	}

	public boolean canViewInProgram()
	{
		return attachmentFileType != FileType.Unsupported;
	}
	
	private void determineFileType() throws IOException
	{
		MimetypesFileTypeMap mimeTypeMap = new MimetypesFileTypeMap();
		mimeTypeMap.addMimeTypes("html htm html");
		mimeTypeMap.addMimeTypes("image png tif jpg jpeg bmp");
		String mimetype = mimeTypeMap.getContentType(attachmentFileToView);
        String type = mimetype.split("/")[0].toLowerCase();
        if(type.equals("html"))
        		attachmentFileType = FileType.HTML;
        else if(type.equals("image"))
			attachmentFileType = FileType.Image;
		else
			attachmentFileType = FileType.Unsupported;
	}

	private void addAttachmentToView() throws Exception
	{
		Node view = null;
		
		if(attachmentFileType == FileType.HTML)
			view = getWebView();
		else if(attachmentFileType == FileType.Image)
			view = getImageView();
		else
			return;
		attachmentStackPane.getChildren().add(view);
	}
	
	private ImageView getImageView() throws MalformedURLException
	{
		Image attachmentImage = new Image(attachmentFileToView.toURI().toURL().toString());
		ImageView attachmentView = new ImageView(attachmentImage);
		return attachmentView;
	}

	private WebView getWebView()
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
	
	enum FileType{Unsupported, Image, HTML};
	
	@FXML
	private StackPane attachmentStackPane;

	private File attachmentFileToView;
	private FileType attachmentFileType;
}
