package org.martus.client.swingui.fields;

import java.util.Vector;

import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.common.fieldspec.GridFieldSpec;

public class UiGridEditor extends UiEditableGrid 
{
	public UiGridEditor(GridFieldSpec fieldSpec, UiDialogLauncher dlgLauncher, int maxGridCharacters)
	{
		super(fieldSpec, dlgLauncher, maxGridCharacters);
	}

	protected Vector createButtons()
	{
		Vector buttons = super.createButtons();
		buttons.insertElementAt(createShowExpandedButton(), 0);
		return buttons;
	}

}
