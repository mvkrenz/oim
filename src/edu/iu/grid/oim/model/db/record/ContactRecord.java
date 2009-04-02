package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ContactRecord extends RecordBase {

	@Key public Integer id;
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
		super(rs);
	}
	//for creating new record
	public ContactRecord() {}
	/*
	public int compareKeysTo(RecordBase o) {
		if(this == o) return 0;
		ContactRecord you = (ContactRecord)o;
		if(id.compareTo(you.id) == 0) return 0;
		return 1;
	}
	*/
}
