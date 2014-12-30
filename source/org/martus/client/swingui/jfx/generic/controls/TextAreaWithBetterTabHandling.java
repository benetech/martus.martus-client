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
package org.martus.client.swingui.jfx.generic.controls;

import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;

import com.sun.javafx.scene.control.behavior.TextAreaBehavior;
import com.sun.javafx.scene.control.skin.TextAreaSkin;

public class TextAreaWithBetterTabHandling extends TextArea
{
	public TextAreaWithBetterTabHandling()
	{
		addEventFilter(KeyEvent.KEY_TYPED, new KeyTypedEventHandler());
		// NOTE: Since JavaFX 8 still inserts the TAB even if we consume the event, 
		// we need to manually remove the TAB after it is inserted.
		textProperty().addListener((changeEvent) -> removeAllTabs(this));
	}

	class KeyTypedEventHandler implements EventHandler<KeyEvent>
	{
		public void handle(KeyEvent event)
		{
			String keyCode = event.getCharacter();
			if(keyCode.equals("\t"))
			{
				onTabTyped(event);
			}
		}

		private void onTabTyped(KeyEvent event)
		{
			if(event.isAltDown() || event.isControlDown() || event.isShiftDown())
				return;
			
			TextArea targetNode = (TextArea)event.getTarget();
			TextAreaSkin skin = (TextAreaSkin)targetNode.getSkin();
			TextAreaBehavior behavior = skin.getBehavior();
			behavior.callAction("TraverseNext");

			// NOTE: This consume won't actually help, apparently because 
			// of the way Java FX 8 uses a KeyBinding, which seems to ignore 
			// whether or not the event was consumed. 
			event.consume();
		}
	}

	private void removeAllTabs(TextArea textArea)
	{
		String oldText = textArea.getText();
		String newText = oldText.replace("\t", "");
		if(!newText.equals(oldText))
			textArea.setText(newText);
	}

}
