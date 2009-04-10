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
	public ArrayList<ResourceDowntimeServiceRecord> getAll() throws SQLException
	{
		ArrayList<ResourceDowntimeServiceRecord> list = new ArrayList<ResourceDowntimeServiceRecord>();
		for(RecordBase it : getCache()) {
			list.add((ResourceDowntimeServiceRecord)it);
		}
		return list;
	}
}
