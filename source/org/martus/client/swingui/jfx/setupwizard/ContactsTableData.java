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
package org.martus.client.swingui.jfx.setupwizard;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import org.martus.common.ContactKey;
import org.martus.common.DammCheckDigitAlgorithm.CheckDigitInvalidException;
import org.martus.common.crypto.MartusCrypto.CreateDigestException;
import org.martus.util.StreamableBase64.InvalidBase64Exception;

public class ContactsTableData
{
	public ContactsTableData(ContactKey contact) throws InvalidBase64Exception, CreateDigestException, CheckDigitInvalidException
	{
		publicKey = contact.getPublicKey();
		contactName = new SimpleStringProperty(contact.getLabel());
		publicCode = new SimpleStringProperty(contact.getFormattedPublicCode40());
		sendToByDefault = new SimpleBooleanProperty(contact.getSendToByDefault());
		canSendTo = new SimpleBooleanProperty(contact.getCanSendTo());
		canReceiveFrom = new SimpleBooleanProperty(contact.getCanReceiveFrom());
		verificationStatus = new SimpleIntegerProperty(contact.getVerificationStatus());
		removeContact = new SimpleStringProperty("X");
	}

	public ContactKey getContact()
	{
		ContactKey contact = new ContactKey(publicKey, contactName.get());
		contact.setSendToByDefault(sendToByDefault.get());
		contact.setVerificationStatus(verificationStatus.get());
		return contact;
	}
	
	public String getContactName()
	{
		return contactName.get();
	}
	
	public void setContactName(String contactNameToUse)
	{
		contactName.set(contactNameToUse);
	}
	
    public SimpleStringProperty contactNameProperty() 
    { 
        return contactName; 
    }

    public String getDeleteContact()
	{
		return removeContact.get();
	}
	
	public void setDeleteContact(String removeContactToUse)
	{
		removeContact.set(removeContactToUse);
	}

	public String getPublicCode()
	{
		return publicCode.get();
	}
	
	public void setPublicCode(String publicCodeToUse)
	{
	}
	
	public void setVerificationStatus(int verificationStatusToUse)
	{
		verificationStatus.set(verificationStatusToUse);
	}
	
	public int getVerificationStatus()
	{
		return verificationStatus.get();
	}
	
	public boolean getCanSendTo()
	{
		return canSendTo.get();
	}
	
	public void setCanSendTo(boolean canSendToToUse)
	{
		canSendTo.set(canSendToToUse);
	}
	
	public boolean getCanReceiveFrom()
	{
		return canReceiveFrom.get();
	}
	
	public void setCanReceiveFrom(boolean canReceiveFromToUse)
	{
		canReceiveFrom.set(canReceiveFromToUse);
	}
	
    public SimpleBooleanProperty canSendToProperty() 
    {
    		return canSendTo;
    }
	
    public SimpleBooleanProperty canReceiveFromProperty() 
    {
    		return canReceiveFrom;
    }

	public boolean getSendToByDefault()
	{
		return sendToByDefault.get();
	}
	
	public void setSendToByDefault(boolean sendToByDefaultToUse)
	{
		sendToByDefault.set(sendToByDefaultToUse);
	}
	
	public SimpleBooleanProperty sendToByDefaultProperty() 
	{
		return sendToByDefault;
	}

	private final SimpleStringProperty contactName;
	private final SimpleStringProperty publicCode;
	private final SimpleBooleanProperty sendToByDefault;
	private final SimpleBooleanProperty canSendTo;
	private final SimpleBooleanProperty canReceiveFrom;
	private final SimpleStringProperty removeContact;
	private final SimpleIntegerProperty verificationStatus;

	private String publicKey;
}
