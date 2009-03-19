package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;


public class SiteRecord implements IRecord {

	public Integer id;
	public String name;
	public String long_name;
	public String description;
	// Should we declare a new address class object? -ag
	public Integer sc_id; 
	public Integer facility_id; 
	public Integer submitter_dn_id; 
	public Boolean active;
	public Boolean disable;
	
	//load from existing record
	public SiteRecord(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		name = rs.getString("name");
		long_name = rs.getString("long_name");
		description = rs.getString("description");
		// Address stuff not there yet including latitude/longitude
		sc_id = rs.getInt("sc_id");
		facility_id = rs.getInt("facility_id");
		submitter_dn_id = rs.getInt("submitter_dn_id");
		active = rs.getBoolean("active");
		disable = rs.getBoolean("disable");
	}
	
	//for creating new record
	public SiteRecord()	throws SQLException { 
	}
}
