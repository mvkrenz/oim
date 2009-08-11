package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import edu.iu.grid.oim.lib.StaticConfig;

public class ContactRecord extends RecordBase implements Cloneable  {

	@Key public Integer id;
	public String name;
	public String primary_email, secondary_email;
	public String primary_phone, secondary_phone;
	public String primary_phone_ext, secondary_phone_ext;
	public String address_line_1, address_line_2;
	public String city, state, zipcode, country;
	public Boolean disable;
	public Boolean person;
	public String im;
	public String photo_url;
	public Integer submitter_dn_id;
	public String contact_preference;
	public String sms_address;
	public Timestamp confirmed;
	
	//load from existing record
	public ContactRecord(ResultSet rs) throws SQLException {
		super(rs);
	}
	//for creating new record
	public ContactRecord() {}
	public String getName() {
		return name;
	}
	public Boolean isPerson() {
		return person;
	}
	public String getFirstName()
	{
		if(name == null) return "";
		String[] tokens = name.split(" ");
		if(tokens.length < 2) {
			return name;
		}
		return tokens[0];
	}
	public String getLastName()
	{
		if(name == null) return "";
		String[] tokens = name.split(" ");
		if(tokens.length < 2) {
			return "";
		}
		String lastname = "";
		for(int i = 1; i < tokens.length; ++i) {
			if(lastname.length() != 0) {
				lastname += " ";
			}
			lastname += tokens[i];
		}
		return lastname;
	}
	public boolean isConfirmationExpired()
	{
		Date when = new Date();
		when.setTime(when.getTime()-1000L*3600*24*StaticConfig.getConfirmationExpiration());
		return confirmed.before(when);
	}
}
