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

import java.awt.Component;
import java.awt.Point;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.event.TreeSelectionEvent;

import org.martus.client.search.FancySearchGridEditor;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.PopUpTreeFieldSpec;
import org.martus.swing.UiCheckBox;

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

		if(!loadValuesCheckBox.isSelected())
			return true;

		try
		{
			foundValues = FancySearchGridEditor.loadFieldValuesWithProgressDialog(mainWindow, ddSpec);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	public Vector getFoundValues()
	{
		return foundValues;
	}
	
	public void valueChanged(TreeSelectionEvent e)
	{
		super.valueChanged(e);
		FieldSpec selectedSpec = getSelectedSpec();
		
		boolean isDropDown = selectedSpec.getType().isDropdown();
		boolean canLoad = canUseMemorizedPossibleValues(selectedSpec);
		boolean mustLoadValues = canLoad && isDropDown;
		loadValuesCheckBox.setSelected(mustLoadValues);
		loadValuesCheckBox.setEnabled(canLoad && !mustLoadValues);
	}
	
	protected Component[] getButtonBoxComponents(MiniLocalization localization)
	{
		if(loadValuesCheckBox == null)
			loadValuesCheckBox = new UiCheckBox(localization.getButtonLabel("LoadFieldValuesFromAllBulletins"));
		Vector components = new Vector(Arrays.asList(super.getButtonBoxComponents(localization)));
		components.insertElementAt(loadValuesCheckBox, 0);
		return (Component[]) components.toArray(new Component[0]);
	}
	
	public static boolean canUseMemorizedPossibleValues(FieldSpec selectedFieldSpec)
	{
		if(selectedFieldSpec.getType().isString())
			return true;
		
		if(selectedFieldSpec.getType().isDropdown())
		{
			DropDownFieldSpec spec = (DropDownFieldSpec) selectedFieldSpec;
			if(spec.getDataSourceGridTag() != null)
				return true;
		}
		return false;
	}

	private UiMainWindow mainWindow;
	private Vector foundValues;
	private UiCheckBox loadValuesCheckBox;
}
