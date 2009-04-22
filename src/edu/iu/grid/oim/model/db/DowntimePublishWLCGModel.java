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
import edu.iu.grid.oim.model.db.record.DowntimePublishWLCGRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.SiteRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class DowntimePublishWLCGModel extends SmallTableModelBase<DowntimePublishWLCGRecord> {
    static Logger log = Logger.getLogger(DowntimePublishWLCGModel.class);  
    
    public DowntimePublishWLCGModel(Authorization auth) 
    {
    	super(auth, "downtime_publish_wlcg");
    }
    DowntimePublishWLCGRecord createRecord() throws SQLException
	{
		return new DowntimePublishWLCGRecord();
	}
	public ArrayList<DowntimePublishWLCGRecord> getAll() throws SQLException
	{
		ArrayList<DowntimePublishWLCGRecord> list = new ArrayList<DowntimePublishWLCGRecord>();
		for(RecordBase it : getCache()) {
			list.add((DowntimePublishWLCGRecord)it);
		}
		return list;
	}
	public DowntimePublishWLCGRecord get(int resource_downtime_id, int downtime_action_id) throws SQLException {
		DowntimePublishWLCGRecord keyrec = new DowntimePublishWLCGRecord();
		keyrec.resource_downtime_id = resource_downtime_id;
		keyrec.downtime_action_id = downtime_action_id;
		return get(keyrec);
	}
}