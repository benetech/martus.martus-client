/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2003, Beneficent
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.martus.common.ContactInfo;
import org.martus.common.FieldSpec;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.test.TestCaseEnhanced;
import org.martus.util.Base64;

public class TestContactInfo extends TestCaseEnhanced
{
    public TestContactInfo(String name) throws IOException
	{
        super(name);
    }

	public void testBasics()
	{
		ContactInfo info = new ContactInfo();
		verifyEmptyInfo(info, "constructor");

		info.setAuthor("fred");
		assertEquals("fred", info.getAuthor());
	
		info.setTemplateDetails(sampleTemplateDetails);
		assertEquals("Details not set?", sampleTemplateDetails, info.getTemplateDetails());

		info.clear();
		verifyEmptyInfo(info, "clear");
	}

	public void testHasContactInfo() throws Exception
	{
		ContactInfo info = new ContactInfo();
		info.setAuthor("fred");
		assertEquals("author isn't enough contact info?", true, info.hasContactInfo());
		info.setAuthor("");
		info.setOrganization("whatever");
		assertEquals("organization isn't enough contact info?", true, info.hasContactInfo());
	}

	public void testLoadVersions() throws Exception
	{
		for(short version = 1; version <= ContactInfo.VERSION; ++version)
		{
			byte[] data = createFileWithSampleData(version);
			ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
			verifyLoadSpecificVersion(inputStream, version);
		}
	}

	public void testSaveFull() throws Exception
	{
		ContactInfo info = new ContactInfo();

		setConfigToSampleData(info);
		verifySampleInfo(info, "testSaveFull", ContactInfo.VERSION);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		info.save(outputStream);
		outputStream.close();

		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		verifyLoadSpecificVersion(inputStream, ContactInfo.VERSION);
	}

	public void testSaveEmpty() throws Exception
	{
		ContactInfo emptyInfo = new ContactInfo();
		ByteArrayOutputStream emptyOutputStream = new ByteArrayOutputStream();
		emptyInfo.save(emptyOutputStream);
		emptyOutputStream.close();

		emptyInfo.setAuthor("should go away");
		ByteArrayInputStream emptyInputStream = new ByteArrayInputStream(emptyOutputStream.toByteArray());
		emptyInfo = ContactInfo.load(emptyInputStream);
		assertEquals("should have cleared", "", emptyInfo.getAuthor());
	}

	public void testSaveNonEmpty() throws Exception
	{
		ContactInfo info = new ContactInfo();
		String server = "server";
		info.setServerName(server);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		info.save(outputStream);
		info.setServerName("should be reverted");

		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		info = ContactInfo.load(inputStream);
		assertEquals("should have reverted", server, info.getServerName());
	}

	public void testRemoveHQKey() throws Exception
	{
		ContactInfo info = new ContactInfo();
		String hqKey = "HQKey";
		info.setHQKey(hqKey);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		info.save(outputStream);
		info.clearHQKey();
		assertEquals("HQ Key Should be cleared", "", info.getHQKey());

		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		info = ContactInfo.load(inputStream);
		assertEquals("HQ key should have reverted", hqKey, info.getHQKey());
	}


	public void testGetContactInfo() throws Exception
	{
		ContactInfo newInfo = new ContactInfo();
		newInfo.setAuthor(sampleAuthor);
		newInfo.setAddress(sampleAddress);
		newInfo.setPhone(samplePhone);
		MartusSecurity signer = new MartusSecurity();
		signer.createKeyPair(512);
		Vector contactInfo = newInfo.getEncodedContactInfo(signer);
		assertEquals("Not encoded?",NetworkInterfaceConstants.BASE_64_ENCODED,contactInfo.get(0));
		assertEquals("Wrong contactinfo size", 10, contactInfo.size());
		String publicKey = (String)contactInfo.get(1);

		assertEquals("Not the publicKey?", signer.getPublicKeyString(), publicKey);
		int contentSize = ((Integer)(contactInfo.get(2))).intValue();
		assertEquals("Not the encoded correct size?", contentSize + 4, contactInfo.size());
		assertEquals("encoded Author not correct?", sampleAuthor, new String(Base64.decode((String)contactInfo.get(3))));
		assertEquals("encoded Address not correct?", sampleAddress,  new String(Base64.decode((String)contactInfo.get(8))));
		assertEquals("encoded phone not correct?", samplePhone,  new String(Base64.decode((String)contactInfo.get(7))));
		
		Vector decodedContactInfo = ContactInfo.decodeContactInfoVectorIfNecessary(contactInfo);
		Vector alreadyDecodedContactInfo = ContactInfo.decodeContactInfoVectorIfNecessary(decodedContactInfo);
		assertEquals("Backward compatibility test, a decoded vector should be equal", decodedContactInfo, alreadyDecodedContactInfo);
		
		assertNotEquals("Still encoded?", NetworkInterfaceConstants.BASE_64_ENCODED, decodedContactInfo.get(0));
		assertEquals("contentSize not the same?", contentSize, ((Integer)decodedContactInfo.get(1)).intValue());
		assertEquals("decoded Author not correct?", sampleAuthor, decodedContactInfo.get(2));
		assertEquals("decoded Address not correct?", sampleAddress,  decodedContactInfo.get(7));
		assertEquals("decoded phone not correct?", samplePhone,  decodedContactInfo.get(6));
		
		assertTrue("Signature failed with signature in vector?", signer.verifySignatureOfVectorOfStrings(decodedContactInfo, publicKey));

		String signature = (String)decodedContactInfo.remove(decodedContactInfo.size()-1);
		assertTrue("Signature failed with signature removed from vector?", signer.verifySignatureOfVectorOfStrings(decodedContactInfo, publicKey, signature));

		
	}
	
	public void testIsServerConfigured()
	{
		ContactInfo newInfo = new ContactInfo();
		assertFalse("Didn't set up a server should not exist", newInfo.isServerConfigured());
		newInfo.setServerName("tmp Server");
		assertFalse("server publick key not set yet", newInfo.isServerConfigured());
		newInfo.setServerPublicKey("some key");
		assertTrue("Server should be setup now", newInfo.isServerConfigured());
	}
	
	
	
	void setConfigToSampleData(ContactInfo info)
	{
		info.setAuthor(sampleAuthor);
		info.setOrganization(sampleOrg);
		info.setEmail(sampleEmail);
		info.setWebPage(sampleWebPage);
		info.setPhone(samplePhone);
		info.setAddress(sampleAddress);
		info.setServerName(sampleServerName);
		info.setServerPublicKey(sampleServerKey);
		info.setTemplateDetails(sampleTemplateDetails);
		info.setHQKey(sampleHQKey);
		info.setSendContactInfoToServer(sampleSendContactInfoToServer);
		info.setServerCompliance(sampleServerCompliance);
		info.setCustomFieldSpecs(sampleCustomFieldSpecs);
	}

	void verifyEmptyInfo(ContactInfo info, String label)
	{
		assertEquals(label + ": Full has contact info", false, info.hasContactInfo());
		assertEquals(label + ": sampleSource", "", info.getAuthor());
		assertEquals(label + ": sampleOrg", "", info.getOrganization());
		assertEquals(label + ": sampleEmail", "", info.getEmail());
		assertEquals(label + ": sampleWebPage", "", info.getWebPage());
		assertEquals(label + ": samplePhone", "", info.getPhone());
		assertEquals(label + ": sampleAddress", "", info.getAddress());
		assertEquals(label + ": sampleServerName", "", info.getServerName());
		assertEquals(label + ": sampleServerKey", "", info.getServerPublicKey());
		assertEquals(label + ": sampleTemplateDetails", "", info.getTemplateDetails());
		assertEquals(label + ": sampleHQKey", "", info.getHQKey());
		assertEquals(label + ": sampleSendContactInfoToServer", false, info.shouldContactInfoBeSentToServer());
		assertEquals(label + ": sampleServerComplicance", "", info.getServerCompliance());
		assertEquals(label + ": sampleCustomFieldSpecs", defaultCustomFieldSpecs, info.getCustomFieldSpecs());

	}

	void verifySampleInfo(ContactInfo info, String label, int VERSION)
	{
		assertEquals(label + ": Full has contact info", true, info.hasContactInfo());
		assertEquals(label + ": sampleSource", sampleAuthor, info.getAuthor());
		assertEquals(label + ": sampleOrg", sampleOrg, info.getOrganization());
		assertEquals(label + ": sampleEmail", sampleEmail, info.getEmail());
		assertEquals(label + ": sampleWebPage", sampleWebPage, info.getWebPage());
		assertEquals(label + ": samplePhone", samplePhone, info.getPhone());
		assertEquals(label + ": sampleAddress", sampleAddress, info.getAddress());
		assertEquals(label + ": sampleServerName", sampleServerName, info.getServerName());
		assertEquals(label + ": sampleServerKey", sampleServerKey, info.getServerPublicKey());
		assertEquals(label + ": sampleTemplateDetails", sampleTemplateDetails, info.getTemplateDetails());
		assertEquals(label + ": sampleHQKey", sampleHQKey, info.getHQKey());
		if(VERSION >= 2)
			assertEquals(label + ": sampleSendContactInfoToServer", sampleSendContactInfoToServer, info.shouldContactInfoBeSentToServer());
		else
			assertEquals(label + ": sampleSendContactInfoToServer", false, info.shouldContactInfoBeSentToServer());
		if(VERSION >= 3)
			; // Version 3 added no data fields

		if(VERSION >= 4)
			assertEquals(label + ": sampleServerComplicance", sampleServerCompliance, info.getServerCompliance());
		else
			assertEquals(label + ": sampleServerComplicance", "", info.getServerCompliance());
		if(VERSION >= 5)
			assertEquals(label + ": sampleCustomFieldSpecs", sampleCustomFieldSpecs, info.getCustomFieldSpecs());	
		else
			assertEquals(label + ": sampleCustomFieldSpecs", defaultCustomFieldSpecs, info.getCustomFieldSpecs());
	}

	void verifyLoadSpecificVersion(ByteArrayInputStream inputStream, short VERSION)
	{
		ContactInfo info = new ContactInfo();
		info = ContactInfo.load(inputStream);
		verifySampleInfo(info, "testLoadVersion" + VERSION, VERSION);
	}

	public byte[] createFileWithSampleData(short VERSION)
		throws IOException
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(outputStream);
		out.writeShort(VERSION);
		out.writeUTF(sampleAuthor);
		out.writeUTF(sampleOrg);
		out.writeUTF(sampleEmail);
		out.writeUTF(sampleWebPage);
		out.writeUTF(samplePhone);
		out.writeUTF(sampleAddress);
		out.writeUTF(sampleServerName);
		out.writeUTF(sampleTemplateDetails);
		out.writeUTF(sampleHQKey);
		out.writeUTF(sampleServerKey);
		if(VERSION >= 2)
		{
			out.writeBoolean(sampleSendContactInfoToServer);
		}
		if(VERSION >= 3)
			; // Version 3 added no data fields
		if(VERSION >= 4)
		{
			out.writeUTF(sampleServerCompliance);
		}
		if(VERSION >= 5)
		{
			out.writeUTF(sampleCustomFieldSpecs);
		}
		out.close();
		return outputStream.toByteArray();
	}
	
	final String defaultCustomFieldSpecs = FieldSpec.buildFieldListString(FieldSpec.getDefaultPublicFieldSpecs());

//Version 1
	final String sampleAuthor = "author";
	final String sampleOrg = "org";
	final String sampleEmail = "email";
	final String sampleWebPage = "web";
	final String samplePhone = "phone";
	final String sampleAddress = "address\nline2";
	final String sampleServerName = "server name";
	final String sampleServerKey = "server pub key";
	final String sampleTemplateDetails = "details\ndetail2";
	final String sampleHQKey = "1234324234";
//Version 2
	final boolean sampleSendContactInfoToServer = true;
//Version 3
	//nothing added just signed.
//Version 4
	final String sampleServerCompliance = "I am compliant";
//Version 5
	final String sampleCustomFieldSpecs = "language;author;custom,Custom Field;title;entrydate";
}
