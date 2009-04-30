package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.ContactRankModel;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.SCModel;

public class ContactRecord extends RecordBase {

	@Key public Integer id;
	public String name;
	@Restricted public String primary_email, secondary_email;
	@Restricted public String primary_phone, secondary_phone;
	@Restricted public String primary_phone_ext, secondary_phone_ext;
	@Restricted public String address_line_1, address_line_2;
	public String city, state, zipcode, country;
	public Boolean active;
	public Boolean disable;
	public Boolean person;
	public Integer submitter_dn_id;
	@Restricted public String contact_preference;
	
	//load from existing record
	public ContactRecord(ResultSet rs) throws SQLException {
		super(rs);
	}
	//for creating new record
	public ContactRecord() {}
	
	public String getFirstName()
	{
		String[] tokens = name.split(" ");
		if(tokens.length < 2) {
			return name;
		}
		return tokens[0];
	}
	public String getLastName()
	{
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
