package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Action;
import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;

public class LogModel extends DBModel {
    static Logger log = Logger.getLogger(LogModel.class);  
    
    //enum Type {authenticate, insert, update, delete}
    
    public LogModel(Connection _con, Authorization _auth) 
    {
    	super(_con, _auth);
    }
    
    public void insert(String type, Integer key, String detail) throws AuthorizationException
    {
		auth.check(Action.log);
		try {
			PreparedStatement stmt = null;

			String logsql = "INSERT INTO log (`type`, `key`, `detail`) VALUES (?, ?, ?)";
			stmt = con.prepareStatement(logsql); 
			stmt.setString(1, type);
			stmt.setInt(2, key);
			stmt.setString(3, detail);

			stmt.executeUpdate(); 
			stmt.close(); 

		} catch(SQLException e) {
			log.error(e.getMessage());
		}  	
    }
}
