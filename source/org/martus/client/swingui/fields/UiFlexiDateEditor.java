/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2006, Beneficent
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.martus.clientside.UiLocalization;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.swing.UiLabel;
import org.martus.swing.UiParagraphPanel;
import org.martus.swing.UiRadioButton;
import org.martus.swing.Utilities;
import org.martus.util.MultiCalendar;

public class UiFlexiDateEditor extends UiField
{
	public UiFlexiDateEditor(UiLocalization localizationToUse, FieldSpec specToUse)
	{
		localization = localizationToUse;
		spec = specToUse;
		init();
	}	
	
	private void init()
	{
		component = new JPanel();
		component.setLayout(new BorderLayout());

		exactDateRB = new UiRadioButton(localization.getFieldLabel("DateExact"), true);			
		exactDateRB.addItemListener(new RadioItemListener());
		
		flexiDateRB = new UiRadioButton(localization.getFieldLabel("DateRange"));		
		flexiDateRB.addItemListener(new RadioItemListener());

		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(exactDateRB);
		radioGroup.add(flexiDateRB);		

		UiParagraphPanel dateButtons = new UiParagraphPanel();				
		dateButtons.addComponents(exactDateRB, flexiDateRB);
		Box dateSelection = Box.createHorizontalBox();
		Utilities.addComponentsRespectingOrientation(dateSelection, new Component[] {dateButtons, Box.createHorizontalGlue()});
		component.add(dateSelection, BorderLayout.NORTH);
		
		buildBeginDateBox();
		buildEndDateBox();

		component.add(buildExactDateBox(), BorderLayout.CENTER);
	}
	
	private JComponent buildFlexiDateBox()
	{	
		flexiDateBox = Box.createHorizontalBox();
		Component[] items = new Component[] {createSpaceSeperator(), getBeginDateBox(), new UiLabel("  -  "), getEndDateBox(), createSpaceSeperator()};
		Utilities.addComponentsRespectingOrientation(flexiDateBox, items);
		return flexiDateBox;
	}

	private UiLabel createSpaceSeperator()
	{
		return new UiLabel(" ");
	}
	
	private JComponent buildExactDateBox()
	{		
		exactDateBox = Box.createHorizontalBox();
		Component[] items = new Component[] {createSpaceSeperator(), getBeginDateBox()};
		Utilities.addComponentsRespectingOrientation(exactDateBox, items);
		return exactDateBox;			
	}
				
	private void buildBeginDateBox()
	{				
		bgDateBox = new UiDateEditorComponent(localization, isCustomDate());								
	}
	
	private JComponent getBeginDateBox()
	{	
		return bgDateBox;											
	}

	private void buildEndDateBox()
	{		
		endDateBox = new UiDateEditorComponent(localization, isCustomDate());								
	}
		
	private JComponent getEndDateBox()
	{
		return endDateBox;		
	}

	protected boolean isCustomDate()
	{
		return StandardFieldSpecs.isCustomFieldTag(spec.getTag());
	}
		
	public JComponent getComponent()
	{
		return component;
	}

	public JComponent[] getFocusableComponents()
	{
		return (flexiDateRB.isSelected())? loadFlexidatePanelComponents():loadExactDatePanelComponents();				
	}
	
	protected JComponent[] loadFlexidatePanelComponents()
	{
		JComponent[] beginDate = bgDateBox.getFocusableComponents();
		JComponent[] endDate = endDateBox.getFocusableComponents();
		return new JComponent[]{exactDateRB, flexiDateRB, 
					beginDate[0], beginDate[1], beginDate[2],
					endDate[0], endDate[1], endDate[2],};
	}
	
	private JComponent[] loadExactDatePanelComponents()
	{
		JComponent[] mdy = bgDateBox.getFocusableComponents();
		return new JComponent[]{exactDateRB, flexiDateRB, mdy[0], mdy[1], mdy[2],};
	}

	final class RadioItemListener implements ItemListener
	{
		public void itemStateChanged(ItemEvent e)
		{
			if (isFlexiDate())														
				removeExactDatePanel();									
			
			if (isExactDate())				
				removeFlexidatePanel();					
		}
	}
	
	void removeExactDatePanel()
	{
		component.remove(exactDateBox);	
		component.add(buildFlexiDateBox(), BorderLayout.CENTER);																	
		component.revalidate();		
	}
	
	void removeFlexidatePanel()
	{
		component.remove(flexiDateBox);						
		component.add(buildExactDateBox(), BorderLayout.CENTER);
		component.revalidate();		
	}

	public void validate() throws UiField.DataInvalidException 
	{
		
		if(isFlexiDate())
		{
			if(getEndDate().before(getBeginDate()))
			{
				bgDateBox.requestFocus();
				throw new DateRangeInvertedException();
			}
		}
		
		if(isCustomDate())
			return;		
		
		MultiCalendar today = new MultiCalendar();
		if (getBeginDate().after(today))
		{
			bgDateBox.requestFocus();	
			throw new UiDateEditor.DateFutureException();
		}
		if (isFlexiDate())
		{		
			if (getEndDate().after(today))
			{
				bgDateBox.requestFocus();	
				throw new UiDateEditor.DateFutureException();				
			}
		}
	}
	
	protected boolean isFlexiDate()
	{
		return flexiDateRB.isSelected();
	}
	
	protected boolean isExactDate()
	{
		return exactDateRB.isSelected();
	}

	public String getText()
	{
		final MultiCalendar beginDate = getBeginDate();
		if(isExactDate())
			return MartusFlexidate.toStoredDateFormat(beginDate);
		
		MultiCalendar endDate = getEndDate();
		if(endDate.getGregorianYear() == MultiCalendar.YEAR_NOT_SPECIFIED)
			return MartusFlexidate.toStoredDateFormat(beginDate);
		
		return MartusFlexidate.toBulletinFlexidateFormat(beginDate, endDate);
	}

	private MultiCalendar getBeginDate() 
	{		
		return bgDateBox.getDate();
	}
	
	private MultiCalendar getEndDate() 
	{				
		return endDateBox.getDate();
	}	
		
	public void setText(String newText)
	{		
		MartusFlexidate mfd = localization.createFlexidateFromStoredData(newText);
		bgDateBox.setDate(mfd.getBeginDate());
		endDateBox.setDate(mfd.getEndDate());
			
		if (mfd.hasDateRange())
			flexiDateRB.setSelected(true);
	}
	
	public static class DateRangeInvertedException extends UiField.DataInvalidException
	{
		public DateRangeInvertedException()
		{
			super(null);
		}
		
		public DateRangeInvertedException(String tag)
		{
			super(tag);
		}

	}
	
	JComponent 					component;
			
	protected UiLocalization localization;	
	private UiRadioButton 		exactDateRB;
	private UiRadioButton 		flexiDateRB;
	protected Box			 	flexiDateBox;
	private Box				 	exactDateBox;
	UiDateEditorComponent bgDateBox;
	UiDateEditorComponent endDateBox;
	private FieldSpec			spec;
	
}
