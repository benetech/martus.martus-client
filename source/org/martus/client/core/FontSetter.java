package org.martus.client.core;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.martus.common.MartusLogger;

/**
 * @author roms
 *         Date: 3/14/13
 */
public class FontSetter {

	/**
	 *
	 * @param fontName String name of desired font
	 */
	public static void setUIFont(String fontName)
	{
		createOriginalDefaults();
		Enumeration keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource)
			{
				if (null == originalDefaults.get(key))
				{
					originalDefaults.put(key, value);
				}
				FontUIResource orig = (FontUIResource) value;
				Font font = new Font(fontName, orig.getStyle(), orig.getSize());
				UIManager.put(key, new FontUIResource(font));
			}
		}
	 }

	public static void restoreDefaults()
	{
		MartusLogger.log("FontSetter.restorDefaults()");
		Enumeration keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements())
		{
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource)
			{
				UIManager.put(key, originalDefaults.get(key));
			}
		}
	}

	private static void createOriginalDefaults()
	{
		if (null == originalDefaults)
		{
			originalDefaults = new HashMap<Object,Object>();
		}
	}

	private static Map<Object, Object> originalDefaults;
}
