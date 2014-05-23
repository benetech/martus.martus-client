/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
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
package org.martus.client.swingui;

import org.martus.client.core.MartusApp;
import org.martus.client.core.MartusApp.MartusAppInitializationException;
import org.martus.common.EnglishCommonStrings;


public class UiSession
{
	public UiSession() throws MartusAppInitializationException
	{
		setLocalization(new MartusLocalization(MartusApp.getTranslationsDirectory(), UiSession.getAllEnglishStrings()));
		app = new MartusApp(getLocalization());
	}

	public static String[] getAllEnglishStrings()
	{
		String[] clientStrings = EnglishStrings.strings;
		int lenghtClient = clientStrings.length;
		String[] commonStrings = EnglishCommonStrings.strings;
		int lenghtCommon = commonStrings.length;
		String[] allEnglishStrings = new String[lenghtCommon+lenghtClient];
		System.arraycopy(clientStrings,0,allEnglishStrings,0,lenghtClient);
		System.arraycopy(commonStrings,0,allEnglishStrings,lenghtClient,lenghtCommon);
		return allEnglishStrings;
	}

	private void setLocalization(MartusLocalization martusLocalization)
	{
		localization = martusLocalization;
	}

	public MartusLocalization getLocalization()
	{
		return localization;
	}

	public MartusApp getApp()
	{
		return app;
	}

	private MartusLocalization localization;
	private MartusApp app;

}
