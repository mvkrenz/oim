package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.AuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.DowntimeClassRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.SiteRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class DowntimeClassModel extends SmallTableModelBase<DowntimeClassRecord> {
    static Logger log = Logger.getLogger(DowntimeClassModel.class);  
    
    public DowntimeClassModel(Authorization auth) 
    {
    	super(auth, "downtime_class");
    }
    DowntimeClassRecord createRecord() throws SQLException
	{
		return new DowntimeClassRecord();
	}
	public ArrayList<DowntimeClassRecord> getAll() throws SQLException
	{
		ArrayList<DowntimeClassRecord> list = new ArrayList<DowntimeClassRecord>();
		for(RecordBase it : getCache()) {
			list.add((DowntimeClassRecord)it);
		}
		return list;
	}
	public DowntimeClassRecord get(int id) throws SQLException {
		DowntimeClassRecord keyrec = new DowntimeClassRecord();
		keyrec.id = id;
		return get(keyrec);
	}
}