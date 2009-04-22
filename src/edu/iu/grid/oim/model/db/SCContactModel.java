package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.SCContactRecord;

public class SCContactModel extends SmallTableModelBase<SCContactRecord> {
    static Logger log = Logger.getLogger(SCContactModel.class); 

	public SCContactModel(Authorization _auth) {
		super(_auth, "sc_contact");
	}
	SCContactRecord createRecord() throws SQLException
	{
		return new SCContactRecord();
	}
	public ArrayList<SCContactRecord> getBySCID(int sc_id) throws SQLException
	{ 
		ArrayList<SCContactRecord> list = new ArrayList<SCContactRecord>();
		for(RecordBase rec : getCache()) {
			SCContactRecord sccrec = (SCContactRecord)rec;
			if(sccrec.sc_id == sc_id) list.add(sccrec);
		}
		return list;
	}	
	
	public HashMap<Integer/*contact_type_id*/, ArrayList<SCContactRecord>> groupByContactTypeID(ArrayList<SCContactRecord> recs) throws SQLException
	{
		fillCache();
		
		HashMap<Integer, ArrayList<SCContactRecord>> list = new HashMap<Integer, ArrayList<SCContactRecord>>();
		for(SCContactRecord rec : recs) {
			//group records by type_id and create lists of contact_id
			ArrayList<SCContactRecord> array = null;
			if(!list.containsKey(rec.contact_type_id)) {
				//never had this type
				array = new ArrayList<SCContactRecord>();
				list.put(rec.contact_type_id, array);
			} else {
				array = list.get(rec.contact_type_id);
			}	
			array.add(rec);
		}
		return list;		
	}
	
	public ArrayList<SCContactRecord> getByContactID(int contact_id) throws SQLException
	{
		ArrayList<SCContactRecord> list = new ArrayList<SCContactRecord>();
		for(RecordBase rec : getCache()) {
			SCContactRecord sccrec = (SCContactRecord)rec;
			if(sccrec.contact_id == contact_id) list.add(sccrec);
		}		
		return list;
	}
}
