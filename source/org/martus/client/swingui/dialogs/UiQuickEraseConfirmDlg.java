/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2003, Beneficent
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
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.martus.client.swingui.UiLocalization;
import org.martus.swing.ParagraphLayout;
import org.martus.swing.Utilities;
public class UiQuickEraseConfirmDlg extends JDialog
{
	public UiQuickEraseConfirmDlg(JFrame owner, UiLocalization localization, String baseTag)
	{
		super(owner, "", true);

		setTitle(localization.getWindowTitle("confirm" + baseTag));		
		String cause = localization.getFieldLabel("confirm" + baseTag + "effect");	
		String effect = localization.getFieldLabel("confirm" + baseTag + "cause");	
		JLabel effectLabel = new JLabel(effect);
		JLabel causeLabel  = new JLabel(cause);
		
		String scrubStr = localization.getFieldLabel("ScrubDataBeforeDelete");
		String deleteKeyPairStr = localization.getFieldLabel("DeleteKeypair");
		String exitWhenCompleteStr = localization.getFieldLabel("ExitWhenComplete");
		
		scrubBeforeDelete 	= new JCheckBox(scrubStr, false);
		deleteKeyPair 		= new JCheckBox(deleteKeyPairStr, false);
		exitWhenComplete	= new JCheckBox(exitWhenCompleteStr, false);			

		JButton ok = new JButton(localization.getButtonLabel("ok"));
		ok.addActionListener(new OkHandler());
		JButton cancel = new JButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(new CancelHandler());

		getContentPane().setLayout(new ParagraphLayout());	
		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);		
		getContentPane().add(effectLabel);	
		getContentPane().add(causeLabel, ParagraphLayout.NEW_LINE);	
		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_LINE);	
		getContentPane().add(deleteKeyPair, ParagraphLayout.NEW_LINE);		
		getContentPane().add(scrubBeforeDelete, ParagraphLayout.NEW_LINE);
		getContentPane().add(exitWhenComplete, ParagraphLayout.NEW_LINE);
		
		getContentPane().add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);		
		getContentPane().add(ok);
		getContentPane().add(cancel);

		getRootPane().setDefaultButton(ok);

		Utilities.centerDlg(this);
		setResizable(false);
	}	
	
	class OkHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			action=true;
			dispose();
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
	
	public boolean isOkayPressed()
	{
		return action;
	}
	
	public boolean isScrubCheckBoxSelected()
	{
		return scrubBeforeDelete.isSelected();
	}
	
	public boolean isDeleteKeypairSelected()
	{
		return deleteKeyPair.isSelected(); 
	}
	
	public boolean isExitWhenCompleteSelected()
	{
		return exitWhenComplete.isSelected(); 
	}

	JCheckBox scrubBeforeDelete;
	JCheckBox deleteKeyPair;
	JCheckBox exitWhenComplete;
	boolean action;
}
