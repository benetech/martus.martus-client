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

import java.awt.ComponentOrientation;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.martus.client.core.ConfigInfo;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.UiScrollPane;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.common.clientside.UiLanguageDirection;
import org.martus.common.clientside.UiSingleTextField;
import org.martus.swing.ParagraphLayout;
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

		ComponentOrientation componentOrientation = UiLanguageDirection.getComponentOrientation();
		source = new UiSingleTextField(50,componentOrientation);
		organization = new UiSingleTextField(50,componentOrientation);
		email = new UiSingleTextField(50,componentOrientation);
		webpage = new UiSingleTextField(50,componentOrientation);
		phone = new UiSingleTextField(50,componentOrientation);
		address = new UiTextArea(5, 50, componentOrientation);
		address.setLineWrap(true);
		address.setWrapStyleWord(true);
		UiScrollPane addressScrollPane = new UiScrollPane(address, UiScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				UiScrollPane.HORIZONTAL_SCROLLBAR_NEVER,componentOrientation);

		source.setText(info.getAuthor());
		organization.setText(info.getOrganization());
		email.setText(info.getEmail());
		webpage.setText(info.getWebPage());
		phone.setText(info.getPhone());
		address.setText(info.getAddress());

		JPanel panel = new JPanel();
		panel.setLayout(new ParagraphLayout());
		JLabel space = new JLabel(" ");
		panel.add(space, ParagraphLayout.NEW_PARAGRAPH);
		panel.add(new JLabel());

		UiWrappedTextArea infoRequired = new UiWrappedTextArea(localization.getFieldLabel("ContactInfoRequiredFields"), 60, componentOrientation);
		infoRequired.setFont(space.getFont());
		infoRequired.setRows(2);
		panel.add(infoRequired);

		String authorPrompt = localization.getFieldLabel("AuthorRequired");
		panel.add(new JLabel(authorPrompt), ParagraphLayout.NEW_PARAGRAPH);
		panel.add(source);

		String organizationPrompt = localization.getFieldLabel("OrganizationRequired");
		panel.add(new JLabel(organizationPrompt), ParagraphLayout.NEW_PARAGRAPH);
		panel.add(organization);
		panel.add(new JLabel(localization.getFieldLabel("email")), ParagraphLayout.NEW_PARAGRAPH);
		panel.add(email);
		panel.add(new JLabel(localization.getFieldLabel("webpage")), ParagraphLayout.NEW_PARAGRAPH);
		panel.add(webpage);
		panel.add(new JLabel(localization.getFieldLabel("phone")), ParagraphLayout.NEW_PARAGRAPH);
		panel.add(phone);
		panel.add(new JLabel(localization.getFieldLabel("address")), ParagraphLayout.NEW_PARAGRAPH);
		panel.add(addressScrollPane);
		panel.add(new JLabel(" "), ParagraphLayout.NEW_PARAGRAPH);
		panel.add(new JLabel(localization.getFieldLabel("ContactInfoDescriptionOfFields")));

		panel.add(new JLabel(" "), ParagraphLayout.NEW_PARAGRAPH);
		UiWrappedTextArea infoFuture = new UiWrappedTextArea(localization.getFieldLabel("ContactInfoFutureUseOfFields"), 60, componentOrientation);
		infoFuture.setFont(space.getFont());
		infoFuture.setRows(3);
		panel.add(infoFuture);

		panel.add(new JLabel(" "), ParagraphLayout.NEW_PARAGRAPH);
		panel.add(new JLabel(localization.getFieldLabel("ContactInfoUpdateLater")));

		panel.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		panel.add(ok);
		panel.add(cancel);
		
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

	UiSingleTextField source;
	UiSingleTextField organization;
	UiSingleTextField email;
	UiSingleTextField webpage;
	UiSingleTextField phone;
	UiTextArea address;

	JButton ok;
}
