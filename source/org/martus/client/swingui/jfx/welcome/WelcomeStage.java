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
package org.martus.client.swingui.jfx.welcome;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.ContentController;
import org.martus.client.swingui.jfx.FxNonWizardStage;
import org.martus.client.swingui.jfx.FxScene;

public class WelcomeStage extends FxNonWizardStage
{
	public WelcomeStage(UiMainWindow mainWindow) throws Exception
	{
		super(mainWindow);
		
		setShellController(new WelcomeShellController(getMainWindow()));
		setCurrentController(new FxWelcomeController(getMainWindow()));
	}

	@Override
	public void showCurrentScene() throws Exception
	{
		ContentController contentPaneController = getCurrentController();

		showCurrentPage(contentPaneController);
	}

	@Override
	protected FxScene createScene() throws Exception
	{
		return new WelcomeScene(getExternalFxmlDirectory());
	}
}
