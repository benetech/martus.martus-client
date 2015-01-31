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
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.activation.MimetypesFileTypeMap;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.common.MartusLogger;
import org.martus.common.utilities.GeoTag;
import org.martus.common.utilities.JpegGeoTagReader;
import org.martus.common.utilities.JpegGeoTagReader.NotJpegException;

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
        if(type.equals("image"))
			return FileType.Image;
//        else if(type.equals("html")) //TODO: Add back once we are confident we can view HTML securly inside Martus
//   			return FileType.HTML;        
		return FileType.Unsupported;
	}

	private void setAttachmentInView() throws Exception
	{
		if(attachmentFileType == FileType.HTML || 
				attachmentFileType == FileType.Image)
			attachmentStackPane.getChildren().add(getWebView());
		
		GeoTag tag = readGeoTag();
		showMapButton.setVisible(tag.hasData());
	}

	private GeoTag readGeoTag() throws Exception
	{
		InputStream in = attachmentFileToView.toURI().toURL().openStream();
		try
		{
			JpegGeoTagReader reader = new JpegGeoTagReader();
			GeoTag tag = reader.readMetadata(in);
			return tag;
		}
		catch(NotJpegException e)
		{
			// NOTE: this is harmless
			return new GeoTag();
		}
		catch(Exception e)
		{
			logAndNotifyUnexpectedError(e);
			return new GeoTag();
		}
		finally
		{
			in.close();
		}
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
	
	@FXML
	private void showOnMap()
	{
		// NOTE: We can't call notifyDlg here, possibly because we are inside a modal dialog
		try
		{
			MartusLogger.log("Map URL: " + createMapRequestUrl());
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private String createMapRequestUrl() throws Exception
	{
		int zoomFactor = 14;
		GeoTag tag = readGeoTag();
		String baseUrl = "https://maps.googleapis.com/maps/api/staticmap";
		String marker = "markers=%7C" + tag.getLatitude() + "," + tag.getLongitude();
		String size = "size=640x640";
		String zoom = "zoom=" + zoomFactor;
		return baseUrl + "?" + 	marker + "&" + size + "&" + zoom;
	}

	enum FileType{Unsupported, Image, HTML};
	
	@FXML
	private VBox attachmentStackPane;

	@FXML
	private Button showMapButton;
	
	private File attachmentFileToView;
	private FileType attachmentFileType;
}
