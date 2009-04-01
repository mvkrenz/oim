package edu.iu.grid.oim.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;

public class FieldOfScienceModel extends SmallTableModelBase<FieldOfScienceRecord> {
    static Logger log = Logger.getLogger(FieldOfScienceModel.class);  
	
    public FieldOfScienceModel(
    		java.sql.Connection _con, 
    		edu.iu.grid.oim.lib.Authorization _auth) 
    {
    	super(_con, _auth, "field_of_science");
    }
    FieldOfScienceRecord createRecord(ResultSet rs) throws SQLException
	{
		return new FieldOfScienceRecord(rs);
	}
}
