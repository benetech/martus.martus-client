/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2003, Beneficent
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
package org.martus.client.swingui;

import java.awt.Toolkit;
import java.util.Arrays;

import javax.swing.JPasswordField;


public class UiPasswordField extends JPasswordField
{
	public UiPasswordField(int columns)
	{
		super(columns);
		virtualPassword = new char[MAX_PASSWORD_LENGTH];
		clearVirtualPassword();
	}
	
	public void clearVirtualPassword()
	{
		scrubData(virtualPassword);
		virtualPasswordLength = 0;
	}

	public char[] getPassword()
	{
		if(inVirtualMode)
		{	
			char[] virtual = getVirtualPassword();
			clearVirtualPassword();
			return virtual;
		}
		return super.getPassword();
	}

	
	public void setVirtualMode(boolean virtualMode)
	{
		inVirtualMode = virtualMode;
		if(inVirtualMode)
		{
			super.setEditable(false);
			char[] tempPassword = super.getPassword();
			virtualPasswordLength = tempPassword.length;
			System.arraycopy(tempPassword, 0, virtualPassword, 0, virtualPasswordLength);
			scrubData(tempPassword);
			updateFakeVisualPassword();
			return;
		}
		
		super.setEditable(true);
		if(virtualPasswordLength == 0)
		{
			super.setText("");
			return;
		}

		char[] exactSizePassword = getVirtualPassword();
		super.setText(new String(exactSizePassword));
		clearVirtualPassword();
	}
	
	private char[] getVirtualPassword()
	{
		char[] exactSizePassword = new char[virtualPasswordLength];
		System.arraycopy(virtualPassword,0,exactSizePassword,0,virtualPasswordLength);
		return exactSizePassword;
	}

	public void appendChar(char charToAppend)
	{
		if(!inVirtualMode || virtualPasswordLength+1 >= MAX_PASSWORD_LENGTH)
		{
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		virtualPassword[virtualPasswordLength] = charToAppend;
		++virtualPasswordLength;
		updateFakeVisualPassword();
	}

	public void deleteLastChar()
	{
		if(!inVirtualMode || virtualPasswordLength == 0)
		{
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		--virtualPasswordLength;
		updateFakeVisualPassword();
	}

	private void updateFakeVisualPassword()
	{
		String fakePasswordData = "";
		for(int i = 0; i<virtualPasswordLength; ++i)
			fakePasswordData += "*";
		super.setText(fakePasswordData);
	}

	private void scrubData(char[] data)
	{
		final char scrubDataByte = 0x55;
		Arrays.fill(data, scrubDataByte);
	}
	
	final int MAX_PASSWORD_LENGTH = 1024;
	private boolean inVirtualMode;
	private char[] virtualPassword;
	private int virtualPasswordLength;
}
