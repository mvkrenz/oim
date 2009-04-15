package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.ResourceGroupModel;
import edu.iu.grid.oim.model.db.SCModel;

public class ResourceRecord extends RecordBase {

	@Key public Integer id;
	public String name;
	public String description;
	public String fqdn;
	public String url;
	public Integer resource_group_id;

	public Boolean active;
	public Boolean disable;
	
	//load from existing record
	public ResourceRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public ResourceRecord() {}
	
	public String getTitle() {
		return "Resource " + name;
	}
	public ArrayList<String> getLables() {
		ArrayList<String> labels = new ArrayList();
		labels.add("resource");
		labels.add("resource_"+id);
		labels.add("resource_group_"+resource_group_id);
		return labels;
	}
	public String toString(Object field, Authorization auth)
	{
		if(field == null) return null;
		try {
			if(field == resource_group_id) {
				ResourceGroupModel model = new ResourceGroupModel(auth);
				ResourceGroupRecord rec = model.get(resource_group_id);
				return rec.name;
			}
		} catch(SQLException e) {
			//forget it then..
		}
		return field.toString();
	}
}
