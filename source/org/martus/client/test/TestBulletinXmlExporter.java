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

package org.martus.client.test;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Vector;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.BulletinXmlExporter;
import org.martus.common.FieldCollection;
import org.martus.common.FieldCollectionForTesting;
import org.martus.common.GridData;
import org.martus.common.GridRow;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.crypto.MartusCrypto.EncryptionException;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.DropDownFieldSpec;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.FieldTypeDateRange;
import org.martus.common.fieldspec.FieldTypeMessage;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.fieldspec.TestCustomFieldSpecValidator;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.FieldDataPacket;
import org.martus.common.utilities.MartusFlexidate;
import org.martus.util.MultiCalendar;
import org.martus.util.TestCaseEnhanced;

public class TestBulletinXmlExporter extends TestCaseEnhanced
{
	public TestBulletinXmlExporter(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();
		if(store==null)
		{
			store = new MockBulletinStore();
		}
	}

	public void testBasics() throws Exception
	{
		Bulletin b = new Bulletin(store.getSignatureGenerator());
		b.setAllPrivate(false);

		final String sampleAuthor = "someone special";

		b.set(BulletinConstants.TAGAUTHOR, sampleAuthor);

		Vector list = new Vector();
		list.add(b);
		String result = doExport(list, false);
		assertContains("<MartusBulletinExportFormatVersion>1</MartusBulletinExportFormatVersion>", result);
		assertContains("<MartusBulletins>", result);
		assertContains("<MartusBulletin>", result);
		assertContains("<ExportMetaData>", result);
		assertContains("<BulletinMetaData>", result);
		assertContains(b.getAccount(), result);
		assertContains(b.getLocalId(), result);
		assertContains(sampleAuthor, result);
		assertContains("<MainFieldSpecs>", result);
		assertContains("<FieldValues>", result);
		assertNotContains("<PrivateFieldSpecs>", result);
		assertNotContains("<PrivateData>", result);
		assertNotContains("<AttachmentList>", result);
		assertNotContains("<History>", result);
	}
	
	public void testExportingFieldSpecs() throws Exception
	{
		FieldSpec[] topSpecs = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpec[] bottomSpecs = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		String choice1 = "choice A";
		String choice2 = "choice B";

		ChoiceItem[] choicesNoDups = {new ChoiceItem("no Dup", choice1), new ChoiceItem("second", choice2)};
		String dropdownTag = "ddTag";
		String dropdownLabel = "dropdown column label";
		DropDownFieldSpec dropDownSpecNoDuplicates = new DropDownFieldSpec(choicesNoDups);
		dropDownSpecNoDuplicates.setLabel(dropdownLabel);
		dropDownSpecNoDuplicates.setTag(dropdownTag);
		
		String booleanLabel = "Boolean Label";
		String booleanTag = "TagBoolean";
		FieldSpec booleanSpec = FieldSpec.createFieldSpec(booleanLabel, new FieldTypeBoolean());
		booleanSpec.setTag(booleanTag);
		
		GridFieldSpec gridWithNoDuplicateDropdownEntries = new GridFieldSpec();
		String gridTag = "GridTag";
		String gridLabel = "Grid Label";
		gridWithNoDuplicateDropdownEntries.setTag(gridTag);
		gridWithNoDuplicateDropdownEntries.setLabel(gridLabel);
		gridWithNoDuplicateDropdownEntries.addColumn(dropDownSpecNoDuplicates);
		gridWithNoDuplicateDropdownEntries.addColumn(booleanSpec);

		String messageLabel = "message Label";
		String messageTag = "messageTag";
		FieldSpec messageSpec = FieldSpec.createFieldSpec(messageLabel, new FieldTypeMessage());
		messageSpec.setTag(messageTag);
		
		topSpecs = TestCustomFieldSpecValidator.addFieldSpec(topSpecs, gridWithNoDuplicateDropdownEntries);
		bottomSpecs = TestCustomFieldSpecValidator.addFieldSpec(bottomSpecs,dropDownSpecNoDuplicates);
		bottomSpecs = TestCustomFieldSpecValidator.addFieldSpec(bottomSpecs,booleanSpec);
		bottomSpecs = TestCustomFieldSpecValidator.addFieldSpec(bottomSpecs, messageSpec);
		
		Bulletin b = new Bulletin(store.getSignatureGenerator(), topSpecs, bottomSpecs);
		b.setAllPrivate(false);

		final String sampleAuthor = "someone special";
		b.set(BulletinConstants.TAGAUTHOR, sampleAuthor);

		String expectedTopFieldSpecs = "<MainFieldSpecs>\n" +
			  "<Field type='LANGUAGE'>\n" +
			  "<Tag>language</Tag>\n" + 
			  "<Label></Label>\n" + 
			  "</Field>\n" +
			  "<Field type='STRING'>\n" +
			  "<Tag>author</Tag>\n" + 
			  "<Label></Label>\n" + 
			  "</Field>\n" +
			  "<Field type='STRING'>\n" +
			  "<Tag>organization</Tag>\n" + 
			  "<Label></Label>\n" + 
			  "</Field>\n" +
			  "<Field type='STRING'>\n" +
			  "<Tag>title</Tag>\n" + 
			  "<Label></Label>\n" + 
			  "</Field>\n" +
			  "<Field type='STRING'>\n" +
			  "<Tag>location</Tag>\n" + 
			  "<Label></Label>\n" + 
			  "</Field>\n" +
			  "<Field type='STRING'>\n" +
			  "<Tag>keywords</Tag>\n" + 
			  "<Label></Label>\n" + 
			  "</Field>\n" +
			  "<Field type='DATERANGE'>\n" +
			  "<Tag>eventdate</Tag>\n" + 
			  "<Label></Label>\n" + 
			  "</Field>\n" +
			  "<Field type='DATE'>\n" +
			  "<Tag>entrydate</Tag>\n" + 
			  "<Label></Label>\n" + 
			  "</Field>\n" +
			  "<Field type='MULTILINE'>\n" +
			  "<Tag>summary</Tag>\n" + 
			  "<Label></Label>\n" + 
			  "</Field>\n" +
			  "<Field type='MULTILINE'>\n" +
			  "<Tag>publicinfo</Tag>\n" + 
			  "<Label></Label>\n" + 
			  "</Field>\n" +
			  "<Field type='GRID'>\n" +
			  "<Tag>"+gridTag+"</Tag>\n" +
			  "<Label>"+gridLabel+"</Label>\n" +
			  "<GridSpecDetails>\n" +
			  "<Column type='DROPDOWN'>\n" +
			  "<Tag>"+dropdownTag+"</Tag>\n" +
			  "<Label>"+dropdownLabel+"</Label>\n" +
			  "<Choices>\n" +
			  "<Choice>"+choice1+"</Choice>\n" +
			  "<Choice>"+choice2+"</Choice>\n" +
			  "</Choices>\n" +
			  "</Column>\n" +
			  "<Column type='BOOLEAN'>\n" +
			  "<Tag>"+booleanTag+"</Tag>\n" +
			  "<Label>"+booleanLabel+"</Label>\n" +
			  "</Column>\n" +
			  "</GridSpecDetails>\n" +
			  "</Field>\n" +
			  "</MainFieldSpecs>\n\n";

		BulletinXmlExporter exporter = new BulletinXmlExporter(new MiniLocalization());
		StringWriter writer = new StringWriter();
		exporter.writeFieldSpecs(writer, b.getTopSectionFieldSpecs(), "MainFieldSpecs");
		String result = writer.toString();
		writer.close();
		assertEquals(expectedTopFieldSpecs, result);
		
		String expectedBottomFieldSpecs = "<PrivateFieldSpecs>\n" +
		  "<Field type='MULTILINE'>\n" +
		  "<Tag>privateinfo</Tag>\n" + 
		  "<Label></Label>\n" + 
		  "</Field>\n" +
		  "<Field type='DROPDOWN'>\n" +
		  "<Tag>"+dropdownTag+"</Tag>\n" +
		  "<Label>"+dropdownLabel+"</Label>\n" +
		  "<Choices>\n" +
		  "<Choice>"+choice1+"</Choice>\n" +
		  "<Choice>"+choice2+"</Choice>\n" +
		  "</Choices>\n" +
		  "</Field>\n" +
		  "<Field type='BOOLEAN'>\n" +
		  "<Tag>"+booleanTag+"</Tag>\n" +
		  "<Label>"+booleanLabel+"</Label>\n" +
		  "</Field>\n" +
		  "<Field type='MESSAGE'>\n" +
		  "<Tag>"+messageTag+"</Tag>\n" +
		  "<Label>"+messageLabel+"</Label>\n" +
		  "</Field>\n" +
		  "</PrivateFieldSpecs>\n\n";

	writer = new StringWriter();
	exporter.writeFieldSpecs(writer, b.getBottomSectionFieldSpecs(), "PrivateFieldSpecs");
	result = writer.toString();
	writer.close();
	assertEquals(expectedBottomFieldSpecs, result);
	}
	
	public void testExportGrids() throws Exception
	{
		String gridTag = "MyGridTag";
		String xmlFieldType = "<CustomFields>" +
				"<Field type='GRID'>" +
				"<Tag>"+gridTag+"</Tag>" +
				"<Label>Victim Information</Label>" +
				"<GridSpecDetails>" +
				"<Column type='STRING'>\n" +
				"<Tag></Tag>\n" +
				"<Label>Name of Victim</Label>\n" +
				"</Column>\n" +
				"<Column type='STRING'>\n" +
				"<Tag></Tag>\n" +
				"<Label>Age of Victim</Label>\n" +
				"</Column>\n" +
				"<Column type='DATE'>\n" +
				"<Tag></Tag>\n" +
				"<Label>Date Of Event</Label>\n" +
				"</Column>\n" +
				"</GridSpecDetails>" +
				"</Field></CustomFields>";
		GridFieldSpec newSpec = (GridFieldSpec)FieldCollection.parseXml(xmlFieldType)[0]; 
		FieldCollection fields = FieldCollectionForTesting.extendFields(StandardFieldSpecs.getDefaultTopSectionFieldSpecs(), newSpec);				
		
		Bulletin b = new Bulletin(store.getSignatureGenerator(), fields.getSpecs(), StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		b.setAllPrivate(false);
		GridData gridData = new GridData(newSpec);
		GridRow row = new GridRow(newSpec);
		row.setCellText(0, "rowData1");
		row.setCellText(1, "rowData2");
		row.setCellText(2, "20060504");
		gridData.addRow(row);
		b.set(gridTag, gridData.getXmlRepresentation());
		
		Vector list = new Vector();
		list.add(b);
		String result = doExport(list, false);
		assertContains("<Field tag='MyGridTag'>\n" +
				"<Value><GridData columns='3'>\n" +
				"<Row>\n" +
				"<Column>rowData1</Column>\n" +
				"<Column>rowData2</Column>\n" +
				"<Column>Simple:20060504</Column>\n" +
				"</Row>\n" +
				"</GridData>\n" +
				"</Value>\n" +
				"</Field>", result);
	}
	
	public void testExportHistory() throws Exception
	{
		String localId1 = "pretend local id";
		String localId2 = "another fake local id";

		BulletinHistory fakeHistory = new BulletinHistory();
		fakeHistory.add(localId1);
		fakeHistory.add(localId2);
		
		Bulletin b = new Bulletin(store.getSignatureGenerator());
		b.setHistory(fakeHistory);
		
		Vector list = new Vector();
		list.add(b);
		String result = doExport(list, false);
		
		assertContains("<History>", result);
		assertContains("<Ancestor>", result);
		assertContains(localId1, result);
		assertContains(localId2, result);
	}

	public void testExportWithPublicAttachments() throws Exception
	{
		Bulletin b = new Bulletin(store.getSignatureGenerator());
		b.setAllPrivate(false);
		final File sampleAttachmentFile1 = addNewPublicSampleAttachment(b);
		final File sampleAttachmentFile2 = addNewPublicSampleAttachment(b);

		Vector list = new Vector();
		list.add(b);
		String result = doExport(list, false);

		assertContains(sampleAttachmentFile1.getName(), result);
		assertContains(sampleAttachmentFile2.getName(), result);
	}

	public void testExportMultipleBulletins() throws Exception
	{
		Bulletin b1 = new Bulletin(store.getSignatureGenerator());
		b1.setAllPrivate(false);
		Bulletin b2 = new Bulletin(store.getSignatureGenerator());
		b2.setAllPrivate(false);

		final String sampleTitle1 = "a big event took place!";
		final String sampleTitle2 = "watch this space";
		b1.set(BulletinConstants.TAGTITLE, sampleTitle1);
		b2.set(BulletinConstants.TAGTITLE, sampleTitle2);

		StringWriter writer = new StringWriter();
		Vector list = new Vector();
		list.add(b1);
		list.add(b2);
		BulletinXmlExporter exporter = new BulletinXmlExporter(new MiniLocalization());
		exporter.exportBulletins(writer, list, false);
		String result = writer.toString();

		assertContains(sampleTitle1, result);
		assertContains("<Field tag='title'", result);
		assertContains(sampleTitle2, result);
	}

	public void testExportPrivateData() throws Exception
	{
		Bulletin b = new Bulletin(store.getSignatureGenerator());
		b.setAllPrivate(false);

		final String samplePublic = "someone special";
		final String samplePrivate = "shhhhh! it's private!";

		b.set(BulletinConstants.TAGPUBLICINFO, samplePublic);
		b.set(BulletinConstants.TAGPRIVATEINFO, samplePrivate);		

		Vector list = new Vector();
		list.add(b);
		String publicOnly = doExport(list, false);
		assertContains("<Field>\n<Tag>PublicDataOnly</Tag>", publicOnly);
		assertContains(samplePublic, publicOnly);
		assertNotContains(samplePrivate, publicOnly);

		String publicAndPrivate = doExport(list, true);
		assertContains("<Field>\n<Tag>PublicAndPrivateData</Tag>", publicAndPrivate);
		
		assertContains("<FieldValues>", publicAndPrivate);
		assertContains("<MainFieldSpecs>", publicAndPrivate);
		assertContains("<PrivateFieldSpecs>", publicAndPrivate);
		
		assertContains(samplePublic, publicAndPrivate);
		assertContains(samplePrivate, publicAndPrivate);
	}

	public void testExportWithPrivateAttachment() throws Exception
	{
		Bulletin b = new Bulletin(store.getSignatureGenerator());
		b.setAllPrivate(false);
		final File sampleAttachmentFile1 = addNewPrivateSampleAttachment(b);

		Vector list = new Vector();
		list.add(b);

		String publicOnly = doExport(list, false);
		assertNotContains(sampleAttachmentFile1.getName(), publicOnly);

		String publicAndPrivate = doExport(list, true);
		assertContains(sampleAttachmentFile1.getName(), publicAndPrivate);
	}

	public void testExportAnAllPrivateBulletin() throws Exception
	{
		Bulletin b = new Bulletin(store.getSignatureGenerator());
		b.setAllPrivate(true);
		final String sampleAuthor = "someone special";
		b.set(BulletinConstants.TAGAUTHOR, sampleAuthor);

		Vector list = new Vector();
		list.add(b);
		String publicOnly = doExport(list, false);
		String publicAndPrivate = doExport(list, true);

		assertContains(b.getAccount(), publicOnly);
		assertContains(b.getLocalId(), publicOnly);
		assertContains("<Field>\n<Tag>AllPrivate</Tag>", publicOnly);
		assertNotContains(sampleAuthor, publicOnly);
		assertContains("<FieldValues>", publicOnly);
		assertContains("<!--  No Private FieldSpecs or Data was exported  -->", publicOnly);
		assertNotContains("<MainFieldSpecs>", publicOnly);
		assertNotContains("<PrivateFieldSpecs>", publicOnly);

		assertContains(b.getAccount(), publicAndPrivate);
		assertContains(b.getLocalId(), publicAndPrivate);
		assertContains("<Tag>AllPrivate</Tag>", publicAndPrivate);
		assertContains(sampleAuthor, publicAndPrivate);
		assertContains("<FieldValues>", publicAndPrivate);
		assertContains("<MainFieldSpecs>", publicAndPrivate);
		assertContains("<PrivateFieldSpecs>", publicAndPrivate);
	}

	public void testExportCustomFieldValue() throws Exception
	{
		String customTag1 = "custom1";
		String customTag2 = "custom2";
		String label1 = "Witness1 name";
		String label2 = "Witness2 name";					
		
		String xmlFieldType = "<CustomFields><Field><Tag>"+customTag1+"</Tag>" +
			"<Label>" + label1 + "</Label></Field></CustomFields>";
		FieldSpec newSpec1 = FieldCollection.parseXml(xmlFieldType)[0]; 
		xmlFieldType = "<CustomFields><Field><Tag>"+customTag2+"</Tag>" +
			"<Label>" + label2 + "</Label></Field></CustomFields>";
		FieldSpec newSpec2 = FieldCollection.parseXml(xmlFieldType)[0]; 
		FieldSpec[] extraFieldSpecs = {newSpec1, newSpec2};
		FieldCollection fields = FieldCollectionForTesting.extendFields(StandardFieldSpecs.getDefaultTopSectionFieldSpecs(), extraFieldSpecs);
		
		Bulletin b = new Bulletin(store.getSignatureGenerator(), fields.getSpecs(), StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		b.setAllPrivate(false);
		
		FieldDataPacket fdp = b.getFieldDataPacket();
		assertTrue("contain custom fied?", fdp.fieldExists(customTag1));
		assertTrue("contain custom field?", fdp.fieldExists(customTag2));
		
		final String samplePublic = "public name";		
		b.set(BulletinConstants.TAGPUBLICINFO, samplePublic);				
		final String sampleAuthor = "John Smith";
		b.set(BulletinConstants.TAGAUTHOR, sampleAuthor);				
										
		b.set(customTag1, "a<bc");
		b.set(customTag2, "&test");			

		Vector list = new Vector();
		list.add(b);

		String result = doExport(list, false);

		assertContains(samplePublic, result);
		assertContains("<FieldValues>", result);
		assertContains("<Value>a&lt;bc</Value>", result);
		assertContains("<Value>&amp;test</Value>", result);

		b.set(customTag1, ">");
		b.set(customTag2, "&");			

		list = new Vector();
		list.add(b);

		result = doExport(list, false);

		assertContains(samplePublic, result);
		assertContains("<FieldValues>", result);
		assertContains("<Value>&gt;</Value>", result);
		assertContains("<Value>&amp;</Value>", result);						
		
	}

	public void testExportCustomFiledSignalCharacterOfTagAndLabel() throws Exception
	{
		String customTag1 = "A";
		String customTag2 = "custom";
		String label1 = "Witness1 name";
		String label2 = "N";					
		
		String xmlFieldType = "<CustomFields><Field><Tag>"+customTag1+"</Tag>" +
			"<Label>" + label1 + "</Label></Field></CustomFields>";
		FieldSpec newSpec1 = FieldCollection.parseXml(xmlFieldType)[0]; 
		xmlFieldType = "<CustomFields><Field><Tag>"+customTag2+"</Tag>" +
			"<Label>" + label2 + "</Label></Field></CustomFields>";
		FieldSpec newSpec2 = FieldCollection.parseXml(xmlFieldType)[0]; 

		FieldSpec[] extraFieldSpecs = {newSpec1, newSpec2};
		FieldCollection fields = FieldCollectionForTesting.extendFields(StandardFieldSpecs.getDefaultTopSectionFieldSpecs(), extraFieldSpecs);
		
		Bulletin b = new Bulletin(store.getSignatureGenerator(), fields.getSpecs(), StandardFieldSpecs.getDefaultBottomSectionFieldSpecs());
		b.setAllPrivate(false);				
										
		b.set(customTag1, "abc");
		b.set(customTag2, "test");			

		Vector list = new Vector();
		list.add(b);

		String result = doExport(list, false);
		
		assertContains("<FieldValues>", result);
		assertContains("<Tag>A</Tag>", result);
		assertContains("<Label>N</Label>", result);		
	}

	public void testXmlEscaping() throws Exception
	{
		String needsEscaping = "a < b && b > c";

		Bulletin b = new Bulletin(store.getSignatureGenerator());
		b.set(Bulletin.TAGAUTHOR, needsEscaping);
		final String result = getExportedXml(b);
		assertNotContains("exported unescaped?", needsEscaping, result);
		assertContains("didn't write escaped?", "a &lt; b &amp;&amp; b &gt; c", result);
	}

	public void testExportSimpleDate() throws Exception
	{
		Bulletin b = new Bulletin(store.getSignatureGenerator());
		String entryDate = b.get(Bulletin.TAGENTRYDATE);
		final String result = getExportedXml(b);
		assertContains("didn't write good simple date?", "Simple:"+entryDate, result);
	}

	public void testExportDateRange() throws Exception
	{
		String rawDateRangeString = createSampleDateRangeString();

		Bulletin b = new Bulletin(store.getSignatureGenerator());
		b.set(Bulletin.TAGEVENTDATE, rawDateRangeString);
		final String result = getExportedXml(b);
		assertNotContains("exported raw flexidate?", rawDateRangeString, result);
		assertContains("didn't write good date range?", "Range:2005-05-01,2005-05-30", result);
	}

	private String getExportedXml(Bulletin b) throws IOException
	{
		StringWriter dest = new StringWriter();
		Vector list = new Vector();
		list.add(b);
		BulletinXmlExporter exporter = new BulletinXmlExporter(new MiniLocalization());
		exporter.exportBulletins(dest, list, true);
		final String result = dest.toString();
		return result;
	}

	private String createSampleDateRangeString()
	{
		final int MAY = 5;
		MultiCalendar beginDate = MultiCalendar.createFromGregorianYearMonthDay(2005, MAY, 1);
		MultiCalendar endDate = MultiCalendar.createFromGregorianYearMonthDay(2005, MAY, 30);
		String rawDateRangeString = MartusFlexidate.toBulletinFlexidateFormat(beginDate, endDate);
		return rawDateRangeString;
	}
	
	public void testExportGridDateRange() throws Exception
	{
		GridFieldSpec gridSpec = new GridFieldSpec();
		gridSpec.setTag("grid");
		FieldSpec dateRangeSpec = FieldSpec.createCustomField("range", "Date Range", new FieldTypeDateRange());
		gridSpec.addColumn(dateRangeSpec);
		
		GridData data = new GridData(gridSpec);
		data.addEmptyRow();
		String rawDateRangeString = createSampleDateRangeString();
		data.setValueAt(rawDateRangeString, 0, 0);
		
		FieldSpec[] publicSpecs = new FieldSpec[] {gridSpec};
		FieldSpec[] privateSpecs = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		Bulletin b = new Bulletin(store.getSignatureGenerator(), publicSpecs, privateSpecs);
		b.set(gridSpec.getTag(), data.getXmlRepresentation());
		
		final String result = getExportedXml(b);
		assertNotContains("exported raw flexidate?", rawDateRangeString, result);
		assertContains("didn't write good date range?", "2005-05-01,2005-05-30", result);
	}
	
	String doExport(Vector list, boolean includePrivateData) throws IOException
	{
		StringWriter writer = new StringWriter();
		BulletinXmlExporter exporter = new BulletinXmlExporter(new MiniLocalization());
		exporter.exportBulletins(writer, list, includePrivateData);
		String result = writer.toString();
		return result;
	}

	File addNewPublicSampleAttachment(Bulletin b)
		throws IOException, EncryptionException
	{
		final File sampleAttachmentFile = createTempFile();
		AttachmentProxy ap = new AttachmentProxy(sampleAttachmentFile);
		b.addPublicAttachment(ap);
		return sampleAttachmentFile;
	}

	File addNewPrivateSampleAttachment(Bulletin b)
		throws IOException, EncryptionException
	{
		final File sampleAttachmentFile = createTempFile();
		AttachmentProxy ap = new AttachmentProxy(sampleAttachmentFile);
		b.addPrivateAttachment(ap);
		return sampleAttachmentFile;
	}

	static ClientBulletinStore store;
}
