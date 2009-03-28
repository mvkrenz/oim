package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.ContactRankRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class ContactRankModel extends DBModel {

	public static HashMap<Integer, ContactRankRecord> cache = null;
		
	public ContactRankModel(Connection _con, Authorization _auth) {
		super(_con, _auth);
	}
	
	public ContactRankRecord get(int id) throws SQLException
	{
		fillCache();
		Integer key = new Integer(id);
		if(cache.containsKey(key)) {
			return cache.get(key);
		}
		
		log.warn("Couldn't find contact_type where id = " + id);
		return null;
	}
	
	public Collection<ContactRankRecord> getAll() throws SQLException {
		fillCache();
		return cache.values();
	}
	private void fillCache() throws SQLException
	{
		if(cache == null) {
			PreparedStatement stmt = null;
			cache = new HashMap<Integer, ContactRankRecord>();
	
			String sql = "SELECT * from contact_rank";
			stmt = con.prepareStatement(sql); 
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				ContactRankRecord rec = new ContactRankRecord(rs);
				cache.put(rec.id, rec);
			}
		}
	}
}
