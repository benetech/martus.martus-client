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

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import org.martus.client.core.CustomFieldError;
import org.martus.client.core.CustomFieldTemplate;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.clientside.Localization;
import org.martus.swing.UiButton;
import org.martus.swing.UiFileChooser;
import org.martus.swing.UiLabel;
import org.martus.swing.UiScrollPane;
import org.martus.swing.UiTextArea;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;


public class UiCustomFieldsDlg extends JDialog
{
	public UiCustomFieldsDlg(UiMainWindow owner, String xmlFieldSpecs)
	{
		super(owner, "", true);
		mainWindow = owner; 
		String baseTag = "CustomFields";
		UiLocalization localization = owner.getLocalization();
		setTitle(localization.getWindowTitle("input" + baseTag));

		UiWrappedTextArea label = new UiWrappedTextArea(localization.getFieldLabel("input" + baseTag + "Info"));
		text = createXMLTextArea(xmlFieldSpecs);
		UiScrollPane textPane = createScrollPane(text);

		JButton defaults = new UiButton(localization.getButtonLabel("customDefault"));
		defaults.addActionListener(new CustomDefaultHandler());
		JButton importTemplate = new UiButton(localization.getButtonLabel("customImport"));
		importTemplate.addActionListener(new ImportTemplateHandler());
		JButton exportTemplate = new UiButton(localization.getButtonLabel("customExport"));
		exportTemplate.addActionListener(new ExportTemplateHandler());

		
		JButton ok = new UiButton(localization.getButtonLabel("input" + baseTag + "ok"));
		ok.addActionListener(new OkHandler());
		JButton cancel = new UiButton(localization.getButtonLabel("cancel"));
		cancel.addActionListener(new CancelHandler());
		JButton help = new UiButton(localization.getButtonLabel("customHelp"));
		help.addActionListener(new CustomHelpHandler());

		Box vBox = Box.createVerticalBox();
		vBox.add(defaults);
		vBox.add(exportTemplate);
		vBox.add(importTemplate);
		
		Box buttons = Box.createHorizontalBox();
		Component buttonsToAdd[] = {vBox, Box.createHorizontalGlue(), ok, cancel, help};  
		Utilities.addComponentsRespectingOrientation(buttons, buttonsToAdd);

		JPanel customFieldsPanel = new JPanel();
		customFieldsPanel.setBorder(new EmptyBorder(10,10,10,10));
		customFieldsPanel.setLayout(new BoxLayout(customFieldsPanel, BoxLayout.Y_AXIS));
		customFieldsPanel.add(label);
		customFieldsPanel.add(new UiLabel(" "));
		customFieldsPanel.add(textPane);
		customFieldsPanel.add(new UiLabel(" "));
		customFieldsPanel.add(buttons);
	
		getContentPane().add(customFieldsPanel);
		getRootPane().setDefaultButton(ok);
		Utilities.centerDlg(this);
		setResizable(true);
	}
	
	public void setFocusToInputField()
	{
		text.requestFocus();
	}

	class OkHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			if(!validateXml(text.getText()))
				return;
			result = text.getText();
			dispose();
		}
	}

	class CancelHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			dispose();
		}
	}
	
	
	class CustomDefaultHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			result = "";
			dispose();
		}
	}
	
	class ImportTemplateHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			File currentDirectory = new File(mainWindow.getApp().getCurrentAccountDirectoryName());
			FileFilter filter = new MCTFileFilter();
			String windowTitle = mainWindow.getLocalization().getWindowTitle("ImportCustomizationTemplateOpen");
			String buttonLabel = mainWindow.getLocalization().getButtonLabel("customImport");
			UiFileChooser.FileDialogResults results = UiFileChooser.displayFileOpenDialog(mainWindow, windowTitle, null, currentDirectory, buttonLabel, filter);
			if (results.wasCancelChoosen())
				return;
			File importFile = results.getFileChoosen();

			CustomFieldTemplate template = new CustomFieldTemplate();
			if(template.importTemplate(importFile))
			{
				text.setText(template.getImportedText());
				mainWindow.notifyDlg("ImportingCustomizationTemplateSuccess");
			}
			else
			{
				displayXMLError(template);
				mainWindow.notifyDlg("ErrorImportingCustomizationTemplate");
			}
		}
	}

	class ExportTemplateHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			String windowTitle = mainWindow.getLocalization().getWindowTitle("ExportCustomizationTemplateSaveAs");
			FileFilter filter = new MCTFileFilter();
			UiFileChooser.FileDialogResults results = UiFileChooser.displayFileSaveDialog(mainWindow, windowTitle, mainWindow.getApp().getCurrentAccountDirectory(), filter);
			
			if (results.wasCancelChoosen())
				return;
			File destFileOriginal = results.getFileChoosen();
			File destFile = null;
			if(destFileOriginal.getName().endsWith(MartusApp.CUSTOMIZATION_TEMPLATE_EXTENSION))
				destFile = destFileOriginal;
			else
				destFile = new File(destFileOriginal.getAbsolutePath() + MartusApp.CUSTOMIZATION_TEMPLATE_EXTENSION);
			if(destFile.exists())
				if(!mainWindow.confirmDlg("OverWriteExistingFile"))
					return;
			CustomFieldTemplate template = new CustomFieldTemplate();
			if(template.ExportTemplate(destFile, text.getText()))
			{
				mainWindow.notifyDlg("ExportingCustomizationTemplateSuccess");
			}
			else
			{
				displayXMLError(template);
				mainWindow.notifyDlg("ErrorExportingCustomizationTemplate");
			}
		}
	}
	
	class MCTFileFilter extends FileFilter
	{
		public boolean accept(File pathname)
		{
			if(pathname.isDirectory())
				return true;
			return(pathname.getName().endsWith(MartusApp.CUSTOMIZATION_TEMPLATE_EXTENSION));
		}

		public String getDescription()
		{
			return mainWindow.getLocalization().getFieldLabel("CustomizationTemplateFiles");
		}
	}

	class CustomHelpHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			UiLocalization localization = mainWindow.getLocalization();
			String message = localization.getFieldLabel("CreateCustomFieldsHelp1");
			message += localization.getFieldLabel("CreateCustomFieldsHelp2");
			String examples = localization.getFieldLabel("CreateCustomFieldsHelp3");
			UiTextArea xmlExamples = createXMLTextArea(examples);
			xmlExamples.setCaretPosition(0);
			xmlExamples.setBackground(new JFrame().getBackground());
			
			xmlExamples.setEditable(false);
			UiScrollPane pane = createScrollPane(xmlExamples);

			new UiShowScrollableTextDlg(mainWindow, "CreateCustomFieldsHelp", "ok", Localization.UNUSED_TAG, Localization.UNUSED_TAG, message, pane);
		}
	}
	
	public boolean validateXml(String xmlToValidate)
	{
		CustomFieldTemplate template = new CustomFieldTemplate();
		if(template.validateXml(xmlToValidate))
			return true;

		displayXMLError(template); 
		return false;
	}

	void displayXMLError(CustomFieldTemplate template)
	{
		Vector errors = template.getErrors();
		if(errors == null)
			return;
		String header1 = mainWindow.getLocalization().getFieldLabel("ErrorCustomFieldHeader1");
		String header2 = mainWindow.getLocalization().getFieldLabel("ErrorCustomFieldHeader2");
		String header3 = mainWindow.getLocalization().getFieldLabel("ErrorCustomFieldHeader3");
		String header4 = mainWindow.getLocalization().getFieldLabel("ErrorCustomFieldHeader4");
		String errorMessage = GetDataAndSpacing(header1, HEADER_SPACING_1);
		errorMessage += GetDataAndSpacing(header2, HEADER_SPACING_2);
		errorMessage += GetDataAndSpacing(header3, HEADER_SPACING_3);
		errorMessage += header4;
		errorMessage += '\n';
		for(int i = 0; i<errors.size(); ++i)
		{
			CustomFieldError thisError = (CustomFieldError)errors.get(i);
			String thisErrorMessage = GetDataAndSpacing(thisError.getCode(), HEADER_SPACING_1);
			thisErrorMessage += GetDataAndSpacing(thisError.getType(), HEADER_SPACING_2);
			thisErrorMessage += GetDataAndSpacing(thisError.getTag(), HEADER_SPACING_3);
			thisErrorMessage += thisError.getLabel();
			errorMessage += thisErrorMessage;
			errorMessage += '\n';
		}
		new UiShowScrollableTextDlg(mainWindow,"ErrorCustomFields", "ok", Localization.UNUSED_TAG, "ErrorCustomFields", errorMessage, null);
	}
		
	private String GetDataAndSpacing(String data, int columnSpacing)
	{
		String columnData = data;
		for(int i = data.length(); i < columnSpacing; ++i)
		{
			columnData += " ";
		}
		columnData += " ";
		return columnData;
	}

	UiTextArea createXMLTextArea(String text)
	{
		UiTextArea msgArea = new UiTextArea(20, 80);
		msgArea.setText(text);
		msgArea.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		msgArea.setLineWrap(true);
		msgArea.setWrapStyleWord(true);
		return msgArea;
	}

	UiScrollPane createScrollPane(UiTextArea textArea)
	{
		UiScrollPane textPane = new UiScrollPane(textArea, UiScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				UiScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		textPane.getVerticalScrollBar().setFocusable(false);
		return textPane;
	}

	public String getResult()
	{
		return result;
	}

	UiTextArea text;
	String result = null;
	UiMainWindow mainWindow;

	private int HEADER_SPACING_1 = 6;
	private int HEADER_SPACING_2 = 11;
	private int HEADER_SPACING_3 = 14;
	
}
