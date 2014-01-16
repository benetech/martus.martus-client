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
package org.martus.client.swingui.jfx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.martus.client.swingui.dialogs.UiDialogLauncher;
import org.martus.clientside.UiLocalization;
import org.martus.common.fieldspec.ChoiceItem;

public class FxPureJavaSelectLanguageScene extends MartusPureJavaScene implements MartusController
{
	public static class Factory extends MartusSceneFactory
	{
		public Factory(UiLocalization localizationToUse, String defaultLanguageCodeToUse)
		{
			defaultLanguageCode = defaultLanguageCodeToUse;
			localization = localizationToUse;
		}
		
		@Override
		public MartusScene createScene()
		{
			FxPureJavaSelectLanguageScene scene = new FxPureJavaSelectLanguageScene(localization, defaultLanguageCode);
			controller = scene;
			return scene;
		}
		
		@Override
		public MartusController getController()
		{
			return controller;
		}
		
		private UiLocalization localization;
		private String defaultLanguageCode;
		private MartusController controller;
	
	}
	
	public FxPureJavaSelectLanguageScene(UiLocalization localizationToUse, String defaultLanguageCodeToUse)
	{
		defaultLanguageCode = defaultLanguageCodeToUse;
		localization = localizationToUse;

		BorderPane basePane = new BorderPane();
		basePane.setLeft(constructLeftSide());
		basePane.setCenter(constructRightSide());
        getRootGroup().getChildren().add(basePane);

        String css = this.getClass().getResource("background.css").toExternalForm();
		getStylesheets().add(css);			

	}

	public String GetLanguage()
	{
		return languageCodeChosen;
	}
	
	private VBox constructRightSide()
	{
		HBox logoHeader = new HBox();
		logoHeader.setPrefWidth(RIGHT_COLUMN_MAX_WIDTH);
//			logoHeader.setAlignment(Pos.CENTER_RIGHT);
		Image logo = new Image(UiDialogLauncher.class.getResourceAsStream("MartusLogo.png"));
		logoHeader.getChildren().add(new ImageView(logo));
		
		Label rightDocumentationTitle = new Label("Securing your digital documentation");
		rightDocumentationTitle.setPrefWidth(RIGHT_COLUMN_MAX_WIDTH);
		rightDocumentationTitle.setAlignment(Pos.CENTER_RIGHT);

		Label rightChooseLanguage = new Label(localization.getFieldLabel("FieldTypeLANGUAGE"));
		
		ChoiceBox<String> languageChoice = new ChoiceBox<String>();
		GridPane languagesAvailable = new GridPane();
//			languagesAvailable.setPadding(new Insets(MARGIN_SPACING));
		
		addAllUILanguagesAvailableAndListenerToChoiceBoxAndGrid(languageChoice, languagesAvailable);
		
		Button getStartedButton = new Button("Get Started");
		addButtonEventHandler(getStartedButton);						
		
		HBox buttonBox = new HBox();
		buttonBox.setPrefWidth(RIGHT_COLUMN_MAX_WIDTH);
		buttonBox.setAlignment(Pos.CENTER_RIGHT);
		buttonBox.getChildren().add(getStartedButton);
		
		VBox rightColumn = new VBox();
		rightColumn.getStyleClass().add("RightPanel");

		ObservableList<Node> children = rightColumn.getChildren();
//			rightColumn.setPadding(new Insets(MARGIN_SPACING));
		rightColumn.setPrefWidth(RIGHT_COLUMN_MAX_WIDTH);
		children.add(logoHeader);
		children.add(rightDocumentationTitle);
//			addSpaces(rightColumn, WHITESPACE_COUNT);
		children.add(rightChooseLanguage);
//			addSpaces(rightColumn, 1);
		children.add(languageChoice);
//			addSpaces(rightColumn, 1);
		children.add(languagesAvailable);
//			addSpaces(rightColumn, 3);
		children.add(buttonBox);
		return rightColumn;
	}

	private void addButtonEventHandler(Button getStartedButton)
	{
		getStartedButton.setOnAction(
				new EventHandler<ActionEvent>() 
				{
			   		//@Override 
			   		public void handle(ActionEvent e) 
			   		{
			   			getShell().dispose();
			   		}
				}
				);
	}

	private VBox constructLeftSide()
	{
		Image logo = new Image(UiDialogLauncher.class.getResourceAsStream("MartusLogo.png"));

		VBox leftColumn = new VBox();
		leftColumn.getStyleClass().add("LeftPanel");
		leftColumn.setPrefWidth(LEFT_COLUMN_MAX_WIDTH);
		Label leftGetStartedTitle = new Label("Getting started with Martus is easy!");
		leftGetStartedTitle.getStyleClass().add("LeftTitle");
		setLabelPropertiesForWrappingText(leftColumn, leftGetStartedTitle);
		
		ObservableList<Node> children = leftColumn.getChildren();
		children.add(leftGetStartedTitle);
		
		GridPane grid = new GridPane();
		grid.getStyleClass().add("LeftGrid");

		String entry1 = "Create an Account";
		String description1 = "You will need to enter a user name and secure password.";
		addRow(grid, 1, logo, entry1, description1);

		String entry2 = "Use Tor (optional)";
		String description2 = "Tor is currently the best tool available to hide your network activities.";
		addRow(grid, 2, logo, entry2, description2);
		
		String entry3 = "Setup Server Storage";
		String description3 = "Back up your bulletons on a secure server.";
		addRow(grid, 3, logo, entry3, description3);

		String entry4 = "Add Contacts";
		String description4 = "Share your bulletins with other Martus users and receive bulletins from others.";
		addRow(grid, 4, logo, entry4, description4);

		String entry5 = "Import Forms";
		String description5 = "Start with form templates to upload bulletins quickly.";
		addRow(grid, 5, logo, entry5, description5);

		String entry6 = "Backup Your Key";
		String description6 = "Files encrypted with the Martus key can only be opened with that key.  Back it up to be sure you can always get your documents.";
		addRow(grid, 6, logo, entry6, description6);
		
		children.add(grid);
		
		return leftColumn;
	}

	public void addRow(GridPane grid, int row, Image icon,
			String entryText, String descriptionText)
	{
		ImageView iconView = new ImageView(icon);
		iconView.getStyleClass().add("Icon");
		resizeImage(iconView);

		Label entryLabel = new Label(entryText);
		entryLabel.getStyleClass().add("Entry");
		Label descriptionLabel = new Label(descriptionText);
		descriptionLabel.getStyleClass().add("Description");
		descriptionLabel.setWrapText(true);

		VBox box = new VBox();
		box.getChildren().add(entryLabel);
		box.getChildren().add(descriptionLabel);

		grid.add(iconView, 1, row);
		grid.add(box, 2, row);
	}

	private void setLabelPropertiesForWrappingText(VBox leftColumn,
			Label leftGetStartedTitle)
	{
		leftGetStartedTitle.setWrapText(true);
		leftGetStartedTitle.setPrefWidth(leftColumn.getPrefWidth());
	}

	private void resizeImage(ImageView logoView1)
	{
		logoView1.setFitWidth(40);			
		logoView1.setFitHeight(40);
	}

	private void addAllUILanguagesAvailableAndListenerToChoiceBoxAndGrid(ChoiceBox<String> languageChoice, GridPane languagesAvailableGrid)
	{
		allUILanguagesSupported = localization.getUiLanguages();
		int selectedIndex = 0;
		for(int i = 0; i < allUILanguagesSupported.length; ++i)
		{
			String currentCode = allUILanguagesSupported[i].getCode();
			localization.setCurrentLanguageCode(currentCode);
			String languageName = localization.getLanguageName(currentCode);

			languageChoice.getItems().add(languageName);
			if(currentCode.matches(defaultLanguageCode))
				selectedIndex = i;
		}
		languageChoice.getSelectionModel().select(selectedIndex);
		languageChoice.getSelectionModel().selectedIndexProperty().addListener(languageComboBoxListener());

		
		languagesAvailableGrid.getColumnConstraints().add(new ColumnConstraints(200));
		languagesAvailableGrid.getColumnConstraints().add(new ColumnConstraints(200));
		int nCurrentLanguageIndex = 0;
		for(int row = 0; row < 12; ++row)
		{
			for(int column = 0; column <2; ++column)
			{
				if(nCurrentLanguageIndex < allUILanguagesSupported.length)
				{
					String currentCode = allUILanguagesSupported[nCurrentLanguageIndex].getCode();
					localization.setCurrentLanguageCode(currentCode);
					String languageName = localization.getLanguageName(currentCode);
					++nCurrentLanguageIndex;
					Label staticLanguageName = new Label(languageName);
					languagesAvailableGrid.add(staticLanguageName, column, row);
				}
			}
			if(nCurrentLanguageIndex >= allUILanguagesSupported.length)
				break;
		}
		localization.setCurrentLanguageCode(defaultLanguageCode);

	}

	private ChangeListener<Number> languageComboBoxListener()
	{
		return new ChangeListener<Number>() 
		 {
		   // @Override
		   public void changed(ObservableValue<? extends Number> ov,
		    Number old_value, Number new_value) 
		    {
		    	languageCodeChosen = allUILanguagesSupported[new_value.intValue()].getCode();
		    	localization.setCurrentLanguageCode(languageCodeChosen);
		    }
		 };
	}

//		private void addSpaces(VBox item, int numberOfSpaces)
//		{
//			for(int i = 0; i < numberOfSpaces; ++i)
//			{
//				item.getChildren().add(new Label(" "));
//			}
//		}

	private final int LEFT_COLUMN_MAX_WIDTH = 350;
	private final int RIGHT_COLUMN_MAX_WIDTH = 450;
//	private final int MARGIN_SPACING = 30;
//	private final int WHITESPACE_COUNT = 5;

	String languageCodeChosen;
	UiLocalization localization;
	String defaultLanguageCode;
	ChoiceItem[] allUILanguagesSupported;
}
