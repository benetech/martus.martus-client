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

package org.martus.client.test;

import java.util.Map;
import java.util.Vector;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.EnglishStrings;
import org.martus.client.swingui.UiConstants;
import org.martus.client.swingui.UiLocalization;
import org.martus.common.MartusUtilities;
import org.martus.common.clientside.ChoiceItem;
import org.martus.common.clientside.DateUtilities;
import org.martus.common.clientside.Localization;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.util.StringInputStream;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeStringWriter;

public class TestLocalization extends TestCaseEnhanced
{
    public TestLocalization(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();
		if(bd == null)
			bd = new UiLocalization(MartusApp.getTranslationsDirectory(), EnglishStrings.strings);
 	}
	
	public void testDefaultDateFormats()
	{
		assertEquals("English should always return the default date format", DateUtilities.getDefaultDateFormatCode(), bd.getDefaultDateFormatForLanguage(UiBasicLocalization.ENGLISH));
		assertEquals("Spanish should always return Slash DMY", DateUtilities.DMY_SLASH.getCode(), bd.getDefaultDateFormatForLanguage(UiBasicLocalization.SPANISH));
		assertEquals("Russian should always return Dot DMY", DateUtilities.DMY_DOT.getCode(), bd.getDefaultDateFormatForLanguage(UiBasicLocalization.RUSSIAN));
		assertEquals("An unknown Language should always return the default date format", DateUtilities.getDefaultDateFormatCode(), bd.getDefaultDateFormatForLanguage("ZZ"));
	}
	
	public void testIsLanguageFile()
	{
		assertTrue(Localization.isLanguageFile("Martus-en.mtf"));
		assertTrue(Localization.isLanguageFile("Martus-ab.mtf"));
		assertTrue(Localization.isLanguageFile("martus-ab.mtf"));
		assertTrue(Localization.isLanguageFile("MARTUS-ab.MTF"));
	}
	
	public void testValidLanguageCodes()
	{
		assertFalse("null should not be recognized", bd.isRecognizedLanguage(null));
		assertTrue("English should be recognized", bd.isRecognizedLanguage(UiBasicLocalization.ENGLISH));
		assertTrue("Spanish should be recognized", bd.isRecognizedLanguage(UiBasicLocalization.SPANISH));
		assertTrue("Russian should be recognized", bd.isRecognizedLanguage(UiBasicLocalization.RUSSIAN));
		assertFalse("Unknown should not be recognized", bd.isRecognizedLanguage("XX"));
	}

	public void testToFileNameForeignChars()
	{
		String english = "abcdefghijklmnopqrstuvwxyz";
		assertEquals(english.substring(0, 20), MartusUtilities.toFileName(english));
		//TODO add test for russian.
	}
	
	
	public void testLanguages()
	{
		ChoiceItem[] languages = bd.getUiLanguages();
		assertTrue("Should have multiple languages", languages.length > 1);

		boolean foundEnglish = false;
		for(int i = 0; i < languages.length; ++i)
		{
			String code = languages[i].getCode();
			if(code.equals("en"))
			{
				assertEquals("English", true, bd.isLanguageLoaded("en"));
				foundEnglish = true;
			}
			else
			{
				assertEquals(code, false, bd.isLanguageLoaded(code));
			}
		}

		assertTrue("must have english", foundEnglish);
	}

	public void testLanguageCodeFromFilename()
	{
		assertEquals("", bd.getLanguageCodeFromFilename("Martus.mtf"));
		assertEquals("", bd.getLanguageCodeFromFilename("Martus-es.xyz"));
		assertEquals("es", bd.getLanguageCodeFromFilename("Martus-es.mtf"));
		assertEquals("Martus mtf files are not case Sensitive", "es", bd.getLanguageCodeFromFilename("martus-es.mtf"));
	}

	public void testTranslations()
	{
		bd.loadTranslationFile("es");

		assertEquals("Print", bd.getLabel("en", "button", "print"));
		assertEquals("No translation found", "<whatever:not in the map>", bd.getLabel("en", "whatever", "not in the map"));

		assertEquals("Imprimir", bd.getLabel("es", "button", "print"));

		assertEquals("<category:sillytag>", bd.getLabel("en", "category", "sillytag"));
		assertEquals("<<category:sillytag>>", bd.getLabel("es", "category", "sillytag"));
		bd.addTranslation("en", "category:sillytag=something");
		assertEquals("<something>", bd.getLabel("es", "category", "sillytag"));
		assertEquals("something", bd.getLabel("en", "category", "sillytag"));
		bd.addTranslation("es", "category:sillytag=es/something");
		assertEquals("es/something", bd.getLabel("es", "category", "sillytag"));

		assertEquals("<Print>", bd.getLabel("xx", "button", "print"));
	}
	
	public void testLoadTranslations() throws Exception
	{
		String sampleFileContents = "# This is a comment with =\na:b=c\nd:e=f";
		StringInputStream in = new StringInputStream(sampleFileContents);
		bd.loadTranslations("qq", in);
		assertEquals("c", bd.getLabel("qq", "a", "b"));
		Map qq = bd.getStringMap("qq");
		assertEquals("not 2?", 2, qq.size());
	}
	
	public void testExportTranslations() throws Exception
	{
		UnicodeStringWriter writer = UnicodeStringWriter.create();
		bd.exportTranslations("en", UiConstants.versionLabel, writer);
		String result = writer.toString();
		assertEquals("no leading ByteOrderMark?", 0xFEFF, result.charAt(0));
		assertEquals("no leading comment?", 1, result.indexOf("#"));
	}

	public void testAddTranslation()
	{
		// must call getLabel first, to initialize the hash (this should go away soon!)
		assertEquals("<<b:c>>", bd.getLabel("xx", "b", "c"));
		bd.addTranslation("en", "b:c=bc");
		assertEquals("<bc>", bd.getLabel("xx", "b", "c"));
		bd.addTranslation("a", "invalid=because-bad-language");
		bd.addTranslation("en", null);
		bd.addTranslation("en", "invalid-because-no-equals");
		bd.addTranslation("en", "b:c=new\\nline");
		assertEquals("new\nline", bd.getLabel("en", "b", "c"));
	}

/*TODO: Evaluate whether any of these tests are still useful
 * because they are not already covered elsewhere
 *
	public void testLoadTranslations()
	{
		try
		{
			assertEquals("xx shouldn't exist yet", false, bd.isLanguageLoaded("xx"));
			bd..loadTranslationFile("xx", "@#<>%#$%#");
			assertEquals("xx should exist now", true, bd.isLanguageLoaded("xx"));

			File file = createTempFileFromName("$$$MartusTestLoadTranslations");
			UnicodeWriter writer = new UnicodeWriter(file);
			writer.write("f:g=fg\n");
			writer.write("j:k=jk\n");
			writer.close();
			assertEquals("<h>", bd.getLabel("xx", "f", "g", "h"));
			assertEquals("<l>", bd.getLabel("xx", "j", "k", "l"));
			bd.loadTranslationFile("xx", file.getCanonicalPath());
			assertEquals("fg", bd.getLabel("xx", "f", "g", "h"));
			assertEquals("jk", bd.getLabel("xx", "j", "k", "l"));
		}
		catch (IOException e)
		{
			assertTrue(e.toString(), false);
		}

		file.delete();
	}
*/

	public void testGetAllTranslationStrings()
	{
		final String sillyEnglish = "a:b=c";
		final String sillyEsperanto = sillyEnglish + "x";

		Vector strings;
		strings = bd.getAllTranslationStrings("eo");
		assertNotNull("Null vector", strings);

		int count = strings.size();
		assertEquals("Should not contain english silly key yet", false, strings.contains(sillyEnglish));
		assertEquals("Should not contain esperanto silly key yet", false, strings.contains(sillyEsperanto));

		bd.addTranslation("eo", sillyEsperanto);
		strings = bd.getAllTranslationStrings("eo");
		assertEquals("Should not have added a string", count, strings.size());
		assertEquals("Still should not contain english silly key", false, strings.contains(sillyEnglish));
		assertEquals("Still should not contain esperanto silly key", false, strings.contains(sillyEsperanto));

		bd.addTranslation("en", sillyEnglish);
		strings = bd.getAllTranslationStrings("eo");
		assertEquals("Should have added one string", count+1, strings.size());
		assertEquals("Should now contain esperanto silly key", true, strings.contains(sillyEsperanto));
	}

	static UiLocalization bd;
}
