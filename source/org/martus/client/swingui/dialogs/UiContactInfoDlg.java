/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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
import org.martus.clientside.Burmese;
import org.martus.clientside.UiLocalization;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiParagraphPanel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiTextArea;
import org.martus.swing.UiTextField;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;

public class UiContactInfoDlg extends JDialog implements ActionListener
{
	public UiContactInfoDlg(UiMainWindow mainWindow, ConfigInfo infoToUse)
	{
		super(mainWindow.getCurrentActiveFrame(), "", true);
		info = infoToUse;

		UiLocalization localization = mainWindow.getLocalization();
	
		setTitle(localization.getWindowTitle("setupcontact"));
		ok = new UiButton(localization.getButtonLabel("ok"));
		ok.addActionListener(this);
		JButton cancel = new UiButton(localization.getButtonLabel("cancel"));
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

		setInitialValues();

		UiParagraphPanel panel = new UiParagraphPanel();

		UiWrappedTextArea infoRequired = new UiWrappedTextArea(localization.getFieldLabel("ContactInfoRequiredFields"), 60);
		JLabel space = new UiLabel(" ");
		infoRequired.setFont(space.getFont());
		infoRequired.setRows(2);
		panel.addOnNewLine(infoRequired);

		String authorPrompt = localization.getFieldLabel("AuthorRequired");
		panel.addComponents(new UiLabel(authorPrompt), source);

		String organizationPrompt = localization.getFieldLabel("OrganizationRequired");
		panel.addComponents(new UiLabel(organizationPrompt), organization);
		panel.addComponents(new UiLabel(localization.getFieldLabel("email")), email);
		panel.addComponents(new UiLabel(localization.getFieldLabel("webpage")), webpage);
		panel.addComponents(new UiLabel(localization.getFieldLabel("phone")), phone);
		panel.addComponents(new UiLabel(localization.getFieldLabel("address")), addressScrollPane);
		
		StringBuffer helpmsg = new StringBuffer();
		helpmsg.append(localization.getFieldLabel("ContactInfoDescriptionOfFields"));
		helpmsg.append('\n');
		helpmsg.append(localization.getFieldLabel("ContactInfoFutureUseOfFields"));
		helpmsg.append('\n');
		helpmsg.append(localization.getFieldLabel("ContactInfoUpdateLater"));
		UiWrappedTextArea infoFuture = new UiWrappedTextArea(helpmsg.toString(), 30);
		panel.addOnNewLine(infoFuture);

		panel.addComponents(ok, cancel);
		UiScrollPane scroller = new UiScrollPane(panel);
		getContentPane().add(scroller);
		getRootPane().setDefaultButton(ok);
		Utilities.centerDlg(this);
		setVisible(true);
		toFront();
	}

	private void setInitialValues()
	{
		if (info.getUseZawgyi())
		{
		 	source.setText(Burmese.getDisplayable(info.getAuthor()));
			organization.setText(Burmese.getDisplayable(info.getOrganization()));
			email.setText(Burmese.getDisplayable(info.getEmail()));
			webpage.setText(Burmese.getDisplayable(info.getWebPage()));
			phone.setText(Burmese.getDisplayable(info.getPhone()));
			address.setText(Burmese.getDisplayable(info.getAddress()));
		} else
		{
			source.setText(info.getAuthor());
			organization.setText(info.getOrganization());
			email.setText(info.getEmail());
			webpage.setText(info.getWebPage());
			phone.setText(info.getPhone());
			address.setText(info.getAddress());
		}
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
			if (info.getUseZawgyi())
			{
				info.setAuthor(Burmese.getStorable(source.getText()));
				info.setOrganization(Burmese.getStorable(organization.getText()));
				info.setEmail(Burmese.getStorable(email.getText()));
				info.setWebPage(Burmese.getStorable(webpage.getText()));
				info.setPhone(Burmese.getStorable(phone.getText()));
				info.setAddress(Burmese.getStorable(address.getText()));
			}   else
			{
				info.setAuthor(source.getText());
				info.setOrganization(organization.getText());
				info.setEmail(email.getText());
				info.setWebPage(webpage.getText());
				info.setPhone(phone.getText());
				info.setAddress(address.getText());
			}
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
