package edu.iu.grid.oim.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;

public class OsgGridTypeModel extends DBModel {
    static Logger log = Logger.getLogger(OsgGridTypeModel.class);  
    
    public OsgGridTypeModel(
    		java.sql.Connection _con, 
    		edu.iu.grid.oim.lib.Authorization _auth) 
    {
    	super(_con, _auth);
    }
    
	public ResultSet getAll() throws AuthorizationException
	{
		ResultSet rs = null;
		try {
			Statement stmt = con.createStatement();
		    if (stmt.execute("SELECT * FROM osg_grid_type")) {
		    	 rs = stmt.getResultSet();
		    }
		} catch(SQLException e) {
			log.error(e.getMessage());
		}
		return rs;
	}
	public ArrayList<OsgGridTypeRecord> getAllEditable() throws AuthorizationException, SQLException
	{
		ArrayList<OsgGridTypeRecord> list = new ArrayList();
	    ResultSet rs = getAll();
	    
	    if(auth.allows("admin_osg_grid_type")) {
	    	//admin can edit grid types
		    while(rs.next()) {
		    	list.add(new OsgGridTypeRecord(rs));
		    }	    	
	    } else {
	    	//non-goc admin cannot edit
	    }
		return list;
	}
	
	public OsgGridTypeRecord get(int osg_grid_type_id) throws AuthorizationException, SQLException
	{
		ResultSet rs = null;

		PreparedStatement stmt = null;

		String sql = "SELECT * FROM osg_grid_type WHERE id = ?";
		stmt = con.prepareStatement(sql); 
		stmt.setInt(1, osg_grid_type_id);

		rs = stmt.executeQuery();
		if(rs.next()) {
			return new OsgGridTypeRecord(rs);
		}
		log.warn("Couldn't find osg_grid_type where id = " + osg_grid_type_id);

		return null;
	}
	
	public void insert(OsgGridTypeRecord rec) throws AuthorizationException, SQLException
	{
		auth.check("write_osg_grid_type");
		PreparedStatement stmt = null;

		String sql = "INSERT INTO osg_grid_type "+
			" VALUES (null, ?,?)";
		stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); 
		
		stmt.setString(1, rec.name);
		stmt.setString(2, rec.description);
	
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
		log.insert("insert_osg_grid_type", id, stmt.toString());
		
		stmt.close();
	}
	
	public void update(OsgGridTypeRecord rec) throws AuthorizationException, SQLException
	{
		auth.check("write_osg_grid_type");
		PreparedStatement stmt = null;

		String sql = "UPDATE osg_grid_type SET "+
			"name=?, description=? "+
			"WHERE id=?";
		stmt = con.prepareStatement(sql); 
		
		stmt.setString(1, rec.name);
		stmt.setString(2, rec.description);
		stmt.setInt(3, rec.id);
		
		
		stmt.executeUpdate(); 
		LogModel log = new LogModel(con, auth);
		log.insert("update_osg_grid_type", rec.id, stmt.toString());
		
		stmt.close(); 	
	}
	
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
}
