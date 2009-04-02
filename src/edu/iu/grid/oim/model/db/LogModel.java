package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.LogRecord;

public class LogModel extends SmallTableModelBase<LogRecord> {
    static Logger log = Logger.getLogger(LogModel.class);  
    
    //enum Type {authenticate, insert, update, delete}
    
    public LogModel(Connection _con, Authorization _auth) 
    {
    	super(_con, _auth, "log");
    }
    LogRecord createRecord(ResultSet rs) throws SQLException
	{
		return new LogRecord(rs);
	}
    
    public void insert(String type, String model, String xml) throws SQLException
    {
    	//no auth check... accessing log table is non-auth action
		PreparedStatement stmt = null;

		String logsql = "INSERT INTO log (`type`, `model`, `xml`, `dn_id`) VALUES (?, ?, ?, ?)";
		stmt = con.prepareStatement(logsql); 
		stmt.setString(1, type);
		stmt.setString(2, model);
		stmt.setString(3, xml);
		stmt.setInt(4, auth.getDNID());

		stmt.executeUpdate(); 
		stmt.close(); 
    }
}
