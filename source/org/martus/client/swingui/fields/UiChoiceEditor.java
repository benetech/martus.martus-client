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
		setText(LAST, newCode);
	}
	
	private void setText(int level, String newCode)
	{
		UiComboBox widget = getComboBox(level);
		for(int row = 0; row < widget.getItemCount(); ++row)
		{
			ChoiceItem choiceItem = (ChoiceItem)widget.getItemAt(row);
			if(choiceItem.getCode().equals(newCode))
			{
				widget.setSelectedIndex(row);
				return;
			}
		}
		System.out.println("UiChoiceEditor.setText: Couldn't find " + newCode);
		int select = -1;
		if(widget.getItemCount() > 0)
			select = 0;
		widget.setSelectedIndex(select);
	}

	private String ensureValid(ChoiceItem[] choices, String text) 
	{
		// FIXME: Needs to be updated to handle nested dropdowns
		for(int i = 0; i < choices.length; ++i)
			if(choices[i].getCode().equals(text))
				return text;

		return "";
	}
	
	public void setChoices(ReusableChoices[] newChoices)
	{
		int LAST = newChoices.length - 1;
		ReusableChoices reusableChoices = newChoices[LAST];
		ChoiceItem[] choices = reusableChoices.getChoices();
		
		
		String existingValue = getText();

		comboBoxes.clear();
		container.removeAll();
		
		UiComboBox combo = new UiComboBox();
		combo.setRenderer(new UiChoiceListCellRenderer());
		for(int i = 0; i < choices.length; ++i)
		{
			combo.addItem(choices[i]);
		}
		comboBoxes.add(combo);
		container.add(combo);
		setText(ensureValid(choices, existingValue));

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

