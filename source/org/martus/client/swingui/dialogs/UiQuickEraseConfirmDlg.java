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
package org.martus.client.swingui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.martus.client.core.QuickEraseOptions;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.swing.ParagraphLayout;
import org.martus.swing.Utilities;

public class UiQuickEraseConfirmDlg extends JDialog
{
	public UiQuickEraseConfirmDlg(UiMainWindow mainWIndowToUse, UiBasicLocalization localization, String baseTag)
	{
		super(mainWIndowToUse, localization.getWindowTitle("confirm" + baseTag), true);
		mainWindow = mainWIndowToUse;
		
		JButton ok = new JButton(localization.getButtonLabel("ok"));
		ok.addActionListener(new OkHandler());
		JButton cancel = new JButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(new CancelHandler());

		getContentPane().setLayout(new ParagraphLayout());	
		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		
		JPanel panel = quickEraseOptioinsLayout(localization, baseTag);		
		getContentPane().add(panel);		
		
		String donotPromptStr = localization.getFieldLabel("DonotPrompt");			
		donotPrompt	= new JCheckBox(donotPromptStr, false);			
		getContentPane().add(donotPrompt, ParagraphLayout.NEW_LINE);
		
		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);	
		getContentPane().add(ok);
		getContentPane().add(cancel);	
		
		getRootPane().setDefaultButton(ok);

		Utilities.centerDlg(this);
	}	
	
	private JPanel quickEraseOptioinsLayout(UiBasicLocalization localization, String baseTag)
	{
		String cause = localization.getFieldLabel("confirm" + baseTag + "effect");	
		String effect = localization.getFieldLabel("confirm" + baseTag + "cause");	
		JLabel effectLabel = new JLabel(effect);
		JLabel causeLabel  = new JLabel(cause);
		
		String scrubStr = localization.getFieldLabel("ScrubDataBeforeDelete");
		String deleteKeyPairStr = localization.getFieldLabel("DeleteKeypair");
		String exitWhenCompleteStr = localization.getFieldLabel("ExitWhenComplete");
		
		scrubBeforeDelete = new JCheckBox(scrubStr, false);
		deleteKeyPair = new JCheckBox(deleteKeyPairStr, false);
		deleteKeyPair.addActionListener(new KeyPairEraseHandler());
		exitWhenComplete = new JCheckBox(exitWhenCompleteStr, false);
		JPanel panel = new JPanel();
		panel.setLayout(new ParagraphLayout());
		panel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(),""));
		
		panel.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);		
		panel.add(effectLabel);	
		panel.add(causeLabel, ParagraphLayout.NEW_LINE);	
		panel.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		panel.add(deleteKeyPair);		
		panel.add(scrubBeforeDelete, ParagraphLayout.NEW_LINE);
		panel.add(exitWhenComplete, ParagraphLayout.NEW_LINE);
		
		return panel;
	}

	class OkHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			if(!donotPrompt.isSelected() && !mainWindow.confirmDlgBeep("QuickEraseActionOkPressed"))
				return;
			action=true;
			setQuickEraseOptions();
			dispose();
		}
	}
	
	class KeyPairEraseHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			if(deleteKeyPair.isSelected())
			{	
				exitWhenComplete.setSelected(true);
				exitWhenComplete.setEnabled(false);
				mainWindow.notifyDlg("ChoosingQuickEraseKeyPair");
			}
			else
			{
				exitWhenComplete.setEnabled(true);
			}
		}
	}
	
	class CancelHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			action=false;			
			dispose();
		}
	}	
	
	public QuickEraseOptions getQuickEraseOptions()
	{
		return options;
	}
	
	public boolean isOkayPressed()
	{
		return action;
	}
	
	void setQuickEraseOptions()
	{
		options = new QuickEraseOptions();
		options.setScrubOption(scrubBeforeDelete.isSelected());
		options.setDeleteKeyPairOption(deleteKeyPair.isSelected());
		options.setExitWhenCompleteOption(exitWhenComplete.isSelected());
		options.setDonotPromptOption(donotPrompt.isSelected());	
	}
		
	JCheckBox scrubBeforeDelete;
	JCheckBox deleteKeyPair;
	JCheckBox exitWhenComplete;
	JCheckBox donotPrompt;
	QuickEraseOptions options;
	UiMainWindow mainWindow;
	boolean action;
}
