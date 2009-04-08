package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.ActionModel;
import edu.iu.grid.oim.model.db.AuthorizationTypeModel;
import edu.iu.grid.oim.model.db.SCModel;

public class VORecord extends RecordBase 
{
	@Key public Integer id;
	public String name;
	public String long_name;
	public String description;
	public String primary_url;
	public String aup_url;
	public String membership_services_url;
	public String purpose_url;
	public String support_url;
	public String app_description;
	public String community;
	public Integer sc_id;
	public Boolean active;
	public Boolean disable;
	public String footprints_id;
	
	//load from existing record
	public VORecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public VORecord() {}
	
	public String getTitle() {
		return "VO " + name;
	}
	
	public ArrayList<String> getLables() {
		ArrayList<String> labels = new ArrayList();
		labels.add("vo");
		labels.add("vo_"+id);
		labels.add("sc_"+id);
		return labels;
	}
	public String toString(Object field, Authorization auth)
	{
		if(field == null) return null;
		try {
			if(field == sc_id) {
				SCModel model = new SCModel(auth);
				SCRecord rec = model.get(sc_id);
				return rec.name;
			}
		} catch(SQLException e) {
			//forget it then..
		}
		return field.toString();
	}
}
