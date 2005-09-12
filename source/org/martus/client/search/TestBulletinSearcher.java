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

package org.martus.client.search;

import java.io.File;

import org.martus.client.core.SafeReadableBulletin;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.field.MartusDateRangeField;
import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.FieldTypeDateRange;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.test.UnicodeConstants;
import org.martus.util.TestCaseEnhanced;


public class TestBulletinSearcher extends TestCaseEnhanced
{

	public TestBulletinSearcher(String name)
	{
		super(name);
	}
	
	public void setUp()
	{
		 localization = new MiniLocalization();
	}
	
	public void testDoesMatchSpecificField() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		Bulletin realBulletin = new Bulletin(security);

		String fieldToSearch = Bulletin.TAGLOCATION;
		String otherField = Bulletin.TAGAUTHOR;
		String sampleValue = "green";
		String otherValue = "ignoreme";
		realBulletin.set(fieldToSearch, sampleValue);
		realBulletin.set(otherField, otherValue);
		
		SafeReadableBulletin b = new SafeReadableBulletin(realBulletin);
		BulletinSearcher specific = new BulletinSearcher(new SearchTreeNode(fieldToSearch, "", sampleValue));
		assertTrue("didn't find specific field?", specific.doesMatch(b, localization));
		BulletinSearcher wrongValue= new BulletinSearcher(new SearchTreeNode(fieldToSearch, "", otherValue));
		assertFalse("found wrong value?", wrongValue.doesMatch(b, localization));
		BulletinSearcher wrongField = new BulletinSearcher(new SearchTreeNode(otherField, "", sampleValue));
		assertFalse("found in wrong field?", wrongField.doesMatch(b, localization));
	}
	
	public void testDoesMatchNoSuchField() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		Bulletin realBulletin = new Bulletin(security);
		
		String noSuchField = "nosuchfield";
		String sampleValue = "sample data";
		SafeReadableBulletin b = new SafeReadableBulletin(realBulletin);
		BulletinSearcher contains = new BulletinSearcher(new SearchTreeNode(noSuchField, "", sampleValue));
		assertFalse(": matched non-existant field?", contains.doesMatch(b, localization));
		BulletinSearcher lessThan = new BulletinSearcher(new SearchTreeNode(noSuchField, "<", sampleValue));
		assertFalse("< matched non-existant field?", lessThan.doesMatch(b, localization));
		
	}

	public void testDoesMatchComparisons() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		Bulletin b = new Bulletin(security);

		String fieldToSearch = Bulletin.TAGLOCATION;
		String belowSample = "blue";
		String sampleValue = "green";
		String aboveSample = "red";
		b.set(fieldToSearch, sampleValue);

		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, ">=", belowSample, true);
		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, ">=", sampleValue, true);
		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, ">=", aboveSample, false);

		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, ">", belowSample, true);
		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, ">", sampleValue, false);
		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, ">", aboveSample, false);

		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, "<=", belowSample, false);
		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, "<=", sampleValue, true);
		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, "<=", aboveSample, true);

		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, "<", belowSample, false);
		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, "<", sampleValue, false);
		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, "<", aboveSample, true);

		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, "=", belowSample, false);
		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, "=", sampleValue, true);
		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, "=", aboveSample, false);

		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, "!=", belowSample, true);
		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, "!=", sampleValue, false);
		verifyOperatorComparison("testDoesMatchComparisons", b, fieldToSearch, "!=", aboveSample, true);
	}

	private void verifyOperatorComparison(String caller, Bulletin realBulletin, String fieldToSearch, String operator, String value, boolean expected)
	{
		SafeReadableBulletin b = new SafeReadableBulletin(realBulletin);
		String actual = b.getPossiblyNestedField(fieldToSearch).getSearchableData(localization);
		BulletinSearcher searcher = new BulletinSearcher(new SearchTreeNode(fieldToSearch, operator, value));
		String message = caller + ": " + actual + " " + operator + value + " ";
		assertEquals(message, expected, searcher.doesMatch(b, localization));
	}
	
	public void testGetPossiblyNestedField() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		Bulletin realBulletin = new Bulletin(security);
		SafeReadableBulletin b = new SafeReadableBulletin(realBulletin);
		MartusField noSuchField = b.getPossiblyNestedField("no.such.field");
		assertNull("didn't return null for bogus field?", noSuchField);
		MartusField noSubfield = b.getPossiblyNestedField("entrydate.no.such.subfield");
		assertNull("didn't return null for bogus subfield?", noSubfield);
	}
	
	
	public void testDoesMatch() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		Bulletin realBulletin = new Bulletin(security);
		realBulletin.set("author", "hello");
		realBulletin.set("summary", "summary");
		realBulletin.set("title", "Jos"+UnicodeConstants.ACCENT_E_LOWER+"e");
		realBulletin.set(Bulletin.TAGEVENTDATE, "2002-04-04");
		realBulletin.set(Bulletin.TAGENTRYDATE, "2002-10-15");
		byte[] sampleBytes1 = {1,1,2,3,0,5,7,11};
		byte[] sampleBytes2 = {3,1,4,0,1,5,9,2,7};
		File tempFile1 = createTempFileWithData(sampleBytes1);
		File tempFile2 = createTempFileWithData(sampleBytes2);
		AttachmentProxy publicProxy = new AttachmentProxy(tempFile1);
		String publicProxyLabel = "publicProxy.txt";
		publicProxy.setLabel(publicProxyLabel);
		AttachmentProxy privateProxy = new AttachmentProxy(tempFile2);

		realBulletin.addPublicAttachment(publicProxy);
		realBulletin.addPrivateAttachment(privateProxy);

		SafeReadableBulletin b = new SafeReadableBulletin(realBulletin);

		BulletinSearcher helloWithAnyDate = new BulletinSearcher(new SearchTreeNode("hello"));
		assertEquals("hello", true, helloWithAnyDate.doesMatch(b, localization));

		// field names should not be searched
		BulletinSearcher fieldTagWithAnyDate = new BulletinSearcher(new SearchTreeNode("author"));
		assertEquals("author", false, fieldTagWithAnyDate.doesMatch(b, localization));
		// id should not be searched
		BulletinSearcher localIdWithAnyDate = new BulletinSearcher(new SearchTreeNode(b.getLocalId()));
		assertEquals("getLocalId()", false, localIdWithAnyDate.doesMatch(b, localization));

		BulletinSearcher noText = new BulletinSearcher(new SearchTreeNode(""));
		assertEquals("Blank must match", true, noText.doesMatch(b, localization));

		BulletinSearcher allCaps = new BulletinSearcher(new SearchTreeNode("HELLO"));
		assertEquals("HELLO", true, allCaps.doesMatch(b, localization));
		BulletinSearcher utf8 = new BulletinSearcher(new SearchTreeNode("jos"+UnicodeConstants.ACCENT_E_LOWER+"e"));
		assertEquals("jos"+UnicodeConstants.ACCENT_E_LOWER+"e", true, utf8.doesMatch(b, localization));
		BulletinSearcher utf8MixedCase = new BulletinSearcher(new SearchTreeNode("jos"+UnicodeConstants.ACCENT_E_UPPER+"e"));
		assertEquals("jos"+UnicodeConstants.ACCENT_E_UPPER+"e", true, utf8MixedCase.doesMatch(b, localization));
		BulletinSearcher nonUtf8 = new BulletinSearcher(new SearchTreeNode("josee"));
		assertEquals("josee", false, nonUtf8.doesMatch(b, localization));

		SearchParser parser = SearchParser.createEnglishParser();
		BulletinSearcher andRightFalse = new BulletinSearcher(parser.parseJustAmazonValueForTesting("hello and goodbye"));
		assertEquals("right false and", false, andRightFalse.doesMatch(b, localization));
		BulletinSearcher andLeftFalse = new BulletinSearcher(parser.parseJustAmazonValueForTesting("goodbye and hello"));
		assertEquals("left false and", false, andLeftFalse.doesMatch(b, localization));
		BulletinSearcher andBothTrue = new BulletinSearcher(parser.parseJustAmazonValueForTesting("Hello and Summary"));
		assertEquals("true and", true, andBothTrue.doesMatch(b, localization));

		BulletinSearcher orBothFalse = new BulletinSearcher(parser.parseJustAmazonValueForTesting("swinging and swaying"));
		assertEquals("false or", false, orBothFalse.doesMatch(b, localization));
		BulletinSearcher orRightFalse = new BulletinSearcher(parser.parseJustAmazonValueForTesting("hello or goodbye"));
		assertEquals("left true or", true, orRightFalse.doesMatch(b, localization));
		BulletinSearcher orLeftFalse = new BulletinSearcher(parser.parseJustAmazonValueForTesting("goodbye or hello"));
		assertEquals("right true or", true, orLeftFalse.doesMatch(b, localization));
		BulletinSearcher orBothTrue = new BulletinSearcher(parser.parseJustAmazonValueForTesting("hello or summary"));
		assertEquals("both true or", true, orBothTrue.doesMatch(b, localization));

		BulletinSearcher publicAttachmentWithAnyDate = new BulletinSearcher(new SearchTreeNode(publicProxyLabel.substring(0, publicProxyLabel.length()-4)));
		assertEquals("Public Attachment without .txt extension?", true, publicAttachmentWithAnyDate.doesMatch(b, localization));

		BulletinSearcher privateAttachmentWithAnyDate = new BulletinSearcher(new SearchTreeNode(privateProxy.getLabel().toUpperCase()));
		assertEquals("Private Attachment?", true, privateAttachmentWithAnyDate.doesMatch(b, localization));
	}
	
	public void testDateRangeShouldntMatchAnyRandomString()
	{
		FieldSpec spec = FieldSpec.createStandardField("daterange", new FieldTypeDateRange());
		MartusDateRangeField dateRange = new MartusDateRangeField(spec);
		dateRange.setData("");
		assertFalse("empty date range contains a string?", dateRange.contains("lsijflidj"));
	}

	public void testLocalId() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		Bulletin b = new Bulletin(security);
		
		verifyOperatorComparison("testLocalId", b, "_localId", "", b.getLocalId(), true);
	}
		
	public void testDateMatchesLastSaved() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		Bulletin b = new Bulletin(security);
		b.getBulletinHeaderPacket().updateLastSavedTime();
		String rawLastSaved = b.getLastSavedDate();
		String formattedLastSaved = localization.convertStoredDateToDisplay(rawLastSaved);
		
		verifyOperatorComparison("testDateMatchesLastSaved", b, "_lastSavedDate", "", formattedLastSaved, true);
	}
		
	public void testFlexiDateMatches() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		Bulletin b = new Bulletin(security);
		b.set(Bulletin.TAGEVENTDATE, "2003-08-20,20030820+3");
		
		verifyOperatorComparison("testFlexiDateMatches", b, Bulletin.TAGEVENTDATE, "", "08/20/2003", true);
		verifyOperatorComparison("testFlexiDateMatches", b, Bulletin.TAGEVENTDATE, "", "08/21/2003", false);
		verifyOperatorComparison("testFlexiDateMatches", b, Bulletin.TAGEVENTDATE, "", "08/23/2003", true);
		verifyOperatorComparison("testFlexiDateMatches", b, Bulletin.TAGEVENTDATE, "", "08/26/2003", false);

		verifyOperatorComparison("testFlexiDateMatches", b, Bulletin.TAGEVENTDATE+"." + MartusDateRangeField.SUBFIELD_BEGIN, "", "08/20/2003", true);
		verifyOperatorComparison("testFlexiDateMatches", b, Bulletin.TAGEVENTDATE+"." + MartusDateRangeField.SUBFIELD_BEGIN, "", "08/21/2003", false);
		verifyOperatorComparison("testFlexiDateMatches", b, Bulletin.TAGEVENTDATE+"." + MartusDateRangeField.SUBFIELD_END, "", "08/23/2003", true);
		verifyOperatorComparison("testFlexiDateMatches", b, Bulletin.TAGEVENTDATE+"." + MartusDateRangeField.SUBFIELD_END, "", "08/22/2003", false);

		verifyOperatorComparison("testFlexiDateMatches", b, Bulletin.TAGEVENTDATE+"." + MartusDateRangeField.SUBFIELD_BEGIN, ">=", "08/20/2003", true);
		verifyOperatorComparison("testFlexiDateMatches", b, Bulletin.TAGEVENTDATE+"." + MartusDateRangeField.SUBFIELD_BEGIN, ">", "08/20/2003", false);
		verifyOperatorComparison("testFlexiDateMatches", b, Bulletin.TAGEVENTDATE+"." + MartusDateRangeField.SUBFIELD_BEGIN, "<=", "08/20/2003", true);
		verifyOperatorComparison("testFlexiDateMatches", b, Bulletin.TAGEVENTDATE+"." + MartusDateRangeField.SUBFIELD_BEGIN, "<", "08/19/2003", false);
	}
	
	public void testBooleanMatches() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		
		FieldSpec[] publicSpecs = new FieldSpec[] 
		{
			FieldSpec.createCustomField("true", "should be true", new FieldTypeBoolean()),
			FieldSpec.createCustomField("false", "should be false", new FieldTypeBoolean()),
			FieldSpec.createCustomField("bogus", "will be blank", new FieldTypeBoolean()),
		};
		
		Bulletin b = new Bulletin(security, publicSpecs, StandardFieldSpecs.getDefaultPrivateFieldSpecs());
		b.set("true", FieldSpec.TRUESTRING);
		b.set("false", FieldSpec.FALSESTRING);
		b.set("bogus", "");
		
		String localizedTrue = localization.getButtonLabel("yes");
		String localizedFalse = localization.getButtonLabel("no");
		
		verifyOperatorComparison("testBooleanMatches", b, "true", "", localizedTrue, true);
		verifyOperatorComparison("testBooleanMatches", b, "true", "", localizedFalse, false);
		verifyOperatorComparison("testBooleanMatches", b, "false", "", localizedFalse, true);
		verifyOperatorComparison("testBooleanMatches", b, "false", "", localizedTrue, false);
		verifyOperatorComparison("testBooleanMatches", b, "bogus", "", localizedFalse, true);
		verifyOperatorComparison("testBooleanMatches", b, "bogus", "", localizedTrue, false);
		verifyOperatorComparison("testBooleanMatches", b, "true", "!=", localizedFalse, true);
		
	}
	
	public void testMatchingSearchableNotRaw() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		Bulletin b = new Bulletin(security);
		
		String localizedArabic = localization.getLanguageName(MiniLocalization.ARABIC); 
		b.set(Bulletin.TAGLANGUAGE, MiniLocalization.ARABIC);
		BulletinSearcher contains = new BulletinSearcher(new SearchTreeNode(Bulletin.TAGLANGUAGE , "", localizedArabic));
		assertTrue("not looking at searchable form?", contains.doesMatch(new SafeReadableBulletin(b), localization));
		BulletinSearcher equals  = new BulletinSearcher(new SearchTreeNode(Bulletin.TAGLANGUAGE , "=", localizedArabic));
		assertTrue("not looking at searchable form?", equals.doesMatch(new SafeReadableBulletin(b), localization));
		
		
	}
	
	MiniLocalization localization;
}
