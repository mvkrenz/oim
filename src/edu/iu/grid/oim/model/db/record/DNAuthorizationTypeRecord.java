package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.ActionModel;
import edu.iu.grid.oim.model.db.AuthorizationTypeModel;
import edu.iu.grid.oim.model.db.DNModel;

public class DNAuthorizationTypeRecord extends RecordBase {

	@Key public Integer dn_id;
	@Key public Integer authorization_type_id;
	
	//load from existing record
	public DNAuthorizationTypeRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public DNAuthorizationTypeRecord() {}
	/*
	public String getTitle() {
		return "DN Authorization Type Record";
	}
	
	public ArrayList<String> getLables() {
		ArrayList<String> labels = new ArrayList();
		labels.add("dn_authorization_type");
		labels.add("dn_" + dn_id);
		return labels;
	}

	public String toString(Object field, Authorization auth)
	{
		if(field == null) return null;
		try {
			if(field == dn_id) {
				DNModel model = new DNModel(auth);
				DNRecord rec = model.get(dn_id);
				return rec.dn_string;
			} else if(field == authorization_type_id) {
				AuthorizationTypeModel model = new AuthorizationTypeModel(auth);
				AuthorizationTypeRecord rec = model.get(authorization_type_id);	
				return rec.name;
			}
		} catch(SQLException e) {
			//forget it then..
		}
		return field.toString();
	}
	*/
}
