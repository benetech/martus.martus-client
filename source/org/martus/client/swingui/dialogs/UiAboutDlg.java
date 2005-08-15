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

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.martus.client.swingui.UiConstants;
import org.martus.client.swingui.UiMainWindow;
import org.martus.clientside.UiLocalization;
import org.martus.common.VersionBuildDate;
import org.martus.swing.UiButton;
import org.martus.swing.UiLabel;
import org.martus.swing.UiVBox;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;

public class UiAboutDlg extends JDialog implements ActionListener
{
	public UiAboutDlg(UiMainWindow owner)
		throws HeadlessException
	{
		super(owner, "" , true);
//		System.out.println("Number of calls to verifyPacketSignature " + Packet.callsToVerifyPacketSignature);
//		System.out.println("Cumulative time in verifyPacketSignature " + Packet.millisInVerifyPacketSignature);
//		System.out.println("Number of calls to XmlPacketLoader " + XmlPacketLoader.callsToXmlPacketLoader);
//		System.out.println("Cumulative time in XmlPacketLoader " + XmlPacketLoader.millisInXmlPacketLoader);

		
		UiLocalization localization = owner.getLocalization();
		
		setTitle(localization.getWindowTitle("about"));

		JLabel icon = new JLabel(new ImageIcon(UiAboutDlg.class.getResource("MartusLogo.gif")),JLabel.LEFT);

		String versionInfo = UiConstants.programName;
		versionInfo += " " + localization.getFieldLabel("aboutDlgVersionInfo");
		versionInfo += " " + UiConstants.versionLabel;
		
		String mtfVersionInfo = localization.getFieldLabel("aboutDlgTranslationVersionInfo");
		mtfVersionInfo += " " + localization.getFieldLabel("translationVersion");
		if(!localization.isCurrentTranslationOfficial())
			mtfVersionInfo +="X";

		String buildDate = localization.getFieldLabel("aboutDlgBuildDate");
		buildDate += " " + VersionBuildDate.getVersionBuildDate();

		JButton ok = new UiButton(localization.getButtonLabel("ok"));
		ok.addActionListener(this);
		ok.addKeyListener(new MakeEnterKeyExit());

		Box vBoxVersionInfo = new UiVBox();
		vBoxVersionInfo.add(new UiLabel(versionInfo));
		vBoxVersionInfo.add(new UiLabel(mtfVersionInfo));
		vBoxVersionInfo.add(new UiLabel(UiConstants.copyright));
		vBoxVersionInfo.add(new UiLabel(UiConstants.website));
		vBoxVersionInfo.add(new UiLabel(buildDate));

		Box hBoxVersionAndIcon = Box.createHorizontalBox();
		hBoxVersionAndIcon.add(Box.createHorizontalGlue());
		hBoxVersionAndIcon.add(vBoxVersionInfo);
		hBoxVersionAndIcon.add(Box.createHorizontalGlue());
		hBoxVersionAndIcon.add(icon);

		Box hBoxOk = Box.createHorizontalBox();
		hBoxOk.add(Box.createHorizontalGlue());
		hBoxOk.add(ok);
		hBoxOk.add(Box.createHorizontalGlue());

		final String disclaimer = localization.getFieldLabel("aboutDlgDisclaimer");
		final String credits = localization.getFieldLabel("aboutDlgCredits");
		final String notice = "\n" + disclaimer + "\n\n" + credits + "\n\n" + APACHENOTICE;

		UiVBox vBoxDetails = new UiVBox();
		vBoxDetails.addCentered(new UiWrappedTextArea(notice));
		vBoxDetails.addCentered(hBoxOk);

		Box hBoxDetails = Box.createHorizontalBox();
		hBoxDetails.add(vBoxDetails);

		UiVBox vBoxAboutDialog = new UiVBox();
		vBoxAboutDialog.addCentered(hBoxVersionAndIcon);
		vBoxAboutDialog.addCentered(hBoxDetails);
		getContentPane().add(vBoxAboutDialog);

		Utilities.centerDlg(this);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent ae)
	{
		dispose();
	}

	public class MakeEnterKeyExit extends KeyAdapter
	{
		public void keyPressed(KeyEvent ke)
		{
			if (ke.getKeyCode() == KeyEvent.VK_ENTER)
				dispose();
		}
	}

	final String APACHENOTICE = "This product includes software developed by the Apache Software Foundation (http://www.apache.org/).";
}
