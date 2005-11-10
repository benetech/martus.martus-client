/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005, Beneficent
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
package org.martus.client.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;
import org.martus.clientside.PasswordHelper;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.XmlBulletinsImporter;
import org.martus.common.bulletin.XmlBulletinsImporter.FieldSpecVerificationException;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.crypto.MartusCrypto.AuthorizationFailedException;
import org.martus.common.crypto.MartusCrypto.CryptoInitializationException;
import org.martus.common.crypto.MartusCrypto.InvalidKeyPairFileVersionException;
import org.martus.common.fieldspec.CustomFieldError;
import org.martus.common.fieldspec.CustomFieldSpecValidator;
import org.martus.util.UnicodeReader;

public class ImportXmlBulletin
{
	public static void main(String[] args)
	{
		File importDirectory = null;
		File keyPairFile = null;
		boolean prompt = true;
		int exitStatus = 0;

		System.out.println("Martus Bulletin XML Importer");
				
		for (int i = 0; i < args.length; i++)
		{
			if(args[i].startsWith("--keypair"))
			{
				keyPairFile = new File(args[i].substring(args[i].indexOf("=")+1),"MartusKeyPair.dat");
			}
			
			if(args[i].startsWith("--import-directory="))
			{
				importDirectory = new File(args[i].substring(args[i].indexOf("=")+1));
			}

			if( args[i].startsWith("--no-prompt") )
			{
				prompt = false;
			}
		}

		if(importDirectory == null || keyPairFile == null )
		{
			System.err.println("\nUsage: ImportXmlBulletin --import-directory=<pathToXmlFiles> --keypair=<pathToKeyPairFile> [--no-prompt]");
			System.exit(2);
		}
		
		if(!importDirectory.exists() || !importDirectory.isDirectory())
		{
			System.err.println("Cannot find directory: " + importDirectory);
			System.exit(3);
		}
		
		if(!keyPairFile.exists() || !keyPairFile.isFile())
		{
			System.err.println("Cannot find file: " + keyPairFile);
			System.exit(3);
		}
		
		MartusCrypto security = null;
		String userName = "";
		try
		{
			if(prompt)
			{
				System.out.print("Enter User Name:");
				System.out.flush();
			}

			BufferedReader reader = new BufferedReader(new UnicodeReader(System.in));
			userName = reader.readLine();
		}
		catch(Exception e)
		{
			System.err.println("ImportXmlBulletin.main UserName: " + e);
			System.exit(4);
		}
		
		String userPassPhrase = "";
		try
		{
			if(prompt)
			{
				System.out.print("Enter Password:");
				System.out.flush();
			}

			BufferedReader reader = new BufferedReader(new UnicodeReader(System.in));
			//TODO security issue here password is a string.
			userPassPhrase = reader.readLine();
		}
		catch(Exception e)
		{
			System.err.println("ImportXmlBulletin.main Password: " + e);
			System.exit(4);
		}

		//TODO security issue here passphrase is a string.
		char[] passpharse = PasswordHelper.getCombinedPassPhrase(userName, userPassPhrase.toCharArray());
		try
		{
			security = loadCurrentMartusSecurity(keyPairFile, passpharse);
		}
		catch(Exception e)
		{
			System.err.println("Error username or password incorrect: " + e);
			System.exit(5);
		}
		File[] bulletinXmlFilesToImport = fileFilter(importDirectory);
		if(bulletinXmlFilesToImport.length == 0)
		{
			System.err.println("Error No XML bulletins found.");
			System.exit(6);
			
		}
		for(int i= 0; i < bulletinXmlFilesToImport.length; ++i)
		{
			try
			{
				FileInputStream xmlIn = new FileInputStream(bulletinXmlFilesToImport[i]);
				XmlBulletinsImporter importer = new XmlBulletinsImporter(security, xmlIn);
				Bulletin[] bulletins = importer.getBulletins();
				for(int j = 0; j < bulletins.length; ++j)
				{
					Bulletin b =  bulletins[j];
					System.out.println("Importing:" +b.get(Bulletin.TAGTITLE));
				}
			}
			catch(FieldSpecVerificationException e)
			{
				System.err.println("FieldSpecVerificationException Error in File:" + bulletinXmlFilesToImport[i].getName());
				System.err.println(getValidationErrorMessage(e.getErrors()));
				System.exit(7);
				
			}
			catch(Exception e)
			{
				System.err.println("ImportXmlBulletin Error in File:" + bulletinXmlFilesToImport[i].getName());
				System.err.println("ImportXmlBulletin.main importing Error: " + e);
				System.exit(8);
			}
		}
		System.out.println("Finished!");
		System.exit(exitStatus);
	}
	private static MartusCrypto loadCurrentMartusSecurity(File keyPairFile, char[] passphrase) throws CryptoInitializationException, FileNotFoundException, IOException, InvalidKeyPairFileVersionException, AuthorizationFailedException
	{
		MartusCrypto security = new MartusSecurity();
		FileInputStream in = new FileInputStream(keyPairFile);
		security.readKeyPair(in, passphrase);
		in.close();
		return security;
	}
	
	private static File[] fileFilter(File startingDir)
	{
		File[] filesToImport = startingDir.listFiles(new FileFilter()
		{
			public boolean accept(File fileName)
			{
				return fileName.getName().toUpperCase().endsWith(".XML");	
			}
		});
		return filesToImport;
	}
	
	
	public static String getValidationErrorMessage(Vector errors)
	{
		StringBuffer validationErrorMessages = new StringBuffer();
		for(int i = 0; i<errors.size(); ++i)
		{
			validationErrorMessages.append("\n\nBulletin " +(i+1)+"\n");
			CustomFieldSpecValidator currentValidator = (CustomFieldSpecValidator)errors.get(i);
			Vector validationErrors = currentValidator.getAllErrors();
			for(int j = 0; j<validationErrors.size(); ++j)
			{
				CustomFieldError thisError = (CustomFieldError)validationErrors.get(j);
				StringBuffer thisErrorMessage = new StringBuffer(thisError.getCode());
				thisErrorMessage.append(" : ");
				thisErrorMessage.append(thisError.getType());
				thisErrorMessage.append(" : ");
				thisErrorMessage.append(thisError.getTag());
				thisErrorMessage.append(" : ");
				thisErrorMessage.append(thisError.getLabel());
				validationErrorMessages.append(thisErrorMessage);
				validationErrorMessages.append('\n');
			}
		}		
		validationErrorMessages.append("\n\nTo see a list of the errors, please run Martus go to Options, Custom Fields and change <CustomFields> to <xCustomFields> and press OK.");
		return validationErrorMessages.toString();  
	}
	
}
