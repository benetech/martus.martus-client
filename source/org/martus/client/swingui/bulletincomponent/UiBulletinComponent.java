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
import org.martus.swing.ParagraphLayout;

abstract public class UiBulletinComponent extends JPanel implements Scrollable, ChangeListener
{
	public UiBulletinComponent(UiMainWindow mainWindowToUse)
	{
		super();
		mainWindow = mainWindowToUse;
	}

	public void createSections()
	{
		publicStuff = createPublicSection();
		FieldSpec[] publicFieldSpecs = currentBulletin.getPublicFieldSpecs();
		Field[] publicFieldEntries = createFieldEntries(publicStuff, publicFieldSpecs);

		privateStuff = createPrivateSection();
		FieldSpec[] privateFieldSpecs = currentBulletin.getPrivateFieldSpecs();
		Field[] privateFieldEntries = createFieldEntries(privateStuff, privateFieldSpecs);
		
		fields = combineArraysOfFields(publicFieldEntries, privateFieldEntries);

		ensureBothSectionsLineUp();
		setLayout(new BorderLayout());
		add(publicStuff, BorderLayout.NORTH);
		add(privateStuff, BorderLayout.SOUTH);
	}

	private Field[] combineArraysOfFields(
		Field[] publicFieldEntries,
		Field[] privateFieldEntries)
	{
		int numPublicFields = publicFieldEntries.length;
		int numPrivateFields = privateFieldEntries.length;
		Field[] combined = new Field[numPublicFields + numPrivateFields];
		System.arraycopy(publicFieldEntries, 0, combined, 0, numPublicFields);
		System.arraycopy(privateFieldEntries, 0, combined, numPublicFields, numPrivateFields);
		return combined;
	}

	private UiBulletinComponentSection createPrivateSection()
	{
		return createBulletinComponentSection(UiBulletinComponentSection.ENCRYPTED);
	}

	private UiBulletinComponentSection createPublicSection()
	{
		UiBulletinComponentSection publicSection = createBulletinComponentSection(UiBulletinComponentSection.NOT_ENCRYPTED);
		allPrivateField = createBoolField();
		allPrivateField.initalize();
		publicSection.add(publicSection.createLabel(new FieldSpec("allprivate", FieldSpec.TYPE_BOOLEAN)), ParagraphLayout.NEW_PARAGRAPH);
		publicSection.add(allPrivateField.getComponent());
		return publicSection;
	}

	private void ensureBothSectionsLineUp()
	{
		publicStuff.matchFirstColumnWidth(privateStuff);
		privateStuff.matchFirstColumnWidth(publicStuff);
	}

	private Field[] createFieldEntries(
		UiBulletinComponentSection target,
		FieldSpec[] publicFieldSpecs
		)
	{
		int numPublicFields = publicFieldSpecs.length;
		UiField[] publicFields = target.createLabelsAndFields(target, publicFieldSpecs);
		target.createAttachmentTable();
		if(!isEditable)
			publicStuff.disableEdits();
		Field[] publicFieldEntries = new Field[numPublicFields];
		for(int fieldNum = 0; fieldNum < numPublicFields; ++fieldNum)
		{
			publicFieldEntries[fieldNum] = new Field();
			publicFieldEntries[fieldNum].tag = publicFieldSpecs[fieldNum].getTag();
			publicFieldEntries[fieldNum].field = publicFields[fieldNum];
		}
		return publicFieldEntries;
	}

	public UiMainWindow getMainWindow()
	{
		return mainWindow;
	}

	public Bulletin getCurrentBulletin()
	{
		return currentBulletin;
	}

	public void copyDataToBulletin(Bulletin bulletin) throws
			IOException,
			MartusCrypto.EncryptionException
	{
	}		

	public void validateData() throws UiField.DataInvalidException 
	{
	}
	
	public boolean isBulletinModified() throws
			IOException,
			MartusCrypto.EncryptionException
	{				
		return false;
	}	

	public void copyDataFromBulletin(Bulletin bulletinToShow) throws IOException
	{
		removeAll();
		currentBulletin = bulletinToShow;
		if(currentBulletin == null)
		{
			publicStuff = null;
			privateStuff = null;
			repaint();
			return;
		}
		
		createSections();

		String isAllPrivate = UiField.FALSESTRING;
		if(currentBulletin.isAllPrivate())
			isAllPrivate = UiField.TRUESTRING;
		allPrivateField.setText(isAllPrivate);

		publicStuff.clearAttachments();
		privateStuff.clearAttachments();

		FieldDataPacket publicData = null;
		FieldDataPacket privateData = null;
		publicData = currentBulletin.getFieldDataPacket();
		privateData = currentBulletin.getPrivateFieldDataPacket();
		publicStuff.copyDataFromPacket(publicData);
		privateStuff.copyDataFromPacket(privateData);

		publicStuff.clearWarningIndicator();
		privateStuff.clearWarningIndicator();

		if(!currentBulletin.isValid())
		{
			System.out.println("Damaged: " + currentBulletin.getLocalId());
			String text = mainWindow.getLocalization().getFieldLabel("MayBeDamaged");
			publicStuff.updateWarningIndicator(text);
			privateStuff.updateWarningIndicator(text);
		}
		else if(currentBulletin.hasUnknownTags())
		{
			System.out.println("Unknown tags: " + currentBulletin.getLocalId());
			String text = mainWindow.getLocalization().getFieldLabel("BulletinHasUnknownStuff");
			publicStuff.updateWarningIndicator(text);
			privateStuff.updateWarningIndicator(text);
		}
		else if(!currentBulletin.getAccount().equals(mainWindow.getApp().getAccountId()))
		{
			String text = mainWindow.getLocalization().getFieldLabel("BulletinNotYours");
			publicStuff.updateWarningIndicator(text);
			privateStuff.updateWarningIndicator(text);
			
		}

		repaint();
	}

	public void updateEncryptedIndicator(boolean isEncrypted)
	{
		if(publicStuff == null)
			return;
			
		publicStuff.updateEncryptedIndicator(isEncrypted);
		publicStuff.updateSectionBorder(isEncrypted);

		privateStuff.updateSectionBorder(true);
	}

	public void setEncryptionChangeListener(EncryptionChangeListener listener)
	{
		encryptionListener = listener;
	}

	protected void fireEncryptionChange(boolean newState)
	{
		if(encryptionListener != null)
			encryptionListener.encryptionChanged(newState);
	}

	// ChangeListener interface
	public void stateChanged(ChangeEvent event)
	{
		String flagString = allPrivateField.getText();
		boolean nowEncrypted = (flagString.equals(UiField.TRUESTRING));
		if(wasEncrypted != nowEncrypted)
		{
			wasEncrypted = nowEncrypted;
			fireEncryptionChange(nowEncrypted);
		}
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
	
	class Field
	{
		public String tag;
		public UiField field;
	}

	UiMainWindow mainWindow;

	Field[] fields;
	UiField allPrivateField;
	Bulletin currentBulletin;
	EncryptionChangeListener encryptionListener;
	boolean wasEncrypted;
	boolean isEditable;
	UiBulletinComponentSection publicStuff;
	UiBulletinComponentSection privateStuff;	

	abstract public UiField createBoolField();
	abstract public UiBulletinComponentSection createBulletinComponentSection(boolean encrypted);
}
