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
package org.martus.client.swingui.jfx.landing.bulletins;

import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class BulletinTableRowData
{
	public BulletinTableRowData(Bulletin bulletin, boolean onServer, MiniLocalization localization)
	{
		uid = bulletin.getUniversalId();
		title = new SimpleStringProperty(bulletin.get(Bulletin.TAGTITLE));
		author = new SimpleStringProperty(bulletin.get(Bulletin.TAGAUTHOR));
		long dateLastSaved = bulletin.getBulletinHeaderPacket().getLastSavedTime();
		dateSaved = new SimpleStringProperty(localization.formatDateTime(dateLastSaved));
		this.onServer = new SimpleBooleanProperty(onServer);
		viewBulletin = new SimpleStringProperty("View");
		editBulletin = new SimpleStringProperty("Edit");
	}
	
	public UniversalId getUniversalId()
	{
		return uid;
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

    public String getViewBulletin()
	{
		return viewBulletin.get();
	}
	
    public SimpleStringProperty viewBulletinProperty() 
    { 
        return viewBulletin; 
    }

    public String getEditBulletin()
 	{
 		return editBulletin.get();
 	}
 	
     public SimpleStringProperty editBulletinProperty() 
     { 
         return editBulletin; 
     }

    static public final String TITLE_PROPERTY_NAME = "title";
    static public final String AUTHOR_PROPERTY_NAME = "author";
    static public final String DATE_SAVDED_PROPERTY_NAME = "dateSaved";
    static public final String ON_SERVER_PROPERTY_NAME = "onServer";
    static public final String VIEW_BULLETIN_PROPERTY_NAME = "viewBulletin";
    static public final String EDIT_BULLETIN_PROPERTY_NAME = "editBulletin";
    
    private final SimpleStringProperty title;
	private final SimpleStringProperty author;
	private final SimpleStringProperty dateSaved;
	private final SimpleBooleanProperty onServer;
	private final SimpleStringProperty viewBulletin;
	private final SimpleStringProperty editBulletin;
	
	private final UniversalId uid;
}
