package edu.iu.grid.oim.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;

public class FieldOfScienceModel extends DBModel {
    static Logger log = Logger.getLogger(FieldOfScienceModel.class);  
    private static HashMap<Integer, FieldOfScienceRecord> cache = null;
	
    public FieldOfScienceModel(
    		java.sql.Connection _con, 
    		edu.iu.grid.oim.lib.Authorization _auth) 
    {
    	super(_con, _auth);
    }
    
	public void fillCache() throws SQLException
	{
		if(cache == null) {
			cache = new HashMap();
			ResultSet rs = null;
			Statement stmt = con.createStatement();
		    if (stmt.execute("SELECT * FROM field_of_science")) {
		    	 rs = stmt.getResultSet();
		    }
		    while(rs.next()) {
		    	FieldOfScienceRecord rec = new FieldOfScienceRecord(rs);
		    	cache.put(rec.id, rec);
		    }
		}
	}
	public HashMap<Integer, FieldOfScienceRecord> getAll() throws SQLException {
		fillCache();
		return cache;
	}
	
	public void emptyCache()
	{
		cache = null;
	}

	public FieldOfScienceRecord get(int field_of_science_id) throws SQLException
	{
		fillCache();
		return cache.get(field_of_science_id);
	}
	
	public Integer insert(FieldOfScienceRecord rec) throws AuthorizationException, SQLException
	{
		auth.check("write_osg_grid_type");
		PreparedStatement stmt = null;

		String sql = "INSERT INTO field_of_science "+
			" VALUES (null,?)";
		stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); 
		stmt.setString(1, rec.name);
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
		log.insert("insert_field_of_science", id, stmt.toString());
		
		stmt.close();
		
		return id;
	}
	
	public void update(OsgGridTypeRecord rec) throws AuthorizationException, SQLException
	{
		auth.check("write_osg_grid_type");
		PreparedStatement stmt = null;

		String sql = "UPDATE osg_grid_type SET name=? WHERE id=?";
		stmt = con.prepareStatement(sql); 
		
		stmt.setString(1, rec.name);
		stmt.setInt(3, rec.id);
		
		stmt.executeUpdate(); 
		LogModel log = new LogModel(con, auth);
		log.insert("update_field_of_science", rec.id, stmt.toString());
		
		stmt.close(); 	
	}
	
	/*
	public void delete(int id) throws AuthorizationException, SQLException
	{
		auth.check("admin_osg_grid_type");
		PreparedStatement stmt = null;

		String sql = "DELETE FROM osg_grid_type WHERE id=?";
		stmt = con.prepareStatement(sql); 
		
		stmt.setInt(1, id);
		
		stmt.executeUpdate(); 
		LogModel log = new LogModel(con, auth);
		log.insert("delete_osg_grid_type", id, stmt.toString());
		
		stmt.close(); 	
	}
	*/
}
