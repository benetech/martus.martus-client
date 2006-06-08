/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006, Beneficent
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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.martus.clientside.UiLocalization;
import org.martus.common.fieldspec.PopUpTreeFieldSpec;
import org.martus.common.fieldspec.SearchableFieldChoiceItem;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiScrollPane;


public class UiPopUpTreeEditor extends UiField implements ActionListener
{
	public UiPopUpTreeEditor(UiLocalization localizationToUse)
	{
		owner = null;
		localization = localizationToUse;
		
		listeners = new Vector();
		
		panel = new JPanel(new BorderLayout());
		label = new UiLabel();
		button = new UiButton(localization.getButtonLabel("PopUpTreeChoose"));
		button.addActionListener(this);
		
		panel.add(label, BorderLayout.CENTER);
		panel.add(button, BorderLayout.AFTER_LINE_ENDS);
	}

	public JComponent getComponent()
	{
		return panel;
	}

	public JComponent[] getFocusableComponents()
	{
		return new JComponent[] {button};
	}

	public String getText()
	{
		return selectedItem.getCode();
	}

	public void setText(String newText)
	{
		selectedItem = spec.findCode(newText);
		label.setText(selectedItem.toString());
	}
	
	public void setSpec(PopUpTreeFieldSpec specToUse)
	{
		spec = specToUse;
	}
	
	public void actionPerformed(ActionEvent event)
	{
		FieldTreeDialog dlg = new FieldTreeDialog(owner, spec, localization);
		dlg.setVisible(true);
		DefaultMutableTreeNode selectedNode = dlg.getSelectedNode();
		selectedItem = (SearchableFieldChoiceItem)selectedNode.getUserObject();
		label.setText(selectedNode.toString());
		notifyListeners();
	}
	
	void notifyListeners()
	{
		ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "none");
		for(int i = 0; i < listeners.size(); ++i)
		{
			ActionListener listener = (ActionListener)listeners.get(i);
			listener.actionPerformed(event);
		}
	}
	
	public void addActionListener(ActionListener listenerToAdd)
	{
		listeners.add(listenerToAdd);
	}
	
	static class FieldTreeDialog extends JDialog
	{
		public FieldTreeDialog(JFrame owner, PopUpTreeFieldSpec spec, UiLocalization localization)
		{
			super(owner);
			TreeModel model = spec.getModel();
			tree = new JTree(model);
			tree.setRootVisible(false);
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree.addMouseListener(new MouseHandler());
			tree.addSelectionRow(0);
			
			okButton = new UiButton(localization.getButtonLabel("ok"));
			okButton.addActionListener(new OkButtonHandler());
			cancelButton = new UiButton(localization.getButtonLabel("cancel"));
			cancelButton.addActionListener(new CancelButtonHandler());
			Box buttonBox = Box.createHorizontalBox();
			buttonBox.add(Box.createHorizontalGlue());
			buttonBox.add(okButton);
			buttonBox.add(cancelButton);
			
			setModal(true);
			Container contentPane = getContentPane();
			contentPane.setLayout(new BorderLayout());
			contentPane.add(new UiScrollPane(tree), BorderLayout.CENTER);
			contentPane.add(buttonBox, BorderLayout.AFTER_LAST_LINE);
			pack();
			
		}
		
		public DefaultMutableTreeNode getSelectedNode()
		{
			return selectedNode;
		}
		
		void saveAndExit()
		{
			TreePath selectedPath = tree.getSelectionPath();
			selectedNode = (DefaultMutableTreeNode)selectedPath.getLastPathComponent();
			dispose();
		}
		
		class OkButtonHandler implements ActionListener
		{
			public void actionPerformed(ActionEvent e)
			{
				saveAndExit();
			}
			
		}
		
		class CancelButtonHandler implements ActionListener
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
			
		}
		
		class MouseHandler implements MouseListener
		{
			public void mouseClicked(MouseEvent e)
			{
			}

			public void mouseEntered(MouseEvent e)
			{
			}

			public void mouseExited(MouseEvent e)
			{
			}
			
			public void mousePressed(MouseEvent e)
			{
			}
			
			public void mouseReleased(MouseEvent e)
			{
				if(e.getClickCount() == 2)
					saveAndExit();
			}
		}
		
		UiButton okButton;
		UiButton cancelButton;
		
		JTree tree;
		DefaultMutableTreeNode selectedNode;
	}

	JFrame owner;
	UiLocalization localization;
	PopUpTreeFieldSpec spec;
	JPanel panel;
	UiLabel label;
	UiButton button;
	SearchableFieldChoiceItem selectedItem;
	Vector listeners;
}
