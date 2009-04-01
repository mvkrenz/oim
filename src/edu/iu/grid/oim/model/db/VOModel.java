package edu.iu.grid.oim.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class VOModel extends SmallTableModelBase<VORecord>
{	
    static Logger log = Logger.getLogger(VOModel.class);  

    public VOModel(java.sql.Connection con, Authorization auth) 
    {
    	super(con, auth, "vo");
    }
    VORecord createRecord(ResultSet rs) throws SQLException
	{
		return new VORecord(rs);
	}
	
	public Collection<VORecord> getAllEditable() throws SQLException
	{	   
		ArrayList<VORecord> list = new ArrayList();
	    if(auth.allows("admin_vo")) {
	    	//admin can edit all scs
	    	for(RecordBase rec : getCache()) {
	    		list.add((VORecord)rec);
	    	}
	    } else {
	    	//only select record that is editable
	    	HashSet<Integer> accessible = getEditableIDs();
		    for(RecordBase rec : getCache()) {
		    	VORecord vorec = (VORecord)rec;
		    	if(accessible.contains(vorec.id)) {
		    		list.add(vorec);
		    	}
		    }	    	
	    }
	    return list;
	}
	
	//returns all record id that the user has access to
	private HashSet<Integer> getEditableIDs() throws SQLException
	{
		HashSet<Integer> list = new HashSet<Integer>();
		VOContactModel model = new VOContactModel(con, auth);
		Collection<VOContactRecord> vcrecs = model.getByContactID(auth.getContactID());
		for(VOContactRecord rec : vcrecs) {
			list.add(rec.vo_id);
		}
		return list;
	}
	
	public VORecord getParentVO(int child_vo_id) throws SQLException
	{
		//lookup parent_vo from vo_vo table
		PreparedStatement stmt = null;

		String sql = "SELECT * FROM vo_vo WHERE child_vo_id = ?";
		stmt = con.prepareStatement(sql); 
		stmt.setInt(1, child_vo_id);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			VORecord keyrec = new VORecord();
			keyrec.id = rs.getInt("parent_vo_id");
			return (VORecord)get(keyrec);
		} 
		return null;
		
	}
		
	public void updateParentVOID(Integer child_vo_id, Integer parent_vo_id) throws SQLException, AuthorizationException
	{
		auth.check("write_vo");	 //TODO - should I create write_vo_vo? I feel this should be part of write_vo..
		String logstr = "";
		con.setAutoCommit(false);

		//remove current mapping
		try {
			String sql = "DELETE FROM vo_vo where child_vo_id = ?";
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setInt(1, child_vo_id);
			stmt.executeUpdate();
			logstr += stmt.toString()+"\n";
		} catch (SQLException e) {
			con.rollback();
			log.error("Failed to remove previous records for child_vo_id: " + child_vo_id);
			throw new SQLException(e);
		}
		
		//insert new vo_vo records 
		if(parent_vo_id != null) {
			//if parent_vo_id is null, don't insert anythin
			try {
				String sql = "INSERT INTO vo_vo (child_vo_id, parent_vo_id) VALUES (?, ?)";
				PreparedStatement stmt = con.prepareStatement(sql); 
				stmt.setInt(1, child_vo_id);
				stmt.setInt(2, parent_vo_id);
				stmt.executeUpdate();
				logstr += stmt.toString()+"\n";
				
			} catch (SQLException e) {
				con.rollback();
				log.error("Failed to insert new records for child_vo_id: " + child_vo_id);
				throw new SQLException(e);
			} 
		}
		
		con.commit();
		con.setAutoCommit(true);
		
		LogModel lmodel = new LogModel(con, auth);
		lmodel.insert("update", "vo_vo", logstr);
				
		emptyCache();
	}

}

