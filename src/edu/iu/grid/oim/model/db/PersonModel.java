package edu.iu.grid.oim.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Action;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.PersonRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;

public class PersonModel extends DBModel {
    static Logger log = Logger.getLogger(PersonModel.class);  
    
    public PersonModel(
    		java.sql.Connection _con, 
    		edu.iu.grid.oim.lib.Authorization _auth) 
    {
    	super(_con, _auth);
    }
    
	public ResultSet getAll() throws AuthorizationException
	{
		auth.check(Action.read_person);
		ResultSet rs = null;
		try {
			Statement stmt = con.createStatement();
		    if (stmt.execute("SELECT * FROM person")) {
		    	 rs = stmt.getResultSet();
		    }
		} catch(SQLException e) {
			log.error(e.getMessage());
		}
		return rs;
	}
	
	public PersonRecord get(int person_id) throws AuthorizationException
	{
		auth.check(Action.read_person);
		ResultSet rs = null;
		try {
			PreparedStatement stmt = null;

			String sql = "SELECT * FROM person WHERE id = ?";
			stmt = con.prepareStatement(sql); 
			stmt.setInt(1, person_id);

			rs = stmt.executeQuery();
			if(rs.next()) {
				return new PersonRecord(rs);
			}
			log.warn("Couldn't find person where id = " + person_id);
		} catch(SQLException e) {
			log.error(e.getMessage());
		}
		return null;
	}
}
