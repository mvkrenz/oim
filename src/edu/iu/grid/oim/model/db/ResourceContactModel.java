package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;

public class ResourceContactModel extends SmallTableModelBase<ResourceContactRecord> {
    static Logger log = Logger.getLogger(ResourceContactModel.class); 
	
	public ResourceContactModel(Authorization _auth) {
		super(_auth, "resource_contact");
	}
	ResourceContactRecord createRecord(ResultSet rs) throws SQLException
	{
		return new ResourceContactRecord(rs);
	}

	public ArrayList<ResourceContactRecord> getByResourceID(int resource_id) throws SQLException
	{ 
		ArrayList<ResourceContactRecord> list = new ArrayList<ResourceContactRecord>();
		for(RecordBase rec : getCache()) {
			ResourceContactRecord vcrec = (ResourceContactRecord)rec;
			if(vcrec.resource_id == resource_id) list.add(vcrec);
		}
		return list;
	}	
	
	public HashMap<Integer/*contact_type_id*/, ArrayList<ResourceContactRecord>> groupByContactTypeID(ArrayList<ResourceContactRecord> recs) throws SQLException
	{
		fillCache();
		
		HashMap<Integer, ArrayList<ResourceContactRecord>> list = new HashMap<Integer, ArrayList<ResourceContactRecord>>();
		for(ResourceContactRecord rec : recs) {
			//group records by type_id and create lists of contact_id
			ArrayList<ResourceContactRecord> array = null;
			if(!list.containsKey(rec.contact_type_id)) {
				//never had this type
				array = new ArrayList<ResourceContactRecord>();
				list.put(rec.contact_type_id, array);
			} else {
				array = list.get(rec.contact_type_id);
			}	
			array.add(rec);
		}
		return list;		
	}
	
	public ArrayList<ResourceContactRecord> getByContactID(int contact_id) throws SQLException
	{
		ArrayList<ResourceContactRecord> list = new ArrayList<ResourceContactRecord>();
		for(RecordBase rec : getCache()) {
			ResourceContactRecord vcrec = (ResourceContactRecord)rec;
			if(vcrec.contact_id == contact_id) list.add(vcrec);
		}		
		return list;
	}
}
