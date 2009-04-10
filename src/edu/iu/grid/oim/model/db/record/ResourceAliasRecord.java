package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.SCModel;

public class ResourceAliasRecord extends RecordBase {

	@Key public Integer resource_id;
	@Key public String resource_alias;
	
	//load from existing record
	public ResourceAliasRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public ResourceAliasRecord() {}
	
	public String getTitle() {
		return "Resource Alias ";
	}
	
	public ArrayList<String> getLables() {
		ArrayList<String> labels = new ArrayList();
		labels.add("resource_alias");
		labels.add("resource_"+resource_id);
		return labels;
	}
	public String toString(Object field, Authorization auth)
	{
		if(field == null) return null;
		try {
			if(field == resource_id) {
				ResourceModel model = new ResourceModel(auth);
				ResourceRecord rec = model.get(resource_id);
				return rec.name;
			}
		} catch(SQLException e) {
			//forget it then..
		}
		return field.toString();
	}
}
