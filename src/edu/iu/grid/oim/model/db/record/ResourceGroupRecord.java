package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.OsgGridTypeModel;
import edu.iu.grid.oim.model.db.ResourceGroupModel;
import edu.iu.grid.oim.model.db.SiteModel;

public class ResourceGroupRecord extends RecordBase {

	@Key public Integer id;
	public String name;
	public String description;
	public Integer site_id;
	public Integer osg_grid_type_id;

	public Boolean active;
	public Boolean disable;
	
	//load from existing record
	public ResourceGroupRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public ResourceGroupRecord() {}
	/*
	public String getTitle() {
		return "Resource Group : " + name;
	}
	
	public ArrayList<String> getLables() {
		ArrayList<String> labels = new ArrayList();
		labels.add("resource_group");
		labels.add("resource_group_"+id);
		labels.add("site_" + site_id);
		return labels;
	}
	public String toString(Object field, Authorization auth)
	{
		if(field == null) return null;
		try {
			if(field == site_id) {
				SiteModel model = new SiteModel(auth);
				SiteRecord rec = model.get(site_id);
				return rec.name;
			} else if (field == osg_grid_type_id) {
				OsgGridTypeModel model = new OsgGridTypeModel(auth);
				OsgGridTypeRecord rec = model.get(osg_grid_type_id);
				return rec.name;				
			}
		} catch(SQLException e) {
			//forget it then..
		}
		return field.toString();
	}
	*/
}
