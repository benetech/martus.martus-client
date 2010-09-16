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

import org.martus.common.MiniLocalization;
import org.martus.common.ReusableChoices;
import org.martus.common.fieldspec.ChoiceItem;
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
		for(int level = 0; level < setsOfChoices.length; ++level)
		{
			String displayText = "";

			ChoiceItem[] choices = setsOfChoices[level].getChoices();
			int LAST = setsOfChoices.length - 1;
			if(level == LAST)
			{
				displayText = findLabelByCode(choices, newText);
			}
			else
			{
				displayText = findLabelByPartialCode(choices, newText);
			}
			
			UiLabel widget = new UiLabel();
			widget.setText(" " + displayText + " ");
			widget.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			container.add(widget);
		}
	}

	private String findLabelByCode(ChoiceItem[] choices, String code)
	{
		for(int index = 0; index < choices.length; ++index)
			if(code.equals(choices[index].getCode()))
				return choices[index].toString();
		
		return "";
	}

	private String findLabelByPartialCode(ChoiceItem[] choices, String code)
	{
		for(int index = 0; index < choices.length; ++index)
		{
			String thisCode = choices[index].getCode();
			if(thisCode.length() > 0 && code.startsWith(thisCode))
				return choices[index].toString();
		}
		
		return "";
	}

	public JComponent getComponent()
	{
		return container;
	}

	public JComponent[] getFocusableComponents()
	{
		return new JComponent[0];
	}

	public void setChoices(ReusableChoices[] newChoices)
	{
		setsOfChoices = newChoices;
	}

	private Box container;
	private ReusableChoices[] setsOfChoices;
}
