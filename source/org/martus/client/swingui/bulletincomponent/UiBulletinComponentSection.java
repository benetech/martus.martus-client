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
import java.awt.Component;
import java.awt.Font;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.UiWarningLabel;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.swing.UiLabel;
import org.martus.swing.UiParagraphPanel;
import org.martus.swing.Utilities;

import com.jhlabs.awt.BasicGridLayout;

abstract public class UiBulletinComponentSection extends JPanel
{
	UiBulletinComponentSection(UiMainWindow mainWindowToUse, String groupTag)
	{
		super(new BasicGridLayout());
		mainWindow = mainWindowToUse;
		groups = new Vector();
		
		setBorder(new EtchedBorder());
		
		sectionHeading = new UiLabel("", null, JLabel.LEFT);
		sectionHeading.setVerticalTextPosition(JLabel.TOP);
		sectionHeading.setFont(sectionHeading.getFont().deriveFont(Font.BOLD));
		
		warningIndicator = new UiWarningLabel();
		clearWarningIndicator();
		
		Box box = Box.createHorizontalBox();
		Utilities.addComponentsRespectingOrientation(box, new Component[] {
				sectionHeading, 
				warningIndicator,
				Box.createHorizontalGlue(),
				});
		add(box);

		groupTag = "_Section" + groupTag;
		startNewGroup(groupTag, getLocalization().getFieldLabel(groupTag));
	}
	
	public void addComponents(JComponent labelComponent, JComponent fieldComponent)
	{
		currentGroup.addComponents(labelComponent, fieldComponent);
	}

	public void startNewGroup(String tag, String title)
	{
		if(currentGroup != null && currentGroup.isEmpty())
		{
			groups.remove(currentGroup);
			remove(currentGroup);
		}

		currentGroup = new FieldGroup(tag, title);
		add(currentGroup);
		groups.add(currentGroup);

		currentGroup.setBorder(new LineBorder(Color.BLUE));
	}
	
	class FieldGroup extends JPanel
	{
		public FieldGroup(String tag, String title)
		{
			super(new BasicGridLayout(1, 2));
			contents = new UiParagraphPanel();
			contents.outdentFirstField();
			MartusApp app = getMainWindow().getApp();
			MartusLocalization localization = getMainWindow().getLocalization();
			FieldHolder fieldHolder = new FieldHolder(contents, localization);

			JComponent[] firstRow = new JComponent[] {new HiderButton(app, "_Section" + tag, fieldHolder), new UiLabel(title)};
			JComponent[] secondRow = new JComponent[] {new UiLabel(""), fieldHolder};
			Utilities.addComponentsRespectingOrientation(this, firstRow);
			Utilities.addComponentsRespectingOrientation(this, secondRow);
		}
		
		public void addComponents(JComponent left, JComponent right)
		{
			contents.addComponents(left, right);
		}
		
		public boolean isEmpty()
		{
			return (contents.getComponentCount() == 0);
		}
		
		UiParagraphPanel contents;
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

	void matchFirstColumnWidth(UiBulletinComponentSection otherSection)
	{
// FIXME: Not clear if this is necessary or helpful...either delete it or make it work again
//		int thisWidth = currentGroup.getFirstColumnMaxWidth(this);
//		int otherWidth = otherSection.currentGroup.getFirstColumnMaxWidth(otherSection);
//		if(otherWidth > thisWidth)
//			currentGroup.setFirstColumnWidth(otherWidth);
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
	FieldSpec[] fieldSpecs;
	
	FieldGroup currentGroup;
	Vector groups;
}
