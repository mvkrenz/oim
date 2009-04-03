package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;

public class ResourceGroupModel extends SmallTableModelBase<ResourceGroupRecord> {
    static Logger log = Logger.getLogger(ResourceGroupModel.class); 

	public ResourceGroupModel(Authorization _auth) {
		super(_auth, "resource_group");
	}
	ResourceGroupRecord createRecord(ResultSet rs) throws SQLException
	{
		return new ResourceGroupRecord(rs);
	}
	public ResourceGroupRecord get(int id) throws SQLException {
		ResourceGroupRecord keyrec = new ResourceGroupRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	public ArrayList<ResourceGroupRecord> getAll() throws SQLException
	{
		ArrayList<ResourceGroupRecord> list = new ArrayList<ResourceGroupRecord>();
		for(RecordBase it : getCache()) {
			list.add((ResourceGroupRecord)it);
		}
		return list;
	}
}
