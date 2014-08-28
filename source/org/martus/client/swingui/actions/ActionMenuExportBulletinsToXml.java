/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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

package org.martus.client.swingui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Vector;

import org.martus.client.bulletinstore.ExportBulletins;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.packet.UniversalId;

public class ActionMenuExportBulletinsToXml extends UiMenuAction implements ActionDoer
{
	public ActionMenuExportBulletinsToXml(UiMainWindow mainWindowToUse, UniversalId[] bulletinsIdsToUse, File exportFileToUse, boolean includeAttachmentsToUse)
	{
		super(mainWindowToUse, "ExportBulletins");
		bulletinIdsToExport = bulletinsIdsToUse;
		exportFile = exportFileToUse;
		includeAttachments = includeAttachmentsToUse;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}
	
	@Override
	public void doAction()
	{
		try
		{
			exportBulletins(getMainWindow().getBulletins(bulletinIdsToExport));	
		} 
		catch (Exception e)
		{
			getMainWindow().unexpectedErrorDlg(e);
		}
	}
	
	private void exportBulletins(Vector bulletinsToExport)
	{
		try
		{
			ExportBulletins exporter = new ExportBulletins(mainWindow);
			exporter.doExport(exportFile.getAbsoluteFile(), bulletinsToExport, ALWAYS_EXPORT_PRIVATE_DATA, includeAttachments, SHOULD_EXPORT_ALL_VERSIONS);
		} 
		catch (Exception e)
		{
			getMainWindow().unexpectedErrorDlg(e);
		}
	}

	private final boolean ALWAYS_EXPORT_PRIVATE_DATA = true;
	private final boolean SHOULD_EXPORT_ALL_VERSIONS = false;
	private boolean includeAttachments;
	private UniversalId[] bulletinIdsToExport;
	private File exportFile;	
}