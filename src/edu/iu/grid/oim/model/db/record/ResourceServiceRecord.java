package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.ServiceModel;

public class ResourceServiceRecord extends RecordBase {

	@Key public Integer service_id;
	@Key public Integer resource_id;
	public String endpoint_override;
	public Boolean hidden;
	public Boolean central;
	public String server_list_regex;
	
	//load from existing record
	public ResourceServiceRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public ResourceServiceRecord() {}
	/*
	public String getTitle() {
		return "Resource Service";
	}
	
	public ArrayList<String> getLables() {
		ArrayList<String> labels = new ArrayList();
		labels.add("resource_service");
		labels.add("resource_" + resource_id);
		labels.add("resource_service_" + service_id);
		return labels;
	}
	public String toString(Object field, Authorization auth)
	{
		if(field == null) return null;
		try {
			if(field == service_id) {
				ServiceModel model = new ServiceModel(auth);
				ServiceRecord rec = model.get(service_id);
				return rec.name;
			} else if(field == resource_id) {
				ResourceModel model = new ResourceModel(auth);
				ResourceRecord rec = model.get(resource_id);
				return rec.name;				
			}
		} catch(SQLException e) {
			//forget it then..
		}
		return field.toString();
	}
	*/
}
