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
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class VOModel extends DBModel {
    static Logger log = Logger.getLogger(VOModel.class);  
    
    public VOModel(java.sql.Connection con, Authorization auth) 
    {
    	super(con, auth);
    }
    
	public ResultSet getAll() throws AuthorizationException, SQLException
	{
		auth.check(Action.read_vo);
		ResultSet rs = null;

		Statement stmt = con.createStatement();
	    if (stmt.execute("SELECT * FROM virtualorganization")) {
	    	 rs = stmt.getResultSet();
	    }
	    
		return rs;
	}
	
	public ArrayList<VORecord> getAllAccessible() throws AuthorizationException, SQLException
	{
		auth.check(Action.read_vo);
		ResultSet rs = null;
		ArrayList<VORecord> list = new ArrayList();

		Statement stmt = con.createStatement();
	    if (stmt.execute("SELECT * FROM virtualorganization")) {
	    	 rs = stmt.getResultSet();
	    }
	    
	    Set<Integer> accessible = getAccessibleIDs();
	   
	    while(rs.next()) {
	    	VORecord rec = new VORecord(rs);
	    	if(accessible.contains(rec.id)) {
	    		list.add(rec);
	    	}
	    }	    
		return list;
	}
	
	//returns all record id that the user has access to
	private Set<Integer> getAccessibleIDs() throws SQLException, AuthorizationException
	{
		auth.check(Action.read_vocontact);
		Set<Integer> list = new HashSet<Integer>();
		ResultSet rs = null;

		PreparedStatement stmt = null;

		String sql = "SELECT * FROM vo_contact WHERE person_id = ?";
		stmt = con.prepareStatement(sql); 
		stmt.setInt(1, auth.getPersonID());

		rs = stmt.executeQuery();
		while(rs.next()) {
			VOContactRecord rec = new VOContactRecord(rs);	    
			if(auth.allows(Action.admin_vo) || isAccessibleType(rec.type_id)) {
				list.add(rec.vo_id);
			}
		}
		return list;
	}
	
	public VORecord get(int vo_id) throws AuthorizationException, SQLException
	{
		auth.check(Action.read_vo);
		ResultSet rs = null;

		PreparedStatement stmt = null;

		String sql = "SELECT * FROM virtualorganization WHERE id = ?";
		stmt = con.prepareStatement(sql); 
		stmt.setInt(1, vo_id);

		rs = stmt.executeQuery();
		if(rs.next()) {
			return new VORecord(rs);
		}
		log.warn("Couldn't find vo where id = " + vo_id);

		return null;
	}
	
	public void insert(VORecord rec) throws AuthorizationException, SQLException
	{
		auth.check(Action.write_vo);
		PreparedStatement stmt = null;

		String sql = "INSERT INTO virtualorganization "+
			" VALUES (null, ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); 
		
		stmt.setString(1, rec.name);
		stmt.setString(2, rec.long_name);
		stmt.setString(3, rec.description);
		stmt.setString(4, rec.primary_url);
		stmt.setString(5, rec.aup_url);
		stmt.setString(6, rec.membership_services_url);
		stmt.setString(7, rec.purpose_url);
		stmt.setString(8, rec.support_url);		
		stmt.setString(9, rec.app_description);
		stmt.setString(10, rec.community);
		stmt.setInt(11, rec.sc_id);
		
		if(rec.parent_vo_id == null) {
			stmt.setNull(12, java.sql.Types.INTEGER);
		} else {
			stmt.setInt(12, rec.parent_vo_id);	
		}
		
		stmt.setBoolean(13, rec.active);					
		stmt.setBoolean(14, rec.disable);	
		stmt.setString(15, rec.footprints_id);	
		
		stmt.executeUpdate();
		
		//pull generated id
		ResultSet keys = stmt.getGeneratedKeys();
		Integer id = null;
		if (keys != null) {
			if (keys.next()) {
				id = keys.getInt(1);
			}
		}
		
		LogModel log = new LogModel(con, auth);
		log.insert("insert_vo", id, stmt.toString());
		
		stmt.close();
	}
	
	public void update(VORecord rec) throws AuthorizationException, SQLException
	{
		auth.check(Action.write_vo);
		PreparedStatement stmt = null;

		String sql = "UPDATE virtualorganization SET "+
			"name=?, long_name=?, description=?, primary_url=?, aup_url=?, membership_services_url=?, "+
			"purpose_url=?, support_url=?, app_description=?, community=?, sc_id=?, parent_vo_id=?, active=?, disable=?, footprints_id=? "+
			"WHERE id=?";
		stmt = con.prepareStatement(sql); 
		
		stmt.setString(1, rec.name);
		stmt.setString(2, rec.long_name);
		stmt.setString(3, rec.description);
		stmt.setString(4, rec.primary_url);
		stmt.setString(5, rec.aup_url);
		stmt.setString(6, rec.membership_services_url);
		stmt.setString(7, rec.purpose_url);
		stmt.setString(8, rec.support_url);		
		stmt.setString(9, rec.app_description);
		stmt.setString(10, rec.community);
		stmt.setInt(11, rec.sc_id);
		
		if(rec.parent_vo_id == null) {
			stmt.setNull(12, java.sql.Types.INTEGER);
		} else {
			stmt.setInt(12, rec.parent_vo_id);	
		}
		
		stmt.setBoolean(13, rec.active);					
		stmt.setBoolean(14, rec.disable);	
		stmt.setString(15, rec.footprints_id);	
		
		stmt.setInt(16, rec.id);
		
		stmt.executeUpdate(); 
		LogModel log = new LogModel(con, auth);
		log.insert("update_vo", rec.id, stmt.toString());
		
		stmt.close(); 	
	}
	
	public void delete(int id) throws AuthorizationException, SQLException
	{
		auth.check(Action.write_vo);
		PreparedStatement stmt = null;

		String sql = "DELETE FROM virtualorganization WHERE id=?";
		stmt = con.prepareStatement(sql); 
		
		stmt.setInt(1, id);
		
		stmt.executeUpdate(); 
		LogModel log = new LogModel(con, auth);
		log.insert("delete_vo", id, stmt.toString());
		
		stmt.close(); 	
	}
}

