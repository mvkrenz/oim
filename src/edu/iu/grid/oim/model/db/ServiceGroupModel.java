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
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.ServiceGroupRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;

public class ServiceGroupModel extends SmallTableModelBase<ServiceGroupRecord> {
    static Logger log = Logger.getLogger(ServiceGroupModel.class);  
	
    public ServiceGroupModel(Authorization _auth) 
    {
    	super(_auth, "service_group");
    }
    ServiceGroupRecord createRecord(ResultSet rs) throws SQLException
	{
		return new ServiceGroupRecord(rs);
	}
	public ServiceGroupRecord get(int id) throws SQLException {
		ServiceGroupRecord keyrec = new ServiceGroupRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	public ArrayList<ServiceGroupRecord> getAll() throws SQLException
	{
		ArrayList<ServiceGroupRecord> list = new ArrayList<ServiceGroupRecord>();
		for(RecordBase it : getCache()) {
			list.add((ServiceGroupRecord)it);
		}
		return list;
	}
}
