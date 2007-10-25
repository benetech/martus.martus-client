/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2007, Beneficent
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
package org.martus.client.swingui.fields.attachments;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.tablemodels.AttachmentTableModel;
import org.martus.common.bulletin.AttachmentProxy;

import com.jhlabs.awt.GridLayoutPlus;

abstract public class UiAttachmentComponent extends JPanel
{
	public UiAttachmentComponent(UiMainWindow mainWindowToUse)
	{
		GridLayoutPlus layout = new GridLayoutPlus(0, 1, 0, 0, 0, 0);
		setLayout(layout);
		
		mainWindow = mainWindowToUse;
		model = new AttachmentTableModel(mainWindow);

	}
	
	abstract AbstractAttachmentPanel createAttachmentPanel(int row);
	
	protected MartusLocalization getLocalization()
	{
		return mainWindow.getLocalization();
	}

	public void updateTable()
	{
		removeAll();
		JPanel headerContainer = new JPanel(new BorderLayout());
		headerContainer.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		headerContainer.add(createHeaderRow());
		add(headerContainer);
		for(int row = 0; row < model.getRowCount(); ++row)
		{
			add(createAttachmentPanel(row));
		}
	}

	public void addAttachment(AttachmentProxy a)
	{
		model.add(a);
		updateTable();
	}

	public void clearAttachments()
	{
		model.clear();
		updateTable();
	}


	protected AbstractAttachmentRow createHeaderRow()
	{
		return new AttachmentHeaderRow(getLocalization());
	}


	protected UiMainWindow mainWindow;
	protected AttachmentTableModel model;
}
