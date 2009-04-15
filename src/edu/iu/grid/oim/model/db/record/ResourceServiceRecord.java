package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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
}
