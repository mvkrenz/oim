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
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VOReportContactRecord;

public class VOReportContactModel extends DBModel {
    static Logger log = Logger.getLogger(VOReportContactModel.class); 
	public static HashMap<Integer/*vo_id*/, ArrayList<VOReportContactRecord>> cache = null;
	
	public VOReportContactModel(Connection _con, Authorization _auth) {
		super(_con, _auth);
		// TODO Auto-generated constructor stub
	}
	
	public HashMap<Integer/*type_id*/, ArrayList<VOReportContactRecord>> get(Integer vo_report_name_id) throws AuthorizationException, SQLException
	{	
		fillCache();

		HashMap<Integer, ArrayList<VOReportContactRecord>> list = new HashMap();
		if(cache.containsKey(vo_report_name_id)) {
			ArrayList<VOReportContactRecord> recs = cache.get(vo_report_name_id);
			for(VOReportContactRecord rec : recs) {
				//group records by type_id and create lists of contact_id
				ArrayList<VOReportContactRecord> array = null;
				if(!list.containsKey(rec.contact_type_id)) {
					//never had this type
					array = new ArrayList<VOReportContactRecord>();
					list.put(rec.contact_type_id, array);
				} else {
					array = list.get(rec.contact_type_id);
				}	
				array.add(rec);
			}
			return list;
		}
		
		log.warn("Couldn't find any record where vo_id = " + vo_report_name_id);
		return list;
	}
	
	private void fillCache() throws SQLException
	{
		if(cache == null) {
			cache = new HashMap();
			
			String sql = "SELECT * FROM vo_report_contact order by contact_rank_id";
			PreparedStatement stmt = con.prepareStatement(sql); 
			ResultSet rs = stmt.executeQuery();
	
			while(rs.next()) {
				VOReportContactRecord rec = new VOReportContactRecord(rs);
				
				//group records by vo_id and put it in the cache
				ArrayList<VOReportContactRecord> a = null;
				if(!cache.containsKey(rec.vo_report_name_id)) {
					//never had this type
					a = new ArrayList<VOReportContactRecord>();
					cache.put(rec.vo_report_name_id, a);
				} else {
					a = cache.get(rec.vo_report_name_id);
				}
				a.add(rec);
			}
		}
	}
    public void emptyCache() //used after we do insert/update
    {
   		cache = null;
    }

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
		lmodel.insert("update_vocontact", vo_report_name_id, logstr);
				
		emptyCache();
	}
}
