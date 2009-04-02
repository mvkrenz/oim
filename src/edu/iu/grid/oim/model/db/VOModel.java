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
import edu.iu.grid.oim.model.db.record.VOVORecord;

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
	public VORecord get(int id) throws SQLException {
		VORecord keyrec = new VORecord();
		keyrec.id = id;
		return get(keyrec);
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
		VOVOModel model = new VOVOModel(con, auth);
		VOVORecord vovo = model.get(child_vo_id);
		if(vovo == null) return null;
		return get(vovo.parent_vo_id);	
	}
}

