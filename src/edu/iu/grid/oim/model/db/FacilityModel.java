package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.FacilityRecord;

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
}
