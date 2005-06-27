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

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComponent;

import org.martus.client.swingui.UiConstants;
import org.martus.client.swingui.UiLocalization;
import org.martus.swing.UiTextArea;

public class UiNormalTextEditor extends UiNormalTextField
{
	public UiNormalTextEditor(UiLocalization localizationToUse)
	{
		super(localizationToUse);
		widget = new UiTextAreaWithPadding(1, UiConstants.textFieldColumns);
		widget.setLineWrap(true);
		widget.setWrapStyleWord(true);
		widget.setFont(new Font("SansSerif", Font.PLAIN, UiConstants.defaultFontSize));
		widget.addKeyListener(new myKeyListener());
		supportContextMenu();
	}
	
	class UiTextAreaWithPadding extends UiTextArea
	{
		public UiTextAreaWithPadding(int rows, int cols)
		{
			super(rows, cols);
		}

	}
	
	public JComponent[] getFocusableComponents()
	{
		return new JComponent[]{widget};
	}
	
	class myKeyListener implements KeyListener
	{
		public void keyPressed(KeyEvent e)
		{
		}
		public void keyReleased(KeyEvent e)
		{
		}
		public void keyTyped(KeyEvent e)
		{
			widget.repaint(); //Java Bug to fix Arabic subscripts getting clipped.
		}
	}
}


