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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.martus.client.swingui.UiMainWindow;
import org.martus.common.clientside.Localization;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;



public class UiShowScrollableTextDlg extends JDialog implements ActionListener
{
	public UiShowScrollableTextDlg(UiMainWindow owner, String titleTag, String okButtonTag, String cancelButtonTag, String descriptionTag, String text)
	{
		this(owner, titleTag, okButtonTag, cancelButtonTag, descriptionTag, text, new HashMap());
	}
	
	public UiShowScrollableTextDlg(UiMainWindow owner, String titleTag, String okButtonTag, String cancelButtonTag, String descriptionTag, String text, Map tokenReplacement)
	{
		super(owner, "", true);
		mainWindow = owner;

		try 
		{
			UiBasicLocalization localization = mainWindow.getLocalization();
			String windowTitle = localization.getWindowTitle(titleTag);
			setTitle(TokenReplacement.replaceTokens(windowTitle, tokenReplacement));
			String buttonLabel = localization.getButtonLabel(okButtonTag);
			ok = new JButton(TokenReplacement.replaceTokens(buttonLabel, tokenReplacement));
			ok.addActionListener(this);
			JButton cancel = null;
			if(!cancelButtonTag.equals(Localization.UNUSED_TAG))
			{
				buttonLabel = localization.getButtonLabel(cancelButtonTag);
				cancel = new JButton(TokenReplacement.replaceTokens(buttonLabel, tokenReplacement));
				cancel.addActionListener(this);
			}
			
			details = new UiWrappedTextArea(TokenReplacement.replaceTokens(text, tokenReplacement), 85);
			Rectangle rect = details.getVisibleRect();
			details.setEditable(false);
			JScrollPane detailScrollPane = new JScrollPane(details, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			detailScrollPane.setPreferredSize(new Dimension(rect.x, 400));		

			JPanel panel = new JPanel();
			panel.setBorder(new EmptyBorder(10,10,10,10));
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			
			if(!descriptionTag.equals(Localization.UNUSED_TAG))
			{
				String fieldLabel = localization.getFieldLabel(descriptionTag);
				fieldLabel = TokenReplacement.replaceTokens(fieldLabel, tokenReplacement);
				panel.add(new JLabel(" "));
				panel.add(new UiWrappedTextArea(fieldLabel));
			}
			panel.add(new JLabel(" "));
			panel.add(detailScrollPane);
			panel.add(new JLabel(" "));
			
			Box buttons = Box.createHorizontalBox();
			Dimension preferredSize = details.getPreferredSize();
			preferredSize.height = ok.getPreferredSize().height;
			buttons.setPreferredSize(preferredSize);
			buttons.add(ok);
			if(cancelButtonTag.length() != 0)
			{
				buttons.add(Box.createHorizontalGlue());
				buttons.add(cancel);
			}
			panel.add(buttons);
			
			getContentPane().add(panel);
			getRootPane().setDefaultButton(ok);
			Utilities.centerDlg(this);
			show();
		} 
		catch (TokenInvalidException e) 
		{
			e.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent ae)
	{
		result = false;
		if(ae.getSource() == ok)
			result = true;
		dispose();
	}

	public boolean getResult()
	{
		return result;
	}
	
	JButton ok;
	UiWrappedTextArea details;
	boolean result;
	UiMainWindow mainWindow;
}
