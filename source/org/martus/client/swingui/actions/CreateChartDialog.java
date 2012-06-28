/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2012, Beneficent
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
package org.martus.client.swingui.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JDialog;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.reports.ChartAnswers;
import org.martus.client.search.FieldChooserSpecBuilder;
import org.martus.client.search.SearchFieldTreeNode;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiPopUpFieldChooserEditor;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.fieldspec.PopUpTreeFieldSpec;
import org.martus.common.fieldspec.SearchFieldTreeModel;
import org.martus.common.fieldspec.SearchableFieldChoiceItem;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiParagraphPanel;
import org.martus.swing.Utilities;

public class CreateChartDialog extends JDialog
{
	public CreateChartDialog(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
		
		setTitle(getLocalization().getWindowTitle("CreateChart"));
		setModal(true);
		
		UiParagraphPanel panel = new UiParagraphPanel();
		getContentPane().add(panel);
		
		panel.addComponents(createLabel("ChartFieldToCount"), createFieldChooserButton());
		
		ok = new UiButton(getLocalization().getButtonLabel("ok"));
		ok.addActionListener(new OkHandler());

		UiButton cancel = new UiButton(getLocalization().getButtonLabel("cancel"));
		cancel.addActionListener(new CancelHandler());
		Box buttonBox = Box.createHorizontalBox();
		Utilities.addComponentsRespectingOrientation(buttonBox, new Component[] {ok, cancel});
		
		panel.addComponents(new UiLabel(" "), buttonBox);
		
		pack();
	}
	
	public boolean getResult()
	{
		return result;
	}

	private Component createFieldChooserButton()
	{
		chooser = new UiPopUpFieldChooserEditor(getMainWindow());
		FieldChooserSpecBuilder specBuilder = new FieldChooserSpecBuilder(getLocalization());
		PopUpTreeFieldSpec treeSpec = specBuilder.createSpec(getStore());
		removeGridFields(treeSpec);
		chooser.setSpec(treeSpec);

		FieldSpec dateEnteredSpec = StandardFieldSpecs.findStandardFieldSpec(BulletinConstants.TAGENTRYDATE);
		SearchableFieldChoiceItem initialChoice = new SearchableFieldChoiceItem(dateEnteredSpec);
		String initialCode = initialChoice.getCode();
		chooser.setText(initialCode);

		return chooser.getComponent();
	}
	
	class OkHandler implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			doOk();
		}

	}
	
	protected void doOk()
	{
		result = true;
		dispose();
	}

	class CancelHandler implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			doCancel();
		}

	}

	protected void doCancel()
	{
		result = false;
		dispose();
	}
	
	private ClientBulletinStore getStore()
	{
		return getMainWindow().getApp().getStore();
	}

	private void removeGridFields(PopUpTreeFieldSpec treeSpec)
	{
		SearchFieldTreeModel model = treeSpec.getTreeModel();
		SearchFieldTreeNode rootNode = (SearchFieldTreeNode) model.getRoot();
		for(int i = rootNode.getChildCount() - 1; i >= 0; --i)
		{
			SearchFieldTreeNode fieldNode = (SearchFieldTreeNode) rootNode.getChildAt(i);
			SearchableFieldChoiceItem fieldChoiceItem = fieldNode.getChoiceItem();
			FieldSpec spec = fieldChoiceItem.getSpec();
			if(spec.getParent() != null)
				rootNode.remove(i);
		}
	}

	public ChartAnswers getAnswers()
	{
		MiniFieldSpec fieldToCount = chooser.getSelectedMiniFieldSpec();
		ChartAnswers answers = new ChartAnswers(fieldToCount, getLocalization());
		answers.setSubtitle("User-entered subtitle here");
		
		return answers;
	}
	
	private Component createLabel(String fieldName)
	{
		return new UiLabel(getLabel(fieldName));
	}

	private String getLabel(String fieldName)
	{
		return getLocalization().getFieldLabel(fieldName);
	}

	public MartusLocalization getLocalization()
	{
		return getMainWindow().getLocalization();
	}

	private UiMainWindow getMainWindow()
	{
		return mainWindow;
	}

	private UiMainWindow mainWindow;
	private UiPopUpFieldChooserEditor chooser;
	private UiButton ok;
	
	private boolean result;
}
