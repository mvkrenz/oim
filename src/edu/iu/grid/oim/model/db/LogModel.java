package edu.iu.grid.oim.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.LogRecord;

public class LogModel extends SmallTableModelBase<LogRecord> {
    static Logger log = Logger.getLogger(LogModel.class);  
    
    //enum Type {authenticate, insert, update, delete}
    
    public LogModel(Authorization _auth) 
    {
    	super(_auth, "log");
    }
    
    LogRecord createRecord(ResultSet rs) throws SQLException
	{
		return new LogRecord(rs);
	}
    
    public int insert(String type, String model, String xml) throws SQLException
    {
    	//no auth check... accessing log table is non-auth action
    	
		String sql = "INSERT INTO log (`type`, `model`, `xml`, `dn_id`) VALUES (?, ?, ?, ?)";
		PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); 
		stmt.setString(1, type);
		stmt.setString(2, model);
		stmt.setString(3, xml);
		Integer dn_id = auth.getDNID();
		if(dn_id == null) {
			stmt.setNull(4, java.sql.Types.INTEGER);
		} else {
			stmt.setInt(4, dn_id);
		}
		stmt.executeUpdate(); 
		
		ResultSet ids = stmt.getGeneratedKeys();  
		if(!ids.next()) {
			throw new SQLException("didn't get a new log id");
		}
		int logid = ids.getInt(1);
		stmt.close();
		return logid;
    }
}
