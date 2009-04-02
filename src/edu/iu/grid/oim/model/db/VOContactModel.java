package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.VOContactRecord;

public class VOContactModel extends SmallTableModelBase<VOContactRecord> {
    static Logger log = Logger.getLogger(VOContactModel.class); 
	
	public VOContactModel(Authorization _auth) {
		super(_auth, "vo_contact");
	}
	VOContactRecord createRecord(ResultSet rs) throws SQLException
	{
		return new VOContactRecord(rs);
	}

	public ArrayList<VOContactRecord> getByVOID(int vo_id) throws SQLException
	{ 
		ArrayList<VOContactRecord> list = new ArrayList<VOContactRecord>();
		for(RecordBase rec : getCache()) {
			VOContactRecord vcrec = (VOContactRecord)rec;
			if(vcrec.vo_id == vo_id) list.add(vcrec);
		}
		return list;
	}	
	
	public HashMap<Integer/*contact_type_id*/, ArrayList<VOContactRecord>> groupByContactTypeID(ArrayList<VOContactRecord> recs) throws SQLException
	{
		fillCache();
		
		HashMap<Integer, ArrayList<VOContactRecord>> list = new HashMap<Integer, ArrayList<VOContactRecord>>();
		for(VOContactRecord rec : recs) {
			//group records by type_id and create lists of contact_id
			ArrayList<VOContactRecord> array = null;
			if(!list.containsKey(rec.contact_type_id)) {
				//never had this type
				array = new ArrayList<VOContactRecord>();
				list.put(rec.contact_type_id, array);
			} else {
				array = list.get(rec.contact_type_id);
			}	
			array.add(rec);
		}
		return list;		
	}
	
	public ArrayList<VOContactRecord> getByContactID(int contact_id) throws SQLException
	{
		ArrayList<VOContactRecord> list = new ArrayList<VOContactRecord>();
		for(RecordBase rec : getCache()) {
			VOContactRecord vcrec = (VOContactRecord)rec;
			if(vcrec.contact_id == contact_id) list.add(vcrec);
		}		
		return list;
	}
}
