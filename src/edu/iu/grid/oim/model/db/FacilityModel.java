package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Action;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;

public class FacilityModel extends DBModel {
    static Logger log = Logger.getLogger(FacilityModel.class);  
    
    public FacilityModel(
    		java.sql.Connection _con, 
    		edu.iu.grid.oim.lib.Authorization _auth) 
    {
    	super(_con, _auth);
    }
    
	public ResultSet getAll() throws AuthorizationException
	{
		auth.check(Action.read_facility);
		ResultSet rs = null;
		try {
			Statement stmt = con.createStatement();
		    if (stmt.execute("SELECT * FROM facility")) {
		    	 rs = stmt.getResultSet();
		    }
		} catch(SQLException e) {
			log.error(e.getMessage());
		}
		return rs;
	}
}
