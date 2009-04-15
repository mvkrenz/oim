package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ResourceWLCGRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;

public class ResourceWLCGModel extends SmallTableModelBase<ResourceWLCGRecord> {
    static Logger log = Logger.getLogger(ResourceWLCGModel.class);  
	
    public ResourceWLCGModel(Authorization _auth) 
    {
    	super(_auth, "resource_wlcg");
    }
    ResourceWLCGRecord createRecord(ResultSet rs) throws SQLException
	{
		return new ResourceWLCGRecord(rs);
	}
	public ResourceWLCGRecord get(int id) throws SQLException {
		ResourceWLCGRecord keyrec = new ResourceWLCGRecord();
		keyrec.resource_id = id;
		return get(keyrec);
	}
	public ArrayList<ResourceWLCGRecord> getAll() throws SQLException
	{
		ArrayList<ResourceWLCGRecord> list = new ArrayList<ResourceWLCGRecord>();
		for(RecordBase it : getCache()) {
			list.add((ResourceWLCGRecord)it);
		}
		return list;
	}
}
