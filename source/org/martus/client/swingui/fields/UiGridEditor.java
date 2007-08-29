package org.martus.client.swingui.fields;

import java.util.Map;
import java.util.Vector;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.GridFieldSpec;

public class UiGridEditor extends UiEditableGrid 
{
	public UiGridEditor(UiMainWindow mainWindow, GridFieldSpec fieldSpec, UiDialogLauncher dlgLauncher, Map gridFields, int maxGridCharacters)
	{
		super(mainWindow, fieldSpec, dlgLauncher, gridFields, maxGridCharacters);
	}

	protected Vector createButtons()
	{
		Vector buttons = super.createButtons();
		buttons.insertElementAt(createShowExpandedButton(), 0);
		return buttons;
	}

	public ChoiceItem[] buildChoicesFromColumnValues(String gridColumnLabel)
	{
		int gridColumn = model.findColumn(gridColumnLabel);

		ChoiceItem[] values = new ChoiceItem[1 + model.getRowCount()];
		values[0] = new ChoiceItem("", "");
		for(int row = 0; row < model.getRowCount(); ++row)
		{
			String thisValue = (String)model.getValueAt(row, gridColumn);
			values[row + 1] = new ChoiceItem(thisValue, thisValue);
		}
		
		return values;
		
	}
}
