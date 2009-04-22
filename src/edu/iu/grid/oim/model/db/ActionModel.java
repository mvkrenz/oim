package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.ActionRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

public class ActionModel extends SmallTableModelBase<ActionRecord> {
    static Logger log = Logger.getLogger(ActionModel.class);  
    
    public ActionModel(Authorization auth) 
    {
    	super(auth, "action");
    }
    public String getName()
    {
    	return "Action";
    }
    ActionRecord createRecord() throws SQLException
	{
		return new ActionRecord();
	}
	public ActionRecord get(int id) throws SQLException {
		ActionRecord keyrec = new ActionRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	public ArrayList<ActionRecord> getAll() throws SQLException
	{
		ArrayList<ActionRecord> list = new ArrayList<ActionRecord>();
		for(RecordBase it : getCache()) {
			list.add((ActionRecord)it);
		}
		return list;
	}
}