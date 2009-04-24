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
	/*	
	public String getTitle() {
		return "Contact Information for " + name;
	}
	

	public ArrayList<String> getLables() {
		ArrayList<String> labels = new ArrayList();
		labels.add("contact");
		labels.add("contact_"+id);
		labels.add("dn_"+submitter_dn_id);
		return labels;
	}
	
	public String toString(Object field, Authorization auth)
	{
		if(field == null) return null;
		try {
			if(field == submitter_dn_id) {
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
