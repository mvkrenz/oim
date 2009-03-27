package edu.iu.grid.oim.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;

public class ContactModel extends DBModel {
    static Logger log = Logger.getLogger(ContactModel.class);  
	public static HashMap<Integer, ContactRecord> cache = null;
    
    public ContactModel(
    		java.sql.Connection _con, 
    		edu.iu.grid.oim.lib.Authorization _auth) 
    {
    	super(_con, _auth);
    }
    
	public HashMap<Integer, ContactRecord> getAll() throws SQLException
	{
		fillCache();
		return cache;
	}
	
	public ContactRecord get(int id) throws SQLException
	{
		fillCache();

		Integer key = new Integer(id);
		if(cache.containsKey(key)) {
			return cache.get(key);
		}
		log.warn("Couldn't find contact where id = " + id);
		return null;
	}
	
	synchronized private void fillCache() throws SQLException
	{
		if(cache != null) {
			return;
		}
		PreparedStatement stmt = null;
		cache = new HashMap<Integer, ContactRecord>();

		String sql = "SELECT * from contact";
		stmt = con.prepareStatement(sql); 
		ResultSet rs = stmt.executeQuery();
		while(rs.next()) {
			ContactRecord rec = new ContactRecord(rs);
			cache.put(rec.id, rec);
		}
	}
}
