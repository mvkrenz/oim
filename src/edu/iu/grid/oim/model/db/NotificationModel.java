package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.LogRecord;
import edu.iu.grid.oim.model.db.record.NotificationRecord;

public class NotificationModel extends SmallTableModelBase<NotificationRecord> {
    static Logger log = Logger.getLogger(NotificationModel.class);  
	public static HashMap<Integer, NotificationRecord> cache = null;
	
    public NotificationModel(
    		java.sql.Connection _con, 
    		edu.iu.grid.oim.lib.Authorization _auth) 
    {
    	super(_con, _auth, "notification");
    }
    NotificationRecord createRecord(ResultSet rs) throws SQLException
	{
		return new NotificationRecord(rs);
	}
  
}
