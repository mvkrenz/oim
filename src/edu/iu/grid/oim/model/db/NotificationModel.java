package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.ActionRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.LogRecord;
import edu.iu.grid.oim.model.db.record.NotificationRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.VORecord;

public class NotificationModel extends SmallTableModelBase<NotificationRecord> {
    static Logger log = Logger.getLogger(NotificationModel.class);  
	public static HashMap<Integer, NotificationRecord> cache = null;
	
    public NotificationModel(Authorization _auth) 
    {
    	super(_auth, "notification");
    }
    NotificationRecord createRecord(ResultSet rs) throws SQLException
	{
		return new NotificationRecord(rs);
	}
	public ArrayList<NotificationRecord> getAll() throws SQLException
	{
		ArrayList<NotificationRecord> list = new ArrayList<NotificationRecord>();
		for(RecordBase it : getCache()) {
			list.add((NotificationRecord)it);
		}
		return list;
	}
	public NotificationRecord get(int id) throws SQLException {
		NotificationRecord keyrec = new NotificationRecord();
		keyrec.id = id;
		return get(keyrec);
	}
}
