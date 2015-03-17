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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.util.XFormUtils;
import org.martus.client.swingui.jfx.generic.data.ObservableChoiceItemList;
import org.martus.client.swingui.jfx.landing.bulletins.AttachmentTableRowData;
import org.martus.common.Exceptions.ImportXFormsException;
import org.martus.common.FieldSpecCollection;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.MartusXml;
import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.ReusableChoices;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletinstore.BulletinStore;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.field.MartusField;
import org.martus.common.fieldspec.DataInvalidException;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeNormal;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.packet.UniversalId;
import org.martus.common.utilities.BurmeseUtilities;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.swing.FontHandler;
import org.martus.util.MultiCalendar;
import org.martus.util.xml.XmlUtilities;

/**
 * This class wraps a Bulletin object, exposing each data member as a 
 * Property, to make life easier when working in JavaFX.
 */
public class FxBulletin
{
	public FxBulletin(MiniLocalization localizationToUse)
	{
		localization = localizationToUse;
		
		fields = new HashMap<String, FxBulletinField>();
		attachments = FXCollections.observableArrayList();
		hasBeenValidatedProperty = new SimpleBooleanProperty();
		
		clear();
	}

	public void copyDataFromBulletin(Bulletin b, BulletinStore store) throws Exception
	{
		clear();
		
		if (isXFormsBulletin(b))
			b = createNewBulletinFromXFormsBulletin(b);
		
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

		copyReusableChoiceListsFromBulletinSection(b.getTopSectionFieldSpecs());
		copyReusableChoiceListsFromBulletinSection(b.getBottomSectionFieldSpecs());

		setFieldPropertiesFromBulletinSection(b, b.getTopSectionFieldSpecs());
		setFieldPropertiesFromBulletinSection(b, b.getBottomSectionFieldSpecs());
		
		addAttachmentProxies(b.getPrivateAttachments(), store.getDatabase());
		addAttachmentProxies(b.getPublicAttachments(), store.getDatabase());

		validBulletin = store.isBulletinValid(b);
		String authorPublicKeyString = b.getAccount();
		String accountId = store.getAccountId();
		boolean notYourBulletin = !authorPublicKeyString.equals(accountId);
		if(notYourBulletin)
			notAuthorizedToRead = !b.getAuthorizedToReadKeys().containsKey(accountId);
	}
	
	private void addAttachmentProxies(AttachmentProxy[] attachmentsToAdd, ReadableDatabase db)
	{
		for (AttachmentProxy attachmentProxy : attachmentsToAdd)
		{
			AttachmentTableRowData attachmentRow = new AttachmentTableRowData(attachmentProxy, db);
			
			String storable = attachmentRow.nameProperty().getValue();
			String displayable = storable;
			if(FontHandler.isDoZawgyiConversion())
				displayable = BurmeseUtilities.getDisplayable(storable);
			attachmentRow.nameProperty().setValue(displayable);
			attachmentProxy.setLabel(displayable);
			
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
			FxBulletinField field = getField(fieldTag);
			String value = field.getValue();
			String storable = value;
			if(FontHandler.isDoZawgyiConversion())
				storable = BurmeseUtilities.getStorable(value);
			modified.set(fieldTag, storable);
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
			String displayable = proxy.getLabel();
			String storable = displayable;
			if(FontHandler.isDoZawgyiConversion())
				storable = BurmeseUtilities.getStorable(displayable);
			proxy.setLabel(storable);
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

	public FxBulletinField getField(FieldSpec fieldSpec)
	{
		return getField(fieldSpec.getTag());
	}

	public FxBulletinField getField(String tag)
	{
		return fields.get(tag);
	}

	public FxBulletinGridField getGridField(String gridTag)
	{
		FxBulletinField field = getField(gridTag);
		if(!field.isGrid())
			throw new RuntimeException("not a grid: " + gridTag);
		
		return (FxBulletinGridField) field;
	}

	public SimpleStringProperty fieldProperty(String fieldTag)
	{
		FieldSpec foundSpec = fieldSpecs.findBytag(fieldTag);
		if(foundSpec == null)
			throw new NullPointerException("No such field: " + fieldTag);

		if(foundSpec.getType().isGrid())
			throw new NullPointerException("fieldProperty not available for a grid: " + fieldTag);
		
		FxBulletinField field = fields.get(fieldTag);
		return field.valueProperty();
	}
	
	public ObservableBooleanValue isValidProperty(String fieldTag)
	{
		FieldSpec foundSpec = fieldSpecs.findBytag(fieldTag);
		if(foundSpec == null)
			throw new NullPointerException("No such field: " + fieldTag);

		if(foundSpec.getType().isGrid())
			throw new NullPointerException("fieldProperty not available for a grid: " + fieldTag);

		return fields.get(fieldTag).fieldIsValidProperty();
	}
	
	public Vector<ObservableChoiceItemList> gridColumnValuesProperty(String gridTag, String gridColumnLabel)
	{
		return getGridField(gridTag).gridColumnValuesProperty(gridColumnLabel);
	}

	public BooleanProperty getImmutableOnServerProperty()
	{
		return immutableOnServer;
	}
	
	public PoolOfReusableChoicesLists getAllReusableChoicesLists()
	{
		return fieldSpecs.getAllReusableChoiceLists();
	}

	public ReusableChoices getReusableChoices(String onlyReusableChoicesCode)
	{
		ReusableChoices reusableChoices = fieldSpecs.getReusableChoices(onlyReusableChoicesCode);
		return reusableChoices;
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
			
			String tag = spec.getTag();
			fields.get(tag).validate();
		}
	}

	private void clear()
	{
		hasBeenModified = false;
		validBulletin = false;
		notAuthorizedToRead = false;
		hasBeenValidatedProperty.setValue(false);
		
		safelyClearValue(universalIdProperty);
		safelyClearValue(bulletinHistory);
		
		bulletinHistory = null;
		universalIdProperty = null;
		bulletinLocalId = null;
		versionProperty = null;
		
		safelyClearList(authorizedToReadKeys);
		safelyClearList(attachments);

		fields.forEach((key, field) -> clearField(field));
		fields.clear();
		
		fieldSpecs = new FieldSpecCollection();
	}

	private void safelyClearValue(ReadOnlyObjectWrapper valueToClear)
	{
		if(valueToClear != null)
		{
			valueToClear.setValue(null);
		}
	}

	private void safelyClearList(ObservableList valueToClear)
	{
		if(valueToClear != null)
		{
			valueToClear.clear();
		}
	}
	
	private void clearField(FxBulletinField field)
	{
		field.clear();
	}
	
	private void setFieldPropertiesFromBulletinSection(Bulletin b, FieldSpecCollection bulletinFieldSpecs) throws Exception
	{
		for(int i = 0; i < bulletinFieldSpecs.size(); ++i)
		{
			FieldSpec fieldSpec = bulletinFieldSpecs.get(i);
			String fieldTag = fieldSpec.getTag();
			MartusField martusField = b.getField(fieldTag);
			String value = getFieldValue(martusField);
			if(FontHandler.isDoZawgyiConversion())
				value = BurmeseUtilities.getDisplayable(value);

			fieldSpecs.add(fieldSpec);
			
			FxBulletinField field = FxBulletinField.createFxBulletinField(this, fieldSpec, getLocalization());
			fields.put(fieldTag, field);
			
			setField(fieldSpec, value);
//			System.out.println("copyDataFromBulletin " + fieldTag + ":" + value);
		}
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

		FxBulletinField field = fields.get(tag);
		field.setValue(value);
		field.addValueListener((observable, oldValue, newValue) -> hasBeenModified = true);
	}
	
	private void copyReusableChoiceListsFromBulletinSection(FieldSpecCollection bulletinFieldSpecs)
	{
		fieldSpecs.addAllReusableChoicesLists(bulletinFieldSpecs.getAllReusableChoiceLists());
	}
	
	public MiniLocalization getLocalization()
	{
		return localization;
	}

	//TODO add unit tests for this
	public boolean isValidBulletin()
	{
		return validBulletin;
	}
	
	//TODO add unit tests for this
	public boolean notAuthorizedToRead()
	{
		return notAuthorizedToRead;
	}
	
	private Bulletin createNewBulletinFromXFormsBulletin(Bulletin bulletinWithXForms) throws Exception
	{
		String xFormsModelXmlAsString = getXformsModelWithoutRootElement(bulletinWithXForms);
		String xFormsInstanceXmlAsString = getXFormsInstanceWithoutRootElement(bulletinWithXForms);
		FormEntryController formEntryController = importXFormsData(xFormsModelXmlAsString, xFormsInstanceXmlAsString);
		if (formEntryController == null)
			throw new ImportXFormsException();
		
		FieldSpecCollection fieldSpecsFromXForms = createFieldSpecsFromXForms(formEntryController);
		
		return createBulletin(bulletinWithXForms, formEntryController, fieldSpecsFromXForms);
	}

	private String getXFormsInstanceWithoutRootElement(Bulletin bulletinWithXForms)
	{
		String xFormsInstanceXmlAsString = bulletinWithXForms.getFieldDataPacket().getXFormsInstanceAsString();
		
		return stripRootElement(xFormsInstanceXmlAsString, MartusXml.XFormsInstanceElementName);
	}

	private String getXformsModelWithoutRootElement(Bulletin bulletinWithXForms)
	{
		String xFormsModelXmlAsString = bulletinWithXForms.getFieldDataPacket().getXFormsModelAString();
		
		return stripRootElement(xFormsModelXmlAsString, MartusXml.XFormsModelElementName);
	}

	private String stripRootElement(String xml, String elementNameToStrip)
	{
		return XmlUtilities.stripXmlStartEndElements(xml, elementNameToStrip);
	}

	private Bulletin createBulletin(Bulletin bulletinWithXForms, FormEntryController formEntryController, FieldSpecCollection fieldsFromXForms) throws Exception
	{
		Bulletin bulletinLoadedFromXForms = new Bulletin(bulletinWithXForms.getSignatureGenerator(), new FieldSpecCollection(), fieldsFromXForms);		
		resetFormEntryControllerIndex(formEntryController);
		int event;
		while ((event = formEntryController.stepToNextEvent()) != FormEntryController.EVENT_END_OF_FORM) 
		{
			//FIXME urgent - This method only handles string fields
			//This will change as more fields are added
			if (event != FormEntryController.EVENT_QUESTION) 
			{
				continue;
			}
		
			FormEntryPrompt questionPrompt = formEntryController.getModel().getQuestionPrompt();
			IAnswerData answer = questionPrompt.getAnswerValue();
			QuestionDef question = questionPrompt.getQuestion();
			final int dataType = questionPrompt.getDataType();
			if (dataType == Constants.DATATYPE_TEXT)
			{
				TreeReference reference = (TreeReference) question.getBind().getReference();
				if (answer != null) 
				{
					FieldDataPacket privateFieldDataPacket = bulletinLoadedFromXForms.getPrivateFieldDataPacket();
					String xFormsFieldTag = reference.getNameLast();
					privateFieldDataPacket.set(xFormsFieldTag, answer.getDisplayText());
				}
			}
		}
		
		return bulletinLoadedFromXForms;
	}

	private FieldSpecCollection createFieldSpecsFromXForms(FormEntryController formEntryController)
	{
		FieldSpecCollection fieldsFromXForms = new FieldSpecCollection();
		int event;
		while ((event = formEntryController.stepToNextEvent()) != FormEntryController.EVENT_END_OF_FORM) 
		{
			//FIXME urgent - This method only handles string fields
			//This will change as more fields are added
			if (event != FormEntryController.EVENT_QUESTION) 
			{
				continue;
			}
			
			FormEntryPrompt questionPrompt = formEntryController.getModel().getQuestionPrompt();
			IAnswerData answer = questionPrompt.getAnswerValue();
			QuestionDef question = questionPrompt.getQuestion();
			final int dataType = questionPrompt.getDataType();
			if (dataType == Constants.DATATYPE_TEXT)
			{
				TreeReference reference = (TreeReference) question.getBind().getReference();
				if (answer != null) 
				{
					FieldSpec fieldSpec = FieldSpec.createCustomField(reference.getNameLast(), questionPrompt.getQuestion().getLabelInnerText(), new FieldTypeNormal());
					fieldsFromXForms.add(fieldSpec);
				}
			}
		}
		
		return fieldsFromXForms;
	}

	private void resetFormEntryControllerIndex(FormEntryController formEntryController)
	{
		while (formEntryController.stepToPreviousEvent() != FormEntryController.EVENT_BEGINNING_OF_FORM);
	}

    private FormEntryController importXFormsData(String xFormsModelXmlAsString, String xFormsInstance) 
    {
    	InputStream xFormsModelInputStream = new ByteArrayInputStream(xFormsModelXmlAsString.getBytes(StandardCharsets.UTF_8));
		FormDef formDef = XFormUtils.getFormFromInputStream(xFormsModelInputStream);
		FormEntryModel formEntryModel = new FormEntryModel(formDef);
		FormEntryController formEntryController = new FormEntryController(formEntryModel);
		
    	byte[] xFormsInstanceBytes = xFormsInstance.getBytes(StandardCharsets.UTF_8);
    	TreeElement modelRootElement = formEntryController.getModel().getForm().getInstance().getRoot().deepCopy(true);
    	TreeElement instanceRootElement = XFormParser.restoreDataModel(xFormsInstanceBytes, null).getRoot();
    	if (!instanceRootElement.getName().equals(modelRootElement.getName()))
    		return null;
    	
    	if (instanceRootElement.getMult() != TreeReference.DEFAULT_MUTLIPLICITY)
    		return null;
    	
    	populateDataModel(modelRootElement);
    	modelRootElement.populate(instanceRootElement, formEntryController.getModel().getForm());
    	populateFormEntryControllerModel(formEntryController, modelRootElement);
    	fixLanguageIusses(formEntryController);
    	
    	return formEntryController;
    }

	private void populateFormEntryControllerModel(FormEntryController formEntryController, TreeElement modelRoot)
	{
		formEntryController.getModel().getForm().getInstance().setRoot(modelRoot);
	}

	private void fixLanguageIusses(FormEntryController formEntryController)
	{
		//NOTE: this comment is from Collect's java rosa seference
    	// fix any language issues
    	// : http://bitbucket.org/javarosa/main/issue/5/itext-n-appearing-in-restored-instances
		if (formEntryController.getModel().getLanguages() != null) 
    		formEntryController.getModel().getForm().localeChanged(formEntryController.getModel().getLanguage(), formEntryController.getModel().getForm().getLocalizer());
	}

	private void populateDataModel(TreeElement modelRootElement)
	{
		TreeReference treeReference = TreeReference.rootRef();
    	treeReference.add(modelRootElement.getName(), TreeReference.INDEX_UNBOUND);
	}
	
	private boolean isXFormsBulletin(Bulletin bulletinToUse)
	{
		return bulletinToUse.getFieldDataPacket().containXFormsData();
	}
	
	private MiniLocalization localization;
	private boolean hasBeenModified;
	private SimpleBooleanProperty hasBeenValidatedProperty;
	
	private HashMap<String, FxBulletinField> fields;
	
	private ReadOnlyObjectWrapper<UniversalId> universalIdProperty;
	private ReadOnlyIntegerProperty versionProperty;
	private ReadOnlyStringProperty bulletinLocalId;

	private FieldSpecCollection fieldSpecs;
	private ObservableList<HeadquartersKey> authorizedToReadKeys;
	private ReadOnlyObjectWrapper<BulletinHistory> bulletinHistory;

	private BooleanProperty immutableOnServer;
	private ObservableList<AttachmentTableRowData> attachments;
	
	private boolean validBulletin;
	private boolean notAuthorizedToRead;
}
