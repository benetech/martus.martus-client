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

import javafx.stage.FileChooser;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.clientside.FormatFilter;

abstract public class BaseExportController extends FxController
{
	public BaseExportController(UiMainWindow mainWindowToUse, String initialFileExportName)
	{
		super(mainWindowToUse);
		this.initialFileExportName = initialFileExportName;
	}

	protected String getInitialFileAbsolutePath()
	{
		File fullPathOfInitialLocation = new File(getRootDirectory(), initialFileExportName);
		return(fullPathOfInitialLocation.getAbsolutePath());
	}
	
	protected File getRootDirectory()
	{
		return getApp().getMartusDataRootDirectory();
	}
	
	protected File getFileSaveLocation(String FileChooserTitle, FormatFilter fileFilter)
	{
		//FIXME: This dialog can be hidden behind
		MartusLocalization localization = getLocalization();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(getRootDirectory());
		fileChooser.setInitialFileName(initialFileExportName);
		fileChooser.setTitle(localization.getWindowTitle(FileChooserTitle));
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter(fileFilter.getDescription(), fileFilter.getWildCardExtension()),
				new FileChooser.ExtensionFilter(localization.getFieldLabel("AllFiles"), "*.*"));
		File templateFile = fileChooser.showSaveDialog(null);
		return templateFile;
	}

	private String initialFileExportName;

}
