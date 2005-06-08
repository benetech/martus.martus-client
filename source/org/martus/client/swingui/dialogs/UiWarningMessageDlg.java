/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005, Beneficent
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.NotSerializableException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.martus.swing.UiButton;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;


public class UiWarningMessageDlg extends JDialog implements ActionListener
{
	public UiWarningMessageDlg(JFrame owner, String title, String warningMessageLtoR, String warningMessageRtoL)
	{
		super(owner, title, true);

		JButton okButton = new UiButton("OK");
		okButton.addActionListener(this);
		
		UiWrappedTextArea areaLtoR = new UiWrappedTextArea(warningMessageLtoR);
		areaLtoR.setRows(areaLtoR.getRows()-5);//TODO:Remove this once we fix the UiWrappedTextArea to get the correct row count.
		areaLtoR.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		areaLtoR.setBorder(new EmptyBorder(5,5,5,5));
		JPanel ltorPanel = new JPanel();
		ltorPanel.setBorder(new LineBorder(Color.BLACK));
		ltorPanel.add(areaLtoR);
		
		UiWrappedTextArea areaRtoL = new UiWrappedTextArea(warningMessageRtoL);
		areaRtoL.setBorder(new EmptyBorder(5,5,5,5));
		JPanel rtolPanel = new JPanel();
		rtolPanel.setBorder(new LineBorder(Color.BLACK));
		rtolPanel.add(areaRtoL);
		areaRtoL.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		Box vbox = Box.createVerticalBox();
		
		vbox.add(ltorPanel);
		vbox.add(rtolPanel);
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5,5,5,5));
		panel.add(vbox);
		getContentPane().add(new UiScrollPane(panel), BorderLayout.CENTER);
		
		JPanel pb = new JPanel();
		pb.add(okButton);
		getContentPane().add(pb, BorderLayout.SOUTH);

		Utilities.centerDlg(this);
		setResizable(true);
		getRootPane().setDefaultButton(okButton);
		okButton.requestFocus(true);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e)
	{
		dispose();
	}

	// This class is NOT intended to be serialized!!!
	private static final long serialVersionUID = 1;
	private void writeObject(java.io.ObjectOutputStream stream) throws IOException
	{
		throw new NotSerializableException();
	}

}
