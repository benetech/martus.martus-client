/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2004, Beneficent
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

package org.martus.client.swingui.bulletincomponent;

import java.util.HashMap;

import javax.swing.JLabel;

import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.UiWarningLabel;
import org.martus.common.bulletin.Bulletin;
import org.martus.swing.ParagraphLayout;
import org.martus.util.TokenReplacement;


public class UiBulletinComponentHeader extends UiBulletinComponentSection
{
	UiBulletinComponentHeader(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		UiMainWindow mainWindow = getMainWindow();
		updateSectionBorder(false);
		
		String iconFileName = "BulletinViewHeading.png";
		UiLocalization localization = mainWindow.getLocalization();
		setSectionIconAndTitle(iconFileName, localization.getFieldLabel("BulletinViewHeading"));

		hqSummary = new UiWarningLabel();
		add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		add(hqSummary);
	}

	void copyDataFromBulletin(Bulletin currentBulletin)
	{
		int numberOfHqs = currentBulletin.getAuthorizedToReadKeys().size();
		if(numberOfHqs > 0)
		{
			hqSummary.setVisible(true);
			hqSummary.setText(getSummaryString(numberOfHqs));
		}
		else
		{
			hqSummary.setVisible(false);
			hqSummary.setText("");
		}
	}

	private String getSummaryString(int numberOfHqs)
	{
		UiMainWindow mainWindow = getMainWindow();
		UiLocalization localization = mainWindow.getLocalization();
		String summaryText = localization.getFieldLabel("BulletinViewHQInfo");
		try
		{
			HashMap tokenReplacement = new HashMap();
			tokenReplacement.put("#N#", Integer.toString(numberOfHqs));
			summaryText = TokenReplacement.replaceTokens(summaryText, tokenReplacement);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return summaryText;
	}

	JLabel hqSummary;
}
