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
package org.martus.client.swingui.jfx.setupwizard.tasks;

import java.util.Vector;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.martus.client.core.MartusApp;
import org.martus.common.ContactKey;
import org.martus.common.fieldspec.CustomFieldTemplate;

public class DownloadTemplateListForAccountTask extends AbstractAppTask
{
	public DownloadTemplateListForAccountTask(MartusApp appToUse, ContactKey keyToUse, ObservableList<CustomFieldTemplate> listToUse)
	{
		super(appToUse);
		
		contactKey = keyToUse;
		formTemplates = listToUse;
	}

	@Override
	protected Void call() throws Exception
	{
		formTemplates.clear();
		formTemplates.addAll(getFormTemplates(contactKey));
		return null;
	}

	protected ObservableList<CustomFieldTemplate> getFormTemplates(ContactKey contactKey) throws Exception
	{
		return getFormTemplates(contactKey.getPublicKey());
	}
	
	protected ObservableList<CustomFieldTemplate> getFormTemplates(String publicKey) throws Exception
	{
		Vector returnedVectorListOfTemplatesFromServer = getApp().getListOfFormTemplatesOnServer(publicKey);

		return getTitlesFromResults(publicKey, returnedVectorListOfTemplatesFromServer);
	}
	
	private ObservableList<CustomFieldTemplate> getTitlesFromResults(String publicKey, Vector<Vector<String>> returnedVectorListOfTemplatesFromServer) throws Exception
	{
		ObservableList<CustomFieldTemplate> formTemplates = FXCollections.observableArrayList();
		for (int index = 0; index < returnedVectorListOfTemplatesFromServer.size(); ++index)
		{
			Vector<String> titleAndDescrptonVector = returnedVectorListOfTemplatesFromServer.get(index);
			String title = titleAndDescrptonVector.get(0);
			formTemplates.add(getApp().getFormTemplateOnServer(publicKey, title));
		}
		
		return formTemplates;
	}

	private ContactKey contactKey;
	private ObservableList<CustomFieldTemplate> formTemplates;
}
