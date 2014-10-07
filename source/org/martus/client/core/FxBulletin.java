/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
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
package org.martus.client.core;

import java.util.HashMap;
import java.util.Vector;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.martus.client.swingui.jfx.generic.data.ObservableChoiceItemList;
import org.martus.client.swingui.jfx.landing.bulletins.AttachmentTableRowData;
import org.martus.client.swingui.jfx.landing.bulletins.GridRowData;
import org.martus.common.FieldSpecCollection;
import org.martus.common.GridData;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MartusLogger;
import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.ReusableChoices;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DataInvalidException;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.UniversalId;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.util.MultiCalendar;

/**
 * This class wraps a Bulletin object, exposing each data member as a 
 * Property, to make life easier when working in JavaFX.
 */
public class FxBulletin
{
	public FxBulletin(MiniLocalization localizationToUse)
	{
		localization = localizationToUse;
		
		fieldProperties = new HashMap<String, SimpleStringProperty>();
		fieldValidators = new HashMap<String, FieldValidator>();
		attachments = FXCollections.observableArrayList();
		hasBeenValidatedProperty = new SimpleBooleanProperty();
	}

	public void copyDataFromBulletin(Bulletin b, ReadableDatabase db) throws Exception
	{
		clear();
		
		universalIdProperty = new ReadOnlyObjectWrapper<UniversalId>(b.getUniversalId());
		versionProperty = new SimpleIntegerProperty(b.getVersion());

		immutableOnServer = new SimpleBooleanProperty(b.getImmutableOnServer());

		HeadquartersKeys hqKeys = b.getAuthorizedToReadKeysIncludingPending();
		authorizedToReadKeys = FXCollections.observableArrayList();
		for(int i = 0; i < hqKeys.size(); ++i)
		{
			authorizedToReadKeys.add(hqKeys.get(i));
		}
		
		bulletinHistory = new ReadOnlyObjectWrapper<BulletinHistory>(b.getHistory());
		bulletinLocalId = new SimpleStringProperty(b.getBulletinHeaderPacket().getLocalId());

		setFieldPropertiesFromBulletinSection(b, b.getTopSectionFieldSpecs());
		setFieldPropertiesFromBulletinSection(b, b.getBottomSectionFieldSpecs());
		
		copyReusableChoiceListsFromBulletinSection(b.getTopSectionFieldSpecs());
		copyReusableChoiceListsFromBulletinSection(b.getBottomSectionFieldSpecs());

		addAttachmentProxies(b.getPrivateAttachments(), db);
		addAttachmentProxies(b.getPublicAttachments(), db);
	}
	
	private void addAttachmentProxies(AttachmentProxy[] attachmentsToAdd, ReadableDatabase db)
	{
		for (AttachmentProxy attachmentProxy : attachmentsToAdd)
		{
			AttachmentTableRowData attachmentRow = new AttachmentTableRowData(attachmentProxy, db);
			attachments.add(attachmentRow);
		}
	}

	public void copyDataToBulletin(Bulletin modified) throws Exception
	{
		modified.setImmutableOnServer(immutableOnServer.get());
		
		modified.getFieldDataPacket().setFieldSpecs(fieldSpecs);
		modified.getPrivateFieldDataPacket().setFieldSpecs(new FieldSpecCollection());
		
		for(int i = 0; i < fieldSpecs.size(); ++i)
		{
			FieldSpec fieldSpec = fieldSpecs.get(i);
			String fieldTag = fieldSpec.getTag();
			String value = fieldProperty(fieldTag).getValue();
			modified.set(fieldTag, value);
//			System.out.println("copyDataToBulletin " + fieldTag + ":" + value);
		}
		HeadquartersKeys modifiedKeys = new HeadquartersKeys();
		authorizedToReadKeys.forEach(key -> modifiedKeys.add(key));
		modified.clearAuthorizedToReadKeys();
		modified.setAuthorizedToReadKeys(modifiedKeys);

		modified.clearPublicAttachments();
		modified.clearPrivateAttachments();

		for(int i = 0; i < attachments.size(); ++i)
		{
			AttachmentProxy proxy = attachments.get(i).getAttachmentProxy();
			modified.addPrivateAttachment(proxy);
		}
	}

	public ReadOnlyObjectWrapper<UniversalId> universalIdProperty()
	{
		return universalIdProperty;
	}
	
	public ReadOnlyIntegerProperty versionProperty()
	{
		return versionProperty;
	}

	public ObservableList<HeadquartersKey> getAuthorizedToReadList()
	{
		return authorizedToReadKeys;
	}
	
	public ReadOnlyObjectWrapper<BulletinHistory> getHistory()
	{
		return bulletinHistory;
	}

	public ReadOnlyStringProperty bulletinLocalIdProperty()
	{
		return bulletinLocalId;
	}

	public ReadOnlyBooleanProperty hasBeenValidatedProperty()
	{
		return hasBeenValidatedProperty;
	}

	public Vector<FieldSpec> getFieldSpecs()
	{
		Vector<FieldSpec> specs = new Vector<FieldSpec>();
		for(int i = 0; i < fieldSpecs.size(); ++i)
		{
			FieldSpec fieldSpec = fieldSpecs.get(i);
			specs.add(fieldSpec);
		}
		
		return specs;
	}

	public SimpleStringProperty fieldProperty(String fieldTag)
	{
		FieldSpec foundSpec = fieldSpecs.findBytag(fieldTag);
		if(foundSpec == null)
			throw new NullPointerException("No such field: " + fieldTag);

		if(foundSpec.getType().isGrid())
			throw new NullPointerException("fieldProperty not available for a grid: " + fieldTag);
		
		return fieldProperties.get(fieldTag);
	}
	
	public ObservableBooleanValue isValidProperty(String fieldTag)
	{
		FieldSpec foundSpec = fieldSpecs.findBytag(fieldTag);
		if(foundSpec == null)
			throw new NullPointerException("No such field: " + fieldTag);

		if(foundSpec.getType().isGrid())
			throw new NullPointerException("fieldProperty not available for a grid: " + fieldTag);

		return fieldValidators.get(fieldTag).isValidProperty();
	}
	
	public ObservableList<GridRowData> gridDataProperty(String tag)
	{
		return dataForGrids.get(tag);
	}

	public BooleanProperty getImmutableOnServerProperty()
	{
		return immutableOnServer;
	}
	
	public Vector<ObservableChoiceItemList> getChoiceItemLists(String fieldTag) throws Exception
	{
		FieldSpec spec = fieldSpecs.findBytag(fieldTag);
		if(spec == null)
			throw new NullPointerException("No such field: " + fieldTag);
		
		if(!spec.getType().isDropdown())
			throw new Exception("Field is not a dropdown: " + fieldTag);
		
		DropDownFieldSpec dropDownSpec = (DropDownFieldSpec) spec;
		boolean isDataDriven = (dropDownSpec.getDataSource() != null);
		boolean isReusable = (dropDownSpec.getReusableChoicesCodes().length > 0);
		if(isDataDriven)
			return new Vector<ObservableChoiceItemList>();
		
		if(isReusable)
			return getReusableChoiceItemLists(dropDownSpec);
		
		return getSimpleChoiceItemLists(dropDownSpec);
	}

	private Vector<ObservableChoiceItemList> getReusableChoiceItemLists(DropDownFieldSpec dropDownSpec)
	{
		Vector<ObservableChoiceItemList> listOfLists = new Vector<ObservableChoiceItemList>();

		String[] reusableChoicesCodes = dropDownSpec.getReusableChoicesCodes();

		for(int i = 0; i < reusableChoicesCodes.length; ++i)
		{
			String onlyReusableChoicesCode = reusableChoicesCodes[i];
			ReusableChoices reusableChoices = fieldSpecs.getReusableChoices(onlyReusableChoicesCode);
			ChoiceItem[] choiceItems = reusableChoices.getChoices();
			ObservableChoiceItemList list = new ObservableChoiceItemList();
			ChoiceItem emptyItemAtTheStartOfEveryReusableList = new ChoiceItem("", "");
			list.add(emptyItemAtTheStartOfEveryReusableList);
			list.addAll(choiceItems);
			
			listOfLists.add(list);
		}
		return listOfLists;
	}

	public Vector<ObservableChoiceItemList> getSimpleChoiceItemLists(DropDownFieldSpec dropDownSpec)
	{
		Vector<ObservableChoiceItemList> listOfLists = new Vector<ObservableChoiceItemList>();
		ObservableChoiceItemList simpleChoices = new ObservableChoiceItemList();
		simpleChoices.addAll(dropDownSpec.getAllChoices());
		listOfLists.add(simpleChoices);
		return listOfLists;
	}

	public ObservableList<AttachmentTableRowData> getAttachments()
	{
		return attachments;
	}
	
	public boolean hasBeenModified()
	{
		return hasBeenModified;
	}

	public void validateData() throws DataInvalidException
	{
		hasBeenValidatedProperty.setValue(true);

		for(int i = 0; i < fieldSpecs.size(); ++i)
		{
			FieldSpec spec = fieldSpecs.get(i);
			String value = fieldProperty(spec.getTag()).getValue();
			validateField(spec, value, getLocalization());
		}
	}

	protected static void validateField(FieldSpec spec, String value, MiniLocalization localization) throws DataInvalidException
	{
		String label = ZawgyiLabelUtilities.getDisplayableLabel(spec, localization);
		validateField(spec, label, value, localization);
	}

	private static void validateField(FieldSpec spec, String displayableLabel, String fieldDataValue, MiniLocalization localization) throws DataInvalidException
	{
		FieldType type = spec.getType();
		if(type.isGrid())
		{
			MartusLogger.logError("******* Validation not handled yet for " + type.getTypeName());
			return;
		}
		spec.validate(displayableLabel, fieldDataValue, localization);
	}

	private void clear()
	{
		hasBeenModified = false;
		hasBeenValidatedProperty.setValue(false);
		
		if(universalIdProperty != null)
		{
			universalIdProperty.setValue(null);
			universalIdProperty = null;
		}
		
		if(bulletinLocalId != null)
			bulletinLocalId = null;
		
		if(bulletinHistory != null)
		{
			bulletinHistory.setValue(null);
			bulletinHistory = null;
		}

		if(authorizedToReadKeys != null)
			authorizedToReadKeys.clear();

		if(versionProperty != null)
			versionProperty = null;
		
		fieldProperties.forEach((key, property) -> clearProperty(property));
		fieldProperties.clear();
		
		dataForGrids = new HashMap();
		
		fieldSpecs = new FieldSpecCollection();
		
		if(attachments != null)
			attachments.clear();

	}

	public void clearProperty(SimpleStringProperty property)
	{
		property.setValue(null);
	}
	
	private void setFieldPropertiesFromBulletinSection(Bulletin b, FieldSpecCollection bulletinFieldSpecs) throws Exception
	{
		for(int i = 0; i < bulletinFieldSpecs.size(); ++i)
		{
			FieldSpec fieldSpec = bulletinFieldSpecs.get(i);
			fieldSpecs.add(fieldSpec);
			String fieldTag = fieldSpec.getTag();
			MartusField field = b.getField(fieldTag);
			String value = getFieldValue(field);
			if(fieldSpec.getType().isGrid())
				setGridField((GridFieldSpec)fieldSpec, value, bulletinFieldSpecs.getAllReusableChoiceLists());
			else
				setField(fieldSpec, value);
//			System.out.println("copyDataFromBulletin " + fieldTag + ":" + value);
		}
	}

	private void setGridField(GridFieldSpec gridSpec, String xmlGridData, PoolOfReusableChoicesLists poolOfReusableChoicesLists) throws Exception
	{
		GridData data = new GridData(gridSpec, poolOfReusableChoicesLists);
		data.setFromXml(xmlGridData);
		
		GridFieldData gridFieldData = new GridFieldData();
		for(int row = 0; row < data.getRowCount(); ++row)
		{
			GridRowData rowData = new GridRowData();
			for(int column = 0; column < data.getColumnCount(); ++column)
			{
				String columnLabel = gridSpec.getColumnLabel(column);
				String value = data.getValueAt(row, column);
				rowData.put(columnLabel, value);
			}
			
			gridFieldData.add(rowData);
		}
		
		dataForGrids.put(gridSpec.getTag(), gridFieldData);
	}

	private String getFieldValue(MartusField field)
	{
		if(field.getType().isDate())
			return getDateFieldValue(field);
		else if(field.getType().isDateRange())
			return getDateRangeFieldValue(field);
		
		return field.getData();
	}

	private String getDateFieldValue(MartusField field)
	{
		String data = field.getData();
		if(data.isEmpty())
			return data;
		
		MultiCalendar multiCalendar = MultiCalendar.createFromIsoDateString(data);
		if(multiCalendar.isUnknown())
			return "";
		
		return data;
	}

	private String getDateRangeFieldValue(MartusField field)
	{
		String data = field.getData();
		if(!MartusFlexidate.isFlexidateString(data))
			return getDateFieldValue(field);
		
		String isoBaseDate = MartusFlexidate.extractIsoDateFromStoredDate(data);
		int numberOfDays = MartusFlexidate.extractRangeFromStoredDate(data);
		
		MartusFlexidate flexidate = new MartusFlexidate(isoBaseDate, numberOfDays);
		MultiCalendar begin = flexidate.getBeginDate();
		MultiCalendar end = flexidate.getEndDate();
		
		if(begin.isUnknown())
			begin = end;
		if(end.isUnknown())
			end = begin;
		
		if(begin.isUnknown())
			return "";
		
		return data;
	}

	private void setField(FieldSpec spec, String value)
	{
		String tag = spec.getTag();

		SimpleStringProperty property = new SimpleStringProperty(value);
		fieldProperties.put(tag, property);
		property.addListener((observable, newValue, oldValue) -> hasBeenModified = true);

		FieldValidator fieldValidator = new FieldValidator(spec, getLocalization());
		fieldValidators.put(tag, fieldValidator);
		fieldValidator.updateStatus(value);
		property.addListener(fieldValidator);
	}
	
	private void copyReusableChoiceListsFromBulletinSection(FieldSpecCollection bulletinFieldSpecs)
	{
		fieldSpecs.addAllReusableChoicesLists(bulletinFieldSpecs.getAllReusableChoiceLists());
	}
	
	static class FieldValidator implements ChangeListener<String>
	{
		public FieldValidator(FieldSpec specToUse, MiniLocalization localizationToUse)
		{
			spec = specToUse;
			localization = localizationToUse;
			isValidProperty = new SimpleBooleanProperty(); 
		}
		
		public ObservableBooleanValue isValidProperty()
		{
			return isValidProperty;
		}
		
		@Override
		public void changed(ObservableValue<? extends String> property, String oldValue, String newValue)
		{
			updateStatus(newValue);
		}

		public void updateStatus(String newValue)
		{
			boolean isValid = false;
			try
			{
				if(newValue == null)
					newValue = "";
				FxBulletin.validateField(spec, newValue, localization);
				isValid = true;
			} 
			catch (DataInvalidException noNeedToLogOrThrow)
			{
				isValid = false;
			}
			
			isValidProperty.setValue(isValid);
		}

		private FieldSpec spec;
		private MiniLocalization localization;
		private SimpleBooleanProperty isValidProperty;
	}
	
	
	public MiniLocalization getLocalization()
	{
		return localization;
	}

	private MiniLocalization localization;
	private boolean hasBeenModified;
	private SimpleBooleanProperty hasBeenValidatedProperty;
	
	private HashMap<String, SimpleStringProperty> fieldProperties;
	private HashMap<String, FieldValidator> fieldValidators;
	private HashMap<String, GridFieldData> dataForGrids;
	
	private ReadOnlyObjectWrapper<UniversalId> universalIdProperty;
	private ReadOnlyIntegerProperty versionProperty;
	private ReadOnlyStringProperty bulletinLocalId;

	private FieldSpecCollection fieldSpecs;
	private ObservableList<HeadquartersKey> authorizedToReadKeys;
	private ReadOnlyObjectWrapper<BulletinHistory> bulletinHistory;

	private BooleanProperty immutableOnServer;
	private ObservableList<AttachmentTableRowData> attachments;
}
