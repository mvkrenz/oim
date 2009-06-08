package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.SCModel;

public class SiteRecord extends RecordBase 
{
	@Key public Integer id;
	public String name;
	public String long_name;
	public String description;
	public String address_line_1, address_line_2;
	public String city, state, zipcode, country;
	public Double longitude, latitude; // Need to change this to float but it is text in the DB right now
	public Integer sc_id; 
	public Integer facility_id; 
	public Integer submitter_dn_id; 
	public Boolean active;
	public Boolean disable;
	
	//load from existing record
	public SiteRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public SiteRecord()	throws SQLException {}
	public String getName() {
		return name;
	}
	/*
	public String getTitle() {
		return "Site " + name;
	}
	
	public ArrayList<String> getLables() {
		ArrayList<String> labels = new ArrayList();
		labels.add("site");
		labels.add("site_"+id);
		labels.add("sc_"+sc_id);
		labels.add("facility_"+facility_id);
		labels.add("submitter_dn_id_"+submitter_dn_id);
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
			} else if(field == facility_id) {
				FacilityModel model = new FacilityModel(auth);
				FacilityRecord rec = model.get(facility_id);
				return rec.name;				
			} else if(field == submitter_dn_id) {
				DNModel model = new DNModel(auth);
				DNRecord rec = model.get(submitter_dn_id);
				return rec.dn_string;						
			}
		} catch(SQLException e) {
			//forget it then..
		}
		return field.toString();
	}
	*/
}
