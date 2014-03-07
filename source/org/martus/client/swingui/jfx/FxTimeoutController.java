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

import java.net.URL;
import java.util.ResourceBundle;
import java.util.TimerTask;

import org.martus.client.swingui.UiMainWindow;
import org.martus.common.MartusUtilities;

import javafx.concurrent.Task;


public class FxTimeoutController extends FxBackgroundActivityController
{
	public FxTimeoutController(UiMainWindow mainWindowToUse, String titleToUse, String messageToUse, Task taskToUse, int maxSecondsToCompleteTaskToUse)
	{
		super(mainWindowToUse, titleToUse, messageToUse, taskToUse);
		maxSecondsToCompleteTask = maxSecondsToCompleteTaskToUse;
	}

	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		backgroundTick = new TimeoutTimerTask();
		MartusUtilities.startTimer(backgroundTick, BACKGROUND_TIMEOUT_CHECK_EVERY_SECOND);
	}

	class TimeoutTimerTask extends TimerTask
	{
		public void run()
		{
			double percentComplete = (double)currentNumberOfSecondsCompleted/(double)maxSecondsToCompleteTask;
			double percentLeftBeforeTimedOut = 1.0 - percentComplete;
			updateProgressBar(percentLeftBeforeTimedOut);
			++currentNumberOfSecondsCompleted;
			if(currentNumberOfSecondsCompleted >= maxSecondsToCompleteTask)
				forceCloseDialog();
		}
	}

	@Override
	public void forceCloseDialog()
	{
		backgroundTick.cancel();
		backgroundTick = null;
		super.forceCloseDialog();
	}

	@Override
	public void cancelPressed()
	{
		forceCloseDialog();
	}
	protected int maxSecondsToCompleteTask;
	protected int currentNumberOfSecondsCompleted;
	protected TimeoutTimerTask backgroundTick;

	final int BACKGROUND_TIMEOUT_CHECK_EVERY_SECOND = 1000;
}
