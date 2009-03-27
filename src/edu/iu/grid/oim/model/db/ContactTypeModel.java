package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class ContactTypeModel extends DBModel {

	public static HashMap<Integer, ContactTypeRecord> cache = null;
		
	public ContactTypeModel(Connection _con, Authorization _auth) {
		super(_con, _auth);
	}
	
	public ContactTypeRecord get(int id) throws SQLException
	{
		fillCache();
		Integer key = new Integer(id);
		if(cache.containsKey(key)) {
			return cache.get(key);
		}
		
		log.warn("Couldn't find contact_type where id = " + id);
		return null;
	}
	
	synchronized private void fillCache() throws SQLException
	{
		if(cache != null) {
			return;
		}
		PreparedStatement stmt = null;
		cache = new HashMap<Integer, ContactTypeRecord>();

		String sql = "SELECT * from contact_type";
		stmt = con.prepareStatement(sql); 
		ResultSet rs = stmt.executeQuery();
		while(rs.next()) {
			ContactTypeRecord rec = new ContactTypeRecord(rs);
			cache.put(rec.id, rec);
		}
	}
}
