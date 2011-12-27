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
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.martus.common.MartusLogger;
import org.martus.common.VersionBuildDate;
import org.martus.swing.UiOptionPane;
import org.martus.swing.Utilities;
import org.miradi.main.RuntimeJarLoader;

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
		System.out.println("Java version: " + System.getProperty("java.version"));

		try
		{
			addThirdPartyJarsToClasspath();
		} 
		catch (Exception e)
		{
			System.out.println("Error loading third-party jars");
			e.printStackTrace();
		}
		
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
		
		UiMainWindow.timeoutInXSeconds = DEFAULT_TIMEOUT_SECONDS;
		int foundTimeout = findOption(options, TIMEOUT_OPTION_TEXT);
		if(foundTimeout >= 0)
		{
			String fullOption = (String)options.get(foundTimeout);
			String requestedTimeoutMinutes = fullOption.substring(TIMEOUT_OPTION_TEXT.length());
			System.out.println("Requested timeout in minutes: " + requestedTimeoutMinutes);
			int timeoutMinutes = Integer.parseInt(requestedTimeoutMinutes);
			UiMainWindow.timeoutInXSeconds = 60 * timeoutMinutes;
			options.remove(foundTimeout);
		}
		
		if(options.size() > 0)
		{
			System.out.println("Incorrect command line parameter");
			System.out.println("The only valid options are:");
			System.out.println("--testall");
			System.out.println("--folders-unsorted");
			System.out.println("--timeout-minutes=<nn>");
			System.exit(1);
		}
		
		try
		{
			if(Utilities.isMSWindows())
				UIManager.put("Application.useSystemFontSettings", new Boolean(false));

			boolean useSystemLookAndFeel = true;
			String osName = System.getProperty("os.name");
			String osVersion = System.getProperty("os.version");
			System.out.println(osName + ": " + osVersion);
			if(osName.startsWith("Windows"))
			{
				int comparedToWindow7 = osVersion.compareTo("6.1");
				if(comparedToWindow7 >= 0)
					useSystemLookAndFeel = false;
			}
			
			if(useSystemLookAndFeel)
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

	private static int findOption(Vector options, String optionText)
	{
		for(int i = 0; i < options.size(); ++i)
		{
			String option = (String) options.get(i);
			if(option.startsWith(optionText))
			{
				return i;
			}
		}
		
		return -1;
	}

	public static void addThirdPartyJarsToClasspath() throws Exception
	{
		String jarSubdirectoryName = "ThirdParty";
		File miradiDirectory = getAppCodeDirectory();
		File thirdPartyDirectory = new File(miradiDirectory, jarSubdirectoryName);
		RuntimeJarLoader.addJarsInSubdirectoryToClasspath(thirdPartyDirectory, getThirdPartyJarNames());
		System.err.println("Miradi code running from: " + miradiDirectory.getAbsolutePath());
		System.err.println("Added jars to classpath: " + thirdPartyDirectory.getAbsolutePath());
	}
	
	private static String[] getThirdPartyJarNames()
	{
		return new String[] {
			"icu4j-3.4.4.jar",
			"infinitemonkey-1.0.jar",
			"js-2006-03-08.jar",
			"junit-3.8.2.jar",
			"layouts-2006-08-10.jar",
			"persiancalendar-2.1.jar",
			"velocity-1.4.jar",
			"velocity-dep-1.4.jar",
			"xmlrpc-1.2-b1.jar",
		};
	}

	public static File getAppCodeDirectory() throws URISyntaxException
	{
		final URL resourceUrl = Martus.class.getResource("/org");
		String imagesURIString = resourceUrl.toURI().getSchemeSpecificPart();
		String imagesPathString = stripPrefix(imagesURIString);
		
		int bangAt = imagesPathString.indexOf('!');
		if(bangAt < 0)
		{
			File imagesDirectory = new File(imagesPathString);
			final File directory = imagesDirectory.getParentFile();
			return directory;
		}
		
		String jarURIString = imagesPathString.substring(0, bangAt);
		File jarFile = new File(jarURIString);
		final File directory = jarFile.getParentFile();
		return directory;
	}

	private static String stripPrefix(String uri)
	{
		int startOfRealPath = uri.indexOf(':') + 1;
		return uri.substring(startOfRealPath);
	}

	private final static String TIMEOUT_OPTION_TEXT = "--timeout-minutes=";
	private final static int DEFAULT_TIMEOUT_SECONDS = (10 * 60);

}
