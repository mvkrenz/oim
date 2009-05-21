package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.mysql.jdbc.Field;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.DowntimeClassModel;
import edu.iu.grid.oim.model.db.DowntimeSeverityModel;
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
	
	//load from existing record
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
