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

package org.martus.client.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.martus.common.LegacyCustomFields;
import org.martus.common.StandardFieldSpecs;

public class ConfigInfo implements Serializable
{
	public ConfigInfo()
	{
		clear();
	}

	public boolean hasEnoughContactInfo()
	{
		if(author != null && author.length() > 0)
			return true;

		if(organization != null && organization.length() > 0)
			return true;

		return false;
	}

	public void setAuthor(String newSource)		{ author = newSource; }
	public void setOrganization(String newOrg)		{ organization = newOrg; }
	public void setEmail(String newEmail)			{ email = newEmail; }
	public void setWebPage(String newWebPage)		{ webPage = newWebPage; }
	public void setPhone(String newPhone)			{ phone = newPhone; }
	public void setAddress(String newAddress)		{ address = newAddress; }
	public void setServerName(String newServerName){ serverName = newServerName; }
	public void setServerPublicKey(String newServerPublicKey){serverPublicKey = newServerPublicKey; }
	public void setTemplateDetails(String newTemplateDetails){ templateDetails = newTemplateDetails; }
	public void setHQKey(String newHQKey)			{ hqKey = newHQKey; }
	public void setSendContactInfoToServer(boolean newSendContactInfoToServer) {sendContactInfoToServer = newSendContactInfoToServer; }
	public void setServerCompliance(String newCompliance) {serverCompliance = newCompliance;}
	public void setCustomFieldSpecs(String newSpecs)	{customFieldSpecs = newSpecs;}
	public void setCustomFieldXml(String newXml)	{customFieldXml = newXml;}
	public void setForceBulletinsAllPrivate(boolean newForceBulletinsAllPrivate)	{forceBulletinsAllPrivate = newForceBulletinsAllPrivate; }
	public void setBackedUpKeypairEncrypted(boolean newBackedUpKeypairEncrypted)	{backedUpKeypairEncrypted = newBackedUpKeypairEncrypted; }
	public void setBackedUpKeypairShare(boolean newBackedUpKeypairShare)	{backedUpKeypairShare = newBackedUpKeypairShare; }
	public void setAllHQKeysXml(String allHQKeysXml){this.allHQKeysXml = allHQKeysXml;}

	public void clearHQKey()						{ hqKey = ""; }
	public void clearPromptUserRequestSendToServer() { mustAskUserToSendToServer = false; }

	public short getVersion()			{ return version; }
	public String getAuthor()			{ return author; }
	public String getOrganization()	{ return organization; }
	public String getEmail()			{ return email; }
	public String getWebPage()			{ return webPage; }
	public String getPhone()			{ return phone; }
	public String getAddress()			{ return address; }
	public String getServerName()		{ return serverName; }
	public String getServerPublicKey()	{ return serverPublicKey; }
	public String getTemplateDetails() { return templateDetails; }
	public String getHQKey() 			{ return hqKey; }
	public boolean shouldContactInfoBeSentToServer() { return sendContactInfoToServer; }
	public boolean promptUserRequestSendToServer() { return mustAskUserToSendToServer; }
	public String getServerCompliance() {return serverCompliance;}
	public String getCustomFieldSpecs() {return customFieldSpecs;}
	public String getCustomFieldXml()	{return customFieldXml;}
	public boolean shouldForceBulletinsAllPrivate()	{ return forceBulletinsAllPrivate;}
	public boolean hasUserBackedUpKeypairEncrypted()	{ return backedUpKeypairEncrypted;}
	public boolean hasUserBackedUpKeypairShare()	{ return backedUpKeypairShare;}
	public String getAllHQKeysXml()		{return allHQKeysXml;}

	public boolean isServerConfigured()
	{
		return (serverName.length()>0 && serverPublicKey.length()>0);
	}
	
	public void clear()
	{
		version = VERSION;
		author = "";
		organization = "";
		email = "";
		webPage = "";
		phone = "";
		address = "";
		serverName = "";
		serverPublicKey="";
		templateDetails = "";
		hqKey = "";
		sendContactInfoToServer = false;
		mustAskUserToSendToServer = false;
		serverCompliance = "";
		customFieldSpecs = LegacyCustomFields.buildFieldListString(StandardFieldSpecs.getDefaultPublicFieldSpecs());
		customFieldXml = "";
		forceBulletinsAllPrivate = false;
		backedUpKeypairEncrypted = false;
		backedUpKeypairShare = false;
		allHQKeysXml = "";
	}

	public static ConfigInfo load(InputStream inputStream)
	{
		ConfigInfo loaded =  new ConfigInfo();
		try
		{
			DataInputStream in = new DataInputStream(inputStream);
			loaded.version = in.readShort();
			loaded.author = in.readUTF();
			loaded.organization = in.readUTF();
			loaded.email = in.readUTF();
			loaded.webPage = in.readUTF();
			loaded.phone = in.readUTF();
			loaded.address = in.readUTF();
			loaded.serverName = in.readUTF();
			loaded.templateDetails = in.readUTF();
			loaded.hqKey = in.readUTF();
			loaded.serverPublicKey = in.readUTF();
			
			if(loaded.version >= 2)
				loaded.sendContactInfoToServer = in.readBoolean();
			else
				loaded.mustAskUserToSendToServer = true;
				
			if(loaded.version >= 4)
				loaded.serverCompliance = in.readUTF();
				
			if(loaded.version >= 5)
				loaded.customFieldSpecs = in.readUTF();
				
			if(loaded.version >= 6)
				loaded.customFieldXml = in.readUTF();

			if(loaded.version >= 7)
				loaded.forceBulletinsAllPrivate = in.readBoolean();

			if(loaded.version >= 8)
			{
				loaded.backedUpKeypairEncrypted = in.readBoolean();
				loaded.backedUpKeypairShare = in.readBoolean();
			}
			if(loaded.version >= 9)
				loaded.allHQKeysXml = in.readUTF();

			in.close();
		}
		catch (Exception e)
		{
			System.out.println("ConfigInfo.load " + e);
		}
		return loaded;
	}

	public void save(OutputStream outputStream)
	{
		try
		{
			DataOutputStream out = new DataOutputStream(outputStream);
			out.writeShort(VERSION);
			out.writeUTF(author);
			out.writeUTF(organization);
			out.writeUTF(email);
			out.writeUTF(webPage);
			out.writeUTF(phone);
			out.writeUTF(address);
			out.writeUTF(serverName);
			out.writeUTF(templateDetails);
			out.writeUTF(hqKey);
			out.writeUTF(serverPublicKey);
			out.writeBoolean(sendContactInfoToServer);
			out.writeUTF(serverCompliance);
			out.writeUTF(customFieldSpecs);
			out.writeUTF(customFieldXml);
			out.writeBoolean(forceBulletinsAllPrivate);
			out.writeBoolean(backedUpKeypairEncrypted);
			out.writeBoolean(backedUpKeypairShare);
			out.writeUTF(allHQKeysXml);
			out.close();
		}
		catch(Exception e)
		{
			System.out.println("ConfigInfo.save error: " + e);
		}
	}
	
	private boolean mustAskUserToSendToServer;
	
	public static final short VERSION = 9;
	//Version 1
	private short version;
	private String author;
	private String organization;
	private String email;
	private String webPage;
	private String phone;
	private String address;
	private String serverName;
	private String serverPublicKey;
	private String templateDetails;
	private String hqKey;
	//Version 2
	private boolean sendContactInfoToServer;
	//Version 3 flag to indicate AccountMap.txt is signed.
	//Version 4
	private String serverCompliance;
	//Version 5
	private String customFieldSpecs;
	//Version 6
	private String customFieldXml;
	//Version 7
	private boolean forceBulletinsAllPrivate;
	//Version 8
	private boolean backedUpKeypairEncrypted;
	private boolean backedUpKeypairShare;
	//Version 9
	private String allHQKeysXml;
	
}
