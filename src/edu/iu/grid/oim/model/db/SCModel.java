package edu.iu.grid.oim.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
    
	public ResultSet getAll() throws AuthorizationException, SQLException
	{
		ResultSet rs = null;
		Statement stmt = con.createStatement();
	    if (stmt.execute("SELECT * FROM sc")) {
	    	 rs = stmt.getResultSet();
	    }

		return rs;
	}
	
	public ArrayList<SCRecord> getAllAccessible() throws AuthorizationException, SQLException
	{
		ArrayList<SCRecord> list = new ArrayList();
		
		ResultSet rs = getAll();
   
	    if(auth.allows("admin_sc")) {
	    	//admin can edit all scs
		    while(rs.next()) {
		    	list.add(new SCRecord(rs));
		    }	    	
	    } else {
	    	HashSet<Integer> accessible = getAccessibleIDs();
		    while(rs.next()) {
		    	SCRecord rec = new SCRecord(rs);
		    	if(accessible.contains(rec.id)) {
		    		list.add(rec);
		    	}
		    }	    	
	    }
		return list;
	}
	
	//returns all record id that the user has access to
	private HashSet<Integer> getAccessibleIDs() throws SQLException
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
			if(auth.allows("admin_sc") || isAccessibleType(rec.type_id)) {
				list.add(rec.sc_id);
			}
		}
		
		return list;
	}
	
	public SCRecord get(int sc_id) throws AuthorizationException, SQLException
	{
		ResultSet rs = null;
		PreparedStatement stmt = null;

		String sql = "SELECT * FROM sc WHERE id = ?";
		stmt = con.prepareStatement(sql); 
		stmt.setInt(1, sc_id);


		rs = stmt.executeQuery();
		if(rs.next()) {
			return new SCRecord(rs);
		}
		log.warn("Couldn't find sc where id = " + sc_id);
		return null;
	}
}

