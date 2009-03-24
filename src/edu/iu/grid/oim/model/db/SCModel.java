package edu.iu.grid.oim.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Action;
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
    
	public ResultSet getAll() throws AuthorizationException
	{
		auth.check(Action.read_sc);
		ResultSet rs = null;
		try {
			Statement stmt = con.createStatement();
		    if (stmt.execute("SELECT * FROM supportcenter")) {
		    	 rs = stmt.getResultSet();
		    }
		} catch(SQLException e) {
			log.error(e.getMessage());
		}
		return rs;
	}
	
	public ArrayList<SCRecord> getAllAccessible() throws AuthorizationException, SQLException
	{
		auth.check(Action.read_sc);
		ResultSet rs = null;
		ArrayList<SCRecord> list = new ArrayList();

		Statement stmt = con.createStatement();
	    if (stmt.execute("SELECT * FROM supportcenter")) {
	    	 rs = stmt.getResultSet();
	    }
	    
	    Set<Integer> accessible = getAccessibleIDs();
	    while(rs.next()) {
	    	SCRecord rec = new SCRecord(rs);
	    	if(accessible == null || accessible.contains(rec.id)) {
	    		list.add(rec);
	    	}
	    }	    
		return list;
	}
	
	//returns all record id that the user has access to
	private Set<Integer> getAccessibleIDs()
	{
		Set<Integer> list = new HashSet<Integer>();
		ResultSet rs = null;
		try {
			PreparedStatement stmt = null;

			String sql = "SELECT * FROM sc_contact WHERE person_id = ?";
			stmt = con.prepareStatement(sql); 
			stmt.setInt(1, auth.getPersonID());

			rs = stmt.executeQuery();
			while(rs.next()) {
				SCContactRecord rec = new SCContactRecord(rs);
				if(auth.allows(Action.admin_sc) || isAccessibleType(rec.type_id)) {
					list.add(rec.sc_id);
				}
			}
		} catch(SQLException e) {
			log.error(e.getMessage());
		}	
		
		return list;
	}
	
	public SCRecord get(int sc_id) throws AuthorizationException
	{
		auth.check(Action.read_sc);
		ResultSet rs = null;
		try {
			PreparedStatement stmt = null;

			String sql = "SELECT * FROM supportcenter WHERE id = ?";
			stmt = con.prepareStatement(sql); 
			stmt.setInt(1, sc_id);

			rs = stmt.executeQuery();
			if(rs.next()) {
				return new SCRecord(rs);
			}
			log.warn("Couldn't find sc where id = " + sc_id);
		} catch(SQLException e) {
			log.error(e.getMessage());
		}
		return null;
	}

	/*
	public void insertVO(VORecord rec) throws AuthorizationException
	{
		auth.check(Action.insert_vo);
		try {
			PreparedStatement stmt = null;

			String sql = "INSERT INTO virtualorganization VALUES (null, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			stmt = con.prepareStatement(sql); 
			stmt.setString(1, rec.name);
			stmt.setString(2, rec.description);
			stmt.setString(3, rec.fqdn);
			stmt.setString(4, rec.url);
			stmt.setBoolean(5, rec.interop_bdii);
			stmt.setBoolean(6, rec.interop_monitoring);
			stmt.setBoolean(7, rec.interop_accounting);
			stmt.setString(8, rec.wlcg_accounting_name);
			stmt.setBoolean(9, rec.active);
			stmt.setBoolean(10, rec.disable);
			stmt.setInt(11, rec.resource_group_id);
			stmt.executeUpdate(); 
			stmt.close(); 
			
			LogModel log = new LogModel(con, auth);
			log.insert("resource", stmt.toString());
		} catch(SQLException e) {
			log.error(e.getMessage());
		}
	}
	*/
}

