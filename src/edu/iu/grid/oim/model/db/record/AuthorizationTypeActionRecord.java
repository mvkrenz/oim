package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthorizationTypeActionRecord extends RecordBase {

	@Key public Integer authorization_type_id;
	@Key public Integer action_id;
	
	//load from existing record
	public AuthorizationTypeActionRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public AuthorizationTypeActionRecord() {}
}
