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

package org.martus.client.swingui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiChoiceEditor;
import org.martus.clientside.UiLocalization;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.swing.UiButton;
import org.martus.swing.UiCheckBox;
import org.martus.swing.UiLabel;
import org.martus.swing.UiParagraphPanel;
import org.martus.swing.Utilities;



public class UiPreferencesDlg extends JDialog implements ActionListener
{
	public UiPreferencesDlg(UiMainWindow mainWindow)
	{
		super(mainWindow, "", true);
		owner = mainWindow;
		UiLocalization localization = owner.getLocalization();
		
		setTitle(localization.getMenuLabel("Preferences"));

		languageDropdown = new UiChoiceEditor(new DropDownFieldSpec(localization.getUiLanguages()));
		languageDropdown.setText(localization.getCurrentLanguageCode());
		
		ChoiceItem[] mdyChoices = new ChoiceItem[] {
			new ChoiceItem("ymd", buildMdyLabel("ymd")),
			new ChoiceItem("mdy", buildMdyLabel("mdy")),
			new ChoiceItem("dmy", buildMdyLabel("dmy")),
		};
		DropDownFieldSpec mdyChoiceSpec = new DropDownFieldSpec(mdyChoices);
		mdyDropdown = new UiChoiceEditor(mdyChoiceSpec);
		mdyDropdown.setText(localization.getMdyOrder());
		
		ChoiceItem[] delimiterChoices = new ChoiceItem[] {
			new ChoiceItem("/", localization.getFieldLabel("DateDelimiterSlash")),
			new ChoiceItem("-", localization.getFieldLabel("DateDelimiterDash")),
			new ChoiceItem(".", localization.getFieldLabel("DateDelimiterDot")),
		};
		DropDownFieldSpec delimiterChoiceSpec = new DropDownFieldSpec(delimiterChoices);
		delimiterDropdown = new UiChoiceEditor(delimiterChoiceSpec);
		delimiterDropdown.setText("" + localization.getDateDelimiter());
		
		ChoiceItem[] calendarChoices = new ChoiceItem[] {
			new ChoiceItem(MiniLocalization.GREGORIAN_SYSTEM, localization.getFieldLabel("CalendarSystemGregorian")),
			new ChoiceItem(MiniLocalization.THAI_SYSTEM, localization.getFieldLabel("CalendarSystemThai")),
		};
		DropDownFieldSpec calendarChoiceSpec = new DropDownFieldSpec(calendarChoices);
		calendarDropdown = new UiChoiceEditor(calendarChoiceSpec);
		calendarDropdown.setText(localization.getCurrentCalendarSystem());
		
		allPrivate = new UiCheckBox();
		allPrivate.setText(localization.getFieldLabel("preferencesAllPrivate"));
		allPrivate.setSelected(owner.getBulletinsAlwaysPrivate());
		
		UiParagraphPanel preferences = new UiParagraphPanel();
		preferences.addComponents(new UiLabel(localization.getFieldLabel("language")), languageDropdown.getComponent());
		preferences.addComponents(new UiLabel(localization.getFieldLabel("mdyOrder")), mdyDropdown.getComponent());
		preferences.addComponents(new UiLabel(localization.getFieldLabel("DateDelimiter")), delimiterDropdown.getComponent());
		preferences.addComponents(new UiLabel(localization.getFieldLabel("CalendarSystem")), calendarDropdown.getComponent());
		
		preferences.addBlankLine();
		preferences.addOnNewLine(allPrivate);
		preferences.addBlankLine();
		
		ok = new UiButton(localization.getButtonLabel("ok"));
		ok.addActionListener(this);
		cancel = new UiButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(this);
		preferences.addComponents(ok, cancel);
		
		getContentPane().add(preferences);
		getRootPane().setDefaultButton(ok);
		
		Utilities.centerDlg(this);
		setResizable(true);
		setVisible(true);
	}
	
	private String buildMdyLabel(String mdyOrder)
	{
		UiLocalization localization = owner.getLocalization();
		String result = "";
		for(int i = 0; i < mdyOrder.length(); ++i)
		{
			String tag = "Unknown";
			char thisPart = mdyOrder.charAt(i);
			switch(thisPart)
			{
				case 'y': tag = "Year"; break;
				case 'm': tag = "Month"; break;
				case 'd': tag = "Day"; break;
			}
			result += localization.getFieldLabel("DatePart" + tag) + " ";
		}
		
		return result;
	}

	public void actionPerformed(ActionEvent ae)
	{
		
		if(ae.getSource() == ok)
		{
			MartusLocalization localization = owner.getLocalization();
			String languageCodeSelected = languageDropdown.getText();
			UiMainWindow.displayDefaultUnofficialTranslationMessageIfNecessary(owner.getCurrentActiveFrame(), localization, languageCodeSelected);
			UiMainWindow.displayIncompatibleMtfVersionWarningMessageIfNecessary(owner.getCurrentActiveFrame(), localization, languageCodeSelected);
			localization.setMdyOrder(mdyDropdown.getText());
			localization.setDateDelimiter(delimiterDropdown.getText().charAt(0));
			localization.setCurrentCalendarSystem(calendarDropdown.getText());
			localization.setCurrentLanguageCode(languageDropdown.getText());
			owner.setBulletinsAlwaysPrivate(allPrivate.isSelected());
		}
		dispose();
	}


	UiMainWindow owner;
	UiChoiceEditor languageDropdown;
	private UiChoiceEditor mdyDropdown;
	private UiChoiceEditor delimiterDropdown;
	private UiChoiceEditor calendarDropdown;
	private JCheckBox allPrivate;
	private JButton ok;
	private JButton cancel;
}
