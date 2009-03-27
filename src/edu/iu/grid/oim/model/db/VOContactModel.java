package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.VOContactRecord;

public class VOContactModel extends DBModel {
    static Logger log = Logger.getLogger(VOContactModel.class); 
	public static HashMap<Integer/*vo_id*/, ArrayList<VOContactRecord>> cache = null;
	
	public VOContactModel(Connection _con, Authorization _auth) {
		super(_con, _auth);
		// TODO Auto-generated constructor stub
	}
	
	public HashMap<Integer/*type_id*/, ArrayList<Integer/*person_id*/>> get(Integer vo_id) throws AuthorizationException, SQLException
	{	
		fillCache();

		HashMap<Integer, ArrayList<Integer>> list = new HashMap();
		if(cache.containsKey(vo_id)) {
			ArrayList<VOContactRecord> recs = cache.get(vo_id);
			for(VOContactRecord rec : recs) {
				//group records by type_id and create lists of person_id
				ArrayList<Integer> a = null;
				if(!list.containsKey(rec.contact_type_id)) {
					//never had this type
					a = new ArrayList<Integer>();
					list.put(rec.contact_type_id, a);
				} else {
					a = list.get(rec.contact_type_id);
				}	
				a.add(rec.contact_id);
			}
			return list;
		}
		
		log.warn("Couldn't find any record where vo_id = " + vo_id);
		return list;
	}
	
	synchronized private void fillCache() throws SQLException
	{
		if(cache != null) {
			return;
		}
		
		cache = new HashMap();
		
		String sql = "SELECT * FROM vo_contact";
		PreparedStatement stmt = null;
		stmt = con.prepareStatement(sql); 
		ResultSet rs = stmt.executeQuery();

		while(rs.next()) {
			VOContactRecord rec = new VOContactRecord(rs);
			
			//group records by vo_id and put it in the cache
			ArrayList<VOContactRecord> a = null;
			if(!cache.containsKey(rec.vo_id)) {
				//never had this type
				a = new ArrayList<VOContactRecord>();
				cache.put(rec.vo_id, a);
			} else {
				a = cache.get(rec.vo_id);
			}
			a.add(rec);
		}
	}
}
