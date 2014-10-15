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
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.activation.MimetypesFileTypeMap;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxController;

public class AttachmentViewController extends FxController
{
	public AttachmentViewController(UiMainWindow mainWindowToUse, File attachmentFile)
	{
		super(mainWindowToUse);
		try
		{
			attachmentFileToView = attachmentFile;
			attachmentFileType = determineFileType(attachmentFileToView);
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		} 
	}

	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		try
		{
			setAttachmentInView();
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	public boolean canViewInProgram()
	{
		return attachmentFileType != FileType.Unsupported;
	}
	
	private FileType determineFileType(File file) throws IOException
	{
		MimetypesFileTypeMap mimeTypeMap = new MimetypesFileTypeMap();
		mimeTypeMap.addMimeTypes("html htm html");
		mimeTypeMap.addMimeTypes("image png tif jpg jpeg bmp");
		String mimetype = mimeTypeMap.getContentType(file);
        String type = mimetype.split("/")[0].toLowerCase();
        if(type.equals("html"))
        		return FileType.HTML;
        else if(type.equals("image"))
			return FileType.Image;
		return FileType.Unsupported;
	}

	private void setAttachmentInView() throws Exception
	{
		if(attachmentFileType == FileType.HTML || 
				attachmentFileType == FileType.Image)
			attachmentStackPane.getChildren().add(getWebView());
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
