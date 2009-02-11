package edu.iu.grid.oim.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;

public class LogModel extends Model {
    static Logger log = Logger.getLogger(LogModel.class);  
    
    enum Type {authenticate, insert, update, delete}
    
    public LogModel(Connection _con) 
    {
    	super(_con, null);
    }
/*    
	public Integer Authenticate(Integer dn_id, String url, String method)
	{
		ResultSet rs = null;
		try {
			PreparedStatement stmt = null;

			String sql = "INSERT INTO log (`type`, `dn_id`, `comment`, `key`) VALUES (?, ?, ?, ?)";
			stmt = con.prepareStatement(sql); 
			stmt.setString(1, "authenticate");
			stmt.setInt(2, dn_id);
			stmt.setString(3, method);
			stmt.setString(4, url);

			stmt.executeUpdate(); 
			stmt.close(); 

		} catch(SQLException e) {
			log.error(e.getMessage());
		}
		return null;
	}
*/
}
