package org.martus.client.swingui;

import javax.swing.text.JTextComponent;

import org.martus.clientside.Burmese;

public class UiFontEncodingHelper
{
	public UiFontEncodingHelper(boolean useZawgyi)
	{
		this.useZawgyi = useZawgyi;
	}

	public void setDisplayableText(JTextComponent textField, String value)
	{
		if (useZawgyi)
			value = Burmese.getDisplayable(value);
		textField.setText(value);
	}

	public String getDisplayable(String value)
	{
		if (useZawgyi)
			value = Burmese.getDisplayable(value);
		return value;
	}

	public String getStorable(String value)
	{
		if (useZawgyi)
			value = Burmese.getStorable(value);
		return value;
	}

	private boolean useZawgyi;
}
