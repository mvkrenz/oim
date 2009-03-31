package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.FacilityRecord;

public class FacilityModel extends DBModel {
    static Logger log = Logger.getLogger(FacilityModel.class);  
	public static HashMap<Integer, FacilityRecord> cache = null;
	
    public FacilityModel(
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
		    if (stmt.execute("SELECT * FROM facility")) {
		    	 rs = stmt.getResultSet();
		    	 while(rs.next()) {
		    		 FacilityRecord rec = new FacilityRecord(rs);
		    		 cache.put(rec.id, rec);
		    	 }
		    }
		}
	}
	public void emptyCache()
	{
		cache = null;
	}
	
	HashMap<Integer, FacilityRecord> getAll() {
		return cache;
	}
}
