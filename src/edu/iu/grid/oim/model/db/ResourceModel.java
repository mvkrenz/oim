package edu.iu.grid.oim.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Action;
import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.ResourceRecord;

public class ResourceModel extends DBModel {
    static Logger log = Logger.getLogger(ResourceModel.class);  
    
    public ResourceModel(java.sql.Connection con, Authorization auth) 
    {
    	super(con, auth);
    }
    
	public ResultSet getAll() throws AuthorizationException
	{
		ResultSet rs = null;
		try {
			Statement stmt = con.createStatement();
		    if (stmt.execute("SELECT * FROM resource")) {
		    	 rs = stmt.getResultSet();
		    }
		} catch(SQLException e) {
			log.error(e.getMessage());
		}
		return rs;
	}
	
	public ResultSet getAllByGroupBy(int group_id) throws AuthorizationException
	{
		ResultSet rs = null;
		try {
			PreparedStatement pstmt = con.prepareStatement(
	                "SELECT * FROM resource where resource_group_id = ?");
	        pstmt.setInt(1, group_id);
	        rs = pstmt.executeQuery();
		} catch(SQLException e) {
			log.error(e.getMessage());
		}
		return rs;
	}
	
	public void insert(ResourceRecord rec) throws AuthorizationException, SQLException
	{
		auth.check(Action.write_resource);
		PreparedStatement stmt = null;

		String sql = "INSERT INTO resource VALUES (null, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
		
		//pull generated id
		ResultSet keys = stmt.getGeneratedKeys();
		Integer id = null;
		if (keys != null) {
			if (keys.next()) {
				id = keys.getInt(1);
			}
		}
				
		LogModel log = new LogModel(con, auth);
		log.insert("insert_resource", id, stmt.toString());
		
		stmt.close();
	}
}

