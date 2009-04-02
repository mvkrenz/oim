package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.VOVORecord;

public class VOVOModel extends SmallTableModelBase<VOVORecord> {
    static Logger log = Logger.getLogger(VOVOModel.class);  
	
    public VOVOModel(
    		java.sql.Connection _con, 
    		edu.iu.grid.oim.lib.Authorization _auth) 
    {
    	super(_con, _auth, "vo_vo");
    }
    VOVORecord createRecord(ResultSet rs) throws SQLException
	{
		return new VOVORecord(rs);
	}
	public VOVORecord get(int id) throws SQLException {
		VOVORecord keyrec = new VOVORecord();
		keyrec.child_vo_id = id;
		return get(keyrec);
	}
}
