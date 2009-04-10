package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceAliasRecord;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;

public class ResourceAliasModel extends SmallTableModelBase<ResourceAliasRecord> {
    static Logger log = Logger.getLogger(ResourceAliasModel.class); 

	public ResourceAliasModel(Authorization _auth) {
		super(_auth, "resource_alias");
	}
	ResourceAliasRecord createRecord(ResultSet rs) throws SQLException
	{
		return new ResourceAliasRecord(rs);
	}
	public ArrayList<ResourceAliasRecord> getAll() throws SQLException
	{
		ArrayList<ResourceAliasRecord> list = new ArrayList<ResourceAliasRecord>();
		for(RecordBase it : getCache()) {
			list.add((ResourceAliasRecord)it);
		}
		return list;
	}
	public ArrayList<ResourceAliasRecord> getAllByResourceID(int resource_id) throws SQLException
	{
		ArrayList<ResourceAliasRecord> list = new ArrayList<ResourceAliasRecord>();
		for(ResourceAliasRecord it : getAll()) {
			if(it.resource_id.compareTo(resource_id) == 0) {
				list.add(it);
			}
		}
		return list;		
	}
}
