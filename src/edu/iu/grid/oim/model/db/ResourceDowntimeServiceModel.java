package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceAliasRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeServiceRecord;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;

public class ResourceDowntimeServiceModel extends SmallTableModelBase<ResourceDowntimeServiceRecord> {
    static Logger log = Logger.getLogger(ResourceDowntimeServiceModel.class); 

	public ResourceDowntimeServiceModel(Authorization _auth) {
		super(_auth, "resource_downtime_service");
	}
	ResourceDowntimeServiceRecord createRecord(ResultSet rs) throws SQLException
	{
		return new ResourceDowntimeServiceRecord(rs);
	}
	public Collection<ResourceDowntimeServiceRecord> getAll() throws SQLException
	{
		ArrayList<ResourceDowntimeServiceRecord> list = new ArrayList<ResourceDowntimeServiceRecord>();
		for(RecordBase it : getCache()) {
			list.add((ResourceDowntimeServiceRecord)it);
		}
		return list;
	}
	public Collection<ResourceDowntimeServiceRecord> getByDowntimeID(int downtime_id) throws SQLException
	{
		ArrayList<ResourceDowntimeServiceRecord> list = new ArrayList<ResourceDowntimeServiceRecord>();
		for(RecordBase it : getCache()) {
			ResourceDowntimeServiceRecord rec = (ResourceDowntimeServiceRecord)it;
			if(rec.resource_downtime_id.compareTo(downtime_id) == 0) {
				list.add(rec);
			}
		}
		return list;
	}
	public Collection<ResourceDowntimeServiceRecord> getAllByResourceID(int resource_id) throws SQLException 
	{
		ArrayList<ResourceDowntimeServiceRecord> list = new ArrayList<ResourceDowntimeServiceRecord>();
		
		//grap all downtime_id
		ResourceDowntimeModel rdmodel = new ResourceDowntimeModel(auth);
		Collection<ResourceDowntimeRecord> downtimes = rdmodel.getFutureDowntimesByResourceID(resource_id);
		for(ResourceDowntimeRecord downtime : downtimes) {
			list.addAll(getByDowntimeID(downtime.id));
		}
		
		return list;
	}
}
