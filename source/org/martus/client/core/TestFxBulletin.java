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
package org.martus.client.core;

import java.util.Vector;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;

import org.martus.common.FieldSpecCollection;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinForTesting;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.RequiredFieldIsBlankException;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.UniversalId;
import org.martus.util.TestCaseEnhanced;

public class TestFxBulletin extends TestCaseEnhanced
{
	public TestFxBulletin(String name)
	{
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		security = MockMartusSecurity.createClient();
		localization = new MiniLocalization();
	}
	
	public void testValidate() throws Exception
	{
		FxBulletin fxb = new FxBulletin(getLocalization());
		Bulletin b = new BulletinForTesting(security);
		fxb.copyDataFromBulletin(b);

		fxb.validateData();
		
		Vector<FieldSpec> specs = fxb.getFieldSpecs();
		specs.forEach(spec -> {if(spec.getTag().equals(Bulletin.TAGAUTHOR)) spec.setRequired();});
		try
		{
			fxb.validateData();
			fail("Should have thrown for blank required fields");
		}
		catch(RequiredFieldIsBlankException ignoreExpected)
		{
		}
	}
	
	public void testVersion() throws Exception
	{
		FxBulletin fxb = new FxBulletin(getLocalization());
		ReadOnlyIntegerProperty versionPropertyNull = fxb.getVersionProperty();
		assertEquals(null, versionPropertyNull);
		
		Bulletin b = new BulletinForTesting(security);
		fxb.copyDataFromBulletin(b);
		ReadOnlyIntegerProperty versionProperty = fxb.getVersionProperty();
		assertEquals(Integer.valueOf(b.getVersion()), versionProperty.getValue());
		assertEquals(Integer.valueOf(1), versionProperty.getValue());
		
		Bulletin bulletinWith3Versions = new BulletinForTesting(security);
		bulletinWith3Versions.setSealed();
		BulletinHistory localHistory = bulletinWith3Versions.getHistory();
		localHistory.add("history1");
		localHistory.add("history2");
		bulletinWith3Versions.setHistory(localHistory);
		assertEquals("Bulletin2 doesn't have 3 versions?", 3, bulletinWith3Versions.getVersion());
		fxb.copyDataFromBulletin(bulletinWith3Versions);
		assertEquals("This is a readOnlyInteger so it will not change", Integer.valueOf(1), versionProperty.getValue());
		versionProperty = fxb.getVersionProperty();
		assertEquals(Integer.valueOf(bulletinWith3Versions.getVersion()), versionProperty.getValue());
		assertEquals(Integer.valueOf(3), versionProperty.getValue());
	}
	
	public void testUniversalId() throws Exception
	{
		FxBulletin fxb = new FxBulletin(getLocalization());
		ReadOnlyObjectWrapper<UniversalId> universalIdPropertyNull = fxb.universalIdProperty();
		assertEquals(null, universalIdPropertyNull);
		
		Bulletin b = new BulletinForTesting(security);
		fxb.copyDataFromBulletin(b);
		ReadOnlyObjectWrapper<UniversalId> universalIdProperty = fxb.universalIdProperty();
		assertEquals(b.getUniversalId(), universalIdProperty.getValue());
		
		Bulletin b2 = new BulletinForTesting(security);
		assertNotEquals("Bulletins have same id?", b.getUniversalId(), b2.getUniversalId());
		fxb.copyDataFromBulletin(b2);
		assertEquals(null, universalIdProperty.getValue());
		ReadOnlyObjectWrapper<UniversalId> universalIdProperty2 = fxb.universalIdProperty();
		assertEquals(b2.getUniversalId(), universalIdProperty2.getValue());
	}
	
	public void testAuthorizedToRead() throws Exception
	{
		FxBulletin fxb = new FxBulletin(getLocalization());
		ObservableList<HeadquartersKey> headquartersKeysListNull = fxb.authorizedToReadList();
		assertEquals(null, headquartersKeysListNull);
		
		Bulletin b = new BulletinForTesting(security);
		HeadquartersKey key1 = new HeadquartersKey("account1");
		Vector keysToUse = new Vector();
		keysToUse.add(key1);
		HeadquartersKeys keys = new HeadquartersKeys(keysToUse);
		b.setAuthorizedToReadKeys(keys);
		
		fxb.copyDataFromBulletin(b);
		ObservableList<HeadquartersKey> headquartersKeysList = fxb.authorizedToReadList();
		HeadquartersKeys keysFromBulletin = b.getAuthorizedToReadKeys();
		assertEquals(keysFromBulletin.size(), headquartersKeysList.size());
		assertEquals(key1, headquartersKeysList.get(0));
		
		Bulletin b2 = new BulletinForTesting(security);
		HeadquartersKey key2 = new HeadquartersKey("account2");
		HeadquartersKey key3 = new HeadquartersKey("account3");
		Vector newKeys = new Vector();
		newKeys.add(key2);
		newKeys.add(key3);
		HeadquartersKeys b2keys = new HeadquartersKeys(newKeys);
		b2.setAuthorizedToReadKeys(b2keys);

		fxb.copyDataFromBulletin(b2);
		ObservableList<HeadquartersKey> headquartersKeysList2 = fxb.authorizedToReadList();
		assertEquals(2, b2.getAuthorizedToReadKeys().size());
		assertEquals(2, headquartersKeysList2.size());
		assertEquals(b2.getAuthorizedToReadKeys().size(), headquartersKeysList2.size());
		assertNotEquals(headquartersKeysList.size(), headquartersKeysList2.size());
		assertTrue(headquartersKeysList2.contains(key2));
		assertTrue(headquartersKeysList2.contains(key3));
		assertFalse(headquartersKeysList2.contains(key1));
		
		headquartersKeysList2.add(key1);
		assertTrue(headquartersKeysList2.contains(key1));
		assertFalse(b2.getAuthorizedToReadKeys().contains(key1));
		Bulletin copyOfModifiedBulletinB2 = new BulletinForTesting(security);
		fxb.copyDataToBulletin(copyOfModifiedBulletinB2);
		assertTrue("After copying data back into this new modified Bulletin we don't have key that was added?", copyOfModifiedBulletinB2.getAuthorizedToReadKeys().contains(key1));
		assertEquals(3, headquartersKeysList2.size());
		assertEquals(3, copyOfModifiedBulletinB2.getAuthorizedToReadKeys().size());
		assertEquals(2, b2.getAuthorizedToReadKeys().size());
		
		
	}

	public void testTitle() throws Exception
	{
		FxBulletin fxb = new FxBulletin(getLocalization());
		Bulletin b = new BulletinForTesting(security);
		fxb.copyDataFromBulletin(b);

		SimpleStringProperty emptyTitleProperty = fxb.getFieldProperty(Bulletin.TAGTITLE);
		assertEquals("", emptyTitleProperty.getValue());
		b.set(Bulletin.TAGTITLE, "This is a title");
		fxb.copyDataFromBulletin(b);
		assertNull(emptyTitleProperty.getValue());
		SimpleStringProperty titleProperty = fxb.getFieldProperty(Bulletin.TAGTITLE);
		assertEquals(b.get(Bulletin.TAGTITLE), titleProperty.getValue());
	}
	
	public void testBottomSectionField() throws Exception
	{
		final String PRIVATE_TAG = Bulletin.TAGPRIVATEINFO;
		final String PRIVATE_DATA_1 = "private info";
		final String PRIVATE_DATA_2 = "This is new and better private info";

		FxBulletin fxb = new FxBulletin(getLocalization());
		Bulletin b = new BulletinForTesting(security);
		b.set(PRIVATE_TAG, PRIVATE_DATA_1);
		fxb.copyDataFromBulletin(b);
		
		SimpleStringProperty privateInfoProperty = fxb.getFieldProperty(PRIVATE_TAG);
		assertEquals(b.get(PRIVATE_TAG), privateInfoProperty.getValue());
		privateInfoProperty.setValue(PRIVATE_DATA_2);
		
		Bulletin modified = new Bulletin(security);
		fxb.copyDataToBulletin(modified);
		assertEquals(PRIVATE_DATA_2, modified.get(PRIVATE_TAG));
		assertEquals(PRIVATE_DATA_2, modified.getFieldDataPacket().get(PRIVATE_TAG));
		assertEquals("", modified.getPrivateFieldDataPacket().get(PRIVATE_TAG));
	}
	
	public void testFieldSequence() throws Exception
	{
		FxBulletin fxb = new FxBulletin(getLocalization());
		Bulletin before = new BulletinForTesting(security);
		fxb.copyDataFromBulletin(before);
		Bulletin after = new Bulletin(security);
		fxb.copyDataToBulletin(after);
		Vector<String> beforeTags = extractFieldTags(before);
		Vector<String> afterTags = extractFieldTags(after);
		assertEquals(beforeTags, afterTags);
	}

	private Vector<String> extractFieldTags(Bulletin b)
	{
		Vector<String> fieldTags = new Vector<String>();
		FieldSpecCollection topSpecs = b.getTopSectionFieldSpecs();
		for(int i = 0; i < topSpecs.size(); ++i)
		{
			FieldSpec fieldSpec = topSpecs.get(i);
			fieldTags.add(fieldSpec.getTag());
		}
		FieldSpecCollection bottomSpecs = b.getBottomSectionFieldSpecs();
		for(int i = 0; i < bottomSpecs.size(); ++i)
		{
			FieldSpec fieldSpec = bottomSpecs.get(i);
			fieldTags.add(fieldSpec.getTag());
		}
		
		return fieldTags;
	}
	
	private MiniLocalization getLocalization()
	{
		return localization;
	}
	
	private MockMartusSecurity security;
	private MiniLocalization localization;
}
