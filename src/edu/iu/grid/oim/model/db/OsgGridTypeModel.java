package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.model.db.record.NotificationRecord;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
import edu.iu.grid.oim.lib.Authorization;

public class OsgGridTypeModel extends SmallTableModelBase<OsgGridTypeRecord> {
    static Logger log = Logger.getLogger(OsgGridTypeModel.class);  
	
    public OsgGridTypeModel(Authorization _auth) 
    {
    	super(_auth, "osg_grid_type");
    }
    OsgGridTypeRecord createRecord(ResultSet rs) throws SQLException
	{
		return new OsgGridTypeRecord(rs);
	}
}
