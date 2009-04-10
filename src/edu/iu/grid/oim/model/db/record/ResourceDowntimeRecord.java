package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.SCModel;

public class ResourceDowntimeRecord extends RecordBase {

	@Key public Integer id;
	public Timestamp timestamp;
	public Timestamp start_time;
	public Timestamp end_time;
	public String downtime_summary;
	public Integer downtime_class_id;
	public Integer downtime_severity_id;
	public Integer resource_id;
	public Integer dn_id;
	public Boolean disable;
	
	//load from existing record
	public ResourceDowntimeRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public ResourceDowntimeRecord() {}
	
	public String getTitle() {
		return "Resource Downtime";
	}
	
	public ArrayList<String> getLables() {
		ArrayList<String> labels = new ArrayList();
		labels.add("resource_downtime");
		labels.add("resource_" + resource_id);
		return labels;
	}
	public String toString(Object field, Authorization auth)
	{
		if(field == null) return null;
		//TODO
		return field.toString();
	}
}
