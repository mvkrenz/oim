package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;


public class SiteRecord implements IRecord {

	public Integer id;
	public String name;
	public String long_name;
	public String description;
	public String address_line_1, address_line_2;
	public String city, state, zipcode, country;
	public String longitude, latitude; // Need to change this to float but it is text in the DB right now
	public Integer sc_id; 
	public Integer facility_id; 
	public Integer submitter_dn_id; 
	public Boolean active;
	public Boolean disable;
	
	//load from existing record
	public SiteRecord(ResultSet rs) throws SQLException {
		id          = rs.getInt("id");
		name        = rs.getString("name");
		long_name   = rs.getString("long_name");
		description = rs.getString("description");

		address_line_1 = rs.getString("address_line_1");
		address_line_2 = rs.getString("address_line_2");
		
		city    = rs.getString("city");
		state   = rs.getString("state");
		zipcode = rs.getString("zipcode");
		country = rs.getString("country");

		// Need to change this to float but it is text in the DB right now
		longitude = rs.getString("longitude");
		latitude  = rs.getString("latitude");
		
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
