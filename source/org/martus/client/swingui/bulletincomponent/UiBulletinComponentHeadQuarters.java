/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005, Beneficent
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

import org.martus.client.swingui.UiMainWindow;
import org.martus.common.HQKey;
import org.martus.common.HQKeys;
import org.martus.common.bulletin.Bulletin;
import org.martus.swing.UiLabel;
import org.martus.swing.UiWrappedTextArea;
import org.martus.util.Base64.InvalidBase64Exception;

public class UiBulletinComponentHeadQuarters extends UiBulletinComponentSection
{
	public UiBulletinComponentHeadQuarters(UiMainWindow mainWindowToUse, Bulletin bulletinToUse, String tagQualifierToUse)
	{
		super(mainWindowToUse);
		bulletin = bulletinToUse;
		hqKeysAuthorizedToReadThisBulletin = bulletin.getAuthorizedToReadKeys();
		
		String hqText = getLabel("HQInfoFor" + tagQualifierToUse); 
		UiWrappedTextArea hqInfo = new UiWrappedTextArea(hqText, 85);
		hqInfo.setEditable(false);
		addComponents(new UiLabel(""),hqInfo);
	}

	protected String getLabel(String tag)
	{
		return getLocalization().getFieldLabel("BulletinHeadQuarters" + tag);
	}
	
	protected String getHQLabelIfPresent(HQKey key)
	{
		String hqLabelIfPresent = mainWindow.getApp().getHQLabelIfPresent(key);
		if(hqLabelIfPresent.length() == 0)
		{
			String publicCode = key.getPublicKey();
			try
			{
				publicCode = key.getPublicCode();
			}
			catch (InvalidBase64Exception e)
			{
				e.printStackTrace();
			}
			String hqNotConfigured = getLocalization().getFieldLabel("HQNotConfigured");
			hqLabelIfPresent = publicCode + " " + hqNotConfigured;
		}
		return hqLabelIfPresent;
	}

	Bulletin bulletin;
	HQKeys hqKeysAuthorizedToReadThisBulletin;
}
