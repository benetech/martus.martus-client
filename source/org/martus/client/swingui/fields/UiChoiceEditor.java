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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;

import org.martus.client.core.LanguageChangeListener;
import org.martus.common.ListOfReusableChoicesLists;
import org.martus.common.MiniLocalization;
import org.martus.common.ReusableChoices;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.swing.UiComboBox;
import org.martus.swing.UiLanguageDirection;
import org.martus.util.language.LanguageOptions;

public class UiChoiceEditor extends UiChoice implements ActionListener
{
	public UiChoiceEditor(MiniLocalization localizationToUse)
	{
		super(localizationToUse);

		container = Box.createHorizontalBox();
		comboBoxes = new Vector();

		addActionListener(this);
	}
	
	public void addActionListener(ActionListener listener)
	{
		for(int i = 0; i < getLevelCount(); ++i)
			getComboBox(i).addActionListener(listener);
	}
	
	private int getLevelCount()
	{
		return comboBoxes.size();
	}

	private UiComboBox getComboBox(int i)
	{
		return (UiComboBox) comboBoxes.get(i);
	}

	class UiChoiceListCellRenderer extends DefaultListCellRenderer
	{
		
		public Component getListCellRendererComponent(JList list, Object choiceItem, int index, boolean isSelected, boolean cellHasFocus)
		{
			String spaceSoValueWontBeHiddenIfEmpty = " ";
			String choiceText = "";
			if(choiceItem != null)
				choiceText = choiceItem.toString();

			String displayString = choiceText + spaceSoValueWontBeHiddenIfEmpty;
			Component cellRenderer = super.getListCellRendererComponent(list, displayString, index, isSelected,
					cellHasFocus);
			cellRenderer.setComponentOrientation(UiLanguageDirection.getComponentOrientation());
			return cellRenderer;
		}

		public Dimension getPreferredSize()
		{
			Dimension d = super.getPreferredSize();
			d.height += LanguageOptions.getExtraHeightIfNecessary();
			return d;
		}
	}

	public String getText()
	{
		String result = "";
		for(int level = 0; level < getLevelCount(); ++level)
		{
			UiComboBox widget = getComboBox(level);
			if(widget == null)
				System.out.println("UiChoiceEditor.getText null widget!");
			ChoiceItem choice = (ChoiceItem)widget.getSelectedItem();
			if(choice == null)
				break;
			if(choice.getCode().length() == 0)
				break;
			result = choice.getCode();
		}

		return result;
		
	}

	public void setText(String newCode)
	{
		if(getLevelCount() == 0)
		{
			if(newCode.length() > 0)
				System.out.println("Attempted to setText " + newCode + " in a choice editor with no dropdowns");
			return;
		}

		for(int level = 0; level < getLevelCount(); ++level)
		{
			UiComboBox widget = getComboBox(level);
			int rowToSelect = -1;
			
			int LAST = getLevelCount() - 1;
			if(level == LAST)
			{
				rowToSelect = findItemByCode(widget, newCode);
			}
			else
			{
				rowToSelect = findItemByPartialMatch(widget, newCode);
			}
			
			if(rowToSelect < 0 && newCode.length() > 0)
			{
				System.out.println("UiChoiceEditor.setText: Couldn't find " + newCode);
				rowToSelect = findItemByCode(widget, "");
			}
			widget.setSelectedIndex(rowToSelect);
		}
	}
	
	private int findItemByPartialMatch(UiComboBox widget, String code)
	{
		for(int row = 0; row < widget.getItemCount(); ++row)
		{
			ChoiceItem choiceItem = (ChoiceItem)widget.getItemAt(row);
			String choiceItemCode = choiceItem.getCode();
			if(choiceItemCode.length() > 0 && code.startsWith(choiceItemCode))
				return row;
		}
		
		return -1;
	}

	int findItemByCode(UiComboBox widget, String code)
	{
		for(int row = 0; row < widget.getItemCount(); ++row)
		{
			ChoiceItem choiceItem = (ChoiceItem)widget.getItemAt(row);
			if(choiceItem.getCode().equals(code))
			{
				widget.setSelectedIndex(row);
				return row;
			}
		}
		
		return -1;
	}

	public void setChoices(ListOfReusableChoicesLists newChoices)
	{
		choiceLists = newChoices;
		
		String existingValue = getText();
		for(int i = 0; i < comboBoxes.size(); ++i)
			((UiComboBox)comboBoxes.get(i)).removeActionListener(this);
		comboBoxes.clear();
		container.removeAll();

		for(int level = 0; level < newChoices.size(); ++level)
		{
			ReusableChoices reusableChoices = newChoices.get(level);
			ChoiceItem[] choices = reusableChoices.getChoices();
	
			UiComboBox combo = new UiComboBox();
			combo.setRenderer(new UiChoiceListCellRenderer());
			for(int i = 0; i < choices.length; ++i)
			{
				combo.addItem(choices[i]);
			}
			combo.addActionListener(this);
			comboBoxes.add(combo);
			container.add(combo);
		}
		setText(existingValue);
	}

	public void actionPerformed(ActionEvent e) 
	{
		if(observer != null)
			observer.languageChanged(getText());
		updateEditabilityOfComboBoxes(e.getSource());
	}

	private void updateEditabilityOfComboBoxes(Object eventSource)
	{
		boolean shouldBeEnabled = true;

		for(int level = 0; level < comboBoxes.size(); ++level)
		{
			UiComboBox combo = (UiComboBox) comboBoxes.get(level);
			
			if(level > 0)
			{
				UiComboBox previousCombo = (UiComboBox) comboBoxes.get(level-1);
				ChoiceItem previousSelected = (ChoiceItem) previousCombo.getSelectedItem();
				String previousCode = "";
				if(previousSelected == null)
					previousSelected = (ChoiceItem) previousCombo.getItemAt(findItemByCode(previousCombo, ""));
				if(previousSelected != null)
					previousCode = previousSelected.getCode();
				
				if(previousCode.length() > 0)
				{
					updateWidgetChoices(level, previousCode);
				}
				else
				{
					shouldBeEnabled = false;
					combo.setSelectedIndex(-1);
				}
			}
			combo.setEnabled(shouldBeEnabled);
		}
	}

	private void updateWidgetChoices(int level, String previousCode)
	{
		if(isUpdateInProgress)
			return;
		
		isUpdateInProgress = true;
		try
		{
			UiComboBox combo = getComboBox(level);
			ChoiceItem wasSelected = (ChoiceItem) combo.getSelectedItem();
	
			ReusableChoices existingChoices = new ReusableChoices("", "");
			for(int row = 0; row < combo.getItemCount(); ++row)
			{
				ChoiceItem choice = (ChoiceItem) combo.getItemAt(row);
				existingChoices.add(choice);
			}
			ReusableChoices possibleChoices = choiceLists.get(level);
			ReusableChoices newChoices = new ReusableChoices("", "");
			for(int choiceIndex = 0; choiceIndex < possibleChoices.size(); ++choiceIndex)
			{
				ChoiceItem choice = possibleChoices.get(choiceIndex);
				if(choice.getCode().startsWith(previousCode))
					newChoices.add(choice);
			}
			
			if(newChoices.findByCode("") == null)
				newChoices.insertAtTop(new ChoiceItem("", ""));
	
			if(newChoices.equals(existingChoices))
				return;
			
			combo.removeAllItems();
			for(int choiceIndex = 0; choiceIndex < newChoices.size(); ++choiceIndex)
			{
				combo.addItem(newChoices.get(choiceIndex));
			}
			
			combo.setSelectedItem(wasSelected);
		}
		finally
		{
			isUpdateInProgress = false;
		}
	}

	public JComponent getComponent()
	{
		return container;
	}

	public JComponent[] getFocusableComponents()
	{
		return (JComponent[])comboBoxes.toArray(new JComponent[0]);
	}

	public void setLanguageListener(LanguageChangeListener listener)
	{
		observer = listener;
	}
	
	private Box container;
	private Vector comboBoxes;
	private ListOfReusableChoicesLists choiceLists;
	private LanguageChangeListener observer;
	private boolean isUpdateInProgress;
}

