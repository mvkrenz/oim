package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceAliasRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeRecord;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;

public class ResourceDowntimeModel extends SmallTableModelBase<ResourceDowntimeRecord> {
    static Logger log = Logger.getLogger(ResourceDowntimeModel.class); 

	public ResourceDowntimeModel(Authorization _auth) {
		super(_auth, "resource_downtime");
	}
	ResourceDowntimeRecord createRecord(ResultSet rs) throws SQLException
	{
		return new ResourceDowntimeRecord(rs);
	}
	public ResourceDowntimeRecord get(int id) throws SQLException {
		ResourceDowntimeRecord keyrec = new ResourceDowntimeRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	
	public Collection<ResourceDowntimeRecord> getAll() throws SQLException {
		ArrayList<ResourceDowntimeRecord> list = new ArrayList<ResourceDowntimeRecord>();
		for(RecordBase it : getCache()) {
			list.add((ResourceDowntimeRecord)it);
		}
		return list;
	}
	
	public Collection<ResourceDowntimeRecord> getFutureDowntimesByResourceID(int resource_id) throws SQLException
	{
		ArrayList<ResourceDowntimeRecord> list = new ArrayList<ResourceDowntimeRecord>();
		for(RecordBase it : getCache()) {
			ResourceDowntimeRecord rec = (ResourceDowntimeRecord)it;
			//search for downtime that ends in future.
			if(rec.resource_id == resource_id && rec.end_time.compareTo(new Date()) > 0) {
				list.add(rec);
			}
		}
		return list;
	}
}
