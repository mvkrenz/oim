package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.db.record.VOReportContactRecord;

public class VOReportContactModel extends SmallTableModelBase<VOReportContactRecord> {
    static Logger log = Logger.getLogger(VOReportContactModel.class); 
	
	public VOReportContactModel(Authorization _auth) {
		super(_auth, "vo_report_contact");
		// TODO Auto-generated constructor stub
	}
	VOReportContactRecord createRecord(ResultSet rs) throws SQLException
	{
		return new VOReportContactRecord(rs);
	}
/*
	public void update(Integer vo_report_name_id, ArrayList<VOReportContactRecord> contactRecords) throws AuthorizationException, SQLException 
	{
		auth.check("write_vocontact");
	
		String logstr = "";
		con.setAutoCommit(false);

		//remove all current contacts
		try {
			String sql = "DELETE FROM vo_report_contact where vo_id = ?";
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setInt(1, vo_report_name_id);
			stmt.executeUpdate();
			logstr += stmt.toString()+"\n";
		} catch (SQLException e) {
			con.rollback();
			log.error("Failed to remove previous records for vo_id: " + vo_report_name_id);
			throw new SQLException(e);
		}
		
		//insert new contact records in batch
		try {
			String sql = "INSERT INTO vo_contact (contact_id, vo_id, contact_type_id, contact_rank_id)"+
			" VALUES (?, ?, ?, ?)";
			PreparedStatement stmt = con.prepareStatement(sql); 
			
			for(VOReportContactRecord rec : contactRecords) {
				stmt.setInt(1, rec.contact_id);
				stmt.setInt(2, vo_report_name_id);
				stmt.setInt(3, rec.contact_type_id);
				stmt.setInt(4, rec.contact_rank_id);
				stmt.addBatch();
				logstr += stmt.toString()+"\n";
			}
			
			stmt.executeBatch();
			
		} catch (BatchUpdateException e) {
			con.rollback();
			log.error("Failed to insert new records for vo_id: " + vo_report_name_id);
			throw new SQLException(e);
		} 
		
		con.commit();
		con.setAutoCommit(true);
		
		LogModel lmodel = new LogModel(con, auth);
		lmodel.insert("update", "vocontact", logstr);
				
		emptyCache();
	}
*/
}
