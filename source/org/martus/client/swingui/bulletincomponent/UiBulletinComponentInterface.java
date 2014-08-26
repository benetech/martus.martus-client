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

package org.martus.client.swingui.bulletincomponent;

import java.io.IOException;

import javax.swing.event.ChangeEvent;

import org.martus.client.core.BulletinLanguageChangeListener;
import org.martus.client.core.EncryptionChangeListener;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.fieldspec.DataInvalidException;

public interface UiBulletinComponentInterface
{
	public abstract UiMainWindow getMainWindow();
	abstract public void copyDataToBulletin(Bulletin bulletin) throws IOException, MartusCrypto.EncryptionException;
	public abstract void copyDataFromBulletin(Bulletin bulletinToShow) throws Exception;
	abstract public void validateData() throws DataInvalidException;
	abstract public boolean isBulletinModified() throws Exception;
	public abstract void scrollToTop();

	// ChangeListener interface
	abstract public void stateChanged(ChangeEvent event);

	abstract public void setLanguageChangeListener(BulletinLanguageChangeListener listener);
	// LanguageChangeListener interface
	public abstract void bulletinLanguageHasChanged(String newBulletinLanguageCode);

	abstract public void setEncryptionChangeListener(EncryptionChangeListener listener);
	public abstract void updateEncryptedIndicator(boolean isEncrypted);
	public abstract void encryptAndDisableAllPrivate();
	public abstract boolean isAllPrivateBoxChecked();

}