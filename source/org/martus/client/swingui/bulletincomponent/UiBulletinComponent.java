/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;

import javax.swing.JCheckBox;
import javax.swing.Scrollable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.martus.client.core.EncryptionChangeListener;
import org.martus.client.core.LanguageChangeListener;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiField;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.packet.FieldDataPacket;
import org.martus.swing.UiParagraphPanel;

abstract public class UiBulletinComponent extends UiParagraphPanel implements Scrollable, ChangeListener, LanguageChangeListener 
{
	abstract public void setEncryptionChangeListener(EncryptionChangeListener listener);
	abstract public void setLanguageChangeListener(LanguageChangeListener listener);
	abstract public UiBulletinComponentDataSection createBulletinComponentDataSection(String sectionName);
	abstract public void copyDataToBulletin(Bulletin bulletin) throws
			IOException, MartusCrypto.EncryptionException;
	abstract public void validateData() throws UiField.DataInvalidException; 
	abstract public boolean isBulletinModified() throws
			IOException, MartusCrypto.EncryptionException;
	abstract UiBulletinComponentHeaderSection createHeaderSection();
	abstract UiBulletinComponentHeadQuartersSection createHeadQuartersSection();

	// ChangeListener interface
	abstract public void stateChanged(ChangeEvent event);

	// LanguageChangeListener interface
	abstract public void languageChanged(String newLanguage);



	public UiBulletinComponent(UiMainWindow mainWindowToUse)
	{
		super();
		mainWindow = mainWindowToUse;
	}

	public void createSections()
	{
		headerSection = createHeaderSection();
		publicSection = createDataSection(Bulletin.TOP_SECTION, currentBulletin.getTopSectionFieldSpecs(), SOMETIMES_ENCRYPTED);
		privateSection = createDataSection(Bulletin.BOTTOM_SECTION, currentBulletin.getBottomSectionFieldSpecs(), ALWAYS_ENCRYPTED);
		headquartersSection = createHeadQuartersSection();
		
		ensureSectionsLineUp();
		
		addOnNewLine(headerSection);
		addOnNewLine(publicSection);
		addOnNewLine(privateSection);
		addOnNewLine(headquartersSection);
	}
	
	private UiBulletinComponentDataSection createDataSection(String section, 
			FieldSpec[] fieldSpecs, int encryptionStatus)
	{
		UiBulletinComponentDataSection target = createBulletinComponentDataSection(section);
		if(encryptionStatus == SOMETIMES_ENCRYPTED)
			createAllPrivateField(target);
		target.createLabelsAndFields(fieldSpecs, this);

		return target;
	}
	
	private void createAllPrivateField(UiBulletinComponentDataSection target)
	{
		FieldSpec allPrivateFieldSpec = FieldSpec.createStandardField("allprivate", new FieldTypeBoolean());
		allPrivateField = target.createAndAddLabelAndField(allPrivateFieldSpec);
		allPrivateField.setListener(this);
	}

	private void ensureSectionsLineUp()
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
			headquartersSection = null;
			repaint();
			return;
		}
		
		createSections();
		
		headerSection.setBulletin(currentBulletin);

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

		String accountId = mainWindow.getApp().getAccountId();
		
		boolean notYourBulletin = !currentBulletin.getAccount().equals(accountId);
		boolean notAuthorizedToRead = !currentBulletin.getAuthorizedToReadKeys().containsKey(accountId);
		mainWindow.setWaitingCursor();
		boolean isBulletinValid = mainWindow.getStore().isBulletinValid(currentBulletin);
		mainWindow.resetCursor();
		
		if(!isBulletinValid || (notYourBulletin && notAuthorizedToRead))
		{
			String text;
			if(notYourBulletin)
			{
				text = mainWindow.getLocalization().getFieldLabel("NotAuthorizedToViewPrivate");
				if(currentBulletin.isAllPrivate())
					publicSection.updateWarningIndicator(text);
			}
			else
			{
				System.out.println("Damaged: " + currentBulletin.getLocalId());
				text = mainWindow.getLocalization().getFieldLabel("MayBeDamaged");
				publicSection.updateWarningIndicator(text);
			}
			privateSection.updateWarningIndicator(text);
		}
		else if(currentBulletin.hasUnknownTags())
		{
			System.out.println("Unknown tags: " + currentBulletin.getLocalId());
			String text = mainWindow.getLocalization().getFieldLabel("BulletinHasUnknownStuff");
			publicSection.updateWarningIndicator(text);
			privateSection.updateWarningIndicator(text);
		}
		else if(notYourBulletin)
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
	UiBulletinComponentHeaderSection headerSection;
	UiBulletinComponentDataSection publicSection;
	UiBulletinComponentDataSection privateSection;	
	UiBulletinComponentHeadQuartersSection headquartersSection;

	private static final int SOMETIMES_ENCRYPTED = 1;
	private static final int ALWAYS_ENCRYPTED = 2;
}
