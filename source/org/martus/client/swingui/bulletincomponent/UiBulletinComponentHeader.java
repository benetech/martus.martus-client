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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;

import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.UiBulletinDetailsDialog;
import org.martus.common.bulletin.Bulletin;
import org.martus.util.TokenReplacement;


public class UiBulletinComponentHeader extends UiBulletinComponentSection
{
	UiBulletinComponentHeader(UiMainWindow mainWindowToUse, String tagQualifierToUse)
	{
		super(mainWindowToUse);
		tagQualifier = tagQualifierToUse;
		UiLocalization localization = getLocalization();
		String buttonText = localization.getButtonLabel("BulletinDetails");
		JButton detailsButton = new JButton(buttonText);
		detailsButton.addActionListener(new DetailsListener());
		addComponents(detailsButton, warningIndicator);

		lastSavedLabel = new JLabel(localization.getFieldLabel(Bulletin.TAGLASTSAVED));
		dateTime = new JLabel("");
		EtchedBorder border = new EtchedBorder();
		dateTime.setBorder(border);
		addComponents(lastSavedLabel, dateTime);

		JLabel versionLabel = new JLabel(localization.getFieldLabel("BulletinVersionNumber"));
		versionNumber = new JLabel("#");
		versionNumber.setBorder(border);
		addComponents(versionLabel, versionNumber);
		
		hqLabel = new JLabel(localization.getFieldLabel("HQSummaryLabel"));
		hqSummary = new JLabel("");
		addComponents(hqLabel, hqSummary);

	}
	
	public void setBulletin(Bulletin bulletinToShow)
	{
		bulletin = bulletinToShow;
		int numberOfHqs = bulletin.getAuthorizedToReadKeys().size();
		if(numberOfHqs > 0)
		{
			hqSummary.setText(getSummaryString(numberOfHqs));
			hqLabel.setVisible(true);
			hqSummary.setVisible(true);
		}
		else
		{
			hqSummary.setText("");
			hqLabel.setVisible(false);
			hqSummary.setVisible(false);
		}
		versionNumber.setText("  " + Integer.toString(bulletin.getVersion())+ "  ");

		long time = bulletin.getLastSavedTime();
		if(time == 0)
		{
			lastSavedLabel.setVisible(false);
			dateTime.setVisible(false);
		}
		else
		{
			dateTime.setText("  " + getLocalization().formatDateTime(time) + "  ");
			lastSavedLabel.setVisible(true);
			dateTime.setVisible(true);
		}
	}
	
	private String getSummaryString(int numberOfHqs)
	{
		String summaryText = getLocalization().getFieldLabel(tagQualifier + "BulletinHQInfo");
		try
		{
			HashMap tokenReplacement = new HashMap();
			tokenReplacement.put("#NumberOfHQs#", Integer.toString(numberOfHqs));
			summaryText = TokenReplacement.replaceTokens(summaryText, tokenReplacement);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return summaryText;
	}
	
	void showBulletinDetails()
	{
		UiBulletinDetailsDialog dlg = new UiBulletinDetailsDialog(mainWindow, bulletin, tagQualifier);
		dlg.show();
	}

	class DetailsListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			showBulletinDetails();
		}
		
	}

	String tagQualifier;
	Bulletin bulletin;
	private JLabel lastSavedLabel;
	private JLabel dateTime;
	private JLabel hqLabel;
	private JLabel hqSummary;
	private JLabel versionNumber;
}
