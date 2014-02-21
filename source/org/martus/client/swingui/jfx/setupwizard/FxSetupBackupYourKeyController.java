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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.FxController;
import org.martus.util.FileVerifier;

public class FxSetupBackupYourKeyController	extends	AbstractFxSetupWizardController
{
	public FxSetupBackupYourKeyController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@FXML
	public void createSingelEncryptedFile() throws Exception
	{
		doBackupKeyPairToSingleEncryptedFile();
	}

	@Override
	public FxController getNextControllerClassName()
	{
		return new FxSelectLanguageController(getMainWindow());
	}

	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/SetupBackupYourKey.fxml";
	}
	
	private void doBackupKeyPairToSingleEncryptedFile() throws Exception 
	{
		backupMessageLabel.setText("");
		File keypairFile = getApp().getCurrentKeyPairFile();
		if(keypairFile.length() > UiMainWindow.MAX_KEYPAIRFILE_SIZE)
		{
			backupMessageLabel.setText("keypair file too large!");
			return;
		}

		FileChooser fileChooser = new FileChooser();
		File martusRootDir = getApp().getMartusDataRootDirectory();
		fileChooser.setInitialDirectory(martusRootDir);
		fileChooser.setInitialFileName("MartusKeyPairBackup.dat");
		fileChooser.setTitle("Backup Key File");
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("All Files", "*.*"),
				new FileChooser.ExtensionFilter("Martus Key (*.dat)", "*.dat"));
		File newBackupFile = fileChooser.showSaveDialog(null);
		if(newBackupFile == null)
			return;

		FileInputStream input = new FileInputStream(keypairFile);
		FileOutputStream output = new FileOutputStream(newBackupFile);

		int originalKeyPairFileSize = (int) keypairFile.length();
		byte[] inputArray = new byte[originalKeyPairFileSize];

		input.read(inputArray);
		output.write(inputArray);
		input.close();
		output.close();
		if(FileVerifier.verifyFiles(keypairFile, newBackupFile))
		{
			backupMessageLabel.setText( newBackupFile.getName() + " created.");
			getApp().getConfigInfo().setBackedUpKeypairEncrypted(true);
			getApp().saveConfigInfo();
		}
	}
	
	
	@FXML
	private Label backupMessageLabel;
}
