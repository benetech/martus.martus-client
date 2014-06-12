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
package org.martus.client.swingui.jfx.landing;

import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class BulletinTableData
{
	public BulletinTableData(Bulletin bulletin, boolean onServer, MiniLocalization localization)
	{
		title = new SimpleStringProperty(bulletin.get(Bulletin.TAGTITLE));
		author = new SimpleStringProperty(bulletin.get(Bulletin.TAGAUTHOR));
		long dateLastSaved = bulletin.getBulletinHeaderPacket().getLastSavedTime();
		dateSaved = new SimpleStringProperty(localization.formatDateTime(dateLastSaved));
		this.onServer = new SimpleBooleanProperty(onServer);
	}
	
	public String getTitle()
	{
		return title.get();
	}

    public SimpleStringProperty titleProperty() 
    { 
        return title; 
    }
	
	public String getAuthor()
	{
		return author.get();
	}

    public SimpleStringProperty authorProperty() 
    { 
        return author; 
    }

    public String getDateSaved()
	{
		return dateSaved.get();
	}
	
    public SimpleStringProperty dateSavedProperty() 
    { 
        return dateSaved; 
    }

	public boolean isOnServer()
	{
		return onServer.get();
	}

    public SimpleBooleanProperty onServerProperty() 
    {
    		return onServer;
    }

    
    static public final String TITLE_PROPERTY_NAME = "title";
    static public final String AUTHOR_PROPERTY_NAME = "author";
    static public final String DATE_SAVDED_PROPERTY_NAME = "dateSaved";
    static public final String ON_SERVER_PROPERTY_NAME = "onServer";
    
    private final SimpleStringProperty title;
	private final SimpleStringProperty author;
	private final SimpleStringProperty dateSaved;
	private final SimpleBooleanProperty onServer;

}
