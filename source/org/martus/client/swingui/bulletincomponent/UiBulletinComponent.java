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

package org.martus.client.swingui.bulletincomponent;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.martus.client.core.EncryptionChangeListener;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiField;
import org.martus.common.FieldSpec;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.packet.FieldDataPacket;

abstract public class UiBulletinComponent extends JPanel implements Scrollable, ChangeListener
{
	abstract public void setEncryptionChangeListener(EncryptionChangeListener listener);
	abstract public UiBulletinComponentDataSection createBulletinComponentDataSection();
	abstract public void copyDataToBulletin(Bulletin bulletin) throws
			IOException, MartusCrypto.EncryptionException;
	abstract public void validateData() throws UiField.DataInvalidException; 
	abstract public boolean isBulletinModified() throws
			IOException, MartusCrypto.EncryptionException;
	abstract UiBulletinComponentHeader createHeaderSection();
	abstract Vector getHqKeys();

	// ChangeListener interface
	abstract public void stateChanged(ChangeEvent event);




	public UiBulletinComponent(UiMainWindow mainWindowToUse)
	{
		super();
		mainWindow = mainWindowToUse;
	}

	public void createSections()
	{
		headerSection = createHeaderSection();
		publicSection = createDataSection(currentBulletin.getPublicFieldSpecs(), SOMETIMES_ENCRYPTED);
		privateSection = createDataSection(currentBulletin.getPrivateFieldSpecs(), ALWAYS_ENCRYPTED);
		ensureBothSectionsLineUp();
		setLayout(new BorderLayout());
		add(headerSection, BorderLayout.NORTH);
		add(publicSection, BorderLayout.CENTER);
		add(privateSection, BorderLayout.SOUTH);
	}
	
	private UiBulletinComponentDataSection createDataSection(
		FieldSpec[] fieldSpecs,
		int encryptionStatus)
	{
		UiBulletinComponentDataSection target = createBulletinComponentDataSection();
		if(encryptionStatus == SOMETIMES_ENCRYPTED)
			createAllPrivateField(target);

		target.createLabelsAndFields(fieldSpecs);

		return target;
	}
	
	private void createAllPrivateField(UiBulletinComponentDataSection target)
	{
		FieldSpec allPrivateFieldSpec = FieldSpec.createStandardField("allprivate", FieldSpec.TYPE_BOOLEAN);
		allPrivateField = target.createAndAddLabelAndField(allPrivateFieldSpec);
		allPrivateField.setListener(this);
	}

	private void ensureBothSectionsLineUp()
	{
		publicSection.matchFirstColumnWidth(privateSection);
		privateSection.matchFirstColumnWidth(publicSection);
	}

	public Bulletin getCurrentBulletin()
	{
		return currentBulletin;
	}

	public void copyDataFromBulletin(Bulletin bulletinToShow) throws IOException
	{
		removeAll();
		currentBulletin = bulletinToShow;
		if(currentBulletin == null)
		{
			headerSection = null;
			publicSection = null;
			privateSection = null;
			repaint();
			return;
		}
		
		createSections();
		
		headerSection.setHqKeys(getHqKeys());

		String isAllPrivate = FieldSpec.FALSESTRING;
		if(currentBulletin.isAllPrivate())
			isAllPrivate = FieldSpec.TRUESTRING;
		allPrivateField.setText(isAllPrivate);

		FieldDataPacket publicData = currentBulletin.getFieldDataPacket();
		publicSection.clearAttachments();
		publicSection.copyDataFromPacket(publicData);
		publicSection.clearWarningIndicator();

		FieldDataPacket privateData = currentBulletin.getPrivateFieldDataPacket();
		privateSection.clearAttachments();
		privateSection.copyDataFromPacket(privateData);
		privateSection.clearWarningIndicator();

		if(!currentBulletin.isValid())
		{
			System.out.println("Damaged: " + currentBulletin.getLocalId());
			String text = mainWindow.getLocalization().getFieldLabel("MayBeDamaged");
			publicSection.updateWarningIndicator(text);
			privateSection.updateWarningIndicator(text);
		}
		else if(currentBulletin.hasUnknownTags())
		{
			System.out.println("Unknown tags: " + currentBulletin.getLocalId());
			String text = mainWindow.getLocalization().getFieldLabel("BulletinHasUnknownStuff");
			publicSection.updateWarningIndicator(text);
			privateSection.updateWarningIndicator(text);
		}
		else if(!currentBulletin.getAccount().equals(mainWindow.getApp().getAccountId()))
		{
			String text = mainWindow.getLocalization().getFieldLabel("BulletinNotYours");
			publicSection.updateWarningIndicator(text);
			privateSection.updateWarningIndicator(text);
			
		}

		repaint();
	}

	public void updateEncryptedIndicator(boolean isEncrypted)
	{
		if(publicSection != null)
			publicSection.updateEncryptedIndicator(isEncrypted);
		
		if(privateSection != null)
			privateSection.updateEncryptedIndicator(true);
	}
	
	public void encryptAndDisableAllPrivate()
	{
		JCheckBox allPrivate = ((JCheckBox)(allPrivateField.getComponent()));
		allPrivate.setSelected(true);
		allPrivate.setEnabled(false);
		updateEncryptedIndicator(true);
	}

	public boolean isAllPrivateBoxChecked()
	{
		boolean isAllPrivate = false;
		if(allPrivateField.getText().equals(FieldSpec.TRUESTRING))
			isAllPrivate = true;
		return isAllPrivate;
	}

	// Scrollable interface
	public Dimension getPreferredScrollableViewportSize()
	{
		return getPreferredSize();
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return 20;
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return 100;
	}

	public boolean getScrollableTracksViewportWidth()
	{
		return false;
	}

	public boolean getScrollableTracksViewportHeight()
	{
		return false;
	}
	// End scrollable interface
	
	UiMainWindow mainWindow;

	UiField allPrivateField;
	Bulletin currentBulletin;
	UiBulletinComponentHeader headerSection;
	UiBulletinComponentDataSection publicSection;
	UiBulletinComponentDataSection privateSection;	

	private static final int SOMETIMES_ENCRYPTED = 1;
	private static final int ALWAYS_ENCRYPTED = 2;
}
