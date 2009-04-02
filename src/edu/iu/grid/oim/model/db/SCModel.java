package edu.iu.grid.oim.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.SCContactRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
public class SCModel extends SmallTableModelBase<SCRecord> {
    static Logger log = Logger.getLogger(SCModel.class);  
    
    public SCModel(Authorization auth) 
    {
    	super(auth, "sc");
    }    
    SCRecord createRecord(ResultSet rs) throws SQLException
	{
		return new SCRecord(rs);
	}

	public Collection<SCRecord> getAllEditable() throws SQLException
	{		
		ArrayList<SCRecord> list = new ArrayList();
	    if(auth.allows("admin_sc")) {
	    	//admin can edit all scs
	    	for(RecordBase rec : getCache()) {
	    		list.add((SCRecord)rec);
	    	}
	    } else {
	    	//only select record that is editable
	    	HashSet<Integer> accessible = getEditableIDs();
		    for(RecordBase rec : getCache()) {
		    	SCRecord screc = (SCRecord)rec;
		    	if(accessible.contains(screc.id)) {
		    		list.add(screc);
		    	}
		    }	    	
	    }
	    return list;
	}
	
	//returns all record id that the user has access to
	private HashSet<Integer> getEditableIDs() throws SQLException
	{
		HashSet<Integer> list = new HashSet<Integer>();
		ResultSet rs = null;

		PreparedStatement stmt = null;

		String sql = "SELECT * FROM sc_contact WHERE contact_id = ?";
		stmt = getConnection().prepareStatement(sql); 
		stmt.setInt(1, auth.getContactID());

		rs = stmt.executeQuery();
		while(rs.next()) {
			SCContactRecord rec = new SCContactRecord(rs);
			list.add(rec.sc_id);
		}
		
		return list;
	}
	public SCRecord get(int id) throws SQLException {
		SCRecord keyrec = new SCRecord();
		keyrec.id = id;
		return get(keyrec);
	}
}

