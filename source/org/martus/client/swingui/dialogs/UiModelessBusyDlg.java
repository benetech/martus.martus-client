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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.io.IOException;
import java.io.NotSerializableException;

import javax.swing.JDialog;
import javax.swing.border.LineBorder;

import org.martus.swing.UiLabel;
import org.martus.swing.Utilities;

public class UiModelessBusyDlg extends JDialog
{

	public UiModelessBusyDlg(String message)
	{
		super();
		getContentPane().add(new UiLabel(" "), BorderLayout.NORTH);
		getContentPane().add(new UiLabel(" "), BorderLayout.SOUTH);
		getContentPane().add(new UiLabel("     "), BorderLayout.EAST);
		getContentPane().add(new UiLabel("     "), BorderLayout.WEST);
		getContentPane().add(new UiLabel(message), BorderLayout.CENTER);
		getRootPane().setBorder(new LineBorder(Color.black, 5));
		setUndecorated(true);
		Utilities.centerDlg(this);
		setResizable(false);
		origCursor = getCursor();
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		setVisible(true);
	}

	public void endDialog()
	{
		setCursor(origCursor);
		dispose();
	}


	// This class is NOT intended to be serialized!!!
	private static final long serialVersionUID = 1;
	private void writeObject(java.io.ObjectOutputStream stream) throws IOException
	{
		throw new NotSerializableException();
	}

	Cursor origCursor;
}
