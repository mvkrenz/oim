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
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class VOModel extends DBModel {
    static Logger log = Logger.getLogger(VOModel.class);  
    
    public VOModel(java.sql.Connection con, Authorization auth) 
    {
    	super(con, auth);
    }
    
	public Collection<VORecord> getAll() throws AuthorizationException, SQLException {
		fillCache();
		return cache.values();
	}
	
	private static HashMap<Integer/*vo_id*/, VORecord> cache = null;
    public void fillCache() throws AuthorizationException, SQLException
	{
		if(cache == null) {
			cache = new HashMap();
			ResultSet rs = null;
			Statement stmt = con.createStatement();
		    if (stmt.execute("SELECT * FROM vo")) {
		    	 rs = stmt.getResultSet();
		    }
		    while(rs.next()) {
		    	VORecord rec = new VORecord(rs);
		    	cache.put(rec.id, rec);
		    }	
		}
	}
    public void emptyCache() //used after we do insert/update
    {
   		cache = null;
    }
	
	public Collection<VORecord> getAllEditable() throws AuthorizationException, SQLException
	{	
		fillCache();
   
	    if(auth.allows("admin_vo")) {
	    	//admin can edit all scs
	    	return cache.values();
	    } else {
	    	//only select record that is editable
			ArrayList<VORecord> list = new ArrayList();
	    	HashSet<Integer> accessible = getEditableIDs();
		    for(VORecord rec : cache.values()) {
		    	if(accessible.contains(rec.id)) {
		    		list.add(rec);
		    	}
		    }	    	
		    return list;
	    }
	}
	
	//returns all record id that the user has access to
	private HashSet<Integer> getEditableIDs() throws SQLException, AuthorizationException
	{
		HashSet<Integer> list = new HashSet<Integer>();
		ResultSet rs = null;

		PreparedStatement stmt = null;

		String sql = "SELECT * FROM vo_contact WHERE contact_id = ?";
		stmt = con.prepareStatement(sql); 
		stmt.setInt(1, auth.getContactID());

		rs = stmt.executeQuery();
		while(rs.next()) {
			VOContactRecord rec = new VOContactRecord(rs);	    
			if(isAccessibleType(rec.contact_type_id)) {
				list.add(rec.vo_id);
			}
		}
		return list;
	}
	
	public VORecord get(int vo_id) throws AuthorizationException, SQLException
	{
		fillCache();
		return cache.get(vo_id);
	}
	
	public void insert(VORecord rec) throws AuthorizationException, SQLException
	{	
		auth.check("write_vo");
		PreparedStatement stmt = null;

		String sql = "INSERT INTO vo "+
			" VALUES (null, ?,?, ?,?, ?,?, ?,?, ?,?, ?,?, ?,?)";
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
		
		stmt.setBoolean(12, rec.active);					
		stmt.setBoolean(13, rec.disable);	
		stmt.setString(14, rec.footprints_id);	
		
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
		emptyCache();
	}
	
	public void update(VORecord rec) throws AuthorizationException, SQLException
	{
		auth.check("write_vo");
		PreparedStatement stmt = null;

		String sql = "UPDATE vo SET "+
			"name=?, long_name=?, description=?, primary_url=?, aup_url=?, membership_services_url=?, "+
			"purpose_url=?, support_url=?, app_description=?, community=?, sc_id=?, active=?, disable=?, footprints_id=? "+
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
		stmt.setBoolean(12, rec.active);					
		stmt.setBoolean(13, rec.disable);	
		stmt.setString(14, rec.footprints_id);	
		stmt.setInt(15, rec.id);
		
		stmt.executeUpdate(); 
		LogModel log = new LogModel(con, auth);
		log.insert("update_vo", rec.id, stmt.toString());
		
		stmt.close(); 	
		emptyCache();
	}
	
	/*
	public void delete(int id) throws AuthorizationException, SQLException
	{
		auth.check("admin_vo");
		PreparedStatement stmt = null;

		String sql = "DELETE FROM vo WHERE id=?";
		stmt = con.prepareStatement(sql); 
		
		stmt.setInt(1, id);
		
		stmt.executeUpdate(); 
		LogModel log = new LogModel(con, auth);
		log.insert("delete_vo", id, stmt.toString());
		
		stmt.close(); 	
		emptyCache();
	}
	*/
}

