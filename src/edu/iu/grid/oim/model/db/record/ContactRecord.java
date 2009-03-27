package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ContactRecord implements IRecord {

	public Integer id;
	public String name;
	public String primary_email, secondary_email;
	public String primary_phone, secondary_phone;
	public String primary_phone_ext, secondary_phone_ext;
	public String address_line_1, address_line_2;
	public String city, state, zipcode, country;
	public Boolean active;
	public Boolean disable;
	public Boolean person;
	public String contact_preference;
	
	//load from existing record
	public ContactRecord(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		
		name = rs.getString("name");
		
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
		person = rs.getBoolean("person");
		
		contact_preference = rs.getString("contact_preference");
		
	}
	
	//for creating new record
	public ContactRecord()
	{
	}
}
