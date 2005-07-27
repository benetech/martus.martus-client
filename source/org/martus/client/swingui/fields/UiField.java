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

package org.martus.client.swingui.fields;

import java.awt.event.FocusListener;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.martus.client.core.LanguageChangeListener;

abstract public class UiField
{
	public UiField()
	{
		focusManager = new FocusManager(this);
	}
	
	public void initalize()
	{
		focusManager.addFocusableComponents();
	}
	
	public void validate() throws DataInvalidException 
	{
	}
	
	public void setListener(ChangeListener listener)
	{
	}
	
	public void setLanguageListener(LanguageChangeListener listener)
	{
	}
	
	public void addFocusListener(FocusListener listener)
	{
		focusManager.addFocusListener(listener);
	}
	
	abstract public JComponent getComponent();
	abstract public JComponent[] getFocusableComponents();
	abstract public String getText();
	abstract public void setText(String newText);

	public static class DataInvalidException extends Exception
	{
		public DataInvalidException()
		{
			localizedTag = null;
		}
		public DataInvalidException(String tag)
		{
			localizedTag = tag;
		}
		public String getlocalizedTag()
		{
			return localizedTag;
		}
		String localizedTag;
	}
	
	FocusManager focusManager;
}

