package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PersonRecord implements IRecord {

	public Integer id;
	public String first_name;
	public String middle_name;
	public String last_name;
	public String primary_email, secondary_email;
	public String primary_phone, secondary_phone;
	public String primary_phone_ext, secondary_phone_ext;
	public String address_line_1, address_line_2;
	public String city, state, zipcode, country;
	public Boolean active;
	public Boolean disable;
	public String contact_preference;
	
	//load from existing record
	public PersonRecord(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		
		first_name = rs.getString("first_name");
		middle_name = rs.getString("middle_name");
		last_name = rs.getString("last_name");
		
		primary_email = rs.getString("primary_email");
		secondary_email = rs.getString("secondary_email");
		
		primary_phone = rs.getString("primary_phone");
		secondary_phone = rs.getString("secondary_phone");
		
		primary_phone_ext = rs.getString("primary_phone_ext");
		secondary_phone_ext = rs.getString("secondary_phone_ext");
		
		address_line_1 = rs.getString("address_line_1");
		address_line_2 = rs.getString("address_line_2");
		
		city = rs.getString("city");
		state = rs.getString("state");
		zipcode = rs.getString("zipcode");
		country = rs.getString("country");
		
		active = rs.getBoolean("active");
		disable = rs.getBoolean("disable");
		
		contact_preference = rs.getString("contact_preference");
		
	}
	
	//for creating new record
	public PersonRecord()
	{
	}
	
	public String getFullName()
	{
		return first_name + " " + last_name;
	}
}
