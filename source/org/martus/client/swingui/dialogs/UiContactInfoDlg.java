/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2005, Beneficent
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
import javax.swing.JDialog;
import javax.swing.JLabel;
import org.martus.client.core.ConfigInfo;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.common.clientside.UiTextField;
import org.martus.swing.UiParagraphPanel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiTextArea;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;

public class UiContactInfoDlg extends JDialog implements ActionListener
{
	public UiContactInfoDlg(UiMainWindow mainWindow, ConfigInfo infoToUse)
	{
		super(mainWindow.getCurrentActiveFrame(), "", true);
		info = infoToUse;

		UiBasicLocalization localization = mainWindow.getLocalization();
	
		setTitle(localization.getWindowTitle("setupcontact"));
		ok = new JButton(localization.getButtonLabel("ok"));
		ok.addActionListener(this);
		JButton cancel = new JButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(this);

		source = new UiTextField(50);
		organization = new UiTextField(50);
		email = new UiTextField(50);
		webpage = new UiTextField(50);
		phone = new UiTextField(50);
		address = new UiTextArea(5, 50);
		address.setLineWrap(true);
		address.setWrapStyleWord(true);
		UiScrollPane addressScrollPane = new UiScrollPane(address, UiScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				UiScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		source.setText(info.getAuthor());
		organization.setText(info.getOrganization());
		email.setText(info.getEmail());
		webpage.setText(info.getWebPage());
		phone.setText(info.getPhone());
		address.setText(info.getAddress());

		UiParagraphPanel panel = new UiParagraphPanel();

		UiWrappedTextArea infoRequired = new UiWrappedTextArea(localization.getFieldLabel("ContactInfoRequiredFields"), 60);
		JLabel space = new JLabel(" ");
		infoRequired.setFont(space.getFont());
		infoRequired.setRows(2);
		panel.addOnNewLine(infoRequired);
		panel.addBlankLine();

		String authorPrompt = localization.getFieldLabel("AuthorRequired");
		panel.addComponents(new JLabel(authorPrompt), source);

		String organizationPrompt = localization.getFieldLabel("OrganizationRequired");
		panel.addComponents(new JLabel(organizationPrompt), organization);
		panel.addComponents(new JLabel(localization.getFieldLabel("email")), email);
		panel.addComponents(new JLabel(localization.getFieldLabel("webpage")), webpage);
		panel.addComponents(new JLabel(localization.getFieldLabel("phone")), phone);
		panel.addComponents(new JLabel(localization.getFieldLabel("address")), addressScrollPane);
		panel.addBlankLine();
		panel.addOnNewLine(new JLabel(localization.getFieldLabel("ContactInfoDescriptionOfFields")));
		panel.addBlankLine();

		UiWrappedTextArea infoFuture = new UiWrappedTextArea(localization.getFieldLabel("ContactInfoFutureUseOfFields"), 60);
		infoFuture.setFont(space.getFont());
		infoFuture.setRows(3);
		panel.addOnNewLine(infoFuture);

		panel.addBlankLine();
		panel.addOnNewLine(new JLabel(localization.getFieldLabel("ContactInfoUpdateLater")));
		panel.addBlankLine();

		panel.addComponents(ok, cancel);
		
		getContentPane().add(panel);
		getRootPane().setDefaultButton(ok);

		Utilities.centerDlg(this);
		setResizable(true);
		setVisible(true);
		toFront();
	}

	public boolean getResult()
	{
		return result;
	}

	public void actionPerformed(ActionEvent ae)
	{
		result = false;
		if(ae.getSource() == ok)
		{
			info.setAuthor(source.getText());
			info.setOrganization(organization.getText());
			info.setEmail(email.getText());
			info.setWebPage(webpage.getText());
			info.setPhone(phone.getText());
			info.setAddress(address.getText());
			result = true;
		}
		dispose();
	}

	ConfigInfo info;
	boolean result;

	UiTextField source;
	UiTextField organization;
	UiTextField email;
	UiTextField webpage;
	UiTextField phone;
	UiTextArea address;

	JButton ok;
}
