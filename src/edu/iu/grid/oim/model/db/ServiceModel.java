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
import edu.iu.grid.oim.model.db.record.ServiceRecord;

public class ServiceModel extends SmallTableModelBase<ServiceRecord> {
    static Logger log = Logger.getLogger(ServiceModel.class);  
	
    public ServiceModel(Authorization _auth) 
    {
    	super(_auth, "service");
    }
    ServiceRecord createRecord(ResultSet rs) throws SQLException
	{
		return new ServiceRecord(rs);
	}
	public ServiceRecord get(int id) throws SQLException {
		ServiceRecord keyrec = new ServiceRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	public ArrayList<ServiceRecord> getAll() throws SQLException
	{
		ArrayList<ServiceRecord> list = new ArrayList<ServiceRecord>();
		for(RecordBase it : getCache()) {
			list.add((ServiceRecord)it);
		}
		return list;
	}
}
