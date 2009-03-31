package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.NotificationRecord;

public class NotificationModel extends DBModel {
    static Logger log = Logger.getLogger(NotificationModel.class);  
	public static HashMap<Integer, NotificationRecord> cache = null;
	
    public NotificationModel(
    		java.sql.Connection _con, 
    		edu.iu.grid.oim.lib.Authorization _auth) 
    {
    	super(_con, _auth);
    }
    
	public void fillCache() throws SQLException
	{
		if(cache == null) {
			cache = new HashMap();
			
			ResultSet rs = null;
			Statement stmt = con.createStatement();
		    if (stmt.execute("SELECT * FROM notification")) {
		    	 rs = stmt.getResultSet();
		    	 while(rs.next()) {
		    		 NotificationRecord rec = new NotificationRecord(rs);
		    		 cache.put(rec.id, rec);
		    	 }
		    }
		}
	}
	public void emptyCache()
	{
		cache = null;
	}
	
	public HashMap<Integer, NotificationRecord> getAll() throws SQLException {
		fillCache();
		return cache;
	}
}
