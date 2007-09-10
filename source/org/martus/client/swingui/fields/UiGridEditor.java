package org.martus.client.swingui.fields;

import java.util.HashSet;
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

		HashSet existingValues = new HashSet();
		Vector values = new Vector();
		values.add(new ChoiceItem("", ""));
		existingValues.add("");
		for(int row = 0; row < model.getRowCount(); ++row)
		{
			String thisValue = (String)model.getValueAt(row, gridColumn);
			if(existingValues.contains(thisValue))
				continue;
			values.add(new ChoiceItem(thisValue, thisValue));
			existingValues.add(thisValue);
		}
		
		return (ChoiceItem[])values.toArray(new ChoiceItem[0]);
	}
}
