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
package org.martus.client.swingui.jfx.landing.bulletins;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.FxBulletin;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.attachments.ViewAttachmentHandler;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.generic.TableRowData;
import org.martus.client.swingui.jfx.generic.controls.FxButtonTableCellFactory;
import org.martus.client.swingui.jfx.generic.controls.FxImageTableCellFactory;
import org.martus.client.swingui.jfx.landing.bulletins.AttachmentViewController.FileType;
import org.martus.common.MartusLogger;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.crypto.MartusCrypto.CryptoException;
import org.martus.common.database.ReadableDatabase;
import org.martus.common.packet.Packet.InvalidPacketException;
import org.martus.common.packet.Packet.SignatureVerificationException;
import org.martus.common.packet.Packet.WrongPacketTypeException;
import org.martus.util.StreamableBase64.InvalidBase64Exception;


public class BulletinAttachmentsController extends FxController
{
	public BulletinAttachmentsController(UiMainWindow mainWindowToUse, FxBulletin bulletinToUse)
	{
		super(mainWindowToUse);
		bulletin = bulletinToUse;
	}

	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		initalizeColumns();
		initalizeItemsTable();
	}
	
	public void setViewingAttachmentsOnly()
	{
		addAttachmentButton.setVisible(false);
		removeColumn.setVisible(false);
	}
	
	private void initalizeItemsTable()
	{
		ObservableList<AttachmentTableRowData> attachments = bulletin.getAttachments();
		attachments.forEach((rowData) -> loadThumbnailIfAny(rowData));
		attachmentsTable.setItems(attachments);
		attachmentsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		
		BooleanBinding nonEmptyTableBinding = Bindings.isNotEmpty(attachmentsTable.getItems());
		attachmentsTable.visibleProperty().bind(nonEmptyTableBinding);
		
		// FIXME: This is unfinished work. The thumbnails appear to be loading, but 
		// setting up the cell factories is going to take more time than I have right now
		boolean loadThumbnails = false;
		if(loadThumbnails)
			populateThumbnails(attachments);
	}	
	
	private void loadThumbnailIfAny(AttachmentTableRowData rowData)
	{
		try
		{
			AttachmentProxy proxy = rowData.getAttachmentProxy();
			File file = ViewAttachmentHandler.obtainFileForAttachment(proxy, getMainWindow().getStore());

			FileType type = AttachmentViewController.determineFileType(file);
			if(type != FileType.Image)
				return;
			
			FileInputStream in = new FileInputStream(file);
			try
			{
				Image image = new Image(in);
				rowData.imageProperty().setValue(image);
			}
			finally
			{
				in.close();
			}
		} 
		catch (Exception relativelyHarmlessException)
		{
			MartusLogger.logException(relativelyHarmlessException);
		}

	}

	private void populateThumbnails(ObservableList<AttachmentTableRowData> attachments)
	{
		attachments.forEach((attachmentTableRowData) -> populateThumbnail(attachmentTableRowData));
	}

	private void populateThumbnail(	AttachmentTableRowData attachmentTableRowData)
	{
		try
		{
			AttachmentProxy attachmentProxy = attachmentTableRowData.getAttachmentProxy();
			FileType type = AttachmentViewController.determineFileType(attachmentProxy.getLabel());
			if(type == FileType.Image)
			{
				File file = ViewAttachmentHandler.obtainFileForAttachment(attachmentProxy, getMainWindow().getStore());
				Image image = loadImage(file);
				attachmentTableRowData.imageProperty().setValue(image);
			}
		}
		catch(Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	private Image loadImage(File file) throws Exception
	{
		FileInputStream in = new FileInputStream(file);
		try
		{
			Image image = new Image(in);
			return image;
		}
		finally
		{
			in.close();
		}
	}
	
	private static class TableRowStringValueFactory extends PropertyValueFactory<AttachmentTableRowData, String>
	{
		public TableRowStringValueFactory(String propertyName)
		{
			super(propertyName);
		}
		
	}
	
	private static class TableRowBooleanValueFactory extends PropertyValueFactory<TableRowData, Boolean>
	{
		public TableRowBooleanValueFactory(String propertyName)
		{
			super(propertyName);
		}
	}

	private static class TableRowImageValueFactory extends PropertyValueFactory<TableRowData, ReadOnlyProperty<Image>>
	{
		public TableRowImageValueFactory(String propertyName)
		{
			super(propertyName);
		}
	}

	private void initalizeColumns()
	{
		TableRowStringValueFactory nameValueFactory = new TableRowStringValueFactory(AttachmentTableRowData.ATTACHMENT_NAME_PROPERTY_NAME);
		nameColumn.setCellValueFactory(nameValueFactory);
		nameColumn.setCellFactory(TextFieldTableCell.<AttachmentTableRowData>forTableColumn());

		TableRowStringValueFactory sizeValueFactory = new TableRowStringValueFactory(AttachmentTableRowData.ATTACHMENT_SIZE_PROPERTY_NAME);
		sizeColumn.setCellValueFactory(sizeValueFactory);
		sizeColumn.setCellFactory(TextFieldTableCell.<AttachmentTableRowData>forTableColumn());

		thumbnailColumn.setCellFactory(FxImageTableCellFactory.createNormalImageTableCellFactory(() -> viewSelectedAttachment()));
		thumbnailColumn.setCellValueFactory(new TableRowImageValueFactory(AttachmentTableRowData.ATTACHMENT_IMAGE_PROPERTY_NAME));

		Image viewImage = new Image(VIEW_ATTACHMENT_IMAGE_PATH);
		viewColumn.setCellFactory(FxButtonTableCellFactory.createNormalButtonTableCellFactory(viewImage, () -> viewSelectedAttachment()));
		viewColumn.setCellValueFactory(new TableRowBooleanValueFactory(AttachmentTableRowData.ATTACHMENT_VIEW_PROPERTY_NAME));

		Image removeImage = new Image(REMOVE_ATTACHMENT_IMAGE_PATH);
		removeColumn.setCellFactory(FxButtonTableCellFactory.createNormalButtonTableCellFactory(removeImage, () -> removeSelectedAttachment()));
		removeColumn.setCellValueFactory(new TableRowBooleanValueFactory(AttachmentTableRowData.ATTACHMENT_REMOVE_PROPERTY_NAME));

		Image saveImage = new Image(SAVE_ATTACHMENT_IMAGE_PATH);
		saveColumn.setCellFactory(FxButtonTableCellFactory.createNormalButtonTableCellFactory(saveImage, () -> saveSelectedAttachment()));
		saveColumn.setCellValueFactory(new TableRowBooleanValueFactory(AttachmentTableRowData.ATTACHMENT_SAVE_PROPERTY_NAME));
	}
	
	
	private void viewSelectedAttachment()
	{
		AttachmentTableRowData selectedItem = getSelectedAttachmentRowData();
		if(selectedItem == null)
		{
			MartusLogger.log("Attempted to remove Attachment with nothing selected");
			return;
		}
		
		AttachmentProxy proxy = selectedItem.getAttachmentProxy();
		try
		{
			if(viewAttachmentInternally(proxy))
				return;
			
			if(ViewAttachmentHandler.shouldNotViewAttachmentsInExternalViewer())
			{
				showNotifyDialog("ViewAttachmentNotAvailable");
				return;
			}

			ViewAttachmentHandler.launchExternalAttachmentViewer(proxy, getApp().getStore());
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	private void saveSelectedAttachment()
	{
		AttachmentTableRowData selectedItem = getSelectedAttachmentRowData();
		if(selectedItem == null)
		{
			MartusLogger.log("Attempted to save Attachment with nothing selected");
			return;
		}
		
		try
		{
			AttachmentProxy proxy = selectedItem.getAttachmentProxy();
			String fileName = proxy.getLabel();

			FileChooser chooser = new FileChooser();
			chooser.setInitialFileName(fileName);
			File outputFile = chooser.showSaveDialog(null);
			if(outputFile == null)
				return;
			
			File attachmentAlreadyAvailableAsFile = proxy.getFile();
			if(attachmentAlreadyAvailableAsFile != null)
				savePendingAttachment(attachmentAlreadyAvailableAsFile, outputFile);
			else
				saveAttachmentFromDatabase(proxy, outputFile);	
		}
		catch(Exception e)
		{
			MartusLogger.logException(e);
			showNotifyDialog("UnableToSaveAttachment");
		}
	}

	private void saveAttachmentFromDatabase(AttachmentProxy proxy, File outputFile)
			throws IOException, InvalidBase64Exception, InvalidPacketException,
			SignatureVerificationException, WrongPacketTypeException,
			CryptoException
	{
		ClientBulletinStore store = getApp().getStore();
		ReadableDatabase db = store.getDatabase();
		BulletinLoader.extractAttachmentToFile(db, proxy, store.getSignatureVerifier(), outputFile);
	}
	
	private void savePendingAttachment(File source, File target) throws IOException
	{
		Files.copy(source.toPath(), target.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
	}

	private boolean viewAttachmentInternally(AttachmentProxy proxy) throws Exception
	{
		File attachmentFileToView = ViewAttachmentHandler.obtainFileForAttachment(proxy, getMainWindow().getStore());
		AttachmentViewController attachmentViewer = new AttachmentViewController(getMainWindow(), attachmentFileToView);

		if(!attachmentViewer.canViewInProgram())
			return false;
		
		showDialogWithNoButtons("ViewAttachment", attachmentViewer);
		return true;
	}

	private void removeSelectedAttachment()
	{
		AttachmentTableRowData selectedItem = getSelectedAttachmentRowData();
		if(selectedItem == null)
		{
			MartusLogger.log("Attempted to remove Attachment with nothing selected");
			return;
		}
		attachmentsTable.getItems().remove(selectedItem);
	}

	private AttachmentTableRowData getSelectedAttachmentRowData()
	{
		TableViewSelectionModel<AttachmentTableRowData> selectionModel = attachmentsTable.getSelectionModel();
		AttachmentTableRowData selectedItem = selectionModel.getSelectedItem();
		return selectedItem;
	}

	@FXML
	private void onAddAttachment(ActionEvent event) 
	{
		doAction(new AddAttachmentAction(this));
	}

	public void addAttachment(File fileToAdd)
	{
		AttachmentProxy attachmentProxy = new AttachmentProxy(fileToAdd);
		AttachmentTableRowData newAttachmentRowData = new AttachmentTableRowData(attachmentProxy, getApp().getStore().getDatabase());	
		
		attachmentsTable.getItems().add(newAttachmentRowData);
	}
	
	@Override
	public String getFxmlLocation()
	{
		return "landing/bulletins/FxAttachments.fxml";
	}
	
	final private String REMOVE_ATTACHMENT_IMAGE_PATH = "/org/martus/client/swingui/jfx/images/trash.png";
	final private String VIEW_ATTACHMENT_IMAGE_PATH = "/org/martus/client/swingui/jfx/images/view_attachment.png";
	final private String SAVE_ATTACHMENT_IMAGE_PATH = "/org/martus/client/swingui/jfx/images/save_attachment.png";

	@FXML 
	private TableView attachmentsTable;
	
	@FXML
	protected TableColumn<AttachmentTableRowData, String> nameColumn;

	@FXML
	protected TableColumn<AttachmentTableRowData, String> sizeColumn;	
	
	@FXML
	protected TableColumn<TableRowData, ReadOnlyProperty<Image>> thumbnailColumn;
	
	@FXML
	protected TableColumn<TableRowData, Boolean> viewColumn;

	@FXML
	protected TableColumn<TableRowData, Boolean> removeColumn;
	
	@FXML
	protected TableColumn<TableRowData, Boolean> saveColumn;
	
	@FXML
	private Button addAttachmentButton;

	private FxBulletin bulletin;
}
