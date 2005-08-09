/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2005, Beneficent
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.bulletinstore.ClientBulletinStore.BulletinAlreadyExistsException;
import org.martus.client.bulletinstore.ClientBulletinStore.BulletinOlderException;
import org.martus.client.search.BulletinSearcher;
import org.martus.client.search.SearchParser;
import org.martus.client.search.SearchTreeNode;
import org.martus.client.swingui.EnglishStrings;
import org.martus.client.swingui.UiConstants;
import org.martus.clientside.ClientSideNetworkGateway;
import org.martus.clientside.ClientSideNetworkHandlerUsingXmlRpcForNonSSL;
import org.martus.clientside.MtfAwareLocalization;
import org.martus.clientside.PasswordHelper;
import org.martus.common.BulletinSummary;
import org.martus.common.FieldCollection;
import org.martus.common.HQKey;
import org.martus.common.HQKeys;
import org.martus.common.LegacyCustomFields;
import org.martus.common.MartusUtilities;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.Version;
import org.martus.common.Exceptions.ServerCallFailedException;
import org.martus.common.Exceptions.ServerNotAvailableException;
import org.martus.common.FieldCollection.CustomFieldsParseException;
import org.martus.common.HQKeys.HQsException;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.MartusUtilities.PublicInformationInvalidException;
import org.martus.common.MartusUtilities.ServerErrorException;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.crypto.MartusCrypto.DecryptionException;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.crypto.MartusCrypto.NoKeyPairException;
import org.martus.common.database.FileDatabase.MissingAccountMapException;
import org.martus.common.database.FileDatabase.MissingAccountMapSignatureException;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.network.NetworkInterface;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.network.NonSSLNetworkAPI;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.Packet;
import org.martus.common.packet.UniversalId;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.Packet.WrongAccountException;
import org.martus.common.packet.Packet.WrongPacketTypeException;
import org.martus.jarverifier.JarVerifier;
import org.martus.util.Base64;
import org.martus.util.DirectoryUtils;
import org.martus.util.StreamCopier;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;
import org.martus.util.Base64.InvalidBase64Exception;
import org.martus.util.inputstreamwithseek.ByteArrayInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.FileInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;
import org.martus.util.inputstreamwithseek.ZipEntryInputStreamWithSeekThatClosesZipFile;



public class MartusApp
{
	
	public MartusApp(MtfAwareLocalization localizationToUse) throws MartusAppInitializationException
	{
		this(null, determineMartusDataRootDirectory(), localizationToUse);
	}

	public MartusApp(MartusCrypto cryptoToUse, File dataDirectoryToUse, MtfAwareLocalization localizationToUse) throws MartusAppInitializationException
	{
		localization = localizationToUse;
		try
		{
			if(cryptoToUse == null)
				cryptoToUse = new MartusSecurity();

			configInfo = new ConfigInfo();
			currentUserName = "";
			maxNewFolders = MAXFOLDERS;
			martusDataRootDirectory = dataDirectoryToUse;
			store = new ClientBulletinStore(cryptoToUse);
			if(shouldUseUnofficialTranslations())
				localization.includeOfficialLanguagesOnly = false;
			
		}
		catch(MartusCrypto.CryptoInitializationException e)
		{
			throw new MartusAppInitializationException("ErrorCryptoInitialization");
		}

		UpdateDocsIfNecessaryFromMLPFiles();
	}

	static public void setInitialUiDefaultsFromFileIfPresent(MtfAwareLocalization localization, File defaultUiFile)
	{
		if(!defaultUiFile.exists())
			return;
		try
		{
			String languageCode = null;
			UnicodeReader in = new UnicodeReader(defaultUiFile);
			languageCode = in.readLine();
			in.close();
			
			if(MtfAwareLocalization.isRecognizedLanguage(languageCode))
			{
				localization.setCurrentLanguageCode(languageCode);
				localization.setCurrentDateFormatCode(MtfAwareLocalization.getDefaultDateFormatForLanguage(languageCode));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void setServerInfo(String serverName, String serverKey, String serverCompliance)
	{
		configInfo.setServerName(serverName);
		configInfo.setServerPublicKey(serverKey);
		configInfo.setServerCompliance(serverCompliance);
		try
		{
			saveConfigInfo();
		}
		catch (SaveConfigInfoException e)
		{
			System.out.println("MartusApp.setServerInfo: Unable to Save ConfigInfo" + e);
		}

		invalidateCurrentHandlerAndGateway();
	}

	public String getLegacyHQKey()
	{
		return configInfo.getLegacyHQKey();
	}
	
	public HQKeys getAllHQKeys() throws HQsException
	{
		return new HQKeys(configInfo.getAllHQKeysXml());
	}

	public HQKeys getDefaultHQKeys() throws HQsException
	{
		return new HQKeys(configInfo.getDefaultHQKeysXml());
	}

	public HQKeys getAllHQKeysWithFallback()
	{
		try
		{
			return getAllHQKeys();
		}
		catch (HQsException e)
		{
			e.printStackTrace();
			HQKey legacyKey = new HQKey(getLegacyHQKey());
			return new HQKeys(legacyKey);
		}
	}
	
	public HQKeys getDefaultHQKeysWithFallback()
	{
		try
		{
			return getDefaultHQKeys();
		}
		catch (HQsException e)
		{
			e.printStackTrace();
			HQKey legacyKey = new HQKey(getLegacyHQKey());
			return new HQKeys(legacyKey);
		}
	}

	public void addHQLabelsWherePossible(HQKeys keys)
	{
		for(int i = 0; i < keys.size(); ++i)
		{
			HQKey key = keys.get(i);
			key.setLabel(getHQLabelIfPresent(key));
		}
	}

	
	public String getHQLabelIfPresent(HQKey hqKey)
	{
		try
		{
			String hqLabelIfPresent = getAllHQKeys().getLabelIfPresent(hqKey);
			if(hqLabelIfPresent.length() == 0)
			{
				String publicCode = hqKey.getPublicKey();
				try
				{
					publicCode = hqKey.getPublicCode();
				}
				catch (InvalidBase64Exception e)
				{
					e.printStackTrace();
				}
				String hqNotConfigured = localization.getFieldLabel("HQNotConfigured");
				hqLabelIfPresent = publicCode + " " + hqNotConfigured;
			}
			return hqLabelIfPresent;
		}
		catch (HQsException e)
		{
			e.printStackTrace();
			return "";
		}
	}

	public ConfigInfo getConfigInfo()
	{
		return configInfo;
	}
	
	public void setAndSaveHQKeys(HQKeys allHQKeys, HQKeys defaultHQKeys) throws SaveConfigInfoException 
	{
		configInfo.setAllHQKeysXml(allHQKeys.toStringWithLabel());
		configInfo.setDefaultHQKeysXml(defaultHQKeys.toStringWithLabel());
		if(allHQKeys.isEmpty())
			configInfo.clearHQKey();
		else
			configInfo.setLegacyHQKey(allHQKeys.get(0).getPublicKey());
		saveConfigInfo();
	}

	public void saveConfigInfo() throws SaveConfigInfoException
	{
		try
		{
			ByteArrayOutputStream encryptedConfigOutputStream = new ByteArrayOutputStream();
			configInfo.save(encryptedConfigOutputStream);
			byte[] encryptedConfigInfo = encryptedConfigOutputStream.toByteArray();

			ByteArrayInputStream encryptedConfigInputStream = new ByteArrayInputStream(encryptedConfigInfo);
			FileOutputStream configFileOutputStream = new FileOutputStream(getConfigInfoFile());
			getSecurity().encrypt(encryptedConfigInputStream, configFileOutputStream);

			configFileOutputStream.close();
			encryptedConfigInputStream.close();
			encryptedConfigOutputStream.close();


			FileInputStream in = new FileInputStream(getConfigInfoFile());
			byte[] signature = getSecurity().createSignatureOfStream(in);
			in.close();

			FileOutputStream out = new FileOutputStream(getConfigInfoSignatureFile());
			out.write(signature);
			out.close();
		}
		catch (Exception e)
		{
			System.out.println("saveConfigInfo :" + e);
			throw new SaveConfigInfoException();
		}

	}

	public void loadConfigInfo() throws LoadConfigInfoException
	{
		configInfo.clear();

		File sigFile = getConfigInfoSignatureFile();
		File dataFile = getConfigInfoFile();

		if(!dataFile.exists())
		{
			//System.out.println("MartusApp.loadConfigInfo: config file doesn't exist");
			return;
		}

		try
		{
			String accountId = getSecurity().getPublicKeyString();
			if(!isSignatureFileValid(dataFile, sigFile, accountId))
				throw new LoadConfigInfoException();

			InputStreamWithSeek encryptedContactFileInputStream = new FileInputStreamWithSeek(dataFile);
			ByteArrayOutputStream plainTextContactOutputStream = new ByteArrayOutputStream();
			getSecurity().decrypt(encryptedContactFileInputStream, plainTextContactOutputStream);

			byte[] plainTextConfigInfo = plainTextContactOutputStream.toByteArray();
			ByteArrayInputStream plainTextConfigInputStream = new ByteArrayInputStream(plainTextConfigInfo);
			configInfo = ConfigInfo.load(plainTextConfigInputStream);

			plainTextConfigInputStream.close();
			plainTextContactOutputStream.close();
			encryptedContactFileInputStream.close();
			
			FieldSpec[] specs = getCustomFieldSpecs(configInfo);
			store.setPublicFieldTags(specs);
			
			convertLegacyHQToMultipleHQs();
			
		}
		catch (Exception e)
		{
			//System.out.println("Loadcontactinfo: " + e);
			throw new LoadConfigInfoException();
		}
	}
	
	private boolean isSignatureFileValid(File dataFile, File sigFile, String accountId) throws FileNotFoundException, IOException, MartusSignatureException 
	{
		byte[] signature =	new byte[(int)sigFile.length()];
		FileInputStream inSignature = new FileInputStream(sigFile);
		inSignature.read(signature);
		inSignature.close();

		FileInputStream inData = new FileInputStream(dataFile);
		try
		{
			boolean verified = getSecurity().isValidSignatureOfStream(accountId, inData, signature);
			return verified;
		}
		finally
		{
			inData.close();
		}
	}

	private void convertLegacyHQToMultipleHQs() throws HQsException
	{
		String legacyHQKey = configInfo.getLegacyHQKey();
		if(legacyHQKey.length()>0)
		{
			HQKeys hqKeys = getAllHQKeys();
			if(!hqKeys.containsKey(legacyHQKey))
			{
				HQKey legacy = new HQKey(legacyHQKey);
				hqKeys.add(legacy);
				try
				{
					setAndSaveHQKeys(hqKeys, hqKeys);
				}
				catch(MartusApp.SaveConfigInfoException e)
				{
					System.out.println("SaveConfigInfoException: " + e);						
				}
			}
		}
	}

	public static FieldSpec[] getCustomFieldSpecs(ConfigInfo configInfo) throws CustomFieldsParseException
	{
		String xmlSpecs = configInfo.getCustomFieldXml();
		if(xmlSpecs.length() > 0)
			return FieldCollection.parseXml(xmlSpecs);
			
		String legacySpecs = configInfo.getCustomFieldSpecs();
		FieldSpec[] specs = LegacyCustomFields.parseFieldSpecsFromString(legacySpecs);
		return specs;
	}

	public void doAfterSigninInitalization() throws MartusAppInitializationException, FileVerificationException, MissingAccountMapException, MissingAccountMapSignatureException
	{
		store.doAfterSigninInitialization(getCurrentAccountDirectory());
	}
	
	public File getMartusDataRootDirectory()
	{
		return martusDataRootDirectory;
	}

	public File getCurrentAccountDirectory()
	{
		return currentAccountDirectory;
	}
	
	public File getPacketsDirectory()
	{
		return new File(getCurrentAccountDirectory(), PACKETS_DIRECTORY_NAME);
	}
	
	public File getAccountsDirectory()
	{
		return new File(getMartusDataRootDirectory(), ACCOUNTS_DIRECTORY_NAME);
	}
	
	public boolean shouldUseUnofficialTranslations()
	{
		return (new File(getMartusDataRootDirectory(), USE_UNOFFICIAL_TRANSLATIONS_NAME)).exists();
	}
	
	public File getDocumentsDirectory()
	{
		return new File(getMartusDataRootDirectory(), DOCUMENTS_DIRECTORY_NAME);
	}

	public String getCurrentAccountDirectoryName()
	{
		return getCurrentAccountDirectory().getPath() + "/";
	}

	public File getConfigInfoFile()
	{
		return getConfigInfoFileForAccount(getCurrentAccountDirectory());
	}
	
	public File getConfigInfoFileForAccount(File accountDirectory)
	{
		return new File(accountDirectory, "MartusConfig.dat");
	}

	public File getConfigInfoSignatureFile()
	{
		return getConfigInfoSignatureFileForAccount(getCurrentAccountDirectory());
	}

	public File getConfigInfoSignatureFileForAccount(File accountDirectory)
	{
		return new File(accountDirectory, "MartusConfig.sig");
	}

	public File getUploadInfoFile()
	{
		return getUploadInfoFileForAccount(getCurrentAccountDirectory());
	}

	public File getUploadInfoFileForAccount(File accountDirectory)
	{
		return new File(accountDirectory, "MartusUploadInfo.dat");
	}

	public File getUiStateFileForAccount(File accountDirectory)
	{
		return new File(accountDirectory, "UserUiState.dat");
	}
	
	public File getBulletinDefaultDetailsFile()
	{
		return new File(getCurrentAccountDirectoryName(), "DefaultDetails" + DEFAULT_DETAILS_EXTENSION);
	}

	public String getUploadLogFilename()
	{
		return  getCurrentAccountDirectoryName() + "MartusUploadLog.txt";
	}

	public InputStream getHelpMain(String currentLanguageCode)
	{
		return getHelp(currentLanguageCode, getHelpFilename(currentLanguageCode));
	}
	
	public InputStream getHelpTOC(String currentLanguageCode)
	{
		return getHelp(currentLanguageCode, getHelpTOCFilename(currentLanguageCode));
	}
	
	private InputStream getHelp(String currentLanguageCode, String helpFileName)
	{
		if(!localization.isOfficialTranslation(currentLanguageCode))
			return null;

		try 
		{
			File mlpFile = localization.getMlpkFile(currentLanguageCode);
			if(mlpFile.exists() && 
			   JarVerifier.verify(mlpFile,false) == JarVerifier.JAR_VERIFIED_TRUE)
			{
				ZipFile zip = new ZipFile(mlpFile);
				ZipEntry zipEntry = zip.getEntry(helpFileName);
				ZipEntryInputStreamWithSeekThatClosesZipFile stream = new ZipEntryInputStreamWithSeekThatClosesZipFile(zip, zipEntry);
				return stream;
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return EnglishStrings.class.getResourceAsStream(helpFileName);
	}
	
	public String getHelpFilename(String languageCode)
	{
		String helpFile = "MartusHelp-" + languageCode + ".txt";
		return helpFile;
	}

	public String getHelpTOCFilename(String languageCode)
	{
		String helpFile = "MartusHelpTOC-" + languageCode + ".txt";
		return helpFile;
	}
	
	public void UpdateDocsIfNecessaryFromMLPFiles()
	{
		File[] mlpFiles = GetMlpFiles();
		for(int i = 0; i < mlpFiles.length; ++i)
		{
			File mlpFile = mlpFiles[i];
			extractNewerPDFDocumentation(mlpFile);
			extractNewerReadMeDocumentation(mlpFile);
		}
	}

	private void extractNewerReadMeDocumentation(File mlpFile)
	{
		File targetDirectory = getMartusDataRootDirectory();
		String readMeFiles = "README";
		String fileExtension = ".txt";
		extractMatchingFileTypesFromJar(mlpFile, targetDirectory, readMeFiles, fileExtension);
	}

	private void extractNewerPDFDocumentation(File mlpFile)
	{
		File targetDirectory = getDocumentsDirectory();
		String anyPdfFile = "";
		String fileExtension = ".pdf";
		extractMatchingFileTypesFromJar(mlpFile, targetDirectory, anyPdfFile, fileExtension);
	}

	private void extractMatchingFileTypesFromJar(File mlpFile, File targetDirectory, String filesBeginningWith, String filesEndingWith)
	{
		if(JarVerifier.verify(mlpFile, false) != JarVerifier.JAR_VERIFIED_TRUE)
			return;
		JarFile jar = null;
		try
		{
			jar = new JarFile(mlpFile);
			Enumeration entries = jar.entries();
			while(entries.hasMoreElements())
			{
				JarEntry entry = (JarEntry) entries.nextElement();
				String jarEntryName = entry.getName();
				if(filesBeginningWith.length() > 0)
				{
					if(!jarEntryName.startsWith(filesBeginningWith))
						continue;
				}
				if(!jarEntryName.endsWith(filesEndingWith))
					continue;
				File fileOnDisk = new File(targetDirectory, jarEntryName);
				if(isFileNewerOnDisk(fileOnDisk, entry))
					continue;
					
				fileOnDisk.delete();
				targetDirectory.mkdirs();
				copyJarEntryToFile(jar, entry, fileOnDisk);
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(jar != null)
					jar.close();
			}
			catch(IOException e1)
			{
				e1.printStackTrace();
			}
		}
	}
	

	public boolean isFileNewerOnDisk(File fileToCheck, ZipEntry entry)
	{
		if(!fileToCheck.exists())
			return false;
		Date zipFileDate = new Date(entry.getTime());
		Date currentFileDate = new Date(fileToCheck.lastModified());
		return(zipFileDate.before(currentFileDate));
	}

	private void copyJarEntryToFile(JarFile jar, JarEntry entry, File outputFile) throws IOException, FileNotFoundException
	{
		InputStream in = jar.getInputStream(entry);
		FileOutputStream out = new FileOutputStream(outputFile);
		StreamCopier copier = new StreamCopier();
		copier.copyStream(in, out);
		//TODO put closes in a finally block.
		in.close();
		out.close();
		outputFile.setLastModified(entry.getTime());
	}

	public static File getTranslationsDirectory()
	{
		return determineMartusDataRootDirectory();
	}

	public File getCurrentKeyPairFile()
	{
		File dir = getCurrentAccountDirectory();
		return getKeyPairFile(dir);
	}

	public File getKeyPairFile(File dir)
	{
		return new File(dir, KEYPAIR_FILENAME);
	}	

	public static File getBackupFile(File original)
	{
		return new File(original.getPath() + ".bak");
	}
	
	public String getUserName()
	{
		return currentUserName;
	}

	public void loadFolders()
	{
		store.loadFolders();
	}

	public ClientBulletinStore getStore()
	{
		return store;
	}

	public Bulletin createBulletin()
	{
		Bulletin b = store.createEmptyBulletin();
		b.set(Bulletin.TAGAUTHOR, configInfo.getAuthor());
		b.set(Bulletin.TAGORGANIZATION, configInfo.getOrganization());
		b.set(Bulletin.TAGPUBLICINFO, configInfo.getTemplateDetails());
		b.set(Bulletin.TAGLANGUAGE, getCurrentLanguage());
		setDefaultHQKeysInBulletin(b);
		b.setDraft();
		b.setAllPrivate(true);
		return b;
	}

	public void setDefaultHQKeysInBulletin(Bulletin b)
	{
		HQKeys hqKeys = getDefaultHQKeysWithFallback();
		b.setAuthorizedToReadKeys(hqKeys);
	}

	public BulletinFolder getFolderSaved()
	{
		return store.getFolderSaved();
	}

	public BulletinFolder getFolderDiscarded()
	{
		return store.getFolderDiscarded();
	}

	public BulletinFolder getFolderSealedOutbox()
	{
		return store.getFolderSealedOutbox();
	}

	public BulletinFolder getFolderDraftOutbox()
	{
		return store.getFolderDraftOutbox();
	}

	public BulletinFolder createFolderRetrieved()
	{
		String folderName = getNameOfFolderRetrievedSealed();
		return createOrFindFolder(folderName);
	}

	public BulletinFolder createFolderRetrievedFieldOffice()
	{
		String folderName = getNameOfFolderRetrievedFieldOfficeSealed();
		return createOrFindFolder(folderName);
	}

	public String getNameOfFolderRetrievedSealed()
	{
		return store.getNameOfFolderRetrievedSealed();
	}

	public String getNameOfFolderRetrievedDraft()
	{
		return store.getNameOfFolderRetrievedDraft();
	}

	public String getNameOfFolderRetrievedFieldOfficeSealed()
	{
		return store.getNameOfFolderRetrievedFieldOfficeSealed();
	}

	public String getNameOfFolderRetrievedFieldOfficeDraft()
	{
		return store.getNameOfFolderRetrievedFieldOfficeDraft();
	}

	public BulletinFolder createOrFindFolder(String name)
	{
		return store.createOrFindFolder(name);
	}

	public void setMaxNewFolders(int numFolders)
	{
		maxNewFolders = numFolders;
	}

	public BulletinFolder createUniqueFolder(String originalFolderName)
	{
		BulletinFolder newFolder = null;
		String uniqueFolderName = null;
		int folderIndex = 0;
		while (newFolder == null && folderIndex < maxNewFolders)
		{
			uniqueFolderName = originalFolderName;
			if (folderIndex > 0)
				uniqueFolderName += folderIndex;
			newFolder = store.createFolder(uniqueFolderName);
			++folderIndex;
		}
		if(newFolder != null)
			store.saveFolders();
		return newFolder;
	}
	
	public void cleanupWhenCompleteQuickErase()
	{
		store.deleteFoldersDatFile();	
	}
	
	public void deleteKeypairAndRelatedFilesForAccount(File accountDirectory)
	{
		File keyPairFile = getKeyPairFile(accountDirectory);
		DirectoryUtils.scrubAndDeleteFile(keyPairFile);
		DirectoryUtils.scrubAndDeleteFile(getBackupFile(keyPairFile));
		DirectoryUtils.scrubAndDeleteFile(getUserNameHashFile(keyPairFile.getParentFile()));
		DirectoryUtils.scrubAndDeleteFile(getConfigInfoFileForAccount(accountDirectory));
		DirectoryUtils.scrubAndDeleteFile(getConfigInfoSignatureFileForAccount(accountDirectory));
		DirectoryUtils.scrubAndDeleteFile(getUploadInfoFileForAccount(accountDirectory));
		DirectoryUtils.scrubAndDeleteFile(getUiStateFileForAccount(accountDirectory));
		DirectoryUtils.scrubAndDeleteFile(ClientBulletinStore.getFoldersFileForAccount(accountDirectory));
		DirectoryUtils.scrubAndDeleteFile(ClientBulletinStore.getCacheFileForAccount(accountDirectory));
		File[] exportedKeys = exportedPublicKeyFiles(accountDirectory);
		for (int i = 0; i < exportedKeys.length; i++)
		{
			File file = exportedKeys[i];
			DirectoryUtils.scrubAndDeleteFile(file);
		}
	}

	private static File[] exportedPublicKeyFiles(File accountDir)
	{
		File[] mpiFiles = accountDir.listFiles(new FileFilter()
		{
			public boolean accept(File file)
			{
				return (file.isFile() && file.getName().endsWith(".mpi"));	
			}
		});
		return mpiFiles;
	}

	private File[] GetMlpFiles()
	{
		File[] mpiFiles = martusDataRootDirectory.listFiles(new FileFilter()
		{
			public boolean accept(File file)
			{
				return (file.isFile() && file.getName().endsWith(MtfAwareLocalization.MARTUS_LANGUAGE_PACK_SUFFIX));	
			}
		});
		return mpiFiles;
	}

	public boolean deleteAllBulletinsAndUserFolders()
	{
		try
		{											
			store.scrubAllData();
			store.deleteAllData();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public int quarantineUnreadableBulletins()
	{
		return store.quarantineUnreadableBulletins();
	}

	public int repairOrphans()
	{
		Set orphans = store.getSetOfOrphanedBulletinUniversalIds();
		int foundOrphanCount = orphans.size();
		if(foundOrphanCount == 0)
			return 0;

		Iterator it = orphans.iterator();
		while(it.hasNext())
		{
			UniversalId uid = (UniversalId)it.next();
			try
			{
				store.addRepairBulletinToFolders(uid);
			}
			catch (BulletinAlreadyExistsException e)
			{
				System.out.println("Orphan Bulletin already exists.");
			}
			catch (IOException shouldNeverHappen)
			{
				shouldNeverHappen.printStackTrace();
			}
		}

		store.saveFolders();
		return foundOrphanCount;
	}


	public Vector findBulletinInAllVisibleFolders(Bulletin b)
	{
		return store.findBulletinInAllVisibleFolders(b);
	}

	public boolean isDraftOutboxEmpty()
	{
		if(getFolderDraftOutbox().getBulletinCount() == 0)
			return true;
		return false;
	}

	public boolean isSealedOutboxEmpty()
	{
		if(getFolderSealedOutbox().getBulletinCount() == 0)
			return true;
		return false;
	}
	
	public void discardBulletinsFromFolder(BulletinFolder folderToDiscardFrom, Bulletin[] bulletinsToDiscard) throws IOException 
	{
		for (int i = 0; i < bulletinsToDiscard.length; i++)
		{
			Bulletin b = bulletinsToDiscard[i];
			getStore().discardBulletin(folderToDiscardFrom, b);
		}
		getStore().saveFolders();
	}

	public Date getUploadInfoElement(int index)
	{
		File file = getUploadInfoFile();
		if (!file.canRead())
			return null;
		Date date = null;
		try
		{
			ObjectInputStream stream = new ObjectInputStream(new FileInputStream(file));
			for(int i = 0 ; i < index ; ++i)
			{
				stream.readObject();
			}
			date = (Date)stream.readObject();
			stream.close();
		}
		catch (Exception e)
		{
			System.out.println("Error reading from getUploadInfoElement " + index + ":" + e);
		}
		return date;

	}

	public Date getLastUploadedTime()
	{
		return(getUploadInfoElement(0));
	}

	public Date getLastUploadRemindedTime()
	{
		return(getUploadInfoElement(1));
	}


	public void setUploadInfoElements(Date uploaded, Date reminded)
	{
		File file = getUploadInfoFile();
		file.delete();
		try
		{
			ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file));
			stream.writeObject(uploaded);
			stream.writeObject(reminded);
			stream.close();
		}
		catch (Exception e)
		{
			System.out.println("Error writing to setUploadInfoElements:" + e);
		}

	}

	public void setLastUploadedTime(Date uploaded)
	{
		Date reminded = getLastUploadRemindedTime();
		setUploadInfoElements(uploaded, reminded);
	}

	public void setLastUploadRemindedTime(Date reminded)
	{
		Date uploaded = getLastUploadedTime();
		setUploadInfoElements(uploaded, reminded);
	}

	public void resetLastUploadedTime()
	{
		setLastUploadedTime(new Date());
	}

	public void resetLastUploadRemindedTime()
	{
		setLastUploadRemindedTime(new Date());
	}

	public void search(String searchFor, String andKeyword, String orKeyword)
	{
		SearchParser parser = new SearchParser(andKeyword, orKeyword);
		SearchTreeNode searchNode = parser.parse(searchFor);
		BulletinSearcher matcher = new BulletinSearcher(searchNode);

		BulletinFolder searchFolder = createOrFindFolder(store.getSearchFolderName());
		searchFolder.removeAll();

		Vector uids = store.getAllBulletinLeafUids();
		for(int i = 0; i < uids.size(); ++i)
		{
			UniversalId leafBulletinUid = (UniversalId)uids.get(i);
			BulletinHistory history = store.getBulletinRevision(leafBulletinUid).getHistory();
			Vector allRevisions = new Vector();
			allRevisions.add(leafBulletinUid);
			for(int h=0; h<history.size(); ++h)
			{
				allRevisions.add(UniversalId.createFromAccountAndLocalId(leafBulletinUid.getAccountId(), history.get(h)));
			}
			
			for(int j = 0; j < allRevisions.size(); ++j)
			{
				Bulletin b = store.getBulletinRevision((UniversalId)allRevisions.get(j));
				if(b != null && matcher.doesMatch(new SafeReadableBulletin(b)))
				{	
					try
					{
						store.addBulletinToFolder(searchFolder, leafBulletinUid);
					}
					catch (BulletinAlreadyExistsException safeToIgnoreException)
					{
					}
					catch (BulletinOlderException safeToIgnoreException)
					{
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		store.saveFolders();
	}

	public boolean isNonSSLServerAvailable(String serverName)
	{
		if(serverName.length() == 0)
			return false;

		NonSSLNetworkAPI server = new ClientSideNetworkHandlerUsingXmlRpcForNonSSL(serverName);
		return ClientSideNetworkHandlerUsingXmlRpcForNonSSL.isNonSSLServerAvailable(server);
	}

	public boolean isSSLServerAvailable()
	{
		if(currentNetworkInterfaceHandler == null && !isServerConfigured())
			return false;

		return isSSLServerAvailable(getCurrentNetworkInterfaceGateway());
	}
	
	public boolean isServerConfigured()
	{
		return (getServerName().length() > 0);
	}

	public boolean isSignedIn()
	{
		return getSecurity().hasKeyPair();
	}

	public String getServerPublicKey(String serverName) throws
		ServerNotAvailableException,
		PublicInformationInvalidException
	{
		NonSSLNetworkAPI server = new ClientSideNetworkHandlerUsingXmlRpcForNonSSL(serverName);
		return getServerPublicKey(server);
	}

	public String getServerPublicKey(NonSSLNetworkAPI server) throws
		ServerNotAvailableException,
		PublicInformationInvalidException
	{
		return server.getServerPublicKey(getSecurity());
	}

	public boolean requestServerUploadRights(String magicWord)
	{
		try
		{
			NetworkResponse response = getCurrentNetworkInterfaceGateway().getUploadRights(getSecurity(), magicWord);
			if(response.getResultCode().equals(NetworkInterfaceConstants.OK))
				return true;
		}
		catch(MartusCrypto.MartusSignatureException e)
		{
			System.out.println("MartusApp.requestServerUploadRights: " + e);
		}

		return false;
	}

	public Vector getNewsFromServer()
	{
		if(!isSSLServerAvailable())
			return new Vector();

		try
		{
			NetworkResponse response = getCurrentNetworkInterfaceGateway().getNews(getSecurity(), UiConstants.versionLabel);
			if(response.getResultCode().equals(NetworkInterfaceConstants.OK))
				return response.getResultVector();
		}
		catch (MartusSignatureException e)
		{
			System.out.println("MartusApp.getNewsFromServer :" + e);
		}
		return new Vector();
	}

	public String getServerCompliance(ClientSideNetworkGateway gateway) 
		throws ServerCallFailedException, ServerNotAvailableException
	{
		if(!isSSLServerAvailable(gateway))
			throw new ServerNotAvailableException();
		try
		{
			NetworkResponse response = gateway.getServerCompliance(getSecurity());
			if(response.getResultCode().equals(NetworkInterfaceConstants.OK))
				return (String)response.getResultVector().get(0);
		}
		catch (Exception e)
		{
			//System.out.println("MartusApp.getServerCompliance :" + e);
			throw new ServerCallFailedException();
		}		
		throw new ServerCallFailedException();
	}

	void moveBulletinToDamaged(BulletinFolder outbox, UniversalId uid)
	{
		System.out.println("Moving bulletin to damaged");
		BulletinFolder damaged = createOrFindFolder(store.getNameOfFolderDamaged());
		Bulletin b = store.getBulletinRevision(uid);
		store.moveBulletin(b, outbox, damaged);		
	}

	public static class DamagedBulletinException extends Exception
	{
		public DamagedBulletinException(String message)
		{
			super(message);
		}

	}

	public Vector downloadFieldOfficeAccountIds() throws ServerErrorException
	{
		if(!isSSLServerAvailable())
			throw new ServerErrorException();

		ClientSideNetworkGateway networkInterfaceGateway = getCurrentNetworkInterfaceGateway();
		MartusCrypto security = getSecurity();
		String myAccountId = getAccountId();

		return networkInterfaceGateway.downloadFieldOfficeAccountIds(security, myAccountId);
	}
	
	public BulletinHeaderPacket retrieveHeaderPacketFromServer(UniversalId bulletinId) throws Exception
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(bulletinId);
		populatePacketFromServer(bhp, bulletinId.getLocalId());
		return bhp;
	}

	public FieldDataPacket retrieveFieldDataPacketFromServer(UniversalId bulletinId, String dataPacketLocalId) throws Exception
	{
		UniversalId packetUid = UniversalId.createFromAccountAndLocalId(bulletinId.getAccountId(), dataPacketLocalId);
		FieldDataPacket fdp = new FieldDataPacket(packetUid, StandardFieldSpecs.getDefaultPublicFieldSpecs());
		populatePacketFromServer(fdp, bulletinId.getLocalId());
		return fdp;
	}

	private void populatePacketFromServer(Packet packet, String bulletinLocalId) throws MartusSignatureException, ServerErrorException, UnsupportedEncodingException, InvalidBase64Exception, IOException, InvalidPacketException, WrongPacketTypeException, SignatureVerificationException, DecryptionException, NoKeyPairException
	{
		NetworkResponse response = getCurrentNetworkInterfaceGateway().getPacket(getSecurity(), packet.getAccountId(), bulletinLocalId, packet.getLocalId());
		String resultCode = response.getResultCode();
		if(!resultCode.equals(NetworkInterfaceConstants.OK))
			throw new ServerErrorException(resultCode);

		String xmlEncoded = (String)response.getResultVector().get(0);
		String xml = new String(Base64.decode(xmlEncoded), "UTF-8");
		byte[] xmlBytes = xml.getBytes("UTF-8");
		ByteArrayInputStreamWithSeek in =  new ByteArrayInputStreamWithSeek(xmlBytes);
		packet.loadFromXml(in, getSecurity());
	}

	public BulletinSummary retrieveSummaryFromString(String accountId, String summaryAsString)
		throws ServerErrorException
	{
		try
		{
			BulletinSummary summary = BulletinSummary.createFromString(accountId, summaryAsString);
	
			if(!FieldDataPacket.isValidLocalId(summary.getFieldDataPacketLocalId()))
				throw new ServerErrorException();
		
			FieldDataPacket fdp = getFieldDataPacketFromStoreOrServer(summary);
			summary.setFieldDataPacket(fdp);
			
			return summary;
		}
		catch(BulletinSummary.WrongValueCount e)
		{
			throw new ServerErrorException("MartusApp.retrieveSummaryFromString expected: " + e.expected + " but got " + e.got + " values");
		}
		catch(Exception e)
		{
			//System.out.println("MartusApp.retrieveSummaryFromString Exception: bulletinLocalId=" + bulletinLocalId + " packetlocalId=" + packetlocalId );
			//e.printStackTrace();
			throw new ServerErrorException();
		}
}

	private FieldDataPacket getFieldDataPacketFromStoreOrServer(BulletinSummary summary) throws Exception
	{
		UniversalId uid = summary.getUniversalId();
		Bulletin bulletin = store.getBulletinRevision(uid);

		if (bulletin != null)
			return bulletin.getFieldDataPacket();

		return retrieveFieldDataPacketFromServer(uid, summary.getFieldDataPacketLocalId());
		
	}

	public void retrieveOneBulletinToFolder(UniversalId uid, BulletinFolder retrievedFolder, ProgressMeterInterface progressMeter) throws
		BulletinOlderException, Exception
	{
		File tempFile = getCurrentNetworkInterfaceGateway().retrieveBulletin(uid, getSecurity(), serverChunkSize, progressMeter);
		try
		{
			store.importZipFileBulletin(tempFile, retrievedFolder, true);
			Bulletin b = store.getBulletinRevision(uid);
			store.setIsOnServer(b);
		}
		finally
		{
			tempFile.delete();
		}
	}

	public String deleteServerDraftBulletins(Vector uidList) throws
		MartusSignatureException,
		WrongAccountException
	{
		String[] localIds = new String[uidList.size()];
		for (int i = 0; i < localIds.length; i++)
		{
			UniversalId uid = (UniversalId)uidList.get(i);
			if(!uid.getAccountId().equals(getAccountId()))
				throw new WrongAccountException();

			localIds[i] = uid.getLocalId();
		}
		NetworkResponse response = getCurrentNetworkInterfaceGateway().deleteServerDraftBulletins(getSecurity(), getAccountId(), localIds);
		return response.getResultCode();
	}

	public static class AccountAlreadyExistsException extends Exception 
	{
	}

	public static class CannotCreateAccountFileException extends IOException 
	{
	}

	public void createAccount(String userName, char[] userPassPhrase) throws
					Exception
	{
		if(doesAccountExist(userName, userPassPhrase))
			throw new AccountAlreadyExistsException();
		
		if(doesDefaultAccountExist())
			createAdditionalAccount(userName, userPassPhrase);
		else
			createAccountInternal(getMartusDataRootDirectory(), userName, userPassPhrase);
	}

	public boolean doesAccountExist(String userName, char[] userPassPhrase) throws Exception
	{
		return (getAccountDirectoryForUser(userName, userPassPhrase) != null);
	}

	public File getAccountDirectoryForUser(String userName, char[] userPassPhrase) throws Exception
	{
		Vector allAccountDirs = getAllAccountDirectories();
		MartusCrypto tempSecurity = new MartusSecurity();
		for(int i = 0; i<allAccountDirs.size(); ++i )
		{
			File testAccountDirectory = (File)allAccountDirs.get(i);
			if(isUserOwnerOfThisAccountDirectory(tempSecurity, userName, userPassPhrase, testAccountDirectory))
				return testAccountDirectory;
		}
		return null;
	}

	private void createAdditionalAccount(String userName, char[] userPassPhrase) throws Exception
	{
		File tempAccountDir = null;
		try
		{
			File accountsDirectory = getAccountsDirectory();
			accountsDirectory.mkdirs();
			tempAccountDir = File.createTempFile("temp", null, accountsDirectory);
			tempAccountDir.delete();
			tempAccountDir.mkdirs();
			createAccountInternal(tempAccountDir, userName, userPassPhrase);
			String realAccountDirName = getAccountDirectoryName(getAccountId());
			File realAccountDir = new File(accountsDirectory, realAccountDirName);

			if(tempAccountDir.renameTo(realAccountDir))
				setCurrentAccount(userName, realAccountDir);
			else
				System.out.println("createAdditionalAccount rename failed.");
		}
		catch (Exception e)
		{
			System.out.println("createAdditionalAccount failed.");
			DirectoryUtils.deleteEntireDirectoryTree(tempAccountDir);
			throw(e);
		}
	}

	public void createAccountInternal(File accountDataDirectory, String userName, char[] userPassPhrase) throws
		Exception
	{
		File keyPairFile = getKeyPairFile(accountDataDirectory);
		if(keyPairFile.exists())
			throw(new AccountAlreadyExistsException());
		getSecurity().clearKeyPair();
		getSecurity().createKeyPair();
		try
		{
			writeKeyPairFileWithBackup(keyPairFile, userName, userPassPhrase);
			attemptSignInInternal(keyPairFile, userName, userPassPhrase);
		}
		catch(Exception e)
		{
			getSecurity().clearKeyPair();
			throw(e);
		}
	}
	
	public Vector getAllAccountDirectories()
	{
		Vector accountDirectories = new Vector();
		accountDirectories.add(getMartusDataRootDirectory());
		File accountsDirectoryRoot = getAccountsDirectory();
		File[] contents = accountsDirectoryRoot.listFiles();
		if(contents== null)
			return accountDirectories;
		for (int i = 0; i < contents.length; i++)
		{
			File thisFile = contents[i];
			try
			{
				if(!thisFile.isDirectory())
				{	
					continue;
				}
				String name = thisFile.getName();
				if(name.length() != 24)
				{	
					continue;
				}
				if(MartusCrypto.removeNonDigits(name).length() != 20)
				{	
					continue;
				}
				accountDirectories.add(thisFile);
			}
			catch (Exception notAValidAccountDirectory)
			{
			}
		}
		return accountDirectories;
	}
	
	public File getAccountDirectory(String accountId) throws InvalidBase64Exception
	{
		String name = getAccountDirectoryName(accountId);
		File proposedAccountDir = new File(getAccountsDirectory(), name);
		if(proposedAccountDir.exists() && proposedAccountDir.isDirectory())
			return proposedAccountDir;
		
		File dataRootDir = getMartusDataRootDirectory();
		if(!getKeyPairFile(dataRootDir).exists())
			return dataRootDir;
		if(doesDirectoryContainAccount(dataRootDir, accountId))
			return dataRootDir;
		
		proposedAccountDir.mkdirs();
		return proposedAccountDir;
	}

	private String getAccountDirectoryName(String accountId)
		throws InvalidBase64Exception
	{
		return MartusCrypto.getFormattedPublicCode(accountId);
	}
	
	private boolean doesDirectoryContainAccount(File dir, String accountId)
	{
		File configFile = getConfigInfoFileForAccount(dir);
		File sigFile = getConfigInfoSignatureFileForAccount(dir);
		
		try 
		{
			return(isSignatureFileValid(configFile, sigFile, accountId));
		} 
		catch (Exception e) 
		{
			return false;
		}
	}

	public boolean doesAnyAccountExist()
	{
		Vector accountDirectories = getAllAccountDirectories();
		for (int i = 0; i < accountDirectories.size(); i++)
		{
			File thisDirectory = (File)accountDirectories.get(i);
			if(getKeyPairFile(thisDirectory).exists())
				return true;
		}
		return false;
	}
	
	public boolean doesDefaultAccountExist()
	{
		if(getKeyPairFile(getMartusDataRootDirectory()).exists())
			return true;

		File packetsDir = new File(getMartusDataRootDirectory(), PACKETS_DIRECTORY_NAME);
		if(!packetsDir.exists())
			return false;

		return (packetsDir.listFiles().length > 0);
	}

	public void exportPublicInfo(File exportFile) throws
		IOException,
		Base64.InvalidBase64Exception,
		MartusCrypto.MartusSignatureException
	{
		MartusUtilities.exportClientPublicKey(getSecurity(), exportFile);
	}

	public String extractPublicInfo(File file) throws
		IOException,
		Base64.InvalidBase64Exception,
		PublicInformationInvalidException
	{
		Vector importedPublicKeyInfo = MartusUtilities.importClientPublicKeyFromFile(file);
		String publicKey = (String) importedPublicKeyInfo.get(0);
		String signature = (String) importedPublicKeyInfo.get(1);
		MartusUtilities.validatePublicInfo(publicKey, signature, getSecurity());
		return publicKey;
	}

	public File getPublicInfoFile(String fileName)
	{
		fileName = MartusUtilities.toFileName(fileName);
		String completeFileName = fileName + PUBLIC_INFO_EXTENSION;
		return(new File(getCurrentAccountDirectoryName(), completeFileName));
	}

	public void attemptSignIn(String userName, char[] userPassPhrase) throws Exception
	{
		File keyPairFile = getAccountDirectoryForUser(userName, userPassPhrase);
		attemptSignInInternal(getKeyPairFile(keyPairFile), userName, userPassPhrase);
	}
	
	public void attemptReSignIn(String userName, char[] userPassPhrase) throws Exception
	{
		attemptReSignInInternal(getCurrentKeyPairFile(), userName, userPassPhrase);
	}
	
	private String getCurrentLanguage()
	{
		return localization.getCurrentLanguageCode();
	}

	public String getAccountId()
	{
		return getSecurity().getPublicKeyString();
	}
	
	public void writeKeyPairFileWithBackup(File keyPairFile, String userName, char[] userPassPhrase) throws
		CannotCreateAccountFileException
	{
		writeKeyPairFileInternal(keyPairFile, userName, userPassPhrase);
		try
		{
			writeKeyPairFileInternal(getBackupFile(keyPairFile), userName, userPassPhrase);
		}
		catch (Exception e)
		{
			System.out.println("MartusApp.writeKeyPairFileWithBackup: " + e);
		}
	}

	protected void writeKeyPairFileInternal(File keyPairFile, String userName, char[] userPassPhrase) throws
		CannotCreateAccountFileException
	{
		try
		{
			FileOutputStream outputStream = new FileOutputStream(keyPairFile);
			try
			{
				getSecurity().writeKeyPair(outputStream, PasswordHelper.getCombinedPassPhrase(userName, userPassPhrase));
			}
			finally
			{
				outputStream.close();
			}
		}
		catch(IOException e)
		{
			throw(new CannotCreateAccountFileException());
		}

	}

	public void attemptSignInInternal(File keyPairFile, String userName, char[] userPassPhrase) throws Exception
	{
		try
		{
			getSecurity().readKeyPair(keyPairFile, PasswordHelper.getCombinedPassPhrase(userName, userPassPhrase));
			setCurrentAccount(userName, keyPairFile.getParentFile());
		}
		catch(Exception e)
		{
			getSecurity().clearKeyPair();
			currentUserName = "";
			throw e;
		}
	}
	
	public void attemptReSignInInternal(File keyPairFile, String userName, char[] userPassPhrase) throws Exception
	{
		if(!userName.equals(currentUserName))
			throw new MartusCrypto.AuthorizationFailedException();
		MartusCrypto securityOfReSignin = new MartusSecurity();
		FileInputStream inputStream = new FileInputStream(keyPairFile);
		try
		{
			securityOfReSignin.readKeyPair(inputStream, PasswordHelper.getCombinedPassPhrase(userName, userPassPhrase));
		}
		finally
		{
			inputStream.close();
		}
	}

	public void setCurrentAccount(String userName, File accountDirectory) throws IOException
	{
		currentUserName = userName;
		currentAccountDirectory = accountDirectory;
		updateUserNameHashFile();
	}

	private void updateUserNameHashFile() throws IOException
	{
		File hashUserName = getUserNameHashFile(currentAccountDirectory);
		hashUserName.delete();
		String hashOfUserName = MartusCrypto.getHexDigest(currentUserName);
		UnicodeWriter writer = new UnicodeWriter(hashUserName);
		try
		{
			writer.writeln(hashOfUserName);
		}
		finally
		{
			writer.close();
		}
	}
	
	public boolean isUserOwnerOfThisAccountDirectory(MartusCrypto tempSecurity, String userName, char[] userPassPhrase, File accountDirectory) throws IOException
	{
		File thisAccountsHashOfUserNameFile = getUserNameHashFile(accountDirectory);
		if(thisAccountsHashOfUserNameFile.exists())
		{
			UnicodeReader reader = new UnicodeReader(thisAccountsHashOfUserNameFile);
			try
			{
				String hashOfUserName = reader.readLine();
				String hexDigest = MartusCrypto.getHexDigest(userName);
				if(hashOfUserName.equals(hexDigest))
					return true;
			}
			finally
			{
				reader.close();
			}
			return false;
		}

		File thisAccountsKeyPair = getKeyPairFile(accountDirectory);
		try
		{
			tempSecurity.readKeyPair(thisAccountsKeyPair, PasswordHelper.getCombinedPassPhrase(userName, userPassPhrase));
			return true;
		}
		catch (Exception cantBeOurAccount)
		{
			return false;
		}
	}

	public File getUserNameHashFile(File accountDirectory)
	{
		return new File(accountDirectory, "AccountToken.txt");
	}

	public MartusCrypto getSecurity()
	{
		return store.getSignatureGenerator();
	}

	public void setSSLNetworkInterfaceHandlerForTesting(NetworkInterface server)
	{
		currentNetworkInterfaceHandler = server;
	}

	public boolean isSSLServerAvailable(ClientSideNetworkGateway server)
	{
		try
		{
			NetworkResponse response = server.getServerInfo();
			if(!response.getResultCode().equals(NetworkInterfaceConstants.OK))
				return false;

			String version = (String)response.getResultVector().get(0);
			if(version.indexOf("MartusServer") == 0)
				return true;
		}
		catch(Exception notInterestingBecauseTheServerMightJustBeDown)
		{
			//System.out.println("MartusApp.isSSLServerAvailable: " + e);
		}

		return false;
	}

	public ClientSideNetworkGateway getCurrentNetworkInterfaceGateway()
	{
		if(currentNetworkInterfaceGateway == null)
		{
			currentNetworkInterfaceGateway = new ClientSideNetworkGateway(getCurrentNetworkInterfaceHandler());
		}

		return currentNetworkInterfaceGateway;
	}

	private NetworkInterface getCurrentNetworkInterfaceHandler()
	{
		if(currentNetworkInterfaceHandler == null)
		{
			currentNetworkInterfaceHandler = createXmlRpcNetworkInterfaceHandler();
		}

		return currentNetworkInterfaceHandler;
	}

	private NetworkInterface createXmlRpcNetworkInterfaceHandler()
	{
		String ourServer = getServerName();
		String ourServerPublicKey = getConfigInfo().getServerPublicKey();
		return ClientSideNetworkGateway.buildNetworkInterface(ourServer,ourServerPublicKey);
	}

	private void invalidateCurrentHandlerAndGateway()
	{
		currentNetworkInterfaceHandler = null;
		currentNetworkInterfaceGateway = null;
	}

	private String getServerName()
	{
		return configInfo.getServerName();
	}

	private static File determineMartusDataRootDirectory()
	{
		String dir;
		if(Version.isRunningUnderWindows())
		{
			dir = "C:/Martus/";
		}
		else
		{
			String userHomeDir = System.getProperty("user.home");
			dir = userHomeDir + "/.Martus/";
		}
		File file = new File(dir);
		if(!file.exists())
		{
			file.mkdirs();
		}

		return file;
	}

	public void saveBulletin(Bulletin bulletinToSave, BulletinFolder outboxToUse) throws CryptoException, IOException, BulletinOlderException
	{
		store.saveBulletin(bulletinToSave);
		store.ensureBulletinIsInFolder(store.getFolderSaved(), bulletinToSave.getUniversalId());
		store.ensureBulletinIsInFolder(outboxToUse, bulletinToSave.getUniversalId());
		store.removeBulletinFromFolder(store.getFolderDiscarded(), bulletinToSave);
		store.setIsNotOnServer(bulletinToSave);
		store.saveFolders();
	}

	public class SaveConfigInfoException extends Exception 
	{
	}

	public class LoadConfigInfoException extends Exception 
	{
	}

	public static class MartusAppInitializationException extends Exception
	{
		MartusAppInitializationException(String message)
		{
			super(message);
		}
	}

	public File martusDataRootDirectory;
	protected File currentAccountDirectory;
	private MtfAwareLocalization localization;
	public ClientBulletinStore store;
	private ConfigInfo configInfo;
	public NetworkInterface currentNetworkInterfaceHandler;
	public ClientSideNetworkGateway currentNetworkInterfaceGateway;
	public String currentUserName;
	private int maxNewFolders;

	public static final String PUBLIC_INFO_EXTENSION = ".mpi";
	public static final String CUSTOMIZATION_TEMPLATE_EXTENSION = ".mct";
	public static final String DEFAULT_DETAILS_EXTENSION = ".txt";
	public static final String AUTHENTICATE_SERVER_FAILED = "Failed to Authenticate Server";
	public static final String SHARE_KEYPAIR_FILENAME_EXTENSION = ".dat";
	public static final String KEYPAIR_FILENAME = "MartusKeyPair.dat";
	public static final String ACCOUNTS_DIRECTORY_NAME = "accounts";
	public static final String PACKETS_DIRECTORY_NAME = "packets";
	public static final String DOCUMENTS_DIRECTORY_NAME = "Docs";
	public static final String USE_UNOFFICIAL_TRANSLATIONS_NAME = "use_unofficial_translations.txt";
	private final int MAXFOLDERS = 50;
	public int serverChunkSize = NetworkInterfaceConstants.MAX_CHUNK_SIZE;
}

