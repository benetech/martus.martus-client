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
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.field.MartusDateRangeField;
import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.test.UnicodeConstants;
import org.martus.util.TestCaseEnhanced;


public class TestBulletinSearcher extends TestCaseEnhanced
{

	public TestBulletinSearcher(String name)
	{
		super(name);
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
		BulletinSearcher specific = new BulletinSearcher(new SearchTreeNode(":" + fieldToSearch + ":" + sampleValue));
		assertTrue("didn't find specific field?", specific.doesMatch(b));
		BulletinSearcher wrongValue= new BulletinSearcher(new SearchTreeNode(":" + fieldToSearch + ":" + otherValue));
		assertFalse("found wrong value?", wrongValue.doesMatch(b));
		BulletinSearcher wrongField = new BulletinSearcher(new SearchTreeNode(":" + otherField + ":" + sampleValue));
		assertFalse("found in wrong field?", wrongField.doesMatch(b));
	}
	
	public void testDoesMatchNoSuchField() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		Bulletin realBulletin = new Bulletin(security);
		
		String noSuchField = "nosuchfield";
		String sampleValue = "sample data";
		SafeReadableBulletin b = new SafeReadableBulletin(realBulletin);
		BulletinSearcher contains = new BulletinSearcher(new SearchTreeNode(noSuchField + ":" + sampleValue));
		assertFalse(": matched non-existant field?", contains.doesMatch(b));
		BulletinSearcher lessThan = new BulletinSearcher(new SearchTreeNode(noSuchField + "<" + sampleValue));
		assertFalse("< matched non-existant field?", lessThan.doesMatch(b));
		
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
		String actual = b.getPossiblyNestedField(fieldToSearch).getData();
		String expressionEnd = operator + value;
		BulletinSearcher searcher = new BulletinSearcher(new SearchTreeNode(":" + fieldToSearch + ":" + expressionEnd));
		String message = caller + ": " + actual + expressionEnd;
		assertEquals(message, expected, searcher.doesMatch(b));
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
		assertEquals("hello", true, helloWithAnyDate.doesMatch(b));

		// field names should not be searched
		BulletinSearcher fieldTagWithAnyDate = new BulletinSearcher(new SearchTreeNode("author"));
		assertEquals("author", false, fieldTagWithAnyDate.doesMatch(b));
		// id should not be searched
		BulletinSearcher localIdWithAnyDate = new BulletinSearcher(new SearchTreeNode(b.getLocalId()));
		assertEquals("getLocalId()", false, localIdWithAnyDate.doesMatch(b));

		BulletinSearcher noText = new BulletinSearcher(new SearchTreeNode(""));
		assertEquals("Blank must match", true, noText.doesMatch(b));

		BulletinSearcher allCaps = new BulletinSearcher(new SearchTreeNode("HELLO"));
		assertEquals("HELLO", true, allCaps.doesMatch(b));
		BulletinSearcher utf8 = new BulletinSearcher(new SearchTreeNode("jos"+UnicodeConstants.ACCENT_E_LOWER+"e"));
		assertEquals("jos"+UnicodeConstants.ACCENT_E_LOWER+"e", true, utf8.doesMatch(b));
		BulletinSearcher utf8MixedCase = new BulletinSearcher(new SearchTreeNode("jos"+UnicodeConstants.ACCENT_E_UPPER+"e"));
		assertEquals("jos"+UnicodeConstants.ACCENT_E_UPPER+"e", true, utf8MixedCase.doesMatch(b));
		BulletinSearcher nonUtf8 = new BulletinSearcher(new SearchTreeNode("josee"));
		assertEquals("josee", false, nonUtf8.doesMatch(b));

		SearchParser parser = SearchParser.createEnglishParser();
		BulletinSearcher andRightFalse = new BulletinSearcher(parser.parse("", ":", "hello and goodbye"));
		assertEquals("right false and", false, andRightFalse.doesMatch(b));
		BulletinSearcher andLeftFalse = new BulletinSearcher(parser.parse("", ":", "goodbye and hello"));
		assertEquals("left false and", false, andLeftFalse.doesMatch(b));
		BulletinSearcher andBothTrue = new BulletinSearcher(parser.parse("", ":", "Hello and Summary"));
		assertEquals("true and", true, andBothTrue.doesMatch(b));

		BulletinSearcher orBothFalse = new BulletinSearcher(parser.parse("", ":", "swinging and swaying"));
		assertEquals("false or", false, orBothFalse.doesMatch(b));
		BulletinSearcher orRightFalse = new BulletinSearcher(parser.parse("", ":", "hello or goodbye"));
		assertEquals("left true or", true, orRightFalse.doesMatch(b));
		BulletinSearcher orLeftFalse = new BulletinSearcher(parser.parse("", ":", "goodbye or hello"));
		assertEquals("right true or", true, orLeftFalse.doesMatch(b));
		BulletinSearcher orBothTrue = new BulletinSearcher(parser.parse("", ":", "hello or summary"));
		assertEquals("both true or", true, orBothTrue.doesMatch(b));

		BulletinSearcher publicAttachmentWithAnyDate = new BulletinSearcher(new SearchTreeNode(publicProxyLabel.substring(0, publicProxyLabel.length()-4)));
		assertEquals("Public Attachment without .txt extension?", true, publicAttachmentWithAnyDate.doesMatch(b));

		BulletinSearcher privateAttachmentWithAnyDate = new BulletinSearcher(new SearchTreeNode(privateProxy.getLabel().toUpperCase()));
		assertEquals("Private Attachment?", true, privateAttachmentWithAnyDate.doesMatch(b));
	}
	
	public void testDateRangeDoesntMatchAnyString()
	{
		FieldSpec spec = FieldSpec.createStandardField("daterange", FieldSpec.TYPE_DATERANGE);
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
		String lastSaved = b.getLastSavedDate();
		
		verifyOperatorComparison("testDateMatchesLastSaved", b, "_lastSavedDate", "", lastSaved, true);
	}
		
	public void testFlexiDateMatches() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		Bulletin b = new Bulletin(security);
		b.set(Bulletin.TAGEVENTDATE, "2003-08-20,20030820+3");
		
		verifyOperatorComparison("testFlexiDateMatches", b, Bulletin.TAGEVENTDATE, "", "2003-08-20", false);
		verifyOperatorComparison("testFlexiDateMatches", b, Bulletin.TAGEVENTDATE, "", "2003-08-26", false);

		verifyOperatorComparison("testFlexiDateMatches", b, Bulletin.TAGEVENTDATE+"." + MartusDateRangeField.SUBFIELD_BEGIN, "", "2003-08-20", true);
		verifyOperatorComparison("testFlexiDateMatches", b, Bulletin.TAGEVENTDATE+"." + MartusDateRangeField.SUBFIELD_BEGIN, "", "2003-08-21", false);
		verifyOperatorComparison("testFlexiDateMatches", b, Bulletin.TAGEVENTDATE+"." + MartusDateRangeField.SUBFIELD_END, "", "2003-08-23", true);
		verifyOperatorComparison("testFlexiDateMatches", b, Bulletin.TAGEVENTDATE+"." + MartusDateRangeField.SUBFIELD_END, "", "2003-08-22", false);

		verifyOperatorComparison("testFlexiDateMatches", b, Bulletin.TAGEVENTDATE+"." + MartusDateRangeField.SUBFIELD_BEGIN, ">=", "2003-08-20", true);
		verifyOperatorComparison("testFlexiDateMatches", b, Bulletin.TAGEVENTDATE+"." + MartusDateRangeField.SUBFIELD_BEGIN, ">", "2003-08-20", false);
		verifyOperatorComparison("testFlexiDateMatches", b, Bulletin.TAGEVENTDATE+"." + MartusDateRangeField.SUBFIELD_BEGIN, "<=", "2003-08-20", true);
		verifyOperatorComparison("testFlexiDateMatches", b, Bulletin.TAGEVENTDATE+"." + MartusDateRangeField.SUBFIELD_BEGIN, "<", "2003-08-19", false);
	}
	
	public void testBooleanMatches() throws Exception
	{
		MartusCrypto security = MockMartusSecurity.createClient();
		
		FieldSpec[] publicSpecs = new FieldSpec[] 
		{
			FieldSpec.createCustomField("true", "should be true", FieldSpec.TYPE_BOOLEAN),
			FieldSpec.createCustomField("false", "should be false", FieldSpec.TYPE_BOOLEAN),
			FieldSpec.createCustomField("bogus", "will be blank", FieldSpec.TYPE_BOOLEAN),
		};
		
		Bulletin b = new Bulletin(security, publicSpecs, StandardFieldSpecs.getDefaultPrivateFieldSpecs());
		b.set("true", FieldSpec.TRUESTRING);
		b.set("false", FieldSpec.FALSESTRING);
		b.set("bogus", "");
		
		verifyOperatorComparison("testBooleanMatches", b, "true", "", FieldSpec.TRUESTRING, true);
		verifyOperatorComparison("testBooleanMatches", b, "true", "", FieldSpec.FALSESTRING, false);
		verifyOperatorComparison("testBooleanMatches", b, "false", "", FieldSpec.FALSESTRING, true);
		verifyOperatorComparison("testBooleanMatches", b, "false", "", FieldSpec.TRUESTRING, false);
		verifyOperatorComparison("testBooleanMatches", b, "bogus", "", FieldSpec.FALSESTRING, false);
		verifyOperatorComparison("testBooleanMatches", b, "bogus", "", FieldSpec.TRUESTRING, false);
		verifyOperatorComparison("testBooleanMatches", b, "true", "!=", FieldSpec.FALSESTRING, true);
		
	}
}
