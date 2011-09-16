/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2011, Beneficent
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
package org.martus.client.swingui.fields;

import java.awt.Point;
import java.util.Vector;

import javax.swing.JDialog;

import org.martus.client.search.FancySearchGridEditor;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.PopUpTreeFieldSpec;

public class SearchFieldTreeDialog extends FieldTreeDialog
{
	public SearchFieldTreeDialog(UiMainWindow mainWindowToUse, JDialog owner, Point location, PopUpTreeFieldSpec specToUse)
	{
		super(owner, location, specToUse, mainWindowToUse.getLocalization());
		mainWindow = mainWindowToUse;
		foundValues = new Vector();
	}
	
	protected boolean canSaveAndExit(FieldSpec selectedSpec)
	{
		foundValues = new Vector();
		if(!super.canSaveAndExit(selectedSpec))
			return false;
		
		if(selectedSpec == null)
			return true;
		
		if(!selectedSpec.getType().isDropdown())
			return true;

		DropDownFieldSpec ddSpec = (DropDownFieldSpec)selectedSpec;
		if(!ddSpec.hasDataSource())
			return true;
		
		foundValues = FancySearchGridEditor.loadFieldValuesWithProgressDialog(mainWindow, ddSpec);
		return true;
	}
	
	public Vector getFoundValues()
	{
		return foundValues;
	}
	
	private UiMainWindow mainWindow;
	private Vector foundValues;
}
