package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import edu.iu.grid.oim.lib.Authorization;
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
	@Restricted public String contact_preference;
	
	//load from existing record
	public ContactRecord(ResultSet rs) throws SQLException {
		super(rs);
	}
	//for creating new record
	public ContactRecord() {}
	
	public String getTitle() {
		return "Contact Information for " + name;
	}
	
	public ArrayList<String> getLables() {
		ArrayList<String> labels = new ArrayList();
		labels.add("contact");
		labels.add("contact_"+id);
		return labels;
	}
}
