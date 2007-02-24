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

package org.martus.client.swingui.bulletincomponent;

import java.awt.Color;
import java.awt.Font;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.UiWarningLabel;
import org.martus.client.swingui.fields.UiField;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.swing.UiLabel;
import org.martus.swing.UiParagraphPanel;

abstract public class UiBulletinComponentSection extends UiParagraphPanel
{
	UiBulletinComponentSection(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
		setBorder(new EtchedBorder());
		outdentFirstField();

		sectionHeading = new UiLabel("", null, JLabel.LEFT);
		sectionHeading.setVerticalTextPosition(JLabel.TOP);
		sectionHeading.setFont(sectionHeading.getFont().deriveFont(Font.BOLD));
		
		warningIndicator = new UiWarningLabel();
		clearWarningIndicator();
		addComponents(sectionHeading, warningIndicator);
	}
	
	public UiMainWindow getMainWindow()
	{
		return mainWindow;
	}
	
	public MartusLocalization getLocalization()
	{
		return getMainWindow().getLocalization();
	}

	void updateSectionBorder(boolean isEncrypted)
	{
		Color color = Color.lightGray;
		if(isEncrypted)
			color = Color.red;
		setBorder(new LineBorder(color, 5));
	}

	public void clearWarningIndicator()
	{
		warningIndicator.setVisible(false);
	}

	public void updateWarningIndicator(String text)
	{
		warningIndicator.setText(text);
		warningIndicator.setVisible(true);
	}

	int getFirstColumnWidth()
	{
		return getFirstColumnMaxWidth(this);
	}

	void matchFirstColumnWidth(UiBulletinComponentSection otherSection)
	{
		int thisWidth = getFirstColumnWidth();
		int otherWidth = otherSection.getFirstColumnWidth();
		if(otherWidth > thisWidth)
			setFirstColumnWidth(otherWidth);
	}

	protected void setSectionIconAndTitle(String iconFileName, String title)
	{
		Icon icon = new ImageIcon(UiBulletinComponentSection.class.getResource(iconFileName));
		sectionHeading.setIcon(icon);
		sectionHeading.setText(title);
	}

	protected UiMainWindow mainWindow;
	JLabel sectionHeading;
	JLabel warningIndicator;
	UiField[] fields;
	FieldSpec[] fieldSpecs;
}
