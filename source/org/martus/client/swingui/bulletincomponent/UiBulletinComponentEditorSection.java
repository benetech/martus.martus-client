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

import java.io.File;
import java.io.IOException;
import java.io.NotSerializableException;
import java.util.Vector;
import javax.swing.JComponent;
import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiAttachmentEditor;
import org.martus.client.swingui.fields.UiBoolEditor;
import org.martus.client.swingui.fields.UiChoiceEditor;
import org.martus.client.swingui.fields.UiDateEditor;
import org.martus.client.swingui.fields.UiField;
import org.martus.client.swingui.fields.UiFlexiDateEditor;
import org.martus.client.swingui.fields.UiGridEditor;
import org.martus.client.swingui.fields.UiMessageField;
import org.martus.client.swingui.fields.UiMultilineTextEditor;
import org.martus.client.swingui.fields.UiNormalTextEditor;
import org.martus.client.swingui.fields.UiUnknownViewer;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.clientside.ChoiceItem;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;

public class UiBulletinComponentEditorSection extends UiBulletinComponentDataSection
{

	public UiBulletinComponentEditorSection(UiMainWindow ownerToUse)
	{
		super(ownerToUse);
	}

	public UiField createUnknownField()
	{
		return new UiUnknownViewer(getLocalization());
	}
	
	public UiField createNormalField()
	{
		return new UiNormalTextEditor(getLocalization());
	}

	public UiField createMultilineField()
	{
		return new UiMultilineTextEditor(getLocalization());
	}

	public UiField createMessageField(FieldSpec spec)
	{
		return new UiMessageField(spec);
	}

	public UiField createChoiceField(ChoiceItem[] choices)
	{
		return new UiChoiceEditor(choices);
	}

	public UiField createChoiceField(Vector choices)
	{
		return new UiChoiceEditor(choices);
	}

	public UiField createDateField(FieldSpec spec)
	{		
		return new UiDateEditor(getLocalization(), spec);		
	}
	
	public UiField createFlexiDateField(FieldSpec spec)
	{
		return new UiFlexiDateEditor(getLocalization(), spec);	
	}

	public UiField createBoolField()
	{
		return new UiBoolEditor();
	}

	public UiField createGridField(GridFieldSpec fieldSpec)
	{
		UiLocalization localization = mainWindow.getLocalization();
		fieldSpec.setColumnZeroLabel(localization.getFieldLabel("ColumnGridRowNumber"));
		return new UiGridEditor(fieldSpec);
	}
	
	public void addAttachment(AttachmentProxy a)
	{
		attachmentEditor.addAttachment(a);
	}

	public void clearAttachments()
	{
		attachmentEditor.clearAttachments();
	}

	public JComponent createAttachmentTable()
	{
		attachmentEditor = new UiAttachmentEditor(getMainWindow());
		return attachmentEditor;
	}

	public void validateAttachments() throws AttachmentMissingException
	{
		AttachmentProxy[] publicAttachments = attachmentEditor.getAttachments();
		for(int aIndex = 0; aIndex < publicAttachments.length; ++aIndex)
		{
			File file = publicAttachments[aIndex].getFile();
			if (file != null)
			{
				if(!file.exists())
					throw new AttachmentMissingException(file.getAbsolutePath());
			}
		}
	}



	// This class is NOT intended to be serialized!!!
	private static final long serialVersionUID = 1;
	private void writeObject(java.io.ObjectOutputStream stream) throws IOException
	{
		throw new NotSerializableException();
	}

	UiAttachmentEditor attachmentEditor;
}
