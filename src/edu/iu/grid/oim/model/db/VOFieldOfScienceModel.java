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
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VOFieldOfScienceRecord;

public class VOFieldOfScienceModel extends SmallTableModelBase<VOFieldOfScienceRecord> {
    static Logger log = Logger.getLogger(VOFieldOfScienceModel.class); 
	
	public VOFieldOfScienceModel(Connection _con, Authorization _auth) {
		super(_con, _auth, "vo_field_of_science");
	}
	VOFieldOfScienceRecord createRecord(ResultSet rs) throws SQLException
	{
		return new VOFieldOfScienceRecord(rs);
	}

	
	public ArrayList<VOFieldOfScienceRecord> getByVOID(int vo_id) throws SQLException
	{
		ArrayList<VOFieldOfScienceRecord> list = new ArrayList<VOFieldOfScienceRecord>();
		for(RecordBase rec : getCache()) {
			VOFieldOfScienceRecord vcrec = (VOFieldOfScienceRecord)rec;
			if(vcrec.vo_id == vo_id) list.add(vcrec);
		}		
		return list;
	}
/*
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
		lmodel.insert("update", "vo_field_of_science", logstr);
				
		emptyCache();
	}
*/
}
