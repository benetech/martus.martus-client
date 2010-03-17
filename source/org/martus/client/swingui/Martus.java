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

package org.martus.client.swingui;

import java.awt.Toolkit;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.martus.common.MartusLogger;
import org.martus.common.VersionBuildDate;
import org.martus.swing.UiOptionPane;
import org.martus.swing.Utilities;

class Martus
{
    public static void main (String args[])
	{
		final String javaVersion = System.getProperty("java.version");
		final String minimumJavaVersion = "1.4.1";
		if(javaVersion.compareTo(minimumJavaVersion) < 0)
		{
			final String errorMessage = "Requires Java version " + minimumJavaVersion + " or later!";
			System.out.println(errorMessage);
			Toolkit.getDefaultToolkit().beep();
			UiOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(2);
		}
		
		System.out.println(UiConstants.programName);
		System.out.println(UiConstants.versionLabel + " " + VersionBuildDate.getVersionBuildDate());

		Vector options = new Vector(Arrays.asList(args));
		int foundTestAll = options.indexOf("--testall");
		if(foundTestAll < 0)
			foundTestAll = options.indexOf("-testall");
		if(foundTestAll >= 0)
		{
			org.martus.common.test.TestCommon.runTests();
			org.martus.client.test.TestAll.runTests();
			System.exit(0);
		}
		
		int foundFoldersUnsorted = options.indexOf("--folders-unsorted");
		if(foundFoldersUnsorted >= 0)
		{
			System.out.println(options.get(foundFoldersUnsorted));
			UiMainWindow.defaultFoldersUnsorted = true;
			options.remove(foundFoldersUnsorted);
		}
		
		int foundAlphaTester = options.indexOf("--alpha-tester");
		if(foundAlphaTester >= 0)
		{
			System.out.println(options.get(foundAlphaTester));
			UiMainWindow.isAlphaTester = true;
			options.remove(foundAlphaTester);
		}
		
		if(options.size() > 0)
		{
			System.out.println("Incorrect command line parameter");
			System.out.println("The only valid options are:");
			System.out.println("--testall");
			System.out.println("--folders-unsorted");
			System.exit(1);
		}
		
		try
		{
			if(Utilities.isMSWindows())
				UIManager.put("Application.useSystemFontSettings", new Boolean(false));
			
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e)
		{
			System.out.println(e);
			//e.printStatckTrace(System.out);
		}

        UiMainWindow window = new UiMainWindow();
        if(!window.run())
        {
        	MartusLogger.log("Exiting after run()");
        	System.exit(0);
        }
    }
}
