package edu.iu.grid.oim.model.db.record;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.w3c.dom.Document;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.webif.divex.DivEx;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.notification.NotificationBase;

public class NotificationRecord extends RecordBase {
	@Key public Integer id;
	public String notification;
	public Integer dn_id;
	
	public NotificationRecord(ResultSet rs) throws SQLException { super(rs); }
	public NotificationRecord() {}	
	
	private NotificationBase notification_cache = null;
	public NotificationBase getNotification()
	{
		if(notification_cache == null) {
			notification_cache = NotificationBase.factory(this);
		} 
		return  notification_cache;
	}
	public String getTitle()
	{
		NotificationBase notification = getNotification();
		return notification.getTitle();
	}
	//TODO - when I set notification object, I need to update serialized version of that to be updated as well.
	//but how? I think the only way to ensure this is to only allow update during in a constructor...
}
