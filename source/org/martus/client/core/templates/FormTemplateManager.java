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
package org.martus.client.core.templates;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.martus.common.MartusLogger;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.fieldspec.FormTemplate;
import org.martus.util.inputstreamwithseek.ByteArrayInputStreamWithSeek;

public class FormTemplateManager
{
	public static FormTemplateManager createOrOpen(MartusCrypto cryptoToUse, File directoryToUse) throws Exception
	{
		directoryToUse.mkdirs();
		
		FormTemplateManager formTemplateManager = new FormTemplateManager(cryptoToUse, directoryToUse);
		
		return formTemplateManager;
	}
	
	public void putTemplate(FormTemplate template) throws Exception
	{
		saveEncryptedTemplate(template);
	}
	
	public FormTemplate getTemplate(String title) throws Exception
	{
		File file = getTemplateFile(title);
		FormTemplate template = loadEncryptedTemplate(file);
		return template;
	}

	public Set<String> getAvailableTemplateNames() throws Exception
	{
		HashSet<String> available = new HashSet<String>();
		available.add(MARTUS_DEFAULT_FORM_TEMPLATE_NAME);
		
		File[] emctFiles = directory.listFiles(file -> isEmctFile(file));
		for (File file : emctFiles)
		{
			try
			{
				FormTemplate template = loadEncryptedTemplate(file);
				available.add(template.getTitle());
			}
			catch(Exception e)
			{
				MartusLogger.logException(e);
			}
		}
		return available;
	}

	private FormTemplateManager(MartusCrypto cryptoToUse, File directoryToUse) throws Exception
	{
		if(!directoryToUse.isDirectory())
			throw new FileNotFoundException("No such directory: " + directoryToUse.getAbsolutePath());
		
		security = cryptoToUse;
		directory = directoryToUse;
	}
	
	private FormTemplate loadEncryptedTemplate(File dataFile) throws Exception
	{
		File sigFile = getSignatureFileFor(dataFile);
		byte[] plaintextTemplateBytes = MartusSecurity.verifySignatureAndDecryptFile(dataFile, sigFile, security);
		ByteArrayInputStreamWithSeek plainTextTemplateBytesIn = new ByteArrayInputStreamWithSeek(plaintextTemplateBytes);
		FormTemplate template = new FormTemplate();
		template.importTemplate(security, plainTextTemplateBytesIn);
		return template;
	}

	public static File getSignatureFileFor(File dataFile)
	{
		File sigFile = new File(dataFile.getParentFile(), dataFile.getName() + SIG_EXTENSION);
		return sigFile;
	}

	private boolean isEmctFile(File file)
	{
		return file.getName().toLowerCase().endsWith(ENCRYPTED_MCT_EXTENSION);
	}
	
	private void saveEncryptedTemplate(FormTemplate formTemplate) throws Exception
	{
		ByteArrayOutputStream plaintextSignedBytesOut = new ByteArrayOutputStream();
		formTemplate.saveContentsToOutputStream(security, plaintextSignedBytesOut);
		plaintextSignedBytesOut.close();
		byte[] plaintextSignedBytes = plaintextSignedBytesOut.toByteArray();

		String title = formTemplate.getTitle();
		File file = getTemplateFile(title);
		File signatureFile = getSignatureFileFor(file);
		security.encryptAndWriteFileAndSignatureFile(file, signatureFile, plaintextSignedBytes);
	}

	private File getTemplateFile(String title)
	{
		return new File(directory, getTemplateFilename(title));
	}

	public static String getTemplateFilename(String title)
	{
		return FormTemplate.calculateFileNameFromString(title, ENCRYPTED_MCT_EXTENSION);
	}

	public static class DirectoryAlreadyExistsException extends IOException
	{
		public DirectoryAlreadyExistsException(String message)
		{
			super(message);
		}
	}

	public static final String MARTUS_DEFAULT_FORM_TEMPLATE_NAME = "%DefaultFormTemplateName";
	private static final String ENCRYPTED_MCT_EXTENSION = ".emct";
	private static final String SIG_EXTENSION = ".sig";
	
	private MartusCrypto security;
	private File directory;
}
