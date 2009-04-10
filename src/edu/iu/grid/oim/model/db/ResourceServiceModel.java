package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceAliasRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeRecord;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;

public class ResourceServiceModel extends SmallTableModelBase<ResourceServiceRecord> {
    static Logger log = Logger.getLogger(ResourceServiceModel.class); 

	public ResourceServiceModel(Authorization _auth) {
		super(_auth, "resource_service");
	}
	ResourceServiceRecord createRecord(ResultSet rs) throws SQLException
	{
		return new ResourceServiceRecord(rs);
	}
	public ResourceServiceRecord get(int service_id, int resource_id) throws SQLException {
		ResourceServiceRecord keyrec = new ResourceServiceRecord();
		keyrec.service_id = service_id;
		keyrec.resource_id = resource_id;
		return get(keyrec);
	}
	public ArrayList<ResourceServiceRecord> getAll() throws SQLException
	{
		ArrayList<ResourceServiceRecord> list = new ArrayList<ResourceServiceRecord>();
		for(RecordBase it : getCache()) {
			list.add((ResourceServiceRecord)it);
		}
		return list;
	}
	public ArrayList<ResourceServiceRecord> getAllByResourceID(int resource_id) throws SQLException
	{
		ArrayList<ResourceServiceRecord> list = new ArrayList<ResourceServiceRecord>();
		for(ResourceServiceRecord it : getAll()) {
			if(it.resource_id.compareTo(resource_id) == 0) {
				list.add(it);
			}
		}
		return list;		
	}
}
