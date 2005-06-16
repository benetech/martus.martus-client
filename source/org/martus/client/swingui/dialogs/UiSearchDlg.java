/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2005, Beneficent
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

package org.martus.client.swingui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.NotSerializableException;

import javax.swing.JButton;
import javax.swing.JDialog;

import org.martus.client.swingui.UiMainWindow;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.swing.UiButton;
import org.martus.swing.Utilities;

public abstract class UiSearchDlg extends JDialog  implements ActionListener
{
	public UiSearchDlg(UiMainWindow owner)
	{
		super(owner, "", true);
		localization = owner.getLocalization();

		searchButton = createBody(localization);
		
		Utilities.centerDlg(this);
		setResizable(true);
		setVisible(true);
	}
	
	abstract UiButton createBody(UiBasicLocalization localization);
	abstract public String getSearchString();
	abstract void memorizeSearch();

	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource() == searchButton)
		{
			memorizeSearch();
			result = true;
		}
		dispose();
	}


	public boolean getResults()
	{
		return result;
	}

	public UiBasicLocalization getLocalization()
	{
		return localization;
	}

	// This class is NOT intended to be serialized!!!
	private static final long serialVersionUID = 1;
	private void writeObject(java.io.ObjectOutputStream stream) throws IOException
	{
		throw new NotSerializableException();
	}


	boolean result;
	UiBasicLocalization localization;
	
	protected JButton searchButton;

}
