package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import edu.iu.grid.oim.lib.StaticConfig;

public class ContactRecord extends ConfirmableRecord {

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
	public String timezone;
	public String profile;
	public Boolean use_twiki;
	public String twiki_id;
	
	//public Integer quota_usercert_daymax; //3 - User can submit request upto this number
	public Integer count_usercert_year; //will be reset every day by cron
	
	//public Integer quota_hostcert_daymax; //50 - GridAdmin can approve upto this number per day
	public Integer count_hostcert_day; //will be reset every day by cron
	//public Integer quota_hostcert_yearmax; //1000 - GridAdmin can approve upto this number per year
	public Integer count_hostcert_year; //will be reset every year by cron
	
	//load from existing record
	public ContactRecord(ResultSet rs) throws SQLException {
		super(rs);
	}
	//for creating new record
	public ContactRecord() {
		timezone = "UTC";
		profile = "";
		confirmed = new Timestamp(Calendar.getInstance().getTimeInMillis());
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
}
