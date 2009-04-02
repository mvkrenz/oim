package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.ActionRecord;

public class ActionModel extends SmallTableModelBase<ActionRecord> {
    static Logger log = Logger.getLogger(ActionModel.class);  
    
    public ActionModel(Authorization auth) 
    {
    	super(auth, "action");
    }
    ActionRecord createRecord(ResultSet rs) throws SQLException
	{
		return new ActionRecord(rs);
	}
	public ActionRecord get(int id) throws SQLException {
		ActionRecord keyrec = new ActionRecord();
		keyrec.id = id;
		return get(keyrec);
	}
}