/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
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

package org.martus.client.swingui.fields;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;

import org.martus.common.ListOfReusableChoicesLists;
import org.martus.common.MiniLocalization;
import org.martus.swing.UiLabel;

public class UiChoiceViewer extends UiChoice
{
	public UiChoiceViewer(MiniLocalization localizationToUse)
	{
		super(localizationToUse);
		container = Box.createHorizontalBox();
	}
	
	public String getText()
	{
		return "";
	}

	public void setText(String newText)
	{
		container.removeAll();
		String[] displayText = reusableChoicesLists.getDisplayValuesAtAllLevels(newText);
		
		for(int level = 0; level < reusableChoicesLists.size(); ++level)
		{
			UiLabel widget = new UiLabel();
			if(displayText[level].length() > 0)
			{
				widget.setText(" " + displayText[level] + " ");
				if(reusableChoicesLists.size() > 1)
					widget.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				container.add(widget);
			}
		}
	}

	public JComponent getComponent()
	{
		return container;
	}

	public JComponent[] getFocusableComponents()
	{
		return new JComponent[0];
	}

	public void setChoices(ListOfReusableChoicesLists newChoices)
	{
		reusableChoicesLists = newChoices;
	}

	private Box container;
	private ListOfReusableChoicesLists reusableChoicesLists;
}
