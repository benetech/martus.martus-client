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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.Vector;

import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.common.ContactInfo;
import org.martus.common.MartusUtilities;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.MartusUtilities.FileTooLargeException;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.crypto.MartusCrypto.DecryptionException;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.crypto.MartusCrypto.NoKeyPairException;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.packet.Packet;
import org.martus.common.packet.UniversalId;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.Packet.WrongPacketTypeException;
import org.martus.util.Base64;

public class BackgroundUploader
{
	public BackgroundUploader(MartusApp appToUse, ProgressMeterInterface progressMeterToUse)
	{
		app = appToUse;
		progressMeter = progressMeterToUse;
	}

	public UploadResult backgroundUpload() throws
		MartusApp.DamagedBulletinException
	{
		UploadResult uploadResult = new UploadResult();
		uploadResult.result = NetworkInterfaceConstants.UNKNOWN;
	
		BulletinStore store = app.getStore();
		BulletinFolder folderSealedOutbox = app.getFolderSealedOutbox();
		BulletinFolder folderDraftOutbox = app.getFolderDraftOutbox();
		if(store.hasAnyNonDiscardedBulletins(folderSealedOutbox))
			uploadResult = backgroundUploadOneBulletin(folderSealedOutbox);
		else if(store.hasAnyNonDiscardedBulletins(folderDraftOutbox))
			uploadResult = backgroundUploadOneBulletin(folderDraftOutbox);
		else if(app.getConfigInfo().shouldContactInfoBeSentToServer())
			uploadResult = sendContactInfoToServer();
	
		if(uploadResult.isHopelesslyDamaged)
			throw new MartusApp.DamagedBulletinException(uploadResult.exceptionThrown);
		return uploadResult;
	}

	public String uploadBulletin(Bulletin b) throws
			InvalidPacketException, WrongPacketTypeException, SignatureVerificationException, DecryptionException, NoKeyPairException, CryptoException, FileNotFoundException, MartusSignatureException, FileTooLargeException, IOException
	{
		File tempFile = File.createTempFile("$$$MartusUploadBulletin", null);
		try
		{
			tempFile.deleteOnExit();
			UniversalId uid = b.getUniversalId();

			Database db = app.getStore().getDatabase();
			DatabaseKey headerKey = DatabaseKey.createKey(uid, b.getStatus());
			MartusCrypto security = app.getSecurity();
			BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(db, headerKey, tempFile, security);
			
			String tag = getUploadProgressTag(b);
			progressMeter.setStatusMessageTag(tag);
			return uploadBulletinZipFile(uid, tempFile);
		}
		finally
		{
			tempFile.delete();
		}
	}
	
	private String getUploadProgressTag(Bulletin b)
	{
		if(b.isDraft())
			return "UploadingDraftBulletin";
		return "UploadingSealedBulletin";
	}
	
	private String uploadBulletinZipFile(UniversalId uid, File tempFile)
		throws
			FileTooLargeException,
			FileNotFoundException,
			IOException,
			MartusSignatureException
	{
		int totalSize = MartusUtilities.getCappedFileLength(tempFile);
		int offset = 0;
		byte[] rawBytes = new byte[app.serverChunkSize];
		FileInputStream inputStream = new FileInputStream(tempFile);
		String result = null;
		while(true)
		{
			if(progressMeter != null)
				progressMeter.updateProgressMeter(offset, totalSize);
			int chunkSize = inputStream.read(rawBytes);
			if(chunkSize <= 0)
				break;
			byte[] chunkBytes = new byte[chunkSize];
			System.arraycopy(rawBytes, 0, chunkBytes, 0, chunkSize);
		
			String authorId = uid.getAccountId();
			String bulletinLocalId = uid.getLocalId();
			String encoded = Base64.encode(chunkBytes);
		
			NetworkResponse response = app.getCurrentNetworkInterfaceGateway().putBulletinChunk(app.getSecurity(),
								authorId, bulletinLocalId, totalSize, offset, chunkSize, encoded);
			result = response.getResultCode();
			if(!result.equals(NetworkInterfaceConstants.CHUNK_OK) && !result.equals(NetworkInterfaceConstants.OK))
				break;
			offset += chunkSize;
		}
		inputStream.close();
		return result;
	}

	BackgroundUploader.UploadResult uploadOneBulletin(BulletinFolder uploadFromFolder)
	{
		BackgroundUploader.UploadResult uploadResult = new BackgroundUploader.UploadResult();
	
		if(!app.isSSLServerAvailable())
			return uploadResult;
	
		int index = new Random().nextInt(uploadFromFolder.getBulletinCount());
		BulletinStore store = app.getStore();
		Bulletin b = store.chooseBulletinToUpload(uploadFromFolder, index);
		uploadResult.uid = b.getUniversalId();
		try
		{
			uploadResult.result = uploadBulletin(b);
			if(uploadResult.result == null)
				return uploadResult;
			
			if(uploadResult.result.equals(NetworkInterfaceConstants.OK) || 
					uploadResult.result.equals(NetworkInterfaceConstants.DUPLICATE))
			{
				store.setIsOnServer(b);
				uploadFromFolder.remove(uploadResult.uid);
				store.saveFolders();
				
				// TODO: Is the file this creates ever used???
				app.resetLastUploadedTime();
			}
		}
		catch (Packet.InvalidPacketException e)
		{
			uploadResult.exceptionThrown = e.toString();
			uploadResult.isHopelesslyDamaged = true;
		} 
		catch (Packet.WrongPacketTypeException e)
		{
			uploadResult.exceptionThrown = e.toString();
			uploadResult.isHopelesslyDamaged = true;
		} 
		catch (Packet.SignatureVerificationException e)
		{
			uploadResult.exceptionThrown = e.toString();
			uploadResult.isHopelesslyDamaged = true;
		} 
		catch (MartusCrypto.DecryptionException e)
		{
			uploadResult.exceptionThrown = e.toString();
			uploadResult.isHopelesslyDamaged = true;
		} 
		catch (MartusUtilities.FileTooLargeException e)
		{
			uploadResult.exceptionThrown = e.toString();
			uploadResult.isHopelesslyDamaged = true;
		} 
		catch (MartusCrypto.NoKeyPairException e)
		{
			uploadResult.exceptionThrown = e.toString();
		} 
		catch (FileNotFoundException e)
		{
			uploadResult.exceptionThrown = e.toString();
		} 
		catch (MartusCrypto.MartusSignatureException e)
		{
			uploadResult.exceptionThrown = e.toString();
		} 
		catch (MartusCrypto.CryptoException e)
		{
			uploadResult.exceptionThrown = e.toString();
		} 
		catch (IOException e)
		{
			uploadResult.exceptionThrown = e.toString();
		}
		return uploadResult;
	}

	UploadResult backgroundUploadOneBulletin(BulletinFolder uploadFromFolder) throws
		MartusApp.DamagedBulletinException
	{
		UploadResult uploadResult = uploadOneBulletin(uploadFromFolder);
		
		if(uploadResult.isHopelesslyDamaged)
			app.moveBulletinToDamaged(uploadFromFolder, uploadResult.uid);

		return uploadResult;
	}

	public String putContactInfoOnServer(Vector info)  throws
			MartusCrypto.MartusSignatureException
	{
		ClientSideNetworkGateway gateway = app.getCurrentNetworkInterfaceGateway();
		NetworkResponse response = gateway.putContactInfo(app.getSecurity(), app.getAccountId(), info);
		return response.getResultCode();
	}

	UploadResult sendContactInfoToServer()
	{
		BackgroundUploader.UploadResult uploadResult = new BackgroundUploader.UploadResult();
		uploadResult.result = CONTACT_INFO_NOT_SENT;
		
		if(!app.isSSLServerAvailable())
			return uploadResult;
	
		String result = "";
		ConfigInfo configInfo = app.getConfigInfo();
		try
		{
			MartusCrypto signer = app.getSecurity();
			ContactInfo contactInfo = createContactInfo(configInfo);
			Vector contactInfoVector = contactInfo.getSignedEncodedVector(signer);
			result = putContactInfoOnServer(contactInfoVector);
		}
		catch (MartusCrypto.MartusSignatureException e)
		{
			System.out.println("MartusApp.sendContactInfoToServer Sig Error:" + e);
			return uploadResult;
		}
		catch (UnsupportedEncodingException e)
		{
			System.out.println("MartusApp.sendContactInfoToServer Encoding Error:" + e);
			return uploadResult;
		}
		if(!result.equals(NetworkInterfaceConstants.OK))
		{
			System.out.println("MartusApp.sendContactInfoToServer failure:" + result);
			return uploadResult;
		}
		System.out.println("Contact info successfully sent to server");
		uploadResult.result = result;
	
		try
		{
			configInfo.setSendContactInfoToServer(false);
			app.saveConfigInfo();
		}
		catch (SaveConfigInfoException e)
		{
			System.out.println("MartusApp:putContactInfoOnServer Failed to save contactinfo locally:" + e);
		}
		return uploadResult;
	}

	public static class UploadResult
	{
		public UniversalId uid;
		public String result;
		public String exceptionThrown;
		public boolean isHopelesslyDamaged;
	}
	
	private ContactInfo createContactInfo(ConfigInfo sourceOfInfo)
	{
		return new ContactInfo(sourceOfInfo.getAuthor(),
				sourceOfInfo.getOrganization(),
				sourceOfInfo.getEmail(),
				sourceOfInfo.getWebPage(),
				sourceOfInfo.getPhone(),
				sourceOfInfo.getAddress());
	}
	
	public static final String CONTACT_INFO_NOT_SENT="Contact Info Not Sent";

	MartusApp app;
	ProgressMeterInterface progressMeter;
}
