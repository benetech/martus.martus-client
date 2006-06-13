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
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.martus.client.search.SearchFieldTreeNode;
import org.martus.clientside.UiLocalization;
import org.martus.common.fieldspec.PopUpTreeFieldSpec;
import org.martus.common.fieldspec.SearchFieldTreeModel;
import org.martus.common.fieldspec.SearchableFieldChoiceItem;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.Utilities;


public class UiPopUpTreeEditor extends UiField implements ActionListener
{
	public UiPopUpTreeEditor(UiLocalization localizationToUse)
	{
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
	
	public void simulateButtonPress()
	{
		doPopUp();
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
		doPopUp();
	}

	private void doPopUp()
	{
		FieldTreeDialog dlg = FieldTreeDialog.create(panel, spec, localization);
		dlg.selectCode(getText());
		dlg.setVisible(true);
		DefaultMutableTreeNode selectedNode = dlg.getSelectedNode();
		if(selectedNode == null)
			return;
		
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
	
	static class FieldTreeDialog extends JDialog implements TreeSelectionListener
	{
		static public FieldTreeDialog create(JComponent parent, PopUpTreeFieldSpec spec, UiLocalization localization)
		{
			Container topLevel = parent.getTopLevelAncestor();
			return new FieldTreeDialog((JDialog)topLevel, parent.getLocationOnScreen(), spec, localization);
		}
		
		public FieldTreeDialog(JDialog owner, Point location, PopUpTreeFieldSpec specToUse, UiLocalization localization)
		{
			super(owner);
			spec = specToUse;
			
			setTitle(localization.getButtonLabel("PopUpTreeChoose"));
			setLocation(location);
			
			okAction = new OkAction(localization.getButtonLabel("ok"));

			tree = new SearchFieldTree(spec.getModel());
			tree.setRootVisible(false);
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree.addMouseListener(new MouseHandler());
			tree.addKeyListener(new KeyHandler());
			tree.addTreeSelectionListener(this);
			tree.setShowsRootHandles(true);
			tree.setCellRenderer(new BlankLeafRenderer());
			
			okButton = new UiButton(okAction);
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
			Utilities.fitInScreen(this);

			getRootPane().setDefaultButton(okButton);
		}
		
		public void selectCode(String code)
		{
			tree.selectNodeContainingItem(spec.findCode(code));
		}
		
		public DefaultMutableTreeNode getSelectedNode()
		{
			return selectedNode;
		}
		
		void saveAndExitIfValidSelection()
		{
			if(!isSelectionValid())
				return;
			selectedNode = getSelectionIfAny();
			dispose();
		}
		
		SearchFieldTreeNode getSelectionIfAny()
		{
			TreePath selectedPath = tree.getSelectionPath();
			if(selectedPath == null)
				return null;
			SearchFieldTreeNode node = (SearchFieldTreeNode)selectedPath.getLastPathComponent();
			if(node == null)
				return null;
			if(!node.isSelectable())
				return null;
			return node;
		}
		
		boolean isSelectionValid()
		{
			return (getSelectionIfAny() != null);
		}
		
		public void valueChanged(TreeSelectionEvent e)
		{
			okAction.setEnabled(isSelectionValid());
		}
		
		class OkAction extends AbstractAction
		{
			public OkAction(String label)
			{
				super(label);
			}

			public void actionPerformed(ActionEvent e)
			{
				saveAndExitIfValidSelection();
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
				if(e.getClickCount() != 2)
					return;
				
				saveAndExitIfValidSelection();
			}
		}
		
		class KeyHandler extends KeyAdapter
		{
			public void keyTyped(KeyEvent e)
			{
				if(e.getKeyChar() == KeyEvent.VK_ESCAPE)
					dispose();
			}
		}

		OkAction okAction;
		UiButton okButton;
		UiButton cancelButton;
		
		PopUpTreeFieldSpec spec;
		SearchFieldTree tree;
		DefaultMutableTreeNode selectedNode;
	}
	
	static class BlankLeafRenderer extends DefaultTreeCellRenderer
	{
		public BlankLeafRenderer()
		{
			
		}

		public Icon getLeafIcon()
		{
			return new BlankIcon();
		}
		
		static class BlankIcon implements Icon
		{
			public int getIconHeight()
			{
				return 0;
			}

			public int getIconWidth()
			{
				return 0;
			}

			public void paintIcon(Component c, Graphics g, int x, int y)
			{
			}
			
		}
		
	}
	
	static class SearchFieldTree extends JTree
	{
		public SearchFieldTree(TreeModel model)
		{
			super(model);
		}
		
		public void selectNodeContainingItem(SearchableFieldChoiceItem selectedItem)
		{
			SearchFieldTreeModel model = (SearchFieldTreeModel)getModel();
			TreePath rootPath = new TreePath(model.getRoot());
			TreePath foundPath = model.findObject(rootPath, selectedItem.getCode());
			if(foundPath == null)
				throw new RuntimeException("Unable to find in tree: " + selectedItem);
			
			clearSelection();
			addSelectionPath(foundPath);
			scrollPathToVisible(foundPath);
		}
	}

	UiLocalization localization;
	PopUpTreeFieldSpec spec;
	JPanel panel;
	UiLabel label;
	UiButton button;
	SearchableFieldChoiceItem selectedItem;
	Vector listeners;
}
