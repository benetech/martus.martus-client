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
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.reports.ChartAnswers;
import org.martus.client.search.FieldChooserSpecBuilder;
import org.martus.client.search.SearchFieldTreeNode;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiPopUpFieldChooserEditor;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.fieldspec.PopUpTreeFieldSpec;
import org.martus.common.fieldspec.SearchFieldTreeModel;
import org.martus.common.fieldspec.SearchableFieldChoiceItem;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.swing.UiButton;
import org.martus.swing.UiComboBox;
import org.martus.swing.UiLabel;
import org.martus.swing.UiTextField;
import org.martus.swing.Utilities;

import com.jhlabs.awt.GridLayoutPlus;

public class CreateChartDialog extends JDialog
{
	public CreateChartDialog(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;
		
		setTitle(getLocalization().getWindowTitle("CreateChart"));
		setModal(true);
		
		JPanel panel = new JPanel(new GridLayoutPlus(0, 2));
		getContentPane().add(panel);
		
		chartTypeComponent = createChartTypeComponent();
		Component[] typeRow = new Component[] {createLabel("ChartType"), chartTypeComponent};
		Utilities.addComponentsRespectingOrientation(panel, typeRow);
		
		Component[] fieldRow = new Component[] {createLabel("ChartFieldToCount"), createFieldChooserButton()};
		Utilities.addComponentsRespectingOrientation(panel, fieldRow);
		
		subtitleComponent = new UiTextField(40);
		Component[] subtitleRow = new Component[] {createLabel("ChartSubtitle"), subtitleComponent};
		Utilities.addComponentsRespectingOrientation(panel, subtitleRow);
		
		ok = new UiButton(getLocalization().getButtonLabel("ok"));
		ok.addActionListener(new OkHandler());

		UiButton cancel = new UiButton(getLocalization().getButtonLabel("cancel"));
		cancel.addActionListener(new CancelHandler());
		Box buttonBox = Box.createHorizontalBox();
		Utilities.addComponentsRespectingOrientation(buttonBox, new Component[] {Box.createHorizontalGlue(), ok, cancel});
		
		Utilities.addComponentsRespectingOrientation(panel, new Component[] {new UiLabel(" "), buttonBox});
		
		pack();
	}
	
	private UiComboBox createChartTypeComponent()
	{
		ChoiceItem[] choices = new ChoiceItem[] {
			createChartTypeChoiceItem(ChartAnswers.CHART_TYPE_BAR),
			createChartTypeChoiceItem(ChartAnswers.CHART_TYPE_3DBAR),
			createChartTypeChoiceItem(ChartAnswers.CHART_TYPE_PIE),
		};
		chartTypeComponent = new UiComboBox(choices);
		return chartTypeComponent;
	}

	private ChoiceItem createChartTypeChoiceItem(String chartType)
	{
		return new ChoiceItem(chartType, getChartTypeLabel(chartType));
	}

	private String getChartTypeLabel(String chartType)
	{
		return getLocalization().getFieldLabel("ChartType" + chartType);
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
			FieldSpec parentSpec = spec.getParent();
			if(parentSpec != null && parentSpec.getType().isGrid())
				rootNode.remove(i);
		}
	}

	public ChartAnswers getAnswers()
	{
		MiniFieldSpec fieldToCount = chooser.getSelectedMiniFieldSpec();
		ChartAnswers answers = new ChartAnswers(fieldToCount, getLocalization());
		answers.setChartType(getChartTypeCode());
		answers.setSubtitle(subtitleComponent.getText());
		
		return answers;
	}
	
	private String getChartTypeCode()
	{
		ChoiceItem selected = (ChoiceItem) chartTypeComponent.getSelectedItem();
		return selected.getCode();
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
	private UiComboBox chartTypeComponent;
	private UiPopUpFieldChooserEditor chooser;
	private JTextComponent subtitleComponent;
	private UiButton ok;
	
	private boolean result;
}
