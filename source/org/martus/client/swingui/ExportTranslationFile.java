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
package org.martus.client.swingui;

import java.io.File;

import org.martus.client.core.MartusApp;
import org.martus.util.UnicodeWriter;


public class ExportTranslationFile
{

	public static void main (String args[])
	{
		if(args.length != 2)
		{
			System.out.println("If you specify a language code and output filename, " +
								"this will write out a file");
			System.out.println("that contains all the existing translations " +
								"for language xx, plus placeholder");
			System.out.println("tags for all the untranslated strings.");
			System.out.println("Example of a language code: es = Spanish");
			System.exit(1);
		}
	
		String languageCode = args[0].toLowerCase();
		if(languageCode.length() != 2 ||
			!Character.isLetter(languageCode.charAt(0)) ||
			!Character.isLetter(languageCode.charAt(1)))
		{
			System.out.println("Invalid language code. Must be two letters (e.g. 'es')");
			System.exit(2);
		}
	
		System.out.println("Exporting translations for: " + languageCode);
		UiLocalization bd = new UiLocalization(MartusApp.getTranslationsDirectory(), EnglishStrings.strings);
		bd.loadTranslationFile(languageCode);
		File outputFile = new File(args[1]);
	
		try
		{
			UnicodeWriter writer = new UnicodeWriter(outputFile);
			bd.exportTranslations(languageCode, UiConstants.versionLabel, writer);
	
			writer.close();
			System.out.println("Success");
		}
		catch(Exception e)
		{
			System.out.println("FAILED: " + e);
			System.exit(3);
		}
	
	}
	
}
