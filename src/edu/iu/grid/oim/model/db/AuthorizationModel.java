package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.DNRecord;

public class AuthorizationModel {
    static Logger log = Logger.getLogger(AuthorizationModel.class);  
    protected Connection con;
    
    public AuthorizationModel(Connection _con) 
    {
    	con = _con;
    }
    
	public DNRecord findByDN(String dn_string) throws AuthorizationException, SQLException
	{
		ResultSet rs = null;
		PreparedStatement pstmt = con.prepareStatement(
                "SELECT * FROM dn where dn_string = ?");
        pstmt.setString(1, dn_string);
        rs = pstmt.executeQuery();
        if(rs.next()) {
        	return new DNRecord(rs);
        } else {
        	return null;
        }
	}
	
	public Collection<Integer> getAuthTypes(Integer dn_id) throws SQLException
	{
		HashSet<Integer> list = new HashSet<Integer>();
		
		ResultSet rs = null;
		PreparedStatement pstmt = con.prepareStatement(
                "SELECT * FROM dn_authorization_type where dn_id = ?");
        pstmt.setInt(1, dn_id);
        rs = pstmt.executeQuery();
        while(rs.next()) {
        	list.add(rs.getInt("authorization_type_id"));
        }
		return list;
	}
	
	public Collection<Integer> getActionIDs(Integer auth_type_id) throws SQLException
	{
		HashSet<Integer> list = new HashSet<Integer>();
		
		PreparedStatement pstmt = con.prepareStatement(
                "SELECT * FROM authorization_type_action where authorization_type_id = ?");
        pstmt.setInt(1, auth_type_id);
        ResultSet rs = pstmt.executeQuery();
        while(rs.next()) {
        	list.add(rs.getInt("action_id"));
        }
		return list;		
	}
	
	private static HashMap<Integer, String> cache_getAction = null;
	public String getAction(Integer action_id) throws SQLException
	{
		if(cache_getAction == null) {
			cache_getAction = new HashMap<Integer, String>();
			PreparedStatement pstmt = con.prepareStatement("SELECT * FROM action");
			ResultSet rs = pstmt.executeQuery();
	        while(rs.next()) {
	        	cache_getAction.put(rs.getInt("id"), rs.getString("name"));
	        }
		}
		return cache_getAction.get(action_id);
	}
}
