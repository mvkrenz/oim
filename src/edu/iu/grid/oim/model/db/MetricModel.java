package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.ActionRecord;
import edu.iu.grid.oim.model.db.record.MetricRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

public class MetricModel extends SmallTableModelBase<MetricRecord> {
    static Logger log = Logger.getLogger(MetricModel.class);  
    
    public MetricModel(Authorization auth) 
    {
    	super(auth, "metric");
    }
    MetricRecord createRecord(ResultSet rs) throws SQLException
	{
		return new MetricRecord(rs);
	}
	public MetricRecord get(int id) throws SQLException {
		MetricRecord keyrec = new MetricRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	public ArrayList<MetricRecord> getAll() throws SQLException
	{
		ArrayList<MetricRecord> list = new ArrayList<MetricRecord>();
		for(RecordBase it : getCache()) {
			list.add((MetricRecord)it);
		}
		return list;
	}
}