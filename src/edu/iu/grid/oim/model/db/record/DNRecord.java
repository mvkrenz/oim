package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.AuthorizationTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNModel;

public class DNRecord extends RecordBase {

	@Key public Integer id;
	public String dn_string;
	public Integer contact_id;
	
	//load from existing record
	public DNRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public DNRecord() {}
	
	/*
	public String getTitle() {
		return "DN Record";
	}
	
	public ArrayList<String> getLables() {
		ArrayList<String> labels = new ArrayList();
		labels.add("dn");
		labels.add("dn_" + id);
		return labels;
	}

	public String toString(Object field, Authorization auth)
	{
		if(field == null) return null;
		try {
			if(field == contact_id) {
				ContactModel model = new ContactModel(auth);
				ContactRecord rec = model.get(contact_id);
				return rec.name;
			}
		} catch(SQLException e) {
			//forget it then..
		}
		return field.toString();
	}
	*/
}
