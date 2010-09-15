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
			if(choiceItem == null)
				System.out.println("UiChoiceEditor.getRenderer null choiceItem");
			else
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
		int LAST = getLevelCount() - 1;
		if(LAST < 0)
			return "";
		
		UiComboBox widget = getComboBox(LAST);
		if(widget == null)
			System.out.println("UiChoiceEditor.getText null widget!");
		ChoiceItem choice = (ChoiceItem)widget.getSelectedItem();
		if(choice == null)
		{
			System.out.println("UiChoiceEditor.getText null choice!");
			return "";
		}
		return choice.getCode();
	}

	public void setText(String newCode)
	{
		if(getLevelCount() == 0)
		{
			if(newCode.length() > 0)
				System.out.println("Attempted to setText " + newCode + " in a choice editor with no dropdowns");
			return;
		}
		
		int LAST = getLevelCount() - 1;
		int level = LAST;
		UiComboBox widget = getComboBox(level);
		int rowToSelect = findItemByCode(widget, newCode);
		if(rowToSelect < 0)
		{
			System.out.println("UiChoiceEditor.setText: Couldn't find " + newCode);
			rowToSelect = findItemByCode(widget, "");
		}
		widget.setSelectedIndex(rowToSelect);
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

	public void setChoices(ReusableChoices[] newChoices)
	{
		String existingValue = getText();
		comboBoxes.clear();
		container.removeAll();

		for(int level = 0; level < newChoices.length; ++level)
		{
			ReusableChoices reusableChoices = newChoices[level];
			ChoiceItem[] choices = reusableChoices.getChoices();
	
			UiComboBox combo = new UiComboBox();
			combo.setRenderer(new UiChoiceListCellRenderer());
			for(int i = 0; i < choices.length; ++i)
			{
				combo.addItem(choices[i]);
			}
			comboBoxes.add(combo);
			container.add(combo);
		}
		setText(existingValue);
	}

	public void actionPerformed(ActionEvent e) 
	{
		if(observer != null)
			observer.languageChanged(getText());
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
	private LanguageChangeListener observer;
}

