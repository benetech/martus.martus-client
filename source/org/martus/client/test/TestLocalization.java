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

import java.awt.ComponentOrientation;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.SwingConstants;

import org.martus.client.swingui.EnglishStrings;
import org.martus.client.swingui.UiConstants;
import org.martus.client.swingui.UiLocalization;
import org.martus.common.MartusUtilities;
import org.martus.common.clientside.ChoiceItem;
import org.martus.common.clientside.Localization;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.common.utilities.DateUtilities;
import org.martus.jarverifier.JarVerifier;
import org.martus.swing.UiLanguageDirection;
import org.martus.util.DirectoryUtils;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeStringWriter;
import org.martus.util.UnicodeWriter;
import org.martus.util.inputstreamwithseek.StringInputStreamWithSeek;


public class TestLocalization extends TestCaseEnhanced
{
    public TestLocalization(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();
		translationDirectory = createTempDirectory();
		bd = new UiLocalization(translationDirectory, EnglishStrings.strings);
 	}
	
	protected void tearDown() throws Exception
	{
		DirectoryUtils.deleteEntireDirectoryTree(translationDirectory);
		assertFalse("Translation directory still exists?", translationDirectory.exists());
		super.tearDown();
	}
	public void testNonAsciiEnglishTranslations() throws Exception
	{
		String[] strings = EnglishStrings.strings;
		for(int i=0; i < strings.length; ++i)
		{
			String thisString = EnglishStrings.strings[i];
			char[] mtfEntry = thisString.toCharArray();
			for(int c = 0; c < mtfEntry.length - 1; ++c)
			{
				if(mtfEntry[c] == '?')
					if(Character.isLetter(mtfEntry[c+1]))
						System.out.println("Likely non-ASCII character in: " + thisString);
			}
		}
	}
	
	public void testDefaultDateFormats()
	{
		assertEquals("English should always return the default date format", DateUtilities.getDefaultDateFormatCode(), Localization.getDefaultDateFormatForLanguage(UiBasicLocalization.ENGLISH));
		assertEquals("Spanish should always return Slash DMY", DateUtilities.DMY_SLASH.getCode(), Localization.getDefaultDateFormatForLanguage(UiBasicLocalization.SPANISH));
		assertEquals("Russian should always return Dot DMY", DateUtilities.DMY_DOT.getCode(), Localization.getDefaultDateFormatForLanguage(UiBasicLocalization.RUSSIAN));
		assertEquals("Thai should always return Slash DMY", DateUtilities.DMY_SLASH.getCode(), Localization.getDefaultDateFormatForLanguage(UiBasicLocalization.THAI));
		assertEquals("Arabic should always return Slash DMY", DateUtilities.DMY_SLASH.getCode(), Localization.getDefaultDateFormatForLanguage(UiBasicLocalization.ARABIC));
		assertEquals("An unknown Language should always return the default date format", DateUtilities.getDefaultDateFormatCode(), Localization.getDefaultDateFormatForLanguage("ZZ"));
	}
	
	public void testIsLanguageFile()
	{
		assertTrue(Localization.isLanguageFile("Martus-en.mtf"));
		assertTrue(Localization.isLanguageFile("Martus-ab.mtf"));
		assertTrue(Localization.isLanguageFile("martus-ab.mtf"));
		assertTrue(Localization.isLanguageFile("MARTUS-ab.MTF"));
		assertTrue(Localization.isLanguageFile("Martus-en.mlp"));
		assertTrue(Localization.isLanguageFile("Martus-ab.mlp"));
		assertTrue(Localization.isLanguageFile("martus-ab.mlp"));
		assertTrue(Localization.isLanguageFile("MARTUS-ab.MLP"));
	}
	
	public void testValidLanguageCodes()
	{
		assertFalse("null should not be recognized", Localization.isRecognizedLanguage(null));
		assertTrue("English should be recognized", Localization.isRecognizedLanguage(UiBasicLocalization.ENGLISH));
		assertTrue("Spanish should be recognized", Localization.isRecognizedLanguage(UiBasicLocalization.SPANISH));
		assertTrue("Russian should be recognized", Localization.isRecognizedLanguage(UiBasicLocalization.RUSSIAN));
		assertFalse("Unknown should not be recognized", Localization.isRecognizedLanguage("XX"));
	}
	
	public static String test = "test";
	public static String button = "button:"+test+"=";
	public static class EnglishTestStrings
	{
		public static String strings[] = {button+"Test Button English"};
	}

	public void testIsRightToLeftLanguage() throws Exception
	{
		File tmpDir = createTempDirectory();
		tmpDir.deleteOnExit();
		
		UiLocalization directionalLanguages = new UiLocalization(tmpDir, EnglishTestStrings.strings);
		directionalLanguages.includeOfficialLanguagesOnly = false;
		directionalLanguages.setCurrentLanguageCode("en");
		assertFalse("English is a Left To Right language.", UiLanguageDirection.isRightToLeftLanguage());
		assertEquals("Components for English should be Left To Right", UiLanguageDirection.getComponentOrientation(), ComponentOrientation.LEFT_TO_RIGHT);
		assertEquals("Horizontal Alignment for English should be Left", UiLanguageDirection.getHorizontalAlignment(), SwingConstants.LEFT);

		File spanish = new File(tmpDir, "Martus-es.mtf");
		spanish.deleteOnExit();
		UnicodeWriter writer = new UnicodeWriter(spanish);
		String spanishButtonText = "My button";
		writer.writeln(button+spanishButtonText);
		writer.close();
		directionalLanguages.setCurrentLanguageCode("es");
		assertEquals("test Button for spanish not correct?", spanishButtonText, directionalLanguages.getButtonLabel(test));
		assertFalse("Spanish should be a Left to Right language.", UiLanguageDirection.isRightToLeftLanguage());
		assertEquals("Components for Spanish should be Left To Right", UiLanguageDirection.getComponentOrientation(), ComponentOrientation.LEFT_TO_RIGHT);
		assertEquals("Horizontal Alignment for Spanish should be Left", UiLanguageDirection.getHorizontalAlignment(), SwingConstants.LEFT);
		
		String arabicButtonText = "Some other translation";
		File arabic = new File(tmpDir, "Martus-ar.mtf");
		arabic.deleteOnExit();
		writer = new UnicodeWriter(arabic);
		writer.writeln(button+arabicButtonText);
		writer.writeln(Localization.MTF_RIGHT_TO_LEFT_LANGUAGE_FLAG);
		writer.close();
		directionalLanguages.setCurrentLanguageCode("ar");
		assertEquals("test Button for arabic not correct?", arabicButtonText, directionalLanguages.getButtonLabel(test));
		assertTrue("Arabic should be a Right to Left language.", UiLanguageDirection.isRightToLeftLanguage());
		assertEquals("Components for Arabic should be Right To Left", UiLanguageDirection.getComponentOrientation(), ComponentOrientation.RIGHT_TO_LEFT);
		assertEquals("Horizontal Alignment for Arabic should be Right", UiLanguageDirection.getHorizontalAlignment(), SwingConstants.RIGHT);
	}

	public void testToFileNameForeignChars()
	{
		String english = "abcdefghijklmnopqrstuvwxyz";
		assertEquals(english.substring(0, 20), MartusUtilities.toFileName(english));
		//TODO add test for russian.
	}
	
	public void testDefaultLanguages()
	{
		ChoiceItem[] languages = bd.getUiLanguages();
		assertTrue("Should have multiple languages", languages.length > 1);
		boolean foundEnglish = doesLanguageExist(bd, Localization.ENGLISH);
		assertTrue("must have english", foundEnglish);
	}
	
	public void testGetTranslationFileAllowUnofficial() throws Exception
	{
		File translationDirectory = createTempDirectory();
		UiLocalization tmpLocalization = new UiLocalization(translationDirectory, EnglishStrings.strings);
		tmpLocalization.includeOfficialLanguagesOnly = false;
		String languageCode = "ff";
		assertNull("Language doesn't exists should return null", tmpLocalization.getTranslationFile(languageCode));
		File mlpkTranslation = new File(translationDirectory, UiBasicLocalization.getMlpkFilename(languageCode));
		mlpkTranslation.deleteOnExit();
		String data = "test";
		writeDataToFile(mlpkTranslation, data);
		assertEquals("MLP file exists should return it.", mlpkTranslation, tmpLocalization.getTranslationFile(languageCode));
		File mtfTranslation = new File(translationDirectory, UiBasicLocalization.getMtfFilename(languageCode));
		mtfTranslation.deleteOnExit();
		writeDataToFile(mtfTranslation, data);
		assertEquals("MTF file exists should superceed MLP file.", mtfTranslation, tmpLocalization.getTranslationFile(languageCode));
		
	}
	
	public void testGetTranslationFileAllowOfficialOnly() throws Exception
	{
		File translationDirectory = createTempDirectory();
		UiLocalization tmpLocalization = new UiLocalization(translationDirectory, EnglishStrings.strings);
		String languageCode = "ff";
		assertNull("Language doesn't exists should return null", tmpLocalization.getTranslationFile(languageCode));
		File mlpkTranslation = new File(translationDirectory, UiBasicLocalization.getMlpkFilename(languageCode));
		mlpkTranslation.deleteOnExit();
		String data = "test";
		writeDataToFile(mlpkTranslation, data);
		assertNull("MLP file exists but isn't signed should return null", tmpLocalization.getTranslationFile(languageCode));
		File mtfTranslation = new File(translationDirectory, UiBasicLocalization.getMtfFilename(languageCode));
		mtfTranslation.deleteOnExit();
		writeDataToFile(mtfTranslation, data);
		assertNull("MTF file exists but we don't allow unofficial translations", tmpLocalization.getTranslationFile(languageCode));
		
	}

	private void writeDataToFile(File mlpkTranslation, String data) throws IOException
	{
		UnicodeWriter out = new UnicodeWriter(mlpkTranslation);
		out.write(data);
		out.close();
	}

	public void testAddedMTFLanguageFile() throws Exception
	{
		File translationDirectory = createTempDirectory();
		UiLocalization myLocalization = new UiLocalization(translationDirectory, EnglishStrings.strings);
		myLocalization.includeOfficialLanguagesOnly = false;
		assertTrue("Default English should always be trusted.", myLocalization.isOfficialTranslation("en"));

		String someTestLanguageCode = "zz";
		boolean foundSomeTestLanguage = doesLanguageExist(myLocalization, someTestLanguageCode);
		assertFalse("must not have testLanguage yet", foundSomeTestLanguage);
		
		File someTestLanguageFile = new File(translationDirectory,UiBasicLocalization.getMtfFilename(someTestLanguageCode));
		someTestLanguageFile.deleteOnExit();
		String buttonName = "ok";
		String someLanguageTranslationOfOk = "dkjfl";
		writeDataToFile(someTestLanguageFile, "button:"+buttonName+"="+someLanguageTranslationOfOk);
		
		foundSomeTestLanguage = doesLanguageExist(myLocalization, someTestLanguageCode);
		assertTrue("should now have testLanguage", foundSomeTestLanguage);
		assertFalse("An mtf file should always be untrusted", myLocalization.isOfficialTranslation(someTestLanguageCode));
		myLocalization.setCurrentLanguageCode(someTestLanguageCode);
		assertEquals("Incorrect translation", someLanguageTranslationOfOk, myLocalization.getButtonLabel(buttonName));
	}

	public void testAddedUnsignedMTFLanguageFileOfficialOnly() throws Exception
	{
		File translationDirectory = createTempDirectory();
		UiLocalization myLocalization = new UiLocalization(translationDirectory, EnglishStrings.strings);

		String someTestLanguageCode = "zz";
		
		File someTestLanguageFile = new File(translationDirectory,UiBasicLocalization.getMtfFilename(someTestLanguageCode));
		someTestLanguageFile.deleteOnExit();
		String buttonName = "ok";
		String someLanguageTranslationOfOk = "dkjfl";
		writeDataToFile(someTestLanguageFile, "button:"+buttonName+"="+someLanguageTranslationOfOk);
		
		myLocalization.setCurrentLanguageCode(someTestLanguageCode);
		assertEquals("Incorrect translation", "<OK>", myLocalization.getButtonLabel(buttonName));
	}

	public void testJarVerifier() throws Exception
	{
		assertEquals("no file", JarVerifier.ERROR_INVALID_JAR, JarVerifier.verify("nonexistentFile", false));
		assertEquals("no maifest", JarVerifier.ERROR_JAR_NOT_SIGNED, JarVerifier.verify(getClass().getResource("Martus-xx-noManifest.mlp").getFile(), false));
		assertEquals("Missing Entry", JarVerifier.ERROR_MISSING_ENTRIES, JarVerifier.verify(getClass().getResource("Martus-xx-MissingEntry.mlp").getFile(), false));
		assertEquals("Modified Entry", JarVerifier.ERROR_JAR_NOT_SIGNED, JarVerifier.verify(getClass().getResource("Martus-xx-ModifiedEntry.mlp").getFile(), false));
		assertEquals("Not Signed", JarVerifier.ERROR_JAR_NOT_SIGNED, JarVerifier.verify(getClass().getResource("Martus-xx-notSigned.mlp").getFile(), false));
		assertEquals("Not sealed", JarVerifier.ERROR_JAR_NOT_SEALED, JarVerifier.verify(getClass().getResource("Martus-xx-notSealed.mlp").getFile(), false));
		assertEquals("A valid signed jar didn't pass?", JarVerifier.JAR_VERIFIED_TRUE, JarVerifier.verify(getClass().getResource("Martus-xx.mlp").getFile(), false));
	}
	
	public void testAddedMTPKLanguagePackWhenWeAllowUnofficialTranslations() throws Exception
	{
		File translationDirectory = createTempDirectory();
		UiLocalization myLocalization = new UiLocalization(translationDirectory, EnglishStrings.strings);
		myLocalization.includeOfficialLanguagesOnly = false;

		String someTestLanguageCode = "xx";
		boolean foundSomeTestLanguage = doesLanguageExist(myLocalization, someTestLanguageCode);
		assertFalse("must not have testLanguage from pack yet", foundSomeTestLanguage);

		File someTestLanguage = new File(translationDirectory,UiBasicLocalization.getMlpkFilename(someTestLanguageCode));
		someTestLanguage.deleteOnExit();
		copyResourceFileToLocalFile(someTestLanguage, "Martus-xx.mlp");
		
		foundSomeTestLanguage = doesLanguageExist(myLocalization, someTestLanguageCode);
		assertTrue("should now have testLanguage", foundSomeTestLanguage);
		myLocalization.setCurrentLanguageCode(someTestLanguageCode);
		assertEquals("Incorrect translation OK from within language pack", "OK", myLocalization.getButtonLabel("ok"));
		assertEquals("Incorrect translation No from within language pack", "No", myLocalization.getButtonLabel("no"));
		assertTrue("A signed MLP file should be trusted", myLocalization.isOfficialTranslation(someTestLanguageCode));

		File translationDirectory2 = createTempDirectory();
		UiLocalization myLocalization2 = new UiLocalization(translationDirectory2, EnglishStrings.strings);
		myLocalization2.includeOfficialLanguagesOnly = false;
		File someTestLanguage2 = new File(translationDirectory2,UiBasicLocalization.getMlpkFilename(someTestLanguageCode));
		someTestLanguage2.deleteOnExit();
		copyResourceFileToLocalFile(someTestLanguage2, "Martus-xx-notSigned.mlp");
		foundSomeTestLanguage = doesLanguageExist(myLocalization2, someTestLanguageCode);
		assertTrue("should still have testLanguage even if its not signed.", foundSomeTestLanguage);
		myLocalization2.setCurrentLanguageCode(someTestLanguageCode);
		assertEquals("Incorrect translation OK from within unsigned language pack", "OK", myLocalization2.getButtonLabel("ok"));
		assertEquals("Incorrect translation No from within unsigned language pack", "No", myLocalization2.getButtonLabel("no"));
		assertFalse("A unsigned MLPK file should not be trusted", myLocalization2.isOfficialTranslation(someTestLanguageCode));
		assertFalse("Current translation should not be trusted", myLocalization2.isCurrentTranslationOfficial());
		
		assertFalse("A non existant translation should not be trusted.",myLocalization2.isOfficialTranslation("dx"));
	}

	public void testAddedMTPKLanguagePackWhenWeAllowONLYOfficialTranslations() throws Exception
	{
		File translationDirectory = createTempDirectory();
		UiLocalization myLocalization = new UiLocalization(translationDirectory, EnglishStrings.strings);

		String someTestLanguageCode = "xx";
		boolean foundSomeTestLanguage = doesLanguageExist(myLocalization, someTestLanguageCode);
		assertFalse("must not have testLanguage from pack yet", foundSomeTestLanguage);

		File someTestLanguage = new File(translationDirectory,UiBasicLocalization.getMlpkFilename(someTestLanguageCode));
		someTestLanguage.deleteOnExit();
		copyResourceFileToLocalFile(someTestLanguage, "Martus-xx.mlp");
		
		foundSomeTestLanguage = doesLanguageExist(myLocalization, someTestLanguageCode);
		assertTrue("should have testLanguage since it is official", foundSomeTestLanguage);
		myLocalization.setCurrentLanguageCode(someTestLanguageCode);
		assertEquals("Incorrect translation OK from within language pack", "OK", myLocalization.getButtonLabel("ok"));
		assertEquals("Incorrect translation No from within language pack", "No", myLocalization.getButtonLabel("no"));
		assertTrue("A signed MLP file should be trusted", myLocalization.isOfficialTranslation(someTestLanguageCode));

		File translationDirectory2 = createTempDirectory();
		UiLocalization myLocalization2 = new UiLocalization(translationDirectory2, EnglishStrings.strings);
		File someTestLanguage2 = new File(translationDirectory2,UiBasicLocalization.getMlpkFilename(someTestLanguageCode));
		someTestLanguage2.deleteOnExit();
		copyResourceFileToLocalFile(someTestLanguage2, "Martus-xx-notSigned.mlp");
		foundSomeTestLanguage = doesLanguageExist(myLocalization2, someTestLanguageCode);
		assertFalse("should not have testLanguage because its not signed.", foundSomeTestLanguage);
		assertFalse("A unsigned MLPK file should not be trusted", myLocalization2.isOfficialTranslation(someTestLanguageCode));
		assertFalse("A non existant translation should not be trusted.",myLocalization2.isOfficialTranslation("dx"));
		myLocalization2.setCurrentLanguageCode(someTestLanguageCode);
		assertFalse("Current translation should be trusted", myLocalization2.isCurrentTranslationOfficial());
	}

	private boolean doesLanguageExist(UiLocalization dbToUse, String languageCode)
	{
		ChoiceItem[] languages = dbToUse.getUiLanguages();
		boolean foundSomeTestLanguage = false;
		for(int i = 0; i < languages.length; ++i)
		{
			String code = languages[i].getCode();
			if(code.equals(languageCode))
				foundSomeTestLanguage = true;
		}
		return foundSomeTestLanguage;
	}

	public void testLanguageCodeFromFilename()
	{
		assertEquals("", Localization.getLanguageCodeFromFilename("Martus.mtf"));
		assertEquals("", Localization.getLanguageCodeFromFilename("Martus-es.xyz"));
		assertEquals("es", Localization.getLanguageCodeFromFilename("Martus-es.mtf"));
		assertEquals("Martus mtf files are not case Sensitive", "es", Localization.getLanguageCodeFromFilename("martus-es.mtf"));
	}

	public void testTranslations()
	{
		bd.loadTranslationFile("es");

		assertEquals("Print", bd.getLabel("en", "button", "print"));
		assertEquals("No translation found", "<whatever:not in the map>", bd.getLabel("en", "whatever", "not in the map"));

		assertEquals("Imprimir", bd.getLabel("es", "button", "print"));

		assertEquals("<category:sillytag>", bd.getLabel("en", "category", "sillytag"));
		assertEquals("<category:sillytag>", bd.getLabel("es", "category", "sillytag"));
		bd.addTranslation("en", "category:sillytag=something");
		assertEquals("<something>", bd.getLabel("es", "category", "sillytag"));
		assertEquals("something", bd.getLabel("en", "category", "sillytag"));
		bd.addTranslation("es", "category:sillytag=es/something");
		assertEquals("es/something", bd.getLabel("es", "category", "sillytag"));

		assertEquals("<Print>", bd.getLabel("xx", "button", "print"));
	}
	
	public void testLoadTranslations() throws Exception
	{
		bd.addTranslation(Localization.ENGLISH, "a:b=jfjfj");
		bd.addTranslation(Localization.ENGLISH, "d:e=83838");
		String sampleFileContents = 
				"# This is a comment with =\n" +
				"a:b=c\n" +
				"d:e=f";
		StringInputStreamWithSeek in = new StringInputStreamWithSeek(sampleFileContents);
		bd.loadTranslations("qq", in);
		assertEquals("c", bd.getLabel("qq", "a", "b"));
		assertEquals("f", bd.getLabel("qq", "d", "e"));
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
		assertEquals("<b:c>", bd.getLabel("xx", "b", "c"));
		bd.addTranslation("en", "b:c=bc");
		assertEquals("<bc>", bd.getLabel("xx", "b", "c"));
		bd.addTranslation("a", "invalid=because-bad-language");
		bd.addTranslation("en", null);
		bd.addTranslation("en", "invalid-because-no-equals");
		bd.addTranslation("en", "b:c=new\\nline");
		assertEquals("new\nline", bd.getLabel("en", "b", "c"));
		
		String badHash = "ffff";
		bd.addTranslation("xx", "-" + badHash + "-b:c=def");
		assertEquals("<def>", bd.getLabel("xx", "b", "c"));
		String goodHash = bd.getHashOfEnglish("b:c");
		String goodMtfEntry = "-" + goodHash + "-b:c=def";
		bd.addTranslation("xx", goodMtfEntry);
		assertEquals("def", bd.getLabel("xx", "b", "c"));
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

	public void testGetAllTranslationStrings() throws Exception
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
		assertEquals("But still no esperanto silly key", false, strings.contains(sillyEsperanto));

		final String withNewlines = "d:e=f\ng\nh";
		UiBasicLocalization minimalLocalization = new UiBasicLocalization(createTempDirectory(), new String[0]);
		minimalLocalization.addTranslation("en", withNewlines);
		assertContains("-25fb-d:e=f\\ng\\nh", minimalLocalization.getAllTranslationStrings("en"));
	}

	static UiLocalization bd;
	File translationDirectory;
}
