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
import edu.iu.grid.oim.model.db.record.SCRecord;

public class FacilityModel extends SmallTableModelBase<FacilityRecord> {
    static Logger log = Logger.getLogger(FacilityModel.class);  
	
    public FacilityModel(Authorization _auth) 
    {
    	super(_auth, "facility");
    }
    FacilityRecord createRecord(ResultSet rs) throws SQLException
	{
		return new FacilityRecord(rs);
	}
	public FacilityRecord get(int id) throws SQLException {
		FacilityRecord keyrec = new FacilityRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	public ArrayList<FacilityRecord> getAll() throws SQLException
	{
		ArrayList<FacilityRecord> list = new ArrayList<FacilityRecord>();
		for(RecordBase it : getCache()) {
			list.add((FacilityRecord)it);
		}
		return list;
	}
}
