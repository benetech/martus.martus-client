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

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.martus.common.FieldSpecCollection;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MartusLogger;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.fieldspec.DataInvalidException;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldType;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.UniversalId;

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
	}

	public void copyDataFromBulletin(Bulletin b)
	{
		clear();
		
		universalIdProperty = new ReadOnlyObjectWrapper<UniversalId>(b.getUniversalId());
		versionProperty = new SimpleIntegerProperty(b.getVersion());
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
	}

	public void copyDataToBulletin(Bulletin modified) throws Exception
	{
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

	public SimpleStringProperty fieldProperty(String tag)
	{
		return fieldProperties.get(tag);
	}
	
	public Property<Boolean> isValidProperty(String tag)
	{
		return fieldValidators.get(tag).isValidProperty();
	}

	public boolean hasBeenModified()
	{
		return hasBeenModified;
	}

	public void validateData() throws DataInvalidException
	{
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
		if(type.isGrid() || type.isDate() || type.isDateRange())
		{
			MartusLogger.logError("******* Validation not handled yet for " + type.getTypeName());
			return;
		}
		spec.validate(displayableLabel, fieldDataValue, localization);
	}

	private void clear()
	{
		hasBeenModified = false;
		
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
		
		fieldProperties.forEach((key, property) -> property.setValue(null));
		fieldProperties.clear();
		
		fieldSpecs = new FieldSpecCollection();
	}
	
	private void setFieldPropertiesFromBulletinSection(Bulletin b, FieldSpecCollection bulletinFieldSpecs)
	{
		for(int i = 0; i < bulletinFieldSpecs.size(); ++i)
		{
			FieldSpec fieldSpec = bulletinFieldSpecs.get(i);
			fieldSpecs.add(fieldSpec);
			String fieldTag = fieldSpec.getTag();
			String value = b.get(fieldTag);
			setField(fieldSpec, value);
//			System.out.println("copyDataFromBulletin " + fieldTag + ":" + value);
		}
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
	
	static class FieldValidator implements ChangeListener<String>
	{
		public FieldValidator(FieldSpec specToUse, MiniLocalization localizationToUse)
		{
			spec = specToUse;
			localization = localizationToUse;
			isValidProperty = new SimpleBooleanProperty(); 
		}
		
		public Property<Boolean> isValidProperty()
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
		private Property<Boolean> isValidProperty;
	}
	
	
	public MiniLocalization getLocalization()
	{
		return localization;
	}

	private MiniLocalization localization;
	private boolean hasBeenModified;
	
	private HashMap<String, SimpleStringProperty> fieldProperties;
	private HashMap<String, FieldValidator> fieldValidators;
	private ReadOnlyObjectWrapper<UniversalId> universalIdProperty;
	private ReadOnlyIntegerProperty versionProperty;
	private ReadOnlyStringProperty bulletinLocalId;

	private FieldSpecCollection fieldSpecs;
	private ObservableList<HeadquartersKey> authorizedToReadKeys;
	private ReadOnlyObjectWrapper<BulletinHistory> bulletinHistory;
}
