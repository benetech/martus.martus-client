package org.martus.client.swingui.bulletincomponent;

import org.martus.common.clientside.UiBasicLocalization;

public class HeadQuartersTableModelView extends HeadQuartersTableModel
{
	public HeadQuartersTableModelView(UiBasicLocalization localizationToUse)
	{
		super(localizationToUse);
		COLUMN_LABEL = columnCount++;
	}
}
