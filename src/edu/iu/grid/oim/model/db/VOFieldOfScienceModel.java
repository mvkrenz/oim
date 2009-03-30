package edu.iu.grid.oim.model.db;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;

public class VOFieldOfScienceModel extends DBModel {
    static Logger log = Logger.getLogger(VOFieldOfScienceModel.class); 
	public static HashMap<Integer/*vo_id*/, ArrayList<FieldOfScienceRecord>> cache = null;
	
	public VOFieldOfScienceModel(Connection _con, Authorization _auth) {
		super(_con, _auth);
	}
	
	public ArrayList<FieldOfScienceRecord> get(Integer vo_id) throws SQLException
	{	
		fillCache();
		return cache.get(vo_id);
	}
	
	private void fillCache() throws SQLException
	{
		if(cache == null) {
			cache = new HashMap();
			
			String sql = "SELECT * FROM vo_field_of_science order by field_of_science_id";
			PreparedStatement stmt = con.prepareStatement(sql); 
			ResultSet rs = stmt.executeQuery();
			
			FieldOfScienceModel model = new FieldOfScienceModel(con, auth);
	
			while(rs.next()) {
				Integer vo_id = rs.getInt("vo_id");
				
				//group records by vo_id and put it in the cache
				ArrayList<FieldOfScienceRecord> a = null;
				if(!cache.containsKey(vo_id)) {
					//never had this type
					a = new ArrayList<FieldOfScienceRecord>();
					cache.put(vo_id, a);
				} else {
					a = cache.get(vo_id);
				}
				
				//convert field_of_science_id to real field_of_science record
				Integer field_of_science_id = rs.getInt("field_of_science_id");
				FieldOfScienceRecord rec = model.get(field_of_science_id);
				a.add(rec);
			}
		}
	}
	
    public void emptyCache() //used after we do insert/update
    {
   		cache = null;
    }

	public void update(Integer vo_id, ArrayList<Integer> fsids) throws AuthorizationException, SQLException 
	{
		auth.check("write_vo");
	
		String logstr = "";
		con.setAutoCommit(false);

		//remove all current contacts
		try {
			String sql = "DELETE FROM vo_field_of_science where vo_id = ?";
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setInt(1, vo_id);
			stmt.executeUpdate();
			logstr += stmt.toString()+"\n";
		} catch (SQLException e) {
			con.rollback();
			log.error("Failed to remove previous records for vo_id: " + vo_id);
			throw new SQLException(e);
		}
		
		//insert new contact records in batch
		try {
			String sql = "INSERT INTO vo_field_of_science (vo_id, field_of_science_id) VALUES (?, ?)";
			PreparedStatement stmt = con.prepareStatement(sql); 
			
			for(Integer fsid : fsids) {
				stmt.setInt(1, vo_id);
				stmt.setInt(2, fsid);
				stmt.addBatch();
				logstr += stmt.toString()+"\n";
			}
			
			stmt.executeBatch();
			
		} catch (BatchUpdateException e) {
			con.rollback();
			log.error("Failed to insert new records for vo_id: " + vo_id);
			throw new SQLException(e);
		} 
		
		con.commit();
		con.setAutoCommit(true);
		
		LogModel lmodel = new LogModel(con, auth);
		lmodel.insert("update_vo_field_of_science", vo_id, logstr);
				
		emptyCache();
	}
}
