package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

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
	
	//copy ctor.. I need this to disable a downtime
	public ResourceDowntimeRecord(ResourceDowntimeRecord copy) {
		id = copy.id;
		timestamp = copy.timestamp;
		start_time = copy.start_time;
		end_time = copy.end_time;
		downtime_summary = copy.downtime_summary;
		downtime_class_id = copy.downtime_class_id;
		downtime_severity_id = copy.downtime_severity_id;
		resource_id = copy.resource_id;
		dn_id = copy.dn_id;
		disable = copy.disable;
	}
	
	public ResourceDowntimeRecord(ResultSet rs) throws SQLException { 
		super(rs); 
	}
	
	//for creating new record
	public ResourceDowntimeRecord() {}
	/*
	public String getTitle() {
		return "Resource Downtime";
	}
	
	public ArrayList<String> getLables() {
		ArrayList<String> labels = new ArrayList();
		labels.add("resource_downtime");
		labels.add("resource_" + resource_id);
		return labels;
	}
	public String toString(Field field, Authorization auth)
	{
		if(field == null) return null;
		try {
			if(field.getName().compareTo("downtime_class_id") == 0) {
				DowntimeClassModel model = new DowntimeClassModel(auth);
				DowntimeClassRecord rec = model.get(downtime_class_id);
				return rec.name;
			} else if(field.getName().compareTo("downtime_severity_id") == 0) {
				DowntimeSeverityModel model = new DowntimeSeverityModel(auth);
				DowntimeSeverityRecord rec = model.get(downtime_severity_id);
				return rec.name;				
			} else if(field.getName().compareTo("resource_id") == 0) {
				ResourceModel model = new ResourceModel(auth);
				ResourceRecord rec = model.get(resource_id);
				return rec.name;					
			} else if(field.getName().compareTo("dn_id") == 0) {
				DNModel model = new DNModel(auth);
				DNRecord rec = model.get(dn_id);
				return rec.dn_string;						
			}
		} catch(SQLException e) {
			//forget it then..
		}
		return field.toString();
	}
	*/
}
