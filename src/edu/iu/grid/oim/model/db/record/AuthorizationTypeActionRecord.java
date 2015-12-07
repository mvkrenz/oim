package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.ActionModel;
import edu.iu.grid.oim.model.db.AuthorizationTypeModel;

public class AuthorizationTypeActionRecord extends RecordBase {

	@Key public Integer authorization_type_id;
	@Key public Integer action_id;
	
	//load from existing record
	public AuthorizationTypeActionRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public AuthorizationTypeActionRecord() {}
/*
	public String getTitle() {
		return "Authorization Matrix Record";
	}
	
	public ArrayList<String> getLables() {
		ArrayList<String> labels = new ArrayList();
		labels.add("authorization_matrix");
		return labels;
	}

	public String toString(Object field, Context context)
	{
		if(field == null) return null;
		try {
			if(field == authorization_type_id) {
				AuthorizationTypeModel model = new AuthorizationTypeModel(context);
				AuthorizationTypeRecord rec = model.get(authorization_type_id);
				return rec.name;
			} else if(field == action_id) {
				ActionModel model = new ActionModel(context);
				ActionRecord rec = model.get(action_id);	
				return rec.name;
			}
		} catch(SQLException e) {
			//forget it then..
		}
		return field.toString();
	}
*/
}
