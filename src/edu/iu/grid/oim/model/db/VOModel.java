package edu.iu.grid.oim.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Action;
import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.VORecord;

public class VOModel extends DBModel {
    static Logger log = Logger.getLogger(VOModel.class);  
    
    public VOModel(java.sql.Connection con, Authorization auth) 
    {
    	super(con, auth);
    }
    
	public ResultSet getAllVOs() throws AuthorizationException
	{
		auth.check(Action.select_vo);
		ResultSet rs = null;
		try {
			Statement stmt = con.createStatement();
		    if (stmt.execute("SELECT * FROM virtualorganization")) {
		    	 rs = stmt.getResultSet();
		    }
		} catch(SQLException e) {
			log.error(e.getMessage());
		}
		return rs;
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

