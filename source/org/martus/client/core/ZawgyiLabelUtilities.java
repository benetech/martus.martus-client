package org.martus.client.core;

import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.swing.FontHandler;

/**
 * @author roms
 *         Date: 10/21/13
 */
public class ZawgyiLabelUtilities
{
	public static String getDisplayableLabel(FieldSpec spec, MiniLocalization localization)
	{
		boolean custom = StandardFieldSpecs.isCustomFieldTag(spec.getTag());
		if (!custom)
			return localization.getFieldLabel(spec.getTag());

		UiFontEncodingHelper fontHelper = new UiFontEncodingHelper(FontHandler.isDoZawgyiConversion());
		return fontHelper.getDisplayable(spec.getLabel());
	}
}
