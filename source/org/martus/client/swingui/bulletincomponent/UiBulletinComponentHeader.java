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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;

import org.martus.client.swingui.UiMainWindow;
import org.martus.common.HQKey;
import org.martus.common.HQKeys;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.packet.UniversalId;
import org.martus.swing.ParagraphLayout;
import org.martus.util.TokenReplacement;
import org.martus.util.Base64.InvalidBase64Exception;
import org.martus.util.TokenReplacement.TokenInvalidException;


public class UiBulletinComponentHeader extends UiBulletinComponentSection
{
	UiBulletinComponentHeader(UiMainWindow mainWindowToUse, String tagQualifierToUse)
	{
		super(mainWindowToUse);
		tagQualifier = tagQualifierToUse;
		
		String buttonText = getLocalization().getButtonLabel("BulletinDetails");
		JButton detailsButton = new JButton(buttonText);
		detailsButton.addActionListener(new DetailsListener());
		add(detailsButton, ParagraphLayout.NEW_PARAGRAPH);
		add(warningIndicator);

		lastSavedLabel = new JLabel(getLocalization().getFieldLabel(Bulletin.TAGLASTSAVED));
		add(lastSavedLabel, ParagraphLayout.NEW_PARAGRAPH);
		dateTime = new JLabel("");
		EtchedBorder b = new EtchedBorder();
		dateTime.setBorder(b);
		add(dateTime);

		hqLabel = new JLabel(getLocalization().getFieldLabel("HQSummaryLabel"));
		add(hqLabel, ParagraphLayout.NEW_PARAGRAPH);
		hqSummary = new JLabel("");
		hqSummary.setFont(hqSummary.getFont().deriveFont(Font.BOLD));
		add(hqSummary);
	}
	
	public void setBulletinId(UniversalId uid)
	{
		currentUid = uid;
	}

	public void setHqKeys(HQKeys keys)
	{
		hqList = keys;
		int numberOfHqs = hqList.size();
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
	}
	
	public void setLastSaved(long time)
	{
		if(time == 0)
		{
			lastSavedLabel.setVisible(false);
			dateTime.setVisible(false);
		}
		else
		{
			setTime(time);
			lastSavedLabel.setVisible(true);
			dateTime.setVisible(true);
		}
	}
	
	void setTime(long time)
	{
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(time);		
		String rawDateTime = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(cal.getTime());
		String formatted = getLocalization().convertStoredDateTimeToDisplay(rawDateTime);
		dateTime.setText("  " + formatted + "  ");
	}
	
	private String getSummaryString(int numberOfHqs)
	{
		String summaryText = getLocalization().getFieldLabel(tagQualifier + "BulletinHQInfo");
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
	
	class DetailsListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			HashMap map = new HashMap();
			map.put("#A#", getPublicCode());
			map.put("#I#", currentUid.getLocalId());
			map.put("#H#", getHqListText());
			JFrame parent = getMainWindow().getCurrentActiveFrame();
			getMainWindow().notifyDlg(parent, tagQualifier + "ViewBulletinDetails", map);
		}
		
		private String getPublicCode()
		{
			try
			{
				return MartusCrypto.computeFormattedPublicCode(currentUid.getAccountId());
			}
			catch (InvalidBase64Exception e)
			{
				e.printStackTrace();
				return "";
			}		
		}

		private String getHqListText()
		{
			if(hqList.size() == 0)
				return "";
			
			HashMap hqMap = new HashMap();
			String listOfHqPublicKeys = "";
			for(int i=0; i < hqList.size(); ++i)
			{
				String thisHqCode;
				HQKey hqKey = getHqPublicCode(i);
				String thisHqlabel = getMainWindow().getApp().getHQLabelIfPresent(hqKey.getPublicKey());
				try
				{
					thisHqCode = hqKey.getPublicCode();
				}
				catch (InvalidBase64Exception e1)
				{
					e1.printStackTrace();
					thisHqCode = hqKey.getPublicKey();
				}
				if(thisHqlabel.length() > 0)
					thisHqCode += " : " + thisHqlabel;
				listOfHqPublicKeys += "  " + thisHqCode + "\n";
			}
			hqMap.put("#L#", listOfHqPublicKeys);

			String text = getLocalization().getFieldLabel(tagQualifier + "ViewBulletinDetailsHQList");
			try
			{
				text = TokenReplacement.replaceTokens(text, hqMap);
			}
			catch(TokenInvalidException e)
			{
				e.printStackTrace();
			}
			return text;
		}

		private HQKey getHqPublicCode(int i)
		{
			try
			{
				return hqList.get(i);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			HQKey unknown = new HQKey("???", "???");
			return unknown;
		}
	}

	String tagQualifier;
	JLabel lastSavedLabel;
	JLabel dateTime;
	JLabel hqLabel;
	JLabel hqSummary;
	UniversalId currentUid;
	HQKeys hqList;
}
