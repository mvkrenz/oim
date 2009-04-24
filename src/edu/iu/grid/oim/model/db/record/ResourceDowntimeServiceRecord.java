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
import edu.iu.grid.oim.model.db.ServiceModel;

public class ResourceDowntimeServiceRecord extends RecordBase {

	@Key public Integer resource_downtime_id;
	@Key public Integer service_id;
	
	//load from existing record
	public ResourceDowntimeServiceRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public ResourceDowntimeServiceRecord() {}
	/*
	public String getTitle() {
		return "Resource Downtime Service";
	}
	
	public ArrayList<String> getLables() {
		ArrayList<String> labels = new ArrayList();
		labels.add("resource_downtime_" + resource_downtime_id);
		return labels;
	}
	public String toString(Object field, Authorization auth)
	{
		if(field == null) return null;
		try {
			if(field == service_id) {
				ServiceModel model = new ServiceModel(auth);
				ServiceRecord rec = model.get(service_id);
				return rec.description;
			}
		} catch(SQLException e) {
			//forget it then..
		}
		return field.toString();
	}
	*/
}
