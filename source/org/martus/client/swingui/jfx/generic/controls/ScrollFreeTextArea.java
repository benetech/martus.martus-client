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

package org.martus.client.swingui.jfx.generic.controls;

import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/*
 * NOTE: This class is not yet working. It is fairly close in many ways, but 
 * if you hit enter, and then backspace, a vertical scrollbar appears. 
 * There is some debugging code specifically to try to figure out a way 
 * around that, but so far nothing has worked. Calls to applyCss, 
 * requestLayout, and setNeedsLayout have been ineffective. Perhaps they 
 * need to be called at a different moment. 
 * 
 * The code was originally derived from the code here, but has been 
 * modified to the point that it is probably not recognizable:
 * https://javafx-demos.googlecode.com/svn-history/r13/trunk/javafx-demos/
 *   src/main/java/com/ezest/javafx/components/freetextfield/ScrollFreeTextArea.java
 */
public class ScrollFreeTextArea extends StackPane
{
	public ScrollFreeTextArea()
	{
		super();
		configure();
	}

	public ScrollFreeTextArea(String text)
	{
		super();
		configure();
		textArea.setText(text);
	}

	public String getText()
	{
		return textProperty().getValue();
	}

	public StringProperty textProperty()
	{
		return textArea.textProperty();
	}

	private void configure()
	{
		setAlignment(Pos.TOP_LEFT);

		textArea = new TextArea();
		textArea.setWrapText(true);
		textArea.getStyleClass().add("scroll-free-text-area");
		
		disableVerticalScrollBar();  

		text = new Text();
		text.textProperty().bind(textArea.textProperty().concat("\n"));

		flow = new TextFlow(text);
//		labelForSizing.setWrapText(true);
		flow.prefWidthProperty().bind(textArea.widthProperty());
		flow.heightProperty().addListener((observable, oldValue, newValue) -> flowHeightChanged(newValue));
		
		textArea.prefHeightProperty().bind(flow.prefHeightProperty().add(12));
		textArea.minHeightProperty().bind(textArea.prefHeightProperty());
		textArea.heightProperty().addListener((observable, oldValue, newValue) -> textAreaHeightChanged(newValue));
		textArea.textProperty().addListener((observable, oldValue, newValue) -> textAreaTextChanged(newValue));
		
//		lblContainer = new StackPane(labelForSizing);
//		lblContainer.setAlignment(Pos.TOP_LEFT);
//		lblContainer.setPadding(new Insets(12,7,7,7));
//		// Binding the container width to the TextArea width.
//		lblContainer.maxWidthProperty().bind(textArea.widthProperty());

//		textArea.textProperty().addListener(new ChangeListener<String>()
//		{
//			@Override
//			public void changed(
//					ObservableValue<? extends String> paramObservableValue,
//					String paramT1, String value)
//			{
//				layoutForNewLine(getText());
//			}
//		});

//		flow.heightProperty().addListener(new ChangeListener<Number>()
//		{
//			@Override
//			public void changed(
//					ObservableValue<? extends Number> paramObservableValue,
//					Number paramT1, Number paramT2)
//			{
//				layoutForNewLine(getText());
//			}
//		});

//		getChildren().addAll(
//				GroupBuilder.create().children(lblContainer).build(), textArea);
		getChildren().addAll(flow, textArea);
	}

	public void disableVerticalScrollBar()
	{
		// NOTE: The following undocumented call came from
		// https://community.oracle.com/message/10978956
		// Unfortunately it doesn't work, because the scrollbar doesn't actually exist yet
//		ScrollBar scrollBarv = (ScrollBar)textArea.lookup(".scroll-bar:vertical");
//		scrollBarv.setDisable(true);
	}
	
	private void flowHeightChanged(Number newValue)
	{
		System.out.println("Flow height changed:");
		System.out.println("  TA Height now " + textArea.getHeight());
		System.out.println("  Flow Height now " + flow.getHeight());
	}

	private void textAreaTextChanged(String newValue)
	{
		System.out.println("TA text changed:");
		System.out.println("  TextArea text now " + textArea.getText().length());
		System.out.println("  Text text now " + text.getText().length());
	}

	private void textAreaHeightChanged(Number newValue)
	{
		System.out.println("TA height changed:");
		System.out.println("  TA Height now " + textArea.getHeight());
		System.out.println("  Flow Height now " + flow.getHeight());
	}

	private TextFlow flow;
	private Text text;
//	private Label labelForSizing;
//	private StackPane lblContainer;
	private TextArea textArea;
}
