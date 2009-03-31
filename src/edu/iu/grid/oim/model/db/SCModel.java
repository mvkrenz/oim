package edu.iu.grid.oim.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.SCContactRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class SCModel extends DBModel {
    static Logger log = Logger.getLogger(SCModel.class);  
    
    public SCModel(java.sql.Connection con, Authorization auth) 
    {
    	super(con, auth);
    }    

	private static HashMap<Integer/*sc_id*/, SCRecord> cache = null;
	public void fillCache() throws SQLException
	{
		if(cache == null) {
			cache = new HashMap();
			ResultSet rs = null;
			Statement stmt = con.createStatement();
		    if (stmt.execute("SELECT * FROM sc")) {
		    	 rs = stmt.getResultSet();
		    }
		    while(rs.next()) {
		    	SCRecord rec = new SCRecord(rs);
		    	cache.put(rec.id, rec);
		    }	
		}
	}
	public void emptyCache() //used after we do insert/update
    {
	 	cache = null;
    }
	
	public Collection<SCRecord> getAllEditable() throws SQLException
	{	
		fillCache();
   
	    if(auth.allows("admin_sc")) {
	    	//admin can edit all scs
	    	return cache.values();
	    } else {
	    	//only select record that is editable
			ArrayList<SCRecord> list = new ArrayList();
	    	HashSet<Integer> accessible = getEditableIDs();
		    for(SCRecord rec : cache.values()) {
		    	if(accessible.contains(rec.id)) {
		    		list.add(rec);
		    	}
		    }	    	
		    return list;
	    }
	}
	
	//returns all record id that the user has access to
	private HashSet<Integer> getEditableIDs() throws SQLException
	{
		HashSet<Integer> list = new HashSet<Integer>();
		ResultSet rs = null;

		PreparedStatement stmt = null;

		String sql = "SELECT * FROM sc_contact WHERE contact_id = ?";
		stmt = con.prepareStatement(sql); 
		stmt.setInt(1, auth.getContactID());

		rs = stmt.executeQuery();
		while(rs.next()) {
			SCContactRecord rec = new SCContactRecord(rs);
			list.add(rec.sc_id);
		}
		
		return list;
	}
	
	public Collection<SCRecord> getAll() throws SQLException {
		fillCache();
		return cache.values();
	}
	public SCRecord get(int sc_id) throws SQLException
	{
		fillCache();
		return cache.get(sc_id);
	}
}

