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
package org.martus.client.swingui.jfx.setupwizard.tasks;

import org.martus.client.core.MartusApp;
import org.martus.clientside.ClientSideNetworkGateway;

public class IsAvailableServerCompliantTask extends ServerCallTask
{
	public IsAvailableServerCompliantTask(MartusApp appToUse)
	{
		this(appToUse, appToUse.getCurrentNetworkInterfaceGateway());
	}

	public IsAvailableServerCompliantTask(MartusApp appToUse, ClientSideNetworkGateway gatewayToUse)
	{
		super(appToUse, gatewayToUse);
		
		gateway = gatewayToUse;
	}
	
	public boolean isAvailable()
	{
		return isAvailable;
	}
	
	public String getComplianceStatement()
	{
		return complianceStatement;
	}
	
	@Override
	public int getMaxSeconds()
	{
		return getInterface().getTimeoutSecondsForGetServerInfo();
	}

	@Override
	protected Void call() throws Exception
	{
		complianceStatement = app.getServerCompliance(getGateway());
		isAvailable = getApp().isSSLServerAvailable(gateway);
		
		return null;
	}

	private ClientSideNetworkGateway gateway;
	private boolean isAvailable;
	private String complianceStatement;
}
